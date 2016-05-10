/**
 * An implementation of a portal.layer.renderer for rendering WFS Features
 * as transformed by the AuScope portal backend.
 */
Ext.define('portal.layer.renderer.wfs.FeatureRenderer', {
    extend: 'portal.layer.renderer.Renderer',

    config : {
        /**
         * portal.map.Icon - Information about the icon that is used to represent point locations of this WFS
         */
        icon : null
    },

    legend : null,
    allDownloadManagers : null,

    constructor: function(config) {
        this.currentRequestCount = 0;//how many requests are still running
        this.legend = Ext.create('portal.layer.legend.wfs.WFSLegend', {
            iconUrl : config.icon ? config.icon.getUrl() : ''
        });
        this.allDownloadManagers = [];

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Must be called whenever a download manager returns a response (success or failure)
     *
     * You can optionally pass in an array of markers/overlays that were rendered
     */
    _finishDownloadManagerResponse : function(primitiveList) {
        //If we haven't had any data come back yet from another response (and we have data now)
        //update the boolean indicating that we've had data
        if (primitiveList) {
            this.hasData = (primitiveList.length > 0);
        }

        this.currentRequestCount--;
        if (this.currentRequestCount === 0) {
            this.fireEvent('renderfinished', this);
        }
    },

    /**
     * Used to handle the case where the download manager returns an error
     */
    _handleDownloadManagerError : function(dm, message, debugInfo, onlineResource, layer) {
        //store the status
        this.renderStatus.updateResponse(onlineResource.get('url'), message);
        if(debugInfo) {
            this.renderDebuggerData.updateResponse(onlineResource.get('url'), message + debugInfo.info);
        } else {
            this.renderDebuggerData.updateResponse(onlineResource.get('url'), message);
        }

        //we are finished
        this._finishDownloadManagerResponse();
    },

    /**
     * Used for handling the case when the user cancels their download request
     */
    _handleDownloadManagerCancelled : function(dm, onlineResource, layer) {
        //store the status
        this.renderStatus.updateResponse(onlineResource.get('url'), 'Request cancelled by user.');

        //we are finished
        this._finishDownloadManagerResponse();
    },

    /**
     * Used for handling a successful response from a request's download manager
     */
    _handleDownloadManagerSuccess : function(dm, actualFilterParams, data, debugInfo, onlineResource, layer, icon) {
        var me = this;
        //Parse our KML into a set of overlays and markers
        var parser = Ext.create('portal.layer.renderer.wfs.GMLParser', {gml : data.gml, map : me.map});
        var primitives = parser.makePrimitives(icon, onlineResource, layer);
        var count = parser.getFeatureCount();
        
        //Add our single points and overlays to the overlay manager (which will add them to the map)
        this.primitiveManager.addPrimitives(primitives);

        //Store some debug info
        if (debugInfo) {
            this.renderDebuggerData.updateResponse(debugInfo.url, debugInfo.info);
        }

        //store the status
        this.renderStatus.updateResponse(onlineResource.get('url'), count == null ? ('Unknown number of features retrieved.') : (count + " feature(s) retrieved."));

        //we are finished
        this._finishDownloadManagerResponse(primitives);
    },

    /**
     * An implementation of the abstract base function. See comments in superclass
     * for more information.
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer,
     *          function(portal.layer.renderer.Renderer this, portal.csw.OnlineResource[] resources, portal.layer.filterer.Filterer filterer, bool success) callback
     *
     * returns - void
     *
     * resources - an array of data sources which should be used to render data
     * filterer - A custom filter that can be applied to the specified data sources
     * callback - Will be called when the rendering process is completed and passed an instance of this renderer and the parameters used to call this function
     */
    displayData : function(resources, filterer, callback) {
        //start by removing any existing data
        this.abortDisplay();
        this.removeData();

        var me = this;
        var wfsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WFS);

        //Initialise our render status with every URL we will be calling (these will get updated as we go)
        var urls = [];
        for (var i = 0; i < wfsResources.length; i++) {
            urls.push(wfsResources[i].get('url'));
        }
        this.renderStatus.initialiseResponses(urls, 'Loading...');

        //alert any listeners that we are about to start rendering
        this.fireEvent('renderstarted', this, wfsResources, filterer);
        this.currentRequestCount = wfsResources.length; //this will be decremented as requests return

        //Each and every WFS resource will be queried with their own separate download manager

        for (var i = 0; i < wfsResources.length; i++) {
            //Build our filter params object that will make a request
            var filterParams = filterer.getParameters();
            var onlineResource = wfsResources[i];

            filterParams.serviceUrl = onlineResource.data.url;
            filterParams.typeName = onlineResource.data.name;
            filterParams.maxFeatures = 200;

            //Our requesting is handled by a download manager
            var downloadManager = Ext.create('portal.layer.renderer.wfs.FeatureDownloadManager', {
                visibleMapBounds : filterer.getSpatialParam(),
                proxyFetchUrl : this.proxyUrl,
                proxyCountUrl : this.proxyCountUrl,
                filterParams : filterParams,
                listeners : {
                    //Please note that the following bindings override args as ExtJS events append
                    //the listeners object to fired events (we don't want that) so we are forced to override
                    //that parameter using the appendArgs argument in Ext.bind
                    success : Ext.bind(this._handleDownloadManagerSuccess, this, [onlineResource, this.parentLayer, this.icon], 4), //Override args from the 4th argument
                    error : Ext.bind(this._handleDownloadManagerError, this, [onlineResource, this.parentLayer], 3), //Override args from the 3rd
                    cancelled : Ext.bind(this._handleDownloadManagerCancelled, this, [onlineResource, this.parentLayer], 1) //Override args from the 1st
                }
            });

            downloadManager.startDownload();

            this.allDownloadManagers.push(downloadManager);//save this manager in case we need to abort later on
        }
    },

    /**
     * An abstract function for creating a legend that can describe the displayed data. If no
     * such thing exists for this renderer then null should be returned.
     *
     * function(portal.csw.OnlineResource[] resources,
     *          portal.layer.filterer.Filterer filterer)
     *
     * returns - portal.layer.legend.Legend or null
     *
     * resources - (same as displayData) an array of data sources which should be used to render data
     * filterer - (same as displayData) A custom filter that can be applied to the specified data sources
     */
    getLegend : function(resources, filterer) {
        return this.legend;
    },

    /**
     * An abstract function that is called when this layer needs to be permanently removed from the map.
     * In response to this function all rendered information should be removed
     *
     * function()
     *
     * returns - void
     */
    removeData : function() {
        this.primitiveManager.clearPrimitives();
    },

    /**
     * An abstract function - see parent class for more info
     */
    abortDisplay : function() {
        for (var i = 0; i < this.allDownloadManagers.length; i++) {
            this.allDownloadManagers[i].abortDownload();
        }
    }
});