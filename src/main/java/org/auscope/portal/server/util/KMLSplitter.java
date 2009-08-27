package org.auscope.portal.server.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 *
 * This class takes a kml document and splits each of the <Placemark> tags into a separate kml document
 *
 * User: Mathew Wyatt
 * Date: 10/04/2009
 * Time: 2:14:40 PM
 */
public class KMLSplitter {
    /**
     * The xml docuemnt
     */
    private Document kmlDocument;

    /**
     * Takes the kml document as a string
     * @param kmlString
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public KMLSplitter(String kmlString) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        this.kmlDocument = builder.parse(new ByteArrayInputStream(kmlString.getBytes()));
    }

    /**
     * Takes the kml document as an xml document
     * @param kmlDocument
     */
    public KMLSplitter(Document kmlDocument) {
        this.kmlDocument = kmlDocument;
    }

    /**
     * Returns a collection of kml document strings created based on the <Placemark> tag
     * @return
     */
    public String[] getKMLDocuments() throws XPathExpressionException, ParserConfigurationException, TransformerException {

        //get the root element
        Element rootElement = this.kmlDocument.getDocumentElement();

        //get all of the <Placemark> elements
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        XPathExpression expr = xPath.compile("//*[name() = 'Placemark']");
        NodeList placemarkNodes = (NodeList)expr.evaluate(kmlDocument, XPathConstants.NODESET);

        //create updateCSWRecords new array based on the number of placemarks
        String[] kmlStrings = new String[placemarkNodes.getLength()];

        //iterated through and create new documents
        for(int i=0; i<placemarkNodes.getLength(); i++) {

            //create updateCSWRecords new document
            Document newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            //clone the root node, dont clone its children
            Node newDocRoot = rootElement.cloneNode(false);

            //append this placemark to it
            newDocRoot.appendChild(placemarkNodes.item(i));

            //add the root node to this document
            Node importedRoot = newDoc.importNode(newDocRoot, true);
            newDoc.appendChild(importedRoot);

            //the strign writer to contain the newly formed kml
            StringWriter sw = new StringWriter();

            //transform the new document to updateCSWRecords string
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(newDoc), new StreamResult(sw));

            //add the string to the array
            kmlStrings[i] = sw.toString();
        }

        //send it off!
        return kmlStrings;
    }
}
