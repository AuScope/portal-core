/**
 * A factory for parsing a geophysics:Anomalies element.
 */
Ext.define('portal.layer.querier.wfs.factories.GeophysicsAnomaliesFactory', {
    extend : 'portal.layer.querier.wfs.factories.BaseFactory',

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === 'http://csiro.au' &&
               portal.util.xml.SimpleDOM.getNodeLocalName(domNode) === 'Anomalies';
    },

    /**
     * Generates a simple panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl) {
        var gmlId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, '@gml:id');
        var anomalyName = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'geophysics:AnomalyName');
        var dataDerivation = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'geophysics:DataDerivation');
        var dataType = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'geophysics:DataType');

        var actualId = gmlId.substring('Anomalies.'.length);

        //ASSUMPTION - image service at same host as geoserver
        var baseUrl = this._getBaseUrl(wfsUrl);
        var imgUrl = baseUrl + '/getJpeg.aspx?anomalyId=' + escape(actualId);

        //Build our component
        return Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            autoScroll : true,
            items : [{
                xtype : 'fieldset',
                title : 'Geophysics Anomaly',
                items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'Id',
                    value : gmlId
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Anomaly Name',
                    value : anomalyName
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Data Derivation',
                    value : dataDerivation
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Data Type',
                    value : dataType
                },{
                    xtype : 'box',
                    autoEl : {
                        tag:'div',
                        children:[{
                            tag : 'img',
                            src : imgUrl
                        }]
                    }
                }]
            }]
        });
    }
});