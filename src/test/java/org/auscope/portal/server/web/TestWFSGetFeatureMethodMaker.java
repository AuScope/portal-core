package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for WFSGetFeatureMethodMaker
 * @author Josh Vote
 *
 */
public class TestWFSGetFeatureMethodMaker {

    /**
     * Tests that the given HttpMethodBase (representing a WFS request) contains
     * a parameter of a specific value
     *
     * Throws an exception if this function cannot decipher the type of HTTP method
     * @param paramName the parameter to check for
     * @param paramValue [Optional] if specified, the value of paramName (if not specified the existence of paramName will be tested)
     * @throws URIException
     */
    private boolean testWFSParam(HttpMethodBase wfsRequest, String paramName, String paramValue) throws URIException {
        //Get methods involve finding the request parameter "version=X"
        if (wfsRequest instanceof GetMethod) {
            String uriString = wfsRequest.getURI().toString();

            if (paramValue == null) {
                return uriString.contains(paramName);
            } else {
                return uriString.contains(String.format("%1$s=%2$s",paramName, paramValue));
            }

        } else if (wfsRequest instanceof PostMethod) {
            //Post methods involve deciphering the POST body
            RequestEntity entity = ((PostMethod) wfsRequest).getRequestEntity();
            if (entity instanceof StringRequestEntity) {
                String content = ((StringRequestEntity) entity).getContent();

                if (paramValue == null) {
                    return content.contains(paramName);
                } else {
                    return content.contains(String.format("%1$s=\"%2$s\"", paramName, paramValue));
                }
            }
        }

        throw new IllegalArgumentException(String.format("Unable to deal with type: %1$s", wfsRequest.getClass()));
    }

    /**
     * Ensure we are always using WFS 1.1.0
     * @throws Exception
     */
    @Test
    public void testWFSVersion() throws Exception {
        final String expectedVersion = "1.1.0";
        final String serviceUrl = "http://example.url";
        final String typeName = "test:typeName";
        final String featureId = "featureId";
        final int maxFeatures = 99;
        final String filterString = "testFilterString"; //technically not a valid OGC Filter
        final String srsName = "epsg:4326";

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, featureId),"version", expectedVersion));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, filterString, maxFeatures),"version", expectedVersion));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName),"version", expectedVersion));
    }

    @Test
    public void testOptionalParams() throws Exception {
        final String serviceUrl = "http://example.url";
        final String typeName = "test:typeName";
        final String featureId = "featureId";

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertFalse(testWFSParam(mm.makeMethod(serviceUrl, typeName, (Integer)null),"maxFeatures", null));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, 99),"maxFeatures", "99"));

        Assert.assertFalse(testWFSParam(mm.makeMethod(serviceUrl, typeName, (String)null),"featureId", null));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, "id-value"),"featureId", "id-value"));
    }
}
