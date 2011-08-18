package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.DelayedReturnValueAction;
import org.auscope.portal.HttpMethodBaseMatcher;
import org.auscope.portal.HttpMethodBaseMatcher.HttpMethodType;
import org.auscope.portal.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.api.Action;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for CSWCacheService
 * @author Josh Vote
 */
public class TestCSWCacheService extends CSWCacheService {
  //determines the size of the test + congestion
    static final int CONCURRENT_THREADS_TO_RUN = 3;

    //These determine the correct numbers for a single read of the test file
    static final int RECORD_COUNT_TOTAL = 15;
    static final int RECORD_MATCH_TOTAL = 30;
    static final int RECORD_COUNT_WMS = 2;
    static final int RECORD_COUNT_WFS = 12;
    static final int RECORD_COUNT_ERMINE_RECORDS = 2;

    public TestCSWCacheService() throws Exception {
        super(null, null, new ArrayList());
    }

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};


    private CSWCacheService cswCacheService;
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
    private CSWThreadExecutor threadExecutor;
    private CSWGetDataRecordsFilter mockFilter = context.mock(CSWGetDataRecordsFilter.class);
    private HttpClient mockHttpClient = context.mock(HttpClient.class);

    private static final String serviceUrlFormatString = "http://cswservice.%1$s.url/";

    /**
     * Initialises each of our unit tests with a new CSWFilterService
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {

        this.threadExecutor = new CSWThreadExecutor();

          //Create our service list
          ArrayList<CSWServiceItem> serviceUrlList = new ArrayList<CSWServiceItem>(CONCURRENT_THREADS_TO_RUN);
          for (int i = 0; i < CONCURRENT_THREADS_TO_RUN; i++){
              serviceUrlList.add(new CSWServiceItem(String.format("id:%1$s", i + 1) ,String.format(serviceUrlFormatString, i + 1)));
          }

        this.cswCacheService = new CSWCacheService(threadExecutor, httpServiceCaller, serviceUrlList);
    }

    private static HttpMethodBaseMatcher aHttpMethodBase(HttpMethodType type, String url, String postBody) {
        return new HttpMethodBaseMatcher(type, url, postBody);
    }

    /**
     * Tests a regular update goes through and makes multiple requests over multiple threads
     * @throws Exception
     */
    @Test
    public void testMultiUpdate() throws Exception {
        final String moreRecordsString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");
        final String noMoreRecordsString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse_NoMoreRecords.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t1r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t2r1 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t3r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t3r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());

        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");

        final Map<String, Integer> expectedResult = new HashMap<String, Integer>();
        final int totalRequestsMade = CONCURRENT_THREADS_TO_RUN + 2;
        expectedResult.put("er:Commodity", new Integer(totalRequestsMade));
        expectedResult.put("gsml:GeologicUnit", new Integer(totalRequestsMade));
        expectedResult.put("er:MineralOccurrence", new Integer(totalRequestsMade));
        expectedResult.put("manhattan", new Integer(totalRequestsMade));
        expectedResult.put("DS_poi", new Integer(totalRequestsMade));
        expectedResult.put("GeologicUnit", new Integer(totalRequestsMade));
        expectedResult.put("poi", new Integer(totalRequestsMade));
        expectedResult.put("Manhattan", new Integer(totalRequestsMade));
        expectedResult.put("landmarks", new Integer(totalRequestsMade));
        expectedResult.put("DS_poly_landmarks", new Integer(totalRequestsMade));
        expectedResult.put("WFS", new Integer(totalRequestsMade * 2));
        expectedResult.put("gsml:MappedFeature", new Integer(totalRequestsMade * 2));
        expectedResult.put("World", new Integer(totalRequestsMade));
        expectedResult.put("points_of_interest", new Integer(totalRequestsMade));
        expectedResult.put("poly_landmarks", new Integer(totalRequestsMade));
        expectedResult.put("MappedFeature", new Integer(totalRequestsMade));

        context.checking(new Expectations() {{
            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            //Thread 1 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)), with(any(HttpClient.class)));
            inSequence(t1Sequence);
            will(returnValue(t1r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)), with(any(HttpClient.class)));
            inSequence(t1Sequence);
            will(returnValue(t1r2));

            //Thread 2 will make 1 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)), with(any(HttpClient.class)));
            inSequence(t2Sequence);
            will(returnValue(t2r1));

            //Thread 3 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)), with(any(HttpClient.class)));
            inSequence(t3Sequence);
            will(returnValue(t3r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)), with(any(HttpClient.class)));
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
        Assert.assertEquals(expectedResult, this.cswCacheService.getKeywordCache());
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
        final String moreRecordsString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");
        final String noMoreRecordsString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse_NoMoreRecords.xml");
        final ByteArrayInputStream t1r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t1r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t2r1 = new ByteArrayInputStream(noMoreRecordsString.getBytes());
        final ByteArrayInputStream t3r1 = new ByteArrayInputStream(moreRecordsString.getBytes());
        final ByteArrayInputStream t3r2 = new ByteArrayInputStream(noMoreRecordsString.getBytes());

        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");

        final Map<String, Integer> expectedResult = new HashMap<String, Integer>();
        final int totalRequestsMade = CONCURRENT_THREADS_TO_RUN + 1;
        expectedResult.put("er:Commodity", new Integer(totalRequestsMade));
        expectedResult.put("gsml:GeologicUnit", new Integer(totalRequestsMade));
        expectedResult.put("er:MineralOccurrence", new Integer(totalRequestsMade));
        expectedResult.put("manhattan", new Integer(totalRequestsMade));
        expectedResult.put("DS_poi", new Integer(totalRequestsMade));
        expectedResult.put("GeologicUnit", new Integer(totalRequestsMade));
        expectedResult.put("poi", new Integer(totalRequestsMade));
        expectedResult.put("Manhattan", new Integer(totalRequestsMade));
        expectedResult.put("landmarks", new Integer(totalRequestsMade));
        expectedResult.put("DS_poly_landmarks", new Integer(totalRequestsMade));
        expectedResult.put("WFS", new Integer(totalRequestsMade * 2));
        expectedResult.put("gsml:MappedFeature", new Integer(totalRequestsMade * 2));
        expectedResult.put("World", new Integer(totalRequestsMade));
        expectedResult.put("points_of_interest", new Integer(totalRequestsMade));
        expectedResult.put("poly_landmarks", new Integer(totalRequestsMade));
        expectedResult.put("MappedFeature", new Integer(totalRequestsMade));

        context.checking(new Expectations() {{
            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            //Thread 1 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)), with(any(HttpClient.class)));
            inSequence(t1Sequence);
            will(returnValue(t1r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)), with(any(HttpClient.class)));
            inSequence(t1Sequence);
            will(returnValue(t1r2));

            //Thread 2 will throw an exception
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)), with(any(HttpClient.class)));
            inSequence(t2Sequence);
            will(throwException(new Exception()));

            //Thread 3 will make 2 requests
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)), with(any(HttpClient.class)));
            inSequence(t3Sequence);
            will(returnValue(t3r1));
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)), with(any(HttpClient.class)));
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
        Assert.assertEquals(expectedResult, this.cswCacheService.getKeywordCache());
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
        final Sequence t1Sequence = context.sequence("t1Sequence");
        final Sequence t2Sequence = context.sequence("t2Sequence");
        final Sequence t3Sequence = context.sequence("t3Sequence");

        final Map<String, Integer> expectedResult = new HashMap<String, Integer>();

        context.checking(new Expectations() {{
            allowing(httpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));

            //Thread 1 will throw an exception
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 1), null)), with(any(HttpClient.class)));
            inSequence(t1Sequence);
            will(throwException(new Exception()));

            //Thread 2 will throw an exception
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 2), null)), with(any(HttpClient.class)));
            inSequence(t2Sequence);
            will(throwException(new Exception()));

            //Thread 3 will throw an exception
            oneOf(httpServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(HttpMethodType.POST, String.format(serviceUrlFormatString, 3), null)), with(any(HttpClient.class)));
            inSequence(t3Sequence);
            will(throwException(new Exception()));
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

    private static Action delayReturnValue(long msDelay, Object returnValue) throws Exception {
        return new DelayedReturnValueAction(msDelay, returnValue);
    }

    /**
     * Success if only a single update is able to run at any given time (Subsequent updates are terminated)
     * @throws Exception
     */
    @Test
    public void testSingleUpdate() throws Exception {
        final long delay = 1000;
        final String cswResponse = "<?xml version=\"1.0\"?><node></node>";

        context.checking(new Expectations() {{
            //Cant use oneOf as JUnit can't handle exceptions on other threads (see note below)
            //oneOf(httpServiceCaller).getHttpClient();
            //oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(delayReturnValue(delay, cswResponse));

            allowing(httpServiceCaller).getHttpClient();
            allowing(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(delayReturnValue(delay, cswResponse));
        }});

        final CSWCacheService service = this.cswCacheService;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    service.updateCache();
                } catch(Exception e) {
                    Assert.fail(e.toString());
                }
            }
        };

        Calendar start = Calendar.getInstance();

        //Only one of these threads should actually make a service call
        //otherwise our expectations will fail
        UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                Assert.fail(e.toString());
            }
        };
        Thread[] threadList = new Thread[5];
        Thread.setDefaultUncaughtExceptionHandler(eh);
        for (int i = 0; i < threadList.length; i++) {
            threadList[i] = new Thread(r);
            threadList[i].setUncaughtExceptionHandler(eh);
            threadList[i].start();
        }

        //Wait for each thread to terminate (we expect the first
        //thread will wait for the full delay whilst all other threads
        //should return immediately)
        //
        //NOTE - JUnit won't pickup the Mock Object exceptions on the other threads
        //     - Workaround - We still work on the assumption that only a single
        //                    Thread will delay and all others will return immediately
        //                    So we just measure the time and as long as it is less than
        //                    threadList.length * delay we are OK
        for (Thread t : threadList) {
            t.join();
        }

        Calendar finish = Calendar.getInstance();
        long totalTime = finish.getTimeInMillis() - start.getTimeInMillis();
        Assert.assertTrue("Test took too long, assuming other threads are NOT returning immediately", totalTime < (delay * 2));
    }
}
