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
}
