package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.auscope.portal.HttpMethodBaseMatcher;
import org.auscope.portal.HttpMethodBaseMatcher.HttpMethodType;
import org.auscope.portal.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.csw.CSWGetRecordResponse;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the CSWFilterService
 * @author Josh Vote
 *
 */
public class TestCSWFilterService {
    //determines the size of the test + congestion
    static final int CONCURRENT_THREADS_TO_RUN = 3;

    //These determine the correct numbers for a single read of the test file
    static final int RECORD_COUNT_TOTAL = 15;
    static final int RECORD_MATCH_TOTAL = 30;

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};


    private CSWFilterService cswFilterService;
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
    private CSWThreadExecutor threadExecutor;
    private CSWGetDataRecordsFilter mockFilter = context.mock(CSWGetDataRecordsFilter.class);
    private HttpClient mockHttpClient = context.mock(HttpClient.class);
    private ArrayList<CSWServiceItem> serviceUrlList;

    private static final String idFormatString = "id:%1$s";
    private static final String serviceUrlFormatString = "http://cswservice.%1$s.url/";

    /**
     * Initialises each of our unit tests with a new CSWFilterService
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {

        this.threadExecutor = new CSWThreadExecutor();

        //Create our service list
        serviceUrlList = new ArrayList<CSWServiceItem>(CONCURRENT_THREADS_TO_RUN);
        for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++){
            serviceUrlList.add(new CSWServiceItem(String.format(idFormatString, i), String.format(serviceUrlFormatString, i)));
        }

        this.cswFilterService = new CSWFilterService(threadExecutor, httpServiceCaller, serviceUrlList);
    }

    private static HttpMethodBaseMatcher aHttpMethodBase(HttpMethodType type, String url,
            String postBody) {
        return new HttpMethodBaseMatcher(type, url, postBody);
    }

    /**
     * Test that the function is able to actually load CSW records from multiple services
     * @throws Exception
     */
    @Test
    public void testGetCSWRecordsMultiService() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");
        final ByteArrayInputStream is1 = new ByteArrayInputStream(docString.getBytes());
        final ByteArrayInputStream is2 = new ByteArrayInputStream(docString.getBytes());
        final ByteArrayInputStream is3 = new ByteArrayInputStream(docString.getBytes());

        context.checking(new Expectations() {{
            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            allowing(mockFilter).getFilterStringAllRecords();

            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, String.format(serviceUrlFormatString, 0), null)), with(any(HttpClient.class)));will(returnValue(is1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, String.format(serviceUrlFormatString, 1), null)), with(any(HttpClient.class)));will(returnValue(is2));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, String.format(serviceUrlFormatString, 2), null)), with(any(HttpClient.class)));will(returnValue(is3));
        }});

        //We call this twice to test that an update wont commence whilst
        //an update for a service is already running (if it does it will trigger too many calls to getHttpClient
        CSWGetRecordResponse[] records = this.cswFilterService.getFilteredRecords(mockFilter, 100);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        }
        catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        Assert.assertNotNull(records);
        Assert.assertEquals(CONCURRENT_THREADS_TO_RUN, records.length);

        int totalCSWRecords = 0;
        for (CSWGetRecordResponse response : records) {
            totalCSWRecords += response.getRecords().size();
        }
        Assert.assertEquals(RECORD_COUNT_TOTAL * CONCURRENT_THREADS_TO_RUN, totalCSWRecords);
    }

    /**
     * Test that the function is able to actually load CSW records
     * @throws Exception
     */
    @Test
    public void testGetCountMultiService() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");
        final ByteArrayInputStream is1 = new ByteArrayInputStream(docString.getBytes());
        final ByteArrayInputStream is2 = new ByteArrayInputStream(docString.getBytes());
        final ByteArrayInputStream is3 = new ByteArrayInputStream(docString.getBytes());

        context.checking(new Expectations() {{
            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            allowing(mockFilter).getFilterStringAllRecords();

            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, String.format(serviceUrlFormatString, 0), null)), with(any(HttpClient.class)));will(returnValue(is1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, String.format(serviceUrlFormatString, 1), null)), with(any(HttpClient.class)));will(returnValue(is2));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, String.format(serviceUrlFormatString, 2), null)), with(any(HttpClient.class)));will(returnValue(is3));
        }});

        //We call this twice to test that an update wont commence whilst
        //an update for a service is already running (if it does it will trigger too many calls to getHttpClient
        int count = this.cswFilterService.getFilteredRecordsCount(mockFilter, 100);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        }
        catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        Assert.assertEquals(RECORD_MATCH_TOTAL * CONCURRENT_THREADS_TO_RUN, count);
    }

    /**
     * Test that the function is able to actually load CSW records from multiple services
     * @throws Exception
     */
    @Test
    public void testGetCSWRecordsSingleService() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");
        final ByteArrayInputStream is1 = new ByteArrayInputStream(docString.getBytes());

        final int serviceToTest = CONCURRENT_THREADS_TO_RUN / 2;
        final String serviceIdToUse = String.format(idFormatString, serviceToTest);
        final String expectedServiceUrl = String.format(serviceUrlFormatString, serviceToTest);

        context.checking(new Expectations() {{
            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            allowing(mockFilter).getFilterStringAllRecords();

            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, expectedServiceUrl, null)), with(any(HttpClient.class)));will(returnValue(is1));
        }});

        //We call this twice to test that an update wont commence whilst
        //an update for a service is already running (if it does it will trigger too many calls to getHttpClient
        CSWGetRecordResponse response = this.cswFilterService.getFilteredRecords(serviceIdToUse, mockFilter, 100, 1);
        Assert.assertNotNull(response);
        Assert.assertEquals(RECORD_COUNT_TOTAL, response.getRecordsReturned());
    }

    /**
     * Test that the function is able to actually load CSW records from multiple services
     * @throws Exception
     */
    @Test
    public void testGetCountSingleService() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");
        final ByteArrayInputStream is1 = new ByteArrayInputStream(docString.getBytes());

        final int serviceToTest = CONCURRENT_THREADS_TO_RUN / 2;
        final String serviceIdToUse = String.format(idFormatString, serviceToTest);
        final String expectedServiceUrl = String.format(serviceUrlFormatString, serviceToTest);

        context.checking(new Expectations() {{
            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            allowing(mockFilter).getFilterStringAllRecords();

            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, expectedServiceUrl, null)), with(any(HttpClient.class)));will(returnValue(is1));
        }});

        //We call this twice to test that an update wont commence whilst
        //an update for a service is already running (if it does it will trigger too many calls to getHttpClient
        int count = this.cswFilterService.getFilteredRecordsCount(serviceIdToUse, mockFilter, 100);
        Assert.assertEquals(RECORD_MATCH_TOTAL, count);
    }

    /**
     * Simple test to ensure that we can fetch the list of CSWServiceItems
     * @throws Exception
     */
    @Test
    public void testGetCSWServices() throws Exception {
        CSWServiceItem[] actual = this.cswFilterService.getCSWServiceItems();
        CSWServiceItem[] expected = serviceUrlList.toArray(new CSWServiceItem[serviceUrlList.size()]);

        Assert.assertArrayEquals(expected, actual);
    }
}
