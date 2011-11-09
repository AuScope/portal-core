package org.auscope.portal.manager.download;

import java.io.IOException;
import java.io.InputStream;

import org.auscope.portal.server.util.FileIOUtil;

public class DownloadResponse {
    private InputStream responseStream;
    private Exception exception;
    private String requestURL;

    public DownloadResponse(String url){
        requestURL=url;
        exception=null;
        responseStream=null;
    }

    public String getResponseAsString() throws IOException{
        return FileIOUtil.convertStreamtoString(responseStream);
    }

    public String getExceptionAsString(){
        return FileIOUtil.CovertExceptionToString(exception, getRequestURL());
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
    public Exception getException() {
        return exception;
    }

    public void setResponseStream(InputStream responseStream) {
        this.responseStream = responseStream;
    }

    public InputStream getResponseAsStream() {
        return responseStream;
    }

    public boolean hasException(){
        return exception != null;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public String toString(){
        try {
            return this.getResponseAsString() + this.getExceptionAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

}
