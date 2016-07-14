/**
 * 
 */
package org.auscope.portal.core.server.http;

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;

/**
 * @author fri096
 *
 */
@SuppressWarnings("deprecation")
public class HttpClientResponse implements HttpResponse, Closeable {

    private HttpResponse response;
    /**
     * @return
     * @see org.apache.http.HttpResponse#getStatusLine()
     */
    @Override
    public StatusLine getStatusLine() {
        return response.getStatusLine();
    }

    /**
     * @param statusline
     * @see org.apache.http.HttpResponse#setStatusLine(org.apache.http.StatusLine)
     */
    @Override
    public void setStatusLine(StatusLine statusline) {
        response.setStatusLine(statusline);
    }

    /**
     * @return
     * @see org.apache.http.HttpMessage#getProtocolVersion()
     */
    @Override
    public ProtocolVersion getProtocolVersion() {
        return response.getProtocolVersion();
    }

    /**
     * @param ver
     * @param code
     * @see org.apache.http.HttpResponse#setStatusLine(org.apache.http.ProtocolVersion, int)
     */
    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        response.setStatusLine(ver, code);
    }

    /**
     * @param name
     * @return
     * @see org.apache.http.HttpMessage#containsHeader(java.lang.String)
     */
    @Override
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    /**
     * @param ver
     * @param code
     * @param reason
     * @see org.apache.http.HttpResponse#setStatusLine(org.apache.http.ProtocolVersion, int, java.lang.String)
     */
    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        response.setStatusLine(ver, code, reason);
    }

    /**
     * @param name
     * @return
     * @see org.apache.http.HttpMessage#getHeaders(java.lang.String)
     */
    @Override
    public Header[] getHeaders(String name) {
        return response.getHeaders(name);
    }

    /**
     * @param code
     * @throws IllegalStateException
     * @see org.apache.http.HttpResponse#setStatusCode(int)
     */
    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        response.setStatusCode(code);
    }

    /**
     * @param name
     * @return
     * @see org.apache.http.HttpMessage#getFirstHeader(java.lang.String)
     */
    @Override
    public Header getFirstHeader(String name) {
        return response.getFirstHeader(name);
    }

    /**
     * @param reason
     * @throws IllegalStateException
     * @see org.apache.http.HttpResponse#setReasonPhrase(java.lang.String)
     */
    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        response.setReasonPhrase(reason);
    }

    /**
     * @param name
     * @return
     * @see org.apache.http.HttpMessage#getLastHeader(java.lang.String)
     */
    @Override
    public Header getLastHeader(String name) {
        return response.getLastHeader(name);
    }

    /**
     * @return
     * @see org.apache.http.HttpResponse#getEntity()
     */
    @Override
    public HttpEntity getEntity() {
        return response.getEntity();
    }

    /**
     * @param entity
     * @see org.apache.http.HttpResponse#setEntity(org.apache.http.HttpEntity)
     */
    @Override
    public void setEntity(HttpEntity entity) {
        response.setEntity(entity);
    }

    /**
     * @return
     * @see org.apache.http.HttpMessage#getAllHeaders()
     */
    @Override
    public Header[] getAllHeaders() {
        return response.getAllHeaders();
    }

    /**
     * @param header
     * @see org.apache.http.HttpMessage#addHeader(org.apache.http.Header)
     */
    @Override
    public void addHeader(Header header) {
        response.addHeader(header);
    }

    /**
     * @param name
     * @param value
     * @see org.apache.http.HttpMessage#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    /**
     * @return
     * @see org.apache.http.HttpResponse#getLocale()
     */
    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    /**
     * @param header
     * @see org.apache.http.HttpMessage#setHeader(org.apache.http.Header)
     */
    @Override
    public void setHeader(Header header) {
        response.setHeader(header);
    }

    /**
     * @param loc
     * @see org.apache.http.HttpResponse#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale loc) {
        response.setLocale(loc);
    }

    /**
     * @param name
     * @param value
     * @see org.apache.http.HttpMessage#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    /**
     * @param headers
     * @see org.apache.http.HttpMessage#setHeaders(org.apache.http.Header[])
     */
    @Override
    public void setHeaders(Header[] headers) {
        response.setHeaders(headers);
    }

    /**
     * @param header
     * @see org.apache.http.HttpMessage#removeHeader(org.apache.http.Header)
     */
    @Override
    public void removeHeader(Header header) {
        response.removeHeader(header);
    }

    /**
     * @param name
     * @see org.apache.http.HttpMessage#removeHeaders(java.lang.String)
     */
    @Override
    public void removeHeaders(String name) {
        response.removeHeaders(name);
    }

    /**
     * @return
     * @see org.apache.http.HttpMessage#headerIterator()
     */
    @Override
    public HeaderIterator headerIterator() {
        return response.headerIterator();
    }

    /**
     * @param name
     * @return
     * @see org.apache.http.HttpMessage#headerIterator(java.lang.String)
     */
    @Override
    public HeaderIterator headerIterator(String name) {
        return response.headerIterator(name);
    }

    /**
     * @return
     * @deprecated
     * @see org.apache.http.HttpMessage#getParams()
     */
    @Deprecated
    @Override
    public HttpParams getParams() {
        return response.getParams();
    }

    /**
     * @param params
     * @deprecated
     * @see org.apache.http.HttpMessage#setParams(org.apache.http.params.HttpParams)
     */
    @Deprecated
    @Override
    public void setParams(HttpParams params) {
        response.setParams(params);
    }

    private CloseableHttpClient httpClient;

    public HttpClientResponse(HttpResponse response, CloseableHttpClient httpClient) {
        this.response=response;
        this.httpClient=httpClient;
    }

    @Override
    public void close() throws IOException {
        if(httpClient !=null)
            httpClient.close();        
    }

}
