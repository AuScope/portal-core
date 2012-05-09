/**
 * A factory for parsing a er:MiningFeatureOccurrence element.
 */
Ext.define('portal.layer.querier.wfs.factories.MiningFeatureOccurrenceFactory', {
    extend : 'portal.layer.querier.wfs.factories.BaseFactory',

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === this.XMLNS_ER &&
               portal.util.xml.SimpleDOM.getNodeLocalName(domNode) === 'MiningFeatureOccurrence';
    },

    /**
     * Generates a simple tree panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl) {
        var bf = this;

        var gmlId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, '@gml:id');

        var gmlNameNodes = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'er:specification/er:Mine/gml:name');
        var coordinateString = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'er:location/gml:Point/gml:pos');
        var status = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'er:specification/er:Mine/er:status');
        var mineNameNodes = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'er:specification/er:Mine/er:mineName/er:MineName');

        //Our preferred name is based on an encoded boolean
        var preferredName = '';
        var preferredNameNodes = this._filterNodesWithXPath(mineNameNodes, 'er:isPreferred', 'true');
        if (preferredNameNodes.length > 0) {
            preferredName = portal.util.xml.SimpleXPath.evaluateXPathString(preferredNameNodes[0], 'er:mineName');
        }

        var nameUri = '';
        var rfcNameNodes = this._filterNodesWithXPath(gmlNameNodes, '@codeSpace', 'http://www.ietf.org/rfc/rfc2616');
        if (rfcNameNodes.length > 0) {
            nameUri = portal.util.xml.SimpleDOM.getNodeTextContent(rfcNameNodes[0]);
        }

        //Build our component
        return Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            layout : 'fit',
            items : [{
                xtype : 'fieldset',
                title : 'Mining Feature Occurrence',
                labelWidth : 75,
                autoScroll : true,
                items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'Name',
                    value : this._makeWfsUriPopupHtml(nameUri, nameUri, 'Click here for more information about this feature.')
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Preferred Name',
                    value : preferredName
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Location',
                    value : coordinateString
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Status',
                    value : status
                }]
            }],
            buttonAlign : 'right',
            buttons : [{
                text : 'Download Feature',
                iconCls : 'download',
                handler : function() {
                    var getXmlUrl = bf._makeFeatureRequestUrl(wfsUrl, 'er:MiningFeatureOccurrence', gmlId);
                    var url = 'downloadGMLAsZip.do?serviceUrls=' + escape(getXmlUrl);
                    portal.util.FileDownloader.downloadFile(url);
                }
            }]
        });
    }
});