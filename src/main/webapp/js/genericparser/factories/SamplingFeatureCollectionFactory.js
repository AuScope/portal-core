/**
 * A factory for parsing a sa:LocatedSpecimen element.
 */
Ext.ns('GenericParser.Factory');
GenericParser.Factory.SamplingFeatureCollectionFactory = Ext.extend(GenericParser.Factory.BaseFactory, {

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        GenericParser.Factory.SamplingFeatureCollectionFactory.superclass.constructor.call(this, cfg);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === this.XMLNS_SA &&
            SimpleDOM.getNodeLocalName(domNode) === 'SamplingFeatureCollection';
    },

    /**
     * Generates a panel containing all located specimen observations
     */
    parseNode : function(domNode, wfsUrl, rootCfg) {
        var samplingName = SimpleXPath.evaluateXPathString(domNode, 'gml:name');
        var samplingStart = SimpleXPath.evaluateXPathString(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:samplingTime/gml:TimePeriod/gml:beginPosition');
        var samplingEnd = SimpleXPath.evaluateXPathString(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:samplingTime/gml:TimePeriod/gml:endPosition');
        var location = SimpleXPath.evaluateXPathString(domNode, 'sa:member/sa:LocatedSpecimen/sa:samplingLocation/gml:LineString/gml:posList');
        var allTitles = SimpleXPath.evaluateXPathNodeArray(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:observedProperty/@xlink:title');
        var allAmounts = SimpleXPath.evaluateXPathNodeArray(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:result');
        var allUoms = SimpleXPath.evaluateXPathNodeArray(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:result/@uom');


        var items = [];
        for (var i = 0; i < allTitles.length; i++) {
            var uom = SimpleDOM.getNodeTextContent(allUoms[i]);
            var prettyUom = uom;

            switch(uom) {
            case 'http://www.opengis.net/def/uom/UCUM/0/%25':
                prettyUom = '%';
                break;
            case 'http://www.opengis.net/def/uom/UCUM/0/%5Bppth%5D':
                prettyUom = '[ppth]';
                break;
            }


            items.push({
                xtype : 'label',
                fieldLabel : SimpleDOM.getNodeTextContent(allTitles[i]),
                html : String.format('<p qtip="Unit of Measurement: {1}">{0} {2}</p>', SimpleDOM.getNodeTextContent(allAmounts[i]), uom, prettyUom)
            });
        }

        //Build our component
        Ext.apply(rootCfg, {
            layout : 'fit',
            items : [{
                xtype : 'fieldset',
                title : 'Sampling Feature Collection',
                items : [{
                    xtype : 'label',
                    fieldLabel : 'Name',
                    html : this._makeGeneralPopupHtml(samplingName, samplingName, 'Click here for the raw WFS data')
                },{
                    xtype : 'label',
                    fieldLabel : 'Location',
                    text : location
                },{
                    xtype : 'label',
                    fieldLabel : 'Sampling Start',
                    text : samplingStart
                },{
                    xtype : 'label',
                    fieldLabel : 'Sampling End',
                    text : samplingEnd
                },{
                    xtype : 'fieldset',
                    title : 'Observations',
                    items : items
                }]
            }]
        });
        return new GenericParser.BaseComponent(rootCfg);
    }
});