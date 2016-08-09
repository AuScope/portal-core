package org.auscope.portal.core.services.namespaces;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author san218
 * @version $Id$
 */
public class TestCSWNamespaceContext extends PortalTestClass {

    private String TEST_PREFIX = "gmd";
    private String TEST_PREFIX_NOT_EXIST = "uHaHa";
    private String TEST_NAMESPACE_URI = "http://www.isotc211.org/2005/gmd";

    @Test
    public void testContext() {

        NamespaceContext ctx = new CSWNamespaceContext();

        Assert.assertEquals(TEST_NAMESPACE_URI, ctx.getNamespaceURI(TEST_PREFIX));
        Assert.assertEquals(XMLConstants.NULL_NS_URI, ctx.getNamespaceURI(TEST_PREFIX_NOT_EXIST));
    }
}
