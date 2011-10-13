package org.auscope.portal.server.web.controllers;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller that handles geotransects requests.
 *
 * @class GeotransectsController
 * @author jac24m
 */
@Controller
public class GeotransectsController {
    protected final Log logger = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;

    @Autowired
    public GeotransectsController(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    @RequestMapping("/requestGeotransectsData.do")
    public ModelAndView requestGeotransectsData(String serviceUrl) {

        GetMethod httpMethod = new GetMethod(serviceUrl);
        String response = "";
        ModelMap model = new ModelMap();

        try {
            response = serviceCaller.getMethodResponseAsString(httpMethod, serviceCaller.getHttpClient());

            model.put("success", true);
            model.put("errorMsg", "");
            model.put("json", response);

            return new JSONModelAndView(model);

        } catch (Exception ex) {
            logger.info("Error making request", ex);

            model.put("success", false);
            model.put("errorMsg", "Error making request");
            model.put("json", "");

            return new JSONModelAndView(model);
        }
    }

}
