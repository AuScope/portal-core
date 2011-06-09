/**
 * An extension of a normal GridPanel that makes it specialize into rendering a CSWRecordStore
 *
 * This panel will create a 'copy' of the cswRecordStore with cswRecordFilter applied over the top of it.
 * Further - every time the cswRecordStore is altered, the 'copied' data will also be updated
 *
 * id				: unique ID to identify this grid
 * title 			: The title this grid panel will display
 * cswRecordStore 	: an instance of CSWRecordStore that will be used to populate this panel
 * addLayerHandler	: function(CSWRecord) This will be called when the user adds a layer.
 * cswRecordFilter	: function(CSWRecord) This will be called at load and whenever the underlying datastore
 *                    changes. It will be used to filter the underlying datastore
 * visibleFilterHandler : function(CSWRecord) This will be called on each record to test if they are visible on the map
 * showBoundsHandler: function(CSWRecord) called when the user wants to see a brief highlight of the records bounds
 * moveToBoundsHandler: function(CSWRecord) called when the user wants to find the location of the record bounds
 *
 */
CSWRecordGridPanel = function(id, title, description, cswRecordStore, addLayerHandler, cswRecordFilter, visibleFilterHandler, showBoundsHandler, moveToBoundsHandler) {
	this.addLayerHandler = addLayerHandler;
	this.cswRecordFilter = cswRecordFilter;
	//Create our filtered datastore copy
	var dsCopy = new CSWRecordStore();
	cswRecordStore.on('datachanged', this.internalOnDataChanged, this);
	dsCopy.copyFrom(cswRecordStore, cswRecordFilter);
	//This is so we can reference our search panel
	var searchPanelId = id + '-search-panel';
	var rowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{dataIdentificationAbstract}</p><br>')
    });

	/*
	 * knownFeaturesPanel.on("cellclick", showRecordBoundingBox, knownFeaturesPanel.on);

    knownFeaturesPanel.on("celldblclick", moveToBoundingBox, knownFeaturesPanel);
	 * */

	CSWRecordGridPanel.superclass.constructor.call(this, {
		id				 : id,
        stripeRows       : true,
        autoExpandColumn : 'title',
        plugins          : [ rowExpander ],
        viewConfig       : {scrollOffset: 0, forceFit:true},
        title            : '<span qtip="' + description + '">' + title + '</span>',
        region           :'north',
        split            : true,
        height           : 160,
        autoScroll       : true,
        store            : dsCopy,
        originalStore    : cswRecordStore,
        columns: [
            rowExpander,
            {
                id:'title',
                header: "Title",
                sortable: true,
                dataIndex: 'serviceName'
            },{
            	id : 'recordType',
            	header : '',
            	width: 18,
            	dataIndex: 'onlineResources',
            	renderer: function(value, metadata, record) {
            		for (var i = 0; i < value.length; i++) {
            			if (value[i].onlineResourceType == 'WCS' ||
            				value[i].onlineResourceType == 'WFS') {
            				return '<div style="text-align:center"><img src="img/binary.png" width="16" height="16" align="CENTER"/></div>';
            			}
            		}

            		return '<div style="text-align:center"><img src="img/picture.png" width="16" height="16" align="CENTER"/></div>';
            	}
            }, {
            	id:'search',
            	header: '',
            	width: 45,
            	dataIndex: 'geographicElements',
            	resizable: false,
            	menuDisabled: true,
            	sortable: false,
            	fixed: true,
            	renderer: function (value) {
            		if (value.length > 0) {
            			return '<img src="img/magglass.gif"/>';
            		} else {
            			return '';
            		}
            	}
            },{
                id:'contactOrg',
                header: "Provider",
                width: 160,
                sortable: true,
                dataIndex: 'contactOrganisation',
                hidden:true
            }
        ],
        bbar: [{
            text:'Add Layer to Map',
            tooltip:'Add Layer to Map',
            iconCls:'add',
            pressed:true,
            scope:this,
            handler: function() {
        		var cswRecordToAdd = new CSWRecord(this.getSelectionModel().getSelected());
        		addLayerHandler(cswRecordToAdd);
        	}
        }],

        view: new Ext.grid.GroupingView({
            forceFit:true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
            emptyText: '<font size=3px color=#ff0000> No services currently available </font>',
            emptyGroupText : 'Unknown',
            deferEmptyText:false
        }),
        tbar: [
               '<span qtip="Quickly find layers by typing the name here">Search: </span>', ' ',
               new Ext.ux.form.ClientSearchField({
                   store: dsCopy,
                   width:200,
                   id: searchPanelId,
                   fieldName:'serviceName'
               }),
               new Ext.Button({
                   text     :'Visible',
                   tooltip  :'Display only layers in present view window',
                   handler  : function() {
		       	       var searchPanel = Ext.getCmp(searchPanelId);
		    	       searchPanel.runCustomFilter('<visible layers>', function(rec){
		    	    	   return visibleFilterHandler(new CSWRecord(rec));
		    	       });
                   }
               })
           ],
        listeners: {
        	cellclick : function (grid, rowIndex, colIndex, e) {
            	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
            	var cswRecord = grid.getStore().getCSWRecordAt(rowIndex);
            	if (fieldName === 'geographicElements') {
            		e.stopEvent();

	            	showBoundsHandler(cswRecord);
            	} else if (fieldName === 'onlineResources') {
            		e.stopEvent();

            		//Close an existing popup
            		if (this.onlineResourcesPopup && this.onlineResourcesPopup.isVisible()) {
            			this.onlineResourcesPopup.close();
            		}

            		this.onlineResourcesPopup = new CSWRecordDescriptionWindow(cswRecord);
            		this.onlineResourcesPopup.show(e.getTarget());
            	}


        	},

        	celldblclick : function (grid, rowIndex, colIndex, e) {
            	var record = grid.getStore().getAt(rowIndex);
            	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
            	if (fieldName === 'geographicElements') {
            		e.stopEvent();

                	moveToBoundsHandler(grid.getStore().getCSWRecordAt(rowIndex));
            	}


        	},

        	mouseover : function(e, t) {
                e.stopEvent();

                var row = e.getTarget('.x-grid3-row');
                var col = e.getTarget('.x-grid3-col');


                //if there is no visible tooltip then create one, if on is visible already we dont want to layer another one on top
                if (col !== null && (!this.currentToolTip || !this.currentToolTip.isVisible())) {

                    //get the actual data record
                    var theRow = this.getView().findRow(row);
                    var cswRecord = new CSWRecord(this.getStore().getAt(theRow.rowIndex));

                    var autoWidth = !Ext.isIE6 && !Ext.isIE7;

                    //This is for the 'record type' column
                    if (col.cellIndex == '2') {

                    	this.currentToolTip = new Ext.ToolTip({
                            target: e.target ,
                            title: 'Service Information for ' + cswRecord.getServiceName(),
                            autoHide : true,
                            html: 'Click for detailed information about the web services this layer utilises',
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

CSWRecordGridPanel.prototype.addLayerHandler = null;
CSWRecordGridPanel.prototype.cswRecordFilter = null;


Ext.extend(CSWRecordGridPanel, Ext.grid.GridPanel, {
	/**
	 * Whenever the internal datastore changes, update our filtered copy
	 * @param store
	 * @return
	 */
	internalOnDataChanged	: function() {
		this.getStore().copyFrom(this.initialConfig.originalStore, this.cswRecordFilter);
	}
});