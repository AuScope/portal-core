package org.auscope.portal.server.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.springframework.stereotype.Repository;

@Repository
public class WCSGetCoverageMethodMakerGET implements
        IWCSGetCoverageMethodMaker {

    private final Log logger = LogFactory.getLog(getClass());
    
    private HttpMethodBase makeMethod(String serviceURL, String layerName,
            WCSDownloadFormat format, String outputCrs, int outputWidth, int outputHeight, int outputResX, int outputResY, String inputCrs,
            CSWGeographicBoundingBox bbox, String timeConstraint) throws Exception {
        GetMethod httpMethod = new GetMethod(serviceURL);
        
        //Do some simple error checking to align with WCS standard
        if ((outputWidth > 0 && outputHeight == 0) ||
                (outputHeight > 0 && outputWidth == 0)) 
            throw new IllegalArgumentException("Height/Width cannot be used without Width/Height");
        if ((outputResX > 0 && outputResY == 0 ||
                outputResY > 0 && outputResX == 0)) 
            throw new IllegalArgumentException("outputResX/outputResY cannot be used without outputResY/outputResX");
        if ((outputResX > 0 || outputResY > 0) &&
                (outputWidth > 0 || outputHeight > 0)) 
            throw new IllegalArgumentException("outputResX/outputResY cannot be used with outputWidth/outputHeight");
        if (outputResX == 0 && outputResY == 0 && 
                outputWidth == 0 && outputHeight == 0) 
            throw new IllegalArgumentException("One of outputResX/outputResY or outputWidth/outputHeight");
        if (bbox==null && timeConstraint ==null)
            throw new IllegalArgumentException("You must specify at least one bbox or time constraint");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        
        params.add(new NameValuePair("service", "WCS"));
        params.add(new NameValuePair("service", "WCS"));
        params.add(new NameValuePair("version", "1.0.0"));
        params.add(new NameValuePair("request", "GetCoverage"));
        params.add(new NameValuePair("coverage", layerName));
        
        switch (format) {
        case GeoTIFF:
            params.add(new NameValuePair("format", "GeoTIFF"));
            break;
        case NetCDF:
            params.add(new NameValuePair("format", "NetCDF"));
            break;
        default:
            throw new IllegalArgumentException(String.format("format '%1$s' not supported", format.toString()));
        }
        
        if (inputCrs != null) {
            params.add(new NameValuePair("crs", inputCrs));
        }
        
        if (bbox != null) {
            params.add(new NameValuePair("bbox", 
                    String.format("%1$f,%2$f,%3$f,%4$f", 
                            bbox.getEastBoundLongitude(), 
                            bbox.getSouthBoundLatitude(), 
                            bbox.getWestBoundLongitude(), 
                            bbox.getNorthBoundLatitude())));
        }
        
        if (timeConstraint != null) {
            params.add(new NameValuePair("time", timeConstraint));
        }
        
        if (outputWidth > 0) {
            params.add(new NameValuePair("width", Integer.toString(outputWidth)));
        }
        
        if (outputHeight > 0) {
            params.add(new NameValuePair("height", Integer.toString(outputHeight)));
        }
        
        if (outputResX > 0) {
            params.add(new NameValuePair("resx", Integer.toString(outputResX)));
        }
        
        if (outputResY > 0) {
            params.add(new NameValuePair("resy", Integer.toString(outputResY)));
        }
        
        httpMethod.setQueryString(params.toArray(new NameValuePair[params.size()]));
        
        logger.debug(String.format("url='%1$s' query='%2$s'", serviceURL, httpMethod.getQueryString()));
        
        return httpMethod;
    }

    public HttpMethodBase makeMethod(String serviceURL, String layerName,
            WCSDownloadFormat format, String outputCrs, int outputWidth, int outputHeight,
            int outputResX, int outputResY, String inputCrs, CSWGeographicBoundingBox bbox) throws Exception {
        return makeMethod(serviceURL, layerName, format, outputCrs, outputWidth, outputHeight, outputResX, outputResY,inputCrs,bbox,null);
    }

    @Override
    public HttpMethodBase makeMethod(String serviceURL, String layerName,
            WCSDownloadFormat format, String outputCrs, int outputWidth, int outputHeight,
            int outputResX, int outputResY, String timeConstraint) throws Exception {
        return makeMethod(serviceURL, layerName, format, outputCrs, outputWidth, outputHeight, outputResX, outputResY,null,null,timeConstraint);
    }

}
