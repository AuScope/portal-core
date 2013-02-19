package org.auscope.portal.core.server.http.download;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.HttpMethodBase;
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

        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + "<someGmlHere/>" + "\"},\"success\":true}";
        final String dummyJSONResponse2 = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + "<someGmlHere2/>" + "\"},\"success\":true}";

        final InputStream dummyJSONResponseIS=new ByteArrayInputStream(dummyJSONResponse.getBytes());
        final InputStream dummyJSONResponseIS2=new ByteArrayInputStream(dummyJSONResponse2.getBytes());

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[0], null)));
                will(delayReturnValue(10000, dummyJSONResponseIS));
                // calling the service
                oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, serviceUrls[1], null)));
                will(delayReturnValue(200, dummyJSONResponseIS2));
            }
        });

        ServiceDownloadManager sdm=new ServiceDownloadManager(serviceUrls,mockServiceCaller,threadPool);
        DownloadTracker downloadTracker=DownloadTracker.getTracker("victor");
        downloadTracker.startTrack(sdm);
        System.out.println(downloadTracker.getProgress());
//        try{
//            downloadTracker.startTrack(sdm);
//        }catch(InCompleteDownloadException e){
//            e.printStackTrace();
//        }
        while(downloadTracker.getProgress()!=Progression.COMPLETED){

        }

        System.out.println(downloadTracker.getProgress());


    }


}
