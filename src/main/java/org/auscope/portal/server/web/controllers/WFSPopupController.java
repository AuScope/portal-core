package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.service.HttpServiceCaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
    
    
    // ----------------------------------------------------- Instance variables
    
    private HttpServiceCaller serviceCaller;
    private GmlToKml gmlToKml;
    
    
    // ----------------------------------------------------------- Constructors
    
    @Autowired
    public WFSPopupController( HttpServiceCaller serviceCaller,
                               GmlToKml gmlToKml) {
        this.serviceCaller = serviceCaller;
        this.gmlToKml = gmlToKml;
    }    
    
    
    // --------------------------------------------------------- Public Methods
    
    @RequestMapping("/wfsFeaturePopup.do")
    public void wfsFeaturePopup( HttpServletRequest request,
                                 HttpServletResponse response ) 
    throws IOException {
        
        String url = request.getQueryString().replaceFirst("url=", "");        

        try {
            String responseFromCall = serviceCaller.callHttpUrlGET(new URL(url));
            InputStream inXSLT = request.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/WfsToHtml.xsl");
            response.getWriter().println( gmlToKml.convert(responseFromCall, inXSLT, url));
        } catch (IOException ex) {
            log.error("geologicUnitPopup: ", ex);
        }
    }
    
}
