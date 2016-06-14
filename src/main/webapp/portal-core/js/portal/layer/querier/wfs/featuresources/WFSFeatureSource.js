/**
 * Feature source extensions for pulling features directly from a WFS
 * using the 'feature id' parameter.
 */
Ext.define('portal.layer.querier.wfs.featuresources.WFSFeatureSource', {
    extend : 'portal.layer.querier.wfs.FeatureSource',

    extraParams : null,

    constructor : function(config) {
        this.extraParams = config.extraParams ? config.extraParams : {};
        this.callParent(arguments);
    },

    /**
     * See parent class for definition
     */
    getFeature : function(featureId, featureType, wfsUrl, callback) {
        if (!featureId || !featureType || !wfsUrl) {
            callback(null, featureId, featureType, wfsUrl);
            return;
        }

        var params = Ext.apply({}, this.extraParams);
        Ext.apply(params, {
            serviceUrl : wfsUrl,
            typeName : featureType,
            featureId : featureId
        });

        var me = this;
        portal.util.Ajax.request({
            url : 'requestFeature.do',
            params : params,
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
