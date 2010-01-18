package org.auscope.portal.vocabs;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.auscope.portal.Util;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * User: Michael Stegherr
 * Date: 07/09/2009
 * Time: 5:18:48 AM
 */
public class TestConcept {

    private static Concept concept;
    private static XPath xPath;

    @BeforeClass
    public static void setup() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document conceptDocument =
            builder.parse(new ByteArrayInputStream(
                    Util.loadXML("src/test/resources/conceptNode.xml").getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new VocabularyServiceNamespaceContext());

        XPathExpression expr = xPath.compile("/skos:Concept");
        Node conceptNode = (Node)expr.evaluate(conceptDocument, XPathConstants.NODE);
        concept = new Concept(conceptNode);

    }

    @Test
    public void testGetPreferredLabel() throws XPathExpressionException {
        Assert.assertEquals(
                "Preferred label is: Rhyolite - road base",
                "Rhyolite - road base",
                concept.getPreferredLabel());
    }

    @Test
    public void testGetSchemeUrn() throws XPathExpressionException {
        Assert.assertEquals(
                "Scheme URN is: urn:cgi:classifierScheme:PIRSA:commodity",
                "urn:cgi:classifierScheme:PIRSA:commodity",
                concept.getSchemeUrn());
    }

    @Test
    public void testGetConceptUrn() throws XPathExpressionException {
        Assert.assertEquals(
                "Concept URN is: urn:cgi:classifier:PIRSA:commodity:RHYRB",
                "urn:cgi:classifier:PIRSA:commodity:RHYRB",
                concept.getConceptUrn());
    }
}
