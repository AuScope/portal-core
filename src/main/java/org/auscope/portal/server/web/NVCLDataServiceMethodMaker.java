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
     * Generates a method for making request for all NVCL logged elements that belong to a particular dataset
     * @param serviceUrl The URL of the NVCLDataService
     * @param datasetId The dataset ID to query
     * @return
     */
    public HttpMethodBase getLogCollectionMethod(String serviceUrl, String datasetId) {
        GetMethod method = new GetMethod(urlPathConcat(serviceUrl, "getLogCollection.html"));

        ArrayList<NameValuePair> valuePairs = new ArrayList<NameValuePair>();

        //set all of the parameters
        valuePairs.add(new NameValuePair("datasetid", datasetId));

        //attach them to the method
        method.setQueryString((NameValuePair[]) valuePairs.toArray(new NameValuePair[valuePairs.size()]));

        return method;
    }
}
