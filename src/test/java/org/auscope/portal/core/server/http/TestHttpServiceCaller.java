package org.auscope.portal.core.server.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: Mathew Wyatt Date: Jun 3, 2009 Time: 12:01:57 PM
 */
public class TestHttpServiceCaller extends PortalTestClass {

    private WFSGetFeatureMethodMaker methodMaker;
    private HttpServiceCaller httpServiceCaller;
    private static final String SERVICE_URL = "http://localhost?";
    private static final String FEATURE_TYPE = "gh:SomeType";
    private static final String FILTER_STRING = "<filter></filter>";

    @Before
    public void setUp() {

        httpServiceCaller = new HttpServiceCaller(9000);
        methodMaker = new WFSGetFeatureMethodMaker();
        methodMaker.setNamespaces(new ErmlNamespaceContext());
    }

    /**
     * Test a normal service successful call
     * @throws IOException 
     */
    @Test
    public void testHttpServiceCallerRequest() throws IOException {
        HttpPost method = (HttpPost) methodMaker.makePostMethod(SERVICE_URL, FEATURE_TYPE, FILTER_STRING, 0);
        String dummyJSONResponse = "<xml>This is a test xml response</xml>";
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        final HttpClient client = context.mock(HttpClient.class);

        context.checking(new Expectations() {
            {
                oneOf(client).execute(with(any(HttpRequestBase.class)));
                will(returnValue(new org.auscope.portal.core.server.http.download.MyHttpResponse(dummyJSONResponseIS)));
            }
        });

        Assert.assertEquals(dummyJSONResponse, httpServiceCaller.getMethodResponseAsString(method, client));

    }

    /**
     * Test failure call that throws error 503
     * @throws IOException 
     */
    @Test(expected = java.net.ConnectException.class)
    public void testHttpServiceCallerRequest503Error() throws IOException {
        HttpPost method = (HttpPost) methodMaker.makePostMethod(SERVICE_URL, FEATURE_TYPE, FILTER_STRING, 0);
        final String dummyJSONResponse = "<xml>This is a test xml response</xml>";
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        final HttpClient client = context.mock(HttpClient.class);

        context.checking(new Expectations() {
            {
                oneOf(client).execute(with(any(HttpRequestBase.class)));
                will(returnValue(new org.auscope.portal.core.server.http.download.MyHttpResponse(dummyJSONResponseIS,
                        503)));
            }
        });

        httpServiceCaller.getMethodResponseAsString(method, client);

    }

    /**
     * If there is no feature type given, we expect there to be an exception thrown
     */
    @Test(expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoFeatureType() {
        methodMaker.makePostMethod(SERVICE_URL, "", FILTER_STRING, 0);
    }

    /**
     * If there is no URL given, expect there an error to be thrown
     */
    @Test(expected = Exception.class)
    public void testConstructWFSGetFeatureMethodNoURL() {
        methodMaker.makePostMethod("", FEATURE_TYPE, FILTER_STRING, 0);
    }

    /**
     * We expect all properties to be set correctly on the method
     * @throws IOException 
     * @throws  
     */
    @Test
    public void testConstructWFSGetFeatureMethodAllParameters() throws IOException {
        HttpPost method = (HttpPost) methodMaker.makePostMethod(SERVICE_URL, FEATURE_TYPE, FILTER_STRING, 0);

//        String expectedPost = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//                +
//                "<wfs:GetFeature version=\"1.1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" maxFeatures=\"200\">\n"
//                +
//                "    <wfs:Query typeName=\"" + FEATURE_TYPE + "\">" +
//                FILTER_STRING +
//                "    </wfs:Query>" +
//                "</wfs:GetFeature>";

        //Assert.assertEquals(expectedPost, thePost.toString());
        String out = IOUtils.toString(method.getEntity().getContent());
        Assert.assertTrue("Bad WFS namespace", out.contains("xmlns:wfs=\"http://www.opengis.net/wfs\""));
        Assert.assertTrue("Bad OGC namespace", out.contains("xmlns:ogc=\"http://www.opengis.net/ogc\""));
        Assert.assertTrue("Bad GML namespace", out.contains("xmlns:gml=\"http://www.opengis.net/gml\""));
        Assert.assertTrue("Bad ER namespace", out.contains("xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\""));
        Assert.assertFalse("Feature count should NOT be included", out.contains("maxFeatures"));
        Assert.assertTrue("typename not specified", out.contains("wfs:Query typeName=\"" + FEATURE_TYPE + "\""));
        Assert.assertTrue("missing FILTER", out.contains(FILTER_STRING));
    }

}
