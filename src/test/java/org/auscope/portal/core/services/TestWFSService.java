package org.auscope.portal.core.services;

import java.io.InputStream;
import java.net.ConnectException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.services.responses.wfs.WFSTransformedResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.core.xslt.GmlToHtml;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for WFSService
 *
 * @author Josh Vote
 *
 */
public class TestWFSService extends PortalTestClass {
    private GmlToHtml mockGmlToHtml = context.mock(GmlToHtml.class);
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);
    private WFSService service;

    @Before
    public void setup() {
        service = new WFSService(mockServiceCaller, mockMethodMaker, mockGmlToHtml);
    }

    /**
     * Tests the 'single feature' request transformation
     */
    @Test
    public void testGetWfsResponseAsKmlSingleFeature() throws Exception {
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(responseString));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, typeName, featureId, BaseWFSService.DEFAULT_SRS, null);
                will(returnValue(mockMethod));

            }
        });

        WFSResponse response = service.getWfsResponse(serviceUrl, typeName, featureId);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getData());
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

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(throwException(exceptionThrown));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, typeName, featureId, BaseWFSService.DEFAULT_SRS, null);
                will(returnValue(mockMethod));

            }
        });

        try {
            service.getWfsResponse(serviceUrl, typeName, featureId);
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
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(responseString));
                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, typeName, featureId, BaseWFSService.DEFAULT_SRS, null);
                will(returnValue(mockMethod));
            }
        });

        try {
            service.getWfsResponse(serviceUrl, typeName, featureId);
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
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String srsName = "srsName";
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(responseString));

                oneOf(mockMethodMaker).makePostMethod(serviceUrl, typeName, filterString, maxFeatures, srsName,
                        ResultType.Results, null, null);
                will(returnValue(mockMethod));

            }
        });

        WFSResponse response = service.getWfsResponse(serviceUrl, typeName, filterString, maxFeatures, srsName);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getData());
    }

    /**
     * Tests the 'multi feature' request transformation uses the correct default srs if none is specified
     */
    @Test
    public void testGetWfsResponseAsKmlDefaultSRS() throws Exception {
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");
        final String filterString = "<ogc:filter/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {
            {
                allowing(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(responseString));

                allowing(mockMethodMaker).makePostMethod(serviceUrl, typeName, filterString, maxFeatures,
                        BaseWFSService.DEFAULT_SRS, ResultType.Results, null, null);
                will(returnValue(mockMethod));
            }
        });

        //Our test is ensuring that only the DEFAULT_SRS is passed to the mock method maker
        service.getWfsResponse(serviceUrl, typeName, filterString, maxFeatures, null);
        service.getWfsResponse(serviceUrl, typeName, filterString, maxFeatures, "");

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

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(throwException(exceptionThrown));

                oneOf(mockMethodMaker).makePostMethod(serviceUrl, typeName, filterString, maxFeatures, srsName,
                        ResultType.Results, null, null);
                will(returnValue(mockMethod));
            }
        });

        try {
            service.getWfsResponse(serviceUrl, typeName, filterString, maxFeatures, srsName);
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
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");
        final int maxFeatures = 12321;
        final String typeName = "type:Name";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(responseString));

                oneOf(mockMethodMaker).makePostMethod(serviceUrl, typeName, filterString, maxFeatures, srsName,
                        ResultType.Results, null, null);
                will(returnValue(mockMethod));
            }
        });

        try {
            service.getWfsResponse(serviceUrl, typeName, filterString, maxFeatures, srsName);
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
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");
        final String responseHtml = "<html/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";
        final String baseUrl = "https://portal.org/api";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(responseString));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, typeName, featureId, BaseWFSService.DEFAULT_SRS, null);
                will(returnValue(mockMethod));

                oneOf(mockGmlToHtml).convert(with(any(String.class)), with(any(ErmlNamespaceContext.class)), with(any(String.class)));
                will(returnValue(responseHtml));

            }
        });

        WFSTransformedResponse response = service.getWfsResponseAsHtml(serviceUrl, typeName, featureId, baseUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getGml());
        Assert.assertEquals(responseHtml, response.getTransformed());
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
        final String baseUrl = "https://portal.org/api";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(throwException(exceptionThrown));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, typeName, featureId, BaseWFSService.DEFAULT_SRS, null);
                will(returnValue(mockMethod));
            }
        });

        try {
            service.getWfsResponseAsHtml(serviceUrl, typeName, featureId, baseUrl);
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
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";
        final String typeName = "type:Name";
        final String baseUrl = "https://portal.org/api";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
                will(returnValue(responseString));
                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, typeName, featureId, BaseWFSService.DEFAULT_SRS, null);
                will(returnValue(mockMethod));
            }
        });

        try {
            service.getWfsResponseAsHtml(serviceUrl, typeName, featureId, baseUrl);
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
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");
        final String responseKml = "<kml:response/>"; //we aren't testing the validity of this
        final String serviceUrl = "http://service/wfs?request=GetFeature";
        final String baseUrl = "https://portal.org/api";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(with(any(HttpGet.class)));
                will(returnValue(responseString));

                oneOf(mockGmlToHtml).convert(with(any(String.class)), with(any(ErmlNamespaceContext.class)), with(any(String.class)));
                will(returnValue(responseKml));

            }
        });

        WFSTransformedResponse response = service.getWfsResponseAsHtml(serviceUrl, baseUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getGml());
        Assert.assertEquals(responseKml, response.getTransformed());
    }

    /**
     * Tests the 'url' request transformation fails as expected
     */
    @Test
    public void testGetWfsResponseAsHtmlUrlConnectException() throws Exception {
        final ConnectException exceptionThrown = new ConnectException();
        final String serviceUrl = "http://service/wfs?request=GetFeature";
        final String baseUrl = "https://portal.org/api";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(with(any(HttpGet.class)));
                will(throwException(exceptionThrown));
            }
        });

        try {
            service.getWfsResponseAsHtml(serviceUrl, baseUrl);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertSame(exceptionThrown, ex.getCause());
            Assert.assertTrue(ex.getRootMethod() instanceof HttpGet);
        }
    }

    /**
     * Tests the 'single feature' request transformation fails if an OWS exception response is returned
     */
    @Test
    public void testGetWfsResponseAsHtmlUrlOWSException() throws Exception {
        final String serviceUrl = "http://service/wfs";
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");
        final String baseUrl = "https://portal.org/api";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsString(with(any(HttpGet.class)));
                will(returnValue(responseString));
            }
        });

        try {
            service.getWfsResponseAsHtml(serviceUrl, baseUrl);
            Assert.fail("Exception should have been thrown");
        } catch (PortalServiceException ex) {
            Assert.assertTrue(ex.getCause() instanceof OWSException);
            Assert.assertNotNull(ex.getRootMethod());
        }
    }
}
