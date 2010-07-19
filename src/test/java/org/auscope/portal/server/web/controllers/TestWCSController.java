package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker;
import org.auscope.portal.server.web.WCSGetCoverageMethodMakerGET;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker.WCSDownloadFormat;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestWCSController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private IWCSGetCoverageMethodMaker mockMethodMaker = context.mock(WCSGetCoverageMethodMakerGET.class);
    private PortalPropertyPlaceholderConfigurer mockHostConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
    
    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
    
    
    @Before
    public void setup() {
        
    }
    
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
    public void testGeotiffBbox() throws Exception {
        final String serviceUrl = "serviceUrl";
        final String layerName = "layerName";
        final WCSDownloadFormat format = WCSDownloadFormat.GeoTIFF;
        final String outputCrs = "outputCrs";
        final String inputCrs = "inputCrs";
        final int outputWidth = 2;
        final int outputHeight = 1;
        final int outputResX = 0;
        final int outputResY = 0;
        final double northBoundLat = 0.1;
        final double southBoundLat = -0.2;
        final double eastBoundLng = 0.3;
        final double westBoundLng = -0.4;
        final byte[] geotiffData = new byte[] {0,1,2};
        final MyServletOutputStream outStream = new MyServletOutputStream();
        
        context.checking(new Expectations() {{
            //Our method maker call should be passed all the correct variables
            allowing(mockMethodMaker).makeMethod(with(any(String.class)), 
                    with(any(String.class)), 
                    with(any(WCSDownloadFormat.class)),
                    with(any(String.class)),
                    with(any(Integer.class)), 
                    with(any(Integer.class)), 
                    with(any(Integer.class)), 
                    with(any(Integer.class)), 
                    with(any(String.class)),
                    with(any(CSWGeographicBoundingBox.class)));will(returnValue(mockMethod));
            
            //There MUST be a call to release connection
            oneOf(mockMethod).releaseConnection();
            
            //This will return an input stream to our fake geotiff data
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(new ByteArrayInputStream(geotiffData)));
        
            //This is so we can inject our own fake output stream so we can inspect the result
            oneOf(mockResponse).getOutputStream();will(returnValue(outStream));
            oneOf(mockResponse).setContentType("application/zip");
            allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
            
            allowing(mockServiceCaller).getHttpClient();
         }});
        
        WCSController controller = new WCSController(mockServiceCaller, mockMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "GeoTIFF", inputCrs, outputWidth, outputHeight, outputResX, outputResY, outputCrs, northBoundLat, southBoundLat, eastBoundLng, westBoundLng, null, null, mockResponse);
        
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
        final WCSDownloadFormat format = WCSDownloadFormat.NetCDF;
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
        final byte[] netCdfData = new byte[] {4,1,2};
        final MyServletOutputStream outStream = new MyServletOutputStream();
        
        context.checking(new Expectations() {{
            //Our method maker call should be passed all the correct variables
            allowing(mockMethodMaker).makeMethod(with(any(String.class)), 
                    with(any(String.class)), 
                    with(any(WCSDownloadFormat.class)),
                    with(any(String.class)),
                    with(any(Integer.class)), 
                    with(any(Integer.class)), 
                    with(any(Integer.class)), 
                    with(any(Integer.class)),
                    with(any(String.class)),
                    with(any(String.class)));will(returnValue(mockMethod));
            
            //There MUST be a call to release connection
            oneOf(mockMethod).releaseConnection();
            
            //This will return an input stream to our fake netcdf data
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(new ByteArrayInputStream(netCdfData)));
        
            //This is so we can inject our own fake output stream so we can inspect the result
            oneOf(mockResponse).getOutputStream();will(returnValue(outStream));
            oneOf(mockResponse).setContentType("application/zip");
            allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
            
            allowing(mockServiceCaller).getHttpClient();
         }});
        
        WCSController controller = new WCSController(mockServiceCaller, mockMethodMaker, mockHostConfigurer);
        controller.downloadWCSAsZip(serviceUrl, layerName, "NetCDF", inputCrs, outputWidth, outputHeight, outputResX, outputResY, outputCrs, 0.0, 0.0, 0.0, 0.0, "2010-10-1", "23:12:11", mockResponse);
        
        ZipInputStream zip = outStream.getZipInputStream();
        ZipEntry ze = zip.getNextEntry();
        
        Assert.assertNotNull(ze);
        Assert.assertTrue(ze.getName().endsWith(".nc"));
        
        byte[] uncompressedData = new byte[netCdfData.length];
        int dataRead = zip.read(uncompressedData);
        Assert.assertEquals(netCdfData.length, dataRead);
        Assert.assertArrayEquals(netCdfData, uncompressedData);
    }
}
