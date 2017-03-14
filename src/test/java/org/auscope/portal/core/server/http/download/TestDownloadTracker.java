package org.auscope.portal.core.server.http.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpResponse;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDownloadTracker extends PortalTestClass {

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

        final String dummyCsv = "1,2,3,6,3,1";
        final String dummyJSONResponse = "{\"data\":{\"csv\":\"" + dummyCsv + "\"},\"success\":true}";

        final String dummyGml2 = "<someGmlHere>Mary has a little lamb</someGmlHere>";
        final String dummyJSONResponse2 = "{\"data\":{\"gml\":\"" + dummyGml2 + "\"},\"success\":true}";

        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());
        final InputStream dummyJSONResponseIS2 = new ByteArrayInputStream(dummyJSONResponse2.getBytes());

        final HttpResponse response1 = new MyHttpResponse(dummyJSONResponseIS);
        final HttpResponse response2 = new MyHttpResponse(dummyJSONResponseIS2);

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[0], null)));
                will(delayReturnValue(250, response1));

                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[1], null)));
                will(delayReturnValue(3000, response2));

                allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                will(returnValue(null));
            }
        });

        ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls, mockServiceCaller, threadPool,
                this.mockServiceConfiguration);
        DownloadTracker downloadTracker = DownloadTracker.getTracker("victor");
        Assert.assertEquals(Progression.NOT_STARTED, downloadTracker.getProgress());

        long startTime = System.currentTimeMillis();

        downloadTracker.startTrack(sdm);
        Assert.assertEquals(Progression.INPROGRESS, downloadTracker.getProgress());

        while (downloadTracker.getProgress() != Progression.COMPLETED
                && System.currentTimeMillis() < (startTime + 15000)) {
            synchronized (this) {
                try {
                    this.wait(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("testDownloadAll interrupted. Aborting test.");
                }
            }
        }

        Assert.assertEquals(Progression.COMPLETED, downloadTracker.getProgress());

        ZipInputStream zis = new ZipInputStream(downloadTracker.getFile());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            byte[] bytes = new byte[1024];
            Assert.assertNotNull(entry);
            Assert.assertTrue(entry.getName().endsWith(".xml"));
            zis.read(bytes, 0, bytes.length);
            String result = new String(bytes);
            result = result.trim();
            Assert.assertTrue(dummyCsv.equals(result) || dummyGml2.equals(result));
        }
        zis.close();
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

        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyXMLResponse.getBytes());
        final InputStream dummyJSONResponseIS2 = new ByteArrayInputStream(dummyXMLResponse.getBytes());

        final InputStream dummy0FeatureResponse = new ByteArrayInputStream(dummyXML0FeatureResponse.getBytes());

        final HttpResponse response1 = new MyHttpResponse(dummyJSONResponseIS);
        final HttpResponse response2 = new MyHttpResponse(dummyJSONResponseIS2);
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
                this.mockServiceConfiguration);
        DownloadTracker downloadTracker = DownloadTracker.getTracker("victor1");
        Assert.assertEquals(Progression.NOT_STARTED, downloadTracker.getProgress());

        long startTime = System.currentTimeMillis();

        downloadTracker.startTrack(sdm);
        Assert.assertEquals(Progression.INPROGRESS, downloadTracker.getProgress());

        //VT: && System.currentTimeMillis() < (startTime + 15000) a backup to ensure this unit test breaks and finishes in a
        //certain amount of time
        while (downloadTracker.getProgress() != Progression.COMPLETED
                && System.currentTimeMillis() < (startTime + 15000)) {
            synchronized (this) {
                try {
                    this.wait(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("testDownloadAllWithPaging interrupted. Aborting test.");
                }
            }
        }

        Assert.assertEquals(Progression.COMPLETED, downloadTracker.getProgress());

        ZipInputStream zis = new ZipInputStream(downloadTracker.getFile());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            byte[] bytes = new byte[1024];
            Assert.assertNotNull(entry);
            Assert.assertTrue(entry.getName().endsWith(".xml") || entry.getName().endsWith(".zip"));

            if (entry.getName().endsWith(".xml")) {
                zis.read(bytes, 0, bytes.length);
                String result = new String(bytes);
                result = result.trim();
                Assert.assertTrue(dummyXMLResponse.equals(result));
            }

        }
        zis.close();
    }

    @Test
    public void testDownloadWithFailure() throws IOException, URISyntaxException, InCompleteDownloadException {
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs"};

        final String dummyGml = "<someGmlHere>the quick brown fox jumps over the</someGmlHere>";
        final String dummyJSONResponse = "{\"data\":{\"gml\":\"" + dummyGml + "\"},\"success\":true}";

        final String dummyJSONResponse2 = "Exception thrown while attempting to download";

        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        final HttpResponse response = new MyHttpResponse(dummyJSONResponseIS);

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[0], null)));
                will(throwException(new IOException("Test exception thrown")));
                // calling the service
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(
                        with(aHttpMethodBase(null, serviceUrls[1], null)));
                will(delayReturnValue(200, response));

                allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                will(returnValue(null));

            }
        });

        ServiceDownloadManager sdm = new ServiceDownloadManager(serviceUrls, mockServiceCaller, threadPool,
                this.mockServiceConfiguration);
        DownloadTracker downloadTracker = DownloadTracker.getTracker("victor2");

        long startTime = System.currentTimeMillis();
        downloadTracker.startTrack(sdm);
        Assert.assertEquals(Progression.INPROGRESS, downloadTracker.getProgress());

        while (downloadTracker.getProgress() != Progression.COMPLETED
                && System.currentTimeMillis() < (startTime + 15000)) {
            synchronized (this) {
                try {
                    this.wait(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("testDownloadWithFailure interrupted. Aborting test.");
                }
            }
        }

        Assert.assertEquals(Progression.COMPLETED, downloadTracker.getProgress());

        ZipInputStream zis = new ZipInputStream(downloadTracker.getFile());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            byte[] bytes = new byte[1024];
            Assert.assertNotNull(entry);
            Assert.assertTrue(entry.getName().endsWith(".xml") || entry.getName().endsWith(".txt"));
            zis.read(bytes, 0, bytes.length);
            String result = new String(bytes);
            result = result.trim();

            if (entry.getName().endsWith(".xml")) {
                Assert.assertTrue(dummyGml.equals(result));
            } else {
                result.startsWith(dummyJSONResponse2);
            }
        }
        zis.close();
    }

}
