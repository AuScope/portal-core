package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.PlotScalarResponse;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker.PlotScalarGraphType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for NVCLDataService
 * @author Josh Vote
 *
 */
public class TestNVCLDataService {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};


    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private NVCLDataServiceMethodMaker mockMethodMaker = context.mock(NVCLDataServiceMethodMaker.class);
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
    private HttpClient mockHttpClient = context.mock(HttpClient.class);
    private NVCLDataService dataService = new NVCLDataService(mockServiceCaller, mockMethodMaker);

    /**
     * Tests parsing of a getDatasetCollectionResponse
     * @throws Exception
     */
    @Test
    public void testGetDatasetCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "holeIdentifier";
        final String responseString = org.auscope.portal.Util.loadXML("src/test/resources/NVCL_GetDatasetCollectionResponse.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());


        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getDatasetCollectionMethod(serviceUrl, holeIdentifier);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(responseStream));
        }});

        List<GetDatasetCollectionResponse> response = dataService.getDatasetCollection(serviceUrl, holeIdentifier);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals("6dd70215-fe38-457c-be42-3b165fd98c7", response.get(0).getDatasetId());
        Assert.assertEquals("WTB5", response.get(0).getDatasetName());
        Assert.assertEquals("http://example1/geoserverBH/", response.get(0).getOmUrl());

        Assert.assertEquals("7de74515-ae48-4aac-cd43-3bb45dd78cc", response.get(1).getDatasetId());
        Assert.assertEquals("Name#2", response.get(1).getDatasetName());
        Assert.assertEquals("http://example2/geoserverBH/", response.get(1).getOmUrl());
    }

    /**
     * Tests parsing of a getDatasetCollectionResponse fails when we fail to connect to the service
     * @throws Exception
     */
    @Test(expected=ConnectException.class)
    public void testGetDatasetCollectionConnectError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "holeIdentifier";

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getDatasetCollectionMethod(serviceUrl, holeIdentifier);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(throwException(new ConnectException()));
        }});

        dataService.getDatasetCollection(serviceUrl, holeIdentifier);
    }

    /**
     * Tests parsing of a getLogCollectionResponse
     * @throws Exception
     */
    @Test
    public void testGetLogCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetId = "datasetId";
        final boolean forMosaicService = true;
        final String responseString = org.auscope.portal.Util.loadXML("src/test/resources/NVCL_GetLogCollectionResponse.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());


        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getLogCollectionMethod(serviceUrl, datasetId, forMosaicService);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(responseStream));
        }});

        List<GetLogCollectionResponse> response = dataService.getLogCollection(serviceUrl, datasetId, forMosaicService);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals("logid-1", response.get(0).getLogId());
        Assert.assertEquals("logname-1", response.get(0).getLogName());
        Assert.assertEquals(45, response.get(0).getSampleCount());
        Assert.assertEquals("logid-2", response.get(1).getLogId());
        Assert.assertEquals("logname-2", response.get(1).getLogName());
        Assert.assertEquals(0, response.get(1).getSampleCount());
    }

    /**
     * Tests parsing of a getDatasetCollectionResponse fails when we fail to connect to the service
     * @throws Exception
     */
    @Test(expected=ConnectException.class)
    public void testGetLogCollectionConnectError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetIdentifier = "datasetIdentifier";
        final boolean forMosaicService = false;

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getLogCollectionMethod(serviceUrl, datasetIdentifier, forMosaicService);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(throwException(new ConnectException()));
        }});

        dataService.getLogCollection(serviceUrl, datasetIdentifier, forMosaicService);
    }

    /**
     * Tests parsing of a GetMosaicResponse
     * @throws Exception
     */
    @Test
    public void testGetMosaic() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "logId";
        final Integer width = 10;
        final Integer startSampleNo = 11;
        final Integer endSampleNo = 12;
        final InputStream responseStream = context.mock(InputStream.class);
        final String contentType = "text/html";

        final Header mockHeader = context.mock(Header.class);

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getMosaicMethod(serviceUrl, logId, width, startSampleNo, endSampleNo);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(responseStream));
            oneOf(mockMethod).getResponseHeader("Content-Type");will(returnValue(mockHeader));
            oneOf(mockHeader).getValue();will(returnValue(contentType));
        }});

        MosaicResponse response = dataService.getMosaic(serviceUrl, logId, width, startSampleNo, endSampleNo);
        Assert.assertNotNull(response);
        Assert.assertSame(responseStream, response.getResponse());
        Assert.assertEquals(contentType, response.getContentType());
    }

    /**
     * Tests parsing of a GetMosaicResponse fails when we fail to connect to the service
     * @throws Exception
     */
    @Test(expected=ConnectException.class)
    public void testGetMosaicConnectError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "logId";
        final Integer width = 10;
        final Integer startSampleNo = 11;
        final Integer endSampleNo = 12;

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getMosaicMethod(serviceUrl, logId, width, startSampleNo, endSampleNo);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(throwException(new ConnectException()));

        }});

        dataService.getMosaic(serviceUrl, logId, width, startSampleNo, endSampleNo);
    }

    /**
     * Tests parsing of a PlotScalarResponse
     * @throws Exception
     */
    @Test
    public void testGetPlotScalar() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "logId";
        final Integer width = 10;
        final Integer height = 9;
        final Integer startDepth = 11;
        final Integer endDepth = 12;
        final Double samplingInterval = 1.5;
        final PlotScalarGraphType graphType = PlotScalarGraphType.ScatteredChart;
        final InputStream responseStream = context.mock(InputStream.class);
        final String contentType = "text/html";

        final Header mockHeader = context.mock(Header.class);

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getPlotScalarMethod(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(responseStream));
            oneOf(mockMethod).getResponseHeader("Content-Type");will(returnValue(mockHeader));
            oneOf(mockHeader).getValue();will(returnValue(contentType));
        }});

        PlotScalarResponse response = dataService.getPlotScalar(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);
        Assert.assertNotNull(response);
        Assert.assertSame(responseStream, response.getResponse());
        Assert.assertEquals(contentType, response.getContentType());
    }

    /**
     * Tests parsing of a GetMosaicResponse fails when we fail to connect to the service
     * @throws Exception
     */
    @Test(expected=ConnectException.class)
    public void testGetPlotScalarError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "logId";
        final Integer width = 10;
        final Integer height = 9;
        final Integer startDepth = 11;
        final Integer endDepth = 12;
        final Double samplingInterval = 1.5;
        final PlotScalarGraphType graphType = PlotScalarGraphType.ScatteredChart;

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getPlotScalarMethod(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(throwException(new ConnectException()));
        }});

        dataService.getPlotScalar(serviceUrl, logId, startDepth, endDepth, width, height, samplingInterval, graphType);
    }
}
