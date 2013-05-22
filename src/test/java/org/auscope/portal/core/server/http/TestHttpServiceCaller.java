package org.auscope.portal.core.server.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import junit.framework.Assert;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Mathew Wyatt
 * Date: Jun 3, 2009
 * Time: 12:01:57 PM
 */
public class TestHttpServiceCaller extends PortalTestClass {
    private HttpClient mockHttpClient;
    private WFSGetFeatureMethodMaker methodMaker;
    private HttpServiceCaller httpServiceCaller;
    private static final String SERVICE_URL = "http://localhost?";
    private static final String FEATURE_TYPE = "gh:SomeType";
    private static final String FILTER_STRING = "<filter></filter>";

    @Before
    public void setUp() {
        mockHttpClient = context.mock(HttpClient.class);
        httpServiceCaller = new HttpServiceCaller(90000);
        methodMaker = new WFSGetFeatureMethodMaker();
        methodMaker.setNamespaces(new ErmlNamespaceContext());
    }

    /**
     *
     * Due to the complete API change, this test class has to be rewritten.
     */

    @Test
    public void testABC(){
        //do nothing for now
    }

    /**
     * We expect all properties to be set correctly on the method
     * @throws Exception
     */
//    @Test
//    public void testConstructWFSGetFeatureMethodAllParameters() throws Exception {
//        HttpPost method = (HttpPost)methodMaker.makePostMethod(SERVICE_URL, FEATURE_TYPE, FILTER_STRING, 0);
//
//        String expectedPost = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                              "<wfs:GetFeature version=\"1.1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" maxFeatures=\"200\">\n" +
//                              "    <wfs:Query typeName=\""+FEATURE_TYPE+"\">" +
//                                FILTER_STRING +
//                              "    </wfs:Query>" +
//                              "</wfs:GetFeature>";
//
//
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpResponse response = httpclient.execute(method);
//
//        //Assert.assertEquals(expectedPost, thePost.toString());
//        String out = IOUtils.toString(response.getEntity().getContent());
//        Assert.assertTrue("Bad WFS namespace", out.contains("xmlns:wfs=\"http://www.opengis.net/wfs\""));
//        Assert.assertTrue("Bad OGC namespace", out.contains("xmlns:ogc=\"http://www.opengis.net/ogc\""));
//        Assert.assertTrue("Bad GML namespace", out.contains("xmlns:gml=\"http://www.opengis.net/gml\""));
//        Assert.assertTrue("Bad ER namespace", out.contains("xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\""));
//        Assert.assertFalse("Feature count should NOT be included", out.contains("maxFeatures"));
//        Assert.assertTrue("typename not specified", out.contains("wfs:Query typeName=\"" + FEATURE_TYPE + "\""));
//        Assert.assertTrue("missing FILTER", out.contains(FILTER_STRING));
//    }



}
