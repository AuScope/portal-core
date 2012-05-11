package org.auscope.portal.server.web.controllers;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.manager.download.DownloadResponse;
import org.auscope.portal.server.util.FileIOUtil;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.service.PortalServiceException;
import org.auscope.portal.server.web.view.JSONView;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * An abstract controller for providing portal 'standard' JSON response types
 * in the form of a ModelAndView object.
 *
 * Also contains a few utilities for common response types (eg: Zip Streams)
 *
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
        model.put("msg", message);
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
     * Generates a JSON response containing WFS response info
     * @param success The result of the operation
     * @param gml The raw GML response
     * @param kml The transformed KML response
     * @param method The method used to make the request (used for populating debug info)
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(boolean success, String gml, String kml, HttpMethodBase method) {

        if (kml == null || kml.isEmpty()) {
            log.error(String.format("Transform failed", gml));
            return generateJSONResponseMAV(false, null, ErrorMessages.OPERATION_FAILED);
        }

        ModelMap data = new ModelMap();

        data.put("gml", gml);
        data.put("kml", kml);

        ModelMap debug = makeDebugInfoModel(method);

        return generateJSONResponseMAV(success, data, "", debug);
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
            debugInfo.put("url", request.getURI().toString());
        } catch (URIException e) {
            log.debug("Unable to generate URI from request", e);
            debugInfo.put("url", String.format("Error Generating URI - %1$s", e.getMessage()));
        }
        if (request instanceof PostMethod) {
            RequestEntity entity = ((PostMethod) request).getRequestEntity();
            if (entity instanceof StringRequestEntity) {
                debugInfo.put("info", ((StringRequestEntity) entity).getContent());
            }
        }

        return debugInfo;
    }

    /**
     * Writes output to input via an in memory buffer of a certain size
     * @param input The input stream
     * @param output The output stream (will receive input's bytes)
     * @param bufferSize The size (in bytes) of the in memory buffer
     * @throws IOException
     */
    protected void writeInputToOutputStream(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int dataRead;
        do {
            dataRead = input.read(buffer, 0, buffer.length);
            if (dataRead > 0) {
                output.write(buffer, 0, dataRead);
            }
        } while (dataRead != -1);
    }

    /**
     * Writes a series of DownloadResponse objects to a zip stream, each
     * download response will be put into a separate zip entry.
     *
     * @param gmlDownloads The download responses
     * @param zout The stream to receive the zip entries
     */
    protected void writeResponseToZip(List<DownloadResponse> gmlDownloads, ZipOutputStream zout) throws IOException {
        for (int i = 0; i < gmlDownloads.size(); i++) {
            DownloadResponse download = gmlDownloads.get(i);
            String entryName = new SimpleDateFormat((i + 1) + "_yyyyMMdd_HHmmss").format(new Date()) + ".xml";

            // Check that attempt to request is successful
            if (!download.hasException()) {
                InputStream stream = download.getResponseAsStream();

                //Write stream into the zip entry
                zout.putNextEntry(new ZipEntry(entryName));
                writeInputToOutputStream(stream, zout, 1024 * 1024);
                zout.closeEntry();
            } else {
                writeErrorToZip(zout, download.getRequestURL(), download.getException(), entryName + ".error");
            }
        }
    }

    /**
     * Writes an error to a zip stream.
     *
     * @param zout the zout
     * @param debugQuery the debug query
     * @param exceptionToPrint the exception to print
     * @param errorFileName The name of the error file in the zip (defaults to 'error.txt')
     */
    protected void writeErrorToZip(ZipOutputStream zout, String debugQuery, Exception exceptionToPrint, String errorFileName) {
        String message = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            exceptionToPrint.printStackTrace(pw);
            message = String.format("An exception occured whilst requesting/parsing your WCS download.\r\n%1$s\r\nMessage=%2$s\r\n%3$s", debugQuery, exceptionToPrint.getMessage(), sw.toString());
        } finally {
            FileIOUtil.closeQuietly(pw);
            FileIOUtil.closeQuietly(sw);
        }

        try {
            zout.putNextEntry(new ZipEntry(errorFileName == null ? errorFileName : "error.txt"));
            zout.write(message.getBytes());
        } catch (IOException ex) {
            log.error("Couldnt create debug error.txt in output", ex);
        }
    }

    /**
     * Exception resolver that maps exceptions to views presented to the user.
     * @param e The exception
     * @param serviceUrl The Url of the actual service
     * @return ModelAndView object with error message
     */
    protected ModelAndView generateExceptionResponse(Throwable e, String serviceUrl) {
        return generateExceptionResponse(e, serviceUrl, null);
    }

    /**
     * Exception resolver that maps exceptions to views presented to the user.
     * @param e The exception
     * @param serviceUrl The Url of the actual service
     * @param request [Optional] Specify the request object that was used to make the HTTP WFS request. Its contents will be included for debug purposes
     * @return ModelAndView object with error message
     */
    protected ModelAndView generateExceptionResponse(Throwable e, String serviceUrl, HttpMethodBase request) {
        log.debug(String.format("Exception! serviceUrl='%1$s'", serviceUrl), e);

        //Portal service exceptions wrap existing exceptions with a culprit HttpMethodBase
        if (e instanceof PortalServiceException) {
            PortalServiceException portalServiceEx = (PortalServiceException) e;
            return generateExceptionResponse(portalServiceEx.getCause(), serviceUrl, portalServiceEx.getRootMethod());
        }

        // Service down or host down
        if (e instanceof ConnectException || e instanceof UnknownHostException) {
            return this.generateJSONResponseMAV(false, null, ErrorMessages.UNKNOWN_HOST_OR_FAILED_CONNECTION, makeDebugInfoModel(request));
        }

        // Timeouts
        if (e instanceof ConnectTimeoutException) {
            return this.generateJSONResponseMAV(false, null, ErrorMessages.OPERATION_TIMOUT, makeDebugInfoModel(request));
        }

        if (e instanceof SocketTimeoutException) {
            return this.generateJSONResponseMAV(false, null, ErrorMessages.OPERATION_TIMOUT, makeDebugInfoModel(request));
        }

        // An error we don't specifically handle or expect
        return this.generateJSONResponseMAV(false, null, ErrorMessages.FILTER_FAILED, makeDebugInfoModel(request));
    }
}
