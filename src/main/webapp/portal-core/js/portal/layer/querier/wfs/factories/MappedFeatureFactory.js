/**
 * A factory for parsing a gsml:MappedFeature element.
 */
Ext.define('portal.layer.querier.wfs.factories.MappedFeatureFactory', {
    extend : 'portal.layer.querier.wfs.factories.BaseFactory',

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === this.XMLNS_GSML_2 &&
               portal.util.xml.SimpleDOM.getNodeLocalName(domNode) === 'MappedFeature';
    },

    /**
     * Generates a simple tree panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl) {
        var bf = this;

        var gmlId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, '@gml:id');
        var allNames = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'gsml:specification/er:MineralOccurrence/gml:name');
        var specification = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'gsml:specification/er:MineralOccurrence/er:commodityDescription[1]/er:Commodity/er:source/@xlink:href');
        var type = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:specification/er:MineralOccurrence/er:type');
        var minDepositGroup = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gsml:specification/er:MineralOccurrence/er:classification/er:MineralDepositModel/er:mineralDepositGroup');

        var allCommodityNames = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'gsml:specification/er:MineralOccurrence/er:commodityDescription/er:Commodity/gml:name');


        //For IE we can't apply XPath predicates like gml:name[@codeSpace=\'http://www.ietf.org/rfc/rfc2616\']
        //so we do a manual loop instead over gml:name instead
        var idNodes = this._filterNodesWithXPath(allNames, '@codeSpace', 'http://www.ietf.org/rfc/rfc2616');
        var rawId = '';
        if (idNodes.length > 0) {
            rawId = portal.util.xml.SimpleDOM.getNodeTextContent(idNodes[0]);
        }

        //Our items list is dynamic in size
        var items = [{
            xtype : 'displayfield',
            fieldLabel : 'Name',
            value : this._makeWfsUriPopupHtml(rawId, rawId, 'Click here for more information about this feature.')
        },{
            xtype : 'displayfield',
            fieldLabel : 'Type',
            value : type
        },{
            xtype : 'displayfield',
            fieldLabel : 'Mineral Deposit Group',
            value : minDepositGroup
        }];

        //Push out commodity descriptors
        var rfcCommodityNodes = this._filterNodesWithXPath(allCommodityNames, '@codeSpace', 'http://www.ietf.org/rfc/rfc2616');
        for (var i = 0; i < rfcCommodityNodes.length; i++) {
            var commodityUri =  portal.util.xml.SimpleDOM.getNodeTextContent(rfcCommodityNodes[i]);
            items.push({
                xtype : 'displayfield',
                fieldLabel : 'Commodity Descriptor',
                value : this._makeWfsUriPopupHtml(commodityUri, commodityUri, 'Click here for the more information about this commodity.')
            });
        }

        //Build our component
        return Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            layout : 'fit',
            items : [{
                xtype : 'fieldset',
                title : 'Mapped Feature',
                labelWidth : 75,
                autoScroll : true,
                items : items
            }],
            buttonAlign : 'right',
            buttons : [{
                text : 'Download Feature',
                iconCls : 'download',
                handler : function() {
                    var getXmlUrl = bf._makeFeatureRequestUrl(wfsUrl, 'gsml:MappedFeature', gmlId);
                    var url = 'downloadGMLAsZip.do?serviceUrls=' + escape(getXmlUrl);
                    portal.util.FileDownloader.downloadFile(url);
                }
            }]
        });
    }
});