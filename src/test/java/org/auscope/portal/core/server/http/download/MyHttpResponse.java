package org.auscope.portal.core.server.http.download;

import java.io.InputStream;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.message.BasicHeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.auscope.portal.core.server.http.HttpClientResponse;


/*
 *
 * This is a dummy class used for testing purposes only
 *
 */
@SuppressWarnings("deprecation")
public class MyHttpResponse extends HttpClientResponse {
    private static final String LOCATION_HDR = "Location";
    InputStream content;
    int statusCode;
    String location;

    public MyHttpResponse(InputStream is) {
        super(null, null);
        this.content = is;
        this.statusCode = 200;
    }

    public MyHttpResponse(InputStream is, int statusCode) {
        super(null, null);
        this.content = is;
        this.statusCode = statusCode;
    }

    public MyHttpResponse(InputStream is, int statusCode, String location) {
        super(null, null);
        this.content = is;
        this.statusCode = statusCode;
        this.location = location;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public Header[] getHeaders(String name) {
        return null;
    }

    @Override
    public Header getFirstHeader(String name) {
        if (name.equals("Location")) {
            return new BasicHeader(LOCATION_HDR, this.location);
        }
        return null;
    }

    @Override
    public Header getLastHeader(String name) {
        if (name.equals("Location")) {
            return new BasicHeader(LOCATION_HDR, this.location);
        }
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        if (this.location != null) {
            Header[] headerArr = {new BasicHeader(LOCATION_HDR, this.location)};
            return headerArr;
        }
        return null;
    }

    @Override
    public void addHeader(Header header) {
        if (header.getName().equals(LOCATION_HDR)) {
            this.location = header.getValue();
        }
    }

    @Override
    public void addHeader(String name, String value) {
        if (name.equals(LOCATION_HDR)) {
            this.location = value;
        }
    }

    @Override
    public void setHeader(Header header) {
        if (header.getName().equals(LOCATION_HDR)) {
            this.location = header.getValue();
        }
    }

    @Override
    public void setHeader(String name, String value) {
        if (name.equals(LOCATION_HDR)) {
            this.location = value;
        }
    }

    @Override
    public void setHeaders(Header[] headers) {
        for (Header header: headers) {
            if (header.getName().equals(LOCATION_HDR)) {
                this.location = header.getValue();
            }
        }
    }

    @Override
    public void removeHeader(Header header) {
        if (header.getName().equals(LOCATION_HDR)) {
            this.location = null;
        }
    }

    @Override
    public void removeHeaders(String name) {
        if (name.equals(LOCATION_HDR)) {
            this.location = null;
        }
    }

    @Override
    public HeaderIterator headerIterator() {
        if (this.location == null) {
            return null;
        }
        Header[] headers=new Header[] {new BasicHeader(LOCATION_HDR,this.location)};
        return new BasicHeaderIterator(headers,null);
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        if (name.equals(LOCATION_HDR)) {
            Header[] headers=new Header[] {new BasicHeader(LOCATION_HDR,this.location)};
            return new BasicHeaderIterator(headers,null);
        }
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public void setParams(HttpParams params) {
        // empty
    }

    @Override
    public StatusLine getStatusLine() {
        return new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), this.statusCode, "reason");
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        // empty
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        // empty
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        // empty
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        // empty
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        // empty
    }

    @Override
    public HttpEntity getEntity() {
        MyHttpEntity entity = new MyHttpEntity(this.content);
        return entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        // empty
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(Locale loc) {
        // empty
    }

}