package org.auscope.portal.core.server.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;


/**
 * Utility class used to call web service end points.
 */
public class HttpServiceCaller {
    /**
     * The maximum amount of bytes (from a POST) that will be logged
     * if the logging level is set to TRACE
     */
    private static final int MAX_POST_BODY_LOGGING = 1024 * 100;
    private final Log log = LogFactory.getLog(getClass());
    private HttpClientConnectionManager connectionManager;
    private int connectionTimeOut;

    public HttpServiceCaller(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    /**
     * Set an (ideally) ThreadSafe HttpClientConnectionManager to be shared by all
     * HttpClients generated by this service instance
     *
     * @param connectionManager
     */
    public void setConnectionManager(HttpClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Generate a CloseableHttpClient using this instance's configuration
     * @param credentialsProvider if null, no credentialprovider will be used
     * @return
     */
    private CloseableHttpClient generateClient(CredentialsProvider credentialsProvider) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(this.connectionTimeOut)
                .setSocketTimeout(this.connectionTimeOut)
                .build();

        HttpClientBuilder builder = HttpClientBuilder.create()
                .useSystemProperties()
                .setDefaultRequestConfig(requestConfig);

        if (connectionManager != null) {
            builder.setConnectionManager(connectionManager);
        }

        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }

        return builder.build();
    }

    /**
     * Makes a call to a http GetMethod and returns the response as a string.
     *
     * @param method The method to be executed
     * @return
     * @throws ConnectException
     * @throws UnknownHostException
     * @throws ConnectTimeoutException
     * @throws IOException
     */
    public String getMethodResponseAsString(HttpRequestBase method) throws ConnectException, UnknownHostException,
        ConnectTimeoutException, IOException {
        return getMethodResponseAsString(method, (CredentialsProvider) null);
    }

    /**
     * Makes a call to a http GetMethod and returns the response as a string.
     *
     * @param method The method to be executed
     * @param credentialsProvider Credentials provider for performing authentication (if required)
     * @return
     * @throws ConnectException
     * @throws UnknownHostException
     * @throws ConnectTimeoutException
     * @throws IOException
     */
    public String getMethodResponseAsString(HttpRequestBase method, CredentialsProvider credentialsProvider) throws ConnectException, UnknownHostException,
            ConnectTimeoutException, IOException {
        try (CloseableHttpClient httpClient = generateClient(credentialsProvider)) {
            return getMethodResponseAsString(method, httpClient);
        }
    }

    /**
     * Makes a call to a http GetMethod and returns the response as a string.
     *
     * @param method
     *            The method to be executed
     * @param httpClient
     *            The client that will be used
     * @return
     * @throws IOException
     */
    public String getMethodResponseAsString(HttpRequestBase method, HttpClient client) throws IOException {
        //invoke the method
        HttpResponse httpResponse = this.invokeTheMethod(method, client);

        //get the reponse before we close the connection
        //String response = method.getResponseBodyAsString();

        String response;
        try {
            response = responseToString(httpResponse.getEntity().getContent());
        } finally {
            //release the connection
            method.releaseConnection();
        }

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
     * @param method The method to be executed
     * @param credentialsProvider Credentials provider for performing authentication (if required)
     * @return
     * @throws IOException
     */
    public HttpClientInputStream getMethodResponseAsStream(HttpRequestBase method) throws IOException {
        return getMethodResponseAsStream(method, (CredentialsProvider) null);
    }

    /**
     * Invokes a method and returns the binary response as a stream. (Creates a new HttpClient for use with this request)
     *
     * WARNING - ensure you call method.releaseConnection() AFTER you have finished reading the input stream.
     *
     * @param method The method to be executed
     * @param credentialsProvider Credentials provider for performing authentication (if required)
     * @return
     * @throws IOException
     */
    public HttpClientInputStream getMethodResponseAsStream(HttpRequestBase method, CredentialsProvider credentialsProvider) throws IOException {
        CloseableHttpClient httpClient = generateClient(credentialsProvider);
        return new HttpClientInputStream(this.getMethodResponseAsStream(method, httpClient), httpClient);
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
     * @throws IOException
     * @throws
     */
    public InputStream getMethodResponseAsStream(HttpRequestBase method, HttpClient client) throws IOException {
        //invoke the method
        HttpResponse httpResponse = this.invokeTheMethod(method, client);
        return httpResponse.getEntity().getContent();
    }

    /**
     * Invokes a method and returns the binary response. (Creates a new HttpClient for use with this request)
     *
     * @param method The method to be executed
     * @param credentialsProvider Credentials provider for performing authentication (if required)
     * @return
     * @throws IOException
     * @throws
     */
    public byte[] getMethodResponseAsBytes(HttpRequestBase method) throws IOException {
        return getMethodResponseAsBytes(method, (CredentialsProvider) null);
    }

    /**
     * Invokes a method and returns the binary response. (Creates a new HttpClient for use with this request)
     *
     * @param method The method to be executed
     * @param credentialsProvider Credentials provider for performing authentication (if required)
     * @return
     */
    public byte[] getMethodResponseAsBytes(HttpRequestBase method, CredentialsProvider credentialsProvider) throws IOException {
        try (CloseableHttpClient httpClient = generateClient(credentialsProvider)) {
            return getMethodResponseAsBytes(method, httpClient);
        }
    }

    /**
     * Invokes a method and returns the binary response.
     *
     * @param method The method to be executed
     * @param httpClient The client that will be used
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    public byte[] getMethodResponseAsBytes(HttpRequestBase method, HttpClient client) throws IOException {
        //invoke the method
        HttpResponse httpResponse = this.invokeTheMethod(method, client);

        //get the response before we close the connection
        byte[] response = IOUtils.toByteArray(httpResponse.getEntity().getContent());

        //release the connection
        method.releaseConnection();

        //return it
        return response;
    }

    /**
     * Invokes a method and returns the raw HttpClientResponse
     * @param method The method to be executed
     * @param credentialsProvider Credentials provider for performing authentication (if required)
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    public HttpClientResponse getMethodResponseAsHttpResponse(HttpRequestBase method) throws IllegalStateException, IOException {
        CloseableHttpClient httpClient = generateClient(null);
        return new HttpClientResponse(this.invokeTheMethod(method, httpClient), httpClient);
    }

    /**
     * Invokes a method and returns the raw HttpClientResponse
     * @param method The method to be executed
     * @param credentialsProvider Credentials provider for performing authentication (if required)
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    public HttpClientResponse getMethodResponseAsHttpResponse(HttpRequestBase method, CredentialsProvider credentialsProvider) throws IllegalStateException, IOException {
        CloseableHttpClient httpClient = generateClient(credentialsProvider);
        return new HttpClientResponse(this.invokeTheMethod(method, httpClient), httpClient);
    }

    /**
     * Invokes a httpmethod and takes care of some error handling.
     *
     * @param method
     * @param httpClient
     * @throws IOException
     * @throws IllegalStateException
     */
    private HttpResponse invokeTheMethod(HttpRequestBase method, HttpClient client) throws IOException {
        if(client==null) throw new IllegalArgumentException("HttpClient must not be null");

        log.debug("method=" + method.getURI());

        if (log.isTraceEnabled()) {
            log.trace("Outgoing request headers: " + Arrays.toString(method.getAllHeaders()));
            if (method instanceof HttpPost) {
                HttpEntity body = ((HttpPost) method).getEntity();
                byte[] dataHead =  new byte[(int) Math.min(MAX_POST_BODY_LOGGING, body.getContentLength())];
                IOUtils.read(body.getContent(), dataHead);
                String content = new String(dataHead, Charsets.UTF_8);
                log.trace("Outgoing POST body (UTF-8): " + content);
            }
        }

        //decode and re-encode the query portion of the URI.
        if (StringUtils.isNotBlank(method.getURI().getQuery())) {        	
            String decodedQuery = URLDecoder.decode(method.getURI().getQuery(), "UTF-8");        
            // the URI constructor does the encoding for us (" " -> "%20" etc)
            URI uri = new URI(method.getURI().getScheme(),
            		method.getURI().getHost(),
            		method.getURI().getPath(),
            		decodedQuery,
            		method.getURI().getFragment());
            method.setURI(uri);         	
        }          
        
        // make the call
        HttpResponse response = client.execute(method);

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

            log.error("Returned status line: " + response.getStatusLine());
            if (log.isTraceEnabled()) {
                String responseBody = responseToString(response.getEntity().getContent());
                log.trace("Returned response body: " + responseBody);
            }
            throw new HttpException(statusCodeText);
        } else {
            return response;
        }
    }

    /**
     * Convert a Buffered stream into a String and then closes the underlying stream (whether successful or not)
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private String responseToString(InputStream stream) throws IOException {
        try {
            return IOUtils.toString(stream);
        } finally {
            stream.close();
        }
    }
}
