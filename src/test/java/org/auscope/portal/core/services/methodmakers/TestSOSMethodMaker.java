package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Unit tests for SOSMethodMaker
 * @author Josh Vote
 *
 */
public class TestSOSMethodMaker extends PortalTestClass {

    /**
     * Tests that the given HttpMethodBase (representing a SOS request) contains
     * a parameter of a specific value
     *
     * Throws an exception if this function cannot decipher the type of HTTP method
     * @param paramName the parameter to check for
     * @param paramValue [Optional] if specified, the value of paramName (if not specified the existence of paramName will be tested)
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    private boolean testSOSParam(HttpMethodBase sosRequest, String paramName, String paramValue) throws ParserConfigurationException, IOException, SAXException {
        //Get methods involve finding the request parameter "version=X"
        if (sosRequest instanceof GetMethod) {
            String uriString = sosRequest.getURI().toString();

            if (paramValue == null) {
                return uriString.contains(paramName);
            } else {
                return uriString.contains(String.format("%1$s=%2$s",paramName, paramValue));
            }

        } else if (sosRequest instanceof PostMethod) {
            //Post methods involve deciphering the POST body
            RequestEntity entity = ((PostMethod) sosRequest).getRequestEntity();
            if (entity instanceof StringRequestEntity) {
                String content = ((StringRequestEntity) entity).getContent();

                //Assert that we can parse the contents into a DOM document (ie our XML is valid)
                Assert.assertNotNull(DOMUtil.buildDomFromString(content));

                if (paramValue == null) {
                    return content.contains(paramName);
                } else {
                	if (paramName.equals("service") || paramName.equals("version") || paramName.equals("srsName") ) {  
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
     * @throws Exception
     */
    @Test
    public void testMandatoryParam() throws Exception {
    	String sosUrl = "";
        String request = "";

        SOSMethodMaker sosMM = new SOSMethodMaker();
        //test empty sosUrl parameter
        try {
            sosMM.makePostMethod(sosUrl, request);
        } catch (IllegalArgumentException expected) {
        	Assert.assertEquals(expected.getMessage(), "serviceURL parameter can not be null or empty.");
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
     * @throws Exception
     */
    @Test
    public void testServiceParam() throws Exception {
        final String sosUrl = "http://example.url";
        final String request = "GetObservation";

        SOSMethodMaker sosMM = new SOSMethodMaker();

        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request),"service", "SOS"));
    }
    
    /**
     * Ensure we are always specifying version="2.0.0"
     * @throws Exception
     */
    @Test
    public void testVersionParam() throws Exception {
    	final String expectedVersion = "2.0.0";
        final String sosUrl = "http://example.url";
        final String request = "GetObservation";
        final String featureID = "testID";
        final String temporalFilter = "2010-01-31T00:00:00+08/2010-02-21T00:00:00+08";
        final String bboxFilter = "-8.9,-44.0,112.8,154.1,http://www.opengis.net/def/crs/EPSG/0/4326";

        SOSMethodMaker sosMM = new SOSMethodMaker();

        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request),"version", expectedVersion));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, featureID, temporalFilter, bboxFilter),"version", expectedVersion));
    }
    

    

    @Test
    public void testOptionalParams() throws Exception {
        final String sosUrl = "http://example.url";
        final String request = "GetObservation";
        final String featureID = "testID";
        final String temporalFilter = "2010-01-31T00:00:00+08/2010-02-21T00:00:00+08";
        final String bboxFilter = "-8.9,-44.0,112.8,154.1,http://www.opengis.net/def/crs/EPSG/0/4326";
        
        SOSMethodMaker sosMM = new SOSMethodMaker();

        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null),"featureOfInterest", null));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, featureID, null, null),"featureOfInterest", "testID"));

        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null),"beginPosition", null));
        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null),"endPosition", null));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, temporalFilter, null),"beginPosition", "2010-01-31T00:00:00+08"));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, temporalFilter, null),"endPosition", "2010-02-21T00:00:00+08"));

        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null),"lowerCorner", null));
        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null),"upperCorner", null));
        Assert.assertFalse(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, null),"srsName", null));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, bboxFilter),"lowerCorner", "-8.9 112.8"));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, bboxFilter),"upperCorner", "-44.0 154.1"));
        Assert.assertTrue(testSOSParam(sosMM.makePostMethod(sosUrl, request, null, null, bboxFilter),"srsName", "http://www.opengis.net/def/crs/EPSG/0/4326"));

    }
}
