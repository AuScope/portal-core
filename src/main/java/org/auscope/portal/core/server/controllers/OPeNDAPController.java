package org.auscope.portal.core.server.controllers;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.OpendapService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker.OPeNDAPFormat;
import org.auscope.portal.core.services.responses.opendap.AbstractViewVariable;
import org.auscope.portal.core.services.responses.opendap.ViewVariableFactory;
import org.auscope.portal.core.util.FileIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller for marshaling requests for an arbitrary OPeNDAP resource.
 *
 * @author vot002
 *
 */
@Controller
public class OPeNDAPController extends BasePortalController {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

    /** The opendap service. */
    private OpendapService opendapService;

    private int BUFFERSIZE = 1024 * 1024;

    /**
     * Instantiates a new opendap controller.
     *
     * @param serviceCaller
     *            the service caller
     * @param getDataMethodMaker
     *            the get data method maker
     */
    @Autowired
    public OPeNDAPController(OpendapService opendapService) {
        super();
        this.opendapService = opendapService;
    }

    /**
     * Downloads the list of supported download formats (one of these values should be passed to the opendapMakeRequest.do handler).
     *
     * @return the supported formats
     */
    @RequestMapping("/opendapGetSupportedFormats.do")
    public ModelAndView getSupportedFormats() {
        return generateJSONResponseMAV(true, new String[] {"ascii", "dods"}, "");
    }

    /**
     * Downloads the list of queryable variables from the given OPeNDAP Service.
     *
     * JSON ResponseFormat = [ViewVariable]
     *
     * @param opendapUrl
     *            The remote service URL to query
     * @param variableName
     *            the variable name
     * @return the variables
     * @throws Exception
     *             the exception
     */
    @RequestMapping("/opendapGetVariables.do")
    public ModelAndView getVariables(@RequestParam("opendapUrl") final String opendapUrl,
            @RequestParam(required = false, value = "variableName") final String variableName) throws Exception {

        //Attempt to parse our response
        try {
            AbstractViewVariable[] vars = opendapService.getVariables(opendapUrl, variableName);
            return generateJSONResponseMAV(true, vars, "");
        } catch (Exception ex) {
            log.error(String.format("Error parsing from '%1$s'", opendapUrl), ex);
            return generateJSONResponseMAV(false, null,
                    String.format("An error has occured whilst reading data from '%1$s'", opendapUrl));
        }
    }

    /**
     * Makes a request to an OPeNDAP service for data within given constraints.
     *
     * @param opendapUrl
     *            The remote service URL to query
     * @param downloadFormat
     *            How the response data should be formatted
     * @param constraintsJson
     *            [Optional] Must be an object with an element 'constraints' set to a list variable/griddedVariable (See getVariables for more info on JSON
     *            schema)
     * @param response
     *            the response
     * @throws Exception
     *             the exception
     */
    @RequestMapping("/opendapMakeRequest.do")
    public void makeRequest(@RequestParam("opendapUrl") final String opendapUrl,
            @RequestParam("downloadFormat") final String downloadFormat,
            @RequestParam(required = false, value = "constraints") final String constraintsJson,
            @RequestParam(required = false, value = "ftpURL") final String ftpURL,
            HttpServletResponse response) throws Exception {

        log.trace(String.format("opendapUrl='%1$s'", opendapUrl));
        log.trace(String.format("downloadFormat='%1$s'", downloadFormat));
        log.trace(String.format("constraintsJson='%1$s'", constraintsJson));
        log.trace(String.format("ftpURL='%1$s'", ftpURL));

        OPeNDAPFormat format;
        String outputFileName;
        if (downloadFormat.equals("ascii")) {
            format = OPeNDAPFormat.ASCII;
            outputFileName = "data.txt";
        } else if (downloadFormat.equals("dods")) {
            format = OPeNDAPFormat.DODS;
            outputFileName = "data.bin";
        } else {
            throw new IllegalArgumentException("Unsupported format " + downloadFormat);
        }

        //Parse our constraint list (can be null)
        AbstractViewVariable[] constraints = new AbstractViewVariable[0];
        if (constraintsJson != null && !constraintsJson.isEmpty()) {
            JSONObject obj = JSONObject.fromObject(constraintsJson);
            constraints = ViewVariableFactory.fromJSONArray(obj.getJSONArray("constraints"));
        }

        // AUS-2287
        // The rest of this method will result in one of three outcomes:
        //  * Outcome 1: The request is successful - we send back a zip containing query.txt and data.[txt|bin].
        //  * Outcome 2: The request failed because it was too big - we send back a response indicating same.
        //  * Outcome 3: The request failed for some unknown reason - we send back a zip containing query.txt and an error.txt.
        String query = null;
        InputStream dataStream = null;
        PortalServiceException stashedException = null;
        ServletOutputStream servletOutputStream = response.getOutputStream();

        try {
            query = opendapService.getQueryDetails(opendapUrl, format, constraints);
            dataStream = opendapService.getData(opendapUrl, format, constraints);
        } catch (PortalServiceException ex) {
            Throwable cause = ex.getCause();
            String causeMessage = cause == null ? "" : cause.getMessage();

            if (causeMessage.contains("Request too big=")) {
                // Outcome 2:
                // Pull the information out of the exception and return it
                Pattern pattern = Pattern.compile("Request too big=([0-9\\.]+) Mbytes, max=([0-9\\.]+)");
                Matcher matcher = pattern.matcher(causeMessage);

                if (matcher.find()) {
                    String requestedSize = matcher.group(1);
                    String maximumSize = matcher.group(2);

                    // If we have an FTP URL we can add a link to it in the error message:
                    String ftpMessage = ftpURL != null && ftpURL.compareTo("") != 0 ?
                            String.format(
                                    "<br/>Alternatively, you can download the data directly from <a href=\"%s\">here</a>.",
                                    ftpURL)
                            : "";

                    String messageString = String
                            .format(
                                    "<html>Error:<br/>Your request has failed. The data you requested was %s MB but the maximum allowed by the server is %s MB.<br/>Please reduce the scope of your query and try again.%s</html>",
                                    requestedSize,
                                    maximumSize,
                                    ftpMessage);

                    servletOutputStream.write(messageString.getBytes());
                    servletOutputStream.close();
                    return;
                }
            }

            // Stash this exception for now, we'll add it to the zip output later.
            stashedException = ex;
        }

        // At this point we know we're going to be sending back a zip file:
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "inline; filename=OPeNDAPDownload.zip;");
        ZipOutputStream zout = new ZipOutputStream(servletOutputStream);

        if (query != null) {
            // This is where we add query.txt for Outcomes 1 & 3:
            zout.putNextEntry(new ZipEntry("query.txt"));
            zout.write(query.getBytes());
        }

        if (dataStream != null) {
            // Outcome 1:
            zout.putNextEntry(new ZipEntry(outputFileName));
            FileIOUtil.writeInputToOutputStream(dataStream, zout, BUFFERSIZE, false);
            FileIOUtil.closeQuietly(dataStream);
        }
        else if (stashedException != null) {
            // Outcome 3:
            FileIOUtil.writeErrorToZip(zout, String.format("Error connecting to '%1$s'", opendapUrl), stashedException,
                    "error.txt");
        }

        FileIOUtil.closeQuietly(zout);
    }
}
