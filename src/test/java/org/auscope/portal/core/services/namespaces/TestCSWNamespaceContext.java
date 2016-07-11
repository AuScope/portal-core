package org.auscope.portal.core.services.namespaces;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author san218
 * @version $Id$
 */
@SuppressWarnings("deprecation")
public class TestCSWNamespaceContext extends PortalTestClass {

    private String TEST_PREFIX = "gmd";
    private String TEST_PREFIX_NOT_EXIST = "uHaHa";
    private String TEST_NAMESPACE_URI = "http://www.isotc211.org/2005/gmd";

    @Test
    public void testContext() {

        NamespaceContext context = new CSWNamespaceContext();

        Assert.assertEquals(TEST_NAMESPACE_URI, context.getNamespaceURI(TEST_PREFIX));
        Assert.assertEquals(XMLConstants.NULL_NS_URI, context.getNamespaceURI(TEST_PREFIX_NOT_EXIST));
    }
}
