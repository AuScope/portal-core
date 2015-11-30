/**
 * Class for transforming individual 'features' (WFS or otherwise) of a KnownLayer into
 * components representing GUI widgets for demonstrating ancillary information about
 * that 'feature'
 *
 * For example, the process of identifying the 'NVCL KnownLayer' and creating a 'Observations Download'
 * window is handled by this class (supported by underlying factories).
 *
 */
Ext.define('portal.layer.querier.wfs.KnownLayerParser', {
    extend: 'Ext.util.Observable',

    /**
     * Builds a new KnownLayerParser from a list of factories. Factories in factoryList will be tested before
     * the items in factoryNames
     *
     * {
     *  factoryNames : String[] - an array of class names which will be instantiated as portal.layer.querier.wfs.factories.BaseFactory objects
     *  factoryList : portal.layer.querier.wfs.factories.BaseFactory[] - an array of already instantiated factory objects
     * }
     */
    constructor : function(config) {
        //The following ordering is important as it dictates the order in which to try
        //factories for parsing a particular node
        this.factoryList = Ext.isArray(config.factoryList) ? config.factoryList : [];
        if (Ext.isArray(config.factoryNames)) {
            for (var i = 0; i < config.factoryNames.length; i++) {
                this.factoryList.push(Ext.create(config.factoryNames[i], config));
            }
        }
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Internal Method - returns a factory instance that can parse the specified feature/KnownLayer or null if it DNE
     */
    _getSupportingFactory : function(featureId, parentKnownLayer, parentOnlineResource, parentLayer) {
        if (!parentKnownLayer) {
            return null;
        }

        for (var i = 0; i < this.factoryList.length; i++) {
            if (this.factoryList[i].supportsKnownLayer(parentKnownLayer)) {
                return this.factoryList[i];
            }
        }

        return null;
    },

    /**
     * Iterates through all internal factories looking for factories that can generate GUI widgets
     * that can also represent the specified feature belonging to knownLayer.
     *
     * Returns a Boolean indicating whether there is a factory that can support this feature/knownLayer
     *
     *
     * @param featureId The unique ID of the feature (belonging to a known layer)
     * @param parentKnownLayer The KnownLayer that 'owns' featureId
     * @param parentOnlineResource The OnlineResource Object belonging to CSWRecord that sourced featureId
     * @param parentLayer The portal.layer.Layer representing the layer generated from parentKnownLayer
     */
    canParseKnownLayerFeature : function(featureId, parentKnownLayer, parentOnlineResource, parentLayer) {
        var supportingFactory = this._getSupportingFactory(featureId, parentKnownLayer, parentOnlineResource, parentLayer);
        return supportingFactory !== null && supportingFactory !== undefined;
    },

    /**
     * Iterates through all internal factories looking for factories that can generate GUI widgets
     * that can also represent the specified feature belonging to knownLayer.
     *
     * Returns a single GenericParser.BaseComponent object
     *
     * @param featureId The unique ID of the feature (belonging to a known layer)
     * @param parentKnownLayer The KnownLayer that 'owns' featureId
     * @param parentOnlineResource The OnlineResource Object belonging to CSWRecord that sourced featureId
     * @param parentLayer The portal.layer.Layer representing the layer generated from parentKnownLayer
     * @param rootCfg [Optional] An Object whose properties will be applied to the top level component parsed (a GenericParser.BaseComponent instance)
     */
    parseKnownLayerFeature : function(featureId, parentKnownLayer, parentOnlineResource, parentLayer) {
        var supportingFactory = this._getSupportingFactory(featureId, parentKnownLayer, parentOnlineResource, parentLayer);
        if (supportingFactory) {
            return supportingFactory.parseKnownLayerFeature(featureId, parentKnownLayer, parentOnlineResource, parentLayer);
        }

        return Ext.create('portal.layer.querier.BaseComponent', {});
    }
});