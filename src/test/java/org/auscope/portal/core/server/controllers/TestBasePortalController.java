package org.auscope.portal.core.server.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.http.client.methods.HttpGet;
import org.auscope.portal.core.server.http.download.DownloadResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.FileIOUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for BasePortalController
 * 
 * @author Josh Vote
 *
 */
public class TestBasePortalController extends PortalTestClass {

    private class BasePortalControllerImpl extends BasePortalController {
        // empty
    }

    private BasePortalControllerImpl basePortalController = new BasePortalControllerImpl();

    /**
     * Asserts that a model and view matches the consistent pattern used throughout the portal
     * 
     * @param mav1
     * @param mav2
     */
    private static void assertModelAndViewConsistency(ModelAndView mav) {
        ModelMap map = mav.getModelMap();

        List<String> validKeys = Arrays.asList("success", "data", "msg", "debugInfo", "totalResults");

        //Ensure there are no erroneous keys
        for (String key : map.keySet()) {
            Assert.assertTrue("Invalid Key: " + key, validKeys.contains(key));
        }

        //Test success
        Assert.assertTrue("No success indicator", map.containsKey("success"));
        Object successObj = map.get("success");
        Assert.assertNotNull(successObj);
        Assert.assertTrue("Success object is not a Boolean", successObj instanceof Boolean);

        //Test some special cases of data
        if (map.containsKey("data")) {
            Object dataObj = map.get("data");
            if (dataObj != null && dataObj instanceof ModelMap) {
                ModelMap data = (ModelMap) dataObj;
                if (data.containsKey("gml") || data.containsKey("kml")) {
                    Assert.assertTrue("gml must be paired with kml", data.containsKey("gml"));
                    Assert.assertTrue("gml must be paired with kml", data.containsKey("kml"));
                    Assert.assertNotNull(data.get("gml"));
                    Assert.assertNotNull(data.get("kml"));
                }
            }
        }

        //Test some special cases of data
        if (map.containsKey("matchedResults")) {
            Assert.assertTrue("matchedResults must be an int", map.get("matchedResults") instanceof Integer);
        }
    }

    /**
     * Tests that BasePortalController has consistent return types for the various options
     */
    @Test
    public void testConsistencyOfResponses() {
        final String serviceUrl = "http://service/url";
        final ModelMap gmlKmlData = new ModelMap();
        final ModelMap debugInfo = new ModelMap();
        final HttpGet getMethod = new HttpGet(serviceUrl);
        final String message = "message string";
        gmlKmlData.put("gml", "gmlString");
        gmlKmlData.put("kml", "kmlString");

        assertModelAndViewConsistency(basePortalController
                .generateExceptionResponse(new ConnectException(), serviceUrl));
        assertModelAndViewConsistency(basePortalController.generateExceptionResponse(new ConnectException(),
                serviceUrl, getMethod));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(true, gmlKmlData, null));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(false, null, null));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(true, null, message));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(false, gmlKmlData, message));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(false, gmlKmlData, message,
                debugInfo));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, null, null));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, null, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, message));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, null, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, null));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, null, null));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, null, 45, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, message,
                debugInfo));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, "gmlString", "kmlString",
                getMethod));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, gmlKmlData, message, 43,
                debugInfo));
    }

    /**
     * Tests the responses are correctly written to a zip output stream
     * @throws IOException 
     */
    @Test
    public void writeResponseToZip() throws IOException {
        DownloadResponse dr1 = new DownloadResponse("http://example.org");
        String dr1Response = "dr1-response-contents";
        dr1.setResponseStream(new ByteArrayInputStream(dr1Response.getBytes()));

        DownloadResponse dr2 = new DownloadResponse("http://example.org");
        String dr2Response = "dr2-response-contents-data";
        dr2.setResponseStream(new ByteArrayInputStream(dr2Response.getBytes()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        ZipOutputStream zout = new ZipOutputStream(outputStream);

        //Write our data out
        FileIOUtil.writeResponseToZip(Arrays.asList(dr1, dr2), zout, false);
        zout.finish();
        zout.close();
        outputStream.close();

        //Collect the zipped data and ensure it was written correctly
        byte[] zipBytes = outputStream.toByteArray();
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes));

        zin.getNextEntry();
        byte[] zData = new byte[dr1Response.getBytes().length];
        Assert.assertEquals(zData.length, zin.read(zData));
        Assert.assertArrayEquals(dr1Response.getBytes(), zData);

        zin.getNextEntry();
        zData = new byte[dr2Response.getBytes().length];
        Assert.assertEquals(zData.length, zin.read(zData));
        Assert.assertArrayEquals(dr2Response.getBytes(), zData);
    }

    /**
     * Tests piping input->output works as expected
     * @throws IOException 
     */
    @Test
    public void testWriteInputToOutputStream() throws IOException {
        final int bufferSize = 134;
        try (final InputStream mockInput = context.mock(InputStream.class);
                final OutputStream outputStream = context.mock(OutputStream.class)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockInput).read(with(any(byte[].class)), with(equal(0)), with(equal(bufferSize)));
                    will(returnValue(bufferSize));

                    oneOf(mockInput).read(with(any(byte[].class)), with(equal(0)), with(equal(bufferSize)));
                    will(returnValue(12));

                    oneOf(mockInput).read(with(any(byte[].class)), with(equal(0)), with(equal(bufferSize)));
                    will(returnValue(-1));

                    allowing(mockInput).close();
                    oneOf(outputStream).write(with(any(byte[].class)), with(equal(0)), with(equal(bufferSize)));
                    oneOf(outputStream).write(with(any(byte[].class)), with(equal(0)), with(equal(12)));
                    allowing(outputStream).close();
                }
            });

            FileIOUtil.writeInputToOutputStream(mockInput, outputStream, bufferSize, false);
        }
    }

    /**
     * Tests piping input->output correctly closes input streams (where appropriate)
     * @throws IOException 
     */
    @Test
    public void testWriteInputToOutputStreamClosing() throws IOException {
        //VT: I removed the IOException expectation because if given an array of inputstream and 1 fail,
        //we should return the rest and encapsulate the exception in the file to inform users.
        final int bufferSize = 134;

        try (final InputStream mockInput = context.mock(InputStream.class);
                final OutputStream outputStream = context.mock(OutputStream.class)) {

            context.checking(new Expectations() {
                {
                    oneOf(mockInput).read(with(any(byte[].class)), with(equal(0)), with(equal(bufferSize)));
                    will(throwException(new IOException()));
                    oneOf(outputStream).write(with(any(byte[].class)));
                    allowing(mockInput).close();
                    allowing(outputStream).close();
                }
            });

            FileIOUtil.writeInputToOutputStream(mockInput, outputStream, bufferSize, true);
        }
    }

    /**
     * Tests piping input->output correctly closes input streams (where appropriate)
     * @throws IOException 
     */
    @Test
    public void testWriteInputToOutputStreamClosing2() throws IOException {
        final int bufferSize = 134;
        try (final InputStream mockInput = context.mock(InputStream.class);
                final OutputStream outputStream = context.mock(OutputStream.class)) {
            // VT: I removed the IOException expectation because if given an
            // array of inputstream and 1 fail,
            // we should return the rest and encapsulate the exception in the
            // file to inform users.
            context.checking(new Expectations() {
                {
                    oneOf(mockInput).read(with(any(byte[].class)), with(equal(0)), with(equal(bufferSize)));
                    will(throwException(new IOException()));
                    oneOf(outputStream).write(with(any(byte[].class)));
                    allowing(outputStream).close();
                    allowing(mockInput).close();
                }
            });

            FileIOUtil.writeInputToOutputStream(mockInput, outputStream, bufferSize, false);
        }
    }
}
