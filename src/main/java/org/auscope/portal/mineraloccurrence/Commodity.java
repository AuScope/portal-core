package org.auscope.portal.mineraloccurrence;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * User: Michael Stegherr
 * Date: 27/03/2009
 * Time: 5:13:06 PM
 */
public class Commodity {
    private Node commodityNode;
    private XPath xPath;

    public Commodity(Node commodityNode) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        this.commodityNode = commodityNode;

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());
    }

    public String getCommodityName() throws XPathExpressionException {
        
        try {
            XPathExpression expr = xPath.compile("er:commodityName");
            Node result = (Node)expr.evaluate(commodityNode, XPathConstants.NODE);
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public String getMineralOccurrenceURI() {

        try {
            XPathExpression expr = xPath.compile("er:source");
            Node result = (Node)expr.evaluate(commodityNode, XPathConstants.NODE);
            return result.getAttributes().getNamedItem("xlink:href").getTextContent();
        } catch (Exception e) {
            return "";
        }
    }
        
    public String getCommodityImportance() throws XPathExpressionException {
        
        try {
            XPathExpression expr = xPath.compile("er:commodityImportance");
            Node result = (Node)expr.evaluate(commodityNode, XPathConstants.NODE);
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }
}