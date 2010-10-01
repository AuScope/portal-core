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
CustomLayersGridPanel = function(id, title, cswRecordStore, addLayerHandler, showBoundsHandler, moveToBoundsHandler) {
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
        title            : title,
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
            '<span style="color:#15428B; font-weight:bold">Enter WMS Url: </span>',
            ' ',
            new Ext.ux.form.SearchTwinTriggerField({
                store: cswRecordStore,
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
            scope : this,
            handler: function() {
	        	var cswRecordToAdd = new CSWRecord(this.getSelectionModel().getSelected());
	    		addLayerHandler(cswRecordToAdd);
            }
        }],
        listeners: {
        	cellclick : function (grid, rowIndex, colIndex, e) {
            	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
            	if (fieldName !== 'geographicElements') {
            		return;
            	}
            	
            	e.stopEvent();
            	
            	showBoundsHandler(grid.getStore().getCSWRecordAt(rowIndex));
        	},
        	
        	celldblclick : function (grid, rowIndex, colIndex, e) {
            	var record = grid.getStore().getAt(rowIndex);
            	var fieldName = grid.getColumnModel().getDataIndex(colIndex);
            	if (fieldName !== 'geographicElements') {
            		return;
            	}
            	
            	e.stopEvent();
            	
            	moveToBoundsHandler(grid.getStore().getCSWRecordAt(rowIndex));
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