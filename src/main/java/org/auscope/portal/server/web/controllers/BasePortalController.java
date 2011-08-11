package org.auscope.portal.server.web.controllers;


import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.view.JSONView;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * An abstract controller for providing portal 'standard' JSON response types
 * in the form of a ModelAndView object.
 * @author Josh Vote
 *
 */
public abstract class BasePortalController {

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Utility method generating the 'standard' Portal response model.
     *
     * @param success The result of the operation
     * @param data [Optional] Raw response information. Can be null, must be serialisable into a JSON object.
     * @param message [Optional] A string indicating more information about status of information
     * @param debugInfo [Optional] Debugging Information. Can be null, must be serialisable into a JSON object
     * @param matchedResults [Optional] The number of results available (not necessarily the count of data)
     * @return
     */
    protected ModelMap generateResponseModel(boolean success, Object data, Integer matchedResults, String message, Object debugInfo) {
        ModelMap model = new ModelMap();

        model.put("data", data);
        model.put("success", success);
        if (matchedResults != null) {
            model.put("totalResults", matchedResults);
        }
        model.put("msg", success);
        if (debugInfo != null) {
            model.put("debugInfo", debugInfo);
        }

        return model;
    }

    /**
     * Utility method to generate a standard ModelAndView response for rendering JSON
     * @param success The result of the operation
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(boolean success) {
        return generateJSONResponseMAV(success, null, "", null, null);
    }

    /**
     * Utility method to generate a standard ModelAndView response for rendering JSON
     * @param success The result of the operation
     * @param data [Optional] Raw response information. Can be null, must be serialisable into a JSON object.
     * @param message [Optional] A string indicating more information about status of information
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(boolean success, Object data, String message) {
        return generateJSONResponseMAV(success, data, message, null, null);
    }

    /**
     * Utility method to generate a standard ModelAndView response for rendering JSON
     * @param success The result of the operation
     * @param data [Optional] Raw response information. Can be null, must be serialisable into a JSON object.
     * @param message [Optional] A string indicating more information about status of information
     * @param matchedResults [Optional] The total amount of data available (not necessarily the count of data)
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(boolean success, Object data, Integer matchedResults, String message) {
        return generateJSONResponseMAV(success, data, message, matchedResults, null);
    }

    /**
     * Utility method to generate a standard ModelAndView response for rendering JSON
     * @param success The result of the operation
     * @param data [Optional] Raw response information. Can be null, must be serialisable into a JSON object.
     * @param message [Optional] A string indicating more information about status of information
     * @param debugInfo [Optional] Debugging Information. Can be null, must be serialisable into a JSON object
     * @param matchedResults [Optional] The total amount of data available (not necessarily the count of data)
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(boolean success, Object data, String message, Object debugInfo) {
        return generateJSONResponseMAV(success, data, message, null, debugInfo);
    }

    /**
     * Utility method to generate a standard ModelAndView response for rendering JSON
     * @param success The result of the operation
     * @param data [Optional] Raw response information. Can be null, must be serialisable into a JSON object.
     * @param message [Optional] A string indicating more information about status of information
     * @param debugInfo [Optional] Debugging Information. Can be null, must be serialisable into a JSON object
     * @param matchedResults [Optional] The total amount of data available (not necessarily the count of data)
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(boolean success, Object data, String message, Integer matchedResults, Object debugInfo) {
        JSONView view = new JSONView();
        ModelMap model = generateResponseModel(success, data, matchedResults, message, debugInfo);

        return new ModelAndView(view, model);
    }

    /**
     * Utility method to generate a HTML MAV response. This will be identical in content to generateJSONResponseMAV but
     * will be set to use a HTML content type. Use this for overcoming weirdness with Ext JS and file uploads.
     * @param success The result of the operation
     * @param data [Optional] Raw response information. Can be null, must be serialisable into a JSON object.
     * @param message [Optional] A string indicating more information about status of information
     * @return
     */
    protected ModelAndView generateHTMLResponseMAV(boolean success, Object data, String message) {
        return generateHTMLResponseMAV(success, data, message, null);
    }

    /**
     * Utility method to generate a HTML MAV response. This will be identical in content to generateJSONResponseMAV but
     * will be set to use a HTML content type. Use this for overcoming weirdness with Ext JS and file uploads.
     * @param success The result of the operation
     * @param data [Optional] Raw response information. Can be null, must be serialisable into a JSON object.
     * @param message [Optional] A string indicating more information about status of information
     * @param debugInfo [Optional] Debugging Information. Can be null, must be serialisable into a JSON object
     * @return
     */
    protected ModelAndView generateHTMLResponseMAV(boolean success, Object data, String message, Object debugInfo) {
        JSONView view = new JSONView();
        view.setContentType("text/html");
        ModelMap model = generateResponseModel(success, data, null, message, debugInfo);

        return new ModelAndView(view, model);
    }

    /**
     * Turns a HttpRequest into a Map of
     * url - the URI of request
     * info - [Optional] The body of the request if relevant (for POST)
     * @param request cannot be null
     * @return
     */
    protected ModelMap makeDebugInfoModel(HttpMethodBase request) {
        if (request == null) {
            return null;
        }

        ModelMap debugInfo = new ModelMap();
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
}
