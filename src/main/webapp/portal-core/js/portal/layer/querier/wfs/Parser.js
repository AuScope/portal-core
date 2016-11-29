/**
 * Class for transforming a W3C DOM Document into a GenericParserComponent
 * by utilising a number of 'plugin' factories.
 */
Ext.define('portal.layer.querier.wfs.Parser', {
    extend: 'Ext.util.Observable',

    /**
     * Builds a new Parser from a list of factories. Factories in factoryList will be tested before
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
            var cfg = {
                parser : this
            };

            for (var i = 0; i < config.factoryNames.length; i++) {
                this.factoryList.push(Ext.create(config.factoryNames[i], cfg));
            }
        }
        // Call our superclass constructor to complete construction process.
        this.callParent(arguments);
    },

    /**
     * Iterates through all internal factories until domNode can be parsed into GenericParser.BaseComponent
     * @param domNode The W3C DOM node to parse
     * @param wfsUrl The original WFS URL where domNode was sourced from
     * @param rootCfg [Optional] An Object whose properties will be applied to the top level component parsed (a GenericParser.BaseComponent instance)
     */
    parseNode : function(domNode, wfsUrl, applicationProfile) {
        //In the event of an empty node, return an empty component
        if (!domNode) {
            return Ext.create('portal.layer.querier.BaseComponent', {});
        }

        for (var i = 0; i < this.factoryList.length; i++) {
            if (this.factoryList[i].supportsNode(domNode)) {
                return this.factoryList[i].parseNode(domNode, wfsUrl, applicationProfile);
            }
        }

        throw 'Unsupported node type';
    }
});