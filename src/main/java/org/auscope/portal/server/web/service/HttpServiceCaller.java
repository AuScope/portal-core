package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.net.*;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStream;
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
    
    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    @Autowired
    @Qualifier(value = "propertyConfigurer")    
    public void setHostConfigurer(PortalPropertyPlaceholderConfigurer hostConfig) {
        this.hostConfigurer = hostConfig;
    }    
    
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

        log.trace("XML response from server:");
        log.trace("\n" + response);
        //return it
        return response;
    }
    
    /**
     * Invokes a method and returns the binary response as a stream
     * 
     * WARNING - ensure you call method.releaseConnection() AFTER you have finished reading the input stream
     *
     * @return
     */
    public InputStream getMethodResponseAsStream(HttpMethodBase method, HttpClient httpClient) throws Exception {
        //invoke the method
        this.invokeTheMethod(method, httpClient);

        return method.getResponseBodyAsStream();
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

        HttpConnectionManagerParams clientParams = new HttpConnectionManagerParams();

        int SECOND = 1000;      // 1000 millisecond
        
        int BODY_TIMEOUT;
        int SOCK_TIMEOUT;
        int CONN_TIMEOUT;
        
        try {
            BODY_TIMEOUT = SECOND * Integer.parseInt(hostConfigurer.resolvePlaceholder("wait-for-body-content.timeout"));        
            SOCK_TIMEOUT = SECOND * Integer.parseInt(hostConfigurer.resolvePlaceholder("socket.timeout"));
            CONN_TIMEOUT = SECOND * Integer.parseInt(hostConfigurer.resolvePlaceholder("connection-establish.timeout"));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw e;
        }

        log.trace("BODY_TIMEOUT : " + BODY_TIMEOUT);
        log.trace("SOCK_TIMEOUT : " + SOCK_TIMEOUT);
        log.trace("CONN_TIMEOUT : " + CONN_TIMEOUT);
        
        log.debug("method=" + method.getURI());
        log.info("method=" + method.getURI());
        
        
        // Period of time in milliseconds to wait for a content body 
        // sent in response to HEAD method from a non-compliant server.
        clientParams.setParameter( HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT
                                 , BODY_TIMEOUT);

        // Default socket timeout in milliseconds which is the timeout for waiting for data
        clientParams.setSoTimeout(SOCK_TIMEOUT);
        
        // Timeout until connection is etablished.
        clientParams.setConnectionTimeout(CONN_TIMEOUT);

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