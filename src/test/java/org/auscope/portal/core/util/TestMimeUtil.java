package org.auscope.portal.core.util;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestMimeUtil extends PortalTestClass {
    @Test
    public void testNullString() {
        Assert.assertEquals("", MimeUtil.mimeToFileExtension(null));
    }

    @Test
    public void testEmptyString() {
        Assert.assertEquals("", MimeUtil.mimeToFileExtension(""));
    }

    @Test
    public void testNonsenseString() {
        Assert.assertEquals("", MimeUtil.mimeToFileExtension("asjk/ldh/i/lw/dn\\la\n\\w\\dl;an;"));
    }

    @Test
    public void testImageString() {
        Assert.assertEquals("", MimeUtil.mimeToFileExtension("image/"));
        Assert.assertEquals("foo", MimeUtil.mimeToFileExtension("image/foo"));
        Assert.assertEquals("png", MimeUtil.mimeToFileExtension("image/png;mode=8bit"));
        Assert.assertEquals("tiff", MimeUtil.mimeToFileExtension("image/geotiff"));
    }
}
