package org.auscope.portal.core.services;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WCSMethodMaker;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.test.PortalTestClass;
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
    public void testGetCoverage() throws PortalServiceException, URISyntaxException, IOException {
        final String serviceUrl = "http://example.org/wcs";
        final String coverageName = "coverage";
        final String downloadFormat = "geotiff";
        final Dimension outputSize = new Dimension(50, 40);
        final Resolution outputResolution = null;
        final String outputCrs = "outputcrs";
        final String inputCrs = "inputcrs";
        final CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(1.0, 2.0, 3.0, 4.0);
        final TimeConstraint timeConstraint = new TimeConstraint("constraint");
        final Map<String, String> customParameters = new HashMap<>();

        try (final HttpClientInputStream mockStream = context.mock(HttpClientInputStream.class)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockMethodMaker).getCoverageMethod(serviceUrl, coverageName, downloadFormat, outputCrs,
                            outputSize, outputResolution, inputCrs, bbox, timeConstraint, customParameters);
                    will(returnValue(mockMethod));
                    allowing(mockStream).close();
                    oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                    will(returnValue(mockStream));
                }
            });

            Assert.assertSame(mockStream, service.getCoverage(serviceUrl, coverageName, downloadFormat, outputSize,
                    outputResolution, outputCrs, inputCrs, bbox, timeConstraint, customParameters));
        }
    }

    @Test(expected = PortalServiceException.class)
    public void testGetCoverageException() throws PortalServiceException, URISyntaxException, IOException {
        final String serviceUrl = "http://example.org/wcs";
        final String coverageName = "coverage";
        final String downloadFormat = "geotiff";
        final Dimension outputSize = new Dimension(50, 40);
        final Resolution outputResolution = null;
        final String outputCrs = "outputcrs";
        final String inputCrs = "inputcrs";
        final CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(1.0, 2.0, 3.0, 4.0);
        final TimeConstraint timeConstraint = new TimeConstraint("constraint");
        final Map<String, String> customParameters = new HashMap<>();

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
}
