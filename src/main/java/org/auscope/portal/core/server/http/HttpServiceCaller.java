package org.auscope.portal.core.server.http;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.auscope.portal.core.server.http.responses.ByteArrayResponseHandler;
import org.auscope.portal.core.server.http.responses.DomResponseHandler;
import org.auscope.portal.core.server.http.responses.PipingResponseHandler;
import org.auscope.portal.core.server.http.responses.StringResponseHandler;
import org.w3c.dom.Document;


/**
 * Utility class used to call web service end points.
 */
public class HttpServiceCaller {
    private final Log log = LogFactory.getLog(getClass());

    private HttpClient httpClient;

    /**
     * Configures this class with new org.apache.http.impl.client.DefaultHttpClient configured with the specified params
     *
     * A PoolingClientConnectionManager is created to manage multiple connections.
     *
     * @param httpParams The params to configure a org.apache.http.impl.client.DefaultHttpClient
     */
    public HttpServiceCaller(int maxConnections, int maxConnectionsPerRoute) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);

        cm.setMaxTotal(maxConnections);
        cm.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        this.httpClient = new DefaultHttpClient(cm);
    }

    /**
     * Inject a HttpClient to power this class
     * @param httpClient
     */
    public HttpServiceCaller(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Makes a request using a HTTP method and handles the response by returning it as a string
     *
     * @param method The method to be executed
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public String getMethodResponseAsString(HttpRequestBase method) throws ClientProtocolException, IOException  {
        String response = getMethodResponse(method, new StringResponseHandler());

        log.trace("String response from server:");
        log.trace("\n" + response);

        //return it
        return response;
    }

    /**
     * Makes a request using a HTTP method and handles the response by returning it as an array of bytes
     *
     * @param method The method to be executed
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public byte[] getMethodResponseAsBytes(HttpRequestBase method) throws ClientProtocolException, IOException  {
        return getMethodResponse(method, new ByteArrayResponseHandler());
    }

    /**
     * Makes a request using a HTTP method and handles the response by returning it as a parsed XML document
     *
     * @param method The method to be executed
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws Exception
     */
    public Document getMethodResponseAsDocument(HttpRequestBase method) throws ClientProtocolException, IOException  {
        return getMethodResponse(method, new DomResponseHandler());
    }

    /**
     * Makes a request using a HTTP method and handles the response by piping it into an OutputStream.
     *
     * The count of bytes piped is returned
     * @param method The method to be executed
     * @param os The output stream to receive the response bytes
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    public Integer getMethodResponsePiped(HttpRequestBase method, OutputStream os) throws ClientProtocolException, IOException {
        return getMethodResponse(method, new PipingResponseHandler(os));
    }

    /**
     * Makes a request using a HTTP method and handles the response using the specified response handler
     * @param method The method to be executed
     * @param responseHandler Will be used to parse the response
     * @param method
     * @param responseHandler
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public <T> T getMethodResponse(HttpRequestBase method, ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
        return getMethodResponse(method, responseHandler, null);
    }

    /**
     * Makes a request using a HTTP method and handles the response using the specified response handler
     * @param method The method to be executed
     * @param responseHandler Will be used to parse the response
     * @param context the HTTP context that will be used for this request
     */
    public <T> T getMethodResponse(HttpRequestBase method, ResponseHandler<T> responseHandler, HttpContext context) throws ClientProtocolException, IOException {
        return httpClient.execute(method, responseHandler, context);
    }
}
