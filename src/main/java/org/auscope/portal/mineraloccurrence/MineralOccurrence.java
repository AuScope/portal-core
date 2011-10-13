package org.auscope.portal.mineraloccurrence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * User: Michael Stegherr
 * Date: 30/03/2009
 * Time: 3:27:26 PM
 */
public class MineralOccurrence {
    private Node mineralOccurrenceNode;
    private XPath xPath;

    public MineralOccurrence(Node mineralOccurrenceNode) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        this.mineralOccurrenceNode = mineralOccurrenceNode;

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());
    }

    public String getURN() {
        try {
            XPathExpression expr = xPath.compile("gml:name");
            NodeList nameNodes = (NodeList)expr.evaluate(mineralOccurrenceNode, XPathConstants.NODESET);

            // TODO is there updateCSWRecords better way to get the URN than updateCSWRecords string compare?
            for (int i = 0; i < nameNodes.getLength(); i++)
                if(nameNodes.item(i).getTextContent().startsWith("urn"))
                    return nameNodes.item(i).getTextContent();
        } catch (XPathExpressionException e) {
            return "";
        }

        // no URN found
        return "";
    }

    public String getType() {

        try {
            XPathExpression expr = xPath.compile("er:type");
            Node result = (Node)expr.evaluate(mineralOccurrenceNode, XPathConstants.NODE);
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public String getMineralDepositGroup() {

        try {
            XPathExpression expr =
                xPath.compile("er:classification/er:MineralDepositModel/er:mineralDepositGroup");
            Node result = (Node)expr.evaluate(mineralOccurrenceNode, XPathConstants.NODE);
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public Collection<String> getCommodityDescriptionURNs() {

        try {
            XPathExpression expr = xPath.compile("er:commodityDescription");
            NodeList commodityNodes = (NodeList)expr.evaluate(mineralOccurrenceNode, XPathConstants.NODESET);

            ArrayList<String> commodityDescriptionURNs = new ArrayList<String>();

            for(int i=0; i < commodityNodes.getLength(); i++) {
                String URN =
                    commodityNodes.item(i).getAttributes().getNamedItem("xlink:href").getTextContent();

                commodityDescriptionURNs.add(URN);
            }
            return commodityDescriptionURNs;
        } catch (Exception e) {
            //return an empty list
            return new ArrayList<String>();
        }
    }
}
