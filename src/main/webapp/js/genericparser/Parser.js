/**
 * Class for transforming a W3C DOM Document into a GenericParserComponent
 * by utilising a number of 'plugin' factories.
 */
Ext.ns('GenericParser');
GenericParser.Parser = Ext.extend(Ext.util.Observable, {

    factoryList : [],

    /**
     * Accepts all Ext.util.Observable configuration options with the following additions
     * {
     *
     * }
     */
    constructor : function(cfg) {
        GenericParser.Parser.superclass.constructor.call(this, cfg);

        //The following ordering is important as it dictates the order in which to try
        //factories for parsing a particular node
        var cfg = {
            genericParser : this
        };
        this.factoryList.push(new GenericParser.Factory.GeologicUnitFactory(cfg));
        this.factoryList.push(new GenericParser.Factory.LocatedSpecimenFactory(cfg));
        this.factoryList.push(new GenericParser.Factory.SamplingFeatureCollectionFactory(cfg));
        this.factoryList.push(new GenericParser.Factory.BoreholeFactory(cfg));
        this.factoryList.push(new GenericParser.Factory.SimpleFactory(cfg));//The simple factory should always go last
    },


    /**
     * Iterates through all internal factories until domNode can be parsed into GenericParser.BaseComponent
     * @param domNode The W3C DOM node to parse
     * @param wfsUrl The original WFS URL where domNode was sourced from
     * @param rootCfg [Optional] An Object whose properties will be applied to the top level component parsed (a GenericParser.BaseComponent instance)
     */
    parseNode : function(domNode, wfsUrl, rootCfg) {

        //In the event of an empty node, return an empty component
        if (!domNode) {
            return new GenericParser.BaseComponent((rootCfg ? rootCfg : {}));
        }

        for (var i = 0; i < this.factoryList.length; i++) {
            if (this.factoryList[i].supportsNode(domNode)) {
                return this.factoryList[i].parseNode(domNode, wfsUrl, (rootCfg ? rootCfg : {}));
            }
        }

        throw 'Unsupported node type';
    }
});