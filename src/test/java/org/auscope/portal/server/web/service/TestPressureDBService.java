package org.auscope.portal.server.web.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.pressuredb.AvailableOMResponse;
import org.auscope.portal.pressuredb.PressureDBException;
import org.auscope.portal.server.web.PressureDBMethodMaker;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPressureDBService {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private PressureDBService service;
    private PressureDBMethodMaker mockMethodMaker = context.mock(PressureDBMethodMaker.class);
    private HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
    private HttpClient mockHttpClient = context.mock(HttpClient.class);
    private HttpMethodBase mockHttpMethod = context.mock(HttpMethodBase.class);
    private InputStream mockStream = context.mock(InputStream.class);
    
    @Before
    public void setup() {
        service = new PressureDBService(mockMethodMaker, mockHttpServiceCaller);
    }
    
    @Test
    public void testMakeOMRequest() throws Exception {
        final String wellID = "123";
        final String serviceUrl = "http://example.com/pressure-db-dataservice";
        final InputStream responseStream = new FileInputStream("src/test/resources/PressureDB-getAvailableOMResponse.xml");
        
        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeGetAvailableOMMethod(serviceUrl, wellID);will(returnValue(mockHttpMethod));
            
            oneOf(mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockHttpMethod, mockHttpClient);will(returnValue(responseStream));
        }});
        
        //Make our response and have it parsed
        AvailableOMResponse response = service.makeGetAvailableOMRequest(wellID, serviceUrl);
        Assert.assertNotNull(response);
        
        Assert.assertEquals(wellID, response.getWellID());
        
        Assert.assertEquals("http://example.com/pressure-db", response.getOmUrl());
        Assert.assertEquals(true, response.isObsTemperature());
        Assert.assertEquals(true, response.isObsPressureData());
        Assert.assertEquals(false, response.isObsSalinity());
        
        Assert.assertEquals(false, response.isPressureRft());
        Assert.assertEquals(true, response.isPressureDst());
        Assert.assertEquals(false, response.isPressureFitp());
        
        Assert.assertEquals(true, response.isSalinityTds());
        Assert.assertEquals(true, response.isSalinityNacl());
        Assert.assertEquals(true, response.isSalinityCl());
        
        Assert.assertEquals(true, response.isTemperatureT());
    }
    
    @Test(expected=PressureDBException.class)
    public void testMakeOMRequestParserError() throws Exception {
        final String wellID = "123";
        final String serviceUrl = "http://example.com/pressure-db-dataservice";
        final InputStream responseStream = new FileInputStream("src/test/resources/PressureDB-errorResponse.xml");
        
        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeGetAvailableOMMethod(serviceUrl, wellID);will(returnValue(mockHttpMethod));
            
            oneOf(mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockHttpMethod, mockHttpClient);will(returnValue(responseStream));
        }});
        
        //Make our response and have it parsed - it should result in a parser exception
        service.makeGetAvailableOMRequest(wellID, serviceUrl);
    }
    
    @Test(expected=IOException.class)
    public void testMakeOMRequestIOError() throws Exception {
        final String wellID = "123";
        final String serviceUrl = "http://example.com/pressure-db-dataservice";
        final IOException exception = new IOException();
        
        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeGetAvailableOMMethod(serviceUrl, wellID);will(returnValue(mockHttpMethod));
            
            oneOf(mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockHttpMethod, mockHttpClient);will(throwException(exception));
        }});
        
        //make the request - it should throw an exception
        service.makeGetAvailableOMRequest(wellID, serviceUrl);
    }
    
    @Test
    public void testDownload() throws Exception {
        final String wellID = "123";
        final String serviceUrl = "http://example.com/pressure-db-dataservice";
        final String[] features = new String[] {"a", "b", "c"};
        
        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeDownloadMethod(serviceUrl, wellID, features);will(returnValue(mockHttpMethod));
            
            oneOf(mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockHttpMethod, mockHttpClient);will(returnValue(mockStream));
        }});
        
        //make the request - it should return a stream
        InputStream result = service.makeDownloadRequest(wellID, serviceUrl, features);
        Assert.assertSame(mockStream, result);
    }
    
    @Test(expected=IOException.class)
    public void testDownloadError() throws Exception {
        final String wellID = "123";
        final String serviceUrl = "http://example.com/pressure-db-dataservice";
        final String[] features = new String[] {"a", "b", "c"};
        final IOException exception = new IOException();
        
        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeDownloadMethod(serviceUrl, wellID, features);will(returnValue(mockHttpMethod));
            
            oneOf(mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf(mockHttpServiceCaller).getMethodResponseAsStream(mockHttpMethod, mockHttpClient);will(throwException(exception));
        }});
        
        //make the request - it should throw an exception
        service.makeDownloadRequest(wellID, serviceUrl, features);
    }
}
