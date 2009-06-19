package org.auscope.portal.server.web.controllers;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.auscope.portal.server.web.controllers.GetDataSourcesJSONController;
import junit.framework.Assert;

/**
 * User: Mathew Wyatt
 * Date: 06/02/2009
 * Time: 1:21:01 PM
 */
public class TestGetDataSourcesJSONController {

    /**
     * A mock request we can pass to the controllers to test functionality
     */
    MockHttpServletRequest request;

    /**
     * A mock response that we can test valid responses with
     */
    MockHttpServletResponse response;

    /**
     * The controller we are testing
     */
    GetDataSourcesJSONController controller;

    /**
     * Set up test variables
     */
    @Before
    public void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        controller = new GetDataSourcesJSONController();
    }

    /**
     * Tear down or reset variables
     */
    @After
    public void tearDown() {}

    /**
     * This method tests the expansion of the root in the tree to get all of the available themes
     * @throws Exception
     */
    @Test
    public void testGetThemes() throws Exception {
        /*//a get request comes from the extjs tree
        request.setMethod("GET");

        //We are simulating the click of the root node in the tree
        request.setParameter("node", "root");

        //we send the request to our serlvet
        ModelAndView mav = controller.handleRequest(request, response);

        //we need to invoke the view to write to the respones - during normal running conditions this wiil be taken
        //care of by the spring framework
        mav.getView().render(mav.getModel(), request, response);

        //now we need to check if the JSON that is going down the wire is the json we want
        Assert.assertEquals(controller.getThemes().toString(), response.getContentAsString());*/
    }

    /**
     *
     */
    @Test
    public void testGetSpectralInstitutionalProviders() {
               
    }

    /**
     * 
     */
    @Test
    public void testGetInsitionsForBorhole() {

    }

    @Test
    public void testStripURL() {
        String url = "http://auscope-portal.arrc.csiro.au/nvcl/wfs?something=something";
        System.out.println(controller.stripUrlAndGetFeatures(url));
    }
}
