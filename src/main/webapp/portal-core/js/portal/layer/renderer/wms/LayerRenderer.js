/**
 * An implementation of a portal.layer.Renderer for rendering WMS Layers
 * that belong to a set of portal.csw.CSWRecord objects.
 */
Ext.define('portal.layer.renderer.wms.LayerRenderer', {
    extend: 'portal.layer.renderer.Renderer',

    constructor: function(config) {
        this.legend = Ext.create('portal.layer.legend.wms.WMSLegend', {
            iconUrl : config.iconCfg ? config.iconCfg.url : 'portal-core/img/key.png',
            tryGetCapabilitiesFirst : config.tryGetCapabilitiesFirst
        });
        this.callParent(arguments);
    },

    /**
     * A function for displaying layered data from a variety of data sources. This function will
     * raise the renderstarted and renderfinished events as appropriate. The effect of multiple calls
     * to this function (ie calling displayData again before renderfinished is raised) is undefined.
     *
     * This function will re-render itself entirely and thus may call removeData() during the normal
     * operation of this function
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
        this.removeData();
        var wmsResources = portal.csw.OnlineResource.getFilteredFromArray(resources, portal.csw.OnlineResource.WMS);

        var urls = [];
        for (var i = 0; i < wmsResources.length; i++) {
            urls.push(wmsResources[i].get('url'));
        }
        this.renderStatus.initialiseResponses(urls, 'Loading...');
        
        var primitives = [];
        for (var i = 0; i < wmsResources.length; i++) {
            var wmsUrl = wmsResources[i].get('url');
            var wmsLayer = wmsResources[i].get('name');
            var wmsOpacity = filterer.getParameter('opacity');
            
            var filterParams = (Ext.Object.toQueryString(filterer.getMercatorCompatibleParameters()));
            var proxyUrl = this.parentLayer.get('source').get('proxyStyleUrl');

            if(proxyUrl){
                var styleurl =  proxyUrl = Ext.urlAppend(proxyUrl,filterParams);
                Ext.Ajax.request({
                    url: Ext.urlAppend(styleurl),
                    timeout : 180000,
                    scope : this,
                    success: Ext.bind(this._getRenderLayer,this,[wmsResources[i], wmsUrl, wmsLayer, wmsOpacity, filterer],true),
                    failure: function(response, opts) {                    
                        this.fireEvent('renderfinished', this);
                        console.log('server-side failure with status code ' + response.status);
                    }
                });
            } else {
                this._getRenderLayer(null,null,wmsResources[i], wmsUrl, wmsLayer, wmsOpacity, filterer);
            }

        }


        this.hasData = true;

    },
    
    _getRenderLayer : function(response,opts,wmsResource, wmsUrl, wmsLayer, wmsOpacity,filterer){
    	
    	if(wmsOpacity === undefined){
             wmsOpacity = filterer.parameters.opacity;
        }
        var sld_body = "";
        if (response !== null) {
            var sld_body = response.responseText;
            this.sld_body = sld_body;
            if(sld_body.indexOf("<?xml version=")!=0){
                sld_body = null;
                this.sld_body = sld_body;
            }
        }
    
        var layer=this.map.makeWms(undefined, undefined, wmsResource, this.parentLayer, wmsUrl, wmsLayer, wmsOpacity,sld_body)

        layer.getWmsLayer().events.register("loadstart",this,function(){
            var listOfStatus=this.renderStatus.getParameters();
            for(key in listOfStatus){
                if(this._getDomain(key)==this._getDomain(layer.getWmsUrl())){
                    this.renderStatus.updateResponse(key, "Loading WMS");
                    this.fireEvent('renderstarted', this, wmsResource, filterer);
                    break
                }
            }

        });

        //VT: Handle the after wms load clean up event.
        layer.getWmsLayer().events.register("loadend",this,function(evt){
            this.fireEvent('renderfinished', this);
            var listOfStatus=this.renderStatus.getParameters();
            this._updateStatusforWMS(layer.getWmsUrl(),"WMS Loaded");                        
        });
        
        var primitives = [];
        primitives.push(layer);
        this.primitiveManager.addPrimitives(primitives);
        
    },
    
    _updateStatusforWMS : function(updateKey,newValue){
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
     * A function for creating a legend that can describe the displayed data. If no
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
     * A function that is called when this layer needs to be permanently removed from the map.
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
     * You can't abort a WMS layer from rendering as it does so via img tags
     */
    abortDisplay : Ext.emptyFn
});