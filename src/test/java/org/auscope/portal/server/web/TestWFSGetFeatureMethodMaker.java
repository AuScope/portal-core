package org.auscope.portal.server.web;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for WFSGetFeatureMethodMaker
 * @author Josh Vote
 *
 */
public class TestWFSGetFeatureMethodMaker extends PortalTestClass {

    /**
     * Tests that the given HttpMethodBase (representing a WFS request) contains
     * a parameter of a specific value
     *
     * Throws an exception if this function cannot decipher the type of HTTP method
     * @param paramName the parameter to check for
     * @param paramValue [Optional] if specified, the value of paramName (if not specified the existence of paramName will be tested)
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private boolean testWFSParam(HttpMethodBase wfsRequest, String paramName, String paramValue) throws ParserConfigurationException, IOException, SAXException {
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

                //Assert that we can parse the contents into a DOM document (ie our XML is valid)
                Assert.assertNotNull(DOMUtil.buildDomFromString(content));

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
        final String filterString = "<filter/>"; //technically not a valid OGC Filter
        final String srsName = "epsg:4326";

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, featureId),"version", expectedVersion));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, filterString, maxFeatures),"version", expectedVersion));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Hits),"version", expectedVersion));
    }

    @Test
    public void testOptionalParams() throws Exception {
        final String serviceUrl = "http://example.url";
        final String typeName = "test:typeName";

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertFalse(testWFSParam(mm.makeMethod(serviceUrl, typeName, (Integer)null),"maxFeatures", null));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, 99),"maxFeatures", "99"));

        Assert.assertFalse(testWFSParam(mm.makeMethod(serviceUrl, typeName, (String)null),"featureId", null));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, "id-value"),"featureId", "id-value"));

        Assert.assertFalse(testWFSParam(mm.makeMethod(serviceUrl, typeName, null, 0, null, null),"resultType", null));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, null, 0, null, ResultType.Hits),"resultType", "hits"));
        Assert.assertTrue(testWFSParam(mm.makeMethod(serviceUrl, typeName, null, 0, null, ResultType.Results),"resultType", "results"));
    }
}
