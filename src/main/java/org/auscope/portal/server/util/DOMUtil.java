package org.auscope.portal.server.util;

import net.sf.saxon.xpath.XPathFactoryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
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

import java.io.InputStream;
import java.io.StringReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Utility functions for interacting with a DOM object
 *
 * @author Matt Wyatt
 * @author Josh Vote
 */
public class DOMUtil {
    /**
     * Given a String containing XML, parse it and return a DOM object representation (that is namespace aware).
     * @param xmlString A string containing valid XML
     * @return
     */
    public static Document buildDomFromString(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString.toString()));
        Document doc = builder.parse(inputSource);

        return doc;
    }

    /**
     * Given a Stream containing XML, parse it and return a DOM object representation (that is namespace aware).
     * @param xmlString A string containing valid XML
     * @return
     */
    public static Document buildDomFromStream(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(stream);

        return doc;
    }

    /**
     * Given a DOM (sub)tree generate a string representation with no formatting
     * @param node The node to generate the XML for
     * @param omitXmlDeclaration Whether the <?xml?> header should be omitted
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
        t.transform(new DOMSource(node),sr);

        return outText.toString();
    }

    /**
     * Compiles the specified XPath (as a string) into an XPathExpression.
     * @param xPathStr A string representing a valid XPath expression
     * @param nc The namespace that the xPathStr is referencing
     * @return
     * @throws XPathExpressionException
     */
    public static XPathExpression compileXPathExpr(String xPathStr, NamespaceContext nc) throws XPathExpressionException {
        //Force the usage of the Saxon XPath library
        XPathFactory factory = new XPathFactoryImpl();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(nc);
        return xPath.compile(xPathStr);
    }
}
