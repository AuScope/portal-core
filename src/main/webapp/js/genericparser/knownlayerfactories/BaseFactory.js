/**
 * Abstract base class for all Known Layer Parser factories to inherit from.
 */
Ext.ns('GenericParser.KnownLayerFactory');
GenericParser.KnownLayerFactory.BaseFactory = Ext.extend(Ext.util.Observable, {
    /**
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *
     * }
     */
    constructor : function(cfg) {
        GenericParser.KnownLayerFactory.BaseFactory.superclass.constructor.call(this, cfg);
    },

    /**
     * abstract - to be overridden to return a boolean indicating whether this factory can
     * generate GenericParser.BaseComponent objects representing ancillary information about
     * a particular feature in a known layer
     */
    supportsKnownLayer : function(knownLayer) {
        return false;
    },

    /**
     * abstract - Must be overridden by extending classes
     * This function must return an GenericParser.BaseComponent object that represents
     * ancillary information about the specified feature.
     *
     * @param featureId A string representing some form of unique ID
     * @param parentKnownLayer The knownLayer that the feature belongs to (cannot be null)
     * @param parentCSWRecord The CSWRecord that is part of parentKnownLayer that 'featureId' belongs to.
     * @param parentOnlineResource The online resource (belonging to parentCSWRecord) that featureId is derived from
     * @param rootCfg a configuration object to be applied to the root GenericParser.BaseComponent
     */
    parseKnownLayerFeature : function(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource, rootCfg) {
        return null;
    },


    /**
     * Decomposes a 'normal' URL in the form http://url.com/long/path/name to just its prefix + hostname http://url.com
     * @param url The url to decompose
     */
    getBaseUrl : function(url) {
        var splitUrl = url.split('://'); //this should split us into 2 parts
        return splitUrl[0] + '://' + splitUrl[1].slice(0, splitUrl[1].indexOf('/'));
    }
});