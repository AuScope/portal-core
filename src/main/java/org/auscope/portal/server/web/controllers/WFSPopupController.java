package org.auscope.portal.server.web.controllers;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles requests to display WFS features. It calls WFS to get
 * a feature and converts received xml into html for display.
 *
 * @author JarekSanders
 * @version $Id$
 */
@Controller
public class WFSPopupController {

    // -------------------------------------------------------------- Constants

    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());
    public static final String DOCTYPE =
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \n\t \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";


    // ----------------------------------------------------- Instance variables

    private HttpServiceCaller serviceCaller;
    private GmlToKml gmlToKml;
    private WFSGetFeatureMethodMaker wfsMethodMaker;


    // ----------------------------------------------------------- Constructors

    @Autowired
    public WFSPopupController( HttpServiceCaller serviceCaller,
                               GmlToKml gmlToKml,
                               WFSGetFeatureMethodMaker wfsMethodMaker) {
        this.serviceCaller = serviceCaller;
        this.gmlToKml = gmlToKml;
        this.wfsMethodMaker = wfsMethodMaker;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * This method can be utilised by specifying a WFS url, typeName and featureId (in which a WFS request will be generated)
     * OR just by specifying a URL which will be resolved (such as in the case of a resolvable URN which maps to a WFS request
     * at a remote server).
     * @param request
     * @param response
     * @param serviceUrl
     * @param typeName
     * @param featureId
     */
    @RequestMapping("wfsFeaturePopup.do")
    public void wfsFeaturePopup( HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("url") String serviceUrl,
                                 @RequestParam(required=false, value="typeName") String typeName,
                                 @RequestParam(required=false, value="featureId") String featureId) {

        //Build our request (depending on whether we have specifeid typeName/featureId)
        HttpMethodBase wfsRequestMethod = null;
        if (typeName != null && !typeName.isEmpty() && featureId != null && !featureId.isEmpty()) {
            wfsRequestMethod = wfsMethodMaker.makeMethod(serviceUrl, typeName, featureId);
        } else {
            wfsRequestMethod = new GetMethod(serviceUrl);
        }

        //Make our request, transform and then return it.
        try {
            String getMethodURLString = wfsRequestMethod.getURI().toString();

            String responseFromCall = serviceCaller.getMethodResponseAsString(wfsRequestMethod, serviceCaller.getHttpClient());
            InputStream inXSLT = request.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/WfsToHtml.xsl");
            response.setContentType("text/html; charset=utf-8");

            String transformedResponse = gmlToKml.convert(responseFromCall, inXSLT, getMethodURLString);
            response.getOutputStream().write(transformedResponse.getBytes());
        } catch (Exception ex) {
            log.error("wfsFeaturePopup error: ", ex);
        }
    }

}
