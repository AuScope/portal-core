package org.auscope.portal.core.server.controllers;

import java.awt.Dimension;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.DescribeCoverageRecord;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.util.FileIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller that attempts to provide functions for use by the generic WCS use case.
 *
 * @author Josh Vote
 *
 */
@Controller
public class WCSController extends BasePortalController {
    private final Log logger = LogFactory.getLog(getClass());

    /** The format string view's are expected to use when working with this controller */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    private WCSService wcsService;

    private int BUFFERSIZE = 1024 * 1024;

    @Autowired
    public WCSController(WCSService wcsService) {
        this.wcsService = wcsService;
    }

    private String generateOutputFilename(String layerName, String format) throws IllegalArgumentException {
        if (format.toLowerCase().contains("geotiff"))
            return String.format("%1$s.tiff", layerName);
        else if (format.toLowerCase().contains("netcdf"))
            return String.format("%1$s.nc", layerName);
        else
            return String.format("%1$s.%2$s", layerName, format);
    }

    /**
     * Parses an array of date strings that are conforming to DATE_FORMAT into date objects
     * 
     * @param dateStrings
     * @return
     * @throws ParseException
     */
    private Date[] parseDates(final String[] dateStrings) throws ParseException {
        Date[] dates = new Date[dateStrings.length];
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);

        for (int i = 0; i < dateStrings.length; i++) {
            dates[i] = format.parse(dateStrings[i]);
        }

        return dates;
    }

    /**
     * Attempts to parse a time constraint from the listed time information. Returns null if no constraint can be generated
     * 
     * @param timePositions
     * @param timePeriodFrom
     * @param timePeriodTo
     * @param timePeriodResolution
     * @return
     * @throws ParseException
     */
    private TimeConstraint parseTimeConstraint(final String[] timePositions,
            final String timePeriodFrom,
            final String timePeriodTo,
            final String timePeriodResolution) throws ParseException {
        //We will receive a list of time positions
        if (timePositions != null && timePositions.length > 0) {
            return TimeConstraint.parseTimeConstraint(parseDates(timePositions));
            //or an actual time period
        } else if (timePeriodFrom != null && timePeriodTo != null && !timePeriodFrom.isEmpty()
                && !timePeriodTo.isEmpty()) {
            DateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT);
            Date from = inputFormat.parse(timePeriodFrom);
            Date to = inputFormat.parse(timePeriodTo);

            return TimeConstraint.parseTimeConstraint(from, to, timePeriodResolution);
        }

        return null;
    }

    /**
     *
     * @param customParamValues
     *            a list of PARAMETER=VALUE
     * @param customParamIntervals
     *            a list of PARAMETER=MIN/MAX/RESOLUTION
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
                        throw new IllegalArgumentException(String.format(
                                "Couldnt parse double from '%1$s' in customParam '%2$s'", value, kvpString));
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
     * A function that given the parameters for a WCS GetCovereage request will make the request on behalf of the user and return the results in a zipped file.
     *
     * One set of outputWidth/outputHeight or outputResX/outputResy must be specified
     *
     * One of a BBOX constraint or a TIMEPERIOD/TIMEPOSITION constraint must be specified
     *
     * You cannot specify both a TIMEPERIOD and TIMEPOSITION constraint
     *
     * @param serviceUrl
     *            The remote URL to query
     * @param layerName
     *            The coverage layername to request
     * @param downloadFormat
     *            Either [GeoTIFF, NetCDF]
     * @param inputCrs
     *            the coordinate reference system to query
     * @param outputWidth
     *            [Optional] Width of output dataset (Not compatible with outputResX/outputResY)
     * @param outputHeight
     *            [Optional] Height of output dataset (Not compatible with outputResX/outputResY)
     * @param outputResX
     *            [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with
     *            outputWidth/outputHeight)
     * @param outputResY
     *            [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with
     *            outputWidth/outputHeight)
     * @param outputCrs
     *            [Optional] The Coordinate reference system of the output data
     * @param northBoundLatitude
     *            [Optional] [BBOX] A point on the bounding box
     * @param southBoundLatitude
     *            [Optional] [BBOX] A point on the bounding box
     * @param eastBoundLongitude
     *            [Optional] [BBOX] A point on the bounding box
     * @param westBoundLongitude
     *            [Optional] [BBOX] A point on the bounding box
     * @param timePositions
     *            [Optional] [TIMEPOSITION] A list of time positions to query for. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodFrom
     *            [Optional] [TIMEPERIOD] a time range start. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodTo
     *            [Optional] [TIMEPERIOD] a time range end. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodResolution
     *            [Optional] [TIMEPERIOD] a time range resolution (not required for time period)
     * @param customParamValue
     *            [Optional] A list of strings in the form "PARAMETER=VALUE" or "PARAMETER=MIN/MAX/RES" which will be used for compound parameter filtering in
     *            the request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadWCSAsZip.do")
    public void downloadWCSAsZip(
            @RequestParam("serviceUrl") final String serviceUrl,
            @RequestParam("layerName") final String layerName,
            @RequestParam("downloadFormat") final String downloadFormat,
            @RequestParam("inputCrs") final String inputCrs,
            @RequestParam(required = false, value = "outputWidth") final Integer outputWidth,
            @RequestParam(required = false, value = "outputHeight") final Integer outputHeight,
            @RequestParam(required = false, value = "outputResX") final Double outputResX,
            @RequestParam(required = false, value = "outputResY") final Double outputResY,
            @RequestParam(required = false, value = "outputCrs") final String outputCrs,
            @RequestParam(required = false, defaultValue = "0", value = "northBoundLatitude") final double northBoundLatitude,
            @RequestParam(required = false, defaultValue = "0", value = "southBoundLatitude") final double southBoundLatitude,
            @RequestParam(required = false, defaultValue = "0", value = "eastBoundLongitude") final double eastBoundLongitude,
            @RequestParam(required = false, defaultValue = "0", value = "westBoundLongitude") final double westBoundLongitude,
            @RequestParam(required = false, value = "timePosition") final String[] timePositions,
            @RequestParam(required = false, value = "timePeriodFrom") final String timePeriodFrom,
            @RequestParam(required = false, value = "timePeriodTo") final String timePeriodTo,
            @RequestParam(required = false, value = "timePeriodResolution") final String timePeriodResolution,
            @RequestParam(required = false, value = "customParamValue") final String[] customParamValues,
            @RequestParam(required = false, value = "ftpURL") final String ftpURL,
            HttpServletResponse response) throws Exception {
        String outFileName = generateOutputFilename(layerName, downloadFormat);
        TimeConstraint timeConstraint = parseTimeConstraint(timePositions, timePeriodFrom, timePeriodTo,
                timePeriodResolution);
        Map<String, String> customParams = generateCustomParamMap(customParamValues);
        Dimension outputSize = null;
        Resolution outputResolution = null;

        if (outputWidth != null && outputHeight != null) {
            outputSize = new Dimension(outputWidth.intValue(), outputHeight.intValue());
        }

        if (outputResX != null && outputResY != null) {
            outputResolution = new Resolution(outputResX.doubleValue(), outputResY.doubleValue());
        }

        CSWGeographicBoundingBox bbox = null;
        if (!(eastBoundLongitude == 0 &&
                westBoundLongitude == 0 &&
                northBoundLatitude == 0 && southBoundLatitude == 0)) {
            bbox = new CSWGeographicBoundingBox();
            bbox.setEastBoundLongitude(eastBoundLongitude);
            bbox.setSouthBoundLatitude(southBoundLatitude);
            bbox.setNorthBoundLatitude(northBoundLatitude);
            bbox.setWestBoundLongitude(westBoundLongitude);
        }

        logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' timeString='%3$s' layerName='%4$s'", serviceUrl,
                bbox, timeConstraint, layerName));

        // AUS-2287
        // The rest of this method will result in one of three outcomes:
        //  * Outcome 1: The request is successful - we send back a zip containing a file named $outFileName.
        //  * Outcome 2: The request failed because it was (probably) too big - we send back a response indicating same.
        //  * Outcome 3: The request failed for some unknown reason - we send back a zip containing an error.txt.
        InputStream dataStream = null;
        PortalServiceException stashedException = null;
        ServletOutputStream servletOutputStream = response.getOutputStream();

        try {
            dataStream = wcsService.getCoverage(serviceUrl, layerName, downloadFormat, outputSize, outputResolution,
                    outputCrs, inputCrs, bbox, timeConstraint, customParams);
        } catch (PortalServiceException ex) {
            Throwable cause = ex.getCause();
            String causeMessage = cause == null ? "" : cause.getMessage();

            if (causeMessage == null || causeMessage.contains("<ServiceException>Unknown problem</ServiceException>")) {

                if (causeMessage == null) {
                    causeMessage = ex.getMessage();
                }

                // Outcome 2:

                // If we have an FTP URL we can add a link to it in the error message:
                String ftpMessage = ftpURL != null && ftpURL.compareTo("") != 0 ?
                        String.format(
                                "<br/>Alternatively, you can download the data directly from <a href=\"%s\">here</a>.",
                                ftpURL) : "";

                String messageString = String.format(
                        "<html>Error message: " + causeMessage
                                + "<br/>Your request has failed by unexpected reasons.%s</html>",
                        ftpMessage);

                //VT: Note Http 400 is a valid response from the service. https://jira.csiro.au/browse/AUS-2421
                messageString = String
                        .format(
                                "<html>Error message: "
                                        + causeMessage
                                        + "<br/>Your request has failed. This is likely due to the requested data exceeding the server&apos;s size limit.<br/>Please adjust your query and try again.%s</html>",
                                ftpMessage);

                servletOutputStream.write(messageString.getBytes());
                servletOutputStream.close();
                return;
            }

            // Stash this exception for now, we'll add it to the zip output later.
            stashedException = ex;
        }

        // At this point we know we're going to be sending back a zip file:
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "inline; filename=WCSDownload.zip;");
        ZipOutputStream zout = new ZipOutputStream(servletOutputStream);

        if (dataStream != null) {
            // Outcome 1:
            zout.putNextEntry(new ZipEntry(outFileName));
            FileIOUtil.writeInputToOutputStream(dataStream, zout, BUFFERSIZE, false);
            dataStream.close();
        }
        else if (stashedException != null) {
            // Outcome 3:
            FileIOUtil.writeErrorToZip(zout, "", stashedException, "error.txt");
        }

        FileIOUtil.closeQuietly(zout);
    }

    /**
     * Returns a DescribeCoverageRecord as a JSON Response representing the response
     *
     * { success : true/false errorMsg : '' rawXml : [Can be null] <Set to the raw XML string returned from the DescribeCoverageResponse> records : [Can be
     * null] <Set to the DescribeCoverageRecord list parsed from the rawXml> }
     *
     * @param serviceUrl
     * @param layerName
     * @return
     */
    @RequestMapping("/describeCoverage.do")
    public ModelAndView describeCoverage(String serviceUrl, String layerName) {
        DescribeCoverageRecord[] records = null;
        try {
            records = wcsService.describeCoverage(serviceUrl, layerName);
        } catch (Exception ex) {
            logger.error("Error describing coverage", ex);
            return generateJSONResponseMAV(false, null,
                    "Error occured whilst communicating to remote service: " + ex.getMessage());
        }

        return generateJSONResponseMAV(true, records, "");
    }
}
