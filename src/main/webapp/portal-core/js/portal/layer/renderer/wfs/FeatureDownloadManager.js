/**
    Feature Download Manager

    A class for downloading features from a given URL. This class handles all querying for record counts, display
    of modal question's and downloading the actual records
*/
Ext.define('portal.layer.renderer.wfs.FeatureDownloadManager', {
    extend: 'Ext.util.Observable',

    proxyFetchUrl : null,
    proxyCountUrl : null,
    visibleMapBounds : null,
    featureSetSizeThreshold : 200,
    timeout : 1000 * 60 * 20, //20 minute timeout,
    filterParams : {},
    currentRequest : null, //the Ajax request object that is currently running (used for cancelling)


    /**
     * Accepts a Ext.util.Observable configuration object with the following extensions
     * {
     *  visibleMapBounds : [Optional] Object - An object that will be encoded as a bounding box parameter (if required). If not specified, the user will not have the option to select a visible bounds
     *  proxyFetchUrl : String - The URL for proxying requests for the actual records
     *  proxyCountUrl : [Optional] String - The URL for proxying requests for the count of records
     *  featureSetSizeThreshold : [Optional] Number - The minimum number of features that need to be returned before the user is prompted (default 200)
     *  timeout : [Optional] Number - the Ajax request timeout in milliseconds (default is 20 minutes)
     *  filterParams : [Optional] Any additional filter parameters to apply to the request
     * }
     *
     * Registers the following events
     *  success : function(FeatureDownloadManager this, Object filterParamsUsed, Object data, Object debugInfo)
     *  error : function(FeatureDownloadManager this, [Optional] String message, [Optional] Object debugInfo)
     *  cancelled : function(FeatureDownloadManager this)
     */
    constructor : function(cfg) {
        this.proxyFetchUrl = cfg.proxyFetchUrl;
        this.proxyCountUrl = cfg.proxyCountUrl;
        if (cfg.featureSetSizeThreshold) {
            this.featureSetSizeThreshold = cfg.featureSetSizeThreshold;
        }
        if (cfg.filterParams) {
            this.filterParams = cfg.filterParams;
        }
        this.visibleMapBounds = cfg.visibleMapBounds;
        if (cfg.timeout) {
            this.timeout = cfg.timeout;
        }
       
        this.listeners = cfg.listeners;
        this.callParent(arguments);
    },

    /**
     * Utility function for building an Object containing parameters to be sent. Bounding box is optional
     */
    _buildRequestParams : function(boundingBox, maxFeatures) {
        var params = {};
        Ext.apply(params, this.filterParams);
        if (boundingBox) {
            params.bbox = Ext.JSON.encode(boundingBox);
        }
        if (maxFeatures) {
            params.maxFeatures = maxFeatures;
        } else {
            params.maxFeatures = 0;
        }
        return params;
    },

    /**
     * Causes an AJAX request for all feature data to be created and fired off
     * @param boundingBox [Optional] String bounding box encoded as a string
     */
    _doDownload : function (boundingBox, maxFeatures) {
        var params = this._buildRequestParams(boundingBox, maxFeatures);
        this.currentRequest = portal.util.Ajax.request({
            url : this.proxyFetchUrl,
            params : params,
            callback : function(success, data, message, debugInfo) {
                if (success) {
                    this.fireEvent('success', this, params, data, debugInfo);
                } else {
                    this.fireEvent('error', this, message, debugInfo);
                }
            },
            timeout : this.timeout,
            scope : this
        });
    },

    /**
     * Internal function for handling a response from a 'count proxy url'
     */
    _doCount : function(success, data, message, debugInfo) {
        if (!success) {
            this.fireEvent('error', this, message, debugInfo);
            return;
        }

        if (data > this.featureSetSizeThreshold) {
            var callingInstance = this;

            //If we have too many features, tell the user
            var win = Ext.create('Ext.window.Window', {
                width : 600,
                height : 150,
                closable : false,
                modal : true,
                title : 'Warning: Large feature set',
                layout : 'fit',
                items : [{
                    xtype : 'component',
                    autoEl : {
                        tag: 'span',
                        html : Ext.util.Format.format('<p>You are about to display <b>{0}</b> features, doing so could make the portal run extremely slowly. Would you still like to continue?</p><br/><p>Alternatively you can abort this operation, adjust your zoom/filter and then try again.</p>', data)
                    },
                    cls : 'ext-mb-text'
                }],
                dockedItems : [{
                    xtype : 'toolbar',
                    dock : 'bottom',
                    ui : 'footer',
                    layout : {
                        type : 'hbox',
                        pack : 'center'
                    },
                    items : [{
                        xtype : 'button',
                        text : Ext.util.Format.format('Display {0} features', data),
                        handler : function(button) {
                            callingInstance._doDownload(callingInstance._visibleMapBounds);
                            button.ownerCt.ownerCt.close();
                        }
                    },{
                        xtype : 'button',
                        text : 'Abort operation',
                        handler : function(button) {
                            callingInstance.fireEvent('cancelled', callingInstance);
                            button.ownerCt.ownerCt.close();
                        }
                    }]
                }]
            });
            win.show();
        } else {
            //If we have an acceptable number of records, this is how we shall proceed
            this._doDownload(this.visibleMapBounds);
        }
        
    },

    /**
     * Function for starting a download through this FeatureDownloadManager
     */
    startDownload : function() {
        //Firstly attempt to discern how many records are available, this will affect how we proceed
        //If we dont have a proxy for doing this, then just download everything
        if (this.proxyCountUrl && this.proxyCountUrl.length > 0) {
            this.currentRequest = portal.util.Ajax.request({
                url : this.proxyCountUrl,
                params : this._buildRequestParams(this.visibleMapBounds),
                scope : this,
                timeout : this.timeout,
                callback : this._doCount
            });
        } else {
            //if we dont have a URL to proxy our count requests through then just
            //attempt to download all visible features (it's better than grabbing thousands of features)
            this._doDownload(this.visibleMapBounds, this.featureSetSizeThreshold);
        }
    },

    /**
     * Causees any in progress downloads to be aborted immediately. If there are no downloads,
     * nothing will occur.
     */
    abortDownload : function() {
        if (this.currentRequest) {
            Ext.Ajax.abort(this.currentRequest);
        }
    }
});
