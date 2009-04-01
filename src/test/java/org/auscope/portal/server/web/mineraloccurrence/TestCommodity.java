package org.auscope.portal.server.web.mineraloccurrence;

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
 * Date: 30/03/2009
 * Time: 3:14:31 PM
 */
public class TestCommodity {

    private static Commodity commodity;
    private static XPath xPath;

    @BeforeClass
    public static void setup() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(Util.loadXML("src/test/resources/commodityNode.xml").getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/mo:Commodity");
        Node commodityNode = (Node)expr.evaluate(mineDocument, XPathConstants.NODE);
        commodity = new Commodity(commodityNode);

    }

    @Test
    public void testGetCommodityName() throws XPathExpressionException {
        Assert.assertEquals("Commodity name is: Gold", "Gold", commodity.getCommodityName());
    }

    @Test
    public void testGetMineralOccurrenceURI() throws XPathExpressionException {
        Assert.assertEquals("URI is: urn:cgi:feature:GSV:MineralOccurrence:361169", "urn:cgi:feature:GSV:MineralOccurrence:361169", commodity.getMineralOccurrenceURI());
    }

    @Test
    public void testGetCommodityImportance() throws XPathExpressionException {
        Assert.assertEquals("Commodity importance is: major", "major", commodity.getCommodityImportance());
    }
}
