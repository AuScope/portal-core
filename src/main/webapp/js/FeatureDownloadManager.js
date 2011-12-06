/**
    Feature Download Manager

    A class for downloading features from a given URL. This class handles all querying for record counts, display
    of modal question's and downloading the actual records
*/
FeatureDownloadManager = Ext.extend(Ext.util.Observable, {
    _proxyFetchUrl : null,
    _proxyCountUrl : null,
    _featureSetSizeThreshold : 200,
    _timeout : 1000 * 60 * 20, //20 minute timeout,
    _filterParams : {},
    _visibleMapBounds : null,

    /**
     * Accepts a Ext.util.Observable configuration object with the following extensions
     * {
     *  visibleMapBounds : [Optional] Object - An object that will be encoded as a bounding box parameter (if required). If not specified, the user will not have the option to select a visible bounds
     *  proxyFetchUrl : String - The URL for proxying requests for the actual records
     *  proxyCountUrl : [Optional] String - The URL for proxying requests for the count of records
     *  featureSetSizeThreshold : [Optional] Number - The minimum number of features that need to be returned before the user is prompted (default 200)
     *  timeout : [Optional] Number - the Ajax request timeout in milliseconds (default is 20 minutes)
     *  filterParams : [Optional] Any additional filter parameters to apply to the request
     *
     * }
     *
     * Registers the following events
     *  success : function(FeatureDownloadManager this, Object data, Object debugInfo)
     *  error : function(FeatureDownloadManager this, [Optional] String message, [Optional] Object debugInfo)
     *  cancelled : function(FeatureDownloadManager this)
     */
    constructor : function(cfg) {
        this._proxyFetchUrl = cfg.proxyFetchUrl;
        this._proxyCountUrl = cfg.proxyCountUrl;
        if (cfg.visibleMapBounds) {
            this._visibleMapBounds = cfg.visibleMapBounds;
        }
        if (cfg.featureSetSizeThreshold !== undefined) {
            this._featureSetSizeThreshold = cfg.featureSetSizeThreshold;
        }
        if (cfg.filterParams !== undefined) {
            this._filterParams = cfg.filterParams;
        }
        if (cfg.timeout !== undefined) {
            this._timeout = cfg.timeout;
        }

        this.addEvents({
            'success' : true,
            'error' : true,
            'cancelled' : true
        });
        this.listeners = cfg.listeners;
        FeatureDownloadManager.superclass.constructor.call(this, cfg);
    },

    /**
     * Dispatches an AJAX response to the appropriate success/error handler
     */
    _handleDownloadFinish : function(options, success, response) {
        if (success) {
            var jsonResponse = Ext.util.JSON.decode(response.responseText);
            if (jsonResponse) {
                if (jsonResponse.success) {
                    this.fireEvent('success', this, jsonResponse.data, jsonResponse.debugInfo);
                } else {
                    this.fireEvent('error', this, jsonResponse.msg, jsonResponse.debugInfo);
                }
            } else {
                this.fireEvent('error', this, 'Bad JSON response.', null);
            }
        } else {
            this.fireEvent('error', this, 'AJAX feature request failed.', null);
        }
    },

    /**
     * Utility function for building an Object containing parameters to be sent. Bounding box is optional
     */
    _buildRequestParams : function(boundingBox, maxFeatures) {
        var params = {};
        Ext.apply(params, this._filterParams);
        if (boundingBox) {
            params.bbox = Ext.util.JSON.encode(boundingBox);
        }
        if (maxFeatures) {
            params.maxFeatures = maxFeatures;
        }
        return params;
    },

    /**
     * Causes an AJAX request for all feature data to be created and fired off
     * @param boundingBox [Optional] String bounding box encoded as a string
     */
    _doDownload : function (boundingBox, maxFeatures) {
        Ext.Ajax.request({
            url : this._proxyFetchUrl,
            params : this._buildRequestParams(boundingBox, maxFeatures),
            callback : this._handleDownloadFinish,
            timeout : this._timeout,
            scope : this
        });
    },

    /**
     * Internal function for handling a response from a 'count proxy url'
     */
    _doCount : function(options, success, response, alreadyPrompted) {
        if (success) {
            var jsonResponse = Ext.util.JSON.decode(response.responseText);
            if (jsonResponse.data > this._featureSetSizeThreshold) {
                var win = null;
                var callingInstance = this;

                //If we have already prompted the user and they selected to only get the visible records
                //AND there are still too many records, lets be 'smart' about how we proceed
                if (alreadyPrompted) {
                    Ext.MessageBox.show({
                        buttons:{yes:'Download Visible', no:'Abort Download'},
                        fn:function (buttonId) {
                            if (buttonId == 'yes') {
                                callingInstance._doDownload(callingInstance.currentBoundingBox);
                            } else if (buttonId == 'no') {
                                callingInstance.fireEvent('cancelled', callingInstance);
                            }
                        },
                        modal : true,
                        msg : String.format('<p>There will still be {0} features visible. Would you still like to download the visible feature set?</p><br/><p>Alternatively you can cancel this download, adjust your zoom level and try again.</p>', jsonResponse.data),
                        title : 'Warning: Large feature set'
                    });
                }else {
                    //Only give the user of selecting visible bounds IF we have some visible bounds to filter on
                    var buttons = {yes:'Download All', no:'Download Visible', cancel:'Abort Download'};
                    var message = String.format('You are about to fetch {0} features, doing so could make the portal run extremely slowly. Would you like to download only the visible markers instead?', jsonResponse.data);
                    if (!this._visibleMapBounds) {
                        buttons.no = undefined;
                        message = String.format('You are about to fetch {0} features, doing so could make the portal run extremely slowly. Unfortunately the selected layer doesn\'t support any spatial subsetting, would you still like to continue?', jsonResponse.data);
                    }
                    Ext.MessageBox.show({
                        buttons : buttons,
                        fn : function (buttonId) {
                            if (buttonId == 'yes') {
                                callingInstance._doDownload(callingInstance.currentBoundingBox);
                            } else if (buttonId == 'no') {
                                callingInstance._startDownload(callingInstance._visibleMapBounds, true);
                            } else if (buttonId == 'cancel') {
                                callingInstance.fireEvent('cancelled', callingInstance);
                            }
                        },
                        modal : true,
                        msg : message,
                        title : 'Warning: Large feature set'
                    });
                }

            } else {
                //If we have an acceptable number of records, this is how we shall proceed
                this._doDownload(this.currentBoundingBox);
            }
        } else {
            //If the count download fails,
            this.fireEvent('error', this, 'AJAX count request failed.', null);
        }
    },

    /**
     * Starts a new count/download sequence that may be constrained to a specific bounds
     */
    _startDownload : function(boundingBox, alreadyPrompted) {
        this.currentBoundingBox = boundingBox;

        if (alreadyPrompted == null || alreadyPrompted == undefined)
            alreadyPrompted = false;



        //Firstly attempt to discern how many records are available, this will affect how we proceed
        //If we dont have a proxy for doing this, then just download everything
        if (this._proxyCountUrl && this._proxyCountUrl.length > 0) {
            Ext.Ajax.request({
                url : this._proxyCountUrl,
                params : this._buildRequestParams(boundingBox),
                scope : this,
                timeout : this._timeout,
                callback : function(options, success, response) {
                    this._doCount(options, success, response, alreadyPrompted);
                }
            });
        } else {
            //if we dont have a URL to proxy our count requests through then just
            //attempt to download all visible features (it's better than grabbing thousands of features)
            this._doDownload(this._visibleMapBounds, this._featureSetSizeThreshold);
        }
    },

    /**
     * Function for starting a download through this FeatureDownloadManager
     */
    startDownload : function() {
        //Start our download by calling internal function with no parameters
        this._startDownload();
    }
});
