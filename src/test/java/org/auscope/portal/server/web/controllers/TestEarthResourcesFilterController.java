package org.auscope.portal.server.web.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.mineraloccurrence.Mine;
import org.auscope.portal.server.util.GmlToKml;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.httpclient.ConnectTimeoutException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.net.UnknownHostException;
import java.net.ConnectException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestEarthResourcesFilterController {
    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private EarthResourcesFilterController earthResourcesFilterController;
    private MineralOccurrenceService mineralOccurrenceService;
    private HttpServletRequest mockHttpRequest;
    private HttpServletResponse mockHttpResponse;
    private GmlToKml mockGmlToKml;

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setup() {
        this.httpServiceCaller =  context.mock(HttpServiceCaller.class);
        this.mineralOccurrencesResponseHandler = context.mock(MineralOccurrencesResponseHandler.class);
        this.mineralOccurrenceService = context.mock(MineralOccurrenceService.class);
        this.mockGmlToKml = context.mock(GmlToKml.class);
        this.earthResourcesFilterController = new EarthResourcesFilterController(this.mineralOccurrencesResponseHandler, this.mineralOccurrenceService, this.mockGmlToKml);
        this.mockHttpRequest = context.mock(HttpServletRequest.class);
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
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
            oneOf (mineralOccurrenceService).getAllMinesGML(serviceURL);will(returnValue(expectedGML));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(HttpServletRequest.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName,  mockHttpRequest);

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
