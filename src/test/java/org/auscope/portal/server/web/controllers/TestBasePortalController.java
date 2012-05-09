package org.auscope.portal.server.web.controllers;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for BasePortalController
 * @author Josh Vote
 *
 */
public class TestBasePortalController extends PortalTestClass {

    private class BasePortalControllerImpl extends BasePortalController {

    }

    private BasePortalControllerImpl basePortalController = new BasePortalControllerImpl();

    /**
     * Asserts that a model and view matches the consistent pattern used throughout the portal
     * @param mav1
     * @param mav2
     */
    private void assertModelAndViewConsistency(ModelAndView mav) {
        ModelMap map = mav.getModelMap();

        List<String> validKeys = Arrays.asList("success", "data", "msg", "debugInfo", "totalResults");

        //Ensure there are no erroneous keys
        for (String key : map.keySet()) {
            Assert.assertTrue("Invalid Key: " + key, validKeys.contains(key));
        }

        //Test success
        Assert.assertTrue("No success indicator", map.containsKey("success"));
        Object successObj = map.get("success");
        Assert.assertNotNull(successObj);
        Assert.assertTrue("Success object is not a Boolean", successObj instanceof Boolean);

        //Test some special cases of data
        if (map.containsKey("data")) {
            Object dataObj = map.get("data");
            if (dataObj != null && dataObj instanceof ModelMap) {
                ModelMap data = (ModelMap) dataObj;
                if (data.containsKey("gml") || data.containsKey("kml")) {
                    Assert.assertTrue("gml must be paired with kml", data.containsKey("gml"));
                    Assert.assertTrue("gml must be paired with kml", data.containsKey("kml"));
                    Assert.assertNotNull(data.get("gml"));
                    Assert.assertNotNull(data.get("kml"));
                }
            }
        }

      //Test some special cases of data
      if (map.containsKey("matchedResults")) {
          Assert.assertTrue("matchedResults must be an int", map.get("matchedResults") instanceof Integer);
      }
    }

    /**
     * Tests that BasePortalController has consistent return types for the various options
     * @throws Exception
     */
    @Test
    public void testConsistencyOfResponses() throws Exception {
        final String serviceUrl = "http://service/url";
        final ModelMap gmlKmlData = new ModelMap();
        final ModelMap debugInfo = new ModelMap();
        final GetMethod getMethod = new GetMethod(serviceUrl);
        final String message = "message string";
        gmlKmlData.put("gml", "gmlString");
        gmlKmlData.put("kml", "kmlString");

        assertModelAndViewConsistency(basePortalController.generateExceptionResponse(new ConnectException(), serviceUrl));
        assertModelAndViewConsistency(basePortalController.generateExceptionResponse(new ConnectException(), serviceUrl, getMethod));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(true, gmlKmlData, null));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(false, null, null));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(true, null, message));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(false, gmlKmlData, message));
        assertModelAndViewConsistency(basePortalController.generateHTMLResponseMAV(false, gmlKmlData, message, debugInfo));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, null, null));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, null, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, message));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, null, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, null));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, null, null));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, null, 45, message));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(false, gmlKmlData, message, debugInfo));
        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, "gmlString", "kmlString", getMethod));

        assertModelAndViewConsistency(basePortalController.generateJSONResponseMAV(true, gmlKmlData, message, 43, debugInfo));
    }
}
