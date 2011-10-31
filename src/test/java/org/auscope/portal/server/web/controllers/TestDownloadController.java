package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class TestDownloadController {
    private ExecutorService threadPool;

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context
            .mock(HttpServiceCaller.class);

    /**
     * The controller to test
     */
    private DownloadController downloadController;

    /**
     * Mock response
     */
    private HttpServletResponse mockHttpResponse = context
            .mock(HttpServletResponse.class);

    /**
     * Needed so we can check the contents of our zip file after it is written
     */
    final class MyServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        public void write(int i) throws IOException {
            byteArrayOutputStream.write(i);
        }

        public ZipInputStream getZipInputStream() {
            return new ZipInputStream(new ByteArrayInputStream(
                    byteArrayOutputStream.toByteArray()));
        }
    };

    @Before
    public void setUp() {
        downloadController = new DownloadController(httpServiceCaller);
        // TODO : VT jmock 2.5.1 doesn't have great support for testing multi
        // threading. Currently if we allow more then 1 thread to run, I get
        // erratic test errors. 2.6.0 will provide greater support with
        // Synchroniser. http://www.jmock.org/threading-synchroniser.html is
        // what we need in 2.6.0
        // Note: DeterministicExecutor is not needed as threadpool will
        // awaitTermination
        threadPool = Executors.newSingleThreadExecutor();

    }

    /**
     * Test that this function makes all of the approriate calls, and see if it
     * returns gml given some dummy data
     */
    @Test
    public void testDownloadGMLAsZip() throws Exception {
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream();
        final String[] serviceUrls = { "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs" };
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":true}";

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)),
                        with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getHttpClient();
                oneOf(httpServiceCaller).getMethodResponseAsString(
                        with(any(HttpMethodBase.class)),
                        with(any(HttpClient.class)));
                will(returnValue(dummyJSONResponse));
            }
        });

        downloadController.downloadGMLAsZip(serviceUrls, mockHttpResponse,
                threadPool);
        Thread.sleep(100);

        // Check that the zip file contains the correct data
        ZipInputStream in = servletOutputStream.getZipInputStream();
        ZipEntry ze = in.getNextEntry();

        Assert.assertNotNull(ze);
        Assert.assertTrue(ze.getName().endsWith(".xml"));

        byte[] uncompressedData = new byte[dummyGml.getBytes().length];
        int dataRead = in.read(uncompressedData);

        Assert.assertEquals(dummyGml.getBytes().length, dataRead);
        Assert.assertArrayEquals(dummyGml.getBytes(), uncompressedData);

        in.close();
    }

    /**
     * Test that this function makes all of the approriate calls, and see if it
     * returns gml given some dummy data
     *
     * This dummy data is missing the data element but contains a msg property
     * (This added in response to JIRA AUS-1575)
     */
    @Test
    public void testDownloadGMLAsZipWithJSONError() throws Exception {
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream();
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://www.mrt.tas.gov.au:80/web-services/wfs" };
        final String dummyMessage = "hereisadummymessage";
        final String dummyJSONResponse = "{\"msg\": '" + dummyMessage
                + "',\"success\":false}";
        final String dummyJSONResponseNoMsg = "{\"success\":false}";

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                exactly(2).of(mockHttpResponse).setContentType(
                        with(any(String.class)));
                exactly(2).of(mockHttpResponse).setHeader(
                        with(any(String.class)), with(any(String.class)));
                exactly(2).of(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                exactly(2).of(httpServiceCaller).getHttpClient();
                oneOf(httpServiceCaller).getMethodResponseAsString(
                        with(any(HttpMethodBase.class)),
                        with(any(HttpClient.class)));
                will(returnValue(dummyJSONResponse));
                oneOf(httpServiceCaller).getMethodResponseAsString(
                        with(any(HttpMethodBase.class)),
                        with(any(HttpClient.class)));
                will(returnValue(dummyJSONResponseNoMsg));
            }
        });

        downloadController.downloadGMLAsZip(serviceUrls, mockHttpResponse,
                threadPool);
        Thread.sleep(100);

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        String[] names = new String[2];

        ZipEntry ze = zipInputStream.getNextEntry();
        Assert.assertNotNull(ze);
        names[0] = ze.getName();

        ze = zipInputStream.getNextEntry();
        Assert.assertNotNull(ze);
        names[1] = ze.getName();

        if (names[0].endsWith(dummyMessage + ".xml")
                || names[1].endsWith(dummyMessage + ".xml")) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(false);
        }

        if (names[0].endsWith("operation-failed.xml")
                || names[1].endsWith("operation-failed.xml")) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(false);
        }

        zipInputStream.close();
    }

    /**
     * Test that this function makes all of the approriate calls, and see if it
     * returns gml given some dummy data
     */
    @Test
    public void testDownloadGMLAsZipWithError() throws Exception {
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream();
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://www.mrt.tas.gov.au:80/web-services/wfs" };
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\""
                + dummyGml + "\"},\"success\":false}";

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)),
                        with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                exactly(2).of(httpServiceCaller).getHttpClient();
                exactly(2).of(httpServiceCaller).getMethodResponseAsString(
                        with(any(HttpMethodBase.class)),
                        with(any(HttpClient.class)));
                will(returnValue(dummyJSONResponse));
            }
        });

        downloadController.downloadGMLAsZip(serviceUrls, mockHttpResponse,
                threadPool);
        Thread.sleep(100);

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = null;
        while ((ze = zipInputStream.getNextEntry()) != null) {
            Assert.assertTrue(ze.getName().endsWith("operation-failed.xml"));
        }
        zipInputStream.close();
    }

    /**
     * Test that this function makes all of the approriate calls, and see if it
     * returns xml file zipped up
     */
    @Test
    public void testDownloadDataAsZipWithError() throws Exception {
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream();
        final String[] serviceUrls = { "http://someUrl" };
        final String dummyData = "dummyData";
        final Header header = new Header("Content-Type", "text/xml");

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)),
                        with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getHttpClient();
                oneOf(httpServiceCaller).getMethodResponseInBytes(
                        with(any(HttpMethodBase.class)),
                        with(any(HttpClient.class)));
                will(returnValue(dummyData.getBytes()));

                // return a string containing xml, which will denote some form
                // of error from a WMS call
                oneOf(httpServiceCaller).getResponseHeader(
                        with(any(HttpMethodBase.class)),
                        with(any(String.class)));
                will(returnValue(header));
            }
        });

        downloadController.downloadDataAsZip(serviceUrls, "WMS_Layer_Download",
                mockHttpResponse);

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = null;
        while ((ze = zipInputStream.getNextEntry()) != null) {
            // name of the file should end in .xml
            Assert.assertTrue(ze.getName().endsWith(".xml"));

            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            for (int c = zipInputStream.read(); c != -1; c = zipInputStream
                    .read()) {
                fout.write(c);
            }
            zipInputStream.closeEntry();
            fout.close();

            // should only have one entery with the gml data in it
            Assert.assertEquals(new String(dummyData.getBytes()), new String(
                    fout.toByteArray()));
        }
        zipInputStream.close();
    }

    /**
     * Test that this function makes all of the approriate calls, and see if it
     * returns png file zipped up
     *
     * @throws Exception
     */
    @Test
    public void testDownloadDataAsZipWithPNG() throws Exception {
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream();
        final String[] serviceUrls = { "http://someUrl" };
        final String dummyData = "dummyData";
        final Header header = new Header("Content-Type", "image/png");

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)),
                        with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getHttpClient();
                oneOf(httpServiceCaller).getMethodResponseInBytes(
                        with(any(HttpMethodBase.class)),
                        with(any(HttpClient.class)));
                will(returnValue(dummyData.getBytes()));

                // return a string containing xml, which will denote some form
                // of error from a WMS call
                oneOf(httpServiceCaller).getResponseHeader(
                        with(any(HttpMethodBase.class)),
                        with(any(String.class)));
                will(returnValue(header));
            }
        });

        downloadController.downloadDataAsZip(serviceUrls, "WMS_Layer_Download",
                mockHttpResponse);

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = null;
        while ((ze = zipInputStream.getNextEntry()) != null) {
            // name of the file should end in .xml
            Assert.assertTrue(ze.getName().endsWith(".png"));

            ByteArrayOutputStream fout = new ByteArrayOutputStream();
            for (int c = zipInputStream.read(); c != -1; c = zipInputStream
                    .read()) {
                fout.write(c);
            }
            zipInputStream.closeEntry();
            fout.close();

            // should only have one entery with the gml data in it
            Assert.assertEquals(new String(dummyData.getBytes()), new String(
                    fout.toByteArray()));
        }
        zipInputStream.close();
    }

}
