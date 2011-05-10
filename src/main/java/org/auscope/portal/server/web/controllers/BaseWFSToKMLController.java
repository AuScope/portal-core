package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Abstract base class for the various controllers to inherit from.
 * 
 * Provides basic methods/fields for controllers that seek to make a WFS request
 * and then transform the resulting WFS response into KML.
 * 
 * @author Josh Vote
 *
 */
@Controller
public abstract class BaseWFSToKMLController {
    
    protected final Log log = LogFactory.getLog(getClass());
    protected HttpServiceCaller httpServiceCaller;
    protected GmlToKml gmlToKml;
    
    /**
     * Assemble a call to convert GeoSciML into kml format
     * @param geoXML
     * @param httpRequest
     * @param serviceUrl
     */
    protected String convertToKml(String geoXML, HttpServletRequest httpRequest, String serviceUrl) {
        InputStream inXSLT = httpRequest.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/kml.xsl");

        return gmlToKml.convert(geoXML, inXSLT, serviceUrl);
    }

    /**
     * Turns a HttpRequest into a Map of 
     * url - the URI of request
     * info - [Optional] The body of the request if relevant (for POST)
     * @param request cannot be null
     * @return
     */
    protected Map<String,String> makeDebugInfo(HttpMethodBase request) {
        if (request == null) {
            return null;
        }
        
        Map<String,String> debugInfo = new HashMap<String,String>();
        try {
            debugInfo.put("url",request.getURI().toString());
        } catch (URIException e) {
            log.debug("Unable to generate URI from request", e);
            debugInfo.put("url",String.format("Error Generating URI - %1$s", e.getMessage()));
        }
        if (request instanceof PostMethod) {
            RequestEntity entity = ((PostMethod) request).getRequestEntity();
            if (entity instanceof StringRequestEntity) {
                debugInfo.put("info",((StringRequestEntity) entity).getContent());
            }
        }
        
        return debugInfo;
    }
    
    /**
     * Creates a generic response ModelAndView
     * @param success
     * @param message
     * @param data
     * @param debugInfo Optional - can be null or empty
     * @return
     */
    protected ModelAndView makeModelAndView(boolean success, String message, Object data) {
        return makeModelAndView(success, message, data, null);
    }
    
    /**
     * Creates a generic response ModelAndView
     * @param success
     * @param message
     * @param data
     * @param debugInfo Optional - can be null or empty
     * @return
     */
    protected ModelAndView makeModelAndView(boolean success, String message, Object data, Object debugInfo) {
        ModelMap model = new ModelMap();
        model.put("success", success);
        model.put("data", data);
        model.put("msg", message);
        if (debugInfo != null) {
            model.put("debugInfo", debugInfo);
        }
        
        return new JSONModelAndView(model);
    }
    
    /**
     * Create a success response
     * 
     * @param kmlBlob
     * @param gmlBlob
     * @param request Specify the request object that was used to make the HTTP WFS request. Its contents will be included for debug purposes
     * @return
     */
    protected ModelAndView makeModelAndViewKML(final String kmlBlob, final String gmlBlob) {
        return makeModelAndViewKML(kmlBlob, gmlBlob, null);
    }
    
    /**
     * Create a success response
     * 
     * @param kmlBlob
     * @param gmlBlob
     * @param request Specify the request object that was used to make the HTTP WFS request. Its contents will be included for debug purposes
     * @return
     */
    protected ModelAndView makeModelAndViewKML(final String kmlBlob, final String gmlBlob, HttpMethodBase request) {
        final Map<String,String> data = new HashMap<String,String>();
        data.put("kml", kmlBlob);
        data.put("gml", gmlBlob);

        return makeModelAndView(true, "", data, makeDebugInfo(request));
    }
    
    /**
     * Create a failure response
     *
     * @param message
     * @return
     */
    protected ModelAndView makeModelAndViewFailure(final String message) {
        return makeModelAndViewFailure(message, null);
    }

    /**
     * Create a failure response
     * @param message
     * @param request Specify the request object that was used to make the HTTP WFS request. Its contents will be included for debug purposes
     * @return
     */
    protected ModelAndView makeModelAndViewFailure(final String message, HttpMethodBase request) {
        return makeModelAndView(false, message, null, makeDebugInfo(request));
    }
    
    /**
     * Exception resolver that maps exceptions to views presented to the user
     * @param exception
     * @return ModelAndView object with error message
     */
    protected ModelAndView handleExceptionResponse(Exception e, String serviceUrl) {
        return handleExceptionResponse(e, serviceUrl, null);
    }

    /**
     * Exception resolver that maps exceptions to views presented to the user
     * @param e
     * @param serviceUrl
     * @param request Specify the request object that was used to make the HTTP WFS request. Its contents will be included for debug purposes
     * @return ModelAndView object with error message
     */
    protected ModelAndView handleExceptionResponse(Exception e, String serviceUrl, HttpMethodBase request) {

        log.error(String.format("Exception! serviceUrl='%1$s'", serviceUrl),e);

        // Service down or host down
        if(e instanceof ConnectException || e instanceof UnknownHostException) {
            return this.makeModelAndViewFailure(ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION, request);
        }

        // Timouts
        if(e instanceof ConnectTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT, request);
        }

        if(e instanceof SocketTimeoutException) {
            return this.makeModelAndViewFailure(ErrorMessages.OPERATION_TIMOUT, request);
        }

        // An error we don't specifically handle or expect
        return makeModelAndViewFailure(ErrorMessages.FILTER_FAILED, request);
    }
}
