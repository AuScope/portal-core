/**
 * An implementation of a portal.layer.renderer for rendering WFS with WMS Features
 * as transformed by the AuScope portal backend.
 */
Ext.define('portal.layer.renderer.wfs.FeatureWithMapRenderer', {
    extend: 'portal.layer.renderer.Renderer',

    config : {
        /**
         * portal.map.Icon - Information about the icon that is used to represent point locations of this WFS
         */
        icon : null
    },

    legend : null,
    allDownloadManagers : null,
    sld : null,

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
     * Used for handling a successful response from a request's download manaager
     */
    _handleDownloadManagerSuccess : function(dm, actualFilterParams, data, debugInfo, onlineResource, layer, icon) {
        var me = this;
        //Parse our KML into a set of overlays and markers
        var parser = Ext.create('portal.layer.renderer.wfs.KMLParser', {kml : data.kml, map : me.map});
        var primitives = parser.makePrimitives(icon, onlineResource, layer);

        //Add our single points and overlays to the overlay manager (which will add them to the map)
        this.primitiveManager.addPrimitives(primitives);

        //Store some debug info
        if (debugInfo) {
            this.renderDebuggerData.updateResponse(debugInfo.url, debugInfo.info);
        }

        //store the status
        this.renderStatus.updateResponse(onlineResource.get('url'), (primitives.length) + " record(s) retrieved.");

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
        var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WMS);

        var urls = [];
        var wmsRendered=[];
        //alert any listeners that we are about to start rendering wms
        //this.fireEvent('renderstarted', this, wmsResources, filterer);

        //VT: portal.util.URL.base is unable to resolve the name of the
        // local machine instead return localhost. eg localhost:8080/Auscope-Portal/
        var home=portal.util.URL.base;
        if(home.indexOf("localhost") != -1){
            home=home.replace("localhost",LOCALHOST);
        }
      //get the style format encoded as string
        //var styleUrl = escape(Ext.urlAppend(home + this.parentLayer.get('source').get('proxyStyleUrl'), Ext.Object.toQueryString(filterer.getParameters())));
        var styleUrl = escape(Ext.urlAppend(home + this.parentLayer.get('source').get('proxyStyleUrl')));
        this.sld=unescape(styleUrl);

        var primitives = [];
        for (var i = 0; i < wmsResources.length; i++) {
            var wmsUrl = wmsResources[i].get('url');
            // VT: Instead of rendering the WMS url in the status, it is neater to display the wfs url
           // urls.push(wmsUrl);

            var wmsLayer = wmsResources[i].get('name');
            var wmsOpacity = filterer.getParameter('opacity');
            wmsRendered[this._getDomainWithLayerNameId(wmsUrl,wmsLayer)]=1
            wmsUrl=Ext.urlAppend(wmsUrl, 'SLD=' + styleUrl);
            wmsUrl=Ext.urlAppend(wmsUrl, 'SRS=' + 'EPSG%3A3857');

            primitives.push(this.map.makeWms(undefined, undefined, wmsResources[i], this.parentLayer, wmsUrl, wmsLayer, wmsOpacity));

        }

        this.primitiveManager.addPrimitives(primitives);
        this.hasData = true;
        //this array will contain a list of wfs url that are process by its wms component.
        var wmsUrls = [];

        //Initialise our render status with every URL we will be calling (these will get updated as we go)

        for (var i = 0; i < wfsResources.length; i++) {
            var wfsUrl = wfsResources[i].get('url');
            var wfsLayer = wfsResources[i].get('name');
            urls.push(wfsUrl);
            // VT: Instead of rendering the WMS url in the status, it is neater to display the wfs url
            if(wmsRendered[this._getDomainWithLayerNameId(wfsUrl,wfsLayer)]){
                wmsUrls.push(wfsUrl);
            }
        }
        this.renderStatus.initialiseResponses(urls, 'Loading...');

        //VT: somehow determine wms complete?
        for (var i =0; i < wmsUrls.length; i++){
            this.renderStatus.updateResponse(wmsUrls[i], "WMS Image");
        }

        //alert any listeners that we are about to start rendering wfs
        this.fireEvent('renderstarted', this, wfsResources, filterer);
        this.currentRequestCount = wfsResources.length; //this will be decremented as requests return

        //Each and every WFS resource will be queried with their own seperate download manager

        for (var i = 0; i < wfsResources.length; i++) {

            var wfsUrl = wfsResources[i].get('url');
            var wfsLayer = wfsResources[i].get('name');
            //only if WMS has not been built
            if(!wmsRendered[this._getDomainWithLayerNameId(wfsUrl,wfsLayer)]){

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
            }else{
                //VT: the resource is already rendered via wms therefore we can deduct it from currentRequestCount
                this.currentRequestCount--;
                if (this.currentRequestCount === 0) {
                    this.fireEvent('renderfinished', this);
                }
            }

        }
    },

    _getDomainWithLayerNameId : function(url,name){
        return ((url.match(/:\/\/(.[^/]+)/)[1]) +'/'+ name);
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