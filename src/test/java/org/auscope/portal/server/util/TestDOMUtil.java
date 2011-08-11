package org.auscope.portal.server.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import net.sf.saxon.xpath.XPathFactoryImpl;

import org.auscope.portal.Util;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Unit tests for DOMUtil
 * @author Josh Vote
 *
 */
public class TestDOMUtil {
    /**
     * Simple test to ensure that the 2 DOM util methods are reversible
     * @throws Exception
     */
    @Test
    public void testReversibleTransformation() throws Exception{
        String originalXmlString = Util.loadXML("src/test/resources/TestXML_NoPrettyPrint.xml");

        Document doc = DOMUtil.buildDomFromString(originalXmlString);
        String newXmlString = DOMUtil.buildStringFromDom(doc, false);

        Assert.assertEquals(originalXmlString, newXmlString);
    }

    /**
     * Namespace for use with src/test/resources/TestXML_NoPrettyPrint.xml
     * @author vot002
     *
     */
    public class SimpleXMLNamespace implements NamespaceContext {

        private Map<String, String> map;

        public SimpleXMLNamespace() {
            map = new HashMap<String, String>();
            map.put("test", "http://test.namespace");
            map.put("test2", "http://test2.namespace");
        };

        /**
         * This method returns the uri for all prefixes needed.
         * @param prefix
         * @return uri
         */
        public String getNamespaceURI(String prefix) {
            if (prefix == null)
                throw new IllegalArgumentException("No prefix provided!");

            if (map.containsKey(prefix))
                return map.get(prefix);
            else
                return XMLConstants.NULL_NS_URI;

        }

        public String getPrefix(String namespaceURI) {
            // Not needed in this context.
            return null;
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            // Not needed in this context.
            return null;
        }
    }

    /**
     * Simple test to ensure that the DOM object is namespace aware
     * @throws Exception
     */
    @Test
    public void testDOMObjectNamespace() throws Exception{
        //Build our DOM
        String originalXmlString = Util.loadXML("src/test/resources/TestXML_NoPrettyPrint.xml");
        Document doc = DOMUtil.buildDomFromString(originalXmlString);

        //Build our queries (namespace aware)
        XPathFactory factory = new XPathFactoryImpl();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new SimpleXMLNamespace());
        XPathExpression getChild1Expr = xPath.compile("test:root/test2:child1");
        XPathExpression getChild2Expr = xPath.compile("test:root/test2:child2");
        XPathExpression failingExpr = xPath.compile("root/child1");

        Node testNode = (Node) getChild1Expr.evaluate(doc, XPathConstants.NODE);
        Assert.assertNotNull(testNode);
        Assert.assertEquals("child1Value", testNode.getTextContent());

        testNode = (Node) getChild2Expr.evaluate(doc, XPathConstants.NODE);
        Assert.assertNotNull(testNode);
        Assert.assertEquals("child2Value", testNode.getTextContent());

        //This should fail (no namespace specified)
        testNode = (Node) failingExpr.evaluate(doc, XPathConstants.NODE);
        Assert.assertNull(testNode);
    }
}
