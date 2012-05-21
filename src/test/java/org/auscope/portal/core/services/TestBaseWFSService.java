package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSTransformedResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.xslt.PortalXSLTTransformer;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBaseWFSService extends PortalTestClass {

    private PortalXSLTTransformer mockTransformer = context.mock(PortalXSLTTransformer.class);
    private Properties mockProperties = context.mock(Properties.class);
    private HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
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
    public void testGenerateFilterRequest() throws Exception {
        final String wfsUrl = "http://example.org/wfs";
        final String featureType ="my:type";
        final String featureId = null;
        final String filterString = "filterString";
        final Integer maxFeatures = 200;
        final String srs = null;
        final ResultType resultType = ResultType.Results;

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeMethod(wfsUrl, featureType, filterString, maxFeatures, BaseWFSService.DEFAULT_SRS, resultType);will(returnValue(mockMethod));
        }});

        Assert.assertSame(mockMethod, service.generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType));
    }

    @Test
    public void testGenerateFeatureIdRequest() throws Exception {
        final String wfsUrl = "http://example.org/wfs";
        final String featureType ="my:type";
        final String featureId = "fid";
        final String filterString = null;
        final Integer maxFeatures = 200;
        final String srs = "my:srs";
        final ResultType resultType = ResultType.Results;

        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeMethod(wfsUrl, featureType, featureId);will(returnValue(mockMethod));
        }});

        Assert.assertSame(mockMethod, service.generateWFSRequest(wfsUrl, featureType, featureId, filterString, maxFeatures, srs, resultType));
    }

    @Test
    public void testGetFeatureCount() throws Exception {
        final InputStream responseStream = ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/wfs/GetWFSFeatureCount.xml");

        context.checking(new Expectations() {{
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockMethod);will(returnValue(responseStream));
            oneOf(mockMethod).releaseConnection();
        }});

        WFSCountResponse response = service.getWfsFeatureCount(mockMethod);
        Assert.assertNotNull(response);
        Assert.assertEquals(161, response.getNumberOfFeatures());
    }

    @Test(expected=PortalServiceException.class)
    public void testGetFeatureCountError() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockMethod);will(throwException(new IOException()));
            oneOf(mockMethod).releaseConnection();
        }});

        service.getWfsFeatureCount(mockMethod);
    }

    @Test(expected=PortalServiceException.class)
    public void testGetFeatureCountOWSError() throws Exception {
        final InputStream responseStream = getClass().getResourceAsStream("/OWSExceptionSample1.xml");

        context.checking(new Expectations() {{
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockMethod);will(returnValue(responseStream));
            oneOf(mockMethod).releaseConnection();
        }});

        service.getWfsFeatureCount(mockMethod);
    }

    @Test(expected=PortalServiceException.class)
    public void testTransformOwsError() throws Exception {
        final InputStream responseStream = getClass().getResourceAsStream("/OWSExceptionSample1.xml");

        context.checking(new Expectations() {{
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseStream));
        }});

        service.getTransformedWFSResponse(mockMethod, mockTransformer, mockProperties);
    }

    @Test(expected=PortalServiceException.class)
    public void testTransformConnectError() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockMethod);will(throwException(new IOException()));
        }});

        service.getTransformedWFSResponse(mockMethod, mockTransformer, mockProperties);
    }

    @Test
    public void testTransform() throws Exception {
        final String responseString = new java.util.Scanner(ResourceUtil.loadResourceAsStream("org/auscope/portal/core/test/responses/wfs/commodityGetFeatureResponse.xml")).useDelimiter("\\A").next();

        final String convertedString = "transformed-string";

        context.checking(new Expectations() {{
            oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString));

            oneOf(mockTransformer).convert(with(any(String.class)), with(same(mockProperties)));will(returnValue(convertedString));
        }});

        WFSTransformedResponse response = service.getTransformedWFSResponse(mockMethod, mockTransformer, mockProperties);
        Assert.assertNotNull(response);
        Assert.assertEquals(responseString, response.getGml());
        Assert.assertEquals(convertedString, response.getTransformed());
        Assert.assertSame(mockMethod, response.getMethod());
    }



}
