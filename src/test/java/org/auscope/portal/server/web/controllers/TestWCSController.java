package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.domain.wcs.DescribeCoverageRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.IWCSDescribeCoverageMethodMaker;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker;
import org.auscope.portal.server.web.WCSDescribeCoverageMethodMakerGET;
import org.auscope.portal.server.web.WCSGetCoverageMethodMakerGET;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

public class TestWCSController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private IWCSGetCoverageMethodMaker mockGetMethodMaker = context.mock(WCSGetCoverageMethodMakerGET.class);
    private IWCSDescribeCoverageMethodMaker mockDescribeMethodMaker = context.mock(WCSDescribeCoverageMethodMakerGET.class);
    private PortalPropertyPlaceholderConfigurer mockHostConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
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
    
    /**
     * This sets up expectations so a call to downloadWCSAsZip will return succesfully (assuming correct inputs)
     * 
     * and populate outStream with dataToBeReturned
     */
    private void setupWCSDownloadAsZip(final byte[] dataToBeReturned) throws Exception {
        
        outStream = new MyServletOutputStream();
        
        context.checking(new Expectations() {{
            //Our method maker call should be passed all the correct variables
            allowing(mockGetMethodMaker).makeMethod(with(any(String.class)), 
                    with(any(String.class)), 
                    with(any(String.class)),
                    with(any(String.class)),
                    with(any(Integer.class)), 
                    with(any(Integer.class)), 
                    with(any(Double.class)), 
                    with(any(Double.class)), 
                    with(any(String.class)),
                    with(any(CSWGeographicBoundingBox.class)),
                    with(any(String.class)),
                    with(any(Map.class)));will(returnValue(mockMethod));
            
            //There MUST be a call to release connection
            oneOf(mockMethod).releaseConnection();
            
            //This will return an input stream to our fake geotiff data
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(new ByteArrayInputStream(dataToBeReturned)));
        
            //This is so we can inject our own fake output stream so we can inspect the result
            oneOf(mockResponse).getOutputStream();will(returnValue(outStream));
            oneOf(mockResponse).setContentType("application/zip");
            allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
            
            allowing(mockServiceCaller).getHttpClient();
         }});
    }
   
    
    @Test
    public void testBadTimePositions() throws Exception {
        try {
            final String[] timePositions = new String[] {"1986-10-09 12:34:56 FAIL"};
            WCSController controller = new WCSController(mockServiceCaller, mockGetMethodMaker, mockDescribeMethodMaker, mockHostConfigurer);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, 0, 0, "outputCrs", 1, 2, 3, 4, timePositions, null, null, null, null , mockResponse);
            Assert.fail("Should've failed to parse time");
        } catch (ParseException ex) { }
        
        try {
            final String[] timePositions = new String[] {"1986-10-09 12:99:56"};
            WCSController controller = new WCSController(mockServiceCaller, mockGetMethodMaker, mockDescribeMethodMaker, mockHostConfigurer);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, 0, 0, "outputCrs", 1, 2, 3, 4, timePositions, null, null, null, null , mockResponse);
            Assert.fail("Should've failed to parse time");
        } catch (ParseException ex) { }
    }
    
    @Test
    public void testBadCustomParams() throws Exception {
        try {
            final String[] customParamValue = new String[] {"param1=1/a/3", "param2=4", "param1=5"};
            WCSController controller = new WCSController(mockServiceCaller, mockGetMethodMaker, mockDescribeMethodMaker, mockHostConfigurer);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, 0, 0, "outputCrs", 1, 2, 3, 4, null, null, null, null, customParamValue , mockResponse);
            Assert.fail("Should've failed to parse custom params");
        } catch (IllegalArgumentException ex) { }
        
        try {
            final String[] customParamValue = new String[] {"param1=1/2/3", "param2=a", "param1=5"};
            WCSController controller = new WCSController(mockServiceCaller, mockGetMethodMaker, mockDescribeMethodMaker, mockHostConfigurer);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, 0, 0, "outputCrs", 1, 2, 3, 4, null, null, null, null, customParamValue , mockResponse);
            Assert.fail("Should've failed to parse custom params");
        } catch (IllegalArgumentException ex) { }
            
        try {
            final String[] customParamValue = new String[] {"param1=a/2/3", "param2=2", "param1=5"};
            WCSController controller = new WCSController(mockServiceCaller, mockGetMethodMaker, mockDescribeMethodMaker, mockHostConfigurer);
            controller.downloadWCSAsZip("url", "layer", "GeoTIFF", "inputCrs", 1, 1, 0, 0, "outputCrs", 1, 2, 3, 4, null, null, null, null, customParamValue , mockResponse);
            Assert.fail("Should've failed to parse custom params");
        } catch (IllegalArgumentException ex) { }
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
        final double outputResX = 0;
        final double outputResY = 0;
        final double northBoundLat = 0.1;
        final double southBoundLat = -0.2;
        final double eastBoundLng = 0.3;
        final double westBoundLng = -0.4;
        final byte[] geotiffData = new byte[] {0,1,2};
        final String[] timePositions = null;
        final String timePeriodFrom = null;
        final String timePeriodTo = null;
        final String timePeriodResolution = null;
        final String[] customParamValue = new String[] {"param1=1/2/3", "param2=4", "param1=5"};
        
        setupWCSDownloadAsZip(geotiffData);
        
        //This is so we can intercept the custom params to ensure they are generated as per expectations
        IWCSGetCoverageMethodMaker methodInterceptor = new IWCSGetCoverageMethodMaker() {
            
            @Override
            public HttpMethodBase makeMethod(String serviceURL, String layerName,
                    String format, String outputCrs, int outputWidth, int outputHeight,
                    double outputResX, double outputResY, String inputCrs,
                    CSWGeographicBoundingBox bbox, String timeConstraint,
                    Map<String, String> customParams) throws Exception {
                
                Assert.assertEquals("1/2/3,5", customParams.get("param1"));
                Assert.assertEquals("4", customParams.get("param2") );
                Assert.assertNull(customParams.get("paramDNE"));
                
                return mockGetMethodMaker.makeMethod(serviceUrl, layerName,
                        format, outputCrs, outputWidth, outputHeight,
                        outputResX, outputResY, inputCrs, bbox, timeConstraint,
                        customParams);
            }
        };
        
        WCSController controller = new WCSController(mockServiceCaller, methodInterceptor , mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, outputWidth, outputHeight, outputResX, outputResY, outputCrs, northBoundLat, southBoundLat, eastBoundLng, westBoundLng, timePositions, timePeriodFrom, timePeriodTo,timePeriodResolution, customParamValue , mockResponse);
    }
    
    @Test
    public void testGeotiffBbox() throws Exception {
        final String serviceUrl = "serviceUrl";
        final String layerName = "layerName";
        final String format = "GeoTIFF";
        final String outputCrs = "outputCrs";
        final String inputCrs = "inputCrs";
        final int outputWidth = 2;
        final int outputHeight = 1;
        final double outputResX = 0;
        final double outputResY = 0;
        final double northBoundLat = 0.1;
        final double southBoundLat = -0.2;
        final double eastBoundLng = 0.3;
        final double westBoundLng = -0.4;
        final byte[] geotiffData = new byte[] {0,1,2};
        final String[] timePositions = null;
        final String timePeriodFrom = null;
        final String timePeriodTo = null;
        final String timePeriodResolution = null;
        final String[] customParamValue = null;
        
        setupWCSDownloadAsZip(geotiffData);
        
        //This is so we can intercept the custom params to ensure they are generated as per expectations
        IWCSGetCoverageMethodMaker methodInterceptor = new IWCSGetCoverageMethodMaker() {
            
            @Override
            public HttpMethodBase makeMethod(String serviceURL, String layerName,
                    String format, String outputCrs, int outputWidth, int outputHeight,
                    double outputResX, double outputResY, String inputCrs,
                    CSWGeographicBoundingBox bbox, String timeConstraint,
                    Map<String, String> customParams) throws Exception {
                
                Assert.assertEquals(northBoundLat, bbox.getNorthBoundLatitude(), 0.00001);
                Assert.assertEquals(southBoundLat, bbox.getSouthBoundLatitude(), 0.00001);
                Assert.assertEquals(eastBoundLng, bbox.getEastBoundLongitude(), 0.00001);
                Assert.assertEquals(westBoundLng, bbox.getWestBoundLongitude(), 0.00001);
                
                return mockGetMethodMaker.makeMethod(serviceUrl, layerName,
                        format, outputCrs, outputWidth, outputHeight,
                        outputResX, outputResY, inputCrs, bbox, timeConstraint,
                        customParams);
            }
        };
        
        WCSController controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, outputWidth, outputHeight, outputResX, outputResY, outputCrs, northBoundLat, southBoundLat, eastBoundLng, westBoundLng, timePositions, timePeriodFrom, timePeriodTo,timePeriodResolution, customParamValue , mockResponse);
        
        ZipInputStream zip = outStream.getZipInputStream();
        ZipEntry ze = zip.getNextEntry();
        
        Assert.assertNotNull(ze);
        Assert.assertTrue(ze.getName().endsWith(".tiff"));
        
        byte[] uncompressedData = new byte[geotiffData.length];
        int dataRead = zip.read(uncompressedData);
        Assert.assertEquals(geotiffData.length, dataRead);
        Assert.assertArrayEquals(geotiffData, uncompressedData);
    }
    
    @Test
    public void testNetcdfTime() throws Exception {
        final String serviceUrl = "serviceUrl";
        final String layerName = "layerName";
        final String format = "NetCDF";
        final String outputCrs = "outputCrs";
        final String inputCrs = "inputCrs";
        final int outputWidth = 0;
        final int outputHeight = 0;
        final double outputResX = 2.9;
        final double outputResY = 2.2;
        final double northBoundLat = 0.1;
        final double southBoundLat = -0.2;
        final double eastBoundLng = 0.3;
        final double westBoundLng = -0.4;
        final String[] timePositions = new String[] {"1986-10-09 12:34:56 GMT", "1986-05-29 12:34:56 GMT"};
        final String wcsTime = "1986-10-09T12:34:56Z,1986-05-29T12:34:56Z";
        final String timePeriodFrom = null;
        final String timePeriodTo = null;
        final String timePeriodResolution = null;
        final String[] customParams = null;
        final byte[] netCdfData = new byte[] {4,1,2};
        
        setupWCSDownloadAsZip(netCdfData);
        
        //This is so we can intercept the custom params to ensure they are generated as per expectations
        IWCSGetCoverageMethodMaker methodInterceptor = new IWCSGetCoverageMethodMaker() {
            
            @Override
            public HttpMethodBase makeMethod(String serviceURL, String layerName,
                    String format, String outputCrs, int outputWidth, int outputHeight,
                    double outputResX, double outputResY, String inputCrs,
                    CSWGeographicBoundingBox bbox, String timeConstraint,
                    Map<String, String> customParams) throws Exception {
                
                Assert.assertEquals(wcsTime, timeConstraint);
                
                return mockGetMethodMaker.makeMethod(serviceUrl, layerName,
                        format, outputCrs, outputWidth, outputHeight,
                        outputResX, outputResY, inputCrs, bbox, timeConstraint,
                        customParams);
            }
        };
        
        WCSController controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "NetCDF", inputCrs, outputWidth, outputHeight, outputResX, outputResY, outputCrs, northBoundLat, southBoundLat, eastBoundLng, westBoundLng, timePositions, timePeriodFrom, timePeriodTo,timePeriodResolution, customParams , mockResponse);
        
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
        final String xmlResponse = org.auscope.portal.Util.loadXML("src/test/resources/DescribeCoverageResponse1.xml");
        
        context.checking(new Expectations() {{
            //Our method maker call should be passed all the correct variables
            oneOf(mockDescribeMethodMaker).makeMethod(
                    with(serviceUrl), 
                    with(layerName));will(returnValue(mockMethod));
            
            oneOf(mockMethod).getResponseBodyAsString();will(returnValue(xmlResponse));
            
            oneOf(mockServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(xmlResponse));
            oneOf(mockServiceCaller).getHttpClient();
         }});
        
        WCSController controller = new WCSController(mockServiceCaller, mockGetMethodMaker, mockDescribeMethodMaker, mockHostConfigurer);
        ModelAndView mav = controller.describeCoverage(serviceUrl, layerName);
        
        Assert.assertNotNull(mav);
        Map<String, Object> model = mav.getModel();
        
        Assert.assertEquals(true, model.get("success"));
        
        Assert.assertNotNull(model.get("records"));
        Assert.assertEquals(1, ((DescribeCoverageRecord[]) model.get("records")).length);
        Assert.assertNotNull(((DescribeCoverageRecord[]) model.get("records"))[0]);
    }
    
    private class TimeComparatorMethodInterceptor implements IWCSGetCoverageMethodMaker {

    	private String expectedTimeString;
    	
    	public TimeComparatorMethodInterceptor(String expectedTimeString) {
    		this.expectedTimeString = expectedTimeString;
    	}
    	
		@Override
		public HttpMethodBase makeMethod(String serviceURL, String layerName,
				String format, String outputCrs, int outputWidth,
				int outputHeight, double outputResX, double outputResY,
				String inputCrs, CSWGeographicBoundingBox bbox,
				String timeConstraint, Map<String, String> customParams)
				throws Exception {
			
			Assert.assertEquals(expectedTimeString, timeConstraint);
			
			return mockGetMethodMaker.makeMethod(serviceURL, layerName,
                    format, outputCrs, outputWidth, outputHeight,
                    outputResX, outputResY, inputCrs, bbox, timeConstraint,
                    customParams);
		}
    	
    }
    
    @Test
    public void testParseTimeZones() throws Exception {
        final String serviceUrl = "serviceUrl";
        final String layerName = "layerName";
        final String format = "GeoTIFF";
        final String outputCrs = "outputCrs";
        final String inputCrs = "inputCrs";
        final byte[] geotiffData = new byte[] {0,1,2};
        
        
        //Test we can parse UTC
        setupWCSDownloadAsZip(geotiffData);
        String[] inputTimes = new String[] {"2010-01-02 11:22:33 UTC"};
        IWCSGetCoverageMethodMaker methodInterceptor = new TimeComparatorMethodInterceptor("2010-01-02T11:22:33Z");
        WCSController controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, 256, 256, 0, 0, outputCrs, 0, 0, 0, 0, inputTimes, null, null,null, null, mockResponse);
       
        //Test we can parse GMT
        setupWCSDownloadAsZip(geotiffData);
        inputTimes = new String[] {"2011-05-06 11:22:33 GMT"};
        methodInterceptor = new TimeComparatorMethodInterceptor("2011-05-06T11:22:33Z");
        controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, 256, 256, 0, 0, outputCrs, 0, 0, 0, 0, inputTimes, null, null,null, null, mockResponse);
        
        //Test we can parse another timezone
        setupWCSDownloadAsZip(geotiffData);
        inputTimes = new String[] {"2010-03-04 12:22:33 WST"};
        methodInterceptor = new TimeComparatorMethodInterceptor("2010-03-04T04:22:33Z");
        controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, 256, 256, 0, 0, outputCrs, 0, 0, 0, 0, inputTimes, null, null,null, null, mockResponse);
        
        //Test we can parse another timezone
        setupWCSDownloadAsZip(geotiffData);
        inputTimes = new String[] {"2010-03-04 12:22:33 -0500"};
        methodInterceptor = new TimeComparatorMethodInterceptor("2010-03-04T17:22:33Z");
        controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, 256, 256, 0, 0, outputCrs, 0, 0, 0, 0, inputTimes, null, null,null, null, mockResponse);
        
        //Test we can parse another timezone (Using from, to)
        setupWCSDownloadAsZip(geotiffData);
        String startTime = "2010-03-04 12:22:33 +0500";
        String endTime = "2011-04-05 01:55:44 UTC";
        methodInterceptor = new TimeComparatorMethodInterceptor("2010-03-04T07:22:33Z/2011-04-05T01:55:44Z");
        controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, 256, 256, 0, 0, outputCrs, 0, 0, 0, 0, null,startTime, endTime,null, null, mockResponse);
        
        setupWCSDownloadAsZip(geotiffData);
        startTime = "2010-09-14 12:22:33 GMT";
        endTime = "2011-04-05 01:55:44 -0901";
        methodInterceptor = new TimeComparatorMethodInterceptor("2010-09-14T12:22:33Z/2011-04-05T10:56:44Z");
        controller = new WCSController(mockServiceCaller, methodInterceptor, mockDescribeMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, 256, 256, 0, 0, outputCrs, 0, 0, 0, 0, null,startTime, endTime,null, null, mockResponse);
        
    }
}
