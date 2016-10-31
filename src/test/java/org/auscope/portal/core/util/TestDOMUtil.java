package org.auscope.portal.core.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.sf.saxon.xpath.XPathFactoryImpl;

/**
 * Unit tests for DOMUtil
 *
 * @author Josh Vote
 *
 */
public class TestDOMUtil extends PortalTestClass {
    /**
     * Simple test to ensure that the 2 DOM util methods are reversible
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     * @throws TransformerException 
     */
    @Test
    public void testReversibleTransformation() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        final String originalXmlString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/xml/TestXML_NoPrettyPrint.xml");

        final Document doc = DOMUtil.buildDomFromString(originalXmlString);
        final String newXmlString = DOMUtil.buildStringFromDom(doc, false);

        Assert.assertEquals(originalXmlString, newXmlString);
    }

    /**
     * Namespace for use with src/test/resources/TestXML_NoPrettyPrint.xml
     *
     * @author vot002
     *
     */
    public class SimpleXMLNamespace implements NamespaceContext {

        private Map<String, String> map;

        public SimpleXMLNamespace() {
            map = new HashMap<>();
            map.put("test", "http://test.namespace");
            map.put("test2", "http://test2.namespace");
        }

        /**
         * This method returns the uri for all prefixes needed.
         *
         * @param prefix
         * @return uri
         */
        @Override
        public String getNamespaceURI(final String prefix) {
            if (prefix == null)
                throw new IllegalArgumentException("No prefix provided!");

            if (map.containsKey(prefix))
                return map.get(prefix);
            else
                return XMLConstants.NULL_NS_URI;

        }

        @Override
        public String getPrefix(final String namespaceURI) {
            // Not needed in this context.
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(final String namespaceURI) {
            // Not needed in this context.
            return null;
        }
    }

    /**
     * Simple test to ensure that the DOM object is namespace aware
     * @throws XPathExpressionException 
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    @Test
    public void testDOMObjectNamespace() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        //Build our DOM
        final String originalXmlString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/xml/TestXML_NoPrettyPrint.xml");
        final Document doc = DOMUtil.buildDomFromString(originalXmlString);

        //Build our queries (namespace aware)
        final XPathFactory factory = new XPathFactoryImpl();
        final XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new SimpleXMLNamespace());
        final XPathExpression getChild1Expr = xPath.compile("test:root/test2:child1");
        final XPathExpression getChild2Expr = xPath.compile("test:root/test2:child2");
        final XPathExpression failingExpr = xPath.compile("root/child1");

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
