package org.auscope.portal.server.web;

import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.csw.CSWGeographicBoundingBox;

public interface IWCSGetCoverageMethodMaker {
    
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
     * @param timeConstraint [Optional] Set to time1,time2,time3 OR timeStart/timeEnd/[optional]resolution  
     * @param inputCrs The coordinate reference system to use with the bounding box
     * @param outputCrs [Optional] The coordinate reference system the output will be defined with
     * @param customParams [Optional] a map of KVP parameter constraints that will be applied to the get coverage request. (format is either a CSV list of values OR 'MIN/MAX/RESOLUTION' eg 'temperature=1,10/16/2,20')
     * @return
     * @throws Exception
     */
    public HttpMethodBase makeMethod(String serviceURL, String layerName, String format, 
            String outputCrs, int outputWidth, int outputHeight, double outputResX, double outputResY, 
            String inputCrs, CSWGeographicBoundingBox bbox, String timeConstraint, Map<String, String> customParams) throws Exception;
}


