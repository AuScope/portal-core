package org.auscope.portal.core.server.http.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

public class MyHttpEntity implements HttpEntity {

    InputStream content;

    public MyHttpEntity(InputStream is) {
        this.content = is;
    }

    @Override
    public boolean isRepeatable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChunked() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getContentLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Header getContentType() {
        return new BasicHeader("type", "application/xml");
    }

    @Override
    public Header getContentEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getContent() throws IOException,
            IllegalStateException {
        return this.content;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isStreaming() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void consumeContent() throws IOException {
        // TODO Auto-generated method stub

    }

}
