package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for SOSMethodMaker
 * 
 * @author Josh Vote
 *
 */
public class TestSOSMethodMaker extends PortalTestClass {

    /**
     * Tests that the given HttpMethodBase (representing a SOS request) contains a parameter of a specific value
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
    private static boolean testSOSParam(HttpRequestBase sosRequest, String paramName, String paramValue)
            throws ParserConfigurationException, IOException, SAXException {
        //Get methods involve finding the request parameter "version=X"
        if (sosRequest instanceof HttpGet) {
            String uriString = sosRequest.getURI().toString();

            if (paramValue == null) {
                return uriString.contains(paramName);
            } else {
                return uriString.contains(String.format("%1$s=%2$s", paramName, paramValue));
            }

        } else if (sosRequest instanceof HttpPost) {
            //Post methods involve deciphering the POST body
            HttpEntity entity = ((HttpPost) sosRequest).getEntity();
            if (entity instanceof StringEntity) {
                String content = IOUtils.toString(((StringEntity) entity).getContent());

                //Assert that we can parse the contents into a DOM document (ie our XML is valid)
                Assert.assertNotNull(DOMUtil.buildDomFromString(content));

                if (paramValue == null) {
                    return content.contains(paramName);
                } else {
                    if (paramName.equals("service") || paramName.equals("version") || paramName.equals("srsName")) {
                        return content.contains(String.format("%1$s=\"%2$s\"", paramName, paramValue));
                    } else {
                        return content.contains(String.format("%1$s>%2$s", paramName, paramValue));
                    }
                }
            }
        }

        throw new IllegalArgumentException(String.format("Unable to deal with type: %1$s", sosRequest.getClass()));
    }

    /**
     * Ensure we are always specifying sos end point and request type
     */
    @Test
    public void testMandatoryParam() {
        String sosUrl = "";
        String request = "";

        SOSMethodMaker sosMM = new SOSMethodMaker();
        //test empty sosUrl parameter
        try {
            sosMM.makePostMethod(sosUrl, request);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals(expected.getMessage(), "serviceUrl parameter can not be null or empty.");
        }
        sosUrl = "http://example.url";
        //test empty request parameter
        try {
            sosMM.makePostMethod(sosUrl, request);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals(expected.getMessage(), "request parameter can not be null or empty.");
        }

    }

    /**
     * Ensure we are always specifying service="SOS"
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testServiceParam() throws ParserConfigurationException, IOException, SAXException {
        final String sosUrl = "http://example.url";
        final String request = "GetObservation";

        SOSMethodMaker sosMM = new SOSMethodMaker();

        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request), "service", "SOS"));
    }

    /**
     * Ensure we are always specifying version="2.0.0"
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testVersionParam() throws ParserConfigurationException, IOException, SAXException {
        final String expectedVersion = "2.0.0";
        final String sosUrl = "http://example.url";
        final String request = "GetObservation";
        final String featureID = "testID";
        final long oneDay = (long) 1000.0 * 60 * 60 * 24;
        final Date today = new Date(System.currentTimeMillis());
        final Date yesterday = new Date(System.currentTimeMillis() - oneDay);
        final String bboxFilter = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":154.1,\"westBoundLongitude\":112.8,\"southBoundLatitude\":-44.0,\"northBoundLatitude\":-8.9}";
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxFilter, OgcServiceProviderType.GeoServer);

        SOSMethodMaker sosMM = new SOSMethodMaker();

        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request), "version", expectedVersion));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, featureID, yesterday, today, bbox),
                "version", expectedVersion));
    }

    @Test
    public void testOptionalParams() throws ParserConfigurationException, IOException, SAXException {
        final String sosUrl = "http://example.url";
        final String request = "GetObservation";
        final String featureID = "testID";
        final long oneDay = (long) 1000.0 * 60 * 60 * 24;
        final Date today = new Date(System.currentTimeMillis());
        final Date yesterday = new Date(System.currentTimeMillis() - oneDay);
        final String bboxFilter = "{\"crs\":\"EPSG:4326\",\"eastBoundLongitude\":154.1,\"westBoundLongitude\":112.8,\"southBoundLatitude\":-44.0,\"northBoundLatitude\":-8.9}";
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxFilter, OgcServiceProviderType.GeoServer);

        SOSMethodMaker sosMM = new SOSMethodMaker();

        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, null),
                "featureOfInterest", null));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, featureID, null, null, null),
                "featureOfInterest", "testID"));

        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, null), "beginPosition",
                null));
        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, null), "endPosition",
                null));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, yesterday, today, null),
                "beginPosition", new DateTime(yesterday).toString()));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, yesterday, today, null),
                "endPosition", new DateTime(today).toString()));

        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, null), "lowerCorner",
                null));
        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, null), "upperCorner",
                null));
        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, null), "srsName", null));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, bbox), "lowerCorner",
                "-8.9 112.8"));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, bbox), "upperCorner",
                "-44.0 154.1"));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null, bbox), "srsName",
                "http://www.opengis.net/def/crs/EPSG/0/4326"));

    }
}
