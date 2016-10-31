package org.auscope.portal.core.services.namespaces;

import java.util.Iterator;

import javax.xml.XMLConstants;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestIterableNamespace extends PortalTestClass {

    private class TestableIterableNamespace extends IterableNamespace {
        public TestableIterableNamespace() {
            map.put("pref1", "namespace1");
            map.put("pref2", "namespace2");
        }
    }

    /**
     * Tests the fetching of namespaces
     */
    @Test
    public void testGetNamespaceUri() {
        TestableIterableNamespace ns = new TestableIterableNamespace();

        Assert.assertEquals("namespace1", ns.getNamespaceURI("pref1"));
        Assert.assertEquals("namespace2", ns.getNamespaceURI("pref2"));
        Assert.assertEquals(XMLConstants.NULL_NS_URI, ns.getNamespaceURI("dne"));
    }

    /**
     * tests the iteration of prefixes
     */
    @Test
    public void testIteratePrefixes() {
        TestableIterableNamespace ns = new TestableIterableNamespace();
        boolean matchedPref1 = false;
        boolean matchedPref2 = false;

        Iterator<String> i = ns.getPrefixIterator();
        while (i.hasNext()) {
            String next = i.next();
            if (next.equals("pref1")) {
                Assert.assertFalse(matchedPref1);
                matchedPref1 = true;
            }
            if (next.equals("pref2")) {
                Assert.assertFalse(matchedPref2);
                matchedPref2 = true;
            }
        }

        Assert.assertTrue(matchedPref1);
        Assert.assertTrue(matchedPref2);
    }
}
