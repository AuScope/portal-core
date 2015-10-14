/**
 * An implementation of a portal.layer.Renderer for rendering WMS Layers that belong to a set of portal.csw.CSWRecord
 * objects. This extension is for Disjuncted ("OR") Layers of Layers defined through a WFSSelectors KnownLayerSelector
 * (in auscope-known-layers.xml) and was developed by GA in GPT-40.
 */
Ext.define('portal.layer.renderer.wms.DisjunctionLayerRenderer', {
    extend : 'portal.layer.renderer.Renderer',

    constructor : function(config) {
        this.legend = Ext.create('portal.layer.legend.wfs.WMSLegend', {
            iconUrl : config.iconCfg ? config.iconCfg.url : 'portal-core/img/key.png'
        });
        this.callParent(arguments);
    },

    /**
     * A function for displaying layered data from a variety of data sources. This function will raise the renderstarted
     * and renderfinished events as appropriate. The effect of multiple calls to this function (ie calling displayData
     * again before renderfinished is raised) is undefined.
     * 
     * This function will re-render itself entirely and thus may call removeData() during the normal operation of this
     * function
     * 
     * function(portal.csw.OnlineResource[] resources, portal.layer.filterer.Filterer filterer,
     * function(portal.layer.renderer.Renderer this, portal.csw.OnlineResource[] resources,
     * portal.layer.filterer.Filterer filterer, bool success) callback
     * 
     * returns - void
     * 
     * resources - an array of data sources which should be used to render data filterer - A custom filter that can be
     * applied to the specified data sources callback - Will be called when the rendering process is completed and
     * passed an instance of this renderer and the parameters used to call this function
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
            // Find the layer.name that is the same as the selected one
            var wmsLayer = wmsResources[i].get('name');
            if (wmsLayer === filterer.getParameter('serviceFilter')) {
                var wmsUrl = wmsResources[i].get('url');
                var wmsOpacity = filterer.getParameter('opacity');

                var layer = this.map.makeWms(undefined, undefined, wmsResources[i], this.parentLayer, wmsUrl, wmsLayer,
                        wmsOpacity);

                layer.getWmsLayer().events.register("loadstart", this, function() {
                    this.currentRequestCount++;
                    var listOfStatus = this.renderStatus.getParameters();
                    this.fireEvent('renderstarted', this, wmsResources, filterer);
                    this.renderStatus.updateResponse(layer.getWmsUrl(), "Loading WMS");
                });

                // VT: Handle the after wms load clean up event.
                layer.getWmsLayer().events.register("loadend", this, function(evt) {
                    this.currentRequestCount--;
                    var listOfStatus = this.renderStatus.getParameters();
                    this.renderStatus.updateResponse(layer.getWmsUrl(), "WMS Loaded");
                    this.fireEvent('renderfinished', this);
                });

                primitives.push(layer);
            }
        }

        this.primitiveManager.addPrimitives(primitives);
        this.hasData = true;

    },

    /**
     * A function for creating a legend that can describe the displayed data. If no such thing exists for this renderer
     * then null should be returned.
     * 
     * function(portal.csw.OnlineResource[] resources, portal.layer.filterer.Filterer filterer)
     * 
     * returns - portal.layer.legend.Legend or null
     * 
     * resources - (same as displayData) an array of data sources which should be used to render data filterer - (same
     * as displayData) A custom filter that can be applied to the specified data sources
     */
    getLegend : function(resources, filterer) {
        return this.legend;
    },

    /**
     * A function that is called when this layer needs to be permanently removed from the map. In response to this
     * function all rendered information should be removed
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