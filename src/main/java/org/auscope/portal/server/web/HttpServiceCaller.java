package org.auscope.portal.server.web;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
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
    private Logger logger = Logger.getLogger(getClass());

    public HttpServiceCaller() {}

    /**
     * Creates a HttpMethodBase given the following parameters
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @return
     * @throws Exception
     */
    public HttpMethodBase constructWFSGetFeatureMethod(String serviceURL, String featureType, String filterString) throws Exception {

        //pretty hard to do a GetFeature query with a featureType, so we had better check that we have one
        if(featureType == null || featureType.equals(""))
            throw new Exception("featureType parameter can not be null or empty.");

        //pretty hard to do a GetFeature query with a serviceURL, so we had better check that we have one
        if(serviceURL == null || serviceURL.equals(""))
            throw new Exception("serviceURL parameter can not be null or empty.");
/*
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
        */

        //create a method
        PostMethod method = new PostMethod(serviceURL);

        //TODO: remove the mo namespace and have it passed in as a parameter
        String postString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wfs:GetFeature version=\"1.1.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" maxFeatures=\"200\">\n" +
                "    <wfs:Query typeName=\""+featureType+"\">" +
                        filterString +
                "    </wfs:Query>" +
                "</wfs:GetFeature>";

        method.setRequestEntity(new StringRequestEntity(postString));

        //return the GetMethod
        return method;
    }

    /**
     * Makes a call to a http GetMethod
     * @param method
     * @param httpClient
     * @return
     * @throws Exception
     */
    public String callMethod(HttpMethodBase method, HttpClient httpClient) throws ConnectException, UnknownHostException, ConnectTimeoutException, Exception{
        //set the timeout, to 1 minute
        HttpConnectionManagerParams clientParams = new HttpConnectionManagerParams();
        clientParams.setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, 60000);
        clientParams.setSoTimeout(60000);
        clientParams.setConnectionTimeout(60000);

        //create the connection manager and add it to the client
        HttpConnectionManager man = new SimpleHttpConnectionManager();
        man.setParams(clientParams);
        httpClient.setHttpConnectionManager(man);
        
        //make the call
        int statusCode = httpClient.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            logger.error(method.getStatusLine());

            //if its unavailable then throw a connection exception
            if(statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE)
                    throw new ConnectException();

            //if the response is not OK then throw an error
            throw new Exception("Returned status line: " + method.getStatusLine());
        }

        //get the reponse before we close the connection
        String response = method.getResponseBodyAsString();

        //release the connection
        method.releaseConnection();

        //return it
        return response;
    }
    
    /*
    public static void main(String[] args) throws Exception {
        HttpServiceCaller caller = new HttpServiceCaller();
        HttpMethodBase method = caller.constructWFSGetFeatureMethod("http://apacsrv1.arrc.csiro.au/deegree-wfs/services?", "mo:Mine", "");
        caller.callMethod(method, new HttpClient());
    }
   */
    
    /**
     * Generate a new httpClient
     * @return
     */
    public HttpClient getHttpClient() {
        return new HttpClient();        
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