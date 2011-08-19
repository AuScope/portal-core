package org.auscope.portal.server.web;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.DelayedReturnValueAction;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.ExpectationError;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestDistributedHTTPServiceCaller implements Thread.UncaughtExceptionHandler {

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private HttpMethodBase mockMethod1 = context.mock(HttpMethodBase.class, "method1");
    private HttpMethodBase mockMethod2 = context.mock(HttpMethodBase.class, "method2");
    private HttpMethodBase mockMethod3 = context.mock(HttpMethodBase.class, "method3");
    private Object mockAdditionalInfo1 = context.mock(Object.class, "mockAddInfo1");
    private Object mockAdditionalInfo2 = context.mock(Object.class, "mockAddInfo2");
    private Object mockAdditionalInfo3 = context.mock(Object.class, "mockAddInfo3");
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private HttpClient mockHttpClient = context.mock(HttpClient.class);
    private ExecutorService threadPool;
    private List<ExpectationError> expectationErrors;
    private InputStream mockInputStream1 = context.mock(InputStream.class, "stream1");
    private InputStream mockInputStream2 = context.mock(InputStream.class, "stream2");
    private InputStream mockInputStream3 = context.mock(InputStream.class, "stream3");

    private static Action delayReturnValue(long msDelay, Object returnValue) throws Exception {
        return new DelayedReturnValueAction(msDelay, returnValue);
    }

    private static Calendar timerCalendar;
    private static void startTimer() {
        timerCalendar = Calendar.getInstance();
    }

    private static long endTimer() {
        return (Calendar.getInstance().getTimeInMillis() - timerCalendar.getTimeInMillis());
    }

    @Before
    public void initialise() {
        threadPool = Executors.newFixedThreadPool(5); //don't adjust this, most of the tests are built on the assumption of 5 threads
        expectationErrors = new ArrayList<ExpectationError>();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * This is to ensure that no expectation errors on seperate threads go unnoticed
     */
    @After
    public void teardown() {
        if (!expectationErrors.isEmpty()) {
            Assert.fail(expectationErrors.get(0).toString());
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof ExpectationError) {
            expectationErrors.add((ExpectationError)e);
        }
    }

    /**
     * Asserts that value lies within an specified (inclusive) range of values
     * @param value
     * @param lowerBound
     * @param upperBound
     * @param epsilon
     */
    private void assertRange(long value, long lowerBound, long upperBound) {
        if (value < lowerBound ||
            value > upperBound) {
            Assert.fail(String.format("%1$s is not in the range [%2$s, %3$s]", value, lowerBound, upperBound));
        }
    }

    /**
     * Tests that exceptions in the HTTP call will result in exceptions in the next
     * @throws Exception
     */
    @Test
    public void testReturnException() throws Exception {
        final ConnectException expectedError = new ConnectException("fooBARbaz");
        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(Arrays.asList(mockMethod1), mockServiceCaller);



        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1, mockHttpClient);will(throwException(expectedError));
        }});

        dsc.beginCallingServices(threadPool);

        Assert.assertTrue(dsc.hasNext());

        try {
            dsc.next(); //should throw an exception
            Assert.fail("Exception not thrown!!");
        } catch (DistributedHTTPServiceCallerException ex) {
            Assert.assertEquals(expectedError, ex.getCause());
        }
    }

    /**
     * Tests that calls to next will block
     * @throws Exception
     */
    @Test
    public void testBlockingNext() throws Exception {
        final long delay2ms = 500;
        final long timeEpsilonMs = 100; //This should be an order of magnitude smaller than the above delays
        final ConnectException expectedError = new ConnectException("fooBARbaz");
        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(Arrays.asList(mockMethod1, mockMethod2), mockServiceCaller);

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1, mockHttpClient);will(throwException(expectedError));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2, mockHttpClient);will(delayReturnValue(delay2ms, mockInputStream2));
        }});

        //ensure our available and error data return immediately but our
        dsc.beginCallingServices(threadPool);

        //Firstly we should get the error (with little to no delay)
        startTimer();
        Assert.assertTrue(dsc.hasNext());
        assertRange(endTimer(), 0, timeEpsilonMs);//hasNext shouldnt block
        try {
            startTimer();
            dsc.next(); //should throw an exception
            Assert.fail("Exception not thrown!!");
        } catch (DistributedHTTPServiceCallerException ex) {
            assertRange(endTimer(), 0, timeEpsilonMs); ////the first next() shouldnt block
            Assert.assertEquals(expectedError, ex.getCause());
        }

        //Then we should get stream 2 after a delay2ms wait
        startTimer();
        Assert.assertTrue(dsc.hasNext());
        assertRange(endTimer(), 0, timeEpsilonMs);//hasNext shouldnt block
        startTimer();
        Assert.assertEquals(mockInputStream2, dsc.next());
        long delayTime = endTimer() + 1L;

        assertRange(delayTime, delay2ms - timeEpsilonMs, delay2ms + timeEpsilonMs);//this next should block for at least delay2ms

        //And then there should be no more data
        Assert.assertFalse(dsc.hasNext());
        Assert.assertNull(dsc.next());
    }

    /**
     * Tests that calls to next will return the NEXT item to complete
     * @throws Exception
     */
    @Test
    public void testFastestOrdering() throws Exception {
        final long delay1ms = 400;
        final long delay2ms = 100;
        final long delay3ms = 250;

        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(
                Arrays.asList(mockMethod1, mockMethod2, mockMethod3),
                Arrays.asList(mockAdditionalInfo1, mockAdditionalInfo2, mockAdditionalInfo3),
                mockServiceCaller);

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1, mockHttpClient);will(delayReturnValue(delay1ms, mockInputStream1));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2, mockHttpClient);will(delayReturnValue(delay2ms, mockInputStream2));
            oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod3, mockHttpClient);will(delayReturnValue(delay3ms, mockInputStream3));
        }});

        //Do some dodgey timings to ensure we get our data in the right order (ie as the input streams become available)
        dsc.beginCallingServices(threadPool);

        //We should get stream 2
        Assert.assertTrue(dsc.hasNext());
        Assert.assertEquals(mockInputStream2, dsc.next());
        Assert.assertEquals(mockAdditionalInfo2, dsc.getLastAdditionalInformation());

        //Then stream 3
        Assert.assertTrue(dsc.hasNext());
        Assert.assertEquals(mockInputStream3, dsc.next());
        Assert.assertEquals(mockAdditionalInfo3, dsc.getLastAdditionalInformation());

        //Then finally stream 1
        Assert.assertTrue(dsc.hasNext());
        Assert.assertEquals(mockInputStream1, dsc.next());
        Assert.assertEquals(mockAdditionalInfo1, dsc.getLastAdditionalInformation());

        //And then there should be no more data
        Assert.assertFalse(dsc.hasNext());
        Assert.assertNull(dsc.next());
    }

    /**
     * Tests that calls to abort actually work...
     * @throws Exception
     */
    @Test
    public void testAbort() throws Exception {
        //We want a list with more methods than the threadpool has threads
        final List<HttpMethodBase> bigMethodList = Arrays.asList(mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1);
        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(bigMethodList, mockServiceCaller);

        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            exactly(5).of(mockServiceCaller).getMethodResponseAsStream(mockMethod1, mockHttpClient);will(delayReturnValue(100, mockInputStream1));
        }});

        //start our threads executing (we need to use this class to pickup any failures)
        dsc.beginCallingServices(threadPool);

        Thread.sleep(50);//ensure that the first 5 startup (the size of our threadpool)

        dsc.dispose(); //abort everything

        //Wait for the threadpool to shutdown
        threadPool.shutdown();
        Assert.assertTrue("Threadpool didnt shutdown!!", threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS));
    }
}
