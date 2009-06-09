package org.auscope.portal.server.web;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.*;

/**
 * Utility class used to call web service endpoints
 * 
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 11:37:40 AM
 */

public class HttpServiceCaller {

    private HttpClient httpClient;

    public HttpServiceCaller(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Creates a http GetMethod given the following parameters
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @return
     * @throws Exception
     */
    public GetMethod constructWFSGetFeatureMethod(String serviceURL, String featureType, String filterString) throws Exception {

        //pretty hard to do a GetFeature query with a featureType, so we had better check that we have one
        if(featureType == null || featureType.equals(""))
            throw new Exception("featureType parameter can not be null or empty.");

        //pretty hard to do a GetFeature query with a serviceURL, so we had better check that we have one
        if(serviceURL == null || serviceURL.equals(""))
            throw new Exception("serviceURL parameter can not be null or empty.");

        // Create a method instance.
        GetMethod method = new GetMethod(serviceURL);

        //set all of the parameters
        NameValuePair service = new NameValuePair("service", "WFS");
        NameValuePair version = new NameValuePair("version", "1.1.0");
        NameValuePair request = new NameValuePair("request", "GetFeature");
        NameValuePair typeName = new NameValuePair("typeName", featureType);
        NameValuePair filter = new NameValuePair("filter", filterString);
        NameValuePair maxFeatures = new NameValuePair("maxFeatures", "10");

        //attach them to the method
        method.setQueryString(new NameValuePair[]{service, version, request, typeName, filter, maxFeatures});
        
        //return the GetMethod
        return method;
    }

    /**
     * Makes a call to a http GetMethod
     * @param method
     * @return
     * @throws Exception
     */
    public String callMethod(HttpMethodBase method) throws ConnectException, UnknownHostException, ConnectTimeoutException, Exception{
        //make the call
        int statusCode = httpClient.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            //if the response is not OK then throw an error
            throw new Exception("Returned status line: " + method.getStatusLine());
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();

        //return it
        return new String(responseBody);
    }

    /**
     * Given a URL, call it, convert the response into a String and return
     * @param serviceUrl
     * @return
     * @throws IOException
     */
    public String callHttpUrlGET(URL serviceUrl) throws IOException {
        return responseToString(new BufferedInputStream(serviceUrl.openStream()));
    }

    /**
     * Convert a Buffered stream into a String
     * @param stream
     * @return
     * @throws IOException
     */
    public String responseToString(BufferedInputStream stream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }
        return stringBuffer.toString();
    }

}