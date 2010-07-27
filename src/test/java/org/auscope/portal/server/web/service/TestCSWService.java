package org.auscope.portal.server.web.service;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;

import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.server.util.Util;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.ReturnValueAction;
import org.jmock.lib.legacy.ClassImposteriser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;

/**
 * User: Mathew Wyatt
 * Date: 20/08/2009
 * @version $Id$
 */
public class TestCSWService {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Main object we are testing
     */
    private CSWService cswService;

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);

    /**
     * Mock thread executor
     */
    private CSWThreadExecutor threadExecutor = context.mock(CSWThreadExecutor.class);

    /**
     * Mock Util
     * @throws Exception
     */
     private Util util = new Util();//context.mock(Util.class);

    @Before
    public void setup() throws Exception {
       /* context.checking(new Expectations() {{
            //constructor gets a host property
            oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));

            //check that the executor was called
            oneOf(threadExecutor).execute(with(any(Runnable.class)));
        }});*/
        
        this.cswService = new CSWService(threadExecutor, httpServiceCaller, util);
        this.cswService.setServiceUrl("http://localhost");
    }

    //TODO: test when there are no data records and the time interval is less than 5 minutes, see cswService.updateRecordsInBackground() funtion for logic to test
    /**
     * Test that the thread is executed
     * @throws Exception
     */
    @Test
    public void testUpdateCSWRecordsInBackground() throws Exception {
        context.checking(new Expectations() {{
            oneOf(threadExecutor).execute(with(any(Runnable.class)));
        }});

        this.cswService.updateRecordsInBackground();
    }
    
    /**
     * A simple extension on ReturnValue that adds a delay before the object is returned
     * @author vot002
     *
     */
    private static class DelayedReturnValueAction extends ReturnValueAction {
        private long delayMs;
        public DelayedReturnValueAction(long delayMs, Object returnValue) {
            super(returnValue);
            this.delayMs = delayMs;
        }
        
        @Override
        public Object invoke(Invocation i) throws Throwable {
            Thread.sleep(delayMs);
            return super.invoke(i);
        }
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
        final String cswResponse = "foo"; 
        
        context.checking(new Expectations() {{
            //Cant use oneOf as JUnit can't handle exceptions on other threads (see note below)
            //oneOf(httpServiceCaller).getHttpClient();
            //oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(delayReturnValue(delay, cswResponse));
            
            allowing(httpServiceCaller).getHttpClient();
            allowing(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(delayReturnValue(delay, cswResponse));
        }});
        
        final CSWService service = this.cswService;
        
        Runnable r = new Runnable() {
            public void run() {
                service.updateCSWRecords();
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

    /**
     * Test that the function is able to actually load CSW records
     * @throws Exception
     */
    @Test
    public void testUpdateCSWRecords() throws Exception {
        final String docString = org.auscope.portal.Util.loadXML("src/test/resources/cswRecordResponse.xml");

        context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(docString));
        }});

        this.cswService.updateCSWRecords();

        //in the response we loaded from the text file it contains 55 records
        Assert.assertEquals(55, this.cswService.getDataRecords().length);
    }

    /**
     * Test we return WMS records only
     * @throws Exception
     */
    @Test
    public void testGetWMSRecords() throws Exception {
        //make sure the data records are populated
        testUpdateCSWRecords();

        //in the response we loaded from the text file it contains 6 WMS records
        Assert.assertEquals(6, this.cswService.getWMSRecords().length);
    }

    /**
     * Test we return WFS records only
     * @throws Exception
     */
    @Test
    public void testGetWFSRecords() throws Exception {
        //make sure the data records are populated
        testUpdateCSWRecords();

        //in the response we loaded from the text file it contains 41 WFS records
        Assert.assertEquals(41, this.cswService.getWFSRecords().length);
    }
    
    /**
     * Test we return WCS records only
     * @throws Exception
     */
    @Test
    public void testGetWCSRecords() throws Exception {
        //make sure the data records are populated
        testUpdateCSWRecords();

        //in the response we loaded from the text file it contains 2 WCS records
        Assert.assertEquals(2, this.cswService.getWCSRecords().length);
    }

    /**
     * Test we get WFS recrods based on a feature typeName
     * @throws Exception
     */
    @Test
    public void testGetWFSRecordsForTypename() throws Exception {
        //make sure the data records are populated
        testUpdateCSWRecords();

        //in the response we loaded from the text file it contains 2 er:Mine records
        Assert.assertEquals(2, this.cswService.getWFSRecordsForTypename("er:Mine").length);
    }

}
