package org.auscope.portal.server.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

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
    private HttpConnectionManagerParams mockParams;
    private IWFSGetFeatureMethodMaker methodMaker;

    private HttpServiceCaller httpServiceCaller;
    private static final String SERVICE_URL = "http://localhost?";
    private static final String FEATURE_TYPE = "gh:SomeType";
    private static final String FILTER_STRING = "<filter></filter>";

    @Before
    public void setup() {
        mockHttpClient = context.mock(HttpClient.class);
        mockParams = context.mock(HttpConnectionManagerParams.class);
        httpServiceCaller = new HttpServiceCaller();
        methodMaker = new WFSGetFeatureMethodMakerPOST();
        httpServiceCaller.setClientParams(mockParams);
    }

    /**
     * We expect all properties to be set correctly on the method
     * @throws Exception
     */
    @Test
    public void testConstructWFSGetFeatureMethodAllParameters() throws Exception {
        PostMethod method = (PostMethod)methodMaker.makeMethod(SERVICE_URL, FEATURE_TYPE, FILTER_STRING, 0);

        String expectedPost = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                              "<wfs:GetFeature version=\"1.1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" maxFeatures=\"200\">\n" +
                              "    <wfs:Query typeName=\""+FEATURE_TYPE+"\">" +
                                FILTER_STRING +
                              "    </wfs:Query>" +
                              "</wfs:GetFeature>";

        ByteArrayOutputStream thePost = new ByteArrayOutputStream();
        method.getRequestEntity().writeRequest(new BufferedOutputStream(thePost));

        //Assert.assertEquals(expectedPost, thePost.toString());
        String out = thePost.toString();
        Assert.assertTrue("Bad WFS namespace", out.contains("xmlns:wfs=\"http://www.opengis.net/wfs\""));
        Assert.assertTrue("Bad OGC namespace", out.contains("xmlns:ogc=\"http://www.opengis.net/ogc\""));
        Assert.assertTrue("Bad GML namespace", out.contains("xmlns:gml=\"http://www.opengis.net/gml\""));
        Assert.assertTrue("Bad ER namespace", out.contains("xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\""));
        Assert.assertFalse("Feature count should NOT be included", out.contains("maxFeatures"));
        Assert.assertTrue("typename not specified", out.contains("wfs:Query typeName=\"" + FEATURE_TYPE + "\""));
        Assert.assertTrue("missing FILTER", out.contains(FILTER_STRING));
    }

    /**
     * If there is no feature type given, we expect there to be an exception thrown
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoFeatureType() throws Exception {
        methodMaker.makeMethod(SERVICE_URL, "", FILTER_STRING, 0);
    }

    /**
     * If there is no URL given, expect there an error to be thrown
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoURL() throws Exception {
        methodMaker.makeMethod("", FEATURE_TYPE, FILTER_STRING, 0);
    }

    /**
     * Test that the service is being called, and the reponse being returned
     * @throws Exception
     */
    //@Test
    public void testCallGetMethod() throws Exception {
        final HttpMethodBase method = context.mock(HttpMethodBase.class);
        final String returnString = "Allo";
        final BufferedInputStream bis = new BufferedInputStream(method.getResponseBodyAsStream());


        context.checking(new Expectations() {{
            oneOf (mockHttpClient).setHttpConnectionManager(with(any(HttpConnectionManager.class)));
            oneOf (mockHttpClient).executeMethod(method); will(returnValue(HttpStatus.SC_OK));
            oneOf (method).getResponseBodyAsStream(); will(returnValue(InputStream.class));
            oneOf (method).releaseConnection();
            allowing(method).getURI();will(returnValue(null));
        }});

        String response = httpServiceCaller.getMethodResponseAsString(method, mockHttpClient);

        Assert.assertEquals(returnString, response);
    }

    /**
     * Test that an exception is thrown if an error occurs
     * @throws Exception
     */
    @Test (expected = Exception.class)
    public void testCallMethodError() throws Exception {
        final HttpMethodBase method = context.mock(HttpMethodBase.class);

        context.checking(new Expectations() {{
            oneOf (mockHttpClient).setHttpConnectionManager(with(any(HttpConnectionManager.class)));
            oneOf (mockHttpClient).executeMethod(method); will(returnValue(HttpStatus.SC_EXPECTATION_FAILED));
            oneOf (method).getStatusLine();//logger
            oneOf (method).getStatusLine();//exception
            allowing(method).getURI();will(returnValue(null));

        }});

        httpServiceCaller.getMethodResponseAsString(method, mockHttpClient);
    }

}
