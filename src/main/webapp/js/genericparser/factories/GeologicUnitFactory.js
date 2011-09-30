/**
 * Abstract base class for all Generic Parser factories to inherit from.
 */
Ext.ns('GenericParser.Factory');
GenericParser.Factory.GeologicUnitFactory = Ext.extend(GenericParser.Factory.BaseFactory, {

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        GenericParser.Factory.GeologicUnitFactory.superclass.constructor.call(this, cfg);
    },

    /**
     * The simple node supports EVERY type of node as it only displays simplistic information
     * and is intended to be the 'catch all' factory for parsing nodes that have no specific
     * factory written
     */
    supportsNode : function(domNode) {
        return domNode.namespaceURI === this.XMLNS_GSML_2 &&
               domNode.localName === 'GeologicUnit';
    },


    /**
     * Generates a simple tree panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl, rootCfg) {
        //Lookup various fields via xPath
        var gmlId = this._evaluateXPathString(domNode, '@gml:id');
        var coords = this._evaluateXPathString(domNode, 'gsml:occurrence/gsml:MappedFeature/gsml:shape/gml:Point');
        var obsMethod = this._evaluateXPathString(domNode, 'gsml:occurrence/gsml:MappedFeature/gsml:observationMethod/gsml:CGI_TermValue/gsml:value[@codeSpace=\'www.ietf.org/rfc/rfc1738\']');
        var rockMaterial = this._evaluateXPathString(domNode, 'gsml:composition/gsml:CompositionPart/gsml:material/gsml:RockMaterial/gsml:lithology/@xlink:href');
        var proportion = this._evaluateXPathString(domNode, 'gsml:composition/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value');
        var weatheringDesc = this._evaluateXPathString(domNode, 'gsml:weatheringCharacter/gsml:WeatheringDescription/gsml:weatheringProduct/gsml:RockMaterial/gsml:lithology/@xlink:href');

        //Build our component
        Ext.apply(rootCfg, {
            border : false,
            items : [{
                xtype : 'fieldset',
                title : 'Geologic Unit',
                items : [{
                    xtype : 'label',
                    fieldLabel : 'Name',
                    html : this._makeWFSPopupHtml(wfsUrl, 'gsml:GeologicUnit', gmlId, gmlId, 'Click here to open a styled view of this feature.')
                },{
                    xtype : 'label',
                    fieldLabel : 'Location',
                    text : coords
                },{
                    xtype : 'label',
                    fieldLabel : 'Observation Method',
                    html : this._makeVocabPopupHtml('CGI/' + obsMethod, obsMethod, 'Click here to view the SISSVoc concept definition.')
                },{
                    xtype : 'label',
                    fieldLabel : 'Rock Material',
                    html : this._makeVocabPopupHtml('CGI/' + rockMaterial, rockMaterial, 'Click here to view the SISSVoc concept definition.')
                },{
                    xtype : 'label',
                    fieldLabel : 'Proportion',
                    html : this._makeVocabPopupHtml('CGI/' + proportion, proportion, 'Click here to view the SISSVoc concept definition.')
                },{
                    xtype : 'label',
                    fieldLabel : 'Weathering Description',
                    text : weatheringDesc
                }]
            }],
            buttonAlign : 'right',
            buttons : [{
                xtype : 'button',
                text : 'Download Chemistry',
                iconCls : 'download'
            },{
                xtype : 'button',
                text : 'Chemistry Details',
                iconCls : 'info'
            }]
        });
        return new GenericParser.BaseComponent(rootCfg);
    }
});