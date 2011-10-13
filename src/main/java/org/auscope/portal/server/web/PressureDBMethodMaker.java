package org.auscope.portal.server.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Repository;

/**
 * Class for making HTTP methods tailored to pressure DB webservice requests
 * @author Josh Vote
 *
 */
@Repository
public class PressureDBMethodMaker {

    private String makeMethodUrl(String serviceUrl, String pathSuffix) {
        String methodUrl = serviceUrl;
        if (methodUrl.charAt(methodUrl.length() - 1) != '/') {
            methodUrl += '/';
        }

        return methodUrl + pathSuffix;
    }

    /**
     * Makes a HTTP method for a pressure db getAvailableOM request.
     * @param serviceUrl
     * @param wellID
     * @return
     */
    public HttpMethodBase makeGetAvailableOMMethod(String serviceUrl, String wellID) {
        GetMethod method = new GetMethod(makeMethodUrl(serviceUrl, "getAvailableOM.html"));
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new NameValuePair("wellid", wellID));

        method.setQueryString(params.toArray(new NameValuePair[params.size()]));

        return method;
    }

    /**
     * Makes a HTTP method for a pressure db download request.
     * @param serviceUrl
     * @param wellID
     * @return
     */
    public HttpMethodBase makeDownloadMethod(String serviceUrl, String wellID, String[] features) {
        GetMethod method = new GetMethod(makeMethodUrl(serviceUrl, "download.html"));
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new NameValuePair("wellid", wellID));
        for (String feature : features) {
            params.add(new NameValuePair("feature", feature));
        }

        method.setQueryString(params.toArray(new NameValuePair[params.size()]));

        return method;
    }
}
