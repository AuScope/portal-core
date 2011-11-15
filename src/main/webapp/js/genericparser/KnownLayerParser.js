/**
 * Class for transforming individual 'features' (WFS or otherwise) of a KnownLayer into
 * components representing GUI widgets for demonstrating ancillary information about
 * that 'feature'
 *
 * For example, the process of identifying the 'NVCL KnownLayer' and creating a 'Observations Download'
 * window is handled by this class (supported by underlying factories).
 *
 */
Ext.ns('GenericParser');
GenericParser.KnownLayerParser = Ext.extend(Ext.util.Observable, {
    factoryList : [],

    /**
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *
     * }
     */
    constructor : function(cfg) {
        GenericParser.KnownLayerParser.superclass.constructor.call(this, cfg);

        this.factoryList.push(new GenericParser.KnownLayerFactory.NVCLFactory(cfg));
        this.factoryList.push(new GenericParser.KnownLayerFactory.PressureDBFactory(cfg));
    },

    /**
     * Internal Method - returns a factory instance that can parse the specified feature/KnownLayer or null if it DNE
     */
    _getSupportingFactory : function(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource) {
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
     * @param parentCSWRecord The CSWRecord (belonging to parentKnownLayer) that sourced featureId
     * @param parentOnlineResource The OnlineResource Object belonging to CSWRecord that sourced featureId
     */
    canParseKnownLayerFeature : function(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource) {
        var supportingFactory = this._getSupportingFactory(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource);
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
     * @param parentCSWRecord The CSWRecord (belonging to parentKnownLayer) that sourced featureId
     * @param parentOnlineResource The OnlineResource Object belonging to CSWRecord that sourced featureId
     * @param rootCfg [Optional] An Object whose properties will be applied to the top level component parsed (a GenericParser.BaseComponent instance)
     */
    parseKnownLayerFeature : function(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource, rootCfg) {
        var supportingFactory = this._getSupportingFactory(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource);
        if (supportingFactory) {
            return supportingFactory.parseKnownLayerFeature(featureId, parentKnownLayer, parentCSWRecord, parentOnlineResource, (rootCfg ? rootCfg : {}));
        }

        return new GenericParser.BaseComponent({});
    }
});