package org.auscope.portal.core.server.http.download;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
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

        final HttpResponse response1=new MyHttpResponse(dummyJSONResponseIS);
        final HttpResponse response2=new MyHttpResponse(dummyJSONResponseIS2);

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(with(aHttpMethodBase(null, serviceUrls[0], null)));
                will(delayReturnValue(250, response1));

                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(with(aHttpMethodBase(null, serviceUrls[1], null)));
                will(delayReturnValue(3000,response2));

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

        final HttpResponse response=new MyHttpResponse(dummyJSONResponseIS);


        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(with(aHttpMethodBase(null, serviceUrls[0], null)));
                will(throwException(new IOException("Test exception thrown")));
                // calling the service
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(with(aHttpMethodBase(null, serviceUrls[1], null)));
                will(delayReturnValue(200, response));
            }
        });


        ServiceDownloadManager sdm=new ServiceDownloadManager(serviceUrls,mockServiceCaller,threadPool);
        DownloadTracker downloadTracker=DownloadTracker.getTracker("victor2");

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
