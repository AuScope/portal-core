package org.auscope.portal.server.web.controllers;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.gsml.YilgarnLocSpecimenRecords;
import org.auscope.portal.server.web.controllers.TestDownloadController.MyServletOutputStream;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

public class TestYilgarnLocSpecimenController {
	
	private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
	
	private HttpServiceCaller httpServiceCaller = context.mock(HttpServiceCaller.class);
	private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);
	private HttpSession mockHttpSession = context.mock(HttpSession.class);
    private ServletContext mockServletContext = context.mock(ServletContext.class);
	private YilgarnLocSpecimenController yilgarnLocSpecimenController;
	private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);
	
	final class MyServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        public void write(int i) throws IOException {
            byteArrayOutputStream.write(i);
        }
        
        public ZipInputStream getZipInputStream() {
            return new ZipInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        }
    };
	
	@Before
	public void setup(){
		yilgarnLocSpecimenController = new YilgarnLocSpecimenController(httpServiceCaller);
	}
	
	
	@Test	
	public void testLocatedSpecimenFeature() throws Exception{
	
		final String serviceUrl = "http://fake.com/bob";
        final String layerName = "layer_name";
		final String featureId = "feature_id";
        final String xmlResponse = org.auscope.portal.Util.loadXML("src/test/resources/YilgarnLocSpecimenResponse.xml");
		
		context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(xmlResponse));
            
            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});
		
		ModelAndView modelAndView = yilgarnLocSpecimenController.doLocatedSpecimenFeature( serviceUrl,layerName, featureId,mockHttpRequest);
		
		Assert.assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();

        Assert.assertEquals(true, model.get("success"));

        Assert.assertNotNull(model.get("records"));
        Assert.assertEquals(1, ((YilgarnLocSpecimenRecords[]) model.get("records")).length);
        Assert.assertNotNull(((YilgarnLocSpecimenRecords[]) model.get("records"))[0]);
	}
	
	@Test
	public void testYilgarnGeochemistryDownload()throws Exception{
		final String serviceUrl = "http://fake.com/bob";
        final String layerName = "layer_name";
		final String featureId = "feature_id";
		final String xmlResponse ="<gml/>";
		
		context.checking(new Expectations() {{
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));will(returnValue(xmlResponse));
            
            oneOf(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            oneOf(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            oneOf(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});
		
		ModelAndView modelAndView = yilgarnLocSpecimenController.doYilgarnGeochemistryDownload( serviceUrl,layerName, featureId,mockHttpRequest);
		
		Assert.assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();

        Assert.assertEquals(true, model.get("success"));
	}
	
	@Test
	public void testDownloadLocSpecAsZip()throws Exception{
		
		final MyServletOutputStream servletOutputStream = new MyServletOutputStream();
        final String serviceUrls[] = {"http://someUrl"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\"" + dummyGml + "\"},\"success\":true}";
        
        context.checking(new Expectations() {{
            //setting of the headers for the return content
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpResponse).setHeader(with(any(String.class)), with(any(String.class)));
            oneOf(mockHttpResponse).getOutputStream();
            will(returnValue(servletOutputStream));

            //calling the service
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));
            will(returnValue(dummyJSONResponse));
        }});
        
        yilgarnLocSpecimenController.downloadLocSpecAsZip(serviceUrls, mockHttpResponse);
        
     // Check that the zip file contains the correct data
        ZipInputStream in = servletOutputStream.getZipInputStream();
        ZipEntry ze = in.getNextEntry();
        
        Assert.assertNotNull(ze);
        Assert.assertTrue(ze.getName().endsWith(".xml"));
        
        byte[] uncompressedData = new byte[dummyGml.getBytes().length];
        int dataRead = in.read(uncompressedData);
        
        Assert.assertEquals(dummyGml.getBytes().length, dataRead);
        Assert.assertArrayEquals(dummyGml.getBytes(), uncompressedData);
        
        in.close();     
        
	}
	
	
	/**
     * Test that this function makes all of the approriate calls, and see if it returns gml given some dummy data
     */
    @Test
    public void testDownloadGMLAsZipWithError() throws Exception {
        final MyServletOutputStream servletOutputStream = new MyServletOutputStream();
        final String[] serviceUrls = {"http://someUrl"};
        final String dummyGml = "<someGmlHere/>";
        final String dummyJSONResponse = "{\"data\":{\"kml\":\"<someKmlHere/>\", \"gml\":\"" + dummyGml + "\"},\"success\":false}";

        context.checking(new Expectations() {{
            //setting of the headers for the return content
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf(mockHttpResponse).setHeader(with(any(String.class)), with(any(String.class)));
            oneOf(mockHttpResponse).getOutputStream();
            will(returnValue(servletOutputStream));

            //calling the service
            oneOf(httpServiceCaller).getHttpClient();
            oneOf(httpServiceCaller).getMethodResponseAsString(with(any(HttpMethodBase.class)), with(any(HttpClient.class)));
            will(returnValue(dummyJSONResponse));
        }});

        yilgarnLocSpecimenController.downloadLocSpecAsZip(serviceUrls, mockHttpResponse);

        //check that the zip file contains the correct data
        ZipInputStream zipInputStream = servletOutputStream.getZipInputStream();
        ZipEntry ze = null;
        while ((ze = zipInputStream.getNextEntry()) != null) {
            System.out.println(ze.getName());
            Assert.assertTrue(ze.getName().endsWith("operation-failed.xml"));
        }
        zipInputStream.close();
    }
}


