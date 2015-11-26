/**
 * Abstract base class representing a source of a WFS feature
 * that can fetch a single feature encoded as a snippet of DOM.
 */
Ext.define('portal.layer.querier.wfs.FeatureSource', {


    /**
     * function(String featureId, String featureType, String wfsUrl, function callback)
     *
     * The callback function to be executed with a feature node on success or null on failure.
     * callback(DOMNode feature,
     *          String featureId,
     *          String featureType,
     *          String wfsUrl)
     */
    getFeature : portal.util.UnimplementedFunction
});
