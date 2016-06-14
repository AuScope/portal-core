/**
 * Feature source extension for pulling features directly from a WFS
 * using the a filter on a single property.
 *
 * Only the first feature matching a particular filter will be returned
 */
Ext.define('portal.layer.querier.wfs.featuresources.WFSFeatureByPropertySource', {
    extend : 'portal.layer.querier.wfs.FeatureSource',

    property : null,
    value : null,

    /**
     * Accepts a config in the form
     * {
     *  property : String - the property name to filter against
     *  value : [Optional] String - the value of the property to be used as a comparison match. If omitted, the featureId will be used
     * }
     */
    constructor : function(config) {
        this.property = config.property;
        this.value = config.value;

        this.callParent(arguments);
    },

    /**
     * See parent class for definition
     */
    getFeature : function(featureId, featureType, wfsUrl, callback) {
        var value = this.value ? this.value : featureId;

        var me = this;
        portal.util.Ajax.request({
            url : 'requestFeatureByProperty.do',
            params : {
                serviceUrl : wfsUrl,
                typeName : featureType,
                property : this.property,
                value : value
            },
            callback : function(success, data) {
                if (!success) {
                    callback(null, featureId, featureType, wfsUrl);
                    return;
                }

                // Load our xml string into DOM, extract the first feature
                var xmlDocument = portal.util.xml.SimpleDOM.parseStringToDOM(data.gml);
                if (!xmlDocument) {
                    callback(null, featureId, featureType, wfsUrl);
                    return;
                }
                var featureMemberNodes = portal.util.xml.SimpleDOM.getMatchingChildNodes(xmlDocument.documentElement, 'http://www.opengis.net/gml', 'featureMember');
                if (featureMemberNodes.length === 0) {
                    featureMemberNodes = portal.util.xml.SimpleDOM.getMatchingChildNodes(xmlDocument.documentElement, 'http://www.opengis.net/gml', 'featureMembers');
                }
                if (featureMemberNodes.length === 0 || featureMemberNodes[0].childNodes.length === 0) {
                    //we got an empty response - likely because the feature ID DNE.
                    callback(null, featureId, featureType, wfsUrl);
                    return;
                }

                callback(featureMemberNodes[0].childNodes[0], featureId, featureType, wfsUrl);
            }
        });
    }
});
