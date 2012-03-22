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
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *
     * }
     */
    constructor : function(config) {
        //Setup class variables
        this.factoryList = [];
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.knownlayerfactories.NVCLFactory', config));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.knownlayerfactories.PressureDBFactory', config));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.knownlayerfactories.GeodesyFactory', config));

        this.listeners = config.listeners;

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Internal Method - returns a factory instance that can parse the specified feature/KnownLayer or null if it DNE
     */
    _getSupportingFactory : function(featureId, parentKnownLayer, parentOnlineResource) {
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
     */
    canParseKnownLayerFeature : function(featureId, parentKnownLayer, parentOnlineResource) {
        var supportingFactory = this._getSupportingFactory(featureId, parentKnownLayer, parentOnlineResource);
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
     * @param rootCfg [Optional] An Object whose properties will be applied to the top level component parsed (a GenericParser.BaseComponent instance)
     */
    parseKnownLayerFeature : function(featureId, parentKnownLayer, parentOnlineResource) {
        var supportingFactory = this._getSupportingFactory(featureId, parentKnownLayer, parentOnlineResource);
        if (supportingFactory) {
            return supportingFactory.parseKnownLayerFeature(featureId, parentKnownLayer, parentOnlineResource);
        }

        return Ext.create('portal.layer.querier.BaseComponent', {});
    }
});