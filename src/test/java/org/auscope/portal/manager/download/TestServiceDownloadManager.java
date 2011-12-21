package org.auscope.portal.manager.download;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestServiceDownloadManager extends PortalTestClass  {

    private ExecutorService threadPool;
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);

    @Before
    public void setUp() {
        threadPool = Executors.newCachedThreadPool();

    }

    @Test
    public void testDownloadAll() throws Exception {
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";
        final InputStream dummyJSONResponseIS=new ByteArrayInputStream(dummyJSONResponse.getBytes());

        context.checking(new Expectations() {
            {
                // calling the service
                exactly(2).of(mockServiceCaller).getHttpClient();
                exactly(2).of(mockServiceCaller).getMethodResponseAsStream(with(any(HttpMethodBase.class)),with(any(HttpClient.class)));
                    will(returnValue(dummyJSONResponseIS));
            }
        });

        ServiceDownloadManager sdm=new ServiceDownloadManager(serviceUrls,mockServiceCaller,threadPool);
        ArrayList<DownloadResponse> gmlDownloads=sdm.downloadAll();
        for(DownloadResponse response:gmlDownloads){
            Assert.assertEquals(dummyJSONResponseIS,response.getResponseAsStream());
            Assert.assertFalse(response.hasException());
            Assert.assertNull(response.getException());
        }

    }

    @Test
    public void testDownloadAllException() throws Exception {
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://www.mrt.tas.gov.au:80/web-services/wfs"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";
        final InputStream dummyJSONResponseIS=new ByteArrayInputStream(dummyJSONResponse.getBytes());

        context.checking(new Expectations() {
            {
                // calling the service
                exactly(2).of(mockServiceCaller).getHttpClient();
                atLeast(1).of(mockServiceCaller).getMethodResponseAsStream(with(any(HttpMethodBase.class)),with(any(HttpClient.class)));
                    will(onConsecutiveCalls(
                            returnValue(dummyJSONResponseIS),
                            throwException(new Exception("test exception"))));
            }
        });

        ServiceDownloadManager sdm=new ServiceDownloadManager(serviceUrls,mockServiceCaller,threadPool);
        ArrayList<DownloadResponse> gmlDownloads=sdm.downloadAll();
        Assert.assertEquals(serviceUrls.length, gmlDownloads.size());
        for(DownloadResponse response:gmlDownloads){

            if(response.hasException()){
                Assert.assertNotNull(response.getException());
                Assert.assertTrue("test exception".equals(response.getException().getMessage()));
            }else{
                Assert.assertEquals(dummyJSONResponseIS,response.getResponseAsStream());
                Assert.assertNull(response.getException());
            }
        }
    }

    /**
     * A complicated scenario that sees 3 requests being firing off to 2 shared resources.
     *
     * note - this test is built on the assumption maxThreadPerEndpoint=1 and maxThreadPerSession=2
     *        if these values change then this test will be invalid
     *
     * @throws Exception
     */
    @Test
    public void testServiceFairness() throws Exception {
        final String[] serviceUrls = {
            "http://localhost/portal?serviceUrl=http://domain1/wfs",
            "http://localhost/portal?serviceUrl=http://domain1/wfs",
            "http://localhost/portal?serviceUrl=http://domain2/wfs",
        };
        final InputStream[] responseStreams = {
            context.mock(InputStream.class, "is-1"),
            context.mock(InputStream.class, "is-2"),
            context.mock(InputStream.class, "is-3"),
        };
        final long[] responseDelays = {
            400,
            800,
            600
        };

        //The service should hit url 0 and 2 simultaneously and when one returns
        //make a further request to url 1.
        //Because our responses all take specific amounts of time we can expect
        //that the overall processing will take a fixed amount of time
        //
        //The rough formula is
        //0ms      Request url0, Request url2
        //400ms    Response url0, Request url1
        //600ms    Response url2
        //1200ms   Response url1
        context.checking(new Expectations() {{
            allowing(mockServiceCaller).getHttpClient();//We aren't testing this

            //It's too difficult to test a sequence as at step 1 it is undefined
            //as to whether url0/url2 will be requested first (they will be requested at roughly the same time).
            //It's also too difficult to use a JMock state for the same reason - we are stuck comparing execution times

            //first request
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[0], null)), with(any(HttpClient.class)));
            will(delayReturnValue(responseDelays[0], responseStreams[0]));

            //second request
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[2], null)), with(any(HttpClient.class)));
            will(delayReturnValue(responseDelays[2], responseStreams[2]));

            //third request
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[1], null)), with(any(HttpClient.class)));
            will(delayReturnValue(responseDelays[1], responseStreams[1]));
        }});

        //We have a pretty good idea of what the processing time and margin of error should be
        long processingTime = Math.min(responseDelays[0], responseDelays[2]) + responseDelays[1];
        long marginOfError = Math.min(Math.min(responseDelays[0], responseDelays[1]), responseDelays[2]) / 10;
        long minProcessingTime = processingTime - 1;
        long maxProcessingTime = processingTime + marginOfError;

        //Create our service downloader
        ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls,mockServiceCaller,threadPool);

        startTimer();
        ArrayList<DownloadResponse> gmlDownloads = sdm.downloadAll();
        long elapsedTime = endTimer();

        //Given our processing order we expect a specific response time (and no errors).
        //This will indicate our requests are run in a 'fair' order
        Assert.assertTrue(String.format("elapsedTime %1$s is not in the range [%2$s, %3$s]", elapsedTime, minProcessingTime, maxProcessingTime),
                elapsedTime >= minProcessingTime && elapsedTime <= maxProcessingTime);
        Assert.assertNotNull(gmlDownloads);
        for (DownloadResponse dr : gmlDownloads) {
            Assert.assertNotNull(dr);
            Assert.assertFalse(dr.hasException());
        }
    }
}
