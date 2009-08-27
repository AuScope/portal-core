package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.net.*;

/**
 * Utility class used to call web service endpoints
 * 
 * User: Mathew Wyatt
 * Date: 25/03/2009
 * Time: 11:37:40 AM
 */

@Repository
public class HttpServiceCaller {
    private Logger logger = Logger.getLogger(getClass());

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

            //if its unavailable then throw updateCSWRecords connection exception
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

    /**
     * Generate a new httpClient
     * @return
     */
    public HttpClient getHttpClient() {
        return new HttpClient();        
    }
}