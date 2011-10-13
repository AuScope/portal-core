package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.stereotype.Controller;
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
public abstract class BaseWFSToKMLController extends BasePortalController {

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

        return generateJSONResponseMAV(true, data, "", makeDebugInfoModel(request));
    }

    /**
     * Generates a failure response with the specified message
     * @param message
     * @param method
     */
    protected ModelAndView makeModelAndViewFailure(String message, HttpMethodBase method) {
        return generateJSONResponseMAV(false, null, message, makeDebugInfoModel(method));
    }


    /**
     * Exception resolver that maps exceptions to views presented to the user
     * @param exception
     * @return ModelAndView object with error message
     */
    protected ModelAndView generateExceptionResponse(Exception e, String serviceUrl) {
        return generateExceptionResponse(e, serviceUrl, null);
    }

    /**
     * Exception resolver that maps exceptions to views presented to the user
     * @param e
     * @param serviceUrl
     * @param request Specify the request object that was used to make the HTTP WFS request. Its contents will be included for debug purposes
     * @return ModelAndView object with error message
     */
    protected ModelAndView generateExceptionResponse(Exception e, String serviceUrl, HttpMethodBase request) {
        log.error(String.format("Exception! serviceUrl='%1$s'", serviceUrl),e);

        // Service down or host down
        if(e instanceof ConnectException || e instanceof UnknownHostException) {
            return this.generateJSONResponseMAV(false, null, ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION, makeDebugInfoModel(request));
        }

        // Timeouts
        if(e instanceof ConnectTimeoutException) {
            return this.generateJSONResponseMAV(false, null, ErrorMessages.OPERATION_TIMOUT, makeDebugInfoModel(request));
        }

        if(e instanceof SocketTimeoutException) {
            return this.generateJSONResponseMAV(false, null, ErrorMessages.OPERATION_TIMOUT, makeDebugInfoModel(request));
        }

        // An error we don't specifically handle or expect
        return this.generateJSONResponseMAV(false, null, ErrorMessages.FILTER_FAILED, makeDebugInfoModel(request));
    }
}
