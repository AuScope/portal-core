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
     * Tests that the given HttpMethodBase (representing a WFS request) is making a request
     * using the specified WFS version
     *
     * Throws an exception if this function cannot decipher the type of HTTP method
     * @param wfsRequest
     * @param expectedVersion
     * @throws URIException
     */
    private boolean testWFSVersion(HttpMethodBase wfsRequest, String expectedVersion) throws URIException {
        //Get methods involve finding the request parameter "version=X"
        if (wfsRequest instanceof GetMethod) {
            String uriString = wfsRequest.getURI().toString();

            return uriString.contains(String.format("version=%1$s", expectedVersion));
        } else if (wfsRequest instanceof PostMethod) {
            //Post methods involve deciphering the POST body
            RequestEntity entity = ((PostMethod) wfsRequest).getRequestEntity();
            if (entity instanceof StringRequestEntity) {
                String content = ((StringRequestEntity) entity).getContent();

                return content.contains(String.format("version=\"%1$s\"", expectedVersion));
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

        Assert.assertTrue(testWFSVersion(mm.makeMethod(serviceUrl, typeName, featureId), expectedVersion));
        Assert.assertTrue(testWFSVersion(mm.makeMethod(serviceUrl, typeName, filterString, maxFeatures), expectedVersion));
        Assert.assertTrue(testWFSVersion(mm.makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName), expectedVersion));
    }
}
