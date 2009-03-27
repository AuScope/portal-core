package org.auscope.portal.server.web.mineraloccurrence;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.util.Collection;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 4:53:18 PM
 */
public class MineralOccurrencesResponseHandler {

    public static Collection<Mine> getMines(String mineResponse) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document mineDocument = builder.parse(new ByteArrayInputStream(mineResponse.getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/wfs:FeatureCollection/gml:featureMember/mo:Mine");
        NodeList mineNodes = (NodeList)expr.evaluate(mineDocument, XPathConstants.NODESET);
        ArrayList<Mine> mines = new ArrayList<Mine>();

        for(int i=0; i < mineNodes.getLength(); i++) {
            mines.add(new Mine(mineNodes.item(i)));
        }

        return mines;
    }

    public static Collection<Commodity> getCommodities(String commodityResponse) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document commodityDocument = builder.parse(new ByteArrayInputStream(commodityResponse.getBytes("UTF-8")));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("/wfs:FeatureCollection/gml:featureMember/mo:Commodity");
        NodeList commodityNodes = (NodeList)expr.evaluate(commodityDocument, XPathConstants.NODESET);
        ArrayList<Commodity> commodities = new ArrayList<Commodity>();

        for(int i=0; i < commodityNodes.getLength(); i++) {
            commodities.add(new Commodity(commodityNodes.item(i)));
        }

        return commodities;
    }

    public static Collection<MiningActivity> getMiningActivities(String miningActivityResponse) {

        return null;
    }
}
