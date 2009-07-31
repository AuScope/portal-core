package org.auscope.portal.server.util;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.auscope.portal.Util;

import javax.xml.xpath.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * User: Mathew Wyatt
 * Date: 10/04/2009
 * Time: 4:47:40 PM
 */
public class TestKMLSplitter {

    private static XPath xPath;
    private static KMLSplitter kmlSplitter;

    @BeforeClass
    public static void setup() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document kmlDocument = builder.parse(new ByteArrayInputStream(Util.loadXML("src/test/resources/smallSampleKml.kml").getBytes("UTF-8")));
        kmlSplitter = new KMLSplitter(kmlDocument);

        //XPathFactory factory = XPathFactory.newInstance();
        //xPath = factory.newXPath();
        //xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        //XPathExpression expr = xPath.compile("/mo:Commodity");
        //Node commodityNode = (Node)expr.evaluate(mineDocument, XPathConstants.NODE);
    }


    @Test
    public void testSplit() throws TransformerException, XPathExpressionException, ParserConfigurationException {
        String[] kmlDocuments = kmlSplitter.getKMLDocuments();
        Assert.assertEquals("There should be two documents returned", 2, kmlDocuments.length);

        for(String s : kmlDocuments)
            System.out.println(s);

        //TODO: this should outout some stuff but doesnt, the kmlsplitter class is wrecking the document, so fix this bug
        for(String s : kmlSplitter.getKMLDocuments()) {
            System.out.println("1: " +s);
        }
    }
}
