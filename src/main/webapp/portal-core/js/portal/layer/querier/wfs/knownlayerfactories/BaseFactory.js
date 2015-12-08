/**
 * Abstract base class for all Known Layer Parser factories to inherit from.
 */
Ext.define('portal.layer.querier.wfs.knownlayerfactories.BaseFactory', {
    extend : 'Ext.util.Observable',

    /**
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *
     * }
     */
    constructor : function(cfg) {
        this.listeners = cfg.listeners;
        this.callParent(arguments);
    },

    /**
     * abstract - to be overridden to return a boolean indicating whether this factory can
     * generate GenericParser.BaseComponent objects representing ancillary information about
     * a particular feature in a known layer
     *
     * function(knownLayer)
     *
     */
    supportsKnownLayer : portal.util.UnimplementedFunction,

    /**
     * abstract - Must be overridden by extending classes
     * This function must return an portal.layer.querier.BaseComponent object that represents
     * ancillary information about the specified feature.
     *
     * function(featureId, parentKnownLayer, parentOnlineResource, rootCfg, parentLayer)
     *
     * featureId - A string representing some form of unique ID
     * parentKnownLayer - The knownLayer that the feature belongs to (cannot be null)
     * parentOnlineResource - The online resource (belonging to parentCSWRecord) that featureId is derived from
     * rootCfg - a configuration object to be applied to the root GenericParser.BaseComponent
     * parentLayer - The portal.layer.Layer representing the layer generated from parentKnownLayer
     */
    parseKnownLayerFeature : portal.util.UnimplementedFunction,


    /**
     * Decomposes a 'normal' URL in the form http://url.com/long/path/name to just its prefix + hostname http://url.com
     * @param url The url to decompose
     */
    getBaseUrl : function(url) {
        var splitUrl = url.split('://'); //this should split us into 2 parts
        return splitUrl[0] + '://' + splitUrl[1].slice(0, splitUrl[1].indexOf('/'));
    }
});