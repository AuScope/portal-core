package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Repository;

import java.net.*;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.IOException;


/**
 * Utility class used to call web service end points
 * 
 * @version $Id$
 */

@Repository
public class HttpServiceCaller {
    protected final Log log = LogFactory.getLog(getClass());
    /**
     * Makes a call to a http GetMethod and returns the response as a string
     *
     * @param method
     * @param httpClient
     * @return
     * @throws Exception
     */
    public String getMethodResponseAsString(HttpMethodBase method, HttpClient httpClient) throws ConnectException, UnknownHostException, ConnectTimeoutException, Exception{
        //invoke the method
        this.invokeTheMethod(method, httpClient);

        //get the reponse before we close the connection
        String response = method.getResponseBodyAsString();

        //release the connection
        method.releaseConnection();

        //return it
        return response;
    }

    /**
     * Invokes a method and returns the binary response
     *
     * @return
     */
    public byte[] getMethodResponseInBytes(HttpMethodBase method, HttpClient httpClient) throws Exception {
        //invoke the method
        this.invokeTheMethod(method, httpClient);

        //get the reponse before we close the connection
        byte[] response = method.getResponseBody();

        //release the connection
        method.releaseConnection();

        //return it
        return response;
    }

    /**
     * Invokes a httpmethod and takes care of some error handling
     * @param method
     * @param httpClient
     */
    private void invokeTheMethod(HttpMethodBase method, HttpClient httpClient) throws Exception {
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
            log.error(method.getStatusLine());

            //if its unavailable then throw updateCSWRecords connection exception
            if(statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE)
                throw new ConnectException();

            //if the response is not OK then throw an error
            throw new Exception("Returned status line: " + method.getStatusLine());
        }
    }

    /**
     * Returns a header value for a given method and key
     *
     * @param method
     * @param header
     * @return
     */
    public Header getResponseHeader(HttpMethodBase method, String header) {
        return method.getResponseHeader(header);   
    }

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