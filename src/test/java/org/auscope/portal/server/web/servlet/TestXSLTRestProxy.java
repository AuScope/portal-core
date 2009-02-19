package org.auscope.portal.server.web.servlet;

import org.junit.Test;
import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.auscope.portal.server.web.controllers.GetDataSourcesJSONController;

import java.io.UnsupportedEncodingException;

/**
 * User: Mathew Wyatt
 * Date: 18/02/2009
 * Time: 3:29:07 PM
 */

public class TestXSLTRestProxy {

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
    XSLTRestProxy restProxy;


    /**
     * Set up test variables
     */
    @Before
    public void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        restProxy = new XSLTRestProxy();
    }

    /**
     * Test a proxy to a kml file, we just want the binary file sent back through in the httpResponse
     */
    @Test
    public void testRestStyleCallForKmlFile() throws UnsupportedEncodingException {
        //a get request comes from the extjs tree
        request.setMethod("GET");

        //We are simulating the click of the root node in the tree
        request.setParameter("url", "http://mapgadgets.googlepages.com/cta.kml");

        restProxy.doGet(request, response);

        System.out.println(response.getContentAsString());
    }

    /**
     * Test a proxy to a kml file, we just want the binary file sent back through in the httpResponse
     */
    @Test
    public void testRestStyleCallWFSToKML() throws UnsupportedEncodingException {
        //a get request comes from the extjs tree
        request.setMethod("GET");

        //We are simulating the click of the root node in the tree
        request.setParameter("url", "http://auscope-portal.arrc.csiro.au/nvcl/wfs?request=GetFeature&typeName=gsml:Borehole");

        restProxy.doGet(request, response);

        System.out.println(response.getContentAsString());
    }
    

}
