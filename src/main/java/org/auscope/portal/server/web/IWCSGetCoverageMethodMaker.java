package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWGeographicBoundingBox;

public interface IWCSGetCoverageMethodMaker {
    
    public enum WCSDownloadFormat {
        GeoTIFF,
        NetCDF
    }
    
    /**
     * Returns a method that makes a GetCoverage request for a given time constraint
     * (Requires at least one of outputWidth/outputHeight or outputResX/outputResY to be set)
     * 
     * @param serviceURL The remote URL to query
     * @param layerName The name of the coverage layer to fetch
     * @param format What the downloaded format will be
     * @param outputWidth Set to the scaled width of the output grid (or set to 0 if N/A) 
     * @param outputHeight Set to the scaled height of the output grid (or set to 0 if N/A)
     * @param outputResX When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (or set to 0 if N/A)
     * @param outputResY When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (or set to 0 if N/A)
     * @param bbox The BBOX that will constrain the WCS output
     * @param inputCrs [Optional] The coordinate reference system to use with the bounding box
     * @param outputCrs [Optional] The coordinate reference system the output will be defined with
     * @return
     * @throws Exception
     */
    public HttpMethodBase makeMethod(String serviceURL, String layerName, WCSDownloadFormat format, String outputCrs, int outputWidth, int outputHeight, int outputResX, int outputResY, String inputCrs, CSWGeographicBoundingBox bbox) throws Exception;
    
    /**
     * Returns a method that makes a GetCoverage request for a given time constraint
     * (Requires at least one of outputWidth/outputHeight or outputResX/outputResY to be set)
     * 
     * @param serviceURL The remote URL to query
     * @param layerName The name of the coverage layer to fetch
     * @param format What the downloaded format will be
     * @param outputWidth Set to the scaled width of the output grid (or set to 0 if N/A) 
     * @param outputHeight Set to the scaled height of the output grid (or set to 0 if N/A)
     * @param outputResX When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (or set to 0 if N/A)
     * @param outputResY When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (or set to 0 if N/A)
     * @param timeConstraint The time constraint used to fetch the output data
     * @param outputCrs [Optional] The coordinate reference system the output will be defined with
     * @return
     * @throws Exception
     */
    public HttpMethodBase makeMethod(String serviceURL, String layerName, WCSDownloadFormat format, String outputCrs, int outputWidth, int outputHeight, int outputResX, int outputResY, String timeConstraint) throws Exception;
}


