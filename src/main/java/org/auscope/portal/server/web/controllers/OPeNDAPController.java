package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.opendap.AbstractViewVariable;
import org.auscope.portal.server.domain.opendap.OPeNDAPGetDataMethodMaker;
import org.auscope.portal.server.domain.opendap.OPeNDAPGetDataMethodMaker.OPeNDAPFormat;
import org.auscope.portal.server.domain.opendap.ViewVariableFactory;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ucar.nc2.dataset.NetcdfDataset;


/**
 * A controller for marshaling requests for an arbitrary OPeNDAP resource.
 * @author vot002
 *
 */
@Controller
public class OPeNDAPController extends BasePortalController {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

    /** The service caller. */
    private HttpServiceCaller serviceCaller;

    /** The get data method maker. */
    private OPeNDAPGetDataMethodMaker getDataMethodMaker;

    /**
     * Instantiates a new opendap controller.
     *
     * @param serviceCaller the service caller
     * @param getDataMethodMaker the get data method maker
     */
    @Autowired
    public OPeNDAPController(HttpServiceCaller serviceCaller,
            OPeNDAPGetDataMethodMaker getDataMethodMaker) {
        super();
        this.serviceCaller = serviceCaller;
        this.getDataMethodMaker = getDataMethodMaker;
    }

    /**
     * Downloads the list of supported download formats (one of these values
     * should be passed to the opendapMakeRequest.do handler).
     *
     * @return the supported formats
     */
    @RequestMapping("/opendapGetSupportedFormats.do")
    public ModelAndView getSupportedFormats() {
        JSONArray items = new JSONArray();

        items.add(new String[] {"ascii"});
        items.add(new String[] {"dods"});

        return new JSONModelAndView(items);
    }

    /**
     * Downloads the list of queryable variables from the given OPeNDAP Service.
     *
     * JSON ResponseFormat = [ViewVariable]
     *
     * @param opendapUrl The remote service URL to query
     * @param variableName the variable name
     * @return the variables
     * @throws Exception the exception
     */
    @RequestMapping("/opendapGetVariables.do")
    public ModelAndView getVariables(@RequestParam("opendapUrl") final String opendapUrl,
                                     @RequestParam(required=false, value="variableName") final String variableName) throws Exception {

        //Open our connection
        NetcdfDataset ds = null;
        try {
            ds = NetcdfDataset.openDataset(opendapUrl);
        } catch (IOException ex) {
            log.info(String.format("Error connecting to '%1$s'", opendapUrl));
            log.debug("Exception...", ex);
            return generateJSONResponseMAV(false, null, String.format("Error connecting to '%1$s'", opendapUrl));
        }

        //Attempt to parse our response
        try {
            AbstractViewVariable[] vars = ViewVariableFactory.fromNetCDFDataset(ds, variableName);
            return generateJSONResponseMAV(true, vars, "");
        } catch (IOException ex) {
            log.error(String.format("Error parsing from '%1$s'", opendapUrl), ex);
            return generateJSONResponseMAV(false, null, String.format("An error has occured whilst reading data from '%1$s'", opendapUrl));
        }
    }

    /**
     * Close zip with error.
     *
     * @param zout the zout
     * @param debugQuery the debug query
     * @param exceptionToPrint the exception to print
     */
    private void closeZipWithError(ZipOutputStream zout,String debugQuery, Exception exceptionToPrint) {
        String message = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            exceptionToPrint.printStackTrace(pw);
            message = String.format("An exception occured whilst requesting/parsing your WCS download.\r\n%1$s\r\nMessage=%2$s\r\n%3$s", debugQuery, exceptionToPrint.getMessage(), sw.toString());
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
                if (sw != null) {
                    sw.close();
                }
            } catch (IOException ignore) {
                log.debug("Exception: " + ignore.getMessage());
            }
        }

        try {
            zout.putNextEntry(new ZipEntry("error.txt"));

            zout.write(message.getBytes());
        } catch (IOException ex) {
            log.error("Couldnt create debug error.txt in output", ex);
        } finally {
            try {
                zout.close();
            } catch (IOException ex) {
                log.debug("Exception: " + ex.getMessage());
            }
        }
    }

    /**
     * Makes a request to an OPeNDAP service for data within given constraints.
     *
     * @param opendapUrl The remote service URL to query
     * @param downloadFormat How the response data should be formatted
     * @param constraintsJson [Optional] Must be an object with an element 'constraints' set to a list variable/griddedVariable (See getVariables for more info on JSON schema)
     * @param response the response
     * @throws Exception the exception
     */
    @RequestMapping("/opendapMakeRequest")
    public void makeRequest(@RequestParam("opendapUrl") final String opendapUrl,
            @RequestParam("downloadFormat") final String downloadFormat,
            @RequestParam(required=false, value="constraints") final String constraintsJson,
            HttpServletResponse response) throws Exception {

        log.trace(String.format("opendapUrl='%1$s'", opendapUrl));
        log.trace(String.format("downloadFormat='%1$s'", downloadFormat));
        log.trace(String.format("constraintsJson='%1$s'", constraintsJson));

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

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "inline; filename=OPeNDAPDownload.zip;");

        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());

        //Parse our constraint list (can be null)
        AbstractViewVariable[] constraints = new AbstractViewVariable[0];
        if (constraintsJson != null && !constraintsJson.isEmpty()) {
            JSONObject obj = JSONObject.fromObject(constraintsJson);
            constraints = ViewVariableFactory.fromJSONArray(obj.getJSONArray("constraints"));
        }

        //Open our connection
        NetcdfDataset ds = null;
        try {
            ds = NetcdfDataset.openDataset(opendapUrl);
        } catch (IOException ex) {
            log.info(String.format("Error connecting to '%1$s'", opendapUrl));
            log.debug("Exception...", ex);
            closeZipWithError(zout, String.format("Error connecting to '%1$s'", opendapUrl), ex);
            return;
        }

        HttpMethodBase method = null;
        try {
            method = getDataMethodMaker.getMethod(opendapUrl, format, ds, constraints);

            zout.putNextEntry(new ZipEntry("query.txt"));
            zout.write(method.getURI().toString().getBytes());

            zout.putNextEntry(new ZipEntry(outputFileName));

            InputStream inData = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());

            //Read the input in 1MB chunks and don't stop till we run out of data
            writeInputToOutputStream(inData, zout, 1024 * 1024);

            zout.finish();
            zout.flush();
            zout.close();
        } catch (Exception ex) {
            log.info(String.format("Error requesting data from '%1$s'", opendapUrl));
            log.debug("Exception...", ex);
            closeZipWithError(zout, String.format("Error requesting data from '%1$s'", opendapUrl), ex);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
}
