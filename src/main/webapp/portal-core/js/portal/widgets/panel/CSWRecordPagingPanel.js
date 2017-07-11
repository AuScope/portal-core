/**
 * This is a generic paging panal for all CSW Records.
 */
Ext.define('portal.widgets.panel.CSWRecordPagingPanel', {
    extend : 'Ext.grid.Panel',
    alias: 'widget.cswrecordpagingpanel',

    cswRecordStore : null,
    pageSize: 15,

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
                    return this._serviceInformationRenderer(value);
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
                    this._callBackDisplayInfo(record);
                }

            }

        });

      this.callParent(arguments);
    },


    /**
     * Call back function to handle double click of the CSW to bring up a window to display its information
     */
    _callBackDisplayInfo : function(record){
        Ext.create('Ext.window.Window', {
            title : 'CSW Record Information',
            modal: true,
            items : [{
                xtype : 'cswmetadatapanel',
                width : 500,
                border : false,
                cswRecord : record
            }]
        }).show();
    },


    /**
     * Internal method, acts as an ExtJS 4 column renderer function for rendering
     * the service information of the record.
     *
     * http://docs.sencha.com/ext-js/4-0/#!/api/Ext.grid.column.Column-cfg-renderer
     */
    _serviceInformationRenderer : function(onlineResources) {

        var containsDataService = false;
        var containsImageService = false;

        //We classify resources as being data or image sources.
        for (var i = 0; i < onlineResources.length; i++) {
            switch(onlineResources[i].get('type')) {
            case portal.csw.OnlineResource.WFS:
            case portal.csw.OnlineResource.WCS:
            case portal.csw.OnlineResource.SOS:
            case portal.csw.OnlineResource.OPeNDAP:
            case portal.csw.OnlineResource.CSWService:
            case portal.csw.OnlineResource.IRIS:
                containsDataService = true;
                break;
            case portal.csw.OnlineResource.WMS:
            case portal.csw.OnlineResource.WWW:
            case portal.csw.OnlineResource.FTP:
            case portal.csw.OnlineResource.CSW:
            case portal.csw.OnlineResource.UNSUPPORTED:
                containsImageService = true;
                break;
            }
        }

        var iconPath = null;
        if (containsDataService) {
            iconPath = 'portal-core/img/binary.png'; //a single data service will label the entire layer as a data layer
        } else if (containsImageService) {
            iconPath = 'portal-core/img/picture.png';
        } else {
            iconPath = 'portal-core/img/cross.png';
        }

        return this._generateHTMLIconMarkup(iconPath);
    },

    /**
     * Generates an Ext.DomHelper.markup for the specified imageUrl
     * for usage as an image icon within this grid.
     */
    _generateHTMLIconMarkup : function(imageUrl) {
        return Ext.DomHelper.markup({
            tag : 'div',
            style : 'text-align:center;',
            children : [{
                tag : 'img',
                width : 16,
                height : 16,
                align: 'CENTER',
                src: imageUrl
            }]
        });
    }

});
