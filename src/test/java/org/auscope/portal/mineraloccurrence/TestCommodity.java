package org.auscope.portal.mineraloccurrence;

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

    private static Commodity validCommodity;
    private static Commodity invalidCommodity;

    @BeforeClass
    public static void setup() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        //create updateCSWRecords valid commodity
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(Util.loadXML("src/test/resources/commodityNodeValid.xml").getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/er:Commodity");
        Node commodityNode = (Node)expr.evaluate(mineDocument, XPathConstants.NODE);
        validCommodity = new Commodity(commodityNode);

        //create an invalid commodity
        DocumentBuilderFactory domFactory2 = DocumentBuilderFactory.newInstance();
        domFactory2.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder2 = domFactory2.newDocumentBuilder();
        Document mineDocument2 = builder2.parse(new ByteArrayInputStream(Util.loadXML("src/test/resources/commodityNodeInvalid.xml").getBytes("UTF-8")));

        XPathFactory factory2 = XPathFactory.newInstance();
        XPath xPath2 = factory2.newXPath();
        xPath2.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr2 = xPath2.compile("/er:Commodity");
        Node commodityNode2 = (Node)expr2.evaluate(mineDocument2, XPathConstants.NODE);
        invalidCommodity = new Commodity(commodityNode2);

    }

    @Test
    public void testGetCommodityNameValid() throws XPathExpressionException {
        Assert.assertEquals("Commodity name is: Gold", "Gold", validCommodity.getCommodityName());
    }

    @Test
    public void testGetSourcevalid() throws XPathExpressionException {
        Assert.assertEquals("URI is: urn:cgi:feature:GSV:MineralOccurrence:361169", "urn:cgi:feature:GSV:MineralOccurrence:361169", validCommodity.getSource());
    }

    @Test
    public void testGetCommodityImportanceValid() throws XPathExpressionException {
        Assert.assertEquals("Commodity importance is: major", "major", validCommodity.getCommodityImportance());
    }

    @Test
    public void testGetCommodityNameInvalid() throws XPathExpressionException {
        Assert.assertEquals("Commodity name is: empty string", "", invalidCommodity.getCommodityName());
    }

    @Test
    public void testGetSourceInvalid() throws XPathExpressionException {
        Assert.assertEquals("URI is: empty string", "", invalidCommodity.getSource());
    }

    @Test
    public void testGetCommodityImportanceInvalid() throws XPathExpressionException {
        Assert.assertEquals("Commodity importance is: empty string", "", invalidCommodity.getCommodityImportance());
    }
}
