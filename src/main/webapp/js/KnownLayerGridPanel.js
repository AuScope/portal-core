/**
 * An extension of a normal GridPanel that makes it specialize into rendering a KnownLayerStore
 *
 *
 * title 			: The title this grid panel will display
 * map   			: the GMap2 instance
 * knownFeatureTypeStore 	: an instance of KnownLayerStore that will be used to populate this panel
 * cswRecordStore : an instance of CSWRecordStore - its contents will be used as lookups for layers in the KnownLayerStore
 * addLayerHandler	: function(KnownLayerRecord) This will be called when the user adds a layer.
 * visibleLayerHandler: function(KnownLayerRecord) This will be called to filter every visible KnownLayerRecord
 * showBoundsHandler: function(KnownLayerRecord) called when the user wants to see a brief highlight of the records bounds
 * moveToBoundsHandler: function(KnownLayerRecord) called when the user wants to find the location of the record bounds
 *
 */
KnownLayerGridPanel = function(id, title, description, knownFeatureTypeStore, cswRecordStore, addLayerHandler, visibleLayerHandler, showBoundsHandler, moveToBoundsHandler) {
	this.addLayerHandler = addLayerHandler;

	//This is so we can reference our search panel
	var searchPanelId = id + '-search-panel';

	var rowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{description} </p><br>')
    });

	var dsCopy = new KnownLayerStore();
	knownFeatureTypeStore.on('datachanged', this.internalOnDataChanged, this);
	dsCopy.copyFrom(knownFeatureTypeStore, this.knownLayerRecordFilter);

	KnownLayerGridPanel.superclass.constructor.call(this, {
        stripeRows       : true,
        autoExpandColumn : 'title',
        plugins          : [ rowExpander ],
        viewConfig       : {scrollOffset: 0, forceFit:true},
        title            : '<span qtip="' + description + '">' + title + '</span>',
        region           :'north',
        split            : true,
        height           : 160,
        //width: 80,
        autoScroll       : true,
        store            : dsCopy,
        originalStore    : knownFeatureTypeStore,
        view: new Ext.grid.GroupingView({
            forceFit:true,
            groupTextTpl: '{[values.group ? values.group : "Others"]} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
        }),
        columns: [
            rowExpander,
            {
                id:'title',
                header: "Title",
                width: 80,
                sortable: true,
                dataIndex: 'title'
            },{
            	id : 'knownType',
            	header : '',
            	width: 18,
            	dataIndex: 'styleName', //this isn't actually rendered in this column
            	renderer: function(value, metadata, record) {
            		var knownLayerRecord = new KnownLayerRecord(record);

            		var linkedCSWRecords = knownLayerRecord.getLinkedCSWRecords(cswRecordStore);

            		if (linkedCSWRecords.length === 0) {
            			return '<div style="text-align:center"><img src="img/cross.png" width="16" height="16" align="CENTER"/></div>';
            		}

            		for (var i = 0; i < linkedCSWRecords.length; i++) {
            			var onlineResources = linkedCSWRecords[i].getOnlineResources();
	            		for (var j = 0; j < onlineResources.length; j++) {
	            			if (onlineResources[j].onlineResourceType == 'WCS' ||
	            				onlineResources[j].onlineResourceType == 'WFS') {
	            				return '<div style="text-align:center"><img src="img/binary.png" width="16" height="16" align="CENTER"/></div>';
	            			}
	            		}
            		}

            		return '<div style="text-align:center"><img src="img/picture.png" width="16" height="16" align="CENTER"/></div>';
            	}
            },{
            	id:'search',
            	header: '',
            	width: 45,
            	dataIndex: 'proxyUrl', //this isn't actually rendered in this column
            	resizable: false,
            	menuDisabled: true,
            	sortable: false,
            	fixed: true,
            	renderer: function (value, metadata, record) {
            		//Assume every known feature type will have at least one visible bbox
            		return '<img src="img/magglass.gif"/>';
            	}
            },{
                id:'groupCol',
                width: 160,
                sortable: true,
                dataIndex: 'group',
                hidden:true
            }
        ],
        bbar: [{
            text:'Add Layer to Map',
            tooltip:'Add Layer to Map',
            iconCls:'add',
            pressed: true,
            scope: this,
            handler: function() {
                var kftToAdd = new KnownLayerRecord(this.getSelectionModel().getSelected());
                addLayerHandler(kftToAdd);
            }
        }],

        tbar: [
               '<span qtip="Quickly find layers by typing the name here">Search: </span>', ' ',
               new Ext.ux.form.ClientSearchField({
                   store: knownFeatureTypeStore,
                   width:200,
                   id:searchPanelId,
                   fieldName:'title'
               }), new Ext.Button({
                   text     :'Visible',
                   tooltip  :'Display only layers in present view window',
                   handler:function() {
               	   		var searchPanel = Ext.getCmp(searchPanelId);
               	   		searchPanel.runCustomFilter('<visible layers>', function(rec) {
               	   			return visibleLayerHandler(new KnownLayerRecord(rec));
               	   		});
               		}
               })
           ],
        listeners: {
           	cellclick : function (grid, rowIndex, colIndex, e) {
               	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
               	var knownLayerRecord = grid.getStore().getKnownLayerAt(rowIndex);
               	if (fieldName === 'proxyUrl') {
               		e.stopEvent();

                   	showBoundsHandler(knownLayerRecord);
               	} else if (fieldName === 'styleName') {
               		e.stopEvent();
               		var cswRecords = knownLayerRecord.getLinkedCSWRecords(cswRecordStore);

               		//Can show service info if there are no linked records
               		if (cswRecords.length === 0) {
               			return;
               		}

            		//Close an existing popup
            		if (this.onlineResourcesPopup && this.onlineResourcesPopup.isVisible()) {
            			this.onlineResourcesPopup.close();
            		}

            		this.onlineResourcesPopup = new CSWRecordDescriptionWindow(cswRecords, knownLayerRecord);
            		this.onlineResourcesPopup.show(e.getTarget());
               	}
           	},

           	celldblclick : function (grid, rowIndex, colIndex, e) {
               	var record = grid.getStore().getAt(rowIndex);
               	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
               	if (fieldName !== 'proxyUrl') {
               		return;
               	}

               	e.stopEvent();

               	moveToBoundsHandler(grid.getStore().getKnownLayerAt(rowIndex));
           	},

           	mouseover : function(e, t) {
                e.stopEvent();

                var row = e.getTarget('.x-grid3-row');
                var col = e.getTarget('.x-grid3-col');


                //if there is no visible tooltip then create one, if on is visible already we dont want to layer another one on top
                if (col !== null && (!this.currentToolTip || !this.currentToolTip.isVisible())) {

                    //get the actual data record
                    var theRow = this.getView().findRow(row);
                    var knownLayerRecord = new KnownLayerRecord(this.getStore().getAt(theRow.rowIndex));

                    var autoWidth = !Ext.isIE6 && !Ext.isIE7;

                    //This is for the 'record type' column
                    if (col.cellIndex == '2') {
                    	var cswRecords = knownLayerRecord.getLinkedCSWRecords(cswRecordStore);
                    	var text = 'Click for detailed information about the web services this layer utilises';
                    	if (cswRecords.length === 0) {
                    		text = 'This layer currently has no services that it can utilise. Please try reloading the page later.';
                    	}

                    	this.currentToolTip = new Ext.ToolTip({
                            target: e.target ,
                            title: 'Service Information',
                            autoHide : true,
                            html: text,
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
                    //this is the status icon column
                    else if (col.cellIndex == '3') {

                        this.currentToolTip = new Ext.ToolTip({
                            target: e.target ,
                            title: 'Bounds Information',
                            autoHide : true,
                            html: 'Click to see the bounds of this layer',
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
        }
    });
};

KnownLayerGridPanel.prototype.addLayerHandler = null;

Ext.extend(KnownLayerGridPanel, Ext.grid.GridPanel, {

	knownLayerRecordFilter : function(knownLayerRecord) {
		return !knownLayerRecord.getHidden();
	},

	/**
	 * Whenever the internal datastore changes, update our filtered copy
	 */
	internalOnDataChanged : function(store) {
		this.getStore().copyFrom(this.initialConfig.originalStore, this.knownLayerRecordFilter);
	}
});