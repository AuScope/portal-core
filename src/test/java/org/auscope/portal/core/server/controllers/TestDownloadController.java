package org.auscope.portal.core.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.server.controllers.DownloadController;
import org.auscope.portal.core.server.http.HttpClientResponse;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.server.http.download.MyHttpResponse;
import org.auscope.portal.core.test.ByteBufferedServletOutputStream;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class TestDownloadController extends PortalTestClass {
    private ExecutorService threadPool;

    /**
     * Mock httpService caller
     */
    private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
    private ServiceConfiguration mockServiceConfiguration = context.mock(ServiceConfiguration.class);

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
    final class MyServletOutputStream extends ByteBufferedServletOutputStream {
        public MyServletOutputStream(int length) {
            super(length);
        }

        public ZipInputStream getZipInputStream() {
            return new ZipInputStream(new ByteArrayInputStream(
                    this.getStream().toByteArray()));
        }
    }

    @Before
    public void setUp() {
        downloadController = new DownloadController(httpServiceCaller, mockServiceConfiguration);
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
     * Test that this function makes all of the approriate calls, and see if it returns gml given some dummy data
     */
    @Test
    public void testDownloadGMLAsZip() throws Exception {
        final String[] serviceUrls = {"http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs"};
        final String outputFormat = "gml";
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"gml\":\"" + dummyGml + "\"},\"success\":true}";
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream(dummyJSONResponse.length());
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)), with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                will(returnValue(new HttpClientResponse(new MyHttpResponse(dummyJSONResponseIS), null)));

                allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                will(returnValue(null));
            }
        });

        downloadController.downloadGMLAsZip(serviceUrls, mockHttpResponse,
                threadPool, null, outputFormat);
        Thread.sleep(100);
        dummyJSONResponseIS.close();

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
     * Test that this function makes all of the approriate calls, and see if it returns gml given some dummy data
     *
     * This dummy data is missing the data element but contains a msg property (This added in response to JIRA AUS-1575)
     */
    @Test
    public void testDownloadGMLAsZipWithJSONError() throws Exception {

        final String outputFormat = "gml";
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://www.mrt.tas.gov.au:80/web-services/wfs"};
        final String dummyMessage = "hereisadummymessage";
        final String dummyJSONResponse = "{\"msg\": '" + dummyMessage
                + "',\"success\":false}";
        final String dummyJSONResponseNoMsg = "{\"success\":false}";
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream(dummyJSONResponseNoMsg.length());
        final InputStream dummyJSONResponseNoMsgIS = new ByteArrayInputStream(dummyJSONResponseNoMsg.getBytes());
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)), with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                will(returnValue(new HttpClientResponse(new MyHttpResponse(dummyJSONResponseIS), null)));
                oneOf(httpServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                will(delayReturnValue(300, new HttpClientResponse(new MyHttpResponse(dummyJSONResponseNoMsgIS), null)));

                allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                will(returnValue(null));
            }
        });

        downloadController.downloadGMLAsZip(serviceUrls, mockHttpResponse,
                threadPool, null, outputFormat);
        Thread.sleep(500);
        dummyJSONResponseNoMsgIS.close();
        dummyJSONResponseIS.close();

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = zipInputStream.getNextEntry();
        Assert.assertNotNull(ze);
        String name = ze.getName();
        Assert.assertTrue(name.equals("downloadInfo.txt"));

        String error = "Unsuccessful JSON reply from: http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs\n" +
                "hereisadummymessage\n\n" +
                "Unsuccessful JSON reply from: http://www.mrt.tas.gov.au:80/web-services/wfs\n" +
                "No error message\n\n";

        byte[] uncompressedData = new byte[error.getBytes().length];
        zipInputStream.read(uncompressedData);
        String s = new String(uncompressedData);
        Assert.assertTrue(s.contains("hereisadummymessage"));
        Assert.assertTrue(s.contains("No error message"));

        zipInputStream.close();
    }

    /**
     * Test that this function makes all of the approriate calls, and see if it returns gml given some dummy data
     */
    @Test
    public void testDownloadGMLAsZipWithException() throws Exception {
        final String outputFormat = "gml";
        final String[] serviceUrls = {
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://localhost:8088/AuScope-Portal/doBoreholeFilter.do?&serviceUrl=http://www.mrt.tas.gov.au:80/web-services/wfs"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"gml\":\""
                + dummyGml + "\"},\"success\":true}";
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream(dummyJSONResponse.length());
        final InputStream dummyJSONResponseIS2 = new ByteArrayInputStream(dummyJSONResponse.getBytes());

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)), with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                will(throwException(new Exception("Exception test")));
                oneOf(httpServiceCaller).getMethodResponseAsHttpResponse(with(any(HttpRequestBase.class)));
                will(delayReturnValue(100, new HttpClientResponse(new MyHttpResponse(dummyJSONResponseIS2), null)));

                allowing(mockServiceConfiguration).getServiceConfigurationItem(with(any(String.class)));
                will(returnValue(null));
            }
        });

        downloadController.downloadGMLAsZip(serviceUrls, mockHttpResponse,
                threadPool, null, outputFormat);
        Thread.sleep(500);

        dummyJSONResponseIS2.close();

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = null;
        String error = "Exception thrown while attempting to download from";
        int count = 0;
        while ((ze = zipInputStream.getNextEntry()) != null) {
            count++;
            if (ze.getName().equals("downloadInfo.txt")) {

                byte[] uncompressedData = new byte[error.getBytes().length];
                int dataRead = zipInputStream.read(uncompressedData);

                Assert.assertEquals(error.getBytes().length, dataRead);
                Assert.assertArrayEquals(error.getBytes(), uncompressedData);

            } else {
                Assert.assertTrue(ze.getName().endsWith(".xml"));
            }
        }
        Assert.assertEquals(2, count);
        zipInputStream.close();
    }

    /**
     * Test that this function makes all of the approriate calls, and see if it returns xml file zipped up
     */
    @Test
    public void testDownloadDataAsZipWithError() throws Exception {

        final String[] serviceUrls = {"http://someUrl"};
        final String dummyData = "dummyData";
        //final Header header = new BasicHeader("Content-Type", "text/xml");
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream(dummyData.length());

        final HttpResponse httpResponse = context.mock(HttpResponse.class);
        final HttpEntity httpEntity = context.mock(HttpEntity.class);
        final InputStream is = new ByteArrayInputStream(dummyData.getBytes());

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)),
                        with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getMethodResponseAsHttpResponse(
                        with(any(HttpRequestBase.class)));
                will(returnValue(new HttpClientResponse(httpResponse, null)));

                exactly(2).of(httpResponse).getEntity();
                will(returnValue(httpEntity));

                oneOf(httpEntity).getContentType();
                will(returnValue(null));

                oneOf(httpEntity).getContent();
                will(returnValue(is));
            }
        });

        downloadController.downloadDataAsZip(serviceUrls, "WMS_Layer_Download",
                mockHttpResponse);

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = null;
        while ((ze = zipInputStream.getNextEntry()) != null) {
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
     * Test that this function makes all of the approriate calls, and see if it returns png file zipped up
     *
     * @throws Exception
     */
    @Test
    public void testDownloadDataAsZipWithPNG() throws Exception {
        final String[] serviceUrls = {"http://someUrl"};
        final String dummyData = "dummyData";
        //final Header header = new BasicHeader("Content-Type", "image/png");
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream(dummyData.length());

        final HttpResponse httpResponse = context.mock(HttpResponse.class);
        final HttpEntity httpEntity = context.mock(HttpEntity.class);
        final InputStream is = new ByteArrayInputStream(dummyData.getBytes());

        context.checking(new Expectations() {
            {
                // setting of the headers for the return content
                oneOf(mockHttpResponse).setContentType(with(any(String.class)));
                oneOf(mockHttpResponse).setHeader(with(any(String.class)),
                        with(any(String.class)));
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(servletOutputStream));

                // calling the service
                oneOf(httpServiceCaller).getMethodResponseAsHttpResponse(
                        with(any(HttpRequestBase.class)));
                will(returnValue(new HttpClientResponse(httpResponse, null)));

                exactly(2).of(httpResponse).getEntity();
                will(returnValue(httpEntity));

                oneOf(httpEntity).getContentType();
                will(returnValue(null));

                oneOf(httpEntity).getContent();
                will(returnValue(is));

            }
        });

        downloadController.downloadDataAsZip(serviceUrls, "WMS_Layer_Download",
                mockHttpResponse);

        // check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = null;
        while ((ze = zipInputStream.getNextEntry()) != null) {

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
