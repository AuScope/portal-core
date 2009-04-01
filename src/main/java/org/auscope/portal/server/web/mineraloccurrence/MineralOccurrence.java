package org.auscope.portal.server.web.mineraloccurrence;

import java.io.IOException;

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

    public String getURN() throws XPathExpressionException {
        XPathExpression expr = xPath.compile("gml:name");
        NodeList nameNodes = (NodeList)expr.evaluate(mineralOccurrenceNode, XPathConstants.NODESET);

        // TODO room for refactoring: is there a better way to get the URN than a string compare?
        for (int i = 0; i < nameNodes.getLength(); i++) {
            if(nameNodes.item(i).getTextContent().startsWith("urn"))
                return nameNodes.item(i).getTextContent();
        }

        // TODO what to do if there is no URN?
        return nameNodes.item(0).getTextContent();
    }

    public String getType() {

        try {
            XPathExpression expr = xPath.compile("mo:type");
            Node result = (Node)expr.evaluate(mineralOccurrenceNode, XPathConstants.NODE);
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public String getMineralDepositGroup() {

        try {
            XPathExpression expr =
                xPath.compile("mo:classification/mo:MineralDepositModel/mo:mineralDepositGroup");
            Node result = (Node)expr.evaluate(mineralOccurrenceNode, XPathConstants.NODE);
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }
}
