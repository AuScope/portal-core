/**
 * An extension of a normal GridPanel that makes it specialize into rendering a CSWRecordStore.
 *
 * This particular specialization will update the CSWRecordStore on the fly with whatever layers
 * are available at a configurable WMS location.
 *
 * id				: unique ID to identify this grid
 * title 			: The title this grid panel will display
 * cswRecordStore 	: an instance of CSWRecordStore that will be used to populate this panel. It will be regularly loaded
 * addLayerHandler	: function(CSWRecord) This will be called when the user adds a layer.
 * cswRecordFilter	: function(CSWRecord) This will be called at load and whenever the underlying datastore
 *                    changes. It will be used to filter the underlying datastore
 * showBoundsHandler: function(CSWRecord) called when the user wants to see a brief highlight of the records bounds
 * moveToBoundsHandler: function(CSWRecord) called when the user wants to find the location of the record bounds
 *
 */
CustomLayersGridPanel = function(id, title, description, cswRecordStore, addLayerHandler, showBoundsHandler, moveToBoundsHandler) {
	this.addLayerHandler = addLayerHandler;

	var rowExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{dataIdentificationAbstract}</p><br>')
    });

	CustomLayersGridPanel.superclass.constructor.call(this, {
		id				 : id,
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
        store            : cswRecordStore,
        loadMask         : true,
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
            }
        ],
        tbar: [
            '<span qtip="You will need to enter the fully qualified URL of a WMS. Any layers that can be projected in EPSG:4326 will then be made available for display."' +
            'style="color:#15428B; font-weight:bold">Enter WMS Url:</span>',
            ' ',
            {
            	xtype:'button',
            	text:'<font color=#ff0000><b>?</b> </font>',
            	qtip: 'You will need to enter the fully qualified URL of a WMS. Any layers that can be projected in EPSG:4326 will then be made available for display.',
        	   	handler: function(){
            		Ext.Msg.show({
            			title:'Hint',
            			msg: 'You will need to enter the fully qualified URL of a WMS. Any layers that can be projected in EPSG:4326 will then be made available for display.',
            			buttons: Ext.Msg.OK
            		});
            	}

            },
            new Ext.ux.form.SearchTwinTriggerField({
                store: cswRecordStore,
                width:243,
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
            scope : this,
            handler: function() {
	        	var cswRecordToAdd = new CSWRecord(this.getSelectionModel().getSelected());
	    		addLayerHandler(cswRecordToAdd);
            }
        }],
        listeners: {
        	cellclick : function (grid, rowIndex, colIndex, e) {
            	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
            	var cswRecord = grid.getStore().getCSWRecordAt(rowIndex);
            	if (fieldName === 'geographicElements') {
            		e.stopEvent();
	            	showBoundsHandler(cswRecord);
            	}else if (fieldName === 'onlineResources') {
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
                    	if(cswRecord.internalRecord.data.geographicElements.length > 0){
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
        }
    });
};

CustomLayersGridPanel.prototype.addLayerHandler = null;


Ext.extend(CustomLayersGridPanel, Ext.grid.GridPanel, {
	/**
	 * Whenever the internal datastore changes, update our filtered copy
	 * @param store
	 * @return
	 */
	internalOnDataChanged	: function(store) {
		this.getStore().copyFrom(store, this.cswRecordFilter);
	}
});