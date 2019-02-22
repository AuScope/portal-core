package org.auscope.portal.core.server.controllers;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.server.controllers.WCSController;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.DescribeCoverageRecord;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class TestWCSController extends PortalTestClass {

    private WCSService wcsService = context.mock(WCSService.class);
    private MyServletOutputStream outStream;

    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);

    /**
     * Needed so we can check the contents of our zip file after it is written
     */
    final class MyServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        public void write(int i) throws IOException {
            byteArrayOutputStream.write(i);
        }

        public ZipInputStream getZipInputStream() {
            return new ZipInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        }
    };

    @Test
    public void testBadTimePositions() throws Exception {
        try {
            final String[] timePositions = new String[] {"1986-10-09 12:34:56 FAIL"};
            WCSController controller = new WCSController(wcsService);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, null, null, "outputCrs", 1, 2, 3,
                    4, timePositions, null, null, null, null, "", mockResponse);
            Assert.fail("Should've failed to parse time");
        } catch (ParseException ex) {
        }

        try {
            final String[] timePositions = new String[] {"1986-10-09 12:99:56"};
            WCSController controller = new WCSController(wcsService);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, null, null, "outputCrs", 1, 2, 3,
                    4, timePositions, null, null, null, null, "", mockResponse);
            Assert.fail("Should've failed to parse time");
        } catch (ParseException ex) {
        }
    }

    @Test
    public void testBadCustomParams() throws Exception {
        try {
            final String[] customParamValue = new String[] {"param1=1/a/3", "param2=4", "param1=5"};
            WCSController controller = new WCSController(wcsService);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, null, null, "outputCrs", 1, 2, 3,
                    4, null, null, null, null, customParamValue, "", mockResponse);
            Assert.fail("Should've failed to parse custom params");
        } catch (IllegalArgumentException ex) {
        }

        try {
            final String[] customParamValue = new String[] {"param1=1/2/3", "param2=a", "param1=5"};
            WCSController controller = new WCSController(wcsService);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, null, null, "outputCrs", 1, 2, 3,
                    4, null, null, null, null, customParamValue, "", mockResponse);
            Assert.fail("Should've failed to parse custom params");
        } catch (IllegalArgumentException ex) {
        }

        try {
            final String[] customParamValue = new String[] {"param1=a/2/3", "param2=2", "param1=5"};
            WCSController controller = new WCSController(wcsService);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, null, null, "outputCrs", 1, 2, 3,
                    4, null, null, null, null, customParamValue, "", mockResponse);
            Assert.fail("Should've failed to parse custom params");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testCustomParams() throws Exception {
        final String serviceUrl = "serviceUrl";
        final String layerName = "layerName";
        final String format = "GeoTIFF";
        final String outputCrs = "outputCrs";
        final String inputCrs = "inputCrs";
        final int outputWidth = 2;
        final int outputHeight = 1;
        final Double outputResX = null;
        final Double outputResY = null;
        final double northBoundLat = 0.1;
        final double southBoundLat = -0.2;
        final double eastBoundLng = 0.3;
        final double westBoundLng = -0.4;
        final byte[] geotiffData = new byte[] {0, 1, 2};
        final String[] timePositions = null;
        final String timePeriodFrom = null;
        final String timePeriodTo = null;
        final String timePeriodResolution = null;
        final String[] customParamValue = new String[] {"param1=1/2/3", "param2=4", "param1=5"};

        outStream = new MyServletOutputStream();

        context.checking(new Expectations() {
            {
                //Our method maker call should be passed all the correct variables
                oneOf(wcsService).getCoverage(with(serviceUrl),
                        with(layerName),
                        with(format),
                        with(equal(new Dimension(outputWidth, outputHeight))),
                        with((Resolution) null),
                        with(outputCrs),
                        with(inputCrs),
                        with(any(CSWGeographicBoundingBox.class)),
                        with((TimeConstraint) null),
                        with(aMap(new String[] {"param1", "param2"}, new String[] {"1/2/3,5", "4"})));
                will(returnValue(new ByteArrayInputStream(geotiffData)));

                //This is so we can inject our own fake output stream so we can inspect the result
                oneOf(mockResponse).getOutputStream();
                will(returnValue(outStream));
                oneOf(mockResponse).setContentType("application/zip");
                allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
            }
        });

        WCSController controller = new WCSController(wcsService);
        controller.downloadWCSAsZip(serviceUrl, layerName, format, inputCrs, outputWidth, outputHeight, outputResX,
                outputResY, outputCrs, northBoundLat, southBoundLat, eastBoundLng, westBoundLng, timePositions,
                timePeriodFrom, timePeriodTo, timePeriodResolution, customParamValue, "", mockResponse);
    }

    @Test
    public void testNetcdfTime() throws Exception {
        final String serviceUrl = "serviceUrl";
        final String layerName = "layerName";
        final String format = "NetCDF";
        final String outputCrs = "outputCrs";
        final String inputCrs = "inputCrs";
        final Integer outputWidth = null;
        final Integer outputHeight = null;
        final double outputResX = 2.9;
        final double outputResY = 2.2;
        final double northBoundLat = 0.1;
        final double southBoundLat = -0.2;
        final double eastBoundLng = 0.3;
        final double westBoundLng = -0.4;
        final String[] timePositions = new String[] {"1986-10-09 12:34:56 GMT", "1986-05-29 12:34:56 GMT"};
        final TimeConstraint wcsTime = new TimeConstraint("1986-10-09T12:34:56Z,1986-05-29T12:34:56Z");
        final String timePeriodFrom = null;
        final String timePeriodTo = null;
        final String timePeriodResolution = null;
        final String[] customParams = null;
        final byte[] netCdfData = new byte[] {4, 1, 2};

        outStream = new MyServletOutputStream();

        context.checking(new Expectations() {
            {
                //Our method maker call should be passed all the correct variables
                oneOf(wcsService).getCoverage(with(serviceUrl),
                        with(equal(layerName)),
                        with(equal(format)),
                        with(equal((Dimension) null)),
                        //with(any(Dimension.class)),
                        with(equal(new Resolution(outputResX, outputResY))),
                        //with(any(Resolution.class)),
                        with(equal(outputCrs)),
                        with(equal(inputCrs)),
                        with(any(CSWGeographicBoundingBox.class)),
                        with(equal(wcsTime)),
                        //with(any(TimeConstraint.class)),
                        with(any(Map.class)));
                will(returnValue(new ByteArrayInputStream(netCdfData)));

                //This is so we can inject our own fake output stream so we can inspect the result
                oneOf(mockResponse).getOutputStream();
                will(returnValue(outStream));
                oneOf(mockResponse).setContentType("application/zip");
                allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
            }
        });

        WCSController controller = new WCSController(wcsService);
        controller.downloadWCSAsZip(serviceUrl, layerName, format, inputCrs, outputWidth, outputHeight, outputResX,
                outputResY, outputCrs, northBoundLat, southBoundLat, eastBoundLng, westBoundLng, timePositions,
                timePeriodFrom, timePeriodTo, timePeriodResolution, customParams, "", mockResponse);

        ZipInputStream zip = outStream.getZipInputStream();
        ZipEntry ze = zip.getNextEntry();

        Assert.assertNotNull(ze);
        Assert.assertTrue(ze.getName().endsWith(".nc"));

        byte[] uncompressedData = new byte[netCdfData.length];
        int dataRead = zip.read(uncompressedData);
        Assert.assertEquals(netCdfData.length, dataRead);
        Assert.assertArrayEquals(netCdfData, uncompressedData);
    }

    @Test
    public void testDescribeCoverageSuccess() throws Exception {
        final String serviceUrl = "http://fake.com/bob";
        final String layerName = "layer_name";
        final DescribeCoverageRecord[] records = new DescribeCoverageRecord[0];

        context.checking(new Expectations() {
            {
                oneOf(wcsService).describeCoverage(serviceUrl, layerName);
                will(returnValue(records));
            }
        });

        WCSController controller = new WCSController(wcsService);
        ModelAndView mav = controller.describeCoverage(serviceUrl, layerName);

        Assert.assertNotNull(mav);
        Map<String, Object> model = mav.getModel();

        Assert.assertEquals(true, model.get("success"));
        Assert.assertSame(records, model.get("data"));
    }
}
