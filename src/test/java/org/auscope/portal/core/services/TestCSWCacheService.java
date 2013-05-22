package org.auscope.portal.core.services;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.BasicThreadExecutor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.test.jmock.HttpMethodBaseMatcher.HttpMethodType;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for CSWCacheService
 * @author Josh Vote
 */
public class TestCSWCacheService extends PortalTestClass {
    //determines the size of the test + congestion
    static final int CONCURRENT_THREADS_TO_RUN = 3;

    //These determine the correct numbers for a single read of the test file
    static final int RECORD_COUNT_TOTAL = 15;
    static final int RECORD_MATCH_TOTAL = 30;
    static final int RECORD_COUNT_WMS = 2;
    static final int RECORD_COUNT_WFS = 11;
    static final int RECORD_COUNT_ERMINE_RECORDS = 2;


    private CSWCacheService cswCacheService;
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
    private BasicThreadExecutor threadExecutor;

    private static final String serviceUrlFormatString = "http://cswservice.%1$s.url/";

    /**
     * Initialises each of our unit tests with a new CSWFilterService
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        this.threadExecutor = new BasicThreadExecutor();

          //Create our service list
          ArrayList<CSWServiceItem> serviceUrlList = new ArrayList<CSWServiceItem>(CONCURRENT_THREADS_TO_RUN);
          for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++) {
              serviceUrlList.add(new CSWServiceItem(String.format("id:%1$s", i + 1) ,String.format(serviceUrlFormatString, i + 1)));
          }

        this.cswCacheService = new CSWCacheService(threadExecutor, httpServiceCaller, serviceUrlList);
    }

    /**
     * Tests a regular update goes through and makes multiple requests over multiple threads
     * @throws Exception
     */
    @Test
    public void testMultiUpdate() throws Exception {
        final String moreRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");
        final String noMoreRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_NoMoreRecords.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t1r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t2r1 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t3r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t3r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());

        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");

        final int totalRequestsMade = CONCURRENT_THREADS_TO_RUN + 2;

        context.checking(new Expectations() {{
            //Thread 1 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            inSequence(t1Sequence);
            will(returnValue(t1r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            inSequence(t1Sequence);
            will(returnValue(t1r2));

            //Thread 2 will make 1 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)));
            inSequence(t2Sequence);
            will(returnValue(t2r1));

            //Thread 3 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            inSequence(t3Sequence);
            will(returnValue(t3r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            inSequence(t3Sequence);
            will(returnValue(t3r2));
        }});

        //Start our updating and wait for our threads to finish
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //Check our expected responses
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_TOTAL, this.cswCacheService.getRecordCache().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_WMS, this.cswCacheService.getWMSRecords().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_WFS, this.cswCacheService.getWFSRecords().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_ERMINE_RECORDS, this.cswCacheService.getWCSRecords().size());

        //Ensure that our internal state is set to NOT RUNNING AN UPDATE
        Assert.assertFalse(this.cswCacheService.updateRunning);
    }

    /**
     * Tests a regular update goes through and makes multiple requests over multiple threads
     * @throws Exception
     */
    @Test
    public void testMultiUpdateWithErrors() throws Exception {
        final String moreRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");
        final String noMoreRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_NoMoreRecords.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t1r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t3r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t3r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());

        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");

        final int totalRequestsMade = CONCURRENT_THREADS_TO_RUN + 1;

        context.checking(new Expectations() {{
            //Thread 1 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            inSequence(t1Sequence);
            will(returnValue(t1r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            inSequence(t1Sequence);
            will(returnValue(t1r2));

            //Thread 2 will throw an exception
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)));
            inSequence(t2Sequence);
            will(throwException(new Exception()));

            //Thread 3 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            inSequence(t3Sequence);
            will(returnValue(t3r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            inSequence(t3Sequence);
            will(returnValue(t3r2));
        }});

        //Start our updating and wait for our threads to finish
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //Check our expected responses
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_TOTAL, this.cswCacheService.getRecordCache().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_WMS, this.cswCacheService.getWMSRecords().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_WFS, this.cswCacheService.getWFSRecords().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_ERMINE_RECORDS, this.cswCacheService.getWCSRecords().size());

        //Ensure that our internal state is set to NOT RUNNING AN UPDATE
        Assert.assertFalse(this.cswCacheService.updateRunning);
    }

    /**
     * Tests a regular update goes through and makes multiple requests over multiple threads
     * @throws Exception
     */
    @Test
    public void testMultiUpdateAllErrors() throws Exception {
        final Map<String, Integer> expectedResult = new HashMap<String, Integer>();

        context.checking(new Expectations() {{
            for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++) {
                oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, i + 1), null)));
                will(throwException(new Exception()));
            }
        }});

        //Start our updating and wait for our threads to finish
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //Check our expected responses
        Assert.assertEquals(expectedResult, this.cswCacheService.getKeywordCache());
        Assert.assertEquals(0, this.cswCacheService.getRecordCache().size());
        Assert.assertEquals(0, this.cswCacheService.getWMSRecords().size());
        Assert.assertEquals(0, this.cswCacheService.getWFSRecords().size());
        Assert.assertEquals(0, this.cswCacheService.getWCSRecords().size());

        //Ensure that our internal state is set to NOT RUNNING AN UPDATE
        Assert.assertFalse(this.cswCacheService.updateRunning);
    }

    /**
     * Success if only a single update is able to run at any given time (Subsequent updates are terminated)
     * @throws Exception
     */
    @Test
    public void testSingleUpdate() throws Exception {
        final long delay = 1000;
        final String cswResponse = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_NoMoreRecords.xml");


        context.checking(new Expectations() {{
            for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++) {
                oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, String.format(serviceUrlFormatString, i + 1), null)));
                will(delayReturnValue(delay, new ByteArrayInputStream(cswResponse.getBytes())));
            }

        }});

        //Only one of these should trigger an update (the other should return immediately
        cswCacheService.updateCache();
        cswCacheService.updateCache();

        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }
    }

    /**
     * Tests cache service correctly merges records based on keywords
     * @throws Exception
     */
    @Test
    public void testRecordMerging() throws Exception {
        final String mergeRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_MergeRecords.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(mergeRecordsString.getBytes());

        context.checking(new Expectations() {{
            //Thread 1 will make 1 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            will(returnValue(t1r1));

            //Thread 2 will error
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)));
            will(throwException(new ConnectException()));

            //Thread 3 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            will(throwException(new ConnectException()));
        }});

        //Start our updating and wait for our threads to finish
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //There must be exactly 2 records (as 2 out of the 3 are candidates for merging)
        List<CSWRecord> cachedRecords = this.cswCacheService.getRecordCache();
        Assert.assertNotNull(cachedRecords);
        Assert.assertEquals(2, cachedRecords.size());

        //Verify we only have 2 records (correctly merged)
        Assert.assertTrue(cachedRecords.get(0).containsAnyOnlineResource(OnlineResourceType.WFS));
        Assert.assertTrue(cachedRecords.get(0).containsAnyOnlineResource(OnlineResourceType.WMS));
        Assert.assertFalse(cachedRecords.get(0).containsAnyOnlineResource(OnlineResourceType.WCS));
        Assert.assertFalse(cachedRecords.get(1).containsAnyOnlineResource(OnlineResourceType.WFS));
        Assert.assertFalse(cachedRecords.get(1).containsAnyOnlineResource(OnlineResourceType.WMS));
        Assert.assertTrue(cachedRecords.get(1).containsAnyOnlineResource(OnlineResourceType.WCS));

        //Verify our keyword cache is well formed too!
        Map<String, Set<CSWRecord>> keywordCache = this.cswCacheService.getKeywordCache();
        Assert.assertNotNull(keywordCache.get("WFS"));
        Assert.assertEquals(2, keywordCache.get("WFS").size());
        Assert.assertNotNull(keywordCache.get("WMS"));
        Assert.assertEquals(2, keywordCache.get("WMS").size());
        Assert.assertNotNull(keywordCache.get("WCS"));
        Assert.assertEquals(1, keywordCache.get("WCS").size());
        Assert.assertNotNull(keywordCache.get("association:unique-keyword"));
        Assert.assertEquals(1, keywordCache.get("association:unique-keyword").size());
    }

    /**
     * Tests cache service correctly merges online resources when they have the same name, type and URL (sans parameters)
     * @throws Exception
     */
    @Test
    public void testOnlineResourceMerging() throws Exception {
        final String mergeRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_MergeableResources.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(mergeRecordsString.getBytes());

        context.checking(new Expectations() {{
            //Thread 1 will make 1 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            will(returnValue(t1r1));

            //Thread 2 will error
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)));
            will(throwException(new ConnectException()));

            //Thread 3 will error
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            will(throwException(new ConnectException()));
        }});

        //Start our updating and wait for our threads to finish
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //There must be exactly 1 record
        List<CSWRecord> cachedRecords = this.cswCacheService.getRecordCache();
        Assert.assertNotNull(cachedRecords);
        Assert.assertEquals(1, cachedRecords.size());

        //Verify we only have 2 resources (correctly merged) - one merged resource and the other with a null URL
        CSWRecord rec = cachedRecords.get(0);
        Assert.assertTrue(rec.containsAnyOnlineResource(OnlineResourceType.WFS));
        Assert.assertEquals(2, rec.getOnlineResources().length);
        Assert.assertNull(rec.getOnlineResources()[0].getLinkage().getQuery()); //There should be no query string (it gets removed when merging)
        Assert.assertNull(rec.getOnlineResources()[1].getLinkage()); //Should be null
    }

    /**
     * Tests keyword cache gets properly populated
     * @throws Exception
     */
    @Test
    public void testKeywordCache() throws Exception {
        final String noMoreRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_NoMoreRecords.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(noMoreRecordsString.getBytes());

        final Map<String, Integer> expectedResult = new HashMap<String, Integer>();
        final int totalRequestsMade = CONCURRENT_THREADS_TO_RUN + 2;
        expectedResult.put("er:Commodity", 3);
        expectedResult.put("gsml:GeologicUnit", 2);
        expectedResult.put("er:MineralOccurrence", 3);
        expectedResult.put("manhattan", 1);
        expectedResult.put("DS_poi", 1);
        expectedResult.put("GeologicUnit", 2);
        expectedResult.put("poi", 1);
        expectedResult.put("Manhattan", 1);
        expectedResult.put("landmarks", 1);
        expectedResult.put("DS_poly_landmarks", 1);
        expectedResult.put("WFS", 6);
        expectedResult.put("gsml:MappedFeature", 6);
        expectedResult.put("World", 1);
        expectedResult.put("points_of_interest", 1);

        context.checking(new Expectations() {{
            //Thread 1 will make 1 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            will(returnValue(t1r1));

            //Thread 2 will error
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)));
            will(throwException(new ConnectException()));

            //Thread 3 will error
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            will(throwException(new ConnectException()));
        }});

        //Start our updating and wait for our threads to finish
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //Check our expected responses
        Map<String, Set<CSWRecord>> actualKeywordCache = this.cswCacheService.getKeywordCache();
        for (String keyword : expectedResult.keySet()) {
            int actualCount = actualKeywordCache.get(keyword) == null ? 0 : actualKeywordCache.get(keyword).size();
            Assert.assertEquals(keyword, expectedResult.get(keyword), new Integer(actualCount));
        }
    }

    /**
     * Tests a regular update fails when receiving an OWS error response or connection exceptions
     * @throws Exception
     */
    @Test
    public void testVariousErrors() throws Exception {
        final String owsErrorString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(owsErrorString.getBytes());

        context.checking(new Expectations() {{
            //Thread 1 will make a single request
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)));
            will(returnValue(t1r1));

            //Thread 2 will make a single request
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)));
            will(throwException(new ConnectException()));

            //Thread 3 will make a single request
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)));
            will(throwException(new ConnectException()));
        }});

        //Start our updating and wait for our threads to finish
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //Check our expected responses
        Assert.assertEquals(0, this.cswCacheService.getRecordCache().size());
        Assert.assertEquals(0, this.cswCacheService.getWMSRecords().size());
        Assert.assertEquals(0, this.cswCacheService.getWFSRecords().size());
        Assert.assertEquals(0, this.cswCacheService.getWCSRecords().size());

        //Ensure that our internal state is set to NOT RUNNING AN UPDATE
        Assert.assertFalse(this.cswCacheService.updateRunning);
    }

    /**
     * Tests a regular update goes through and makes multiple requests over multiple threads (using GetMethods)
     * @throws Exception
     */
    @Test
    public void testMultiUpdateGet() throws Exception {
        final String moreRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml");
        final String noMoreRecordsString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/csw/cswRecordResponse_NoMoreRecords.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t1r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t2r1 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t3r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t3r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());

        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");

        final int totalRequestsMade = CONCURRENT_THREADS_TO_RUN + 2;

        context.checking(new Expectations() {{
            //Thread 1 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.GET, Pattern.compile(String.format(serviceUrlFormatString, 1) + "?.*"), null)));
            inSequence(t1Sequence);
            will(returnValue(t1r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.GET, Pattern.compile(String.format(serviceUrlFormatString, 1)+ "?.*"), null)));
            inSequence(t1Sequence);
            will(returnValue(t1r2));

            //Thread 2 will make 1 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.GET, Pattern.compile(String.format(serviceUrlFormatString, 2)+ "?.*"), null)));
            inSequence(t2Sequence);
            will(returnValue(t2r1));

            //Thread 3 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.GET, Pattern.compile(String.format(serviceUrlFormatString, 3)+ "?.*"), null)));
            inSequence(t3Sequence);
            will(returnValue(t3r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.GET, Pattern.compile(String.format(serviceUrlFormatString, 3)+ "?.*"), null)));
            inSequence(t3Sequence);
            will(returnValue(t3r2));
        }});

        //Start our updating and wait for our threads to finish
        this.cswCacheService.setForceGetMethods(true);
        Assert.assertTrue(this.cswCacheService.isForceGetMethods());
        Assert.assertTrue(this.cswCacheService.updateCache());
        Thread.sleep(50);
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }

        //Check our expected responses
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_TOTAL, this.cswCacheService.getRecordCache().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_WMS, this.cswCacheService.getWMSRecords().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_WFS, this.cswCacheService.getWFSRecords().size());
        Assert.assertEquals(totalRequestsMade * RECORD_COUNT_ERMINE_RECORDS, this.cswCacheService.getWCSRecords().size());

        //Ensure that our internal state is set to NOT RUNNING AN UPDATE
        Assert.assertFalse(this.cswCacheService.updateRunning);
    }
}
