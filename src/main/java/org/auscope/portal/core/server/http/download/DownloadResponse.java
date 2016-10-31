package org.auscope.portal.core.server.http.download;

import java.io.IOException;
import java.io.InputStream;

import org.auscope.portal.core.util.FileIOUtil;

public class DownloadResponse {
    private InputStream responseStream;
    private Throwable exception;
    private String requestURL;
    private String contentType;

    public DownloadResponse(String url) {
        requestURL = url;
        exception = null;
        responseStream = null;
        contentType = "";
    }

    public String getResponseAsString() throws IOException {
        return FileIOUtil.convertStreamtoString(responseStream);
    }

    public String getExceptionAsString() {
        return FileIOUtil.convertExceptionToString(exception, getRequestURL());
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

    public void setResponseStream(InputStream responseStream) {
        this.responseStream = responseStream;
    }

    public InputStream getResponseAsStream() {
        return responseStream;
    }

    public boolean hasException() {
        return exception != null;
    }

    public String getRequestURL() {
        return requestURL;
    }

    @Override
    public String toString() {
        try {
            return this.getResponseAsString() + this.getExceptionAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
