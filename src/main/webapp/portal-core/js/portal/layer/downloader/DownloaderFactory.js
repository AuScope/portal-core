/**
 * A factory class for creating instances of portal.layer.downloader.Downloader
 */
Ext.define('portal.layer.downloader.DownloaderFactory', {

    map : null, //instance of portal.map.BaseMap

    constructor: function(config){
        this.map = config.map;
        this.callParent(arguments);
    },

    /**
     * An abstract function for building a portal.layer.downloader.Downloader
     * suitable for a given KnownLayer
     *
     * function(portal.knownlayer.KnownLayer knownLayer)
     *
     * returns - This function must return a portal.layer.downloader.Downloader
     *           Returning null will indicate that the known layer is incapable
     *           of downloading information to the user's computer
     */
    buildFromKnownLayer : portal.util.UnimplementedFunction,

    /**
     * An abstract function for building a portal.layer.downloader.Downloader
     * suitable for a given CSWRecord
     *
     * function(portal.csw.CswRecord cswRecord)
     *
     * returns - This function must return a portal.layer.downloader.Downloader
     *           Returning null will indicate that the known layer is incapable
     *           of downloading information to the user's computer
     */
    buildFromCswRecord : portal.util.UnimplementedFunction
});