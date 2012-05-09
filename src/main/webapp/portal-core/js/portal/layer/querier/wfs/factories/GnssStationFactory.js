/**
 * A factory for parsing a ngcp:GnssStation element.
 */
Ext.define('portal.layer.querier.wfs.factories.GnssStationFactory', {
    extend : 'portal.layer.querier.wfs.factories.BaseFactory',

    /**
     * Accepts all GenericParser.Factory.BaseFactory configuration options
     */
    constructor : function(cfg) {
        this.callParent(arguments);
    },

    supportsNode : function(domNode) {
        return domNode.namespaceURI === 'http://www.auscope.org/ngcp' &&
               portal.util.xml.SimpleDOM.getNodeLocalName(domNode) === 'GnssStation';
    },

    /**
     * Generates a simple panel that represents the specified node
     */
    parseNode : function(domNode, wfsUrl) {
        var gmlId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, '@gml:id');

        var stationId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:GPSSITEID');
        var name = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:STATIONNAME');
        var type = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:STATIONTYPE');
        var countryId = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:COUNTRYID');
        var state = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:STATEID');
        var location = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:GEOM/gml:Point/gml:pos');
        var coordinateNo = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:COORDINO');
        var datum = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:DATUM');
        var equipment = portal.util.xml.SimpleXPath.evaluateXPathString(domNode, 'ngcp:EQUIPMENT');

        //Build our component
        return Ext.create('portal.layer.querier.BaseComponent', {
            border : false,
            autoScroll : true,
            items : [{
                xtype : 'fieldset',
                title : 'GNSS Station',
                items : [{
                    xtype : 'displayfield',
                    fieldLabel : 'Station Id',
                    value : stationId
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Name',
                    value : name
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Type',
                    value : type
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Country Id',
                    value : countryId
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'State',
                    value : state
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Location',
                    value : location
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'CoordinateNO',
                    value : coordinateNo
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'DATUM',
                    value : datum
                },{
                    xtype : 'displayfield',
                    fieldLabel : 'Equipment',
                    value : equipment
                }]
            }]
        });
    }
});