package org.auscope.portal.gsml;

import java.lang.reflect.Field;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import junit.framework.Assert;

import org.junit.Test;


/**
 * The Class TestYilgarnNamespaceContext.
 */
public class TestYilgarnNamespaceContext {

    /** The TEST prefix. */
    private static final String TESTPREFIX = "ogc";

    /** The TEST_FAKE_PREFIX . */
    private static final String TESTFAKEPREFIX = "uHaHa";

    /** The namespace uri. */
    private static final String TESTNAMESPACEURI = "http://www.opengis.net/ogc";

    /**
     * Test constructor.
     */
    @Test
    public void testConstructor() {
        String s = "";
        try {
            final Field[] fields = YilgarnNamespaceContext.class.getDeclaredFields();

            for (int i = 0; i < fields.length; ++i) {
                if ("map".equals(fields[i].getName())) {
                    fields[i].setAccessible(true);
                    s = (fields[i].get(new YilgarnNamespaceContext())).toString();
                    break;
                }
            }
            Assert.assertTrue("Expected \"map\" to be pre-populated with some prefixes/URIs in the constructor",s.length() > 2);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }


    /**
     * Test context.
     */
    @Test
    public void testContext() {

        NamespaceContext context = new YilgarnNamespaceContext();

        Assert.assertEquals(TESTNAMESPACEURI, context.getNamespaceURI(TESTPREFIX));
        Assert.assertEquals(XMLConstants.NULL_NS_URI, context.getNamespaceURI(TESTFAKEPREFIX));
    }

}
