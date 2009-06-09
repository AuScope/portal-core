package org.auscope.portal.server.web;

import org.junit.Test;
import org.junit.Before;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: Mathew Wyatt
 * Date: Jun 3, 2009
 * Time: 12:01:57 PM
 */
public class TestHttpServiceCaller {
    private Mockery context = new Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private HttpClient mockHttpClient;

    private HttpServiceCaller httpServiceCaller;
    private static final String SERVICE_URL = "http://localhost?";
    private static final String FEATURE_TYPE = "gh:SomeType";
    private static final String FILTER_STRING = "<filter></filter>";

    @Before
    public void setup() {
        mockHttpClient = context.mock(HttpClient.class);
        httpServiceCaller = new HttpServiceCaller(mockHttpClient);
    }

    /**
     * We expect all properties to be set correctly on the method
     * @throws Exception
     */
    @Test
    public void testConstructWFSGetFeatureMethodAllParameters() throws Exception {
        HttpMethodBase method = httpServiceCaller.constructWFSGetFeatureMethod(SERVICE_URL, FEATURE_TYPE, FILTER_STRING);

        // Create a method instance.
        GetMethod method2 = new GetMethod(SERVICE_URL);

        //set all of the parameters
        NameValuePair service = new NameValuePair("service", "WFS");
        NameValuePair version = new NameValuePair("version", "1.1.0");
        NameValuePair request = new NameValuePair("request", "GetFeature");
        NameValuePair typeName = new NameValuePair("typeName", FEATURE_TYPE);
        NameValuePair filter = new NameValuePair("filter", FILTER_STRING);
        NameValuePair maxFeatures = new NameValuePair("maxFeatures", "10");

        //attach them to the method
        method2.setQueryString(new NameValuePair[]{service, version, request, typeName, filter, maxFeatures});


        Assert.assertEquals(method2.getURI(), method.getURI());
    }

    /**
     * If there is no feature type given, we expect there to be an exception thrown
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoFeatureType() throws Exception {
        httpServiceCaller.constructWFSGetFeatureMethod(SERVICE_URL, "", FILTER_STRING);
    }

    /**
     * If there is no URL given, expect there an error to be thrown
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoURL() throws Exception {
        httpServiceCaller.constructWFSGetFeatureMethod("", FEATURE_TYPE, FILTER_STRING);
    }

    /**
     * Test that the service is being called, and the reponse being returned
     * @throws Exception
     */
    @Test
    public void testCallGetMethod() throws Exception {
        final GetMethod method = context.mock(GetMethod.class);
        final String returnString = "Allo";

        context.checking(new Expectations() {{
            oneOf (mockHttpClient).executeMethod(method); will(returnValue(HttpStatus.SC_OK));
            oneOf (method).getResponseBody(); will(returnValue(returnString.getBytes()));
        }});

        String response = httpServiceCaller.callMethod(method);

        Assert.assertEquals(returnString, response);
    }

    /**
     * Test that an exception is thrown if an error occurs
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testCallGetMethodError() throws Exception {
        final GetMethod method = context.mock(GetMethod.class);

        context.checking(new Expectations() {{
            oneOf (mockHttpClient).executeMethod(method); will(returnValue(HttpStatus.SC_EXPECTATION_FAILED));
            oneOf (method).getStatusLine();
        }});

        httpServiceCaller.callMethod(method);
    }
}
