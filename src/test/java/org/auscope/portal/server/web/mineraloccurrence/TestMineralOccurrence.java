package org.auscope.portal.server.web.mineraloccurrence;

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

    private static MineralOccurrence mineralOccurrence;
    private static XPath xPath;

    @BeforeClass
    public static void setup() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineralOccurrenceDocument = builder.parse(new ByteArrayInputStream(Util.loadXML("src/test/resources/mineralOccurrenceNode.xml").getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/mo:MineralOccurrence");
        Node mineralOccurrenceNode = (Node)expr.evaluate(mineralOccurrenceDocument, XPathConstants.NODE);
        mineralOccurrence = new MineralOccurrence(mineralOccurrenceNode);

    }

    @Test
    public void testGetURN() throws XPathExpressionException {
        Assert.assertEquals("URN is: urn:cgi:feature:PIRSA:MineralOccurrence:394deposit", "urn:cgi:feature:PIRSA:MineralOccurrence:394deposit", mineralOccurrence.getURN());
    }

    @Test
    public void testGetType() throws XPathExpressionException {
        Assert.assertEquals("Type is: ore deposit", "ore deposit", mineralOccurrence.getType());
    }

    @Test
    public void testGetMineralDepositGroup() throws XPathExpressionException {
        Assert.assertEquals("Mineral deposit group is: Hydrothermal: precipitation of ore and gangue from " +
                "watery fluids of diverse origin, temperature range 50-7000C, generally below 4000C, " +
                "pressure 1-3 kbar",
                "Hydrothermal: precipitation of ore and gangue from watery fluids of diverse origin, " +
                "temperature range 50-7000C, generally below 4000C, pressure 1-3 kbar",
                mineralOccurrence.getMineralDepositGroup());
    }
    
    @Test
    public void testGetCommodityDescriptionURNs() {
        ArrayList<String> URNs = new ArrayList<String>();
        URNs.add("urn:cgi:feature:PIRSA:MineralCommodity:394deposit:Au");
        
        Assert.assertEquals(
                "Commodity Description URN is: urn:cgi:feature:PIRSA:MineralCommodity:394deposit:Au",
                URNs,
                mineralOccurrence.getCommodityDescriptionURNs());
    }
}
