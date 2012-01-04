/**
 * A Ext.Window specialised into showing a dialog for the user to download features from a WFS in a zip file
 */
Ext.ns('WFS');
WFS.DownloadWindow = Ext.extend(Ext.Window, {
    DOWNLOAD_CURRENTLY_VISIBLE : 1,
    DOWNLOAD_ORIGINALLY_VISIBLE : 2,
    DOWNLOAD_ALL : 3,

    activeLayerRecord : null,
    currentTooltip : null,

    /**
     *  cfg can contain all the elements for Ext.Window along with the following additions
     *
     *  {
     *      activeLayerRecord - the active layer containing
     *      currentVisibleBounds - BBox - the current visible bounds of the map
     *  }
     *
     *  Also adds the following events
     *  {
     *      renderbbox - function(DownloadWindow this, BBox bbox) - raised when the user is requesting a visualisation of a bounding box
     *      scrolltobbox - function(DownloadWindow this, BBox bbox) - raised when the user is requesting the map to scroll to a specified bounding box
     *  }
     *
     */
    constructor : function(cfg) {
        //Decode the cfg object into the bits we are interested in
        var me = this;
        var filterParameters = cfg.activeLayerRecord.getLastFilterParameters();
        if (!filterParameters) {
            filterParameters = {};
        }
        var isDifferentBBox = filterParameters.bbox !== Ext.util.JSON.encode(cfg.currentVisibleBounds);
        var currentlyVisibleBBox = cfg.currentVisibleBounds;
        var originallyVisibleBBox = null; //this may be undefined

        //Turn our BBox JSON string into a valid BBox object
        if (filterParameters.bbox) {
            var bbox = Ext.util.JSON.decode(filterParameters.bbox);
            originallyVisibleBBox = new BBox(bbox.northBoundLatitude, bbox.southBoundLatitude, bbox.eastBoundLongitude, bbox.westBoundLongitude);
        }

        //Set our default values (if they haven't been set)
        Ext.applyIf(cfg, {
            title : 'Download Options'
        });

        //Set our 'always override' values
        Ext.apply(cfg, {
            layout : 'fit',
            buttonAlign : 'right',
            buttons : [{
               text : 'Download',
               iconCls : 'download',
               handler : function() {
                   var bboxJson = '';
                   var radioGroup = me.findByType('radiogroup')[0];

                   switch(radioGroup.getValue().initialConfig.inputValue) {
                   case me.DOWNLOAD_CURRENTLY_VISIBLE:
                       bboxJson = Ext.util.JSON.encode(cfg.currentVisibleBounds);
                       break;
                   case me.DOWNLOAD_ORIGINALLY_VISIBLE:
                       bboxJson = me.activeLayerRecord.getLastFilterParameters().bbox;
                       break;
                   default:
                       bboxJson = undefined;
                       break;
                   }

                   me._handleDownload(bboxJson);
               }
            }],
            items : [{
                xtype : 'fieldset',
                layout : 'fit',
                items : [{
                    xtype : 'label',
                    style : 'font-size: 12px;',
                    text : 'The portal will make a download request on your behalf and return the results in a ZIP archive. How would you like the portal to filter your download?',
                },{
                    xtype : 'spacer',
                    width : 10,
                    height : 10
                },{
                    //Our radiogroup can see its item list vary according to the presence of bounding boxes
                    xtype : 'radiogroup',
                    columns : [0.99, 18],
                    listeners : {
                        change : function(radioGroup, radio) {
                            switch(radio.initialConfig.inputValue) {
                            case me.DOWNLOAD_CURRENTLY_VISIBLE:
                                me.fireEvent('scrolltobbox', me, currentlyVisibleBBox);
                                break;
                            case me.DOWNLOAD_ORIGINALLY_VISIBLE:
                                me.fireEvent('scrolltobbox', me, originallyVisibleBBox);
                                break;
                            }
                        }
                    },
                    items : [{
                        boxLabel : 'Filter my download using the current visible map bounds.',
                        name : 'wfs-download-radio',
                        inputValue : me.DOWNLOAD_CURRENTLY_VISIBLE,
                        hidden : !isDifferentBBox,
                        checked : isDifferentBBox

                    },{
                        xtype : 'box',
                        autoEl : {
                            tag : 'img',
                            src : 'img/magglass.gif',
                            qtip : 'Click to display the spatial bounds, double click to pan the map so they are visible.'
                        },
                        width : 18,
                        height : 21,
                        hidden : !isDifferentBBox,
                        style : 'padding:3px 0px 0px 0px;',
                        listeners : {
                            render : function(c) {
                                c.getEl().on('click', function(e) {
                                    me.fireEvent('renderbbox', me, currentlyVisibleBBox);
                                }, c);
                                c.getEl().on('dblclick', function(e) {
                                    me.fireEvent('scrolltobbox', me, currentlyVisibleBBox);
                                }, c);
                            }
                        }
                    },{
                        boxLabel : 'Filter my download using the original bounds that were used to load the layer.',
                        name : 'wfs-download-radio',
                        inputValue : me.DOWNLOAD_ORIGINALLY_VISIBLE,
                        checked : !isDifferentBBox && originallyVisibleBBox !== null,
                        hidden : originallyVisibleBBox === null
                    },{
                        xtype : 'box',
                        autoEl : {
                            tag : 'img',
                            src : 'img/magglass.gif',
                            qtip : 'Click to display the spatial bounds, double click to pan the map so they are visible.'
                        },
                        width : 18,
                        height : 21,
                        style : 'padding:3px 0px 0px 0px;',
                        hidden : originallyVisibleBBox === null,
                        listeners : {
                            render : function(c) {
                                c.getEl().on('click', function(e) {
                                    me.fireEvent('renderbbox', me, originallyVisibleBBox);
                                }, c);
                                c.getEl().on('dblclick', function(e) {
                                    me.fireEvent('scrolltobbox', me, originallyVisibleBBox);
                                }, c);
                            }
                        }
                    },{
                        boxLabel : 'Don\'t filter my download. Return all available data.',
                        name : 'wfs-download-radio',
                        inputValue : me.DOWNLOAD_ALL,
                        checked : !isDifferentBBox && originallyVisibleBBox === null
                    }]
                }]
            }],

        });

        WFS.DownloadWindow.superclass.constructor.call(this, cfg);

        //Configure our events / object
        this.addEvents({
            'renderbbox' : true,
            'scrolltobbox' : true
        });
        this.listeners = cfg.listeners;
        this.activeLayerRecord = cfg.activeLayerRecord;

    },

    /**
     * Handles a download for the specified bounding box
     *
     * bbox - A JSON string representing a BBox object
     */
    _handleDownload : function(bbox) {
        var filterParameters = this.activeLayerRecord.getLastFilterParameters();
        if (!filterParameters) {
            filterParameters = {};
        }
        var downloadParameters = {
            serviceUrls : []
        };
        var proxyUrl = this.activeLayerRecord.getProxyFetchUrl() &&  this.activeLayerRecord.getProxyFetchUrl().length > 0 ? this.activeLayerRecord.getProxyFetchUrl() : 'getAllFeatures.do';
        var prefixUrl = window.location.protocol + "//" + window.location.host + WEB_CONTEXT + "/" + proxyUrl + "?";
        var cswRecords = this.activeLayerRecord.getCSWRecordsWithType('WFS');

        //Iterate our WFS records and generate the array of PORTAL BACKEND requests that will be
        //used to proxy WFS requests. That array will be sent to a backend handler for making
        //multiple requests and zipping the responses into a single stream for the user to download.
        for (var i = 0; i < cswRecords.length; i++) {
            var wfsOnlineResources = cswRecords[i].getFilteredOnlineResources('WFS');
            var cswWfsRecordCount = cswRecords.length;

            for (var j = 0; j < wfsOnlineResources.length; j++) {
                //Create a copy of the last set of filter parameters
                var url = wfsOnlineResources[j].url;
                var currentFilterParameters = {};
                Ext.apply(currentFilterParameters, filterParameters);

                currentFilterParameters.serviceUrl = wfsOnlineResources[j].url;
                currentFilterParameters.typeName = wfsOnlineResources[j].name;
                currentFilterParameters.maxFeatures = 0;
                currentFilterParameters.bbox = bbox;

                if(this.activeLayerRecord.isEndpointIncluded(url)) {
                    downloadParameters.serviceUrls.push(Ext.urlEncode(currentFilterParameters, prefixUrl));
                }
            }
        }

        //download the service URLs through our zipping proxy
        FileDownloader.downloadFile('downloadGMLAsZip.do', downloadParameters);
        this.close();
    }
});

WFS.DownloadWindowHandler = function(a,b,c) {
    alert(String.format('a={0}\nb={1}\nc={2}',a,b,c));
}