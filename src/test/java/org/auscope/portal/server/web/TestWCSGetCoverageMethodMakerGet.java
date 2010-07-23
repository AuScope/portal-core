package org.auscope.portal.server.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.junit.Assert;

public class TestWCSGetCoverageMethodMakerGet {
    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private CSWGeographicBoundingBox mockBbox = context.mock(CSWGeographicBoundingBox.class);
    
    private WCSGetCoverageMethodMakerGET methodMaker;
    
    @Before
    public void setup() {
        methodMaker = new WCSGetCoverageMethodMakerGET();
        
        context.checking(new Expectations() {{
           allowing(mockBbox).getEastBoundLongitude();will(returnValue((double)1));
           allowing(mockBbox).getWestBoundLongitude();will(returnValue((double)2));
           allowing(mockBbox).getSouthBoundLatitude();will(returnValue((double)3));
           allowing(mockBbox).getNorthBoundLatitude();will(returnValue((double)4));
        }});
    }
    
    @Test
    public void testBbox() throws Exception {
        HttpMethodBase method = methodMaker.makeMethod("foo", "foo", "GeoTIFF", "foo", 1, 2, 0, 0, "myCrs", mockBbox, null, null);
        
        Assert.assertNotNull(method);
        
        String queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        Assert.assertTrue(queryString.contains("bbox=1.000000%2C3.000000%2C2.000000%2C4.000000"));
        Assert.assertTrue(queryString.contains("crs=myCrs"));
    }
    
    @Test
    public void testTime() throws Exception {
        HttpMethodBase method = methodMaker.makeMethod("foo", "foo", "GeoTIFF", "foo", 1, 1, 0, 0, "myCrs", mockBbox, "thetimeis", null);
        
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
        runOptionTest("response_crs", null, "foo", "foo", "GeoTIFF", "", 1, 2, 0, 0,"incrs",null, "time", null);
        
        //Testing width /height
        runOptionTest("resx", "width", "foo", "foo", "GeoTIFF", "", 1, 2, 0, 0,"incrs",null, "time", customParams);
        runOptionTest("resy", "height", "foo", "foo", "GeoTIFF", "", 1, 2, 0, 0,"incrs",null, "time", null);
        runOptionTest("width", "resx", "foo", "foo", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", customParams);
        runOptionTest("height", "resy", "foo", "foo", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", null);
        
        //Testing custom params
        runOptionTest(null, "param1=param1value", "foo", "foo", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", customParams);
        runOptionTest(null, "param2=param2value", "foo", "foo", "GeoTIFF", "", 0, 0, 1, 2,"incrs",null, "time", customParams);
    }
    
    @Test
    public void testBadArguments() throws Exception {
        try {
            methodMaker.makeMethod("foo", "foo", "GeoTIFF", "foo", 0, 0, 0, 0, "foo", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", "GeoTIFF", "foo", 5, 0, 0, 0, "foo", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", "GeoTIFF", "foo", 0, 5, 0, 0, "foo", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", "GeoTIFF", "foo", 0, 0, 5, 0, "foo", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", "GeoTIFF", "foo", 0, 0, 0, 5, "foo", mockBbox, "time", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
    }
    
}
