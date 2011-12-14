package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ConnectException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.Util;
import org.auscope.portal.server.domain.ows.OWSException;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.domain.wfs.WFSHTMLResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToHtml;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker.ResultType;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for WFSService
 * @author Josh Vote
 *
 */
public class TestWFSService extends PortalTestClass {
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
        final String responseString = Util.loadXML("src/test/resources/EmptyWFSResponse.xml");
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
     * Tests the 'single feature' request transformation fails if an OWS exception response is returned
     */
    @Test
    public void testGetWfsResponseAsKmlOWSException() throws Exception {
        final String responseString = Util.loadXML("src/test/resources/OWSExceptionSample1.xml");
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(returnValue(responseString));
            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, featureId);will(returnValue(mockMethod));
        }});

        try {
            service.getWfsResponseAsKml(serviceUrl, typeName, featureId);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertTrue(ex.getCause() instanceof OWSException);
            Assert.assertSame(mockMethod, ex.getRootMethod());
        }
    }

    /**
     * Tests the 'multi feature' request transformation
     */
    @Test
    public void testGetWfsResponseAsKmlMultiFeature() throws Exception {
        final String responseString = Util.loadXML("src/test/resources/EmptyWFSResponse.xml");
        final String responseKml = "<kml:response/>"; //we aren't testing the validity of this
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = "srsName";
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(returnValue(responseString));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Results);will(returnValue(mockMethod));

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

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Results);will(returnValue(mockMethod));
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
     * Tests the 'multi feature' request transformation fails if an OWS exception response is returned
     */
    @Test
    public void testGetWfsMultiFeatureResponseAsKmlOWSException() throws Exception {

        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = "srsName";
        final String responseString = Util.loadXML("src/test/resources/OWSExceptionSample1.xml");
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(returnValue(responseString));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Results);will(returnValue(mockMethod));
        }});

        try {
            service.getWfsResponseAsKml(serviceUrl, typeName, filterString, maxFeatures, srsName);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertTrue(ex.getCause() instanceof OWSException);
            Assert.assertSame(mockMethod, ex.getRootMethod());
        }
    }

    /**
     * Tests the 'single feature' request transformation
     */
    @Test
    public void testGetWfsResponseAsHtmlSingleFeature() throws Exception {
        final String responseString = Util.loadXML("src/test/resources/EmptyWFSResponse.xml");
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
     * Tests the 'single feature' request transformation fails if an OWS exception response is returned
     */
    @Test
    public void testGetWfsResponseAsHtmlOWSException() throws Exception {
        final String responseString = Util.loadXML("src/test/resources/OWSExceptionSample1.xml");
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, mockClient);will(returnValue(responseString));
            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, featureId);will(returnValue(mockMethod));
        }});

        try {
            service.getWfsResponseAsHtml(serviceUrl, typeName, featureId);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertTrue(ex.getCause() instanceof OWSException);
            Assert.assertSame(mockMethod, ex.getRootMethod());
        }
    }

    /**
     * Tests the 'url' request transformation works as expected
     */
    @Test
    public void testGetWfsResponseAsHtmlUrl() throws Exception {
        final String responseString = Util.loadXML("src/test/resources/EmptyWFSResponse.xml");
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
        final ConnectException exceptionThrown = new ConnectException();
        final String serviceUrl = "http://service/wfs?request=GetFeature";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(with(any(GetMethod.class)), with(any(HttpClient.class)));will(throwException(exceptionThrown));
        }});

        try {
            service.getWfsResponseAsHtml(serviceUrl);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertSame(exceptionThrown, ex.getCause());
            Assert.assertTrue(ex.getRootMethod() instanceof GetMethod);
        }
    }

    /**
     * Tests the 'single feature' request transformation fails if an OWS exception response is returned
     */
    @Test
    public void testGetWfsResponseAsHtmlUrlOWSException() throws Exception {
        final String serviceUrl = "http://service/wfs";
        final String responseString = Util.loadXML("src/test/resources/OWSExceptionSample1.xml");

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsString(with(any(GetMethod.class)), with(any(HttpClient.class)));will(returnValue(responseString));
        }});


        try {
            service.getWfsResponseAsHtml(serviceUrl);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertTrue(ex.getCause() instanceof OWSException);
            Assert.assertNotNull(ex.getRootMethod());
        }
    }

    /**
     * Tests the count request works as expected
     */
    @Test
    public void testGetWfsCount() throws Exception {
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = null;
        final int maxFeatures = 12321;
        final String typeName = "type:Name";
        final InputStream responseStream = new ByteArrayInputStream(Util.loadXML("src/test/resources/GetWFSFeatureCount.xml").getBytes());
        final int expectedCount = 161;

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockClient);will(returnValue(responseStream));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Hits);will(returnValue(mockMethod));

        }});

        WFSCountResponse response = service.getWfsFeatureCount(serviceUrl, typeName, filterString, maxFeatures);
        Assert.assertNotNull(response);
        Assert.assertEquals(expectedCount, response.getNumberOfFeatures());
    }

    /**
     * Tests the count request fails as expected
     */
    @Test
    public void testGetWfsCountError() throws Exception {
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = null;
        final ConnectException exceptionThrown = new ConnectException();
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockClient);will(throwException(exceptionThrown));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Hits);will(returnValue(mockMethod));

        }});

        try {
            service.getWfsFeatureCount(serviceUrl, typeName, filterString, maxFeatures);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertSame(exceptionThrown, ex.getCause());
            Assert.assertSame(mockMethod, ex.getRootMethod());
        }
    }

    /**
     * Tests the count request fails as expected
     */
    @Test
    public void testGetWfsCountOWSError() throws Exception {
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = null;
        final int maxFeatures = 12321;
        final String typeName = "type:Name";
        final InputStream responseStream = getClass().getResourceAsStream("/OWSExceptionSample1.xml");

        context.checking(new Expectations() {{
            oneOf(mockServiceCaller).getHttpClient();will(returnValue(mockClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockClient);will(returnValue(responseStream));

            oneOf(mockMethodMaker).makeMethod(serviceUrl, typeName, filterString, maxFeatures, srsName, ResultType.Hits);will(returnValue(mockMethod));
        }});

        try {
            service.getWfsFeatureCount(serviceUrl, typeName, filterString, maxFeatures);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertTrue(ex.getCause() instanceof OWSException);
            Assert.assertNotNull(ex.getRootMethod());
        }
    }
}
