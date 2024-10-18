package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBaseWFSService extends PortalTestClass {

//    private PortalXSLTTransformer mockTransformer = context.mock(PortalXSLTTransformer.class);
//    private Properties mockProperties = context.mock(Properties.class);
    private HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);
    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);
    private TestableBaseWFSService service;

    public class TestableBaseWFSService extends BaseWFSService {
        public TestableBaseWFSService(HttpServiceCaller httpServiceCaller,
                WFSGetFeatureMethodMaker wfsMethodMaker) {
            super(httpServiceCaller, wfsMethodMaker);
        }
    }

    @Before
    public void init() {
        service = new TestableBaseWFSService(mockHttpServiceCaller, mockMethodMaker);
    }

    @Test
    public void testGenerateFilterRequest() throws URISyntaxException {
        final String wfsUrl = "http://example.org/wfs";
        final String featureType = "my:type";
        String featureId = null;
        final String filterString = "filterString";
        final Integer maxFeatures = 200;
        String srs = null;
        final ResultType resultType = ResultType.Results;

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).makePostMethod(wfsUrl, featureType, filterString, maxFeatures,
                        BaseWFSService.DEFAULT_SRS, resultType, null, null);
                will(returnValue(mockMethod));
            }
        });

        Assert.assertSame(mockMethod,
                service.generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType));
    }

    @Test
    public void testGenerateFilterRequestWithFormatAndIndex() throws URISyntaxException {
        final String wfsUrl = "http://example.org/wfs";
        final String featureType = "my:type";
        String featureId = null;
        final String filterString = "filterString";
        final Integer maxFeatures = 200;
        String srs = null;
        final ResultType resultType = ResultType.Results;
        final String outputFormat = "of";
        final String startIndex = "100";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).makePostMethod(wfsUrl, featureType, filterString, maxFeatures,
                        BaseWFSService.DEFAULT_SRS, resultType, outputFormat, startIndex);
                will(returnValue(mockMethod));
            }
        });

        Assert.assertSame(mockMethod, service.generateWFSRequest(wfsUrl, featureType, featureId, filterString,
                maxFeatures, srs, resultType, outputFormat, startIndex));
    }

    @Test
    public void testGenerateFeatureIdRequest() throws URISyntaxException {
        final String wfsUrl = "http://example.org/wfs";
        final String featureType = "my:type";
        final String featureId = "fid";
        String filterString = null;
        Integer maxFeatures = 200;
        final String srs = "my:srs";
        ResultType resultType = ResultType.Results;

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).makeGetMethod(wfsUrl, featureType, featureId, srs, null);
                will(returnValue(mockMethod));
            }
        });

        Assert.assertSame(mockMethod,
                service.generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType));
    }

    @Test(expected = PortalServiceException.class)
    public void testOwsError() throws PortalServiceException, IOException {
        try (InputStream responseStream = getClass().getResourceAsStream("/OWSExceptionSample1.xml")) {
            context.checking(new Expectations() {
                {
                    oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockMethod);
                    will(returnValue(responseStream));
                }
            });

            service.getWFSResponse(mockMethod);
        }
    }

    @Test(expected = PortalServiceException.class)
    public void testConnectError() throws PortalServiceException, IOException {
        context.checking(new Expectations() {
            {
                oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockMethod);
                will(throwException(new IOException()));
            }
        });

        service.getWFSResponse(mockMethod);
    }

    @Test
    public void testResponse() throws PortalServiceException, IOException {
        try (Scanner sc = new java.util.Scanner(
                ResourceUtil
                        .loadResourceAsStream("org/auscope/portal/core/test/responses/wfs/commodityGetFeatureResponse.xml"))
                                        ) {
            final String responseString = sc.useDelimiter("\\A").next();
            context.checking(new Expectations() {
                {
                    oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockMethod);
                    will(returnValue(responseString));
                }
            });

            WFSResponse response = service.getWFSResponse(mockMethod);
            Assert.assertNotNull(response);
            Assert.assertEquals(responseString, response.getData());
            Assert.assertSame(mockMethod, response.getMethod());
        }
    }
}
