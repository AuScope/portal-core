//this runs on DOM load - you can access all the good stuff now.
var theglobalexml;
//var host = "http://localhost:8080";
//Ext.Ajax.timeout = 180000; //3 minute timeout for ajax calls

//A global instance of GMapInfoWindowManager that helps to open GMap info windows
var mapInfoWindowManager = null;
var map;

// EVIL GLOBAL for all CSW records....
var cswRecordStore = null;

//Send these headers with every AJax request we make...
Ext.Ajax.defaultHeaders = {
    'Accept-Encoding': 'gzip, deflate' //This ensures we use gzip for most of our requests (where available)
};

Ext.onReady(function() {

    // Ext quicktips - check out <span qtip="blah blah"></span> around pretty much anything.
    Ext.QuickTips.init();

    var formFactory = new FormFactory();
    var searchBarThreshold = 6; //how many records do we need to have before we show a search bar

    //Generate our data stores
    cswRecordStore = new CSWRecordStore('getCSWRecords.do');
    var knownLayersStore = new KnownLayerStore('getKnownLayers.do');
    var customLayersStore = new CSWRecordStore('getCustomLayers.do');
    var activeLayersStore = new ActiveLayersStore();

    //Called whenever any of the KnownLayer panels click 'Add to Map'
    //defaultVisibility [boolean] - Optional - Set this to override the visibility setting for the new layer
    var knownLayerAddHandler = function(knownLayer, defaultVisibility, deferLayerLoad) {
        var activeLayerRec = activeLayersStore.getByKnownLayerRecord(knownLayer);

        if (defaultVisibility == null || defaultVisibility == undefined) {
            defaultVisibility = true;
        }

        if (deferLayerLoad == null || deferLayerLoad == undefined) {
            deferLayerLoad = false;
        }

        //Only add if the record isn't already there
        if (!activeLayerRec) {
            //add to active layers (At the top of the Z-order)
            activeLayerRec = activeLayersStore.addKnownLayer(knownLayer, cswRecordStore);

            //invoke this layer as being checked
            activeLayerCheckHandler(activeLayerRec, defaultVisibility, false, deferLayerLoad);

            showCopyrightInfo(activeLayerRec);
        }

        //set this record to selected
        activeLayersPanel.getSelectionModel().selectRecords([activeLayerRec.internalRecord], false);
    };

    //Called whenever any of the CSWPanels click 'Add to Map'
    //defaultVisibility [boolean] - Optional - Set this to override the visibility setting for the new layer
    var cswPanelAddHandler = function(cswRecord, defaultVisibility, deferLayerLoad) {
        var activeLayerRec = activeLayersStore.getByCSWRecord(cswRecord);

        if (defaultVisibility == null || defaultVisibility == undefined) {
            defaultVisibility = true;
        }

        if (deferLayerLoad == null || deferLayerLoad == undefined) {
            deferLayerLoad = false;
        }

        //Only add if the record isn't already there
        if (!activeLayerRec) {
            //add to active layers (At the top of the Z-order)
            activeLayerRec = activeLayersStore.addCSWRecord(cswRecord);

            //invoke this layer as being checked
            activeLayerCheckHandler(activeLayerRec, defaultVisibility, false, deferLayerLoad);

            showCopyrightInfo(activeLayerRec);
        }

        //set this record to selected
        activeLayersPanel.getSelectionModel().selectRecords([activeLayerRec.internalRecord], false);
    };

    //Display any copyright information associated with the layer.
    var showCopyrightInfo = function(activeLayerRec) {
        var html = "";
        var cswRecords = activeLayerRec.getCSWRecords();
        if(cswRecords.length > 0) {
            if(cswRecords.length == 1) {
                var constraints = activeLayerRec.getCSWRecords()[0].getConstraints();

                if(constraints.length > 0 ) {
                    html += "<table cellspacing='10' cellpadding='0' border='0'>";
                    if(constraints.length == 1 && constraints[0].length <= 0) {
                        html += "<tr><td>Copyright: Exclusive right to the publication, production, or sale of the rights to a literary, dramatic, musical, or artistic work, or to the use of a commercial print or label, granted by law for a specified period of time to an author, composer, artist, distributor.</td></tr>";
                    } else {
                        for(var i=0; i<constraints.length; i++) {
                            if(/^http:\/\//.test(constraints[i])) {
                                html += "<tr><td><a href="+constraints[i]+" target='_blank'>" + constraints[i] + "</a></td></tr>";
                            } else {
                                html += "<tr><td>" + constraints[i] + "</td></tr>";
                            }
                        }
                    }
                    html += "</table>";
                }
            } else { //TODO: Uncomment to handle layers with multiple cswRecords
//    			var hasConstraints = false;
//    			var i = 0;
//    			while(hasConstraints == false && i < cswRecords.length) {
//    				var constraints = activeLayerRec.getCSWRecords()[0].getConstraints();
//    				if(constraints.length > 0) {
//    					hasConstraints = true;
//    				}
//    				i++;
//    			}
//
//    			if(hasConstraints == true) {
//    				html += "<table cellspacing='10' cellpadding='0' border='0'>";
//    				html += "<tr><td>";
//    				html += "One or more of the records in this collection has copyright constraints. Please refer to individual records for further information.";
//					html += "</td></tr>";
//    				html += "</table>";
//    			}
            }

            if(html != "") {
                win = new Ext.Window({
                    title		: 'Copyright Information',
                    layout		: 'fit',
                    width		: 500,
                    autoHeight:    true,
                    items: [{
                        xtype 	: 'panel',
                        html	: html,
                        bodyStyle   : 'padding:0px',
                        autoScroll	: true,
                        autoDestroy : true
                    }]
                });

                win.show(this);
            }
        }
    };

    //Returns true if the CSWRecord record intersects the GMap viewport (based on its bounding box)
    var visibleCSWRecordFilter = function(record) {
        var geoEls = record.getGeographicElements();
        var visibleBounds = map.getBounds();

        //Iterate every 'bbox' geographic element type looking for an intersection
        //(They will be instances of the BBox class)
        for (var j = 0; j < geoEls.length; j++) {
            var bbox = geoEls[j];

            var sw = new GLatLng(bbox.southBoundLatitude, bbox.westBoundLongitude);
            var ne = new GLatLng(bbox.northBoundLatitude, bbox.eastBoundLongitude);
            var bboxBounds = new GLatLngBounds(sw,ne);

            if (visibleBounds.intersects(bboxBounds)) {
                return true;
            }
        }
    };

    //Returns true if the current records (from the knownLayersStore)
    //intersects the GMap viewport (based on its bounding box)
    //false otherwise
    var visibleKnownLayersFilter = function(record) {
        var linkedCSWRecords = record.getLinkedCSWRecords(cswRecordStore);
        var visibleBounds = map.getBounds();

        //iterate over every CSWRecord that makes up this layer, look for
        //one whose reported bounds intersects the view port
        for (var i = 0; i < linkedCSWRecords.length; i++) {
            if (visibleCSWRecordFilter(linkedCSWRecords[i])) {
                return true;
            }
        }

        return false;
    };

    //Given a CSWRecord, show (on the map) the list of bboxes associated with that record temporarily
    //bboxOverlayManager - if specified, will be used to store the overlays, otherwise the cswRecord's
    //                      bboxOverlayManager will be used
    var showBoundsCSWRecord = function(cswRecord, bboxOverlayManager) {
        var geoEls = cswRecord.getGeographicElements();

        if (!bboxOverlayManager) {
            bboxOverlayManager = cswRecord.getBboxOverlayManager();
            if (bboxOverlayManager) {
                bboxOverlayManager.clearOverlays();
            } else {
                bboxOverlayManager = new OverlayManager(map);
                cswRecord.setBboxOverlayManager(bboxOverlayManager);
            }
        }

        //Iterate our geographic els to get our list of bboxes
        for (var i = 0; i < geoEls.length; i++) {
            var geoEl = geoEls[i];
            if (geoEl instanceof BBox) {
                var polygonList = geoEl.toGMapPolygon('00FF00', 0, 0.7,'#00FF00', 0.6);

                for (var j = 0; j < polygonList.length; j++) {
                    polygonList[j].title = 'bbox';
                    bboxOverlayManager.addOverlay(polygonList[j]);
                }
            }
        }

        //Make the bbox disappear after a short while
        var clearTask = new Ext.util.DelayedTask(function(){
            bboxOverlayManager.clearOverlays();
        });

        clearTask.delay(2000);
    };

    //Pans/Zooms the map so the specified BBox object is visible
    var moveMapToBounds = function(bbox) {
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

    //Pans the map so that all bboxes linked to this record are visible.
    //If currentBounds is specified
    var moveToBoundsCSWRecord = function(cswRecord) {
        var bboxExtent = cswRecord.generateGeographicExtent();

        if (!bboxExtent) {
            return;
        }

        moveMapToBounds(bboxExtent);
    };

    //Given a KnownLayer, show (on the map) the list of bboxes associated with that layer temporarily
    var showBoundsKnownLayer = function(knownLayer) {
        var bboxOverlayManager = knownLayer.getBboxOverlayManager();
        if (bboxOverlayManager) {
            bboxOverlayManager.clearOverlays();
        } else {
            bboxOverlayManager = new OverlayManager(map);
            knownLayer.setBboxOverlayManager(bboxOverlayManager);
        }

        var linkedRecords = knownLayer.getLinkedCSWRecords(cswRecordStore);
        for (var i = 0; i < linkedRecords.length; i++) {
            showBoundsCSWRecord(linkedRecords[i], bboxOverlayManager);
        }
    };

    var moveToBoundsKnownLayer = function(knownLayer) {
        var linkedRecords = knownLayer.getLinkedCSWRecords(cswRecordStore);
        var superBbox = null;
        for (var i = 0; i < linkedRecords.length; i++) {
            var bboxToCombine =  linkedRecords[i].generateGeographicExtent();
            if (bboxToCombine !== null) {
                if (superBbox === null) {
                    superBbox = bboxToCombine;
                } else {
                    superBbox = superBbox.combine(bboxToCombine);
                }
            }
        }

        if (superBbox) {
            moveMapToBounds(superBbox);
        }
    };

    //Returns true if the specified cswRecord is linked or related by a known layer
    var isCSWRecordKnown = function(cswRecord) {

        //little utility function that returns true if the cswRecord is known by knownLayer
        var isKnownBy = function(knownLayer, cswRecord) {

            //Check this known layer
            var childRecords = knownLayer.getLinkedCSWRecords(cswRecordStore);
            for (var i = 0; i < childRecords.length; i++) {
                if (childRecords[i].getFileIdentifier() === cswRecord.getFileIdentifier()) {
                    return true;
                }
            }

            //Check the related known layers
            var relatedRecords = knownLayer.getRelatedCSWRecords(cswRecordStore);
            for (var i = 0; i < relatedRecords.length; i++) {
                if (relatedRecords[i].getFileIdentifier() === cswRecord.getFileIdentifier()) {
                    return true;
                }
            }

            return false;
        };

        //This is our known layer iterator
        var recordKnown = false;
        knownLayersStore.each(function(rec) {
            var knownLayer = new KnownLayerRecord(rec);

            //check if this layer
            if (isKnownBy(knownLayer, cswRecord)) {
                recordKnown = true;//we found the record
                return false;//abort iteration
            }
        });

        return recordKnown;
    };

    //-----------Known Features Panel Configurations (Groupings of various CSWRecords)
    var knownLayersPanel = new KnownLayerGridPanel('kft-layers-panel',
                                                            'Featured Layers',
                                                            'Layers or layer groups with custom handlers',
                                                            knownLayersStore,
                                                            cswRecordStore,
                                                            knownLayerAddHandler,
                                                            visibleKnownLayersFilter,
                                                            showBoundsKnownLayer,
                                                            moveToBoundsKnownLayer);

    //----------- Map Layers Panel Configurations (Drawn from CSWRecords that aren't a KnownLayer)
    var mapLayersFilter = function(cswRecord) {
        var serviceName = cswRecord.getServiceName();
        if (!serviceName || serviceName.length === 0) {
            return false;
        }

        //ensure its not referenced via KnownLayer
        return !isCSWRecordKnown(cswRecord);
    };
    var mapLayersPanel = new CSWRecordGridPanel('wms-layers-panel',
                                                'Registered Layers',
                                                'Other layers present in the Registry',
                                                cswRecordStore,
                                                cswPanelAddHandler,
                                                mapLayersFilter,
                                                visibleCSWRecordFilter,
                                                showBoundsCSWRecord,
                                                moveToBoundsCSWRecord);



    //------ Custom Layers
    var customLayersPanel = new CustomLayersGridPanel('custom-layers-panel',
                                                    'Custom Layers',
                                                    'Add your own WMS Layers',
                                                    customLayersStore,
                                                    cswPanelAddHandler,
                                                    showBoundsCSWRecord,
                                                    moveToBoundsCSWRecord);

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
            var activeLayerRecord = new ActiveLayersRecord(activeLayersPanel.getSelectionModel().getSelected());
            loadLayer(activeLayerRecord);
        }
    });

    /**
     * Used to show extra details for querying services
     */
    var filterPanel = new Ext.Panel({
        title: '<span qtip="Layer Specific filter properties. <br>Dont forget to hit \'Apply Filter\'">Filter Properties</span>',
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
                html: '<p id="filterpanelbox"> Filter options will be shown here for special services.</p>'
            }
        ],
        bbar: ['->', filterButton]
    });

    /**
     *Iterates through the activeLayersStore and updates each WMS layer's Z-Order to is position within the store
     *
     *This function will refresh every WMS layer too
     */
    var updateActiveLayerZOrder = function() {
        //Update the Z index for each WMS item in the store
        for (var i = 0; i < activeLayersStore.getCount(); i++) {
            var activeLayerRec = new ActiveLayersRecord(activeLayersStore.getAt(i));
            var overlayManager = activeLayerRec.getOverlayManager();

            if (overlayManager && activeLayerRec.getLayerVisible()) {
                var newZOrder = activeLayersStore.getCount() - i;

                overlayManager.updateZOrder(newZOrder);
            }
        }
    };

    //Loads the contents for the specified activeLayerRecord (applying any filtering too)
    //overrideFilterParams [Object] - Optional - specify to ignore any calculated filter parameters and use these values instead
    var loadLayer = function(activeLayerRecord, overrideFilterParams) {
        var cswRecords = activeLayerRecord.getCSWRecords();

        //We simplify things by treating the record list as a single type of WFS, WCS or WMS
        //So lets find the first record with a type we can choose (Prioritise WFS -> WCS -> WMS)
        if (cswRecords.length > 0) {
            var cswRecord = cswRecords[0];

            if (cswRecord.getFilteredOnlineResources('WFS').length !== 0) {
                wfsHandler(activeLayerRecord, overrideFilterParams);
            } else if (cswRecord.getFilteredOnlineResources('WCS').length !== 0) {
                wcsHandler(activeLayerRecord);
            } else if (cswRecord.getFilteredOnlineResources('WMS').length !== 0) {
                wmsHandler(activeLayerRecord);
            } else {
                genericRecordHandler(activeLayerRecord, overrideFilterParams);
            }
        }
    };


    /**
     *@param forceApplyFilter (Optional) if set AND isChecked is set AND this function has a filter panel, it will force the current filter to be loaded
     *@param deferLayerLoad (Optional) if set, the layer will be added but it will NOT load any data
     */
    var activeLayerCheckHandler = function(activeLayerRecord, isChecked, forceApplyFilter, deferLayerLoad) {
        //set the record to be selected if checked
        activeLayersPanel.getSelectionModel().selectRecords([activeLayerRecord.internalRecord], false);

        if (activeLayerRecord.getIsLoading()) {
            activeLayerRecord.setLayerVisible(!isChecked); //reverse selection
            Ext.MessageBox.show({
                title: 'Please wait',
                msg: "There is an operation in process for this layer. Please wait until it is finished.",
                buttons: Ext.MessageBox.OK,
                animEl: 'mb9',
                icon: Ext.MessageBox.INFO
            });
            return;
        }

        activeLayerRecord.setLayerVisible(isChecked);

        if (isChecked) {
            var filterPanelObj = activeLayerRecord.getFilterPanel();

            //Create our filter panel if we haven't already
            if (!filterPanelObj) {
                filterPanelObj = formFactory.getFilterForm(activeLayerRecord, map, cswRecordStore);
                activeLayerRecord.setFilterPanel(filterPanelObj);
            }

            //If the filter panel already exists, this may be a case where we are retriggering visiblity
            //in which case just rerun the previous filter
            if (filterPanelObj.form && forceApplyFilter && !filterButton.disabled) {
                filterButton.handler();
            }

            //If there is a filter panel, show it
            if (filterPanelObj.form) {
                filterPanel.add(filterPanelObj.form);
                filterPanel.getLayout().setActiveItem(activeLayerRecord.getId());
            }

            if (!deferLayerLoad) {
                //if we enable the filter button we don't download the layer immediately (as the user will have to enter in filter params)
                if (filterPanelObj.supportsFiltering) {
                    filterButton.enable();
                    filterButton.toggle(true);
                } else {
                    //Otherwise the layer doesn't need filtering, just display it immediately
                    loadLayer(activeLayerRecord);
                }
            }
            filterPanel.doLayout();
        } else {
            //Otherwise we are making the layer invisible, so clear any overlays
            var overlayManager = activeLayerRecord.getOverlayManager();
            if (overlayManager) {
                overlayManager.clearOverlays();
            }

            filterPanel.getLayout().setActiveItem(0);
            filterButton.disable();
        }
    };


    //This will attempt to render the record using only the csw record bounding boxes
    var genericRecordHandler = function(activeLayerRecord, overrideFilterParams) {
        //get our overlay manager (create if required)
        var overlayManager = activeLayerRecord.getOverlayManager();
        if (!overlayManager) {
            overlayManager = new OverlayManager(map);
            activeLayerRecord.setOverlayManager(overlayManager);
        }
        overlayManager.clearOverlays();

        var responseTooltip = new ResponseTooltip();
        activeLayerRecord.setResponseToolTip(responseTooltip);

        var titleFilter = '';
        var keywordFilter = '';
        var resourceProviderFilter = '';
        var filterObj = null;

        if (overrideFilterParams) {
            filterObj = overrideFilterParams;
        } else {
            if(typeof filterPanel.getLayout().activeItem.getForm == 'function') {
                filterObj = filterPanel.getLayout().activeItem.getForm().getValues();
            }
        }

        var regexp = /\*/;
        if(filterObj !== null){
            titleFilter = filterObj.title;
            if(titleFilter !== '' && /^\w+/.test(titleFilter)) {
                regexp = new RegExp(titleFilter, "i");
            }
        }

        if(filterObj !== null && filterObj.keyword !== null) {
            keywordFilter = filterObj.keyword;
        }

        if(filterObj !== null && filterObj.resourceProvider !== null) {
            resourceProviderFilter = filterObj.resourceProvider;
        }

        activeLayerRecord.setLastFilterParameters(filterObj);

        //Get the list of bounding box polygons
        var cswRecords = activeLayerRecord.getCSWRecords();
        var knownLayer = activeLayerRecord.getParentKnownLayer();
        var numRecords = 0;
        for (var i = 0; i < cswRecords.length; i++) {
            if ((titleFilter === '' || regexp.test(cswRecords[i].getServiceName())) &&
                    (keywordFilter === '' || cswRecords[i].containsKeyword(keywordFilter)) &&
                    (resourceProviderFilter === '' || cswRecords[i].getResourceProvider() == resourceProviderFilter)) {
                numRecords++;
                var geoEls = cswRecords[i].getGeographicElements();

                for (var j = 0; j < geoEls.length; j++) {
                    var geoEl = geoEls[j];
                    if (geoEl instanceof BBox) {
                        if(geoEl.eastBoundLongitude == geoEl.westBoundLongitude &&
                            geoEl.southBoundLatitude == geoEl.northBoundLatitude) {
                            //We only have a point
                            var point = new GLatLng(parseFloat(geoEl.southBoundLatitude),
                                    parseFloat(geoEl.eastBoundLongitude));

                            var icon = new GIcon(G_DEFAULT_ICON, activeLayerRecord.getIconUrl());
                            icon.shadow = null;

                            var iconSize = knownLayer.getIconSize();
                            if (iconSize) {
                                icon.iconSize = new GSize(iconSize.width, iconSize.height);
                            }

                            var iconAnchor = knownLayer.getIconAnchor();
                            if(iconAnchor) {
                                icon.iconAnchor = new GPoint(iconAnchor.x, iconAnchor.y);
                            }

                            var marker = new GMarker(point, {icon: icon});
                            marker.activeLayerRecord = activeLayerRecord.internalRecord;
                            marker.cswRecord = cswRecords[i].internalRecord;
                            //marker.onlineResource = onlineResource;

                            //Add our single point
                            overlayManager.markerManager.addMarker(marker, 0);

                        } else { //polygon
                            var polygonList = geoEl.toGMapPolygon('#0003F9', 4, 0.75,'#0055FE', 0.4);

                            for (var k = 0; k < polygonList.length; k++) {
                                polygonList[k].cswRecord = cswRecords[i].internalRecord;
                                polygonList[k].activeLayerRecord = activeLayerRecord.internalRecord;

                                overlayManager.addOverlay(polygonList[k]);
                            }
                        }
                    }
                }
            }
        }
        overlayManager.markerManager.refresh();

        responseTooltip.addResponse("", numRecords + " record(s) retrieved.");

        activeLayerRecord.setHasData(numRecords > 0);
    };

    //The WCS handler will create a representation of a coverage on the map for a given WCS record
    //If we have a linked WMS url we should use that (otherwise we draw an ugly red bounding box)
    var wcsHandler = function(activeLayerRecord) {

        //get our overlay manager (create if required)
        var overlayManager = activeLayerRecord.getOverlayManager();
        if (!overlayManager) {
            overlayManager = new OverlayManager(map);
            activeLayerRecord.setOverlayManager(overlayManager);
        }

        overlayManager.clearOverlays();

        var responseTooltip = new ResponseTooltip();
        activeLayerRecord.setResponseToolTip(responseTooltip);

        //Attempt to handle each CSW record as a WCS (if possible).
        var cswRecords = activeLayerRecord.getCSWRecordsWithType('WCS');
        for (var i = 0; i < cswRecords.length; i++) {
            var wmsOnlineResources = cswRecords[i].getFilteredOnlineResources('WMS');
            var wcsOnlineResources = cswRecords[i].getFilteredOnlineResources('WCS');
            var geographyEls = cswRecords[i].getGeographicElements();

            //Assumption - We only contain a single WCS in a CSWRecord (although more would be possible)
            var wcsOnlineResource = wcsOnlineResources[0];

            if (geographyEls.length === 0) {
                responseTooltip.addResponse(wcsOnlineResource.url, 'No bounding box has been specified for this coverage.');
                continue;
            }

            //We will need to add the bounding box polygons regardless of whether we have a WMS service or not.
            //The difference is that we will make the "WMS" bounding box polygons transparent but still clickable
            var polygonList = [];
            for (var j = 0; j < geographyEls.length; j++) {
                var thisPolygon = null;
                if (wmsOnlineResources.length > 0) {
                    thisPolygon = geographyEls[j].toGMapPolygon('#000000', 0, 0.0,'#000000', 0.0);
                } else {
                    thisPolygon = geographyEls[j].toGMapPolygon('#FF0000', 0, 0.7,'#FF0000', 0.6);
                }

                polygonList = polygonList.concat(thisPolygon);
            }

            //Add our overlays (they will be used for clicking so store some extra info)
            for (var j = 0; j < polygonList.length; j++) {
                polygonList[j].onlineResource = wcsOnlineResource;
                polygonList[j].cswRecord = cswRecords[i].internalRecord;
                polygonList[j].activeLayerRecord = activeLayerRecord.internalRecord;

                overlayManager.addOverlay(polygonList[j]);
            }

            //Add our WMS tiles (if any)
            for (var j = 0; j < wmsOnlineResources.length; j++) {
                var tileLayer = new GWMSTileLayer(map, new GCopyrightCollection(""), 1, 17);
                tileLayer.baseURL = wmsOnlineResources[j].url;
                tileLayer.layers = wmsOnlineResources[j].name;
                tileLayer.opacity = activeLayerRecord.getOpacity();

                overlayManager.addOverlay(new GTileLayerOverlay(tileLayer));
            }

            if(wcsOnlineResources.length > 0 || wmsOnlineResources.length > 0) {
                activeLayerRecord.setHasData(true);
            }
        }


        //This will update the Z order of our WMS layers
        updateActiveLayerZOrder();
    };

    var wfsHandler = function(activeLayerRecord, overrideFilterParams) {
        //if there is already a filter running for this record then don't call another
        if (activeLayerRecord.getIsLoading()) {
            Ext.MessageBox.show({
                title: 'Please wait',
                msg: "There is an operation in process for this layer. Please wait until it is finished.",
                buttons: Ext.MessageBox.OK,
                animEl: 'mb9',
                icon: Ext.MessageBox.INFO
            });
            return;
        }

        //Get our overlay manager (create if required).
        var overlayManager = activeLayerRecord.getOverlayManager();
        if (!overlayManager) {
            overlayManager = new OverlayManager(map);
            activeLayerRecord.setOverlayManager(overlayManager);
        }
        overlayManager.clearOverlays();

        //a response status holder
        var responseTooltip = new ResponseTooltip();
        activeLayerRecord.setResponseToolTip(responseTooltip);

        //Holds debug info
        var debuggerData = new DebuggerData();
        activeLayerRecord.setDebuggerData(debuggerData);

        //Prepare our query/locations
        var cswRecords = activeLayerRecord.getCSWRecordsWithType('WFS');
        var iconUrl = activeLayerRecord.getIconUrl();
        var finishedLoadingCounter = cswRecords.length;
        var parentKnownLayer = activeLayerRecord.getParentKnownLayer();

        //Begin loading from each service
        activeLayerRecord.setIsLoading(true);
        activeLayerRecord.setHasData(false);

        var transId = [];
        var transIdUrl = [];

        for (var i = 0; i < cswRecords.length; i++) {
            //Assumption - We will only have 1 WFS linked per CSW
            var wfsOnlineResource = cswRecords[i].getFilteredOnlineResources('WFS')[0];

            //Proceed with the query only if the resource url is contained in the list
            //of service endpoints for the known layer, or if the list is null.
            if(activeLayerRecord.getServiceEndpoints() == null  ||
                    includeEndpoint(activeLayerRecord.getServiceEndpoints(),
                            wfsOnlineResource.url, activeLayerRecord.includeEndpoints())) {

                //Generate our filter parameters for this service (or use the override values if specified)
                var filterParameters = { };

                if (overrideFilterParams) {
                    filterParameters = overrideFilterParams;
                } else {
                    if (filterPanel.getLayout().activeItem != filterPanel.getComponent(0)) {
                        filterParameters = filterPanel.getLayout().activeItem.getForm().getValues();
                    }
                }

                //Generate our filter parameters for this service
                filterParameters.serviceUrl = wfsOnlineResource.url;
                filterParameters.typeName = wfsOnlineResource.name;

                var visibleMapBounds = fetchVisibleMapBounds(map);
                if (parentKnownLayer && parentKnownLayer.getDisableBboxFiltering()) {
                    visibleMapBounds = null; //some WFS layer groupings may wish to disable bounding boxes
                }

                handleQuery(activeLayerRecord, cswRecords[i], wfsOnlineResource, filterParameters, visibleMapBounds, function() {
                    //decrement the counter
                    finishedLoadingCounter--;

                    //check if we can set the status to finished
                    if (finishedLoadingCounter <= 0) {
                        activeLayerRecord.setIsLoading(false);
                    }
                });
                transId[i] = this.Ext.Ajax.transId;
                transIdUrl[i] = wfsOnlineResource.url;

            } else { //If the endpoint will not be part of this layer just mark it as finished loading
                //decrement the counter
                finishedLoadingCounter--;

                //check if we can set the status to finished
                if (finishedLoadingCounter <= 0) {
                    activeLayerRecord.setIsLoading(false);
                }
            }
        }
        activeLayerRecord.setWFSRequestTransId(transId);
        activeLayerRecord.setWFSRequestTransIdUrl(transIdUrl);
    };

    /**
     * determines whether or not a particular endpoint should be included when loading
     * a layer
     */
    var includeEndpoint = function(endpoints, endpoint, includeEndpoints) {
        for(var i = 0; i < endpoints.length; i++) {
            if(endpoints[i].indexOf(endpoint) >= 0) {
                return includeEndpoints;
            }
        }
        return !includeEndpoints;
    };

    /**
     * internal helper method for Handling WFS filter queries via a proxyUrl and adding them to the map.
     */
    var handleQuery = function(activeLayerRecord, cswRecord, onlineResource, filterParameters, visibleMapBounds, finishedLoadingHandler) {

        var knownLayer = activeLayerRecord.getParentKnownLayer();

        //If we don't have a proxy URL specified, use the generic 'getAllFeatures.do' or 'getFeatureCount.do'
        var url = 'getAllFeatures.do';
        var countUrl = 'getFeatureCount.do';
        if (activeLayerRecord.getProxyFetchUrl()) {
            url = activeLayerRecord.getProxyFetchUrl();
            countUrl = activeLayerRecord.getProxyCountUrl(); //always use this, even if null - the layer may not have a counter specified
        }

        var threshold = 200;
        if (Ext.isNumber(MAX_FEATURES)) {
            threshold = MAX_FEATURES;
        }

        var responseTooltip = activeLayerRecord.getResponseToolTip();
        responseTooltip.addResponse(filterParameters.serviceUrl, "Loading...");

        var debuggerData = activeLayerRecord.getDebuggerData();

        //The download manager will handle requesting feature counts
        var downloadManager = new FeatureDownloadManager({
            visibleMapBounds : visibleMapBounds,
            proxyFetchUrl : url,
            proxyCountUrl : countUrl,
            serviceUrl : onlineResource.url,
            featureSetSizeThreshold : threshold,
            filterParams : filterParameters,
            listeners : {
                //The filterParameters may be appended with filter specific info
                //the actual filter parameters that gets used in the request is returned via
                //the success handler
                success : function(dm, actualFilterParams, data, debugInfo) {
                    var icon = new GIcon(G_DEFAULT_ICON, activeLayerRecord.getIconUrl());

                    //We need to remember exactly how we filtered
                    activeLayerRecord.setLastFilterParameters(actualFilterParams);

                    //Assumption - we are only interested in the first (if any) KnownLayer
                    if (knownLayer) {
                        var iconSize = knownLayer.getIconSize();
                        if (iconSize) {
                            icon.iconSize = new GSize(iconSize.width, iconSize.height);
                        }

                        var iconAnchor = knownLayer.getIconAnchor();
                        if(iconAnchor) {
                            icon.iconAnchor = new GPoint(iconAnchor.x, iconAnchor.y);
                        }

                        var infoWindowAnchor = knownLayer.getInfoWindowAnchor();
                        if(infoWindowAnchor) {
                            icon.infoWindowAnchor = new GPoint(infoWindowAnchor.x, infoWindowAnchor.y);
                        }
                    }

                    //TODO: This is a hack to remove marker shadows. Eventually it should be
                    // put into an external config file or become a session-based preference.
                    icon.shadow = null;

                    //Parse our KML
                    var parser = new KMLParser(data.kml);
                    parser.makeMarkers(icon, function(marker) {
                        marker.activeLayerRecord = activeLayerRecord.internalRecord;
                        marker.cswRecord = cswRecord.internalRecord;
                        marker.onlineResource = onlineResource;
                    });

                    var markers = parser.markers;
                    var overlays = parser.overlays;

                    //Add our single points and overlays
                    var overlayManager = activeLayerRecord.getOverlayManager();
                    overlayManager.markerManager.addMarkers(markers, 0);
                    for(var i = 0; i < overlays.length; i++) {
                        overlayManager.addOverlay(overlays[i]);
                    }
                    overlayManager.markerManager.refresh();

                    //Store some debug info
                    debuggerData.addResponse(debugInfo.url, debugInfo);

                    //store the status
                    responseTooltip.addResponse(onlineResource.url, (markers.length + overlays.length) + " record(s) retrieved.");

                    if(markers.length > 0 || overlays.length > 0) {
                        activeLayerRecord.setHasData(true);
                    }

                    //we are finished
                    finishedLoadingHandler();
                },
                error : function(dm, message, debugInfo) {
                    //store the status
                    responseTooltip.addResponse(onlineResource.url, message);
                    if(debugInfo) {
                        debuggerData.addResponse(onlineResource.url, message + debugInfo.info);
                    } else {
                        debuggerData.addResponse(onlineResource.url, message);
                    }

                    //we are finished
                    finishedLoadingHandler();
                },
                cancelled : function(dm) {
                    //store the status
                    responseTooltip.addResponse(onlineResource.url, 'Request cancelled by user.');

                    //we are finished
                    finishedLoadingHandler();
                }
            }
        });

        downloadManager.startDownload();
    };

    var wmsHandler = function(activeLayerRecord) {

        //Get our overlay manager (create if required).
        var overlayManager = activeLayerRecord.getOverlayManager();
        if (!overlayManager) {
            overlayManager = new OverlayManager(map);
            activeLayerRecord.setOverlayManager(overlayManager);
        }
        overlayManager.clearOverlays();

        //Add each and every WMS we can find
        var cswRecords = activeLayerRecord.getCSWRecordsWithType('WMS');
        for (var i = 0; i < cswRecords.length; i++) {
            var wmsOnlineResources = cswRecords[i].getFilteredOnlineResources('WMS');
            for (var j = 0; j < wmsOnlineResources.length; j++) {
                var tileLayer = new GWMSTileLayer(map, new GCopyrightCollection(""), 1, 17);
                tileLayer.baseURL = wmsOnlineResources[j].url;
                tileLayer.layers = wmsOnlineResources[j].name;
                tileLayer.opacity = activeLayerRecord.getOpacity();

                overlayManager.addOverlay(new GTileLayerOverlay(tileLayer));
            }

            if(wmsOnlineResources.length > 0) {
                activeLayerRecord.setHasData(true);
            }
        }

        //This will handle adding the WMS layer(s) (as well as updating the Z-Order)
        updateActiveLayerZOrder();
    };

    //This handler is called whenever the user selects an active layer
    var activeLayerSelectionHandler = function(activeLayerRecord) {
        //if its not checked then don't do any actions
        if (!activeLayerRecord.getLayerVisible()) {
            filterPanel.getLayout().setActiveItem(0);
            filterButton.disable();
        } else if (activeLayerRecord.getFilterPanel() !== null) {
            var filterPanelObj = activeLayerRecord.getFilterPanel();

            //if filter panel already exists then show it
            if (filterPanelObj && filterPanelObj.form) {
                filterPanel.getLayout().setActiveItem(activeLayerRecord.getId());
            } else {
                filterPanel.getLayout().setActiveItem(0);
            }

            if (filterPanelObj && filterPanelObj.supportsFiltering) {
                filterButton.enable();
                filterButton.toggle(true);
            } else {
                filterButton.disable();
            }
        } else {
            //if this type doesnt need a filter panel then just show the default filter panel
            filterPanel.getLayout().setActiveItem(0);
            filterButton.disable();
        }
    };


    //This handler is called on records that the user has requested to delete from the active layer list
    var activeLayersRemoveHandler = function(activeLayerRecord) {
        if (activeLayerRecord.getIsLoading()) {
            Ext.MessageBox.show({
                buttons: {yes:'Stop Processing', no:'Cancel'},
                fn: function(buttonId){
                    if(buttonId === 'yes')
                    {
                        activeLayersStopRequest(activeLayerRecord);
                    }
                    else if (buttonId === 'no') {
                        return;
                    }
                },
                icon: Ext.MessageBox.INFO,
                modal:true,
                msg: "Cannot remove the layer because there is an operation in process. Do you want to stop further processing?",
                title: 'Stop Processing!'
            });
        }
        else{

            var overlayManager = activeLayerRecord.getOverlayManager();
            if (overlayManager) {
                overlayManager.clearOverlays();
            }

            //remove it from active layers
            activeLayersStore.removeActiveLayersRecord(activeLayerRecord);

            //set the filter panels active item to 0
            filterPanel.getLayout().setActiveItem(0);

            //Completely destroy the filter panel object as we no longer
            //have any use for it
            var filterPanelObj = activeLayerRecord.getFilterPanel();
            if (filterPanelObj && filterPanelObj.form) {
                filterPanelObj.form.destroy();
            }
        }
    };

    var activeLayersStopRequest = function(activeLayerRecord){
        if (!activeLayerRecord.getIsLoading()) {
            return;
        }
        else{
            var transID = activeLayerRecord.getWFSRequestTransId();
            var transIDUrl = activeLayerRecord.getWFSRequestTransIdUrl();
            for(i =0;i< transID.length; i++){
                if(Ext.Ajax.isLoading(transID[i])){
                    Ext.Ajax.abort(transID[i]);
                    var responseTooltip = activeLayerRecord.getResponseToolTip();
                    responseTooltip.addResponse(transIDUrl[i],"Processing Aborted");
                }
            }
            activeLayerRecord.setIsLoading(false);
        }
    };



    this.activeLayersPanel = new ActiveLayersGridPanel('active-layers-panel',
                                                        'Active Layers',
                                                        'The map layers will display on the map in the order in which they were added.',
                                                        activeLayersStore,
                                                        activeLayerSelectionHandler,
                                                        updateActiveLayerZOrder,
                                                        activeLayersRemoveHandler,
                                                        activeLayersStopRequest,
                                                        activeLayerCheckHandler);

    /**
     * Tooltip for the active layers
     */
    var activeLayersToolTip = null;

    /**
     * Handler for mouse over events on the active layers panel, things like server status, and download buttons
     */
    this.activeLayersPanel.on('mouseover', function(e, t) {
        e.stopEvent();

        var row = e.getTarget('.x-grid3-row');
        var col = e.getTarget('.x-grid3-col');

        //if there is no visible tooltip then create one, if on is visible already we dont want to layer another one on top
        if (col !== null && (activeLayersToolTip === null || !activeLayersToolTip.isVisible())) {

            //get the actual data record
            var theRow = activeLayersPanel.getView().findRow(row);
            var activeLayerRecord = new ActiveLayersRecord(activeLayersPanel.getStore().getAt(theRow.rowIndex));

            var autoWidth = !Ext.isIE6 && !Ext.isIE7;

            //This is for the key/legend column
            if (col.cellIndex == '1') {

                if (activeLayerRecord.getCSWRecordsWithType('WMS').length > 0) {
                    activeLayersToolTip = new Ext.ToolTip({
                        target: e.target ,
                        autoHide : true,
                        html: 'Show the key/legend for this layer' ,
                        anchor: 'bottom',
                        trackMouse: true,
                        showDelay:60,
                        autoHeight:true,
                        autoWidth: autoWidth,
                        listeners : {
                            hide : function(component) {
                                component.destroy();
                            }
                        }
                    });
                }
            }
            //this is the status icon column
            else if (col.cellIndex == '2') {
                var html = 'No status has been recorded.';
                var htmlResponse = false;

                if (activeLayerRecord.getResponseToolTip() != null) {
                    html = activeLayerRecord.getResponseToolTip().getHtml();
                    htmlResponse = true;
                }

                activeLayersToolTip = new Ext.ToolTip({
                    target: e.target ,
                    header: false,
                    //title: 'Status Information',
                    autoHide : true,
                    html: html ,
                    anchor: 'bottom',
                    trackMouse: true,
                    showDelay:60,
                    autoHeight:true,
                    autoWidth: autoWidth,
                    maxWidth:500,
                    width:autoWidth ? undefined : 500,
                    listeners : {
                        hide : function(component) {
                            component.destroy();
                        }
                    }
                });
            }

            //this is the column for download link icons
            else if (col.cellIndex == '5') {
                if(activeLayerRecord.hasData()) {
                    activeLayersToolTip = new Ext.ToolTip({
                        target: e.target ,
                        //title: 'Status Information',
                        autoHide : true,
                        html: 'Download data for this layer.' ,
                        anchor: 'bottom',
                        trackMouse: true,
                        showDelay:60,
                        autoHeight:true,
                        autoWidth: autoWidth,
                        listeners : {
                            hide : function(component) {
                                component.destroy();
                            }
                        }
                    });
                } else {
                    activeLayersToolTip = new Ext.ToolTip({
                        target: e.target ,
                        autoHide : true,
                        html: 'No download is available.' ,
                        anchor: 'bottom',
                        trackMouse: true,
                        showDelay:60,
                        autoHeight:true,
                        autoWidth: autoWidth,
                        listeners : {
                            hide : function(component) {
                                component.destroy();
                            }
                        }
                    });
                }
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
        if (col !== null) {

            //get the actual data record
            var theRow = activeLayersPanel.getView().findRow(row);
            var activeLayerRecord = new ActiveLayersRecord(activeLayersPanel.getStore().getAt(theRow.rowIndex));

            //This is the marker key column
            if (col.cellIndex == '1') {
                //For WMS, we request the Legend and display it
                var cswRecords = activeLayerRecord.getCSWRecordsWithType('WMS');
                if (cswRecords.length > 0) {

                    //Only show the legend window if it's not currently visible
                    var win = activeLayerRecord.getLegendWindow();
                    if (!win || (win && !win.isVisible())) {

                        //Generate a legend for each and every WMS linked to this record
                        var html = '';
                        var titleTypes = '';
                        for (var i = 0; i < cswRecords.length; i++) {
                            var wmsOnlineResources = cswRecords[i].getFilteredOnlineResources('WMS');

                            if (titleTypes.length !== 0) {
                                titleTypes += ', ';
                            }
                            titleTypes += cswRecords[i].getServiceName();

                            for (var j = 0; j < wmsOnlineResources.length; j++) {
                                var url = new LegendManager(wmsOnlineResources[j].url, wmsOnlineResources[j].name).generateImageUrl();

                                html += '<a target="_blank" href="' + url + '">';
                                html += '<img onerror="this.alt=\'There was an error loading this legend. Click here to try again in a new window or contact the data supplier.\'" alt="Loading legend..." src="' + url + '"/>';
                                html += '</a>';
                                html += '<br/>';
                            }
                        }

                        win = new Ext.Window({
                            title		: 'Legend: ' + titleTypes,
                            layout		: 'fit',
                            width		: 200,
                            height		: 300,

                            items: [{
                                xtype 	: 'panel',
                                html	: html,
                                autoScroll	: true
                            }]
                        });

                        //Save our window reference so we can tell if its already been open
                        activeLayerRecord.setLegendWindow(win);

                        win.show(e.getTarget());
                    } else if (win){
                        //The window is already open
                        win.toFront();
                        win.center();
                        win.focus();
                    }
                }
            }
            //this is to add Service Information Popup Window to Active Layers
            else if (col.cellIndex == '2'){

                if (this.onlineResourcesPopup && this.onlineResourcesPopup.isVisible()) {
                    this.onlineResourcesPopup.close();
                }
                var cswRecords = activeLayerRecord.getCSWRecords();
                if (activeLayerRecord.getSource() === 'KnownLayer'){
                    var knownLayerRecord = knownLayersStore.getKnownLayerById(activeLayerRecord.getId());
                    this.onlineResourcesPopup = new CSWRecordDescriptionWindow(cswRecords, knownLayerRecord);
                }else{
                    this.onlineResourcesPopup = new CSWRecordDescriptionWindow(cswRecords);
                }
                this.onlineResourcesPopup.show(e.getTarget());

            }

          //this is for clicking the loading icon
            else if (col.cellIndex == '3') {

                //to get the value of variable used in url
                var gup = function ( name ) {
                    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
                    var regexS = "[\\?&]"+name+"=([^&#]*)";
                    var regex = new RegExp( regexS );
                    var results = regex.exec( window.location.href );
                    if( results === null ) {
                        return "";
                    } else {
                        return results[1];
                    }
                };
                var filter_debugger_param = gup( 'debug' );
                //get the debug window if there is a debug variable with value 1
                if(filter_debugger_param == 1 || filter_debugger_param == "on"){
                       var debugHtml = 'Please generate a request to get the request query.';

                    if (activeLayerRecord.getDebuggerData()) {
                           debugHtml = activeLayerRecord.getDebuggerData().getHtml();
                    }

                    var chkpanel = new Ext.Panel({
                           autoScroll	: true,
                        html	:	debugHtml
                    });
                    var debugWin = new Ext.Window({
                        title: 'WFS Debug Information',
                           layout:'fit',
                           width:500,
                        height:300,

                        items: [chkpanel]
                    });

                    debugWin.show(this);
                }
            }
            //this is the column for download link icons
            else if (col.cellIndex == '5') {
                if(activeLayerRecord.hasData()) {
                    var keys = [];
                    var values = [];

                    var wfsRecords = activeLayerRecord.getCSWRecordsWithType('WFS');
                    var wcsRecords = activeLayerRecord.getCSWRecordsWithType('WCS');
                    var wmsRecords = activeLayerRecord.getCSWRecordsWithType('WMS');

                    //We simplify things by treating the record list as a single type of WFS, WCS or WMS
                    //So lets find the first record with a type we can choose (Prioritise WFS -> WCS -> WMS)
                    var cswRecords = wfsRecords;
                    if (cswRecords.length !== 0) {
                        var filterParameters = activeLayerRecord.getLastFilterParameters();
                        if (!filterParameters) {
                            filterParameters = {};
                        }
                        var bbox = filterParameters.bbox;
                        var boundingbox = Ext.util.JSON.encode(fetchVisibleMapBounds(map));
                        var proxyUrl = activeLayerRecord.getProxyFetchUrl()!== null ? activeLayerRecord.getProxyFetchUrl() : 'getAllFeatures.do';
                        var prefixUrl = window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/" + proxyUrl + "?";

                        if(bbox === null || bbox === undefined){
                            downloadWFS(cswRecords, activeLayerRecord, filterParameters, prefixUrl, null, keys, values);
                        }
                        else{
                            if(bbox === boundingbox){
                                downloadWFS(cswRecords, activeLayerRecord, filterParameters, prefixUrl, bbox, keys, values);
                            }
                            else{
                                Ext.MessageBox.show({
                                    buttons:{yes:'Use current', no:'Use original'},
                                    fn:function (buttonId) {
                                        if (buttonId === 'yes') {
                                            downloadWFS(cswRecords, activeLayerRecord, filterParameters, prefixUrl, boundingbox, keys, values);
                                        } else if (buttonId === 'no') {
                                            downloadWFS(cswRecords, activeLayerRecord, filterParameters, prefixUrl, bbox, keys, values);
                                        }
                                    },
                                    modal:true,
                                    msg:'The visible bounds have changed since you added this layer. Would you like to download using the original or the current visible bounds?',
                                    title:'Warning: Changed Visible Bounds!'
                                });
                            }
                        }
                    }

                    if (wfsRecords.length === 0 && wcsRecords.length !== 0) {
                        cswRecords = wcsRecords;
                        //Assumption - we only expect 1 WCS
                        var wcsOnlineResource = cswRecords[0].getFilteredOnlineResources('WCS')[0];
                        showWCSDownload(wcsOnlineResource.url, wcsOnlineResource.name);
                        return;
                    }

                    //For WMS we download every WMS
                    if (wfsRecords.length === 0 && wcsRecords.length === 0 && wmsRecords.length !== 0) {
                        cswRecords = wmsRecords;
                        for (var i = 0; i < cswRecords.length; i++) {
                            var wmsOnlineResources = cswRecords[i].getFilteredOnlineResources('WMS');
                            for (var j = 0; j < wmsOnlineResources.length; j++) {
                                var boundBox = (map.getBounds().getSouthWest().lng() < 0 ? map.getBounds().getSouthWest().lng() + 360.0 : map.getBounds().getSouthWest().lng()) + "," +
                                map.getBounds().getSouthWest().lat() + "," +
                                (map.getBounds().getNorthEast().lng() < 0 ? map.getBounds().getNorthEast().lng() + 360.0 : map.getBounds().getNorthEast().lng()) + "," +
                                map.getBounds().getNorthEast().lat();

                                 var url = wmsOnlineResources[j].url;
                                 var typeName = wmsOnlineResources[j].name;

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
                                 url += "&LAYERS=" + typeName;
                                 if (this.styles) {
                                     url += "&STYLES=" + this.styles;
                                 } else {
                                     url += "&STYLES="; //Styles parameter is mandatory, using a null string ensures default style
                                 }
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

                                 keys.push('serviceUrls');
                                 values.push(url);
                            }
                        }

                        openWindowWithPost("downloadDataAsZip.do?", 'WMS_Layer_Download', keys, values);
                        return;
                    }
                }
            }
        }
    });


    var downloadWFS = function( cswRecords, activeLayerRecord, filterParameters, prefixUrl, bbox, keys, values){

        for (var i = 0; i < cswRecords.length; i++) {
            var wfsOnlineResources = cswRecords[i].getFilteredOnlineResources('WFS');
            var cswWfsRecordCount = cswRecords.length;
            var WfsOnlineResourceCount = wfsOnlineResources.length;

            for (var j = 0; j < wfsOnlineResources.length; j++) {
                //Generate our filter parameters (or just grab the last set used
                var url = wfsOnlineResources[j].url;

                filterParameters.serviceUrl = wfsOnlineResources[j].url;
                filterParameters.typeName = wfsOnlineResources[j].name;
                filterParameters.maxFeatures = 0;

                if(activeLayerRecord.getServiceEndpoints() === null ||
                        includeEndpoint(activeLayerRecord.getServiceEndpoints(), url, activeLayerRecord.includeEndpoints())) {
                        var currentFilterParameters = copy_obj(filterParameters);
                        currentFilterParameters.bbox = bbox;
                        keys.push('serviceUrls');
                        values.push(Ext.urlEncode(currentFilterParameters, prefixUrl));
                }
            }
        }

        openWindowWithPost("downloadGMLAsZip.do?", 'WFS_Layer_Download_'+new Date().getTime(), keys, values);
        return;
    }

   /** This function copy an object to another by value and not by reference**/
   var copy_obj = function(objToCopy) {
        var obj = new Object();

        for (var e in objToCopy) {
          obj[e] = objToCopy[e];
        }
        return obj;
  };



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
            url += '&filename=' + escape(name);
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
    };

    // basic tabs 1, built from existing content
    var tabsPanel = new Ext.TabPanel({
        //width:450,
        activeTab: 0,
        region:'north',
        split: true,
        height: 225,
        autoScroll: true,
        enableTabScroll: true,
        //autosize:true,
        items:[
            knownLayersPanel,
            mapLayersPanel,
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
        width: 350,
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
     * Add all the panels to the viewport
     */
    var viewport = new Ext.Viewport({
        layout:'border',
        items:[westPanel, centerPanel]
    });

    // Is user's browser suppported by Google Maps?
    if (GBrowserIsCompatible()) {

        map = new GMap2(centerPanel.body.dom);

        /* AUS-1526 search bar. */

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
    GEvent.addListener(map, "click", function(overlay, latlng, overlayLatlng) {
        gMapClickController(map, overlay, latlng, overlayLatlng, activeLayersStore);
    });

    GEvent.addListener(map, "mousemove", function(latlng){
        var latStr = "<b>Long:</b> " + latlng.lng().toFixed(6) +
                   "&nbsp&nbsp&nbsp&nbsp" +
                   "<b>Lat:</b> " + latlng.lat().toFixed(6);
        document.getElementById("latlng").innerHTML = latStr;
    });

    GEvent.addListener(map, "mouseout", function(latlng){
        document.getElementById("latlng").innerHTML = "";
    });

    //Attempts to deserialize the state string and apply its contents to the current map
    var attemptDeserialization = function(stateString) {
        var s = new MapStateSerializer();

        //Attempt to deserialize - there shouldn't be any problems unless we are trying to backport a 'future' serialization string
        try {
            s.deserialize(stateString);
        } catch(er) {
            Ext.MessageBox.show({
                title : 'Unsupported Permanent Link',
                icon : Ext.MessageBox.WARNING,
                buttons : Ext.Msg.OK,
                msg : 'The permanent link that you are using is in a format that this portal cannot recognize. The saved layers and viewport will not be loaded.',
                multiline : false
            });
            return;
        }

        //Pan our map to the appropriate location
        map.setZoom(s.mapState.zoom);
        map.panTo(new GLatLng(s.mapState.center.lat, s.mapState.center.lng));

        var missingLayers = false; //are there any layers serialized that no longer exist?

        //Add the layers, attempt to load whatever layers are available
        //but warn the user if some layers no longer exist
        for (var i = 0; i < s.activeLayers.length; i++) {
            if (s.activeLayers[i].source === 'KnownLayer') {
                if (!s.activeLayers[i].id) {
                    continue;
                }

                var knownLayer = knownLayersStore.getKnownLayerById(s.activeLayers[i].id);
                if (!knownLayer) {
                    missingLayers = true;
                    continue;
                }

                knownLayerAddHandler(knownLayer, s.activeLayers[i].visible, true);
                var activeLayerRec = activeLayersStore.getByKnownLayerRecord(knownLayer);
                if (activeLayerRec) {
                    activeLayerRec.setOpacity(s.activeLayers[i].opacity);

                    if (s.activeLayers[i].visible) {
                        loadLayer(activeLayerRec, s.activeLayers[i].filter);
                    }

                    //Prefill our filter panel (if we have the fields)
                    var filterPanel = activeLayerRec.getFilterPanel();
                    if (filterPanel && filterPanel.form && s.activeLayers[i].filter) {

                        //Register for the load event
                        var filterObj = s.activeLayers[i].filter;
                        filterPanel.form.on('formloaded', function() {
                            filterPanel.form.getForm().setValues(filterObj);

                        });

                        //If the even has already fired we can just load normally
                        if (filterPanel.form.isFormLoaded) {
                            filterPanel.form.getForm().setValues(filterObj);
                        }
                    }
                }

            } else if (s.activeLayers[i].source === 'CSWRecord') {
                //Perform a 'best effort' to find a matching CSWRecord
                var cswRecords = cswRecordStore.getCSWRecordsByOnlineResources(s.activeLayers[i].onlineResources);
                if (cswRecords.length === 0) {
                    missingLayers = true;
                    continue;
                }

                var cswRecord = cswRecords[0];
                cswPanelAddHandler(cswRecord, s.activeLayers[i].visible, true);
                var activeLayerRec = activeLayersStore.getByCSWRecord(cswRecord);
                if (activeLayerRec) {
                    activeLayerRec.setOpacity(s.activeLayers[i].opacity);

                    if (s.activeLayers[i].visible) {
                        loadLayer(activeLayerRec, s.activeLayers[i].filter);
                    }
                }
            }
        }

        if (missingLayers) {
            Ext.MessageBox.show({
                title : 'Missing Layers',
                icon : Ext.MessageBox.WARNING,
                buttons : Ext.Msg.OK,
                msg : 'Some of the layers that were saved no longer exist and will be ignored. The remaining layers will load normally',
                multiline : false
            });
        }
    };

    //As there is a relationship between these two stores,
    //We should refresh any GUI components whose view is dependent on these stores
    cswRecordStore.load({callback : function(r, options, success) {
        knownLayersStore.load({callback : function() {
            cswRecordStore.fireEvent('datachanged');

            //Afterwards we decode any saved state included as a URL parameter
            var urlParams = Ext.urlDecode(window.location.search.substring(1));
            if (urlParams && urlParams.state) {
                //IE will truncate our URL at 2048 characters which destroys our state string.
                //Let's warn the user if we suspect this to have occurred
                if (Ext.isIE && window.location.href.length === 2047) {
                    Ext.MessageBox.show({
                        title : 'Mangled Permanent Link',
                        icon : Ext.MessageBox.WARNING,
                        msg : 'The web browser you are using (Internet Explorer) has likely truncated the permanent link you are using which will probably render it unuseable. The AuScope portal will attempt to restore the saved state anyway.',
                        buttons : Ext.Msg.OK,
                        multiline : false,
                        fn : function() {
                            attemptDeserialization(urlParams.state);
                        }
                    });
                } else {
                    //otherwise there *shouldn't* be any problems
                    attemptDeserialization(urlParams.state);
                }
            }

            if(r.length == 0) {
                Ext.MessageBox.show({
                    title : 'No Services Available',
                    icon : Ext.MessageBox.WARNING,
                    buttons : Ext.Msg.OK,
                    msg : 'The CSW(s) are not returning any records and functionality will be affected.',
                    multiline : false
                });
            }
        }});
    }});


});