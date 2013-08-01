/**
 * An abstract base class to be extended.
 *
 * Represents a grid panel for containing layers
 * that haven't yet been added to the map. Each row
 * will be grouped under a heading, contain links
 * to underlying data sources and have a spatial location
 * that can be viewed by the end user.
 *
 * This class is expected to be extended for usage within
 * the 'Registered Layers', 'Known Layers' and 'Custom Layers'
 * panels in the portal. Support for KnownLayers/CSWRecords and
 * other row types will be injected by implementing the abstract
 * functions of this class
 *
 * Adds the following events :
 *      addlayerrequest(this, Ext.data.Model) - raised whenever the user has indicated to this panel that it wishes
 *                                              to add a specified record to the map as a new layer
 */
Ext.define('portal.widgets.panel.CSWReportPagingPanel', {
    extend : 'Ext.grid.Panel',
    alias: 'widget.cswpagingpanel',

    map : null,
    cswRecordStore : null,
    pageSize: 20,

    constructor : function(cfg) {
        var me = this;

        this.cswRecordStore = Ext.create('Ext.data.Store', {
            id:'CSWPagingStore',
            autoLoad: false,
            model : 'portal.csw.CSWRecord',
            pageSize: this.pageSize,
            proxy: {
                type: 'ajax',
                method : 'GET',
                noCache : false,
                url: 'getUncachedCSWRecords.do',
                extraParams:cfg.cswConfig.extraParams,
                reader: {
                    type: 'json',
                    root: 'data',
                    successProperty: 'success',
                    totalProperty: 'totalResults'
                }
            }
        });


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
                    grid.cswRecordStore.load({
                        params:cfg.cswConfig.pagingConfig
                    });
                },
                itemdblclick : function(grid, record, item, index, e, eOpts ){
                    cfg.cswConfig.callback(record);
                }

            }

        });

      this.callParent(arguments);
    }
});