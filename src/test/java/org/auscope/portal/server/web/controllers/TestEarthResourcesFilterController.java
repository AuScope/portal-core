package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.service.CommodityService;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.auscope.portal.server.web.service.NvclService;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author san218
 * @version $Id$
 */
public class TestEarthResourcesFilterController {
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private EarthResourcesFilterController earthResourcesFilterController;
    private MineralOccurrenceService mineralOccurrenceService;
    private HttpServletRequest mockHttpRequest;
    private HttpServletResponse mockHttpResponse;
    private GmlToKml mockGmlToKml;
    private NvclService mockNvclService;
    private CommodityService mockCommodityService;

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setup() {
        this.mineralOccurrencesResponseHandler = context.mock(MineralOccurrencesResponseHandler.class);
        this.mineralOccurrenceService = context.mock(MineralOccurrenceService.class);
        this.mockGmlToKml = context.mock(GmlToKml.class);
        this.mockNvclService = context.mock(NvclService.class);
        this.earthResourcesFilterController = new EarthResourcesFilterController(this.mineralOccurrencesResponseHandler, this.mineralOccurrenceService,this.mockNvclService, this.mockGmlToKml, this.mockCommodityService);
        this.mockHttpRequest = context.mock(HttpServletRequest.class);
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
        this.mockCommodityService = context.mock(CommodityService.class);
    }
    
    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterSpecificError() throws Exception {
        final String mineName = "testMine";
        final String serviceURL = "http://testblah.com";
        final String expectedKML = ""; 
        final String xmlErrorResponse = org.auscope.portal.Util.loadXML("src/test/resources/GetMineError.xml");
        final StringWriter jsonResponse = new StringWriter();
        
        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName, 0);will(returnValue(xmlErrorResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(expectedKML));
            
            //check that the correct response is getting output
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(jsonResponse)));
        }});
        
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0,  mockHttpRequest);
        
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);
        
        String json = jsonResponse.toString();
        
        //Ensure that we get a response that says failure
        Assert.assertTrue(json.length() > 0);
        Assert.assertFalse(json.contains("\"success\":true"));
        Assert.assertTrue(json.contains("\"success\":false"));
    }
    
    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterAnyError() throws Exception {
        final String serviceURL = "http://testblah.com";
        final String expectedKML = "";
        final String xmlErrorResponse = org.auscope.portal.Util.loadXML("src/test/resources/GetMineError.xml");
        final StringWriter jsonResponse = new StringWriter();
        
        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getAllMinesGML(serviceURL, 0);will(returnValue(xmlErrorResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(expectedKML));
            
            //check that the correct response is getting output
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(jsonResponse)));
        }});
        
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, "", null, 0,  mockHttpRequest);
        
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);
        
        String json = jsonResponse.toString();
        
        //Ensure that we get a response that says failure
        Assert.assertTrue(json.length() > 0);
        Assert.assertFalse(json.contains("\"success\":true"));
        Assert.assertTrue(json.contains("\"success\":false"));
    }
    
    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterAllMines() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "All Mines.."; //to get all mines
        final String expectedKML = "<kml/>";
        final String expectedGML = "<gml/>";
        final String expectedJSONResponse = "{\"data\":{\"kml\":\""+expectedKML+"\",\"gml\":\""+expectedGML+"\"},\"success\":true}";
        final String expectedJSONResponse2= "{\"success\":true,\"data\":{\"gml\":\""+expectedGML+"\",\"kml\":\""+expectedKML+"\"}}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getAllMinesGML(serviceURL, 0);will(returnValue(expectedGML));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        System.out.println(expectedJSONResponse);
        System.out.println(expectedJSONResponse2);
        System.out.println(actualJSONResponse.getBuffer().toString());

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }
    
}
