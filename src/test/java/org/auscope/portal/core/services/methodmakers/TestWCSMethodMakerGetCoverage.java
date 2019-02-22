package org.auscope.portal.core.services.methodmakers;

import java.awt.Dimension;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestWCSMethodMakerGetCoverage extends PortalTestClass {

    private CSWGeographicBoundingBox mockBbox = context.mock(CSWGeographicBoundingBox.class, "simpleBbox");
    private CSWGeographicBoundingBox mockAntiMeridianBbox = context.mock(CSWGeographicBoundingBox.class, "amBbox");
    private CSWGeographicBoundingBox mockMeridianBbox = context.mock(CSWGeographicBoundingBox.class, "mBox");

    private WCSMethodMaker methodMaker;

    @Before
    public void setUp() {
        methodMaker = new WCSMethodMaker();

        context.checking(new Expectations() {
            {
                allowing(mockBbox).getEastBoundLongitude();
                will(returnValue((double) 1));
                allowing(mockBbox).getWestBoundLongitude();
                will(returnValue((double) 2));
                allowing(mockBbox).getSouthBoundLatitude();
                will(returnValue((double) 3));
                allowing(mockBbox).getNorthBoundLatitude();
                will(returnValue((double) 4));

                allowing(mockAntiMeridianBbox).getEastBoundLongitude();
                will(returnValue((double) 1));
                allowing(mockAntiMeridianBbox).getWestBoundLongitude();
                will(returnValue((double) -145));
                allowing(mockAntiMeridianBbox).getSouthBoundLatitude();
                will(returnValue((double) -50));
                allowing(mockAntiMeridianBbox).getNorthBoundLatitude();
                will(returnValue((double) 60));

                allowing(mockMeridianBbox).getEastBoundLongitude();
                will(returnValue((double) 77));
                allowing(mockMeridianBbox).getWestBoundLongitude();
                will(returnValue((double) -56));
                allowing(mockMeridianBbox).getSouthBoundLatitude();
                will(returnValue((double) -50));
                allowing(mockMeridianBbox).getNorthBoundLatitude();
                will(returnValue((double) 60));
            }
        });
    }

    @Test
    public void testBbox() throws URISyntaxException {
        Dimension outputSize = new Dimension(1, 2);
        Resolution outputResolution = null;
        TimeConstraint timeConstraint = null;
        HttpRequestBase method = methodMaker.getCoverageMethod("http://example.com/wcs", "layerName", "GeoTIFF",
                "outputCrs", outputSize, outputResolution, "myCrs", mockBbox, timeConstraint, null);

        Assert.assertNotNull(method);

        String queryString = method.getURI().getQuery();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());

        Assert.assertTrue(queryString.contains("bbox=1.000000,3.000000,2.000000,4.000000"));
        Assert.assertTrue(queryString.contains("crs=myCrs"));
    }

    @Test
    public void testTime() throws URISyntaxException {
        Dimension outputSize = new Dimension(1, 1);
        Resolution outputResolution = null;
        TimeConstraint timeConstraint = new TimeConstraint("thetimeis");
        HttpRequestBase method = methodMaker.getCoverageMethod("http://example.com/wcs", "layerName", "GeoTIFF",
                "outputCrs", outputSize, outputResolution, "myCrs", mockBbox, timeConstraint, null);

        Assert.assertNotNull(method);

        String queryString = method.getURI().getQuery();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());

        Assert.assertTrue(queryString.contains("time=thetimeis"));
    }

    private void runOptionTest(String notToContain, String mustContain, String serviceUrl, String layerName,
            String format, String outputCrs, Dimension outputSize,
            Resolution outputResolution, String inputCrs, CSWGeographicBoundingBox bbox, TimeConstraint timeConstraint,
            Map<String, String> customParams) throws URISyntaxException {

        HttpRequestBase method = methodMaker.getCoverageMethod(serviceUrl, layerName, format, outputCrs, outputSize,
                outputResolution, inputCrs, bbox, timeConstraint, customParams);
        Assert.assertNotNull(method);

        String queryString = method.getURI().getQuery();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());

        if (notToContain != null)
            Assert.assertFalse(queryString.contains(notToContain));

        if (mustContain != null)
            Assert.assertTrue(queryString.contains(mustContain));
    }

    @Test
    public void testOptionalArguments() throws URISyntaxException {
        Map<String, String> customParams = new HashMap<>();
        customParams.put("param1", "param1value");
        customParams.put("param2", "param2value");

        //Testing optional output crs
        runOptionTest("response_crs", null, "http://example.com/wcs", "layerName", "GeoTIFF", "", new Dimension(1, 2),
                null, "incrs", null, new TimeConstraint("time"), null);

        //Testing width /height
        runOptionTest("resx", "width", "http://example.com/wcs", "layerName", "GeoTIFF", "", new Dimension(1, 2), null,
                "incrs", null, new TimeConstraint("time"), customParams);
        runOptionTest("resy", "height", "http://example.com/wcs", "layerName", "GeoTIFF", "", new Dimension(1, 2),
                null, "incrs", null, new TimeConstraint("time"), null);
        runOptionTest("width", "resx", "http://example.com/wcs", "layerName", "GeoTIFF", "", null,
                new Resolution(1, 2), "incrs", null, new TimeConstraint("time"), customParams);
        runOptionTest("height", "resy", "http://example.com/wcs", "layerName", "GeoTIFF", "", null,
                new Resolution(1, 2), "incrs", null, new TimeConstraint("time"), null);

        //Testing custom params
        runOptionTest(null, "param1=param1value", "http://example.com/wcs", "layerName", "GeoTIFF", "", null,
                new Resolution(1, 2), "incrs", null, new TimeConstraint("time"), customParams);
        runOptionTest(null, "param2=param2value", "http://example.com/wcs", "layerName", "GeoTIFF", "", null,
                new Resolution(1, 2), "incrs", null, new TimeConstraint("time"), customParams);
    }

    @Test
    public void testBadArguments() throws URISyntaxException {
        try {
            methodMaker.getCoverageMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", null, null,
                    "inputCrs", mockBbox, new TimeConstraint("time"), null);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            // empty
        }

        try {
            methodMaker.getCoverageMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", new Dimension(
                    0, 5), new Resolution(5, 0), "inputCrs", mockBbox, new TimeConstraint("time"), null);
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            // empty
        }
    }

    private static void compareBboxesInQuery(String queryString, double expectedNorth, double expectedSouth,
            double expectedEast, double expectedWest) {
        try (Scanner sc = new Scanner(queryString)) {
            // Extract our param list as a list of doubles
            String bboxParams = sc.findInLine("&bbox=.*?&");
            bboxParams = bboxParams.split("=")[1];
            bboxParams = bboxParams.replace("&", "");
            try (@SuppressWarnings("resource")
                Scanner sc2 = new Scanner(bboxParams).useDelimiter(",")) {

                Assert.assertTrue(sc2.hasNextDouble());
                double minx = sc2.nextDouble();
                Assert.assertTrue(sc2.hasNextDouble());
                double miny = sc2.nextDouble();
                Assert.assertTrue(sc2.hasNextDouble());
                double maxx = sc2.nextDouble();
                Assert.assertTrue(sc2.hasNextDouble());
                double maxy = sc2.nextDouble();

                Assert.assertEquals(expectedNorth, maxy, 0.01);
                Assert.assertEquals(expectedSouth, miny, 0.01);

                Assert.assertEquals(expectedWest, minx, 0.01);
                Assert.assertEquals(expectedEast, maxx, 0.01);
            }
        }
    }

    /**
     * This test case is to ensure we correctly map North, South, East, West ordinates to a bounding box defined ambiguously as MINX-MAXX and MINY-MAXY
     * @throws URISyntaxException 
     */
    @Test
    public void testBboxMeridians() throws URISyntaxException {
        HttpRequestBase method = methodMaker.getCoverageMethod("http://example.com/wcs", "layerName", "GeoTIFF",
                "outputCrs", new Dimension(1, 2), null, "myCrs", mockAntiMeridianBbox, null, null);

        String queryString = method.getURI().getQuery();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());

        //Because this crosses the anti meridian we adjust longitude from [-180, 180] to [0, 360] to remove ambiguity
        //about which way the bbox will wrap around the earth
        compareBboxesInQuery(queryString, 60, -50, 325, 1);

        method = methodMaker.getCoverageMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs",
                new Dimension(1, 2), null, "myCrs", mockMeridianBbox, null, null);
        queryString = method.getURI().getQuery();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());

        //Because this crosses the meridian we adjust longitude from [-180, 180] to [0, 360] to remove ambiguity
        //about which way the bbox will wrap around the earth
        compareBboxesInQuery(queryString, 60, -50, 236, 77);
    }

}
