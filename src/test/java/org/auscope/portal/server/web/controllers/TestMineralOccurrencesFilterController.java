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


/**
 * User: Mathew Wyyatt
 * Date: 23/03/2009
 * Time: 12:50:56 PM
 */
public class TestMineralOccurrencesFilterController {
    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrencesFilterController minerOccurrenceFilterController;
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
        this.minerOccurrenceFilterController = new MineralOccurrencesFilterController(this.mineralOccurrencesResponseHandler, this.mineralOccurrenceService, this.mockGmlToKml);
        this.mockHttpRequest = context.mock(HttpServletRequest.class);
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
    }

    /**
     * Test that the getAlLMines controller returns the correct JSON response
     * 
     * @throws Exception
     */
    @Test
    public void testGetMineNames() throws Exception {
        final String serviceURL = "http://localhost?";
        final String expectedJSONResponse = "{\"data\":[{\"mineDisplayName\":\"All Mines..\"},{\"mineDisplayName\":\"Balh1\"},{\"mineDisplayName\":\"Balh2\"}],\"success\":true}";
        final String expectedJSONResponse2= "{\"success\":true,\"data\":[{\"mineDisplayName\":\"All Mines..\"},{\"mineDisplayName\":\"Balh1\"},{\"mineDisplayName\":\"Balh2\"}]}";

        final Mine mockMine1 = context.mock(Mine.class);
        final Mine mockMine2 = context.mock(Mine.class, "mockMine2");
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            //return updateCSWRecords list of mock mines for the controller to build up json from
            oneOf (mineralOccurrenceService).getAllMines(serviceURL);will(returnValue(Arrays.asList(mockMine1, mockMine2)));

            //return the names which are in our expectedJSONResponse
            oneOf (mockMine1).getMineNamePreffered(); will(returnValue("Balh1"));
            oneOf (mockMine2).getMineNamePreffered(); will(returnValue("Balh2"));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
            //oneOf (mockPrintWriter).write(expectedJSONResponse);
        }});

        //call getMineNames with updateCSWRecords dud service url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.getMineNames(serviceURL, new ModelMap());

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

    /**
     * Test the event of an exception being thrown when trying to get the mines
     */
    @Test
    public void testGetMinesError() throws Exception {
        final String serviceURL = "http://localhost?";
        final String expectedJSONResponse = "{\"msg\":\""+ ErrorMessages.FILTER_FAILED +"\",\"success\":false}";
        final String expectedJSONResponse2= "{\"success\":false,\"msg\":\""+ ErrorMessages.FILTER_FAILED +"\"}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            //get the call the throw an exception
            oneOf (mineralOccurrenceService).getAllMines(serviceURL);will(throwException(new Exception()));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call getMineNames with updateCSWRecords dud service url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.getMineNames(serviceURL, new ModelMap());

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
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
        final String expectedJSONResponse = "{\"data\":{\"kml\":\""+expectedKML+"\"},\"success\":true}";
        final String expectedJSONResponse2= "{\"success\":true,\"data\":{\"kml\":\""+expectedKML+"\"}}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getAllMinesGML(serviceURL);
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(HttpServletRequest.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMineFilter(serviceURL, mineName,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

    /**
     * Test a mine query with a specified name
     */
    @Test
    public void testDoMineFilterSpecifiedName() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "SomeName"; //random name
        final String expectedKML = "<kml/>";
        final String expectedJSONResponse = "{\"data\":{\"kml\":\""+expectedKML+"\"},\"success\":true}";
        final String expectedJSONResponse2= "{\"success\":true,\"data\":{\"kml\":\""+expectedKML+"\"}}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName);
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(any(HttpServletRequest.class))); will(returnValue(expectedKML));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMineFilter(serviceURL, mineName,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

    /**
     * Test doing a minefilter, and there being an error
     */
    @Test
    public void testDoMineFilterUnknownHost() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "SomeName"; //random name
        final String expectedJSONResponse = "{\"msg\":\""+ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION+"\",\"success\":false}";
        final String expectedJSONResponse2= "{\"success\":false,\"msg\":\""+ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION+"\"}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName);will(throwException(new UnknownHostException()));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMineFilter(serviceURL, mineName,  mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

    @Test
    public void testDoMineralOccurrenceFilter() throws Exception {
        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineralOccurrenceGML("", "", "", "", "", "", "", "", "", "");
            oneOf (mineralOccurrencesResponseHandler).getNumberOfFeatures(with(any(String.class))); will(returnValue(1));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(mockHttpRequest));
        }});

        this.minerOccurrenceFilterController.doMineralOccurrenceFilter("", "", "", "", "", "", "", "", "", "", mockHttpRequest);
    }

    @Test
    public void testDoMineralOccurrenceFilterNoResults() throws Exception {
        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineralOccurrenceGML("", "", "", "", "", "", "", "", "", "");
            oneOf (mineralOccurrencesResponseHandler).getNumberOfFeatures(with(any(String.class))); will(returnValue(0));
            oneOf (mockGmlToKml).convert(with(any(String.class)), with(mockHttpRequest));
        }});

        this.minerOccurrenceFilterController.doMineralOccurrenceFilter("", "", "", "", "", "", "", "", "", "", mockHttpRequest);
    }

    @Test
    public void testDoMineralOccurrenceFilterTimout() throws Exception {
        final String expectedJSONResponse = "{\"msg\":\""+ErrorMessages.OPERATION_TIMOUT+"\",\"success\":false}";
        final String expectedJSONResponse2= "{\"success\":false,\"msg\":\""+ErrorMessages.OPERATION_TIMOUT+"\"}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineralOccurrenceGML("", "", "", "", "", "", "", "", "", "");will(throwException(new ConnectTimeoutException()));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with parameters
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMineralOccurrenceFilter("", "", "", "", "", "", "", "", "", "", mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

    @Test
    public void testDoMiningActivityFilterAllMines() throws Exception {
        final String mineName = "All Mines.."; //to get all mines
        final List<Mine> mines = Arrays.asList(context.mock(Mine.class));

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getAllMines("");will(returnValue(mines));

            oneOf (mineralOccurrenceService).getMiningActivityGML("", mines, "", "", "", "", "", "");
            oneOf (mineralOccurrencesResponseHandler).getNumberOfFeatures(with(any(String.class))); will(returnValue(1));

            oneOf (mockGmlToKml).convert(with(any(String.class)), with(mockHttpRequest));
        }});

        this.minerOccurrenceFilterController.doMiningActivityFilter("", mineName, "", "", "", "", "", "", mockHttpRequest);
    }

    /**
     * Test when we query for all mines, but got no mines in the response
     * @throws Exception
     */
    @Test
    public void testDoMiningActivityFilterAllMinesZeroMines() throws Exception {
        final String mineName = "All Mines.."; //to get all mines
        final List<Mine> mines = Arrays.asList();
        final String expectedJSONResponse = "{\"msg\":\""+ErrorMessages.NO_RESULTS+"\",\"success\":false}";
        final String expectedJSONResponse2= "{\"success\":false,\"msg\":\""+ErrorMessages.NO_RESULTS+"\"}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getAllMines("");will(returnValue(mines));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with parameters
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMiningActivityFilter("", mineName, "", "", "", "", "", "", mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }
    /**
     * Test when we query for MiningActivity, but got no MiningActivity in the response
     * @throws Exception
     */
    @Test
    public void testDoMiningActivityFilterMiningActivityZeroMiningActivities() throws Exception {
        final String mineName = "All Mines.."; //to get all mines
        final List<Mine> mines = Arrays.asList(context.mock(Mine.class));
        final String expectedJSONResponse = "{\"msg\":\""+ErrorMessages.NO_RESULTS+"\",\"success\":false}";
        final String expectedJSONResponse2= "{\"success\":false,\"msg\":\""+ErrorMessages.NO_RESULTS+"\"}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getAllMines("");will(returnValue(mines));

            oneOf (mineralOccurrenceService).getMiningActivityGML("", mines, "", "", "", "", "", "");
            oneOf (mineralOccurrencesResponseHandler).getNumberOfFeatures(with(any(String.class))); will(returnValue(0));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with parameters
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMiningActivityFilter("", mineName, "", "", "", "", "", "", mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }


    @Test
    public void testDoMiningActivityFilterSelectedMine() throws Exception {
        final String mineName = ""; //to get all mines
        final List<Mine> mines = Arrays.asList(context.mock(Mine.class));

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineWithSpecifiedName("", "");will(returnValue(mines));
            oneOf (mineralOccurrencesResponseHandler).getNumberOfFeatures(with(any(String.class))); will(returnValue(1));

            oneOf (mineralOccurrenceService).getMiningActivityGML("", mines, "", "", "", "", "", "");
            oneOf (mineralOccurrencesResponseHandler).getNumberOfFeatures(with(any(String.class))); will(returnValue(1));

            oneOf (mockGmlToKml).convert(with(any(String.class)), with(mockHttpRequest));
        }});

        this.minerOccurrenceFilterController.doMiningActivityFilter("", mineName, "", "", "", "", "", "", mockHttpRequest);
    }

    @Test
    public void testDoMiningActivityFilterWithConnectException() throws Exception {
        final String expectedJSONResponse = "{\"msg\":\""+ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION+"\",\"success\":false}";
        final String expectedJSONResponse2= "{\"success\":false,\"msg\":\""+ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION+"\"}";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
            oneOf (mineralOccurrenceService).getMineWithSpecifiedName("", "");will(throwException(new ConnectException()));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        //call with parameters
        ModelAndView modelAndView = this.minerOccurrenceFilterController.doMiningActivityFilter("", "", "", "", "", "", "", "", mockHttpRequest);

        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected - could be ordered 1 of 2 ways, both valid
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()) ||
                expectedJSONResponse2.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

}
