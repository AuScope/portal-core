package org.auscope.portal.core.server.controllers;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.auscope.portal.core.services.admin.StateService;

/***
 * A controller to store and retrieve the current state of the user interface
 *
 *
 */
@Controller
public class UIStateController extends BasePortalController {

    private final Log logger = LogFactory.getLog(getClass());
    private StateService stateService;

    @Autowired
    public UIStateController(StateService stateService) {
        this.stateService = stateService;
    }


    /**
     * Saves UI state to permanent store
     *
     * @param response
     * @param id - identifier
     * @param state - state string
     * @throws IOException
     */
    @RequestMapping("/saveUIState.do")
    public ModelAndView saveUIState(
            @RequestParam("id") final String id,
            @RequestParam("state") final String state) throws IOException {
        try {
            boolean result = stateService.save(id, state);
            return generateJSONResponseMAV(result);
        } catch (Exception exc) {
            logger.warn(String.format("Error saving UI state: %1$s", exc));
            logger.debug("Exception:", exc);
            return generateJSONResponseMAV(false);
        }
    }


    /**
     * Fetches UI state from permanent store
     *
     * @param response
     * @param id - identifier
     * @throws IOException
     */
    @RequestMapping("/fetchUIState.do")
    public ModelAndView fetchUIState(
            @RequestParam("id") final String id) throws IOException {
        try {
            String state = stateService.fetch(id);
            return generateJSONResponseMAV(true, state, "State");
        } catch (Exception exc) {
            logger.warn(String.format("Error fetching UI state: %1$s", exc));
            logger.debug("Exception:", exc);
            return generateJSONResponseMAV(false);
        }
    }
}
