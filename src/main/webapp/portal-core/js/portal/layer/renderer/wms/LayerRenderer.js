/**
 * An implementation of a portal.layer.Renderer for rendering WMS Layers
 * that belong to a set of portal.csw.CSWRecord objects.
 */
Ext.define('portal.layer.renderer.wms.LayerRenderer', {
    extend: 'portal.layer.renderer.Renderer',

    constructor: function(config) {
        this.legend = Ext.create('portal.layer.legend.wfs.WMSLegend', {
            iconUrl : config.iconCfg ? config.iconCfg.url : 'img/key.png'
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

        this.fireEvent('renderstarted', this, wmsResources, filterer);

        var primitives = [];
        for (var i = 0; i < wmsResources.length; i++) {
            var wmsUrl = wmsResources[i].get('url');
            var wmsLayer = wmsResources[i].get('name');
            var wmsOpacity = filterer.getParameter('opacity');

            primitives.push(this.map.makeWms(undefined, undefined, wmsResources[i], this.parentLayer, wmsUrl, wmsLayer, wmsOpacity));

        }

        this.primitiveManager.addPrimitives(primitives);
        this.hasData = true;
        this.fireEvent('renderfinished', this);
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