package org.auscope.portal.server.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 * User: Mathew Wyatt
 * Date: 17/08/2009
 * Time: 12:10:41 PM
 */

@Controller
public class GSMLController {

    @Autowired private HttpServiceCaller serviceCaller;
    @Autowired private GmlToKml gmlToKml;

    @RequestMapping("/getAllFeatures.do")
    public ModelAndView requestAllFeatures(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           HttpServletRequest request) throws Exception {

        String gmlResponse = serviceCaller.callMethod(new ICSWMethodMaker() {
            public HttpMethodBase makeMethod() {
                GetMethod method = new GetMethod(serviceUrl);

                //set all of the parameters
                NameValuePair request = new NameValuePair("request", "GetFeature");
                NameValuePair elementSet = new NameValuePair("typeName", featureType);

                //attach them to the method
                method.setQueryString(new NameValuePair[]{request, elementSet});

                return method;
            }
        }.makeMethod(), serviceCaller.getHttpClient());

         return makeModelAndViewKML(gmlToKml.convert(gmlResponse, request));
    }

    /**
     * Insert a kml block into a successful JSON response
     * @param kmlBlob
     * @return
     */
    private ModelAndView makeModelAndViewKML(final String kmlBlob) {
        final Map data = new HashMap() {{
            put("kml", kmlBlob);
        }};

        ModelMap model = new ModelMap() {{
            put("success", true);
            put("data", data);
        }};

        return new JSONModelAndView(model);
    }
}
