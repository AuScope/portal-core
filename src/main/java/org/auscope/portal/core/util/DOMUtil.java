package org.auscope.portal.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility functions for interacting with a DOM object
 *
 * @author Matt Wyatt
 * @author Josh Vote
 */
public class DOMUtil {

    /**
     * Utility for accessing a consistent DocumentBuilderFactory (irregardless of what is on the classpath)
     *
     * @return
     */
    private static DocumentBuilderFactory getDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(
                "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", null);
        return factory;
    }

    /**
     * Given a String containing XML, parse it and return a DOM object representation (that is namespace aware).
     *
     * @param xmlString
     *            A string containing valid XML
     * @return
     */
    public static Document buildDomFromString(String xmlString) throws ParserConfigurationException, IOException,
            SAXException {
        return buildDomFromString(xmlString, true);
    }

    /**
     * Given a String containing XML, parse it and return a DOM object representation
     *
     * @param xmlString
     *            A string containing valid XML
     * @param isNamespaceAware
     *            Will this DOM document take into account namespaces?
     * @return
     */
    public static Document buildDomFromString(String xmlString, boolean isNamespaceAware)
            throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = getDocumentBuilderFactory();
        factory.setNamespaceAware(isNamespaceAware); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString.toString()));
        Document doc = builder.parse(inputSource);
        return doc;
    }

    /**
     * Given a Stream containing XML, parse it and return a DOM object representation (that is namespace aware).
     *
     * @param xmlString
     *            A string containing valid XML
     * @return
     */
    public static Document buildDomFromStream(InputStream stream) throws ParserConfigurationException, IOException,
            SAXException {
        return buildDomFromStream(stream, true);
    }

    /**
     * Given a Stream containing XML, parse it and return a DOM object representation (that is namespace aware).
     *
     * @param xmlString
     *            A string containing valid XML
     * @return
     */
    public static Document buildDomFromStream(InputStream stream, boolean isNamespaceAware)
            throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = getDocumentBuilderFactory();
        factory.setNamespaceAware(isNamespaceAware); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(stream);
        return doc;
    }

    /**
     * Given a DOM (sub)tree generate a string representation with no formatting
     *
     * @param node
     *            The node to generate the XML for
     * @param omitXmlDeclaration
     *            Whether the <?xml?> header should be omitted
     *
     * @return
     * @throws TransformerException
     */
    public static String buildStringFromDom(Node node, boolean omitXmlDeclaration) throws TransformerException {
        StringWriter outText = new StringWriter();
        StreamResult sr = new StreamResult(outText);
        Properties oprops = new Properties();
        oprops.put(OutputKeys.METHOD, "xml");
        if (omitXmlDeclaration) {
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } else {
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        }
        TransformerFactory tf = TransformerFactory.newInstance();

        Transformer t = tf.newTransformer();
        t.setOutputProperties(oprops);
        t.transform(new DOMSource(node), sr);

        return outText.toString();
    }

    /**
     * Compiles the specified XPath (as a string) into an XPathExpression.
     *
     * @param xPathStr
     *            A string representing a valid XPath expression
     * @param nc
     *            The namespace that the xPathStr is referencing
     * @return
     * @throws XPathExpressionException
     */
    public static XPathExpression compileXPathExpr(String xPathStr, NamespaceContext nc)
            throws XPathExpressionException {
        //Use saxon explicitly for namespace aware XPath - it's much more performant
        XPathFactory factory = new net.sf.saxon.xpath.XPathFactoryImpl();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(nc);
        return xPath.compile(xPathStr);
    }

    /**
     * Compiles the specified XPath (as a string) into an XPathExpression.
     *
     * @param xPathStr
     *            A string representing a valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    public static XPathExpression compileXPathExpr(String xPathStr) throws XPathExpressionException {
        //Use JAXP for namespace unaware xpath - saxon doesnt handle this sort of behaviour
        //http://stackoverflow.com/questions/21118051/namespace-unaware-xpath-expression-fails-if-saxon-is-on-the-classpath
        XPathFactory factory = new org.apache.xpath.jaxp.XPathFactoryImpl();
        XPath xPath = factory.newXPath();
        return xPath.compile(xPathStr);
    }
}
