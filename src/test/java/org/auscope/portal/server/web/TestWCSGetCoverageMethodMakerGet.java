package org.auscope.portal.server.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestWCSGetCoverageMethodMakerGet {
    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private CSWGeographicBoundingBox mockBbox = context.mock(CSWGeographicBoundingBox.class, "simpleBbox");
    private CSWGeographicBoundingBox mockAntiMeridianBbox = context.mock(CSWGeographicBoundingBox.class, "amBbox");
    private CSWGeographicBoundingBox mockMeridianBbox = context.mock(CSWGeographicBoundingBox.class, "mBox");
    
    private WCSGetCoverageMethodMakerGET methodMaker;
    
    @Before
    public void setup() {
        methodMaker = new WCSGetCoverageMethodMakerGET();
        
        context.checking(new Expectations() {{
           allowing(mockBbox).getEastBoundLongitude();will(returnValue((double)1));
           allowing(mockBbox).getWestBoundLongitude();will(returnValue((double)2));
           allowing(mockBbox).getSouthBoundLatitude();will(returnValue((double)3));
           allowing(mockBbox).getNorthBoundLatitude();will(returnValue((double)4));
           
           allowing(mockAntiMeridianBbox).getEastBoundLongitude();will(returnValue((double)1));
           allowing(mockAntiMeridianBbox).getWestBoundLongitude();will(returnValue((double)-145));
           allowing(mockAntiMeridianBbox).getSouthBoundLatitude();will(returnValue((double)-50));
           allowing(mockAntiMeridianBbox).getNorthBoundLatitude();will(returnValue((double)60));
           
           allowing(mockMeridianBbox).getEastBoundLongitude();will(returnValue((double)77));
           allowing(mockMeridianBbox).getWestBoundLongitude();will(returnValue((double)-56));
           allowing(mockMeridianBbox).getSouthBoundLatitude();will(returnValue((double)-50));
           allowing(mockMeridianBbox).getNorthBoundLatitude();will(returnValue((double)60));
        }});
    }
    
    @Test
    public void testBbox() throws Exception {
        HttpMethodBase method = methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 1, 2, 0, 0, "myCrs", mockBbox, null, null);
        
        Assert.assertNotNull(method);
        
        String queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        Assert.assertTrue(queryString.contains("bbox=1.000000%2C3.000000%2C2.000000%2C4.000000"));
        Assert.assertTrue(queryString.contains("crs=myCrs"));
    }
    
    @Test
    public void testTime() throws Exception {
        HttpMethodBase method = methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 1, 1, 0, 0, "myCrs", mockBbox, "thetimeis", null);
        
        Assert.assertNotNull(method);
        
        String queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        Assert.assertTrue(queryString.contains("time=thetimeis"));
    }
    
    private void runOptionTest(String notToContain, String mustContain,String serviceURL, String layerName,
            String format, String outputCrs, int outputWidth, int outputHeight,
            int outputResX, int outputResY, String inputCrs,CSWGeographicBoundingBox bbox, String timeConstraint, 
            Map<String, String> customParams) throws Exception {
        
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, layerName, format, outputCrs, outputWidth, outputHeight, outputResX, outputResY, inputCrs, bbox, timeConstraint, customParams);
        Assert.assertNotNull(method);
        
        String queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        if (notToContain != null) 
            Assert.assertFalse(queryString.contains(notToContain));
        
        if (mustContain != null)
            Assert.assertTrue(queryString.contains(mustContain));
    }
    
    @Test
    public void testOptionalArguments() throws Exception {
        
        Map<String, String> customParams = new HashMap<String, String>();
        customParams.put("param1", "param1value");
        customParams.put("param2", "param2value");
        
        //Testing optional output crs
        runOptionTest("response_crs", null, "http://example.com/wcs", "layerName", "GeoTIFF", "", 1, 2, 0, 0,"incrs",null, "time", null);
        
        //Testing width /height
        runOptionTest("resx", "width", "http://example.com/wcs", "layerName", "GeoTIFF", "", 1, 2, 0, 0,"incrs",null, "time", customParams);
        runOptionTest("resy", "height", "http://example.com/wcs", "layerName", "GeoTIFF", "", 1, 2, 0, 0,"incrs",null, "time", null);
        runOptionTest("width", "resx", "http://example.com/wcs", "layerName", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", customParams);
        runOptionTest("height", "resy", "http://example.com/wcs", "layerName", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", null);
        
        //Testing custom params
        runOptionTest(null, "param1=param1value", "http://example.com/wcs", "layerName", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", customParams);
        runOptionTest(null, "param2=param2value", "http://example.com/wcs", "layerName", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", customParams);
    }
    
    @Test
    public void testBadArguments() throws Exception {
        try {
            methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 0, 0, 0, 0, "inputCrs", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 5, 0, 0, 0, "inputCrs", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 0, 5, 0, 0, "inputCrs", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 0, 0, 5, 0, "inputCrs", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 0, 0, 0, 5, "inputCrs", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
    }
    
    private void compareBboxesInQuery(String queryString, double expectedNorth, double expectedSouth, double expectedEast, double expectedWest) {
    	Scanner sc = new Scanner(queryString);
    	
    	//Extract our param list as a list of doubles
        String bboxParams = sc.findInLine("&bbox=.*?&");
        bboxParams = bboxParams.split("=")[1];
        bboxParams = bboxParams.replace("&", "");
        sc = new Scanner(bboxParams).useDelimiter("%2C");
        
        Assert.assertTrue(sc.hasNextDouble());
        double minx = sc.nextDouble();
        Assert.assertTrue(sc.hasNextDouble());
        double miny = sc.nextDouble();
        Assert.assertTrue(sc.hasNextDouble());
        double maxx = sc.nextDouble();
        Assert.assertTrue(sc.hasNextDouble());
        double maxy = sc.nextDouble();
        
        Assert.assertEquals(expectedNorth, maxy, 0.01);
        Assert.assertEquals(expectedSouth, miny, 0.01);
        
        Assert.assertEquals(expectedWest, minx, 0.01);
        Assert.assertEquals(expectedEast, maxx, 0.01);
    }
    
    /**
     * This test case is to ensure we correctly map North, South, East, West ordinates to a bounding box defined
     * ambiguously as MINX-MAXX and MINY-MAXY 
     * @throws Exception
     */
    @Test
    public void testBboxMeridians() throws Exception {
    	HttpMethodBase method = methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 1, 2, 0, 0, "myCrs", mockAntiMeridianBbox, null, null);
    	
    	String queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        //Because this crosses the anti meridian we adjust longitude from [-180, 180] to [0, 360] to remove ambiguity 
        //about which way the bbox will wrap around the earth
        compareBboxesInQuery(queryString, 60, -50, 325, 1);
        
        method = methodMaker.makeMethod("http://example.com/wcs", "layerName", "GeoTIFF", "outputCrs", 1, 2, 0, 0, "myCrs", mockMeridianBbox, null, null);
    	queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        //Because this crosses the meridian we adjust longitude from [-180, 180] to [0, 360] to remove ambiguity 
        //about which way the bbox will wrap around the earth
        compareBboxesInQuery(queryString, 60, -50, 236, 77);
    }
    
}
