package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * Implementors will provide methods that will generate a request that will describe a coverage (as opposed to
 * returning its raw data).
 * @author vot002
 *
 */
public interface IWCSDescribeCoverageMethodMaker {
    /**
     * Will make a method that will perform a DescribeCoverage request
     * to the given serviceUrl and return an XML output describing layerName
     * @param serviceUrl The url to query
     * @param layerName The layer to query
     * @return
     */
    public HttpMethodBase makeMethod(String serviceUrl, String layerName) throws Exception;
}
