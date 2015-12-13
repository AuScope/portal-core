/**
 * A factory class for creating instances of portal.layer.renderer.Renderer
 */
Ext.define('portal.layer.renderer.RendererFactory', {

    map : null, //instance of portal.map.BaseMap

    constructor: function(config){
        this.map = config.map;
        this.callParent(arguments);
    },

    /**
     * An abstract function for building a portal.layer.renderer.Renderer
     * suitable for a given KnownLayer
     *
     * function(portal.knownlayer.KnownLayer knownLayer)
     *
     * returns - This function must return a portal.layer.downloader.Downloader
     */
    buildFromKnownLayer : portal.util.UnimplementedFunction,

    /**
     * An abstract function for building a portal.layer.renderer.Renderer
     * suitable for a given CSWRecord
     *
     * function(portal.csw.CswRecord cswRecord)
     *
     * returns - This function must return a portal.layer.downloader.Downloader
     */
    buildFromCswRecord : portal.util.UnimplementedFunction,
    
    /**
     * An abstract function for building a portal.layer.renderer.Renderer
     * suitable for a given CSWRecord
     *
     * function(portal.csw.CswRecord cswRecord)
     *
     * returns - This function must return a portal.layer.downloader.Downloader
     */
    buildFromKMLRecord : portal.util.UnimplementedFunction
});