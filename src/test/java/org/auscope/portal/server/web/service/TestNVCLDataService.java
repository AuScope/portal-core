package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.web.NVCLDataServiceMethodMaker;
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
        final String responseString = org.auscope.portal.Util.loadXML("src/test/resources/NVCL_GetLogCollectionResponse.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());


        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getLogCollectionMethod(serviceUrl, datasetId);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(returnValue(responseStream));
        }});

        List<GetLogCollectionResponse> response = dataService.getLogCollection(serviceUrl, datasetId);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals("logid-1", response.get(0).getLogId());
        Assert.assertEquals("logname-1", response.get(0).getLogName());
        Assert.assertEquals("logid-2", response.get(1).getLogId());
        Assert.assertEquals("logname-2", response.get(1).getLogName());
    }

    /**
     * Tests parsing of a getDatasetCollectionResponse fails when we fail to connect to the service
     * @throws Exception
     */
    @Test(expected=ConnectException.class)
    public void testGetLogCollectionConnectError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetIdentifier = "datasetIdentifier";

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            oneOf(mockMethodMaker).getLogCollectionMethod(serviceUrl, datasetIdentifier);will(returnValue(mockMethod));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod, mockHttpClient);will(throwException(new ConnectException()));
        }});

        dataService.getLogCollection(serviceUrl, datasetIdentifier);
    }
}
