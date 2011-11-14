package org.auscope.portal.server.web;

import java.util.ArrayList;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Repository;

/**
 * Class for generating methods for communicating with an instance of the
 * AuScope NVCL Data service
 *
 * Data Service API - https://twiki.auscope.org/wiki/CoreLibrary/WebServicesDevelopment
 * @author Josh Vote
 *
 */
@Repository
public class NVCLDataServiceMethodMaker extends AbstractMethodMaker {

    /**
     * The types of graphs that can be specified to the plot scalar service
     */
    public enum PlotScalarGraphType {
        StackedBarChart,
        ScatteredChart,
        LineChart
    }

    /**
     * Generates a method for making request for all NVCL DataSets that belong to a particular borehole
     * @param serviceUrl The URL of the NVCLDataService
     * @param holeIdentifier The unique ID of the borehole to query
     */
    public HttpMethodBase getDatasetCollectionMethod(String serviceUrl, String holeIdentifier) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "getDatasetCollection.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("holeidentifier", holeIdentifier));

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }

    /**
     * Generates a method for making a request for all NVCL logged elements that belong to a particular dataset
     * @param serviceUrl The URL of the NVCLDataService
     * @param datasetId The dataset ID to query
     * @param forMosaicService indicates if the getLogCollection service should generate a result specifically for the use of a Mosaic Service
     * @return
     */
    public HttpMethodBase getLogCollectionMethod(String serviceUrl, String datasetId, boolean forMosaicService) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "getLogCollection.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("datasetid", datasetId));
        valuePairs.add(new NameValuePair("mosaicsvc", forMosaicService ? "yes" : "no"));

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }

    /**
     * Generates a method for making a request for the Mosaic imagery for a particular logId
     *
     * The response will be either HTML or a binary stream representing an image
     * @param serviceUrl The URL of the NVCLDataService
     * @param logId The logID (from a getLogCollection request) to query
     * @param width [Optional] specify the number of column the images are to be displayed
     * @param startSampleNo [Optional] the first sample image to be displayed
     * @param endSampleNo [Optional] the last sample image to be displayed
     * @return
     */
    public HttpMethodBase getMosaicMethod(String serviceUrl, String logId, Integer width, Integer startSampleNo, Integer endSampleNo) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "mosaic.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("logid", logId));
        if (width != null) {
            valuePairs.add(new NameValuePair("width", width.toString()));
        }
        if (startSampleNo != null) {
            valuePairs.add(new NameValuePair("startsampleno", startSampleNo.toString()));
        }
        if (endSampleNo != null) {
            valuePairs.add(new NameValuePair("endsampleno", endSampleNo.toString()));
        }

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }

    /**
     * Generates a method for making a request for the Mosaic imagery for a particular logId
     *
     * The response will be either HTML or a binary stream representing an image
     * @param serviceUrl The URL of the NVCLDataService
     * @param logId The logID (from a getLogCollection request) to query
     * @param width [Optional] the width of the image in pixel
     * @param height [Optional] the height of the image in pixel
     * @param startDepth [Optional] the start depth of a borehole collar
     * @param endDepth [Optional] the end depth of a borehole collar
     * @param samplingInterval [Optional] the interval of the sampling
     * @param graphType [Optional] The type of graph to plot
     * @return
     */
    public HttpMethodBase getPlotScalarMethod(String serviceUrl, String logId, Integer startDepth, Integer endDepth, Integer width, Integer height, Double samplingInterval, PlotScalarGraphType graphType) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "plotscalar.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("logid", logId));
        if (width != null) {
            valuePairs.add(new NameValuePair("width", width.toString()));
        }
        if (height != null) {
            valuePairs.add(new NameValuePair("height", height.toString()));
        }
        if (startDepth != null) {
            valuePairs.add(new NameValuePair("startdepth", startDepth.toString()));
        }
        if (endDepth != null) {
            valuePairs.add(new NameValuePair("enddepth", endDepth.toString()));
        }
        if (samplingInterval != null) {
            valuePairs.add(new NameValuePair("samplinginterval", samplingInterval.toString()));
        }
        if (graphType != null) {
            switch (graphType) {
            case LineChart:
                valuePairs.add(new NameValuePair("graphtype", "3"));
                break;
            case ScatteredChart:
                valuePairs.add(new NameValuePair("graphtype", "2"));
                break;
            case StackedBarChart:
                valuePairs.add(new NameValuePair("graphtype", "1"));
                break;
            }
        }

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }
}
