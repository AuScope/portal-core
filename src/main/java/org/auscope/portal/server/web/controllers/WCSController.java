package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.domain.wcs.DescribeCoverageRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.IWCSDescribeCoverageMethodMaker;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller that attempts to provide functions for use by the generic WCS use case.
 *
 * @author vot002
 *
 */
@Controller
public class WCSController {
    protected final Log logger = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;
    private IWCSGetCoverageMethodMaker getCoverageMethodMaker;
    private IWCSDescribeCoverageMethodMaker describeCoverageMethodMaker;

    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    @Autowired
    public WCSController(HttpServiceCaller serviceCaller, IWCSGetCoverageMethodMaker methodMaker, IWCSDescribeCoverageMethodMaker describeCoverageMethodMaker, PortalPropertyPlaceholderConfigurer hostConfigurer) {
        this.serviceCaller = serviceCaller;
        this.getCoverageMethodMaker = methodMaker;
        this.describeCoverageMethodMaker = describeCoverageMethodMaker;
        this.hostConfigurer = hostConfigurer;
    }

    private String generateOutputFilename(String layerName, String format) throws IllegalArgumentException {
        if (format.toLowerCase().contains("geotiff"))
            return String.format("%1$s.tiff", layerName);
        else if (format.toLowerCase().contains("netcdf"))
            return String.format("%1$s.nc", layerName);
        else
            return String.format("%1$s.%2$s", layerName, format);
    }

    private String parseTimeConstraint(final String[] timePositions,
                                 final String timePeriodFrom,
                                 final String timePeriodTo,
                                 final String timePeriodResolution) throws ParseException {
        String timeString = null;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        outputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        //We will receive a list of time positions
        if (timePositions != null && timePositions.length > 0) {
            StringBuilder sb = new StringBuilder();

            for (String s : timePositions) {
                if (s != null && !s.isEmpty()) {

                    Date d = inputFormat.parse(s);
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(outputFormat.format(d));
                }
            }

            timeString = sb.toString();
        //or an actual time period
        } else if (timePeriodFrom != null && timePeriodTo != null && !timePeriodFrom.isEmpty() && !timePeriodTo.isEmpty()) {

            Date from = inputFormat.parse(timePeriodFrom);
            Date to = inputFormat.parse(timePeriodTo);

            timeString = String.format("%1$s/%2$s", outputFormat.format(from), outputFormat.format(to));
            if (timePeriodResolution != null && !timePeriodResolution.isEmpty())  {
                timeString += String.format("/%1$s", timePeriodResolution);
            }
        }

        return timeString;
    }

    private void closeZipWithError(ZipOutputStream zout,String debugQuery, Exception exceptionToPrint) {
        String message = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            exceptionToPrint.printStackTrace(pw);
            message = String.format("An exception occured whilst requesting/parsing your WCS download.\r\n%1$s\r\nMessage=%2$s\r\n%3$s",debugQuery, exceptionToPrint.getMessage(), sw.toString());
        } finally {
            try {
                if(pw != null)  pw.close();
                if(sw != null)  sw.close();
            } catch (Exception ignore) {}
        }

        try {
            zout.putNextEntry(new ZipEntry("error.txt"));

            zout.write(message.getBytes());
        } catch (Exception ex) {
            logger.error("Couldnt create debug error.txt in output", ex);
        } finally {
            try {
                zout.close();
            } catch (Exception ex) {}
        }
    }

    /**
     *
     * @param customParamValues a list of PARAMETER=VALUE
     * @param customParamIntervals a list of PARAMETER=MIN/MAX/RESOLUTION
     * @return
     */
    private Map<String, String> generateCustomParamMap(final String[] customParamValues) {
        Map<String, String> customKvps = new HashMap<String, String>();

        if (customParamValues != null) {
            for (String kvpString : customParamValues) {
                String[] kvp = kvpString.split("=");
                if (kvp.length != 2)
                    throw new IllegalArgumentException("Couldnt parse customParamValue " + kvpString);

                //This is a sanity check to ensure we are getting all numbers
                String[] values = kvp[1].split("/");
                for (String value : values) {
                    try {
                        Double.parseDouble(value);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException(String.format("Couldnt parse double from '%1$s' in customParam '%2$s'", value, kvpString));
                    }
                }

                String valueList = customKvps.get(kvp[0]);
                if (valueList == null)
                    valueList = "";
                else
                    valueList += ",";

                valueList += kvp[1];
                customKvps.put(kvp[0], valueList);
            }
        }

        return customKvps;
    }

    /**
     * A function that given the parameters for a WCS GetCovereage request will make the request
     * on behalf of the user and return the results in a zipped file.
     *
     * One set of outputWidth/outputHeight or outputResX/outputResy must be specified
     *
     * One of a BBOX constraint or a TIMEPERIOD/TIMEPOSITION constraint must be specified
     *
     * You cannot specify both a TIMEPERIOD and TIMEPOSITION constraint
     *
     * @param serviceUrl The remote URL to query
     * @param layerName The coverage layername to request
     * @param downloadFormat Either [GeoTIFF, NetCDF]
     * @param inputCrs the coordinate reference system to query
     * @param outputWidth [Optional] Width of output dataset (Not compatible with outputResX/outputResY)
     * @param outputHeight [Optional] Height of output dataset (Not compatible with outputResX/outputResY)
     * @param outputResX [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputResY [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputCrs [Optional] The Coordinate reference system of the output data
     * @param northBoundLatitude [Optional] [BBOX] A point on the bounding box
     * @param southBoundLatitude [Optional] [BBOX] A point on the bounding box
     * @param eastBoundLongitude [Optional] [BBOX] A point on the bounding box
     * @param westBoundLongitude [Optional] [BBOX] A point on the bounding box
     * @param timePositions [Optional] [TIMEPOSITION] A list of time positions to query for. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodFrom [Optional] [TIMEPERIOD] a time range start. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodTo [Optional] [TIMEPERIOD] a time range end. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodResolution [Optional] [TIMEPERIOD] a time range resolution (not required for time period)
     * @param customParamValue [Optional] A list of strings in the form "PARAMETER=VALUE" or "PARAMETER=MIN/MAX/RES" which will be used for compound parameter filtering in the request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadWCSAsZip.do")
    public void downloadWCSAsZip(@RequestParam("serviceUrl") final String serviceUrl,
                                 @RequestParam("layerName") final String layerName,
                                 @RequestParam("downloadFormat") final String downloadFormat,
                                 @RequestParam("inputCrs") final String inputCrs,
                                 @RequestParam(required=false, defaultValue="0", value="outputWidth") final int outputWidth,
                                 @RequestParam(required=false, defaultValue="0", value="outputHeight") final int outputHeight,
                                 @RequestParam(required=false, defaultValue="0", value="outputResX") final double outputResX,
                                 @RequestParam(required=false, defaultValue="0", value="outputResY") final double outputResY,
                                 @RequestParam(required=false, value="outputCrs") final String outputCrs,
                                 @RequestParam(required=false, defaultValue="0",  value="northBoundLatitude") final double northBoundLatitude,
                                 @RequestParam(required=false, defaultValue="0", value="southBoundLatitude") final double southBoundLatitude,
                                 @RequestParam(required=false, defaultValue="0", value="eastBoundLongitude") final double eastBoundLongitude,
                                 @RequestParam(required=false, defaultValue="0", value="westBoundLongitude") final double westBoundLongitude,
                                 @RequestParam(required=false, value="timePosition") final String[] timePositions,
                                 @RequestParam(required=false, value="timePeriodFrom") final String timePeriodFrom,
                                 @RequestParam(required=false, value="timePeriodTo") final String timePeriodTo,
                                 @RequestParam(required=false, value="timePeriodResolution") final String timePeriodResolution,
                                 @RequestParam(required=false, value="customParamValue") final String[] customParamValues,
                                HttpServletResponse response) throws Exception {

        String outFileName = generateOutputFilename(layerName, downloadFormat);
        String timeString = parseTimeConstraint(timePositions, timePeriodFrom, timePeriodTo, timePeriodResolution);

        Map<String, String> customParams = generateCustomParamMap(customParamValues);

        CSWGeographicBoundingBox bbox = null;
        if (!(eastBoundLongitude == 0 &&
                westBoundLongitude == 0 &&
                northBoundLatitude == 0 &&
                southBoundLatitude == 0)) {
            bbox = new CSWGeographicBoundingBox();
            bbox.setEastBoundLongitude(eastBoundLongitude);
            bbox.setSouthBoundLatitude(southBoundLatitude);
            bbox.setNorthBoundLatitude(northBoundLatitude);
            bbox.setWestBoundLongitude(westBoundLongitude);
        }

        logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' timeString='%3$s' layerName='%4$s'", serviceUrl, bbox, timeString, layerName));

        HttpMethodBase method = getCoverageMethodMaker.makeMethod(serviceUrl, layerName, downloadFormat,
                outputCrs, outputWidth, outputHeight, outputResX, outputResY, inputCrs, bbox, timeString, customParams);

        downloadWCSAsZip(outFileName, method, response);
    }

    /**
     * An internal function that handles using a HttpMethodBase and calling a remote service for the raw binary data that we zip and return to the user.
     * @param outputFileName The name of the file (in the zip output) that we will use
     * @param method the method used to make the request for data
     * @param response the servlet response that will receive the output binary data
     */
    protected void downloadWCSAsZip(String outputFileName,HttpMethodBase method, HttpServletResponse response) throws Exception {

        //Lets make the request and zip up the response before passing it back to the user
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=WCSDownload.zip;");

        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
        try{
            InputStream inData = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());

            zout.putNextEntry(new ZipEntry(outputFileName));

            //Read the input in 1MB chunks and don't stop till we run out of data
            byte[] buffer = new byte[1024 * 1024];
            int dataRead;
            do {
                dataRead = inData.read(buffer, 0, buffer.length);
                if (dataRead > 0) {
                    zout.write(buffer, 0, dataRead);
                }
            } while (dataRead != -1);

            zout.finish();
            zout.flush();
            zout.close();
        } catch (Exception ex) {
            logger.error("Failure downloading WCS - returning error message in ZIP response", ex);
            closeZipWithError(zout,method.getURI().toString(), ex);
        } finally {
            method.releaseConnection(); //Ensure this gets called otherwise we leak connections
        }
    }

    /**
     * Returns a DescribeCoverageRecord as a JSON Response representing the response
     *
     *  {
     *      success : true/false
     *      errorMsg : ''
     *      rawXml : [Can be null] <Set to the raw XML string returned from the DescribeCoverageResponse>
     *      records : [Can be null] <Set to the DescribeCoverageRecord list parsed from the rawXml>
     *  }
     *
     * @param serviceUrl
     * @param layerName
     * @return
     */
    @RequestMapping("/describeCoverage.do")
    public ModelAndView describeCoverage(String serviceUrl, String layerName) {


        HttpMethodBase method = null;

        try {
            method = describeCoverageMethodMaker.makeMethod(serviceUrl, layerName);
        } catch (Exception ex) {
            logger.error("Error generating method", ex);
            return getDescribeCoverageResponse(false, "Error generating request method. Are layerName and serviceUrl specified?", null, null);
        }

        String xmlResponse = null;
        try {
            xmlResponse = serviceCaller.getMethodResponseAsString(method, serviceCaller.getHttpClient());
        } catch (Exception ex) {
            logger.info("Error making request", ex);
            return getDescribeCoverageResponse(false, "Error occured whilst communicating to remote service: " + ex.getMessage(), null, null);
        }

        DescribeCoverageRecord[] records = null;
        try {
            records = DescribeCoverageRecord.parseRecords(xmlResponse);
        } catch (Exception ex) {
            logger.warn("Error parsing request", ex);
            return getDescribeCoverageResponse(false, "Error occured whilst parsing response: " + ex.getMessage(), xmlResponse, null);
        }

        return getDescribeCoverageResponse(true, "No errors found",xmlResponse, records );
    }

    private JSONModelAndView getDescribeCoverageResponse(boolean success, String errorMessage, String responseXml, DescribeCoverageRecord[] records ) {

        ModelMap response = new ModelMap();
        response.put("success", success);
        response.put("errorMsg", errorMessage);
        response.put("rawXml", responseXml);
        response.put("records", records);

        return new JSONModelAndView(response);
    }
}
