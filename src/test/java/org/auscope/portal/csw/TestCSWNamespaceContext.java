package org.auscope.portal.csw;

import java.lang.reflect.Field;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import junit.framework.Assert;

import org.junit.Test;

/** 
 * @author san218
 * @version $Id$
 */
public class TestCSWNamespaceContext {

    private String TEST_PREFIX = "gmd";
    private String TEST_PREFIX_NOT_EXIST = "uHaHa";
    private String TEST_NAMESPACE_URI = "http://www.isotc211.org/2005/gmd";
    
    @Test
    public void testConstructor(){
        String s = "";
        try {
            final Field fields[] = CSWNamespaceContext.class.getDeclaredFields();

            for (int i = 0; i < fields.length; ++i) {
                if ("map".equals(fields[i].getName())) {
                    fields[i].setAccessible(true);
                    s = (fields[i].get(new CSWNamespaceContext())).toString();
                    break;
                }
            }
            Assert.assertTrue("Expected \"map\" to be pre-populated with some prefixes/URIs in the constructor",s.length() > 2);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    
    @Test
    public void testContext() {

        NamespaceContext context = new CSWNamespaceContext();
        
        Assert.assertEquals(TEST_NAMESPACE_URI, context.getNamespaceURI(TEST_PREFIX));
        Assert.assertEquals(XMLConstants.NULL_NS_URI, context.getNamespaceURI(TEST_PREFIX_NOT_EXIST));
    }
}
