package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * User: Mathew Wyatt
 * Date: 01/07/2009
 * Time: 3:22:21 PM
 */
public interface IWFSGetFeatureMethodMaker {
    /**
     * This is an artificial cap that should be applied when fetching features to visualize (render on the map)
     */
    public static final int MAX_FEATURES_TO_VISUALIZE = 200;
    
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString, int maxFeatures) throws Exception;
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString, int maxFeatures, String srsName) throws Exception;
}
