/**
 * A factory class for creating instances of portal.layer.querier.Querier
 */
Ext.define('portal.layer.querier.QuerierFactory', {

    map : null, //instance of portal.map.BaseMap

    constructor: function(config){
        this.map = config.map;
        this.callParent(arguments);
    },

    /**
     * An abstract function for building a portal.layer.querier.Querier
     * suitable for a given KnownLayer
     *
     * function(portal.knownlayer.KnownLayer knownLayer)
     *
     * returns - This function must return a portal.layer.downloader.Downloader
     */
    buildFromKnownLayer : portal.util.UnimplementedFunction,

    /**
     * An abstract function for building a portal.layer.querier.Querier
     * suitable for a given CSWRecord
     *
     * function(portal.csw.CswRecord cswRecord)
     *
     * returns - This function must return a portal.layer.downloader.Downloader
     */
    buildFromCswRecord : portal.util.UnimplementedFunction
});