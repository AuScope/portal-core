/**
 * Class for transforming a W3C DOM Document into a GenericParserComponent
 * by utilising a number of 'plugin' factories.
 */
Ext.define('portal.layer.querier.wfs.Parser', {
    extend: 'Ext.util.Observable',

    constructor : function(config) {
        //The following ordering is important as it dictates the order in which to try
        //factories for parsing a particular node
        var cfg = {
            parser : this
        };
        this.factoryList = [];
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.GeologicUnitFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.LocatedSpecimenFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.SamplingFeatureCollectionFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.BoreholeFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.MappedFeatureFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.MiningFeatureOccurrenceFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.GeophysicsAnomaliesFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.GnssStationFactory', cfg));
        this.factoryList.push(Ext.create('portal.layer.querier.wfs.factories.SimpleFactory', cfg));//The simple factory should always go last

        this.listeners = config.listeners;

        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Iterates through all internal factories until domNode can be parsed into GenericParser.BaseComponent
     * @param domNode The W3C DOM node to parse
     * @param wfsUrl The original WFS URL where domNode was sourced from
     * @param rootCfg [Optional] An Object whose properties will be applied to the top level component parsed (a GenericParser.BaseComponent instance)
     */
    parseNode : function(domNode, wfsUrl) {
        //In the event of an empty node, return an empty component
        if (!domNode) {
            return Ext.create('portal.layer.querier.BaseComponent', {});
        }

        for (var i = 0; i < this.factoryList.length; i++) {
            if (this.factoryList[i].supportsNode(domNode)) {
                return this.factoryList[i].parseNode(domNode, wfsUrl);
            }
        }

        throw 'Unsupported node type';
    }
});