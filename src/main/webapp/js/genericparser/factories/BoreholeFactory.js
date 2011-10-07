/**
 * A factory for parsing a gsml:GeologicUnit element.
 */
Ext.ns('GenericParser.Factory');
GenericParser.Factory.BoreholeFactory = Ext.extend(GenericParser.Factory.BaseFactory, {

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        GenericParser.Factory.BoreholeFactory.superclass.constructor.call(this, cfg);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === this.XMLNS_GSML_2 &&
               this._getNodeLocalName(domNode) === 'Borehole';
    },

    /**
     * Generates a simple tree panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl, rootCfg) {

        var gmlId = this._evaluateXPathString(domNode, '@gml:id');
        var allNames = this._evaluateXPathNodeArray(domNode, 'gml:name');
        var elevationUom = this._evaluateXPathString(domNode, 'gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation/@uomLabels');
        var elevation = this._evaluateXPathString(domNode, 'gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation');
        var startDepth = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:lowerCorner');
        var endDepth = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:upperCorner');
        var coreCustodian = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian/@xlink:title');
        var drillingCo = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:driller/@xlink:title');
        var drillingDate = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling');
        var drillingMethod = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:drillingMethod');
        var inclinationType = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:inclinationType');
        var startPoint = this._evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:startPoint');

        //For IE we can't apply XPath predicates like gml:name[@codeSpace=\'http://www.ietf.org/rfc/rfc2616\']
        //so we do a manual loop instead over gml:name instead
        var rawId = '';
        var boreholeName = '';
        for (var i = 0; i < allNames.length; i++) {
            if (this._evaluateXPathString(allNames[i], '@codeSpace') === 'http://www.ietf.org/rfc/rfc2616') {
                rawId = this._getNodeTextContent(allNames[i]);
            } else {
                boreholeName = this._getNodeTextContent(allNames[i]);
            }
        }

        //Build our component
        Ext.apply(rootCfg, {
            border : false,
            items : [{
                xtype : 'fieldset',
                title : 'Borehole',
                items : [{
                    xtype : 'label',
                    fieldLabel : 'Name',
                    html : this._makeGeneralPopupHtml(rawId, boreholeName, 'Click here for the raw WFS data')
                },{
                    xtype : 'label',
                    fieldLabel : String.format('Elevation ({0})',elevationUom),
                    text : elevation
                },{
                    xtype : 'label',
                    fieldLabel : 'Start Depth (m)',
                    text : startDepth
                },{
                    xtype : 'label',
                    fieldLabel : 'End Depth (m)',
                    text : endDepth
                },{
                    xtype : 'label',
                    fieldLabel : 'Core Custodian',
                    text : coreCustodian
                },{
                    xtype : 'label',
                    fieldLabel : 'Drilling Co.',
                    text : drillingCo
                },{
                    xtype : 'label',
                    fieldLabel : 'Drilling Date',
                    text : drillingDate
                },{
                    xtype : 'label',
                    fieldLabel : 'Drilling Method',
                    text : drillingMethod
                },{
                    xtype : 'label',
                    fieldLabel : 'Inclination Type',
                    text : inclinationType
                },{
                    xtype : 'label',
                    fieldLabel : 'Start Point',
                    text : startPoint
                }]
            }]
            //buttonAlign : 'right',
            //buttons : []
        });
        return new GenericParser.BaseComponent(rootCfg);
    }
});