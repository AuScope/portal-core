package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker.WCSDownloadFormat;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    private IWCSGetCoverageMethodMaker methodMaker;

    private PortalPropertyPlaceholderConfigurer hostConfigurer;
    
    @Autowired
    public WCSController(HttpServiceCaller serviceCaller, IWCSGetCoverageMethodMaker methodMaker, PortalPropertyPlaceholderConfigurer hostConfigurer) {
        this.serviceCaller = serviceCaller;      
        this.methodMaker = methodMaker;
        this.hostConfigurer = hostConfigurer;
    }
    
    private WCSDownloadFormat parseDownloadFormat(String downloadFormat) throws IllegalArgumentException {
        if (downloadFormat.toLowerCase().equals("geotiff")) {
            return WCSDownloadFormat.GeoTIFF;
        } else if (downloadFormat.toLowerCase().equals("netcdf")) {
            return WCSDownloadFormat.NetCDF;
        } else if (downloadFormat.toLowerCase().equals("geotiff_float")) {
            return WCSDownloadFormat.GeoTIFF_Float;
        } else {
            throw new IllegalArgumentException("Unrecognized format: " + downloadFormat);
        }
    }
    
    private String generateOutputFilename(String layerName, WCSDownloadFormat format) throws IllegalArgumentException {
        switch (format) {
        case GeoTIFF:
            return String.format("%1$s.tiff", layerName);
        case NetCDF:
            return String.format("%1$s.nc", layerName);
        default:
            throw new IllegalArgumentException("Unsupported format: " + format.toString());
        }
    }
    
    /**
     * A function that given the parameters for a WCS GetCovereage request will make the request
     * on behalf of the user and return the results in a zipped file.
     * 
     * One set of outputWidth/outputHeight or outputResX/outputResy must be specified
     * 
     * One of usingBboxConstraint / usingTimeConstraint must be specified
     * 
     * @param serviceUrl The remote URL to query
     * @param layerName The coverage layername to request
     * @param downloadFormat Either [GeoTIFF, NetCDF]
     * @param outputWidth [Optional] Width of output dataset (Not compatible with outputResX/outputResY)
     * @param outputHeight [Optional] Height of output dataset (Not compatible with outputResX/outputResY)
     * @param outputResX [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputResY [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputCrs [Optional] The Coordinate reference system of the output data
     * @param inputCrs [Optional] [BBoxConstraint] the coordinate reference system of the bounding box points
     * @param northBoundLatitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param southBoundLatitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param eastBoundLongitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param westBoundLongitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param date [Optional] [TimeConstraint] The time at which to select the dataset
     * @param time [Optional] [TimeConstraint] The time at which to select the dataset
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
                                 @RequestParam(required=false, value="date") final String date,
                                 @RequestParam(required=false, value="time") final String time,
                                HttpServletResponse response) throws Exception {
        WCSDownloadFormat format = parseDownloadFormat(downloadFormat);
        
        //Unfortunately Spring can't handle "ambigious" mappings so we need a top level handler
        //that delegates to our private handlers
        if (date == null || date.isEmpty() ||
            time == null || time.isEmpty()) {
            
            logger.trace("Constraint: Bounding box");
            
            //Assume its a bounding box query
            downloadWCSAsZip(serviceUrl, layerName, format, inputCrs, northBoundLatitude, 
                    southBoundLatitude, eastBoundLongitude, westBoundLongitude, outputWidth, 
                    outputHeight, outputResX, outputResY, outputCrs, response);
        } else {
            
            logger.trace("Constraint: Temporal");
            
            //Assume its a time constrained query
            downloadWCSAsZip(serviceUrl, layerName, format, inputCrs, date, time, outputWidth, 
                    outputHeight, outputResX, outputResY, outputCrs, response);
        }
        
    }
    
    /**
     * A function that given the parameters for a WCS GetCovereage request will make the request
     * on behalf of the user and return the results in a zipped file.
     * 
     * One set of outputWidth/outputHeight or outputResX/outputResy must be specified
     * 
     * The resulting dataset will be constrained to the specified bounding box
     * 
     * @param serviceUrl The remote URL to query
     * @param layerName The coverage layername to request
     * @param downloadFormat Either [GeoTIFF, NetCDF]
     * @param outputWidth [Optional] Width of output dataset (Not compatible with outputResX/outputResY)
     * @param outputHeight [Optional] Height of output dataset (Not compatible with outputResX/outputResY)
     * @param outputResX [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputResY [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputCrs [Optional] The Coordinate reference system of the output data 
     * @param inputCrs [Optional] the coordinate reference system of the bounding box points
     * @param northBoundLatitude A point on the bounding box
     * @param southBoundLatitude A point on the bounding box
     * @param eastBoundLongitude A point on the bounding box
     * @param westBoundLongitude A point on the bounding box
     * @param response
     * @throws Exception
     */
    private void downloadWCSAsZip( final String serviceUrl, final String layerName, final WCSDownloadFormat format,
                                  final String inputCrs, final double northBoundLatitude, final double southBoundLatitude,
                                  final double eastBoundLongitude, final double westBoundLongitude, final int outputWidth,
                                  final int outputHeight, final double outputResX, final double outputResY, final String outputCrs,
                                  HttpServletResponse response) throws Exception {
        
        String outFileName = generateOutputFilename(layerName, format);
        
        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();
        bbox.setEastBoundLongitude(eastBoundLongitude);
        bbox.setSouthBoundLatitude(southBoundLatitude);
        bbox.setNorthBoundLatitude(northBoundLatitude);
        bbox.setWestBoundLongitude(westBoundLongitude);
        
        logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' layerName='%3$s'", serviceUrl, bbox, layerName));
        
        downloadWCSAsZip(outFileName, 
                methodMaker.makeMethod(serviceUrl, layerName, format, outputCrs, outputWidth, outputHeight, outputResX, outputResY, inputCrs, bbox),
                response);
    }
    
    /**
     * A function that given the parameters for a WCS GetCovereage request will make the request
     * on behalf of the user and return the results in a zipped file.
     * 
     * One set of outputWidth/outputHeight or outputResX/outputResy must be specified
     * 
     * The resulting dataset will be constrained to the specified date/time
     * 
     * @param serviceUrl The remote URL to query
     * @param layerName The coverage layername to request
     * @param downloadFormat Either [GeoTIFF, NetCDF]
     * @param outputWidth [Optional] Width of output dataset (Not compatible with outputResX/outputResY)
     * @param outputHeight [Optional] Height of output dataset (Not compatible with outputResX/outputResY)
     * @param outputResX [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputResY [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputCrs [Optional] The Coordinate reference system of the output data
     * @param inputCrs [Optional] the coordinate reference system of the bounding box points
     * @param date [Optional] The date 'YYYY-MM-DD' at which to select the dataset
     * @param time [Optional] The time 'HH:MM:SS' at which to select the dataset
     * @param response
     * @throws Exception
     */
    private void downloadWCSAsZip( final String serviceUrl, final String layerName, final WCSDownloadFormat format,
                                  final String inputCrs, final String date, final String time, final int outputWidth,
                                  final int outputHeight, final double outputResX, final double outputResY, final String outputCrs,
                                  HttpServletResponse response) throws Exception {
        String outFileName = generateOutputFilename(layerName, format);
        
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = inputFormat.parse(date + ' ' + time);
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        logger.debug(String.format("serviceUrl='%1$s' timeConstraint='%2$s' layerName='%3$s'", serviceUrl, outputFormat.format(d), layerName));
        
        downloadWCSAsZip(outFileName, 
                methodMaker.makeMethod(serviceUrl, layerName, format, outputCrs, outputWidth, outputHeight, outputResX, outputResY, inputCrs, outputFormat.format(d)),
                response);
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
        } finally {
            method.releaseConnection(); //Ensure this gets called otherwise we leak connections
            zout.close();
        }
    }
}
