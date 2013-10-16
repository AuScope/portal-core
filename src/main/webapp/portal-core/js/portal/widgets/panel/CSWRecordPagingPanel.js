/**
 * This is a generic paging panal for all CSW Records.
 */
Ext.define('portal.widgets.panel.CSWRecordPagingPanel', {
    extend : 'Ext.grid.Panel',
    alias: 'widget.cswrecordpagingpanel',

    cswRecordStore : null,
    pageSize: 20,

    constructor : function(cfg) {
        var me = this;

        this.cswRecordStore = cfg.store;


        Ext.apply(cfg, {
            hideHeaders : false,
            height: 200,
            layout : 'fit',
            width: 400,
            columns: [{
                header: 'Name',
                dataIndex: 'name',
                width:  390

            },{
                header: 'Administrative Area',
                dataIndex: 'adminArea',
                width: 110
            },{
                header:'Type',
                dataIndex:  'onlineResources',
                width: 100,
                renderer : function(value){
                    return '<div style="text-align:center"><img src="img/picture.png" width="16" height="16" align="CENTER"/></div>';
                }
            }],
            store : this.cswRecordStore,
            viewConfig : {
                forceFit : true,
                enableRowBody:true
            },

            loadMask : {msg : 'Performing CSW Filter...'},
            multiSelect: true,
            dockedItems: [{
                xtype: 'pagingtoolbar',
                pageSize: this.pageSize,
                store: this.cswRecordStore,
                dock: 'bottom',
                displayInfo: true
            }],
            listeners : {
                afterrender : function(grid,eOpts) {
                    grid.cswRecordStore.load();
                },
                itemdblclick : function(grid, record, item, index, e, eOpts ){

                }

            }

        });

      this.callParent(arguments);
    }
});