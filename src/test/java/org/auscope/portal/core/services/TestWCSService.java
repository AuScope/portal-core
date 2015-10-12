package org.auscope.portal.core.services;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WCSMethodMaker;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.DescribeCoverageRecord;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestWCSService extends PortalTestClass {
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private WCSMethodMaker mockMethodMaker = context.mock(WCSMethodMaker.class);

    private WCSService service;

    @Before
    public void init() {
        service = new WCSService(mockServiceCaller, mockMethodMaker);
    }

    @Test
    public void testGetCoverage() throws Exception {
        final String serviceUrl = "http://example.org/wcs";
        final String coverageName = "coverage";
        final String downloadFormat = "geotiff";
        final Dimension outputSize = new Dimension(50, 40);
        final Resolution outputResolution = null;
        final String outputCrs = "outputcrs";
        final String inputCrs = "inputcrs";
        final CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(1.0, 2.0, 3.0, 4.0);
        final TimeConstraint timeConstraint = new TimeConstraint("constraint");
        final Map<String, String> customParameters = new HashMap<String, String>();

        final InputStream mockStream = context.mock(InputStream.class);

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getCoverageMethod(serviceUrl, coverageName, downloadFormat, outputCrs,
                        outputSize, outputResolution, inputCrs, bbox, timeConstraint, customParameters);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(mockStream));
            }
        });

        Assert.assertSame(mockStream, service.getCoverage(serviceUrl, coverageName, downloadFormat, outputSize,
                outputResolution, outputCrs, inputCrs, bbox, timeConstraint, customParameters));
    }

    @Test(expected = PortalServiceException.class)
    public void testGetCoverageException() throws Exception {
        final String serviceUrl = "http://example.org/wcs";
        final String coverageName = "coverage";
        final String downloadFormat = "geotiff";
        final Dimension outputSize = new Dimension(50, 40);
        final Resolution outputResolution = null;
        final String outputCrs = "outputcrs";
        final String inputCrs = "inputcrs";
        final CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(1.0, 2.0, 3.0, 4.0);
        final TimeConstraint timeConstraint = new TimeConstraint("constraint");
        final Map<String, String> customParameters = new HashMap<String, String>();

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getCoverageMethod(serviceUrl, coverageName, downloadFormat, outputCrs,
                        outputSize, outputResolution, inputCrs, bbox, timeConstraint, customParameters);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new IOException()));
            }
        });

        service.getCoverage(serviceUrl, coverageName, downloadFormat, outputSize, outputResolution, outputCrs,
                inputCrs, bbox, timeConstraint, customParameters);
    }

    @Test
    public void testDescribeCoverage() throws Exception {
        final String serviceUrl = "http://example.org/wcs";
        final String coverageName = "coverage";

        final InputStream responseStream = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/wcs/DescribeCoverageResponse1.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).describeCoverageMethod(serviceUrl, coverageName);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(responseStream));
                oneOf(mockMethod).releaseConnection();
            }
        });

        //Just going to do a quick test on the response - tests for parsing the actual data are handled
        //by the DescribeCoverageRecord class
        DescribeCoverageRecord[] recs = service.describeCoverage(serviceUrl, coverageName);
        Assert.assertNotNull(recs);
        Assert.assertEquals(1, recs.length);
    }

    @Test(expected = PortalServiceException.class)
    public void testDescribeCoverageOwsError() throws Exception {
        final String serviceUrl = "http://example.org/wcs";
        final String coverageName = "coverage";

        final InputStream responseStream = getClass().getResourceAsStream("/OWSExceptionSample1.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).describeCoverageMethod(serviceUrl, coverageName);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(responseStream));
                oneOf(mockMethod).releaseConnection();
            }
        });

        service.describeCoverage(serviceUrl, coverageName);
    }

    @Test(expected = PortalServiceException.class)
    public void testDescribeCoverageConnectionError() throws Exception {
        final String serviceUrl = "http://example.org/wcs";
        final String coverageName = "coverage";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).describeCoverageMethod(serviceUrl, coverageName);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new IOException()));
                oneOf(mockMethod).releaseConnection();
            }
        });

        service.describeCoverage(serviceUrl, coverageName);
    }
}
