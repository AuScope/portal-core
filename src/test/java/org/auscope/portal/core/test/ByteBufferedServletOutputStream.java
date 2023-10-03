package org.auscope.portal.core.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

/**
 * Test class extension of a ServletOutputStream that allows access to an underlying byte[] of data that was written
 * 
 * @author vot002
 *
 */
public class ByteBufferedServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream stream;

    public ByteBufferedServletOutputStream(int length) {
        super();

        stream = new ByteArrayOutputStream(length);
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    public byte[] toByteArray() {
        return stream.toByteArray();
    }

    public ByteArrayOutputStream getStream() {
        return this.stream;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }
}
