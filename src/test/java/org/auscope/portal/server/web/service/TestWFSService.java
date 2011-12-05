package org.auscope.portal.server.web.service;

import java.net.ConnectException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.server.domain.wfs.WFSHTMLResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToHtml;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for WFSService
 * @author Josh Vote
 *
 */
public class TestWFSService {

    /** The context. */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private GmlToKml mockGmlToKml = context.mock(GmlToKml.class);
    private GmlToHtml mockGmlToHtml = context.mock(GmlToHtml.class);
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
    private HttpClient mockClient = context.mock(HttpClient.class);
    private WFSService service;

    @Before
    public void setup() {
        service = new WFSService(mockServiceCaller, mockMethodMaker, mockGmlToKml, mockGmlToHtml);
    }

    /**
     * Tests the 'single feature' request transformation
     */
    @Test
    public void testGetWfsResponseAsKmlSingleFeature() throws Exception {
        final String responseString = "<wfs:response/>"; //we aren't testing the validity of this
        final String responseKml = "<kml:response/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(returnValue(responseString));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, featureId);will(returnValue(mockMethod));

            oneOf(mockGmlToKml).convert(responseString, serviceUrl);will(returnValue(responseKml));

        }});

        WFSKMLResponse response = service.getWfsResponseAsKml(serviceUrl, typeName, featureId);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getGml());
        Assert.assertEquals(responseKml, response.getKml());
    }

    /**
     * Tests the 'single feature' request transformation fails when GML cant be downloaded
     */
    @Test
    public void testGetWfsResponseAsKmlSingleFeatureException() throws Exception {
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";
        final ConnectException exceptionThrown = new ConnectException();

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(throwException(exceptionThrown));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, featureId);will(returnValue(mockMethod));

        }});

        try {
            service.getWfsResponseAsKml(serviceUrl, typeName, featureId);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertSame(exceptionThrown, ex.getCause());
            Assert.assertSame(mockMethod, ex.getRootMethod());
        }
    }

    /**
     * Tests the 'multi feature' request transformation
     */
    @Test
    public void testGetWfsResponseAsKmlMultiFeature() throws Exception {
        final String responseString = "<wfs:response/>"; //we aren't testing the validity of this
        final String responseKml = "<kml:response/>"; //we aren't testing the validity of this
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = "srsName";
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(returnValue(responseString));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName);will(returnValue(mockMethod));

            oneOf(mockGmlToKml).convert(responseString, serviceUrl);will(returnValue(responseKml));

        }});

        WFSKMLResponse response = service.getWfsResponseAsKml(serviceUrl, typeName, filterString, maxFeatures, srsName);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getGml());
        Assert.assertEquals(responseKml, response.getKml());
    }

    /**
     * Tests the 'multi feature' request transformation
     */
    @Test
    public void testGetWfsResponseAsKmlMultiFeatureConnectException() throws Exception {
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = "srsName";
        final ConnectException exceptionThrown = new ConnectException();
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(throwException(exceptionThrown));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName);will(returnValue(mockMethod));
        }});

        try {
            service.getWfsResponseAsKml(serviceUrl, typeName, filterString, maxFeatures, srsName);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertSame(exceptionThrown, ex.getCause());
            Assert.assertSame(mockMethod, ex.getRootMethod());
        }
    }

    /**
     * Tests the 'single feature' request transformation
     */
    @Test
    public void testGetWfsResponseAsHtmlSingleFeature() throws Exception {
        final String responseString = "<wfs:response/>"; //we aren't testing the validity of this
        final String responseHtml = "<html/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(returnValue(responseString));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, featureId);will(returnValue(mockMethod));

            oneOf(mockGmlToHtml).convert(responseString, serviceUrl);will(returnValue(responseHtml));

        }});

        WFSHTMLResponse response = service.getWfsResponseAsHtml(serviceUrl, typeName, featureId);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getGml());
        Assert.assertEquals(responseHtml, response.getHtml());
    }

    /**
     * Tests the 'single feature' request transformation fails gracefully
     */
    @Test
    public void testGetWfsResponseAsHtmlSingleFeatureException() throws Exception {
        final ConnectException exceptionThrown = new ConnectException();
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(throwException(exceptionThrown));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, featureId);will(returnValue(mockMethod));
        }});

        try {
            service.getWfsResponseAsHtml(serviceUrl, typeName, featureId);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertSame(exceptionThrown, ex.getCause());
            Assert.assertSame(mockMethod, ex.getRootMethod());
        }
    }

    /**
     * Tests the 'url' request transformation works as expected
     */
    @Test
    public void testGetWfsResponseAsHtmlUrl() throws Exception {
        final String responseString = "<wfs:response/>"; //we aren't testing the validity of this
        final String responseKml = "<kml:response/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs?request=GetFeature";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(with(any(GetMethod.class)), with(any(HttpClient.class)));will(returnValue(responseString));

            oneOf(mockGmlToHtml).convert(responseString, serviceUrl);will(returnValue(responseKml));

        }});

        WFSHTMLResponse response = service.getWfsResponseAsHtml(serviceUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getGml());
        Assert.assertEquals(responseKml, response.getHtml());
    }

    /**
     * Tests the 'url' request transformation fails as expected
     */
    @Test
    public void testGetWfsResponseAsHtmlUrlConnectException() throws Exception {
        final String responseString = "<wfs:response/>"; //we aren't testing the validity of this
        final String responseKml = "<kml:response/>"; //we aren't testing the validity of this
        final ConnectException exceptionThrown = new ConnectException();
        final String serviceUrl = "http://service/wfs?request=GetFeature";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(with(any(GetMethod.class)), with(any(HttpClient.class)));will(throwException(exceptionThrown));

            oneOf(mockGmlToHtml).convert(responseString, serviceUrl);will(returnValue(responseKml));

        }});

        try {
            service.getWfsResponseAsHtml(serviceUrl);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertSame(exceptionThrown, ex.getCause());
            Assert.assertTrue(ex.getRootMethod() instanceof GetMethod);
        }
    }
}
