package org.auscope.portal.server.web.controllers;

import java.net.ConnectException;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author san218
 * @version $Id$
 */
public class TestEarthResourcesFilterController {
    private EarthResourcesFilterController earthResourcesFilterController;
    private MineralOccurrenceService mineralOccurrenceService;

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setUp() {
        this.mineralOccurrenceService = context.mock(MineralOccurrenceService.class);
        this.earthResourcesFilterController = new EarthResourcesFilterController(this.mineralOccurrenceService);
    }

    private void testMAVResponse(ModelAndView mav, Boolean success, String gml, String kml) {
        ModelMap model = mav.getModelMap();


        if (success != null) {
            Assert.assertEquals(success.booleanValue(), model.get("success"));
        }

        if (gml != null) {
            ModelMap data = (ModelMap) model.get("data");

            Assert.assertNotNull(data);
            Assert.assertEquals(gml, data.get("gml"));
        }

        if (kml != null) {
            ModelMap data = (ModelMap) model.get("data");

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
        final ConnectException minOccurenceServiceEx = new ConnectException();
        final HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL, true)));

            oneOf(mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName, 0);will(throwException(minOccurenceServiceEx));
        }});

        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0);

        //Ensure that we get a response that says failure
        testMAVResponse(modelAndView, new Boolean(false), null, null);
    }

    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterAnyError() throws Exception {
        final String serviceURL = "http://testblah.com";
        final String expectedKML = "";
        final HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
        final String xmlErrorResponse = org.auscope.portal.Util.loadXML("src/test/resources/GetMineError.xml");

        context.checking(new Expectations() {{
            oneOf(mineralOccurrenceService).getAllMinesGML(serviceURL, 0);will(returnValue(new WFSKMLResponse(xmlErrorResponse, expectedKML, mockMethod)));
        }});

        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, "", null, 0);

        //Ensure that we get a response that says failure
        testMAVResponse(modelAndView, new Boolean(false), null, null);
    }

    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterAllMines() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = ""; //to get all mines
        final HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
        final String expectedKML = "<kml/>";
        final String expectedGML = "<gml/>";

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL, true)));
            oneOf(mineralOccurrenceService).getAllMinesGML(serviceURL, 0);will(returnValue(new WFSKMLResponse(expectedGML, expectedKML, mockMethod)));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0);

        //Ensure that we get a valid response
        testMAVResponse(modelAndView, new Boolean(true), expectedGML, expectedKML);
    }

    /**
     * Test doing a mine filter and getting all mines
     * @throws Exception
     */
    @Test
    public void testDoMineFilterSingleMine() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "mineName"; //to get all mines
        final HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
        final String expectedKML = "<kml/>";
        final String expectedGML = "<gml/>";

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(new URI(serviceURL, true)));
            oneOf(mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName, 0);will(returnValue(new WFSKMLResponse(expectedGML, expectedKML, mockMethod)));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0);

        //Ensure that we get a valid response
        testMAVResponse(modelAndView, new Boolean(true), expectedGML, expectedKML);
    }

    @Test
    public void testRequestFailure() throws Exception {
        final String serviceURL = "http://localhost?";
        final String mineName = "mineName"; //to get all mines

        context.checking(new Expectations() {{
            oneOf(mineralOccurrenceService).getMineWithSpecifiedNameGML(serviceURL, mineName, 0);will(throwException(new ConnectException()));
        }});

        //call with updateCSWRecords dud url
        ModelAndView modelAndView = this.earthResourcesFilterController.doMineFilter(serviceURL, mineName, null, 0);

        //Ensure that we get a valid response
        testMAVResponse(modelAndView, new Boolean(false), null, null);

    }

}
