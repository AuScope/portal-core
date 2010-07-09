package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * User: Mathew Wyatt
 * Date: 01/07/2009
 * Time: 3:22:21 PM
 */
public interface IWFSGetFeatureMethodMaker {
    
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString, int maxFeatures) throws Exception;
    public HttpMethodBase makeMethod(String serviceURL, String featureType, String filterString, int maxFeatures, String srsName) throws Exception;
}
