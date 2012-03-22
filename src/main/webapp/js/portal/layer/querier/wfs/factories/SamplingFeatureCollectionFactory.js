/**
 * A factory for parsing a sa:LocatedSpecimen element.
 */
Ext.define('portal.layer.querier.wfs.factories.SamplingFeatureCollectionFactory', {
    extend : 'portal.layer.querier.wfs.factories.BaseFactory',

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === this.XMLNS_SA &&
            portal.util.xml.SimpleDOM.getNodeLocalName(domNode) === 'SamplingFeatureCollection';
    },

    /**
     * Generates a panel containing all located specimen observations
     */
    parseNode : function(domNode, wfsUrl) {
        var samplingName = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'gml:name');
        var samplingStart = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:samplingTime/gml:TimePeriod/gml:beginPosition');
        var samplingEnd = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:samplingTime/gml:TimePeriod/gml:endPosition');
        var location = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'sa:member/sa:LocatedSpecimen/sa:samplingLocation/gml:LineString/gml:posList');
        var allTitles = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:observedProperty/@xlink:title');
        var allAmounts = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:result');
        var allUoms = portal.util.xml.SimpleXPath.evaluateXPathNodeArray(domNode, 'sa:member/sa:LocatedSpecimen/sa:relatedObservation/om:Observation/om:result/@uom');


        var items = [];
        for (var i = 0; i < allTitles.length; i++) {
            var uom = portal.util.xml.SimpleDOM.getNodeTextContent(allUoms[i]);
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
                fieldLabel : portal.util.xml.SimpleDOM.getNodeTextContent(allTitles[i]),
                html : Ext.util.Format.format('<p qtip="Unit of Measurement: {1}">{0} {2}</p>', portal.util.xml.SimpleDOM.getNodeTextContent(allAmounts[i]), uom, prettyUom)
            });
        }

        //Build our component
        return Ext.create('portal.layer.querier.BaseComponent', {
            layout : 'fit',
            items : [{
                xtype : 'fieldset',
                title : 'Sampling Feature Collection',
                items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'Name',
                    value : this._makeGeneralPopupHtml(samplingName, samplingName, 'Click here for the raw WFS data')
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Location',
                    value : location
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Sampling Start',
                    value : samplingStart
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Sampling End',
                    value : samplingEnd
                },{
                    xtype : 'fieldset',
                    title : 'Observations',
                    items : items
                }]
            }]
        });
    }
});