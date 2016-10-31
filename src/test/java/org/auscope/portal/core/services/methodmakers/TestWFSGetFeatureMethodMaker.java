package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.namespaces.WFSNamespaceContext;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for WFSGetFeatureMethodMaker
 * 
 * @author Josh Vote
 *
 */
public class TestWFSGetFeatureMethodMaker extends PortalTestClass {

    /**
     * Tests that the given HttpMethodBase (representing a WFS request) contains a parameter of a specific value
     *
     * Throws an exception if this function cannot decipher the type of HTTP method
     * 
     * @param paramName
     *            the parameter to check for
     * @param paramValue
     *            [Optional] if specified, the value of paramName (if not specified the existence of paramName will be tested)
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private static boolean testWFSParam(HttpRequestBase wfsRequest, String paramName, String paramValue)
            throws ParserConfigurationException, IOException, SAXException {
        //Get methods involve finding the request parameter "version=X"
        if (wfsRequest instanceof HttpGet) {
            String uriString = wfsRequest.getURI().toString();

            if (paramValue == null) {
                return uriString.contains(paramName);
            } else {
                return uriString.contains(String.format("%1$s=%2$s", paramName, paramValue));
            }

        } else if (wfsRequest instanceof HttpPost) {
            //Post methods involve deciphering the POST body
            HttpEntity entity = ((HttpPost) wfsRequest).getEntity();
            if (entity instanceof StringEntity) {
                String content = IOUtils.toString((((StringEntity) entity).getContent()));

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
     * @throws URISyntaxException 
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testWFSVersion() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        final String expectedVersion = "1.1.0";
        final String serviceUrl = "http://example.url";
        final String typeName = "test:typeName";
        final String featureId = "featureId";
        final int maxFeatures = 99;
        final String filterString = "<filter/>"; //technically not a valid OGC Filter
        final String srsName = "epsg:4326";

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertTrue(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, featureId, srsName), "version",
                expectedVersion));
        Assert.assertTrue(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, filterString, maxFeatures, srsName),
                "version", expectedVersion));
        Assert.assertTrue(testWFSParam(
                mm.makePostMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Hits),
                "version", expectedVersion));
        Assert.assertTrue(testWFSParam(mm.makeGetCapabilitiesMethod(serviceUrl), "version", expectedVersion));
    }

    /**
     * Ensure we are always specifying service=WFS
     * @throws URISyntaxException 
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testServiceParam() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        final String serviceUrl = "http://example.url";
        final String typeName = "test:typeName";
        final String featureId = "featureId";

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertTrue(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, featureId, ""), "service", "WFS"));
    }

    @Test
    public void testOptionalParams() throws ParserConfigurationException, IOException, SAXException, URISyntaxException  {
        final String serviceUrl = "http://example.url";
        final String typeName = "test:typeName";
        final String srsName = "srs-name";
        final String outputFormat = "o-f";
        final FilterBoundingBox bbox = new FilterBoundingBox("bbox-srs", new double[] {1, 2}, new double[] {3, 4});

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertFalse(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, (Integer) null, null), "maxFeatures",
                null));
        Assert.assertTrue(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, 99, null), "maxFeatures", "99"));

        Assert.assertFalse(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, (String) null, null), "featureId", null));
        Assert.assertTrue(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, "id-value", null), "featureId",
                "id-value"));

        Assert.assertFalse(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, (String) null, null), "srsName", null));
        Assert.assertTrue(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, (String) null, srsName), "srsName",
                srsName));

        Assert.assertFalse(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, (String) null, (Integer) null, null),
                "srsName", null));
        Assert.assertTrue(testWFSParam(
                mm.makeGetMethod(serviceUrl, typeName, (String) null, (Integer) null, outputFormat), "srsName",
                outputFormat));

        Assert.assertFalse(testWFSParam(mm.makePostMethod(serviceUrl, typeName, null, 0, null, null), "resultType",
                null));
        Assert.assertTrue(testWFSParam(mm.makePostMethod(serviceUrl, typeName, null, 0, null, ResultType.Hits),
                "resultType", "hits"));
        Assert.assertTrue(testWFSParam(mm.makePostMethod(serviceUrl, typeName, null, 0, null, ResultType.Results),
                "resultType", "results"));

        Assert.assertFalse(testWFSParam(mm.makePostMethod(serviceUrl, typeName, null, 0, null, null, ""),
                "outputFormat", null));
        Assert.assertFalse(testWFSParam(mm.makePostMethod(serviceUrl, typeName, null, 0, null, null, null),
                "outputFormat", null));
        Assert.assertTrue(testWFSParam(mm.makePostMethod(serviceUrl, typeName, null, 0, null, null, outputFormat),
                "outputFormat", outputFormat));

        Assert.assertFalse(testWFSParam(
                mm.makeGetMethod(serviceUrl, typeName, (Integer) null, null, (FilterBoundingBox) null), "bbox", null));
        Assert.assertTrue(testWFSParam(mm.makeGetMethod(serviceUrl, typeName, 99, null, bbox), "bbox",
                "1.0%2C2.0%2C3.0%2C4.0%2Cbbox-srs"));
    }

    /**
     * Ensure the GetCapabilities statement is well formed as per OGC specifications
     * @throws URISyntaxException 
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testGetCap() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        final String serviceUrl = "http://example.url";

        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();

        Assert.assertTrue(testWFSParam(mm.makeGetCapabilitiesMethod(serviceUrl), "service", "WFS"));
        Assert.assertTrue(testWFSParam(mm.makeGetCapabilitiesMethod(serviceUrl), "version",
                WFSGetFeatureMethodMaker.WFS_VERSION));
        Assert.assertTrue(testWFSParam(mm.makeGetCapabilitiesMethod(serviceUrl), "request", "GetCapabilities"));
    }

    private class MyNamespace extends WFSNamespaceContext {
        public MyNamespace() {
            map.put("p1", "v1");
            map.put("p2", "v2");
        }
    }

    @Test
    public void testNamespaces() throws ParserConfigurationException, IOException, SAXException {
        WFSGetFeatureMethodMaker mm = new WFSGetFeatureMethodMaker();
        mm.setNamespaces(new MyNamespace());

        Assert.assertTrue(testWFSParam(mm.makePostMethod("http://example.org", "type:Name", null, 0, null, null, null),
                "xmlns:p1", "v1"));
        Assert.assertTrue(testWFSParam(mm.makePostMethod("http://example.org", "type:Name", null, 0, null, null, null),
                "xmlns:p2", "v2"));
    }
}
