package org.auscope.portal.gsml;

import java.lang.reflect.Field;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import junit.framework.Assert;

import org.junit.Test;

public class TestYilgarnNamespaceContext {
	
	private String TEST_PREFIX = "ogc";
    private String TEST_PREFIX_NOT_EXIST = "uHaHa";
    private String TEST_NAMESPACE_URI = "http://www.opengis.net/ogc";
    
    @Test
    public void testConstructor(){
        String s = "";
        try {
            final Field fields[] = YilgarnNamespaceContext.class.getDeclaredFields();

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
    
    
    @Test
    public void testContext() {

        NamespaceContext context = new YilgarnNamespaceContext();
        
        Assert.assertEquals(TEST_NAMESPACE_URI, context.getNamespaceURI(TEST_PREFIX));
        Assert.assertEquals(XMLConstants.NULL_NS_URI, context.getNamespaceURI(TEST_PREFIX_NOT_EXIST));
    }

}
