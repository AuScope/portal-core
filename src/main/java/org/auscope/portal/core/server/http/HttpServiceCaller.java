package org.auscope.portal.core.server.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class used to call web service end points.
 */
public class HttpServiceCaller {
    private final Log log = LogFactory.getLog(getClass());

    private HttpConnectionManagerParams clientParams;

    public HttpServiceCaller(HttpConnectionManagerParams clientParams) {
        this.clientParams = clientParams;
    }

    /**
     * Makes a call to a http GetMethod and returns the response as a string.
     *
     * (Creates a new HttpClient for use with this request)
     *
     * @param method The method to be executed
     * @return
     * @throws Exception
     */
    public String getMethodResponseAsString(HttpMethodBase method) throws ConnectException, UnknownHostException, ConnectTimeoutException, Exception{
        return getMethodResponseAsString(method, new HttpClient());
    }

    /**
     * Makes a call to a http GetMethod and returns the response as a string.
     *
     * @param method The method to be executed
     * @param httpClient The client that will be used
     * @return
     * @throws Exception
     */
    public String getMethodResponseAsString(HttpMethodBase method, HttpClient httpClient) throws ConnectException, UnknownHostException, ConnectTimeoutException, Exception{
        //invoke the method
        this.invokeTheMethod(method, httpClient);

        //get the reponse before we close the connection
        //String response = method.getResponseBodyAsString();

        String response = responseToString(new BufferedInputStream(method.getResponseBodyAsStream()));

        //release the connection
        method.releaseConnection();

        log.trace("XML response from server:");
        log.trace("\n" + response);
        //return it
        return response;
    }

    /**
     * Invokes a method and returns the binary response as a stream.
     * (Creates a new HttpClient for use with this request)
     *
     * WARNING - ensure you call method.releaseConnection() AFTER you have finished reading the input stream.
     *
     * @param method The method to be executed
     * @return
     */
    public InputStream getMethodResponseAsStream(HttpMethodBase method) throws Exception {
        return this.getMethodResponseAsStream(method, new HttpClient());
    }

    /**
     * Invokes a method and returns the binary response as a stream.
     *
     * WARNING - ensure you call method.releaseConnection() AFTER you have finished reading the input stream.
     *
     * @param method The method to be executed
     * @param httpClient The client that will be used
     * @return
     */
    public InputStream getMethodResponseAsStream(HttpMethodBase method, HttpClient httpClient) throws Exception {
        //invoke the method
        this.invokeTheMethod(method, httpClient);

        return method.getResponseBodyAsStream();
    }

    /**
     * Invokes a method and returns the binary response.
     * (Creates a new HttpClient for use with this request)
     * @param method The method to be executed
     * @return
     */
    public byte[] getMethodResponseAsBytes(HttpMethodBase method) throws Exception {
        return getMethodResponseAsBytes(method, new HttpClient());
    }

    /**
     * Invokes a method and returns the binary response.
     *
     * @param method The method to be executed
     * @param httpClient The client that will be used
     * @return
     */
    public byte[] getMethodResponseAsBytes(HttpMethodBase method, HttpClient httpClient) throws Exception {
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
     * Invokes a httpmethod and takes care of some error handling.
     * @param method
     * @param httpClient
     */
    private void invokeTheMethod(HttpMethodBase method, HttpClient httpClient) throws Exception {
        log.debug("method=" + method.getURI());

        //create the connection manager and add it to the client
        // VT: Change from SimpleHttpConnectionManager (not thread safe) to
        // MultiThreadedHttpConnectionManager (thread safe)
        HttpConnectionManager man = new MultiThreadedHttpConnectionManager();
        man.setParams(clientParams);
        httpClient.setHttpConnectionManager(man);

        log.trace("Outgoing request headers: "
                + Arrays.toString(method.getRequestHeaders()));

        // make the call
        int statusCode = httpClient.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK &&
            statusCode != HttpStatus.SC_CREATED &&
            statusCode != HttpStatus.SC_ACCEPTED) {
            log.error(method.getStatusLine());

            // if it's unavailable then throw connection exception
            if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                throw new ConnectException();
            }
            
            String responseBody = method.getResponseBodyAsString();

            // if the response is not OK then throw an error
            throw new Exception("Returned status line: " + method.getStatusLine() + 
                    System.getProperty("line.separator") + "Returned response body: " + responseBody);
        }
    }

    /**
     * Convert a Buffered stream into a String.
     * @param stream
     * @return
     * @throws IOException
     */
    public String responseToString(BufferedInputStream stream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        int bufferCount = -1;
        char[] bufferArray=new char[1024];
        //VT:Very bad idea to use readline when dealing with xml response as there may not be a new line
        //which will lead to oom on the heap.
        while((bufferCount = reader.read(bufferArray)) != -1){
            stringBuffer.append(new String(bufferArray,0,bufferCount));
        }

        return stringBuffer.toString();
    }
}
