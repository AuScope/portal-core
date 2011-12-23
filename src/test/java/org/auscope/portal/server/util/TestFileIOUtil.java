package org.auscope.portal.server.util;

import java.io.IOException;
import java.io.InputStream;

import org.auscope.portal.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Test;

/**
 * Unit tests for FileIOUtil
 * @author Josh Vote
 *
 */
public class TestFileIOUtil extends PortalTestClass {
    private InputStream mockInputStream = context.mock(InputStream.class);

    /**
     * Tests closeQuietely works as intended
     * @throws Exception
     */
    @Test
    public void testCloseQuietly() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockInputStream).close();
        }});

        FileIOUtil.closeQuietly(mockInputStream);
    }

    /**
     * Tests closeQuietely works as intended when an error is thrown
     * @throws Exception
     */
    @Test
    public void testCloseQuietlyError() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockInputStream).close();will(throwException( new IOException()));
        }});

        FileIOUtil.closeQuietly(mockInputStream);
    }

    /**
     * Tests closeQuietely works as intended when a null reference is passed
     * @throws Exception
     */
    @Test
    public void testCloseQuietlyNull() throws Exception {
        context.checking(new Expectations() {{

        }});

        FileIOUtil.closeQuietly(null);
    }
}
