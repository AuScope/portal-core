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
     * @param forMosaicService [Optional] indicates if the getLogCollection service should generate a result specifically for the use of a Mosaic Service
     * @return
     */
    public HttpMethodBase getLogCollectionMethod(String serviceUrl, String datasetId, Boolean forMosaicService) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "getLogCollection.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("datasetid", datasetId));
        if (forMosaicService != null) {
            valuePairs.add(new NameValuePair("mosaicsvc", forMosaicService.booleanValue() ? "yes" : "no"));
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

    /**
     * TSG Download Service is part of the DownloadServices.
     * When triggered, the tsg download service will download entire Hylogging dataset from Hylogging
     * database using TSG Adapter and deliver the full dataset in the form of TSG format. The user
     * will have to first make a download request and come back to check the download status.
     *
     * When the download is completed, a link will be provided to download the requested TSG Dataset in zip format.
     *
     * Note : Either one of the dataset id or match string must be provided and not both
     *
     * This method will return a HTML stream
     * @param serviceUrl The URL of the NVCLDataService
     * @param email The user's email address
     * @param datasetId [Optional] a dataset id chosen by user (list of dataset id can be obtained thru calling the get log collection service)
     * @param matchString [Optional] Its value is part or all of a proper drillhole name. The first dataset found to match in the database is downloaded
     * @param lineScan [Optional] yes or no. If no then the main image component is not downloaded. The default is yes.
     * @param spectra [Optional] yes or no. If no then the spectral component is not downloaded. The default is yes.
     * @param profilometer [Optional] yes or no. If no then the profilometer component is not downloaded. The default is yes.
     * @param trayPics [Optional] yes or no. If no then the individual tray pictures are not downloaded. The default is yes.
     * @param mosaicPics [Optional] yes or no. If no then the hole mosaic picture is not downloaded. The default is yes.
     * @param mapPics [Optional] yes or no. If no then the map pictures are not downloaded. The default is yes.
     * @return
     */
    public HttpMethodBase getDownloadTSGMethod(String serviceUrl, String email, String datasetId, String matchString, Boolean lineScan, Boolean spectra, Boolean profilometer, Boolean trayPics, Boolean mosaicPics, Boolean mapPics) {

        if ((datasetId == null && matchString == null) ||
            (datasetId != null && matchString != null)) {
            throw new IllegalArgumentException("must specify ONLY one of datasetId and matchString");
        }

        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "downloadtsg.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("email", email));
        if (datasetId != null) {
            valuePairs.add(new NameValuePair("datasetid", datasetId));
        }
        if (matchString != null) {
            valuePairs.add(new NameValuePair("match_string", matchString));
        }
        if (lineScan != null) {
            valuePairs.add(new NameValuePair("linescan", lineScan.booleanValue() ? "yes" : "no"));
        }
        if (spectra != null) {
            valuePairs.add(new NameValuePair("spectra", spectra.booleanValue() ? "yes" : "no"));
        }
        if (profilometer != null) {
            valuePairs.add(new NameValuePair("profilometer", profilometer.booleanValue() ? "yes" : "no"));
        }
        if (trayPics != null) {
            valuePairs.add(new NameValuePair("traypics", trayPics.booleanValue() ? "yes" : "no"));
        }
        if (mosaicPics != null) {
            valuePairs.add(new NameValuePair("mospic", mosaicPics.booleanValue() ? "yes" : "no"));
        }
        if (mapPics != null) {
            valuePairs.add(new NameValuePair("mappics", mapPics.booleanValue() ? "yes" : "no"));
        }

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }

    /**
     * Checks a user's TSG download status
     *
     * This method will return a HTML stream
     *
     * @param serviceUrl The URL of the NVCLDataService
     * @param email The user's email address
     * @return
     */
    public HttpMethodBase getCheckTSGStatusMethod(String serviceUrl, String email) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "checktsgstatus.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("email", email));

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }

    /**
     * When triggered, the wfs download service will call the Observations and Measurements WFS request,
     * get the GeoSciML? output and compress it into a zip file for download. The user will have to
     * first make a download request and come back to check the download status. When the download is
     * completed, a link will be provided to download the requested Observations and Measurements output
     * in zip format.
     *
     * This method will return a HTML stream
     *
     * @param serviceUrl The URL of the NVCLDataService
     * @param email The user's email address
     * @param boreholeId selected borehole id (use as feature id for filtering purpose)
     * @param omUrl The valid url for the Observations and Measurements WFS
     * @param typeName The url parameter for the wfs request
     * @return
     */
    public HttpMethodBase getDownloadWFSMethod(String serviceUrl, String email, String boreholeId, String omUrl, String typeName) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "downloadwfs.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("email", email));
        valuePairs.add(new NameValuePair("boreholeid", boreholeId));
        valuePairs.add(new NameValuePair("serviceurl", omUrl));
        valuePairs.add(new NameValuePair("typename", typeName));

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }

    /**
     * Checks a user's WFS download status
     *
     * This method will return a HTML stream
     *
     * @param serviceUrl The URL of the NVCLDataService
     * @param email The user's email address
     * @return
     */
    public HttpMethodBase getCheckWFSStatusMethod(String serviceUrl, String email) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "checkwfsstatus.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("email", email));

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }
}
