package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.wfs.WFSHTMLResponse;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.WFSService;
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
    private final Log log = LogFactory.getLog(getClass());
    public static final String DOCTYPE =
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \n\t \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";


    // ----------------------------------------------------- Instance variables

    private WFSService wfsService;


    // ----------------------------------------------------------- Constructors

    @Autowired
    public WFSPopupController(WFSService wfsService) {
        this.wfsService = wfsService;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * This method can be utilised by specifying a WFS url, typeName and featureId (in which a WFS request will be generated)
     * OR just by specifying a URL which will be resolved (such as in the case of a resolvable URN which maps to a WFS request
     * at a remote server).
     * @param serviceUrl Can either be a WFS endpoint OR a URL that when resolved returns a WFS response
     * @param typeName [Optional] If specified a WFS request will be generated
     * @param featureId [Optional] If specified a WFS request will be generated
     * @throws IOException
     */
    @RequestMapping("wfsFeaturePopup.do")
    public void wfsFeaturePopup(HttpServletResponse response,
                                 @RequestParam("url") String serviceUrl,
                                 @RequestParam(required=false, value="typeName") String typeName,
                                 @RequestParam(required=false, value="featureId") String featureId) throws IOException {


        response.setContentType("text/html; charset=utf-8");
        ServletOutputStream outputStream = response.getOutputStream();

        //Make our request, transform and then return it.
        WFSHTMLResponse htmlResponse = null;
        try {
            if (typeName == null) {
                htmlResponse = wfsService.getWfsResponseAsHtml(serviceUrl);
            } else {
                htmlResponse = wfsService.getWfsResponseAsHtml(serviceUrl, typeName, featureId);
            }

            outputStream.write(htmlResponse.getHtml().getBytes());
        } catch (Exception ex) {
            log.warn("Internal error requesting/writing popup contents", ex);
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
