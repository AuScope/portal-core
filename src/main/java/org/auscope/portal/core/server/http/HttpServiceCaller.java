package org.auscope.portal.core.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.commons.httpclient.HttpException;


/**
 * Utility class used to call web service end points.
 */
public class HttpServiceCaller {
    private final Log log = LogFactory.getLog(getClass());
    int connectionTimeOut;

    public HttpServiceCaller(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public String getMethodResponseAsString(HttpRequestBase method) throws ConnectException, UnknownHostException,
            ConnectTimeoutException, Exception {
        return this.getMethodResponseAsString(method, null);
    }

    /**
     * Makes a call to a http GetMethod and returns the response as a string.
     *
     * @param method
     *            The method to be executed
     * @param httpClient
     *            The client that will be used
     * @return
     * @throws Exception
     */
    public String getMethodResponseAsString(HttpRequestBase method, HttpClient client) throws ConnectException,
            UnknownHostException, ConnectTimeoutException, Exception {
        //invoke the method
        HttpResponse httpResponse = this.invokeTheMethod(method, client);

        //get the reponse before we close the connection
        //String response = method.getResponseBodyAsString();

        String response = responseToString(httpResponse.getEntity().getContent());

        //release the connection
        method.releaseConnection();

        log.trace("XML response from server:");
        log.trace("\n" + response);
        //return it
        return response;
    }

    /**
     * Invokes a method and returns the binary response as a stream. (Creates a new HttpClient for use with this request)
     *
     * WARNING - ensure you call method.releaseConnection() AFTER you have finished reading the input stream.
     *
     * @param method
     *            The method to be executed
     * @return
     */
    public InputStream getMethodResponseAsStream(HttpRequestBase method) throws Exception {
        return this.getMethodResponseAsStream(method, null);
    }

    /**
     * Invokes a method and returns the binary response as a stream.
     *
     * WARNING - ensure you call method.releaseConnection() AFTER you have finished reading the input stream.
     *
     * @param method
     *            The method to be executed
     * @param httpClient
     *            The client that will be used
     * @return
     */
    public InputStream getMethodResponseAsStream(HttpRequestBase method, HttpClient client) throws Exception {
        //invoke the method
        HttpResponse httpResponse = this.invokeTheMethod(method, client);
        return httpResponse.getEntity().getContent();
    }

    /**
     * Invokes a method and returns the binary response. (Creates a new HttpClient for use with this request)
     * 
     * @param method
     *            The method to be executed
     * @return
     */
    public byte[] getMethodResponseAsBytes(HttpRequestBase method) throws Exception {
        return getMethodResponseAsBytes(method, null);
    }

    /**
     * Invokes a method and returns the binary response.
     *
     * @param method
     *            The method to be executed
     * @param httpClient
     *            The client that will be used
     * @return
     */
    public byte[] getMethodResponseAsBytes(HttpRequestBase method, HttpClient client) throws Exception {
        //invoke the method
        HttpResponse httpResponse = this.invokeTheMethod(method, client);

        //get the response before we close the connection
        byte[] response = IOUtils.toByteArray(httpResponse.getEntity().getContent());

        //release the connection
        method.releaseConnection();

        //return it
        return response;
    }

    public HttpResponse getMethodResponseAsHttpResponse(HttpRequestBase method) throws Exception {
        return this.invokeTheMethod(method, null);
    }

    /**
     * Invokes a httpmethod and takes care of some error handling.
     * 
     * @param method
     * @param httpClient
     */
    private HttpResponse invokeTheMethod(HttpRequestBase method, HttpClient client) throws Exception {
        log.debug("method=" + method.getURI());
        HttpClient httpClient = null;

        if (client == null) {
            RequestConfig requestConfig = RequestConfig.custom().
                    setConnectTimeout(this.connectionTimeOut)
                    .setSocketTimeout(this.connectionTimeOut)
                    .build();

            HttpClientConnectionManager man = new PoolingHttpClientConnectionManager();

            httpClient = HttpClientBuilder.create()
                    .useSystemProperties()
                    .setConnectionManager(man)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } else {
            httpClient = client;
        }

        log.trace("Outgoing request headers: "
                + Arrays.toString(method.getAllHeaders()));

        // make the call
        HttpResponse response = httpClient.execute(method);
            
        int statusCode = response.getStatusLine().getStatusCode();
        String statusCodeText = HttpStatus.getStatusText(statusCode);
        log.trace("Status code text: '"+statusCodeText+"'");

        if (statusCode != HttpStatus.SC_OK &&
                statusCode != HttpStatus.SC_CREATED &&
                statusCode != HttpStatus.SC_ACCEPTED) {

            // if it's unavailable then throw connection exception
            if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                throw new ConnectException();
            }

            String responseBody = responseToString(response.getEntity().getContent());

            // if the response is not OK then throw an error
            
            log.error("Returned status line: " + response.getStatusLine() +
                    System.getProperty("line.separator") + "Returned response body: " + responseBody);
            throw new HttpException(statusCodeText);
        } else {
            return response;
        }
    }

    /**
     * Convert a Buffered stream into a String.
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    public String responseToString(InputStream stream) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        int bufferCount = -1;
        char[] bufferArray = new char[1024];
        //VT:Very bad idea to use readline when dealing with xml response as there may not be a new line
        //which will lead to oom on the heap.
        while ((bufferCount = reader.read(bufferArray)) != -1) {
            stringBuffer.append(new String(bufferArray, 0, bufferCount));
        }

        return stringBuffer.toString();
    }

    /**
     * Convert a HttpResponse into a String. Closes the HttpResponse after parsing the entire string.
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    public String responseToString(HttpResponse response) throws IOException {
        InputStream s = response.getEntity().getContent();
        try {
            return responseToString(s);
        } finally {
            s.close();
        }
    }
}
