package org.auscope.portal.core.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.auscope.portal.core.server.http.DistributedHTTPServiceCallerException;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.responses.csw.CSWGetDomainResponse;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.test.BasicThreadExecutor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the CSWFilterService
 * 
 * @author Josh Vote
 *
 */
public class TestCSWFilterService extends PortalTestClass {
    //determines the size of the test + congestion
    static final int CONCURRENT_THREADS_TO_RUN = 3;

    //These determine the correct numbers for a single read of the test file
    static final int RECORD_COUNT_TOTAL = 15;
    static final int RECORD_MATCH_TOTAL = 30;

    private CSWFilterService cswFilterService;
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
    private BasicThreadExecutor threadExecutor;
    private CSWGetDataRecordsFilter mockFilter = context.mock(CSWGetDataRecordsFilter.class);
    private ArrayList<CSWServiceItem> serviceUrlList;

    private static final String IDFORMATSTRING = "id:%1$s";
    private static final String SERVICEURLFORMATSTRING = "http://cswservice.%1$s.url/";
    private static final String PROPERTYNAME = "Subject";

    /**
     * Initialises each of our unit tests with a new CSWFilterService
     */
    @Before
    public void setUp() {

        this.threadExecutor = new BasicThreadExecutor();

        //Create our service list
        serviceUrlList = new ArrayList<>(CONCURRENT_THREADS_TO_RUN);
        for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++) {
            serviceUrlList.add(new CSWServiceItem(String.format(IDFORMATSTRING, i), String.format(
                    SERVICEURLFORMATSTRING, i)));
        }

        this.cswFilterService = new CSWFilterService(threadExecutor, httpServiceCaller, serviceUrlList);
    }

    /**
     * Test that the function is able to actually load CSW records from multiple services
     * @throws PortalServiceException 
     * @throws IOException 
     */
    @Test
    public void testGetCSWRecordsMultiService() throws PortalServiceException, IOException {
        final String docString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");

        try (final HttpClientInputStream is1 = new HttpClientInputStream(new ByteArrayInputStream(docString.getBytes()), null);
                final HttpClientInputStream is2 = new HttpClientInputStream(new ByteArrayInputStream(docString.getBytes()), null);
                final HttpClientInputStream is3 = new HttpClientInputStream(
                        new ByteArrayInputStream(docString.getBytes()), null)) {

            context.checking(new Expectations() {
                {

                    allowing(mockFilter).getSortType();
                    allowing(mockFilter).getFilterStringAllRecords();

                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, String.format(SERVICEURLFORMATSTRING, 0), null)));
                    will(returnValue(is1));

                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, String.format(SERVICEURLFORMATSTRING, 1), null)));
                    will(returnValue(is2));

                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, String.format(SERVICEURLFORMATSTRING, 2), null)));
                    will(returnValue(is3));
                }
            });

            // We call this twice to test that an update wont commence whilst
            // an update for a service is already running (if it does it will
            // trigger too many calls to getHttpClient
            CSWGetRecordResponse[] records = this.cswFilterService.getFilteredRecords(mockFilter, 100);
            try {
                threadExecutor.getExecutorService().shutdown();
                threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
            } catch (Exception ex) {
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
    }

    /**
     * Test that the function is able to actually load CSW records
     * @throws IOException 
     * @throws DistributedHTTPServiceCallerException 
     */
    @Test
    public void testGetCountMultiService() throws DistributedHTTPServiceCallerException, IOException {
        final String docString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");
        try (final HttpClientInputStream is1 = new HttpClientInputStream(new ByteArrayInputStream(docString.getBytes()), null);
                final HttpClientInputStream is2 = new HttpClientInputStream(new ByteArrayInputStream(docString.getBytes()), null);
                final HttpClientInputStream is3 = new HttpClientInputStream(
                        new ByteArrayInputStream(docString.getBytes()), null)) {

            context.checking(new Expectations() {
                {
                    allowing(mockFilter).getSortType();
                    allowing(mockFilter).getFilterStringAllRecords();

                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, String.format(SERVICEURLFORMATSTRING, 0), null)));
                    will(returnValue(is1));
                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, String.format(SERVICEURLFORMATSTRING, 1), null)));
                    will(returnValue(is2));
                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, String.format(SERVICEURLFORMATSTRING, 2), null)));
                    will(returnValue(is3));
                }
            });

            // We call this twice to test that an update wont commence whilst
            // an update for a service is already running (if it does it will
            // trigger too many calls to getHttpClient
            int count = this.cswFilterService.getFilteredRecordsCount(mockFilter, 100);
            try {
                threadExecutor.getExecutorService().shutdown();
                threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
            } catch (Exception ex) {
                threadExecutor.getExecutorService().shutdownNow();
                Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
            }

            Assert.assertEquals(RECORD_MATCH_TOTAL * CONCURRENT_THREADS_TO_RUN, count);
        }
    }

    /**
     * Test that the function is able to actually load CSW records from multiple services
     * @throws PortalServiceException 
     * @throws IOException 
     */
    @Test
    public void testGetCSWRecordsSingleService() throws PortalServiceException, IOException {
        final String docString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");
        try (final HttpClientInputStream is1 = new HttpClientInputStream(new ByteArrayInputStream(docString.getBytes()),
                null)) {
            final int serviceToTest = CONCURRENT_THREADS_TO_RUN / 2;
            final String serviceIdToUse = String.format(IDFORMATSTRING, serviceToTest);
            final String expectedServiceUrl = String.format(SERVICEURLFORMATSTRING, serviceToTest);

            context.checking(new Expectations() {
                {
                    allowing(mockFilter).getSortType();
                    allowing(mockFilter).getFilterStringAllRecords();

                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, expectedServiceUrl, null)));
                    will(returnValue(is1));
                }
            });

            // We call this twice to test that an update wont commence whilst
            // an update for a service is already running (if it does it will
            // trigger too many calls to getHttpClient
            CSWGetRecordResponse response = this.cswFilterService.getFilteredRecords(serviceIdToUse, mockFilter, 100,
                    1);
            Assert.assertNotNull(response);
            Assert.assertEquals(RECORD_COUNT_TOTAL, response.getRecordsReturned());
        }
    }

    /**
     * Test that the function is able to actually load CSW records from multiple services
     * @throws IOException 
     * @throws PortalServiceException 
     */
    @Test
    public void testGetCountSingleService() throws IOException, PortalServiceException {
        final String docString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");
        try (final HttpClientInputStream is1 = new HttpClientInputStream(new ByteArrayInputStream(docString.getBytes()),
                null)) {

            final int serviceToTest = CONCURRENT_THREADS_TO_RUN / 2;
            final String serviceIdToUse = String.format(IDFORMATSTRING, serviceToTest);
            final String expectedServiceUrl = String.format(SERVICEURLFORMATSTRING, serviceToTest);

            context.checking(new Expectations() {
                {
                    allowing(mockFilter).getSortType();
                    allowing(mockFilter).getFilterStringAllRecords();

                    oneOf(httpServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, expectedServiceUrl, null)));
                    will(returnValue(is1));
                }
            });

            // We call this twice to test that an update wont commence whilst
            // an update for a service is already running (if it does it will
            // trigger too many calls to getHttpClient
            int count = this.cswFilterService.getFilteredRecordsCount(serviceIdToUse, mockFilter, 100);
            Assert.assertEquals(RECORD_MATCH_TOTAL, count);
        }
    }

    /**
     * Simple test to ensure that we can fetch the list of CSWServiceItems
     */
    @Test
    public void testGetCSWServices() {
        CSWServiceItem[] actual = this.cswFilterService.getCSWServiceItems();
        CSWServiceItem[] expected = serviceUrlList.toArray(new CSWServiceItem[serviceUrlList.size()]);

        Assert.assertArrayEquals(expected, actual);
    }

}
