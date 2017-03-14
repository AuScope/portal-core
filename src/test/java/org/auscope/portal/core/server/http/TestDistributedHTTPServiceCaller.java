package org.auscope.portal.core.server.http;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDistributedHTTPServiceCaller extends PortalTestClass {
    protected final Log logger = LogFactory.getLog(getClass());

    private HttpRequestBase mockMethod1 = context.mock(HttpRequestBase.class, "method1");
    private HttpRequestBase mockMethod2 = context.mock(HttpRequestBase.class, "method2");
    private HttpRequestBase mockMethod3 = context.mock(HttpRequestBase.class, "method3");
    private Object mockAdditionalInfo1 = context.mock(Object.class, "mockAddInfo1");
    private Object mockAdditionalInfo2 = context.mock(Object.class, "mockAddInfo2");
    private Object mockAdditionalInfo3 = context.mock(Object.class, "mockAddInfo3");
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private ExecutorService threadPool;
    private HttpClientInputStream mockInputStream1 = context.mock(HttpClientInputStream.class, "stream1");
    private HttpClientInputStream mockInputStream2 = context.mock(HttpClientInputStream.class, "stream2");
    private HttpClientInputStream mockInputStream3 = context.mock(HttpClientInputStream.class, "stream3");

    @Before
    public void initialise() {
        threadPool = Executors.newFixedThreadPool(5); //don't adjust this, most of the tests are built on the assumption of 5 threads
    }

    @After
    public void teardDown() throws InterruptedException {
        threadPool.shutdownNow();
        threadPool.awaitTermination(5000L, TimeUnit.MILLISECONDS);
    }

    /**
     * Asserts that value lies within an specified (inclusive) range of values
     *
     * @param value
     * @param lowerBound
     * @param upperBound
     * @param epsilon
     */
    private static void assertRange(long value, long lowerBound, long upperBound) {
        if (value < lowerBound ||
                value > upperBound) {
            Assert.fail(String.format("%1$s is not in the range [%2$s, %3$s]", value, lowerBound, upperBound));
        }
    }

    /**
     * Tests that exceptions in the HTTP call will result in exceptions in the next
     * @throws IOException
     */
    @Test
    public void testReturnException() throws IOException  {
        final ConnectException expectedError = new ConnectException("fooBARbaz");
        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(Arrays.asList(mockMethod1),
                mockServiceCaller);

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                will(throwException(expectedError));
            }
        });

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
     * @throws IOException
     */
    @Test
    public void testBlockingNext() throws IOException {
        final long delay2ms = 3000;
        final long timeEpsilonMs = 600; //This should be an order of magnitude smaller than the above delays
        final ConnectException expectedError = new ConnectException("fooBARbaz");
        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(Arrays.asList(mockMethod1,
                mockMethod2), mockServiceCaller);

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                will(throwException(expectedError));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(delayReturnValue(delay2ms, mockInputStream2));
            }
        });

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
     * @throws IOException
     */
    @Test
    public void testFastestOrdering() throws IOException {
        final long delay1ms = 2400;
        final long delay2ms = 600;
        final long delay3ms = 1500;

        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(
                Arrays.asList(mockMethod1, mockMethod2, mockMethod3),
                Arrays.asList(mockAdditionalInfo1, mockAdditionalInfo2, mockAdditionalInfo3),
                mockServiceCaller);

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                will(delayReturnValue(delay1ms, mockInputStream1));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod2);
                will(delayReturnValue(delay2ms, mockInputStream2));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod3);
                will(delayReturnValue(delay3ms, mockInputStream3));
            }
        });

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
     * @throws IOException
     */
    @Test
    public void testAbort() throws IOException {
        //We want a list with more methods than the threadpool has threads
        final List<HttpRequestBase> bigMethodList = Arrays.asList(mockMethod1, mockMethod1, mockMethod1, mockMethod1,
                mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1, mockMethod1,
                mockMethod1, mockMethod1);
        final DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(bigMethodList, mockServiceCaller);

        context.checking(new Expectations() {
            {
                exactly(5).of(mockServiceCaller).getMethodResponseAsStream(mockMethod1);
                will(delayReturnValue(300, mockInputStream1));
            }
        });

        //start our threads executing (we need to use this class to pickup any failures)
        dsc.beginCallingServices(threadPool);

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Assert.fail("testAbort unexpectedly aborted");
        }//ensure that the first 5 startup (the size of our threadpool)

        dsc.dispose(); //abort everything

        //Wait for the threadpool to shutdown
        threadPool.shutdown();
        try {
            Assert.assertTrue("Threadpool didnt shutdown!!", threadPool.awaitTermination(3000, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Assert.fail("Threadpool shutdown unexpectedly aborted");
        }
    }
}
