/**
 * A factory for parsing a gsml:GeologicUnit element.
 */
Ext.ns('GenericParser.Factory');
GenericParser.Factory.GeophysicsAnomaliesFactory = Ext.extend(GenericParser.Factory.BaseFactory, {

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        GenericParser.Factory.GeophysicsAnomaliesFactory.superclass.constructor.call(this, cfg);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === 'http://csiro.au' &&
               SimpleDOM.getNodeLocalName(domNode) === 'Anomalies';
    },

    /**
     * Decomposes a 'normal' URL in the form http://url.com/long/path/name to just its prefix + hostname http://url.com
     * @param url The url to decompose
     */
    getBaseUrl : function(url) {
        var splitUrl = url.split('://'); //this should split us into 2 parts
        return splitUrl[0] + '://' + splitUrl[1].slice(0, splitUrl[1].indexOf('/'));
    },

    /**
     * Generates a simple panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl, rootCfg) {
        var bf = this;
        var gmlId = SimpleXPath.evaluateXPathString(domNode, '@gml:id');
        var anomalyName = SimpleXPath.evaluateXPathString(domNode, 'geophysics:AnomalyName');
        var dataDerivation = SimpleXPath.evaluateXPathString(domNode, 'geophysics:DataDerivation');
        var dataType = SimpleXPath.evaluateXPathString(domNode, 'geophysics:DataType');

        var idPrefix = 'Anomalies.';
        var actualId = gmlId.substring(idPrefix.length);

        var imgUrl = String.format('{0}/getJpeg.aspx?anomalyId={1}', this.getBaseUrl(wfsUrl), actualId);

        //Build our component
        Ext.apply(rootCfg, {
            border : false,
            items : [{
                xtype : 'fieldset',
                title : 'Geophysics Anomaly',
                items : [{
                    xtype : 'label',
                    fieldLabel : 'Id',
                    text : actualId
                },{
                    xtype : 'label',
                    fieldLabel : 'Anomaly Name',
                    text : anomalyName
                },{
                    xtype : 'label',
                    fieldLabel : 'Data Derivation',
                    text : dataDerivation
                },{
                    xtype : 'label',
                    fieldLabel : 'Data Type',
                    text : dataType
                },{
                    xtype : 'box',
                    anchor : '',
                    isFormField : true,
                    fieldLabel : 'Image',
                    autoEl : {
                        tag:'div', children:[{
                            tag : 'img',
                            //qtip : 'You can also have a tooltip on the image',
                            src : imgUrl
                        }]
                    }
                }]
            }],
            buttonAlign : 'right',
            buttons : [{
                xtype : 'button',
                text : 'Download Anomaly',
                iconCls : 'download',
                handler : function() {
                    var getXmlUrl = bf._makeFeatureRequestUrl(wfsUrl, 'geophysics:Anomalies', gmlId);
                    var url = 'downloadGMLAsZip.do?serviceUrls=' + escape(getXmlUrl);
                    FileDownloader.downloadFile(url);
                }
            }]
        });
        return new GenericParser.BaseComponent(rootCfg);
    }
});