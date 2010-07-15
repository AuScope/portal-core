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
     * @param usingBboxConstraint [Optional] Set to "on" if using bbox constraints 
     * @param inputCrs [Optional] [BBoxConstraint] the coordinate reference system of the bounding box points
     * @param northBoundLatitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param southBoundLatitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param eastBoundLongitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param westBoundLongitude [Optional] [BBoxConstraint] A point on the bounding box
     * @param usingTimeConstraint [Optional] Set to "on" if using time constraints
     * @param date [Optional] [TimeConstraint] The time at which to select the dataset
     * @param time [Optional] [TimeConstraint] The time at which to select the dataset
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadWCSAsZip.do")
    public void downloadWCSAsZip(@RequestParam("serviceUrl") final String serviceUrl,
                                 @RequestParam("layerName") final String layerName,
                                 @RequestParam("downloadFormat") final String downloadFormat,
                                 @RequestParam(required=false, defaultValue="0", value="outputWidth") final int outputWidth,
                                 @RequestParam(required=false, defaultValue="0", value="outputHeight") final int outputHeight,
                                 @RequestParam(required=false, defaultValue="0", value="outputResX") final int outputResX,
                                 @RequestParam(required=false, defaultValue="0", value="outputResY") final int outputResY,
                                 @RequestParam(required=false, value="outputCrs") final String outputCrs,
                                 @RequestParam(required=false, value="usingBboxConstraint") final String usingBboxConstraint,
                                 @RequestParam(required=false, value="inputCrs") final String inputCrs,
                                 @RequestParam(required=false, defaultValue="0",  value="northBoundLatitude") final double northBoundLatitude,
                                 @RequestParam(required=false, defaultValue="0", value="southBoundLatitude") final double southBoundLatitude,
                                 @RequestParam(required=false, defaultValue="0", value="eastBoundLongitude") final double eastBoundLongitude,
                                 @RequestParam(required=false, defaultValue="0", value="westBoundLongitude") final double westBoundLongitude,
                                 @RequestParam(required=false, value="usingTimeConstraint") final String usingTimeConstraint,
                                 @RequestParam(required=false, value="date") final String date,
                                 @RequestParam(required=false, value="time") final String time,
                                HttpServletResponse response) throws Exception {
        
        
        
        //Parse our method
        HttpMethodBase method = null;
        WCSDownloadFormat format = null;
        String outFileName = null;
        
        if (downloadFormat.toLowerCase().equals("geotiff")) {
            format = WCSDownloadFormat.GeoTIFF;
            outFileName = String.format("%1$s.tiff", layerName);
        } else if (downloadFormat.toLowerCase().equals("netcdf")) {
            format = WCSDownloadFormat.NetCDF;
            outFileName = String.format("%1$s.nc", layerName);
        } else {
            throw new IllegalArgumentException("Unrecognized format: " + downloadFormat);
        }
        
        if (usingBboxConstraint != null && usingBboxConstraint.equals("on")) {
            CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();
            bbox.setEastBoundLongitude(eastBoundLongitude);
            bbox.setSouthBoundLatitude(southBoundLatitude);
            bbox.setNorthBoundLatitude(northBoundLatitude);
            bbox.setWestBoundLongitude(westBoundLongitude);
            
            method = methodMaker.makeMethod(serviceUrl, layerName, format, outputCrs, outputWidth, outputHeight, outputResX, outputResY, inputCrs, bbox);
        } else if (usingTimeConstraint != null && usingTimeConstraint.equals("on")) {
            //time=2005-05-10T00:00:00Z
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = inputFormat.parse(date + ' ' + time);
            DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            method = methodMaker.makeMethod(serviceUrl, layerName, format, outputCrs, outputWidth, outputHeight, outputResX, outputResY, outputFormat.format(d));
        } else {
            throw new IllegalArgumentException("At least one of 'usingBboxConstraint' or 'usingTimeConstraint' must be set to 'on'");
        }
        
        //Lets make the request and zip up the response before passing it back to the user
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=WCSDownload.zip;");
        
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
        try{
            InputStream inData = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());
            
            zout.putNextEntry(new ZipEntry(outFileName));
            
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
