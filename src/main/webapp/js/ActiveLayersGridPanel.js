/**
 * An extension of a normal GridPanel that makes it specialize into rendering an ActiveLayersStore.
 *
 * id				: String - unique ID to identify this grid
 * title 			: String - The title this grid panel will display
 * activeLayersStore: ActiveLayersStore - Will provide the content for this grid
 * layerSelectionHandler : function(ActiveLayerRecord) - called whenever a layer is selected
 * layerMoveHandler : function() - called whenever a layer is DnD moved
 * layerRemoveHandler : function(ActiveLayerRecord) - called whenever a layer is removed from the grid
 * layerVisibilityHandler: function(ActiveLayerRecord, Boolean, Boolean) called whenever a layer's visibility changes
 *                         The first boolean is the current visibility state
 *                         The second boolean is whether the filter (if any) for this layer should be applied
 */
ActiveLayersGridPanel = function(id, title, description, activeLayersStore, layerSelectionHandler, layerMoveHandler, layerRemoveHandler, layerStopRequest, layerVisibilityHandler) {
	this.layerSelectionHandler = layerSelectionHandler;
	this.layerMoveHandler = layerMoveHandler;
	this.layerRemoveHandler = layerRemoveHandler;
	this.layerVisibilityHandler = layerVisibilityHandler;
	this.layerStopRequest = layerStopRequest;

	 // custom column plugin example
    var activeLayersPanelCheckColumn = new Ext.ux.grid.EventCheckColumn({
        header: "Visible",
        dataIndex: 'layerVisible',
        width: 30,
        handler: function(record, data) {
    		layerVisibilityHandler(new ActiveLayersRecord(record),data,true);
        }
    });

    var activeLayersPanelExpander = new Ext.grid.RowExpander({
        tpl : new Ext.Template('<p>{description}</p><br>')
    });

	var activeLayersRowDragPlugin = new Ext.ux.dd.GridDragDropRowOrder({
        copy: false,
        scrollable: true,
        listeners: {afterrowmove: layerMoveHandler}
     });

	var activeLayersRemoveButton = {
            text:'Remove Layer',
            tooltip:'Remove Layer',
            iconCls:'remove',
            pressed:true,
            handler: function() {
                var record = activeLayersPanel.getSelectionModel().getSelected();
                if (record === null) {
                    return;
                }

                layerRemoveHandler(new ActiveLayersRecord(record));
            }
        };
	

	ActiveLayersGridPanel.superclass.constructor.call(this, {
        plugins: [activeLayersPanelCheckColumn,
                  activeLayersPanelExpander,
                  activeLayersRowDragPlugin],
        id : id,
        stripeRows: true,
        autoExpandColumn: 'title',
        viewConfig: {scrollOffset: 0,forceFit:true},
        title: '<span qtip="' + description + '">' + title + '</span>',
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
                dataIndex: 'keyIconHtml',
                align: 'center'
            },
            {
                id:'isLoading',
                header: "",
                width: 25,
                sortable: false,
                dataIndex: 'isLoading',
                align: 'center',
                renderer: function(value, metaData, record) {
            		if (value) {
            			return '<img src="js/external/extjs/resources/images/default/grid/loading.gif">';
            		} else {
            			return '<img src="js/external/extjs/resources/images/default/grid/nowait.gif">';
            		}
            	}
            },
            {
                id:'title',
                header: "Title",
                width: 100,
                sortable: false,
                dataIndex: 'title'
            },
            activeLayersPanelCheckColumn,
            {
                id:'downloadIconHtml',
                header: "",
                width: 20,
                sortable: false,
                dataIndex: 'keyIconHtml', //this doesn't matter, its not used
                align: 'center',
                renderer: function(value, metaData, record) {
            		var activeLayersRecord = new ActiveLayersRecord(record);
            		var wfsRecords = activeLayersRecord.getCSWRecordsWithType('WFS');
            		var wcsRecords = activeLayersRecord.getCSWRecordsWithType('WCS');

            		if(!activeLayersRecord.hasData()) {
            			if(wfsRecords.length > 0 || wcsRecords.length > 0) {
            			    return '<a href=".." id="mylink" target="_blank"><img src="img/page_code_disabled.png"></a>';
            			} else {
                    		return '<a href=".." id="mylink" target="_blank"><img src="img/picture_link_disabled.png"></a>';
            			}
            		} else {
	            		if (wfsRecords.length > 0 || wcsRecords.length > 0) {
	            			return '<a href=".." id="mylink" target="_blank"><img src="img/page_code.png"></a>';
	            		} else {
	            		    return '<a href=".." id="mylink" target="_blank"><img src="img/picture_link.png"></a>';
	            		}
            		}
            	}
            }
        ],
        bbar: [
            activeLayersRemoveButton
        ],
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true,
            listeners: {
                rowselect: {
                    fn: function(sm, index, record) {
        				layerSelectionHandler(new ActiveLayersRecord(record));
        			}
                }
            }
        }),
        listeners : {
        	cellcontextmenu : function(grid, rowIndex, colIndex, event) {
                //Stop the event propogating
                event.stopEvent();

                //Ensure the row that is right clicked gets selected
                activeLayersPanel.getSelectionModel().selectRow(rowIndex);

                //Create the context menu to hold the buttons
                var contextMenu = new Ext.menu.Menu();
                contextMenu.add(activeLayersRemoveButton);
                //Show the menu
                contextMenu.showAt(event.getXY());
            }
        }
    });
};

ActiveLayersGridPanel.layerSelectionHandler = null;
ActiveLayersGridPanel.layerMoveHandler = null;
ActiveLayersGridPanel.layerRemoveHandler = null;
ActiveLayersGridPanel.layerVisibilityHandler = null;
ActiveLayersGridPanel.layerStopRequest = null;



Ext.extend(ActiveLayersGridPanel, Ext.grid.GridPanel, {

});