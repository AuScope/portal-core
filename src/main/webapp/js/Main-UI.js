//this runs on DOM load - you can access all the good stuff now.
var theglobalexml;
//var host = "http://localhost:8080";
//Ext.Ajax.timeout = 180000; //3 minute timeout for ajax calls

//A global instance of GMapInfoWindowManager that helps to open GMap info windows
var mapInfoWindowManager = null;

Ext.onReady(function() {
    var map;
    var formFactory = new FormFactory();
    var searchBarThreshold = 6; //how many records do we need to have before we show a search bar

    //Converts an array of BBox records into an actual BBox object array
    var convertBboxList = function(v, record) {
        for (var i = 0; i < v.length; i++) {
            v[i] = new BBox(v[i].northBoundLatitude,
            		v[i].southBoundLatitude,
            		v[i].eastBoundLongitude,
            		v[i].westBoundLongitude);
        }
            
        return v;
    };
    
    //-----------Complex Features Panel Configurations
    var complexFeaturesStore = new Ext.data.Store({
        proxy: new Ext.data.HttpProxy(new Ext.data.Connection({url: '/getComplexFeatures.do', timeout:180000})),
        reader: new Ext.data.ArrayReader({}, [
            {   name: 'title'           },
            {   name: 'description'     },
            {   name: 'contactOrgs'     },
            {   name: 'proxyURL'        },
            {   name: 'serviceType'     },
            {   name: 'id'              },
            {   name: 'typeName'        },
            {   name: 'serviceURLs'     },
            {   name: 'layerVisible'    },
            {   name: 'loadingStatus'   },
            {   name: 'iconImgSrc'      },
            {   name: 'iconUrl'         },
            {   name: 'dataSourceImage' },
            {   name: 'bboxes', convert : convertBboxList},
            {	name: 'iconAnchor'		},
            {	name: 'infoWindowAnchor'},
            {   name: 'iconSize'        }
        ]),
        sortInfo: {field:'title', direction:'ASC'}
    });

    var complexFeaturesRowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{description} </p><br>')
    });
    
    //Checks if the given bounding box is meaningful
    //That is it will represent a bounding box that is useful for the end user to visualize
    var isBBoxListMeaningful = function(bboxList) {
    	if (!bboxList)
    		return false;
    	
    	if (bboxList.length == 0) {
    		return false;
    	}
    	
    	
    	var meaningful = false;
    	for (var i = 0; i < bboxList.length && !meaningful; i++) {
    		bbox = bboxList[i];
    		
    		//A bbox that covers the entire planet is not useful
    		meaningful =!bbox.isGlobal();
    	}
    	
    	return meaningful;
    };
    
    //Returns true if the current records intersects the GMap viewport (based on its bounding box)
    //False otherwise
    var visibleRecordsFilter = function(record) {
    	var bboxList = record.get('bboxes');
		if (!isBBoxListMeaningful(bboxList)) {
			return false;
		}
		
		var bbox = bboxList[0];
    	
    	var sw = new GLatLng(bbox.southBoundLatitude, bbox.westBoundLongitude);
    	var ne = new GLatLng(bbox.northBoundLatitude, bbox.eastBoundLongitude);
    	var layerBounds = new GLatLngBounds(sw,ne);
    	
    	var visibleBounds = map.getBounds();
    	return visibleBounds.intersects(layerBounds);
    };

    var complexFeaturesPanel = new Ext.grid.GridPanel({
        stripeRows       : true,
        autoExpandColumn : 'title',
        plugins          : [ complexFeaturesRowExpander ],
        viewConfig       : {scrollOffset: 0, forceFit:true},
        title            : 'Feature Layers',
        region           :'north',
        split            : true,
        height           : 160,
        //width: 80,
        autoScroll       : true,
        store            : complexFeaturesStore,
        columns: [
            complexFeaturesRowExpander,
            {
                id:'title',
                header: "Title",
                width: 80,
                sortable: true,
                dataIndex: 'title'
            },{
            	id:'search',
            	header: '',
            	width: 45,
            	dataIndex: 'bboxes',
            	resizable: false,
            	menuDisabled: true,
            	sortable: false,
            	fixed: true,
            	renderer: function (value) {
            		//Only show the icon if we have a meaningful bounding box 
            		if (isBBoxListMeaningful(value))
            			return '<img src="img/magglass.gif"/>';
            		else
            			return '';
            	}
            }
        ],
        bbar: [{
            text:'Add Layer to Map',
            tooltip:'Add Layer to Map',
            iconCls:'add',
            pressed: true,
            handler: function() {
                var recordToAdd = complexFeaturesPanel.getSelectionModel().getSelected();

                //Only add if the record isn't already there
                if (activeLayersStore.findExact("id",recordToAdd.get("id")) < 0) {
                    //add to active layers (At the top of the Z-order)
                    activeLayersStore.insert(0, [recordToAdd]);
                    
                    //invoke this layer as being checked
                    activeLayerCheckHandler(complexFeaturesPanel.getSelectionModel().getSelected(), true);
                }
                
                //set this record to selected
                activeLayersPanel.getSelectionModel().selectRecords([recordToAdd], false);
            }
        }],
        
        tbar: [
               'Search: ', ' ',
               new Ext.ux.form.ClientSearchField({
                   store: complexFeaturesStore,
                   width:200,
                   id:'search-complex',
                   fieldName:'title'
               }), {
              	   	xtype:'button',
               	   	text:'Visible',
               	   	handler:function() {
               	   		var searchPanel = Ext.getCmp('search-complex');
               	   		searchPanel.runCustomFilter('<visible layers>', visibleRecordsFilter);
                  		}
               }
           ]
    });
        

    //----------- WMS Layers Panel Configurations

    var wmsLayersStore = new Ext.data.GroupingStore({
        proxy: new Ext.data.HttpProxy({url: '/getWMSLayers.do'}),
        reader: new Ext.data.ArrayReader({}, [
            {   name: 'title'           },
            {   name: 'description'     },
            {   name: 'contactOrg'      },
            {   name: 'proxyURL'        },
            {   name: 'serviceType'     },
            {   name: 'id'              },
            {   name: 'typeName'        },
            {   name: 'serviceURLs'     },
            {   name: 'layerVisible'    },
            {   name: 'loadingStatus'   },
            {   name: 'dataSourceImage' },
            {   name: 'opacity'         },
            {   name: 'bboxes', convert : convertBboxList}
        ]),
        groupField:'contactOrg',
        sortInfo: {field:'title', direction:'ASC'}
    });

    var wmsLayersRowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{description}</p><br>')
    });

    var wmsLayersPanel = new Ext.grid.GridPanel({
        stripeRows       : true,
        autoExpandColumn : 'title',
        plugins          : [ wmsLayersRowExpander ],
        viewConfig       : {scrollOffset: 0, forceFit:true},
        title            : 'Map Layers',
        region           :'north',
        split            : true,
        height           : 160,
        autoScroll       : true,
        store            : wmsLayersStore,
        columns: [
            wmsLayersRowExpander,
            {
                id:'title',
                header: "Title",
                sortable: true,
                dataIndex: 'title'
            }, {
            	id:'search',
            	header: '',
            	width: 45,
            	dataIndex: 'bboxes',
            	resizable: false,
            	menuDisabled: true,
            	sortable: false,
            	fixed: true,
            	renderer: function (value) {
            		//Only show the icon if we have a meaningful bounding box 
            		if (isBBoxListMeaningful(value))
            			return '<img src="img/magglass.gif"/>';
            		else
            			return '';
            	}
            },{
                id:'contactOrg',
                header: "Provider",
                width: 160,
                sortable: true,
                dataIndex: 'contactOrg',
                hidden:true
            }
        ],
        bbar: [{
            text:'Add Layer to Map',
            tooltip:'Add Layer to Map',
            iconCls:'add',
            pressed:true,
            handler: function() {
                var recordToAdd = wmsLayersPanel.getSelectionModel().getSelected();

                //Only add if the record isn't already there
                if (activeLayersStore.findExact("id",recordToAdd.get("id")) < 0) {                
                    //add to active layers (At the top of the Z-order)
                    activeLayersStore.insert(0, [recordToAdd]);
                    
                    //invoke this layer as being checked
                    activeLayerCheckHandler(wmsLayersPanel.getSelectionModel().getSelected(), true);
                }

                //set this record to selected
                activeLayersPanel.getSelectionModel().selectRecords([recordToAdd], false);
            }
        }],
        
        view: new Ext.grid.GroupingView({
            forceFit:true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
        }),
        tbar: [
               'Search: ', ' ',
               new Ext.ux.form.ClientSearchField({
                   store: wmsLayersStore,
                   width:200,
                   id:'search-wms-panel',
                   fieldName:'title'
               }), {
            	   	xtype:'button',
            	   	text:'Visible',
            	   	handler:function() {
            	   		var searchPanel = Ext.getCmp('search-wms-panel');
            	   		searchPanel.runCustomFilter('<visible layers>', visibleRecordsFilter);
               		}
               }
           ]

    });

  //----------- WCS Layers Panel Configurations

    var wcsLayersStore = new Ext.data.GroupingStore({
        proxy: new Ext.data.HttpProxy({url: 'getWCSLayers.do'}),
        reader: new Ext.data.ArrayReader({}, [
            {   name: 'title'           },
            {   name: 'description'     },
            {   name: 'contactOrg'      },
            {   name: 'proxyURL'        },
            {   name: 'serviceType'     },
            {   name: 'id'              },
            {   name: 'typeName'        },
            {   name: 'serviceURLs'     },
            {   name: 'openDapURLs'     },
            {   name: 'wmsURLs'     	},
            {   name: 'opacity'         },
            {   name: 'layerVisible'    },
            {   name: 'loadingStatus'   },
            {   name: 'dataSourceImage' },
            {   name: 'bboxes', convert : convertBboxList}
        ]),
        groupField:'contactOrg',
        sortInfo: {field:'title', direction:'ASC'}
    });

    var wcsLayersRowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{description}</p><br>')
    });

    var wcsLayersPanel = new Ext.grid.GridPanel({
        stripeRows       : true,
        autoExpandColumn : 'title',
        plugins          : [ wcsLayersRowExpander ],
        viewConfig       : {scrollOffset: 0, forceFit:true},
        title            : 'Coverage Layers',
        region           :'north',
        split            : true,
        height           : 160,
        autoScroll       : true,
        store            : wcsLayersStore,
        columns: [
            wcsLayersRowExpander,
            {
                id:'title',
                header: "Title",
                sortable: true,
                dataIndex: 'title'
            }, {
            	id:'search',
            	header: '',
            	width: 45,
            	dataIndex: 'bboxes',
            	resizable: false,
            	menuDisabled: true,
            	sortable: false,
            	fixed: true,
            	renderer: function (value) {
            		//Only show the icon if we have a meaningful bounding box 
            		if (isBBoxListMeaningful(value))
            			return '<img src="img/magglass.gif"/>';
            		else
            			return '';
            	}
            },{
                id:'contactOrg',
                header: "Provider",
                width: 160,
                sortable: true,
                dataIndex: 'contactOrg',
                hidden:true
            }
        ],
        bbar: [{
            text:'Add Layer to Map',
            tooltip:'Add Layer to Map',
            iconCls:'add',
            pressed:true,
            handler: function() {
                var recordToAdd = wcsLayersPanel.getSelectionModel().getSelected();

                //Only add if the record isn't already there
                if (activeLayersStore.findExact("id",recordToAdd.get("id")) < 0) {                
                    //add to active layers (At the top of the Z-order)
                    activeLayersStore.insert(0, [recordToAdd]);
                    
                    //invoke this layer as being checked
                    activeLayerCheckHandler(wcsLayersPanel.getSelectionModel().getSelected(), true);
                }

                //set this record to selected
                activeLayersPanel.getSelectionModel().selectRecords([recordToAdd], false);
            }
        }],
        
        view: new Ext.grid.GroupingView({
            forceFit:true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
        }),
        tbar: [
               'Search: ', ' ',
               new Ext.ux.form.ClientSearchField({
                   store: wcsLayersStore,
                   width:200,
                   id:'search-wcs-panel',
                   fieldName:'title'
               }), {
            	   	xtype:'button',
            	   	text:'Visible',
            	   	handler:function() {
            	   		var searchPanel = Ext.getCmp('search-wcs-panel');
            	   		searchPanel.runCustomFilter('<visible layers>', visibleRecordsFilter);
               		}
               }
           ]

    });
    
    //------ Custom Layers

    var customLayersStore = new Ext.data.Store({
        proxy: new Ext.data.HttpProxy({url: '/getCustomLayers.do'}),
        //baseParams : {service_URL : 'fullName' }, // one parameter, column, which is set to ‘fullNname’
        baseParams : { service_URL : '' },
        reader: new Ext.data.ArrayReader({}, [
            {   name: 'title'           },
            {   name: 'description'     },
            {   name: 'contactOrg'      },
            {   name: 'proxyURL'        },
            {   name: 'serviceType'     },
            {   name: 'id'              },
            {   name: 'typeName'        },
            {   name: 'serviceURLs'     },
            {   name: 'layerVisible'    },
            {   name: 'loadingStatus'   },
            {   name: 'dataSourceImage' },
            {   name: 'opacity'         },
            {   name: 'bboxes' 			}
        ]),
        groupField:'contactOrg',
        sortInfo: {field:'title', direction:'ASC'}
    });

    var customLayersRowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{description}</p><br>')
    });
    
    //----------- Search Panel
    var customLayersPanel = new Ext.grid.GridPanel({
        stripeRows       : true,
        autoExpandColumn : 'title',
        plugins          : [ customLayersRowExpander ],
        viewConfig       : {scrollOffset: 0, forceFit:true},
        title            : 'Custom Layers',
        region           :'north',
        split            : true,
        height           : 160,
        //width: 80,
        autoScroll       : true,
        store            : customLayersStore,
        loadMask         : true,
        columns: [
            customLayersRowExpander,
            {
                id:'title',
                header: "Title",
                sortable: true,
                dataIndex: 'title'
            },{
                id:'contactOrg',
                header: "Provider",
                width: 160,
                sortable: true,
                dataIndex: 'contactOrg',
                hidden:true
            }, {
            	id:'search',
            	header: '',
            	width: 45,
            	dataIndex: 'bboxes',
            	resizable: false,
            	menuDisabled: true,
            	sortable: false,
            	fixed: true,
            	renderer: function (value) {
            		//Only show the icon if we have a meaningful bounding box 
            		if (isBBoxListMeaningful(value))
            			return '<img src="img/magglass.gif"/>';
            		else
            			return '';
            	}
            }
        ],
        tbar: [
            '<span style="color:#15428B; font-weight:bold">Enter WMS Url: </span>',
            ' ',
            new Ext.ux.form.SearchTwinTriggerField({
                store: customLayersStore,
                width:260,
                name : 'STTField',
                emptyText : 'http://'
            })
        ],
        bbar: [{
            text:'Add Layer to Map',
            tooltip:'Add Layer to Map',
            iconCls:'add',
            pressed: true,
            region :'south',
            handler: function() {
                var recordToAdd = customLayersPanel.getSelectionModel().getSelected();

                //Only add if the record isn't already there
                if (activeLayersStore.findExact("id",recordToAdd.get("id")) < 0) {
                    //add to active layers (At the top of the Z-order)
                    activeLayersStore.insert(0, [recordToAdd]);
                    
                    //invoke this layer as being checked
                    activeLayerCheckHandler(customLayersPanel.getSelectionModel().getSelected(), true);
                }

                //set this record to selected
                activeLayersPanel.getSelectionModel().selectRecords([recordToAdd], false);
            }
        }]
    });
    
    //Returns an object
    //{
    //    bboxSrs : 'EPSG:4326'
    //    lowerCornerPoints : [numbers]
    //    upperCornerPoints : [numbers]
    //}
    var fetchVisibleMapBounds = function(gMapInstance) {
    	var mapBounds = gMapInstance.getBounds();
		var sw = mapBounds.getSouthWest();
		var ne = mapBounds.getNorthEast();
		var center = mapBounds.getCenter();
		
		var adjustedSWLng = sw.lng(); 
		var adjustedNELng = ne.lng();
		
		//this is so we can fetch data when our bbox is crossing the anti meridian
		//Otherwise our bbox wraps around the WRONG side of the planet
		if (adjustedSWLng <= 0 && adjustedNELng >= 0 || 
			adjustedSWLng >= 0 && adjustedNELng <= 0) {
			adjustedSWLng = (sw.lng() < 0) ? (180 - sw.lng()) : sw.lng();
			adjustedNELng = (ne.lng() < 0) ? (180 - ne.lng()) : ne.lng();
		}
		
		return {
				bboxSrs : 'EPSG:4326',
				lowerCornerPoints : [Math.min(adjustedSWLng, adjustedNELng), Math.min(sw.lat(), ne.lat())],
				upperCornerPoints : [Math.max(adjustedSWLng, adjustedNELng), Math.max(sw.lat(), ne.lat())]
		};
    };
    
    var filterButton = new Ext.Button({
        text     :'Apply Filter >>',
        tooltip  :'Apply Filter',
        disabled : true,
        handler  : function() {
            var selectedRecord = activeLayersPanel.getSelectionModel().getSelected();
            wfsHandler(selectedRecord);
        }
    });

    /**
     * Used to show extra details for querying services
     */
    var filterPanel = new Ext.Panel({
        title: "Filter Properties",
        region: 'south',
        split: true,
        layout: 'card',
        activeItem: 0,
        height: 200,
        autoScroll  : true,
        layoutConfig: {
            layoutOnCardChange: true// Important when not specifying an items array
        },
        items: [
            {
                html: '<p style="margin:15px;padding:15px;border:1px dotted #999;color:#555;background: #f9f9f9;"> Filter options will be shown here for special services.</p>'
            }
        ],
        bbar: ['->', filterButton]
    });

    var activeLayersStore = new Ext.data.Store({
        reader: new Ext.data.ArrayReader({}, [
            {   name:'title'            },
            {   name:'description'      },
            {   name:'proxyURL'         },
            {   name: 'serviceType'     },
            {   name: 'id'              },
            {   name: 'typeName'        },
            {   name: 'serviceURLs'     },
            {   name: 'layerVisible'    },
            {   name: 'loadingStatus'   },
            {   name: 'iconImgSrc'      },
            {   name: 'iconUrl'         },
            {   name: 'dataSourceImage' }
        ])
    });

    /**
     *Iterates through the activeLayersStore and updates each WMS layer's Z-Order to is position within the store
     *
     *This function will refresh every WMS layer too
     */
    var updateActiveLayerZOrder = function() {
        //Update the Z index for each WMS item in the store
        for (var i = 0; i < activeLayersStore.getCount(); i++) {
            var record = activeLayersStore.getAt(i);

            if (record.tileOverlay && (record.get('serviceType') == 'wms' || record.get('serviceType') == 'wcs')) {
                if (record.get('layerVisible') == true) {
                	var newZOrder = activeLayersStore.getCount() - i;
                	if (record.tileOverlay instanceof OverlayManager) {
                		record.tileOverlay.updateZOrder(newZOrder);
                	} else {
	                    record.tileOverlay.zPriority = newZOrder;
	                    map.removeOverlay(record.tileOverlay);
	                    map.addOverlay(record.tileOverlay);
                	}
                }
            }
        }
    };

    /**
     *@param forceApplyFilter (Optional) if set AND isChecked is set AND this function has a filter panel, it will force the current filter to be loaded
     */
    var activeLayerCheckHandler = function(record, isChecked, forceApplyFilter) {
        //set the record to be selected if checked
        activeLayersPanel.getSelectionModel().selectRecords([record], false);

        if (record.get('loadingStatus') == '<img src="js/external/extjs/resources/images/default/grid/loading.gif">') {
        	record.set('layerVisible', !isChecked); //reverse selection
            Ext.MessageBox.show({
                title: 'Please wait',
                msg: "There is an operation in process for this layer. Please wait until it is finished.",
                buttons: Ext.MessageBox.OK,
                animEl: 'mb9',
                icon: Ext.MessageBox.INFO
            });
            return;
        }
        
        record.set('layerVisible', isChecked);

        if (isChecked) {
            //Create our filter panel or use the existing one
            if (record.filterPanel == null) {
                record.filterPanel = formFactory.getFilterForm(record, map);
            } else if (forceApplyFilter && !filterButton.disabled) {
                filterButton.handler(); //If we are using an existing one, we may need to retrigger it's filter
            }
            
            //Hide show filter panel based on WMS / WFS type
            //WFS layers will NOT load immediately if they have a filter type (they wait on the filterButton to be clicked)
            //WFS layers will load normally if they DONT have a filter type
            //WMS layers will ALWAYS load normally
            //WCS layers will ALWAYS load normally
            if (record.get('serviceType') == 'wms') {
                if (record.filterPanel != null) {
                    filterPanel.add(record.filterPanel);
                    filterPanel.getLayout().setActiveItem(record.get('id'));
                    filterPanel.doLayout();
                }

                wmsHandler(record);
            } else if (record.get('serviceType') == 'wfs') {
                if (record.filterPanel != null) {
                    filterPanel.add(record.filterPanel);
                    filterPanel.getLayout().setActiveItem(record.get('id'));
                    filterButton.enable();
                    filterButton.toggle(true);
                    filterPanel.doLayout();
                } else {
                    filterPanel.getLayout().setActiveItem(0);

                    wfsHandler(record);
                }
            } else if (record.get('serviceType') == 'wcs') {
            	if (record.filterPanel != null) {
                    filterPanel.add(record.filterPanel);
                    filterPanel.getLayout().setActiveItem(record.get('id'));
                    filterPanel.doLayout();
                }
            	
                wcsHandler(record);
            }
        } else {
            if (record.get('serviceType') == 'wfs') {
                if (record.tileOverlay instanceof OverlayManager) record.tileOverlay.clearOverlays();
            } else if (record.get('serviceType') == 'wcs') {
            	if (record.tileOverlay instanceof OverlayManager) record.tileOverlay.clearOverlays();
            } else if (record.get('serviceType') == 'wms') {
                //remove from the map
                map.removeOverlay(record.tileOverlay);
            }

            filterPanel.getLayout().setActiveItem(0);
            filterButton.disable();
        }
    };
    
    //The WCS handler will create a representation of a coverage on the map for a given WCS record
    //If we have a linked WMS url we should use that (otherwise we draw an ugly red bounding box)
    var wcsHandler = function(selectedRecord) {
    	
        if (selectedRecord.tileOverlay instanceof OverlayManager) 
        	selectedRecord.tileOverlay.clearOverlays();
        
        var serviceUrlList = selectedRecord.get('serviceURLs');
        var wmsObjList = selectedRecord.get('wmsURLs');
        var serviceUrl = serviceUrlList[0]; //assume a single service url
        
        selectedRecord.responseTooltip = new ResponseTooltip();
        var bboxList = selectedRecord.get('bboxes');
        if (!bboxList || bboxList.length == 0) {
        	selectedRecord.responseTooltip.addResponse(serviceUrl, 'No bounding box has been specified for this coverage.');
        	return;
        }
        
        if (!selectedRecord.tileOverlay)
        	selectedRecord.tileOverlay = new OverlayManager(map);
        
        //We will need to add the bounding box polygons regardless of whether we have a WMS service or not.
        //The difference is that we will make the "WMS" bounding box polygons transparent but still clickable
        var polygonList = null;
        if (wmsObjList.length > 0) {
        	polygonList = bboxToPolygon(bboxList[0],'#000000', 0, 0.0,'#000000', 0.0);
        } else {
        	polygonList = bboxToPolygon(bboxList[0],'#FF0000', 0, 0.7,'#FF0000', 0.6);
        }
        
        //Add our polygons (they may/may not be visible)
        for (var i = 0; i < polygonList.length; i++) {
        	polygonList[i].layerName = selectedRecord.get('typeName');
        	polygonList[i].wcsUrl = serviceUrl;
        	polygonList[i].parentRecord = selectedRecord;
	        
	        selectedRecord.tileOverlay.addOverlay(polygonList[i]);
        }
        
        //Add our WMS tiles (if any)
        for (var i = 0; i < wmsObjList.length; i++) {
        	var tileLayer = new GWMSTileLayer(map, new GCopyrightCollection(""), 1, 17);
            tileLayer.baseURL = wmsObjList[i].url;
            tileLayer.layers = wmsObjList[i].name;
            tileLayer.opacity = 1.0;

            selectedRecord.tileOverlay.addOverlay(new GTileLayerOverlay(tileLayer));
        }
        
        //This will update the Z order of our WMS layers
        updateActiveLayerZOrder();
    };

    var wfsHandler = function(selectedRecord) {
        //if there is already updateCSWRecords filter running for this record then don't call another
        if (selectedRecord.get('loadingStatus') == '<img src="js/external/extjs/resources/images/default/grid/loading.gif">') {
            Ext.MessageBox.show({
                title: 'Please wait',
                msg: "There is an operation in process for this layer. Please wait until it is finished.",
                buttons: Ext.MessageBox.OK,
                animEl: 'mb9',
                icon: Ext.MessageBox.INFO
            });
            return;
        }

        if (selectedRecord.tileOverlay instanceof OverlayManager) selectedRecord.tileOverlay.clearOverlays();

        //a response status holder
        selectedRecord.responseTooltip = new ResponseTooltip();

        var serviceURLs = selectedRecord.get('serviceURLs');
        var proxyURL = selectedRecord.get('proxyURL');
        var iconUrl = selectedRecord.get('iconUrl');

        var finishedLoadingCounter = serviceURLs.length;
        // var markerOverlay = new MarkerOverlay();

        var overlayManager = new OverlayManager(map);
        selectedRecord.tileOverlay = overlayManager;

        //set the status as loading for this record
        selectedRecord.set('loadingStatus', '<img src="js/external/extjs/resources/images/default/grid/loading.gif">');

        var filterParameters = '';
        if (filterPanel.getLayout().activeItem == filterPanel.getComponent(0)) {
        	filterParameters = "&typeName=" + selectedRecord.get('typeName'); 
        } else {
        	filterParameters = filterPanel.getLayout().activeItem.getForm().getValues(true);
        	
        	// Uncomment this to add bbox support AUS-1597 
        	//filterParameters += '&bbox=' + Ext.util.JSON.encode(fetchVisibleMapBounds(map));
        }
        filterParameters += '&maxFeatures=200'; // limit our feature request to 200 so we don't overwhelm the browser
        
        for (var i = 0; i < serviceURLs.length; i++) {
            handleQuery(serviceURLs[i], selectedRecord, proxyURL, iconUrl, overlayManager, filterParameters, function() {
                //decrement the counter
                finishedLoadingCounter--;

                //check if we can set the status to finished
                if (finishedLoadingCounter <= 0) {
                    selectedRecord.set('loadingStatus', '<img src="js/external/extjs/resources/images/default/grid/done.gif">');
                }
            });
        }
    };

    var handleQuery = function(serviceUrl, selectedRecord, proxyURL, iconUrl, overlayManager, filterParameters, finishedLoadingHandler) {
        selectedRecord.responseTooltip.addResponse(serviceUrl, "Loading...");
        GDownloadUrl(proxyURL + '?' + filterParameters + '&serviceUrl=' + serviceUrl, function(data, responseCode) {
            if (responseCode == 200) {
                var jsonResponse = eval('(' + data + ')');
                if (jsonResponse.success) {

                	var icon = new GIcon(G_DEFAULT_ICON, iconUrl);   
                	
                	var iconSize = selectedRecord.get('iconSize');
                	if(iconSize) {
                        icon.iconSize = new GSize(iconSize.width, iconSize.height);
                	}

                	var iconAnchor = selectedRecord.get('iconAnchor');
                    if(iconAnchor) {
                    	icon.iconAnchor = new GPoint(iconAnchor.x, iconAnchor.y);
                    }

                	var infoWindowAnchor = selectedRecord.get('infoWindowAnchor');
                    if(infoWindowAnchor) {
                    	icon.infoWindowAnchor = new GPoint(infoWindowAnchor.x, infoWindowAnchor.y);
                    }

                	//TODO: This is a hack to remove marker shadows. Eventually it should be 
                    // put into an external config file or become a session-based preference.
                	icon.shadow = null;
                	
                    //Parse our KML
                    var parser = new KMLParser(jsonResponse.data.kml);
                    parser.makeMarkers(icon, function(marker) {
                        marker.typeName = selectedRecord.get('typeName');
                        marker.wfsUrl = serviceUrl;
                        marker.parentRecord = selectedRecord;
                    });
                    
                    var markers = parser.markers;
                    var overlays = parser.overlays;
                    
                    //Add our single points and overlays
                    overlayManager.markerManager.addMarkers(markers, 0);
                    for(var i = 0; i < overlays.length; i++) {
                    	overlayManager.addOverlay(overlays[i]);
                    }
                    overlayManager.markerManager.refresh();

                    //store the gml for later download needs
                    selectedRecord.gml = jsonResponse.data.gml;

                    //store the status
                    selectedRecord.responseTooltip.addResponse(serviceUrl, (markers.length + overlays.length) + " records retrieved.");
                } else {
                    //store the status
                    selectedRecord.responseTooltip.addResponse(serviceUrl, jsonResponse.msg);
                }
                //markerOverlay.addList(markers);
            }else if(responseCode == -1) {
                selectedRecord.responseTooltip.addResponse(serviceUrl, "Data request timed out. Please try again later.");
            } else if ((responseCode >= 400) & (responseCode < 500)){
                alert('Request not found, bad request or similar problem. Error code is: ' + responseCode);
            } else if ((responseCode >= 500) & (responseCode <= 506)){
                alert('Requested service not available, not implemented or internal service error. Error code is: ' + responseCode);
            } else {
                alert('Remote server returned error code: ' + responseCode);
            } 

            //we are finito
            finishedLoadingHandler();
        });
    };

    var wmsHandler = function(record) {
        var tileLayer = new GWMSTileLayer(map, new GCopyrightCollection(""), 1, 17);
        tileLayer.baseURL = record.get('serviceURLs')[0];
        tileLayer.layers = record.get('typeName');

        tileLayer.opacity = record.get('opacity');

        //TODO: remove code specific to feature types and styles specific to GSV
        if (record.get('typeName') == 'gsmlGeologicUnit') {
            tileLayer.styles = 'ColorByLithology';
        }

        record.tileOverlay = new GTileLayerOverlay(tileLayer);

        //This will handle adding the WMS layer (as well as updating the Z-Order)
        updateActiveLayerZOrder();
    };

    var activeLayerSelectionHandler = function(sm, index, record) {
        //if its not checked then don't do any actions
        if (record.get('layerVisible') == false) {
            filterPanel.getLayout().setActiveItem(0);
            filterButton.disable();
        } else if (record.filterPanel != null) {
            //if filter panel already exists then show it
            filterPanel.getLayout().setActiveItem(record.get('id'));

            if (record.get('serviceType') == 'wfs') {
                filterButton.enable();
                filterButton.toggle(true);
            } else if (record.get('serviceType') == 'wms') {
                filterButton.disable();
            } else if (record.get('serviceType') == 'wcs') {
            	filterButton.disable();
            }

        } else {
            //if this type doesnt need a filter panel then just show the default filter panel
            filterPanel.getLayout().setActiveItem(0);
            filterButton.disable();
        }
    };

    // custom column plugin example
    var activeLayersPanelCheckColumn = new Ext.ux.grid.EventCheckColumn({
        header: "Visible",
        dataIndex: 'layerVisible',
        width: 30,
        handler: function(record, data) {
            activeLayerCheckHandler(record,data,true);
        }
    });

    var activeLayersPanelExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{description}</p><br>')
    });

    var activeLayersRemoveButton = {
                text:'Remove Layer',
                tooltip:'Remove Layer',
                iconCls:'remove',
                pressed:true,
                handler: function() {
                    var record = activeLayersPanel.getSelectionModel().getSelected();
                    if (record == null)
                        return;

                    if (record.get('loadingStatus') == '<img src="js/external/extjs/resources/images/default/grid/loading.gif">') {
                        Ext.MessageBox.show({
                            title: 'Please wait',
                            msg: "There is an operation in process for this layer. Please wait until it is finished.",
                            buttons: Ext.MessageBox.OK,
                            animEl: 'mb9',
                            icon: Ext.MessageBox.INFO
                        });
                        return;
                    }

                    if (record.get('serviceType') == 'wfs') {
                        if (record.tileOverlay instanceof OverlayManager) { 
                        	record.tileOverlay.clearOverlays();
                        }
                    } else if (record.get('serviceType') == 'wms') {
                        //remove from the map
                        map.removeOverlay(record.tileOverlay);
                    } else if (record.get('serviceType') == 'wcs') {
                        if (record.tileOverlay instanceof OverlayManager) { 
                        	record.tileOverlay.clearOverlays();
                        }
                    }
                    //remove from the map
                    //map.removeOverlay(activeLayersPanel.getSelectionModel().getSelected().tileOverlay);

                    //remove it from active layers
                    activeLayersStore.remove(record);

                    //set the filter panels active item to 0
                    filterPanel.getLayout().setActiveItem(0);
                }
            };

    var activeLayersRowDragPlugin = new Ext.ux.dd.GridDragDropRowOrder({
                    copy: false, 
                    scrollable: true,
                    listeners: {afterrowmove: updateActiveLayerZOrder}
                 });

    this.activeLayersPanel = new Ext.grid.GridPanel({
        plugins: [activeLayersPanelCheckColumn, 
                  activeLayersPanelExpander,
                  activeLayersRowDragPlugin],

        stripeRows: true,
        autoExpandColumn: 'title',
        viewConfig: {scrollOffset: 0,forceFit:true},
        title: 'Active Layers',
        region:'center',
        split: true,
        height: '100',
        ddText:'',
        //autoScroll: true,
        store: activeLayersStore,
        layout: 'fit',
        columns: [
            activeLayersPanelExpander,
            {
                id:'iconImgSrc',
                header: "",
                width: 18,
                sortable: false,
                dataIndex: 'iconImgSrc',
                align: 'center'
            },
            {
                id:'loadingStatus',
                header: "",
                width: 25,
                sortable: false,
                dataIndex: 'loadingStatus',
                align: 'center'
            },
            {
                id:'title',
                header: "Title",
                width: 100,
                sortable: true,
                dataIndex: 'title'
            },
            activeLayersPanelCheckColumn,
            {
                id:'dataSourceImage',
                header: "",
                width: 20,
                sortable: false,
                dataIndex: 'dataSourceImage',
                align: 'center'
            }
        ],
        bbar: [
            activeLayersRemoveButton
        ],

        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true,
            listeners: {
                rowselect: {
                    fn: activeLayerSelectionHandler
                }
            }
        })
    });

    /**
     * Tooltip for the active layers
     */
    var activeLayersToolTip;

    /**
     * Handler for mouse over events on the active layers panel, things like server status, and download buttons
     */
    this.activeLayersPanel.on('mouseover', function(e, t) {
        e.stopEvent();

        var row = e.getTarget('.x-grid3-row');
        var col = e.getTarget('.x-grid3-col');

        //if there is no visible tooltip then create one, if on is visible already we dont want to layer another one on top
        if (col != null && (activeLayersToolTip == null || !activeLayersToolTip.isVisible())) {

            //get the actual data record
            var theRow = activeLayersPanel.getView().findRow(row);
            var record = activeLayersPanel.getStore().getAt(theRow.rowIndex);

            //this is the status icon column
            if (col.cellIndex == '2') {
                var html = 'No status has been recorded.';

                if (record.responseTooltip != null)
                    html = record.responseTooltip.getHtml();

                activeLayersToolTip = new Ext.ToolTip({
                    target: e.target ,
                    title: 'Status Information',
                    autoHide : true,
                    html: html ,
                    anchor: 'bottom',
                    trackMouse: true,
                    showDelay:60,
                    autoHeight:true,
                    autoWidth: true
                });
            }
            //this is the column for download link icons
            else if (col.cellIndex == '5') {
                var serviceType = record.get('serviceType');
                //var html = 'Download layer data.';

                /*if (serviceType == 'wms') { //if a WMS
                    html = 'Click here to view this layers Image in new browser window.';
                } else if (serviceType == 'wfs') {//if a WFS
                    html = 'Click here to view this layers GML in new browser window.';
                }*/

                activeLayersToolTip = new Ext.ToolTip({
                    target: e.target ,
                    //title: 'Status Information',
                    autoHide : true,
                    html: 'Download data for this layer.' ,
                    anchor: 'bottom',
                    trackMouse: true,
                    showDelay:60,
                    autoHeight:true,
                    autoWidth: true
                });
            }
        }
    });

    /**
     * Handler for click events on the active layers panel, used for the  
     * new browser window popup which shows the GML or WMS image
     */
    this.activeLayersPanel.on('click', function(e, t) {
        e.stopEvent();

        var row = e.getTarget('.x-grid3-row');
        var col = e.getTarget('.x-grid3-col');

        // if there is no visible tooltip then create one, if on is 
        // visible already we don't want to layer another one on top
        if (col != null) {

            //get the actual data record
            var theRow = activeLayersPanel.getView().findRow(row);
            var record = activeLayersPanel.getStore().getAt(theRow.rowIndex);
            
            //this is the column for download link icons
            if (col.cellIndex == '5') {
            	var serviceType = record.get('serviceType');
                var serviceUrls = record.get('serviceURLs');
                var keys = [serviceUrls.length];
                var values = [serviceUrls.length];
                
                if (serviceType == 'wms') { //if a WMS, open a new window calling the download controller
                    if (serviceUrls.length >= 1) {

                        for (i = 0; i < serviceUrls.length; i++) {

                            var boundBox = (map.getBounds().getSouthWest().lng() < 0 ? map.getBounds().getSouthWest().lng() + 360.0 : map.getBounds().getSouthWest().lng()) + "," +
                                           map.getBounds().getSouthWest().lat() + "," +
                                           (map.getBounds().getNorthEast().lng() < 0 ? map.getBounds().getNorthEast().lng() + 360.0 : map.getBounds().getNorthEast().lng()) + "," +
                                           map.getBounds().getNorthEast().lat();

                            var url = serviceUrls[i];
                                                          
                            var last_char = url.charAt(url.length - 1);
                            if ((last_char !== "?") && (last_char !== "&")) {
                                if (url.indexOf('?') == -1) {
                                    url += "?";
                                } else {
                                    url += "&";
                                }
                            }
                             
                            url += "REQUEST=GetMap";
                            url += "&SERVICE=WMS";
                            url += "&VERSION=1.1.0";
                            url += "&LAYERS=" + record.get('typeName');
                            if (this.styles)
                                url += "&STYLES=" + this.styles;
                            else
                                url += "&STYLES="; //Styles parameter is mandatory, using a null string ensures default style  
                            /*
                             if (this.sld)
                             url += "&SLD=" + this.sld;*/
                            url += "&FORMAT=" + "image/png";
                            url += "&BGCOLOR=0xFFFFFF";
                            url += "&TRANSPARENT=TRUE";
                            url += "&SRS=" + "EPSG:4326";
                            url += "&BBOX=" + boundBox;
                            url += "&WIDTH=" + map.getSize().width;
                            url += "&HEIGHT=" + map.getSize().height;

                            keys[i] = 'serviceUrls';
                            values[i] = url;
                        }
                        //alert("downloadProxy?" + url);
                        openWindowWithPost("downloadWMSAsZip.do?", 'WMS_Layer_Download_'+new Date().getTime(), keys, values);
                    }

                } else if (serviceType == 'wfs') {//if a WFS open a new window calling the download controller

                    if (serviceUrls.length >= 1) {
                        var filterParameters = filterPanel.getLayout().activeItem == filterPanel.getComponent(0) ? "&typeName=" + record.get('typeName') : filterPanel.getLayout().activeItem.getForm().getValues(true);

                        for (i = 0; i < serviceUrls.length; i++) {
                            //urlsParameter += "serviceUrls=" + serviceUrls[i] + filterParameters.replace('&', '%26') + '&';
                            keys[i] = 'serviceUrls';
                            values[i] =  window.location.protocol + "//" + window.location.host + record.get('proxyURL') + "?" + filterParameters + "&serviceUrl=" + serviceUrls[i];
                        }

                        openWindowWithPost("downloadGMLAsZip.do?", 'WFS_Layer_Download_'+new Date().getTime(), keys, values);
                    }
                } else if (serviceType == 'wcs') {
                	//Lets open the generic wcs download handler
                	showWCSDownload(record.get('serviceURLs')[0], record.get('typeName'));
                }
            }
        }
    });

    this.activeLayersPanel.on('cellcontextmenu', function(grid, rowIndex, colIndex, event) {
        //Stop the event propogating
        event.stopEvent();

        //Ensure the row that is right clicked gets selected
        activeLayersPanel.getSelectionModel().selectRow(rowIndex);

        //Create the context menu to hold the buttons
        var contextMenu = new Ext.menu.Menu();
        contextMenu.add(activeLayersRemoveButton);

        //Show the menu
        contextMenu.showAt(event.getXY());
    });
    
    /**
     * Opens a new window to the specified URL and passes URL parameters like so keys[x]=values[x]
     *
     * @param {String} url
     * @param {String} name
     * @param {Array}  keys
     * @param {Array} values
     */
    var openWindowWithPost = function(url, name, keys, values)
    {
        if (keys && values && (keys.length == values.length)) {
            for (var i = 0; i < keys.length; i++) {
                url += '&' + keys[i] + '=' + escape(values[i]);
            }
        }
        downloadFile(url);
    };

    //downloads given specified file.
    downloadFile = function(url) {
        var body = Ext.getBody();
        var frame = body.createChild({
            tag:'iframe',
            cls:'x-hidden',
            id:'iframe',
            name:'iframe'
        });
        var form = body.createChild({
            tag:'form',
            cls:'x-hidden',
            id:'form',
            target:'iframe',
            method:'POST'
        });
        form.dom.action = url;
        form.dom.submit();
    }
    
    // basic tabs 1, built from existing content
    var tabsPanel = new Ext.TabPanel({
        //width:450,
        activeTab: 0,
        region:'north',
        split: true,
        height: 225,
        autoScroll: true,
        //autosize:true,
        items:[
            complexFeaturesPanel,
            wmsLayersPanel,
            wcsLayersPanel,
            customLayersPanel
        ]
    });

    /**
     * Used as a placeholder for the tree and details panel on the left of screen
     */
    var westPanel = {
        layout: 'border',
        region:'west',
        border: false,
        split:true,
        //margins: '100 0 0 0',
        margins:'100 0 0 3',
        width: 380,
        items:[tabsPanel , activeLayersPanel, filterPanel]
    };

    /**
     * This center panel will hold the google maps
     */
    var centerPanel = new Ext.Panel({
        region: 'center', 
        id: 'center_region', 
        margins: '100 0 0 0', 
        cmargins:'100 0 0 0'
    });

    /**
     * Used for notifications of activity
     *
    var statusBar = new Ext.StatusBar({
        region: "south",
        id: 'my-status',
        hidden: true,

        // defaults to use when the status is cleared:
        defaultText: 'Default status text',
        defaultIconCls: 'default-icon',

        // values to set initially:
        text: 'Ready',
        iconCls: 'ready-icon'
    });
    */

    /**
     * Add all the panels to the viewport
     */
    var viewport = new Ext.Viewport({
        layout:'border',
        items:[westPanel, centerPanel]
    });

    // Is user's browser suppported by Google Maps?
    if (GBrowserIsCompatible()) {

        map = new GMap2(centerPanel.body.dom);
        
        /* TODO:    AUS-1526
        // Two ways of enabling search bar      
        map = new GMap2( centerPanel.body.dom
                       , {googleBarOptions:{ showOnLoad : true//,
                          //resultList:G_GOOGLEBAR_RESULT_LIST_SUPPRESS//,
                                             //onMarkersSetCallback : myCallback
                                            }
                        });
        or ... */
        
        map.enableGoogleBar();
        /*        
        // Problems, find out how to
        1. turn out advertising
        2. Narrow down location seraches to the current map view 
                        (or Australia). Search for Albany retruns Albany, US
        */
        
        map.setUIToDefault();
        
        //add google earth
        map.addMapType(G_SATELLITE_3D_MAP);

        // Large pan and zoom control
        //map.addControl(new GLargeMapControl(),  new GControlPosition(G_ANCHOR_TOP_LEFT));

        // Toggle between Map, Satellite, and Hybrid types
        map.addControl(new GMapTypeControl());

        var startZoom = 4;
        map.setCenter(new google.maps.LatLng(-26, 133.3), startZoom);
        map.setMapType(G_SATELLITE_MAP);

        //Thumbnail map
        var Tsize = new GSize(150, 150);
        map.addControl(new GOverviewMapControl(Tsize));

        map.addControl(new DragZoomControl(), new GControlPosition(G_ANCHOR_TOP_RIGHT, new GSize(345, 7)));
        
        mapInfoWindowManager = new GMapInfoWindowManager(map);
    }

    // Fix for IE/Firefox resize problem (See issue AUS-1364 and AUS-1565 for more info)
    map.checkResize();
    centerPanel.on('resize', function() {
        map.checkResize();
    });
    
    //updateCSWRecords dud gloabal for geoxml class
    theglobalexml = new GeoXml("theglobalexml", map, null, null);

    //event handlers and listeners
    //tree.on('click', function(node, event) { treeNodeOnClickController(node, event, viewport, filterPanel); });
    //tree.on('checkchange', function(node, isChecked) { treeCheckChangeController(node, isChecked, map, statusBar, viewport, downloadUrls, filterPanel); });

    //when updateCSWRecords person clicks on updateCSWRecords marker then do something
    GEvent.addListener(map, "click", function(overlay, latlng) {
        gMapClickController(map, overlay, latlng, activeLayersStore);
    });

    GEvent.addListener(map, "mousemove", function(latlng){
        var latStr = "<b>Long:</b> " + latlng.lng().toFixed(6)
                   + "&nbsp&nbsp&nbsp&nbsp"
                   + "<b>Lat:</b> " + latlng.lat().toFixed(6);
    	document.getElementById("latlng").innerHTML = latStr;
    });

    GEvent.addListener(map, "mouseout", function(latlng){
        document.getElementById("latlng").innerHTML = "";
    });
    
    new Ext.LoadMask(tabsPanel.el, {msg: 'Please Wait...', store: wmsLayersStore});
    //new Ext.LoadMask(complexFeaturesPanel.el, {msg: 'Please Wait...', store: complexFeaturesStore});
    //new Ext.LoadMask(wmsLayersPanel.el, {msg: 'Please Wait...', store: wmsLayersStore});

    complexFeaturesStore.load();
    wmsLayersStore.load();
    wcsLayersStore.load();
    
    //param bbox The bounding box to split
    //param okBboxList The list of boxes that the split boxes will be appended to
    var splitBboxes = function(bbox, okBboxList) {

    	//SPLIT CASE 1: Polygon crossing meridian
    	if (bbox.westBoundLongitude < 0 && bbox.eastBoundLongitude > 0) {
    		var splits = bbox.splitAt(0); 
    		for (var i = 0; i < splits.length; i++) {
    			splitBboxes(splits[i], okBboxList);
    		}
    		return okBboxList;
    	}
    	
    	//SPLIT CASE 2: Polygon crossing anti meridian
    	if (bbox.westBoundLongitude < 0 && bbox.eastBoundLongitude > 0) {
    		var splits = bbox.splitAt(-180); 
    		for (var i = 0; i < splits.length; i++) {
    			splitBboxes(splits[i], okBboxList);
    		}
    		return okBboxList;
    	}
    	
    	//SPLIT CASE 3: Polygon is too wide (Gmap can't handle click events for wide polygons)
    	if (Math.abs(bbox.westBoundLongitude - bbox.eastBoundLongitude) > 60) {
    		var splits = bbox.splitAt((bbox.westBoundLongitude + bbox.eastBoundLongitude) / 2); 
    		for (var i = 0; i < splits.length; i++) {
    			splitBboxes(splits[i], okBboxList);
    		}
    		return okBboxList;
    	}
    	
    	//OTHERWISE - bounding box is OK to render
    	okBboxList.push(bbox);
    	return okBboxList; 
    };
    
    //Converts a portal bbox into an array of GMap Polygon's
    //Normally a single polygon is returned but if the polygon wraps around the antimeridian, it will be split
    //into 2 polygons
    var bboxToPolygon = function(bbox, strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity, opts) {
    	
    	var splits = splitBboxes(bbox, []);
    	var result = [];
    	
    	for (var i = 0; i < splits.length; i++) {
    		var splitBbox = splits[i];
    		var ne = new GLatLng(splitBbox.northBoundLatitude, splitBbox.eastBoundLongitude);
    	    var se = new GLatLng(splitBbox.southBoundLatitude, splitBbox.eastBoundLongitude);
    	    var sw = new GLatLng(splitBbox.southBoundLatitude, splitBbox.westBoundLongitude);
    	    var nw = new GLatLng(splitBbox.northBoundLatitude, splitBbox.westBoundLongitude);
    	    
    	    result.push(new GPolygon([sw, nw, ne, se, sw], strokeColor, strokeWeight, strokeOpacity, fillColor, fillOpacity, opts));
    	}
    	
    	return result;
    };
    
    //Generates a bounding box polygon and puts it on the map for the given record
    var showRecordBoundingBox = function (grid, rowIndex, colIndex, e) {
    	var record = grid.getStore().getAt(rowIndex); 
        var fieldName = grid.getColumnModel().getDataIndex(colIndex); 
        if (fieldName !== 'bboxes') {
        	return;
        }
    	
    	var bboxes = record.get('bboxes');
    	if (!isBBoxListMeaningful(bboxes)) {
    		return;
    	}
    	
    	if (record.bboxOverlayManager) {
    		record.bboxOverlayManager.clearOverlays();
    		record.bboxOverlayManager = null;
    	}
    	
    	var overlayManager = new OverlayManager(map);
    	record.bboxOverlayManager = overlayManager;
    	
    	for (var i = 0; i < bboxes.length; i++) {    	    
    	    var polygonList = bboxToPolygon(bboxes[i],'00FF00', 0, 0.7,'#00FF00', 0.6);
    	    
    	    for (var j = 0; j < polygonList.length; j++) {
    	    	polygonList[j].title = 'bbox';
    	    	record.bboxOverlayManager.addOverlay(polygonList[j]);
    	    }
    	}
    	
    	record.bboxOverlayManager.markerManager.refresh();
    	
    	//Make the bbox disappear after a short while 
    	if (!record.bboxOverlayClearTask) {
	    	record.bboxOverlayClearTask = new Ext.util.DelayedTask(function(){
	    		hideRecordBoundingBox(record);
	    	});
    	}

    	record.bboxOverlayClearTask.delay(2000); 
    };
    
    //Hides a bounding box polygon
    var hideRecordBoundingBox = function (record) {
    	//var record = grid.getStore().getAt(rowIndex); 
    		
        if (record.bboxOverlayManager) {
        	record.bboxOverlayManager.clearOverlays();
        	record.bboxOverlayManager = null;
        }
        
        record.bboxOverlayClearTask = null;
    };
    
    //Moves the GMap portlet display to display the bounding box
    var moveToBoundingBox = function (grid, rowIndex, colIndex, e) {
    	var record = grid.getStore().getAt(rowIndex); 
        var fieldName = grid.getColumnModel().getDataIndex(colIndex); 
        if (fieldName !== 'bboxes') {
        	return;
        }
    	
    	var bboxes = record.get('bboxes');
    	if (!isBBoxListMeaningful(bboxes)) {
    		return;
    	}
    	
    	//Problems : It doesn't take into account multiple bounding boxes
    	//           It fails with bounding boxes spanning the anti-meridian (the layer centre fails).
    	var bbox = bboxes[0];
    	
    	var sw = new GLatLng(bbox.southBoundLatitude, bbox.westBoundLongitude);
    	var ne = new GLatLng(bbox.northBoundLatitude, bbox.eastBoundLongitude);
    	var layerBounds = new GLatLngBounds(sw,ne);
    	
    	//Adjust zoom if required
    	var visibleBounds = map.getBounds();
    	map.setZoom(map.getBoundsZoomLevel(layerBounds));
    	
    	//Pan to position
    	var layerCenter = layerBounds.getCenter();
    	map.panTo(layerCenter);
    	
    	
    	
    };
    
    complexFeaturesPanel.on("cellclick", showRecordBoundingBox, complexFeaturesPanel.on);
    wmsLayersPanel.on("cellclick", showRecordBoundingBox, wmsLayersPanel);
    wcsLayersPanel.on("cellclick", showRecordBoundingBox, wcsLayersPanel);
    
    complexFeaturesPanel.on("celldblclick", moveToBoundingBox, complexFeaturesPanel);
    wmsLayersPanel.on("celldblclick", moveToBoundingBox, wmsLayersPanel);
    wcsLayersPanel.on("celldblclick", moveToBoundingBox, wcsLayersPanel);
    
});