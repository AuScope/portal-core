package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.service.CommodityService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.auscope.portal.server.web.service.BoreholeService;
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
    private CommodityService mockCommodityService;
    private HttpSession mockHttpSession;
    private ServletContext mockServletContext;
    private HttpServiceCaller mockHttpServiceCaller;
    private HttpMethodBase httpMethodBase;
    private HttpClient mockHttpClient;

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setup() {
        this.mineralOccurrencesResponseHandler = context.mock(MineralOccurrencesResponseHandler.class);
        this.mineralOccurrenceService = context.mock(MineralOccurrenceService.class);
        this.mockGmlToKml = context.mock(GmlToKml.class);
        this.mockHttpRequest = context.mock(HttpServletRequest.class);
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
        this.mockCommodityService = context.mock(CommodityService.class);
        this.mockHttpSession = context.mock(HttpSession.class);
        this.mockServletContext = context.mock(ServletContext.class);
        this.mockHttpServiceCaller = context.mock(HttpServiceCaller.class);
        this.httpMethodBase = context.mock(HttpMethodBase.class);
        this.mockHttpClient = context.mock(HttpClient.class);
        this.earthResourcesFilterController = new EarthResourcesFilterController(
        		this.mineralOccurrencesResponseHandler, this.mineralOccurrenceService, 
        		this.mockGmlToKml, this.mockCommodityService, this.mockHttpServiceCaller);
    }

    private void testJSONResponse(String json, Boolean success, String gml, String kml) {
        JSONObject obj = JSONObject.fromObject(json);

        if (success != null) {
            Assert.assertEquals(success.booleanValue(), obj.get("success"));
        }

        if (gml != null) {
            JSONObject data = (JSONObject) obj.get("data");

            Assert.assertNotNull(data);
            Assert.assertEquals(gml, data.get("gml"));
        }

        if (kml != null) {
            JSONObject data = (JSONObject) obj.get("data");

            Assert.assertNotNull(data);
            Assert.assertEquals(kml, data.get("kml"));
        }
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
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String xmlErrorResponse = org.auscope.portal.Util.loadXML("src/test/resources/GetMineError.xml");
        final StringWriter jsonResponse = new StringWriter();

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL)));
            
            oneOf (mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName, 0);will(returnValue(mockMethod));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient); will(returnValue(xmlErrorResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(jsonResponse)));

            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);


        //Ensure that we get a response that says failure
        testJSONResponse(jsonResponse.toString(), new Boolean(false), null, null);
    }

    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterAnyError() throws Exception {
        final String serviceURL = "http://testblah.com";
        final String expectedKML = "";
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String xmlErrorResponse = org.auscope.portal.Util.loadXML("src/test/resources/GetMineError.xml");
        final StringWriter jsonResponse = new StringWriter();

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL)));
            
            oneOf (mineralOccurrenceService).getAllMinesGML(serviceURL, 0);will(returnValue(mockMethod));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient); will(returnValue(xmlErrorResponse));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)),with(any(String.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf(mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(jsonResponse)));

            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, "", null, 0,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //Ensure that we get a response that says failure
        testJSONResponse(jsonResponse.toString(), new Boolean(false), null, null);
    }

    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterAllMines() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = ""; //to get all mines
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String expectedKML = "<kml/>";
        final String expectedGML = "<gml/>";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL)));
            oneOf (mineralOccurrenceService).getAllMinesGML(serviceURL, 0);will(returnValue(mockMethod));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient); will(returnValue(expectedGML));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));

            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //Ensure that we get a valid response
        testJSONResponse(actualJSONResponse.getBuffer().toString(), new Boolean(true), expectedGML, expectedKML);
    }

    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterSingleMine() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "mineName"; //to get all mines
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final String expectedKML = "<kml/>";
        final String expectedGML = "<gml/>";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL)));
            oneOf(mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName, 0);will(returnValue(mockMethod));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient); will(returnValue(expectedGML));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(InputStream.class)), with(any(String.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));


            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //Ensure that we get a valid response
        testJSONResponse(actualJSONResponse.getBuffer().toString(), new Boolean(true), expectedGML, expectedKML);
    }

    @Test
    public void testRequestFailure() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "mineName"; //to get all mines
        final GetMethod mockMethod = context.mock(GetMethod.class);
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf(mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName, 0);will(returnValue(mockMethod));
            oneOf (mockHttpServiceCaller).getHttpClient();will(returnValue(mockHttpClient));
            oneOf (mockHttpServiceCaller).getMethodResponseAsString(mockMethod, mockHttpClient); will(throwException(new ConnectException()));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));

            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL)));
            allowing(mockHttpRequest).getSession();will(returnValue(mockHttpSession));
            allowing(mockHttpSession).getServletContext();will(returnValue(mockServletContext));
            allowing(mockServletContext).getResourceAsStream(with(any(String.class))); will(returnValue(null));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //Ensure that we get a valid response
        testJSONResponse(actualJSONResponse.getBuffer().toString(), new Boolean(false), null, null);

    }

}
