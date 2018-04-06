package org.auscope.portal.core.server.http.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestServiceDownloadManager extends PortalTestClass {

    private ExecutorService threadPool;
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private ServiceConfiguration mockServiceConfiguration = context.mock(ServiceConfiguration.class);

    @Before
    public void setUp() {
        threadPool = Executors.newCachedThreadPool();

    }

    @Test
    public void testDownloadAll() throws IOException, URISyntaxException, InCompleteDownloadException {
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());
        try (final MyHttpResponse httpResponse = new MyHttpResponse(dummyJSONResponseIS)) {

            context.checking(new Expectations() {
                {
                    // calling the service
                    exactly(2).of(mockServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                    will(returnValue(httpResponse));

                    allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                    will(returnValue(null));
                }
            });

            ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls, mockServiceCaller, threadPool,
                    mockServiceConfiguration);
            ArrayList<DownloadResponse> gmlDownloads = sdm.downloadAll();
            for (DownloadResponse response : gmlDownloads) {
                Assert.assertEquals(dummyJSONResponseIS, response.getResponseAsStream());
                Assert.assertFalse(response.hasException());
                Assert.assertNull(response.getException());
            }
        }
    }

    @Test
    public void testDownloadAllWithPaging() throws IOException, URISyntaxException, InCompleteDownloadException {

        final ServiceConfigurationItem scItem = new ServiceConfigurationItem("exampleTestId", "exampleTest.com/test",
                true);

        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://exampleTest.com/test/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs"};

        final String dummyXMLResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wfs:FeatureCollection xmlns:wfs=\"http://www.opengis.net/wfs\" numberOfFeatures=\"5\" timeStamp=\"2014-08-18T07:03:37.594Z\" ></wfs:FeatureCollection>";

        final String dummyXML0FeatureResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wfs:FeatureCollection xmlns:wfs=\"http://www.opengis.net/wfs\" numberOfFeatures=\"0\" timeStamp=\"2014-08-18T07:03:37.594Z\" ></wfs:FeatureCollection>";

        final InputStream dummyXMLResponseIS = new ByteArrayInputStream(dummyXMLResponse.getBytes());
        final InputStream dummyXMLResponseIS2 = new ByteArrayInputStream(dummyXMLResponse.getBytes());

        final InputStream dummy0FeatureResponse = new ByteArrayInputStream(dummyXML0FeatureResponse.getBytes());

        final HttpResponse response1 = new MyHttpResponse(dummyXMLResponseIS);
        final HttpResponse response2 = new MyHttpResponse(dummyXMLResponseIS2);
        final HttpResponse ZeroFeatureResponse = new MyHttpResponse(dummy0FeatureResponse);

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[0] + "&startIndex=0", null)));
                will(delayReturnValue(250, response1));

                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[0] + "&startIndex=5", null)));
                will(delayReturnValue(250, ZeroFeatureResponse));

                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[1], null)));
                will(delayReturnValue(3000, response2));

                oneOf(mockServiceConfiguration).getServiceConfigurationItem(with(serviceUrls[0]));
                will(returnValue(scItem));

                oneOf(mockServiceConfiguration).getServiceConfigurationItem(with(serviceUrls[1]));
                will(returnValue(null));
            }
        });

        ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls, mockServiceCaller, threadPool,
                mockServiceConfiguration);
        ArrayList<DownloadResponse> gmlDownloads = sdm.downloadAll();
        for (DownloadResponse response : gmlDownloads) {
            Assert.assertFalse(response.hasException());
            Assert.assertNull(response.getException());
            String contentType = response.getContentType();
            if (contentType.equals("application/zip")) {
                ZipInputStream zis = new ZipInputStream(response.getResponseAsStream());
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    byte[] bytes = new byte[1024];
                    Assert.assertNotNull(entry);
                    Assert.assertTrue(entry.getName().endsWith(".xml"));
                    zis.read(bytes, 0, bytes.length);
                    String result = new String(bytes);
                    result = result.trim();
                    Assert.assertTrue(dummyXMLResponse.equals(result));
                }
                zis.close();

            } else if (contentType.equals("application/xml")) {
                dummyXMLResponse.equals(response.getResponseAsString());
            } else {
                //VT:It should have never come here
                Assert.assertTrue(false);
            }

        }

    }

    @Test
    public void testDownloadAllException() throws IOException, URISyntaxException, InCompleteDownloadException {
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://www.mrt.tas.gov.au:80/web-services/wfs"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        try (final MyHttpResponse httpResponse = new MyHttpResponse(dummyJSONResponseIS)) {

            context.checking(new Expectations() {
                {
                    // calling the service
                    atLeast(1).of(mockServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                    will(onConsecutiveCalls(
                            returnValue(httpResponse),
                            throwException(new IOException("test exception"))));

                    allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                    will(returnValue(null));
                }
            });

            ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls, mockServiceCaller, threadPool,
                    mockServiceConfiguration);
            ArrayList<DownloadResponse> gmlDownloads = sdm.downloadAll();
            Assert.assertEquals(serviceUrls.length, gmlDownloads.size());
            for (DownloadResponse response : gmlDownloads) {

                if (response.hasException()) {
                    Assert.assertNotNull(response.getException());
                    Assert.assertTrue("test exception".equals(response.getException().getMessage()));
                } else {
                    Assert.assertEquals(dummyJSONResponseIS, response.getResponseAsStream());
                    Assert.assertNull(response.getException());
                }
            }
        }
    }

    /**
     * A complicated scenario that sees 3 requests being firing off to 2 shared resources.
     *
     * note - this test is built on the assumption maxThreadPerEndpoint=1 and maxThreadPerSession=2 if these values change then this test will be invalid
     * @throws IOException 
     * @throws URISyntaxException 
     * @throws InCompleteDownloadException 
     */
    @Test
    public void testServiceFairness() throws IOException, URISyntaxException, InCompleteDownloadException {
        
        // assume this test is NOT running within TRAVIS CI. If it is the result will be ignored.
        // This is done because TRAVIS is not fair even if this service class is.
        
        org.junit.Assume.assumeTrue("Travis environment detected, skipping Service fairness test because Travis performance is not sufficiently predictable to reliably evaluate fairness",System.getenv("TRAVIS")==null);
        
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
                1000,
                2000,
                1500
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
        context.checking(new Expectations() {
            {
                //It's too difficult to test a sequence as at step 1 it is undefined
                //as to whether url0/url2 will be requested first (they will be requested at roughly the same time).
                //It's also too difficult to use a JMock state for the same reason - we are stuck comparing execution times

                //first request
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[0], null)));
                will(delayReturnValue(responseDelays[0], new MyHttpResponse(responseStreams[0])));

                //second request
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[2], null)));
                will(delayReturnValue(responseDelays[2], new MyHttpResponse(responseStreams[2])));

                //third request
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[1], null)));
                will(delayReturnValue(responseDelays[1], new MyHttpResponse(responseStreams[1])));

                allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                will(returnValue(null));
            }
        });

        //We have a pretty good idea of what the processing time and margin of error should be
        long processingTime = Math.min(responseDelays[0], responseDelays[2]) + responseDelays[1];
        long marginOfError = processingTime / 10;
        long minProcessingTime = processingTime - 5;
        long maxProcessingTime = processingTime + marginOfError;

        //Create our service downloader
        ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls, mockServiceCaller, threadPool,
                mockServiceConfiguration);

        startTimer();
        ArrayList<DownloadResponse> gmlDownloads = sdm.downloadAll();
        long elapsedTime = endTimer();

        //Given our processing order we expect a specific response time (and no errors).
        //This will indicate our requests are run in a 'fair' order
        Assert.assertTrue(String.format("elapsedTime %1$s is not in the range [%2$s, %3$s]", elapsedTime,
                minProcessingTime, maxProcessingTime),
                elapsedTime >= minProcessingTime && elapsedTime <= maxProcessingTime);
        Assert.assertNotNull(gmlDownloads);
        for (DownloadResponse dr : gmlDownloads) {
            Assert.assertNotNull(dr);
            Assert.assertFalse(dr.hasException());
        }
    }

    /**
     * Tests a download with no service URL parameter succeeds
     * @throws IOException 
     * @throws URISyntaxException 
     * @throws InCompleteDownloadException 
     */
    @Test
    public void testDownloadNoServiceUrlParam() throws IOException, URISyntaxException, InCompleteDownloadException {
        final String[] serviceUrls = {"http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?param1=value=1&param2=value2"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        context.checking(new Expectations() {
            {
                // calling the service
                exactly(1).of(mockServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                will(returnValue(new MyHttpResponse(dummyJSONResponseIS)));

                allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                will(returnValue(null));
            }
        });

        ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls, mockServiceCaller, threadPool,
                mockServiceConfiguration);
        ArrayList<DownloadResponse> gmlDownloads = sdm.downloadAll();
        for (DownloadResponse response : gmlDownloads) {
            Assert.assertEquals(dummyJSONResponseIS, response.getResponseAsStream());
            Assert.assertFalse(response.hasException());
            Assert.assertNull(response.getException());
        }

    }
}
