package org.auscope.portal.mineraloccurrence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

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
 * Time: 3:27:26 PM
 */
public class TestMineralOccurrence {

    private static MineralOccurrence validMineralOccurrence;
    private static MineralOccurrence invalidMineralOccurrence;

    @BeforeClass
    public static void setup() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        //create updateCSWRecords valid mineral occurrence
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineralOccurrenceDocument = builder.parse(new ByteArrayInputStream(Util.loadXML("src/test/resources/mineralOccurrenceNodeValid.xml").getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/er:MineralOccurrence");
        Node mineralOccurrenceNode = (Node)expr.evaluate(mineralOccurrenceDocument, XPathConstants.NODE);
        validMineralOccurrence = new MineralOccurrence(mineralOccurrenceNode);

        //create an invalid mineral occurrence
        DocumentBuilderFactory domFactory2 = DocumentBuilderFactory.newInstance();
        domFactory2.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder2 = domFactory2.newDocumentBuilder();
        Document mineralOccurrenceDocument2 = builder2.parse(new ByteArrayInputStream(Util.loadXML("src/test/resources/mineralOccurrenceNodeInvalid.xml").getBytes("UTF-8")));

        XPathFactory factory2 = XPathFactory.newInstance();
        XPath xPath2 = factory2.newXPath();
        xPath2.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr2 = xPath2.compile("/er:MineralOccurrence");
        Node mineralOccurrenceNode2 = (Node)expr2.evaluate(mineralOccurrenceDocument2, XPathConstants.NODE);
        invalidMineralOccurrence = new MineralOccurrence(mineralOccurrenceNode2);
    }

    @Test
    public void testGetURNValid() throws XPathExpressionException {
        Assert.assertEquals("URN is: urn:cgi:feature:PIRSA:MineralOccurrence:394deposit", "urn:cgi:feature:PIRSA:MineralOccurrence:394deposit", validMineralOccurrence.getURN());
    }

    @Test
    public void testGetTypeValid() throws XPathExpressionException {
        Assert.assertEquals("Type is: ore deposit", "ore deposit", validMineralOccurrence.getType());
    }

    @Test
    public void testGetMineralDepositGroupValid() throws XPathExpressionException {
        Assert.assertEquals("Mineral deposit group is: Hydrothermal: precipitation of ore and gangue from " +
                "watery fluids of diverse origin, temperature range 50-7000C, generally below 4000C, " +
                "pressure 1-3 kbar",
                "Hydrothermal: precipitation of ore and gangue from watery fluids of diverse origin, " +
                "temperature range 50-7000C, generally below 4000C, pressure 1-3 kbar",
                validMineralOccurrence.getMineralDepositGroup());
    }
    
    @Test
    public void testGetCommodityDescriptionURNsValid() {
        ArrayList<String> URNs = new ArrayList<String>();
        URNs.add("urn:cgi:feature:PIRSA:MineralCommodity:394deposit:Au");
        
        Assert.assertEquals(
                "Commodity Description URN is: urn:cgi:feature:PIRSA:MineralCommodity:394deposit:Au",
                URNs,
                validMineralOccurrence.getCommodityDescriptionURNs());
    }

    @Test
    public void testGetURNInvalid() throws XPathExpressionException {
        Assert.assertEquals("URN is: empty string", "", invalidMineralOccurrence.getURN());
    }

    @Test
    public void testGetTypeInvalid() throws XPathExpressionException {
        Assert.assertEquals("Type is: empty string", "", invalidMineralOccurrence.getType());
    }

    @Test
    public void testGetMineralDepositGroupInvalid() throws XPathExpressionException {
        Assert.assertEquals("",
                invalidMineralOccurrence.getMineralDepositGroup());
    }

    @Test
    public void testGetCommodityDescriptionURNsInvalid() {
        Assert.assertEquals(
                "Commodity Description URN is: an empty list",
                new ArrayList<String>(),
                invalidMineralOccurrence.getCommodityDescriptionURNs());
    }
}
