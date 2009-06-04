package org.auscope.portal.server.web;

import org.junit.Test;
import org.junit.Before;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.internal.ExpectationBuilder;
import org.jmock.internal.ExpectationCollector;
import org.jmock.lib.legacy.ClassImposteriser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

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
    private static final String FILTER = "<filter></filter>";

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
        //URL url = httpServiceCaller.constructWFSGetFeatureUrl(SERVICE_URL, FEATURE_TYPE, FILTER);
        GetMethod method = httpServiceCaller.constructWFSGetFeatureMethod(SERVICE_URL, FEATURE_TYPE, FILTER);
        Assert.assertEquals("WFS", method.getParams().getParameter("service"));
        Assert.assertEquals("1.1.0", method.getParams().getParameter("version"));
        Assert.assertEquals("GetFeature", method.getParams().getParameter("request"));
        Assert.assertEquals(FEATURE_TYPE, method.getParams().getParameter("typeName"));
        Assert.assertEquals(FILTER, method.getParams().getParameter("filter"));
    }

    /**
     * If there is no feature type given, we expect there to be an exception thrown
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoFeatureType() throws Exception {
        httpServiceCaller.constructWFSGetFeatureMethod(SERVICE_URL, "", FILTER);
    }

    /**
     * If there is no URL given, expect there an error to be thrown
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoURL() throws Exception {
        httpServiceCaller.constructWFSGetFeatureMethod("", FEATURE_TYPE, FILTER);
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

        String response = httpServiceCaller.callGetMethod(method);

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

        httpServiceCaller.callGetMethod(method);
    }
}
