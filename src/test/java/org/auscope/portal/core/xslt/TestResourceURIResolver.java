package org.auscope.portal.core.xslt;

import javax.xml.transform.TransformerException;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestResourceURIResolver extends PortalTestClass {

    private ResourceURIResolver resolver;

    @Before
    public void setup() {
        resolver = new ResourceURIResolver(getClass());
    }

    @Test
    public void testGetFileRelative() throws TransformerException {
        Assert.assertNotNull(resolver.resolve("test-resource.txt", null));
    }

    @Test
    public void testGetFileAbsolute() throws TransformerException {
        Assert.assertNotNull(resolver.resolve("/org/auscope/portal/core/xslt/test-resource.txt", null));
    }

    @Test(expected = TransformerException.class)
    public void testGetFileDne() throws TransformerException {
        resolver.resolve("file-that-dne", null);
    }
}
