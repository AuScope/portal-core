package org.auscope.portal.core.util;

import java.io.IOException;
import java.io.InputStream;

import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Test;

/**
 * Unit tests for FileIOUtil
 * 
 * @author Josh Vote
 *
 */
public class TestFileIOUtil extends PortalTestClass {
    private InputStream mockInputStream = context.mock(InputStream.class);

    /**
     * Tests closeQuietely works as intended
     * @throws IOException 
     */
    @Test
    public void testCloseQuietly() throws IOException {
        context.checking(new Expectations() {
            {
                oneOf(mockInputStream).close();
            }
        });

        FileIOUtil.closeQuietly(mockInputStream);
    }

    /**
     * Tests closeQuietely works as intended when an error is thrown
     * @throws IOException 
     */
    @Test
    public void testCloseQuietlyError() throws IOException {
        context.checking(new Expectations() {
            {
                oneOf(mockInputStream).close();
                will(throwException(new IOException()));
            }
        });

        FileIOUtil.closeQuietly(mockInputStream);
    }

    /**
     * Tests closeQuietely works as intended when a null reference is passed
     */
    @Test
    public void testCloseQuietlyNull() {
        context.checking(new Expectations());

        FileIOUtil.closeQuietly(null);
    }
}
