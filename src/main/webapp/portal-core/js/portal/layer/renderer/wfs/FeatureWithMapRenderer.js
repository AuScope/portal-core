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
    sld_body : null,

    constructor: function(config) {
        this.currentRequestCount = 0;//how many requests are still running

        //VT: ALL KNOWLAYER ICON in FeatureWithMapRenderer has to be assigned a marker color in
        // the auscope-knownlayer config file else we will treat it has a pure WMS layer and
        // give it a wms legend.

        if(config.icon.getIsDefault()===true){
            this.legend = Ext.create('portal.layer.legend.wms.WMSLegend', {
                iconUrl : config.iconCfg ? config.iconCfg.url : 'portal-core/img/key.png'
            });
        }else{
            this.legend = Ext.create('portal.layer.legend.wfs.WFSLegend', {
                iconUrl : config.icon ? config.icon.getUrl() : ''
            });
        }
        this.allDownloadManagers = [];
        this.currentRequestCount = 0;
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
        
        this.on('renderfinished', this._cleanupAbort, this);
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
        this.aborted = false;

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
      //  var styleUrl = escape(Ext.urlAppend(home + this.parentLayer.get('source').get('proxyStyleUrl'), unescape(Ext.Object.toQueryString(filterer.getMercatorCompatibleParameters()))));
      //  this.sld=unescape(styleUrl);


        for (var i = 0; i < wmsResources.length; i++) {
            var wmsUrl = wmsResources[i].get('url');
            // VT: Instead of rendering the WMS url in the status, it is neater to display the wfs url

            var wmsLayer = wmsResources[i].get('name');
            var wmsOpacity = filterer.getParameter('opacity');
            //  FT: Generate serviceUrl based on WFS URL, serviceUrl is important for NVCL Borehole
            //      - to display only those with Hylogger Data based on those listed in nvcl:ScannedBoreholeCollection
            for (var j=0; j<wfsResources.length; j++) {
                var wfsHost = this._getDomain(wfsResources[j].get('url'));
                var wmsHost = this._getDomain(wmsUrl);
                if (wfsHost==wmsHost) {
                    serviceUrl = wfsResources[j].get('url');
                }

            }

            if(filterer.getParameters().serviceFilter &&
                    (this._getDomain(wmsResources[i].get('url'))!= this._getDomain(filterer.getParameters().serviceFilter[0]))){
                continue;
            }

            var color = portal.map.Icon.mapIconColor(this.parentLayer.get('source').get('iconUrl'));


            var proxyUrl = this.parentLayer.get('source').get('proxyStyleUrl');
            var filterParams = (Ext.Object.toQueryString(filterer.getMercatorCompatibleParameters()));
            if(typeof(color) != "undefined"){
                filterParams += "&color=" + escape(color);
            }
            filterParams += "&serviceUrl=" + escape(serviceUrl);
            var styleUrl="";
            if(proxyUrl){
                styleUrl = Ext.urlAppend(proxyUrl,filterParams);
                styleUrl = Ext.urlAppend(styleUrl,"layerName="+wmsLayer);
            }else{
                //VT: if style proxy url is not defined, we assign it a default.
                styleUrl = Ext.urlAppend("getDefaultStyle.do","layerName="+wmsLayer);
            }


            wmsRendered[this._getDomain(wmsUrl)]=1;


            Ext.Ajax.request({
                url: styleUrl,
                timeout : 180000,
                scope : this,
                success: Ext.bind(this._getRenderLayer,this,[wmsResources[i], wmsUrl, wmsLayer, wmsOpacity,wfsResources, filterer],true),
                failure: function(response, opts) {                    
                     if (this.currentRequestCount <= 0) {
                         this.fireEvent('renderfinished', this);
                     }
                    console.log('server-side failure with status code ' + response.status);
                }
            });

        }


        this.hasData = true;
        //this array will contain a list of wfs url that are process by its wms component.
        var wmsUrls = [];

        //Initialise our render status with every URL we will be calling (these will get updated as we go)

        for (var i = 0; i < wfsResources.length; i++) {
            var wfsUrl = wfsResources[i].get('url');
            var wfsLayer = wfsResources[i].get('name');
            urls.push(wfsUrl);
            // VT: Instead of rendering the WMS url in the status, it is neater to display the wfs url
            if(wmsRendered[this._getDomain(wfsUrl)]){
                wmsUrls.push(wfsUrl);
                this.renderStatus.updateResponse(wfsUrl, "Loading WMS");
            }
        }
        this.renderStatus.initialiseResponses(urls, 'Loading...');



        //alert any listeners that we are about to start rendering wfs
        this.fireEvent('renderstarted', this, wfsResources, filterer);
        //this.currentRequestCount = wfsResources.length; //this will be decremented as requests return

        //Each and every WFS resource will be queried with their own seperate download manager

        for (var i = 0; i < wfsResources.length; i++) {

            var wfsUrl = wfsResources[i].get('url');
            var wfsLayer = wfsResources[i].get('name');
            //only if WMS has not been built
            if(!wmsRendered[this._getDomain(wfsUrl)]){
                this.currentRequestCount++;
                //Build our filter params object that will make a request
                var filterParams = filterer.getParameters();
                var onlineResource = wfsResources[i];
                filterParams.serviceUrl = wfsUrl;
                filterParams.typeName = wfsLayer;
                filterParams.maxFeatures = 200;

                if(filterer.getParameters().serviceFilter &&
                        filterParams.serviceUrl!=filterer.getParameters().serviceFilter[0]){
                    this.currentRequestCount--;
                    this.renderStatus.updateResponse(filterParams.serviceUrl, "Not Queried");
                    continue;
                }

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
            
        }
        if (this.currentRequestCount === 0) {
            this.fireEvent('renderfinished', this);
        }

    },


    _getRenderLayer : function(response,opts,wmsResource, wmsUrl, wmsLayer, wmsOpacity,wfsResources,filterer){
        if (this.aborted) {
            return;
        }
        
        if(wmsOpacity === undefined){
            wmsOpacity = filterer.parameters.opacity;
        }

        var sld_body = response.responseText;
        this.sld_body = sld_body;
        if(sld_body.indexOf("<?xml version=")!=0){
            this._updateStatusforWFSWMS(wmsUrl, "error: invalid SLD response");
            return
        }
        
        this._updateStatusforWFSWMS(wmsUrl, "Testing Connection");
        this.fireEvent('renderstarted', this, wfsResources, filterer);
        //VT: add for test connection
        this.currentRequestCount++;
        
        Ext.Ajax.request({
            url: "testServiceGetCap.do",
            timeout : 180000,  
            params : {
                serviceUrl : wmsUrl  
            },
            scope : this,
            success: Ext.bind(this._addWMSLayer,this,[wmsResource, wmsUrl, wmsLayer, wmsOpacity,wfsResources, filterer,sld_body],true),
            failure: function(response, opts) {
                 this.currentRequestCount--;
                 this._updateStatusforWFSWMS(wmsUrl, "Address cannot be reached");
                 if (this.currentRequestCount === 0) {                     
                     this.fireEvent('renderfinished', this);
                 }
            }
        });
        
       
    },
    
    _addWMSLayer : function(response,opts,wmsResource, wmsUrl, wmsLayer, wmsOpacity,wfsResources,filterer,sld_body){
        //VT: minus test connection
        this.currentRequestCount--;
        
        if (this.aborted) {
            return;
        }
        
        var layer=this.map.makeWms(undefined, undefined, wmsResource, this.parentLayer, wmsUrl, wmsLayer, wmsOpacity,sld_body)

        layer.getWmsLayer().events.register("loadstart",this,function(){
            this.currentRequestCount++;
            var listOfStatus=this.renderStatus.getParameters();
            for(key in listOfStatus){
                if(this._getDomain(key)==this._getDomain(layer.getWmsUrl())){
                    this.renderStatus.updateResponse(key, "Loading WMS");
                    this.fireEvent('renderstarted', this, wfsResources, filterer);
                    break
                }
            }

        });

        //VT: Handle the after wms load clean up event.
        layer.getWmsLayer().events.register("loadend",this,function(evt){
            this.currentRequestCount--;
            if (this.currentRequestCount === 0) {
                this.fireEvent('renderfinished', this);
            }
            var listOfStatus=this.renderStatus.getParameters();
            this._updateStatusforWFSWMS(layer.getWmsUrl(),"WMS Loaded");                        
        });
        
        var primitives = [];
        primitives.push(layer);
        this.primitiveManager.addPrimitives(primitives);
        
    },
    
    _updateStatusforWFSWMS : function(updateKey,newValue){
        for(key in this.renderStatus.getParameters()){
            if(this._getDomain(key)==this._getDomain(updateKey)){
                this.renderStatus.updateResponse(key, newValue);
                break
            }
        }
    },

    _getDomain : function(data) {
        return portal.util.URL.extractHostNSubDir(data,1);
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
        this.abortDisplay();
        this.primitiveManager.clearPrimitives();
        this.fireEvent('renderfinished', this);
    },

    /**
     * An abstract function - see parent class for more info
     */
    abortDisplay : function() {
        this.aborted = true;
        for (var i = 0; i < this.allDownloadManagers.length; i++) {
            this.allDownloadManagers[i].abortDownload();
        }
    },
    
    _cleanupAbort : function() {
        this.aborted = false;
    }
});