/**
 * 
 */
package org.auscope.portal.core.server.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * @author fri096
 *
 */
public class HttpClientInputStream extends InputStream {

    private InputStream inputStream;
    /**
     * @return
     * @throws IOException
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    /**
     * @param b
     * @return
     * @throws IOException
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    /**
     * @param b
     * @param off
     * @param len
     * @return
     * @throws IOException
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    /**
     * @param n
     * @return
     * @throws IOException
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    /**
     * @return
     * @throws IOException
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    /**
     * @throws IOException
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
        if(httpClient!=null)
            httpClient.close();
    }

    /**
     * @param readlimit
     * @see java.io.InputStream#mark(int)
     */
    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    /**
     * @throws IOException
     * @see java.io.InputStream#reset()
     */
    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    /**
     * @return
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    private CloseableHttpClient httpClient;

    public HttpClientInputStream(InputStream inputStream, CloseableHttpClient httpClient) {
        this.inputStream=inputStream;
        this.httpClient=httpClient;
    }

}
