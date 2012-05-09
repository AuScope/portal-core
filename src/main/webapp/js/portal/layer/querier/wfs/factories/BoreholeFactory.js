/**
 * A factory for parsing a gsml:Borehole element.
 */
Ext.define('portal.layer.querier.wfs.factories.BoreholeFactory', {
    extend : 'portal.layer.querier.wfs.factories.BaseFactory',

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === this.XMLNS_GSML_2 &&
               portal.util.xml.SimpleDOM.getNodeLocalName(domNode) === 'Borehole';
    },

    /**
     * Generates a simple panel that represents the specified borehole node
     */
    parseNode : function(domNode, wfsUrl) {
        var bf = this;
        var gmlId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, '@gml:id');
        var allNames = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'gml:name');
        var elevationUom = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation/@uomLabels');
        var elevation = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:collarLocation/gsml:BoreholeCollar/gsml:elevation');
        var startDepth = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:lowerCorner');
        var endDepth = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:coredInterval/gml:Envelope/gml:upperCorner');
        var coreCustodian = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian/@xlink:title');
        var drillingCo = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:driller/@xlink:title');
        var drillingDate = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling');
        var drillingMethod = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:drillingMethod');
        var inclinationType = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:inclinationType');
        var startPoint = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:indexData/gsml:BoreholeDetails/gsml:startPoint');

        //For IE we can't apply XPath predicates like gml:name[@codeSpace=\'http://www.ietf.org/rfc/rfc2616\']
        //so we do a manual loop instead over gml:name instead
        var rawId = '';
        var boreholeName = '';
        for (var i = 0; i < allNames.length; i++) {
            if (portal.util.xml.SimpleXPath.evaluateXPathString(allNames[i], '@codeSpace') === 'http://www.ietf.org/rfc/rfc2616') {
                rawId = portal.util.xml.SimpleDOM.getNodeTextContent(allNames[i]);
            } else {
                boreholeName = portal.util.xml.SimpleDOM.getNodeTextContent(allNames[i]);
            }
        }

        //Build our component
        return Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            layout : 'fit',
            items : [{
                xtype : 'fieldset',
                title : 'Borehole',
                labelWidth : 75,
                autoScroll : true,
                items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'Name',
                    value : this._makeGeneralPopupHtml(rawId, boreholeName, 'Click here for the raw WFS data')
                },{
                    xtype : 'displayfield',
                    fieldLabel : Ext.util.Format.format('Elevation ({0})',elevationUom),
                    value : elevation
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Start Depth (m)',
                    value : startDepth
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'End Depth (m)',
                    value : endDepth
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Core Custodian',
                    value : coreCustodian
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Drilling Co.',
                    value : drillingCo
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Drilling Date',
                    value : drillingDate
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Drilling Method',
                    value : drillingMethod
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Inclination Type',
                    value : inclinationType
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Start Point',
                    value : startPoint
                }]
            }],
            buttonAlign : 'right',
            buttons : [{
                text : 'Download Borehole',
                iconCls : 'download',
                handler : function() {
                    var getXmlUrl = bf._makeFeatureRequestUrl(wfsUrl, 'gsml:Borehole', gmlId);
                    var url = 'downloadGMLAsZip.do?serviceUrls=' + escape(getXmlUrl);
                    portal.util.FileDownloader.downloadFile(url);
                }
            }]
        });
    }
});