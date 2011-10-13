package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.pressuredb.AvailableOMResponse;
import org.auscope.portal.pressuredb.PressureDBException;
import org.auscope.portal.server.util.ByteBufferedServletOutputStream;
import org.auscope.portal.server.web.service.PressureDBService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class TestPressureDBController {
    
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private PressureDBService mockService = context.mock(PressureDBService.class);
    private PressureDBController controller;
    private HttpServletResponse mockServletResponse = context.mock(HttpServletResponse.class);
    
    @Before
    public void setup() {
        controller = new PressureDBController(mockService);
    }
    
    @Test
    public void testGetOMError() throws Exception {
        final String wellID = "1234";
        final String serviceUrl = "http://example.com";
        final PressureDBException exception = new PressureDBException();
        
        context.checking(new Expectations() {{
            oneOf(mockService).makeGetAvailableOMRequest(wellID, serviceUrl);will(throwException(exception));
        }});
        
        ModelAndView mav = controller.getAvailableOM(serviceUrl, wellID);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }
    
    @Test
    public void testGetOM() throws Exception {
        final String wellID = "1234";
        final String serviceUrl = "http://example.com";
        final AvailableOMResponse response = new AvailableOMResponse();
        
        context.checking(new Expectations() {{
            oneOf(mockService).makeGetAvailableOMRequest(wellID, serviceUrl);will(returnValue(response));
        }});
        
        ModelAndView mav = controller.getAvailableOM(serviceUrl, wellID);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertSame(response, ((Object[])mav.getModel().get("data"))[0]);
    }
    
    @Test
    public void testDownload() throws Exception {
        final String wellID = "1234";
        final String serviceUrl = "http://example.com";
        final String[] features = new String[] {"a", "b", "c"};
        final byte[] expectedData = new byte[1024*1024];
        final InputStream responseStream = new ByteArrayInputStream(expectedData);
        final ByteBufferedServletOutputStream output = new ByteBufferedServletOutputStream(expectedData.length);
        
        for (int i = 0; i < expectedData.length; i++) {
            expectedData[i] = (byte) (i % 256);
        }
        
        context.checking(new Expectations() {{
            oneOf(mockService).makeDownloadRequest(wellID, serviceUrl, features);will(returnValue(responseStream));
            
            allowing(mockServletResponse).setContentType(with(any(String.class)));
            allowing(mockServletResponse).setHeader(with(any(String.class)), with(any(String.class)));
            oneOf(mockServletResponse).getOutputStream();will(returnValue(output));
        }});
        
        controller.download(serviceUrl, wellID, features, mockServletResponse);
        
        Assert.assertArrayEquals(expectedData, output.toByteArray()); 
    }
    
    @Test
    public void testDownloadError() throws Exception {
        final String wellID = "1234";
        final String serviceUrl = "http://example.com";
        final String[] features = new String[] {"a", "b", "c"};       
        final IOException exception = new IOException();
 
        context.checking(new Expectations() {{
            oneOf(mockService).makeDownloadRequest(wellID, serviceUrl, features);will(throwException(exception));
            
            oneOf(mockServletResponse).sendError(with(any(Integer.class)));
        }});
        
        controller.download(serviceUrl, wellID, features, mockServletResponse);
    }
}
