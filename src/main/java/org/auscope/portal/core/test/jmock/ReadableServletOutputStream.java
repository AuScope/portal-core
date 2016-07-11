package org.auscope.portal.core.test.jmock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletOutputStream;

/**
 * A ServletOutputStream extension that buffers any written data and gives methods to access that data.
 * 
 * Use this for testing raw output stream writing functions
 * 
 * @author Josh Vote
 *
 */
public class ReadableServletOutputStream extends ServletOutputStream {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    @Override
    public void write(int i) throws IOException {
        byteArrayOutputStream.write(i);
    }

    public byte[] getDataWritten() {
        return byteArrayOutputStream.toByteArray();
    }

    public ZipInputStream getZipInputStream() {
        return new ZipInputStream(new ByteArrayInputStream(getDataWritten()));
    }
}
