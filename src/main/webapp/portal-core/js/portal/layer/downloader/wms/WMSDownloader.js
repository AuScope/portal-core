/**
 * A downloader that creates an Ext.Window specialised into showing a
 * dialog for the user to download features from a WMS in a zip file
 */
Ext.define('portal.layer.downloader.wms.WMSDownloader', {
    extend: 'portal.layer.downloader.Downloader',

    constructor : function(cfg) {
        this.callParent(arguments);
    },

    /**
     * An implementation of an abstract method, see parent method for details
     *
     * layer - portal.layer.Layer that owns resources
     * resources - an array of data sources that were used to render data
     * renderedFilterer - custom filter that was applied when rendering the specified data sources
     * currentFilterer - The value of the custom filter, this may differ from renderedFilterer if the
     *                   user has updated the form/map without causing a new render to occur
     */
    downloadData : function(layer, resources, renderedFilterer, currentFilterer) {
        var me = this;

        //Assumption - we are only interested in 1 WMS resource
        var wmsResource = portal.csw.OnlineResource.getFilteredFromArray(resources)[0];

        var formatStore = Ext.create('Ext.data.Store',{
            fields : ['format'],
            proxy : {
                type : 'ajax',
                url : 'getLayerFormats.do',
                extraParams : {
                    serviceUrl : wmsResource.get('url')
                },
                reader: {
                    type : 'json',
                    rootProperty : 'data'
                }
            },
            autoLoad : true
        });

        //Create a popup showing our options
        Ext.create('Ext.Window', {
            title : 'Download Options',
            buttonAlign : 'right',
            width : 300,
            height : 150,
            modal : true,
            layout : {
                type : 'vbox',
                align : 'stretch'
            },
            buttons : [{
               text : 'Download',
               iconCls : 'download',
               handler : function(button) {
                   var popup = button.ownerCt.ownerCt;
                   var combo = popup.items.getAt(1);

                   var format = combo.getValue();
                   if (!format || format.length === 0) {
                       return;
                   }

                   var vb = me.map.getVisibleMapBounds();
                   var mapSize = me.map.getMapSizeInPixels();
                   var bboxString = Ext.util.Format.format('{0},{1},{2},{3}',
                           (vb.westBoundLongitude < 0 ? parseInt(vb.westBoundLongitude) + 360.0 : vb.westBoundLongitude),
                           vb.southBoundLatitude,
                           (vb.eastBoundLongitude < 0 ? parseInt(vb.eastBoundLongitude) + 360.0 : vb.eastBoundLongitude),
                           vb.northBoundLatitude);

                   var queryString = Ext.Object.toQueryString({
                      request : 'GetMap',
                      service : 'WMS',
                      version : '1.1.1',
                      layers : wmsResource.get('name'),
                      format : format,
                      styles : '',
                      bgcolor : '0xFFFFFF',
                      transparent : true,
                      srs : 'EPSG:4326',
                      bbox : bboxString,
                      width : mapSize.getWidth(),
                      height : mapSize.getHeight()
                   });

                   //This is the WMS request URL (we will be proxying it through our local zipping proxy)
                   var wmsRequest = Ext.urlAppend(wmsResource.get('url'), queryString);

                   //Pass the WMS request to our zipping proxy
                   portal.util.FileDownloader.downloadFile('downloadDataAsZip.do', {
                       serviceUrls : [wmsRequest],
                       filename : 'WMSDownload.zip'
                   });

                   popup.close();
               }
            }],
            items : [{
                xtype : 'label',
                style : 'font-size: 12px;',
                text : 'What file format would you like to download this layer as?'
            },{
                xtype : 'combo',
                anchor: '100%',
                emptyText : 'Please select a file format.',
                forceSelection: true,
                queryMode: 'remote',
                triggerAction: 'all',
                typeAhead: true,
                typeAheadDelay: 500,
                store : formatStore,
                displayField : 'format',
                valueField : 'format'
            }]
        }).show();
    }
});
