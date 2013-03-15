package org.auscope.portal.core.server.http.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDownloadTracker extends PortalTestClass  {

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

        final String dummyGml = "<someGmlHere>the quick brown fox jumps over the</someGmlHere>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";

        final String dummyGml2 = "<someGmlHere>Mary has a little lamb</someGmlHere>";
        final String dummyJSONResponse2 = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml2 + "\"},\"success\":true}";

        final InputStream dummyJSONResponseIS=new ByteArrayInputStream(dummyJSONResponse.getBytes());
        final InputStream dummyJSONResponseIS2=new ByteArrayInputStream(dummyJSONResponse2.getBytes());

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[0], null)),with(any(HttpClient.class)));
                will(delayReturnValue(3500, dummyJSONResponseIS));
                // calling the service
                oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[1], null)),with(any(HttpClient.class)));
                will(delayReturnValue(200, dummyJSONResponseIS2));
            }
        });


        ServiceDownloadManager sdm=new ServiceDownloadManager(serviceUrls,mockServiceCaller,threadPool);
        DownloadTracker downloadTracker=DownloadTracker.getTracker("victor");
        Assert.assertEquals(Progression.NOT_STARTED, downloadTracker.getProgress());

        long startTime = System.currentTimeMillis();

        downloadTracker.startTrack(sdm);
        Assert.assertEquals(Progression.INPROGRESS, downloadTracker.getProgress());

        while(downloadTracker.getProgress()!=Progression.COMPLETED && System.currentTimeMillis() < (startTime + 15000)){
            synchronized(this){
                this.wait(2000);
            }
        }

        Assert.assertEquals(Progression.COMPLETED,downloadTracker.getProgress());

        ZipInputStream zis = new ZipInputStream(downloadTracker.getFile());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null){
            byte[] bytes= new byte[1024];
            Assert.assertNotNull(entry);
            Assert.assertTrue(entry.getName().endsWith(".xml"));
            zis.read(bytes, 0, bytes.length);
            String result= new String(bytes);
            result=result.trim();
            Assert.assertTrue(dummyGml.equals(result)||dummyGml2.equals(result));
        }
        zis.close();
    }

    @Test
    public void testDownloadWithFailure() throws Exception {
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs"};

        final String dummyGml = "<someGmlHere>the quick brown fox jumps over the</someGmlHere>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";

        final String dummyJSONResponse2 = "Exception thrown while attempting to download";


        final InputStream dummyJSONResponseIS=new ByteArrayInputStream(dummyJSONResponse.getBytes());


        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[0], null)),with(any(HttpClient.class)));
                will(throwException(new IOException("Test exception thrown")));
                // calling the service
                oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[1], null)),with(any(HttpClient.class)));
                will(delayReturnValue(200, dummyJSONResponseIS));
            }
        });


        ServiceDownloadManager sdm=new ServiceDownloadManager(serviceUrls,mockServiceCaller,threadPool);
        DownloadTracker downloadTracker=DownloadTracker.getTracker("victor");

        long startTime = System.currentTimeMillis();
        downloadTracker.startTrack(sdm);
        Assert.assertEquals(Progression.INPROGRESS, downloadTracker.getProgress());

        while(downloadTracker.getProgress()!=Progression.COMPLETED && System.currentTimeMillis() < (startTime + 15000)){
            synchronized(this){
                this.wait(2000);
            }
        }

        Assert.assertEquals(Progression.COMPLETED,downloadTracker.getProgress());

        ZipInputStream zis = new ZipInputStream(downloadTracker.getFile());
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null){
            byte[] bytes= new byte[1024];
            Assert.assertNotNull(entry);
            Assert.assertTrue(entry.getName().endsWith(".xml")||entry.getName().endsWith(".txt"));
            zis.read(bytes, 0, bytes.length);
            String result= new String(bytes);
            result=result.trim();

            if(entry.getName().endsWith(".xml")){
                Assert.assertTrue(dummyGml.equals(result));
            }else{
                result.startsWith(dummyJSONResponse2);
            }
        }
        zis.close();
    }

}
