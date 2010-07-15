package org.auscope.portal.server.web;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker.WCSDownloadFormat;
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
        HttpMethodBase method = methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 1, 2, 0, 0, "myCrs", mockBbox);
        
        Assert.assertNotNull(method);
        
        String queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        Assert.assertTrue(queryString.contains("bbox=1.000000%2C3.000000%2C2.000000%2C4.000000"));
        Assert.assertTrue(queryString.contains("crs=myCrs"));
    }
    
    @Test
    public void testTime() throws Exception {
        HttpMethodBase method = methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 1, 2, 0, 0, "thetimeis");
        
        Assert.assertNotNull(method);
        
        String queryString = method.getQueryString();
        Assert.assertNotNull(queryString);
        Assert.assertFalse(queryString.isEmpty());
        
        Assert.assertTrue(queryString.contains("time=thetimeis"));
    }
    
    @Test
    public void testBadArguments() throws Exception {
        try {
            methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 0, 0, 0, 0, "foo", mockBbox);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 5, 6, 7, 8, "foo", mockBbox);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 5, 0, 0, 0, "foo", mockBbox);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 0, 5, 0, 0, "foo", mockBbox);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 0, 0, 5, 0, "foo", mockBbox);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 0, 0, 0, 5, "foo", mockBbox);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
        
        try {
            methodMaker.makeMethod("foo", "foo", WCSDownloadFormat.GeoTIFF, "foo", 1, 2, 3, 5, "foo", null);
            Assert.fail();
        } catch (IllegalArgumentException ex) { }
    }
    
}
