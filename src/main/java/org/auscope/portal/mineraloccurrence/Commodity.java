package org.auscope.portal.mineraloccurrence;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * User: Michael Stegherr
 * Date: 27/03/2009
 * @version $Id$
 */
public class Commodity {
    private final Log log = LogFactory.getLog(getClass());
    private Node commodityNode;

    public Commodity(Node commodityNode) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        this.commodityNode = commodityNode;
    }

    public String getName() {
        String result = "";
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

            XPathExpression expr = xPath.compile("gml:name");
            NodeList list = (NodeList)expr.evaluate(commodityNode, XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getAttributes().getNamedItem("codeSpace").getNodeValue().compareTo("http://www.ietf.org/rfc/rfc2141") == 0) {
                    result = list.item(i).getTextContent();
                }
            }
            log.trace("result in commodity is :" + result);
            return result;
        } catch (Exception e) {
            return "";
        }
    }

    public String getCommodityName() throws XPathExpressionException {

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

            XPathExpression expr = xPath.compile("er:commodityName");
            Node result = (Node)expr.evaluate(commodityNode, XPathConstants.NODE);
            log.trace("commodity class to get text commodity name "+ result.getTextContent() +"was the content");
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public String getCommodityImportance() throws XPathExpressionException {

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

            XPathExpression expr = xPath.compile("er:commodityImportance");
            Node result = (Node)expr.evaluate(commodityNode, XPathConstants.NODE);
            log.trace("commodity class to getCommodityImportance"+ result.getTextContent() +"was the content");
            return result.getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public String getSource() {

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

            XPathExpression expr = xPath.compile("er:source");
            Node result = (Node)expr.evaluate(commodityNode, XPathConstants.NODE);
            String search  = "urn:cgi";
            String s = result.getAttributes().getNamedItem("xlink:href").getTextContent();
            log.trace("commodity class to getSource"+ s.substring(s.indexOf(search)) +"was the content");
            return s.substring(s.indexOf(search));
        } catch (Exception e) {
            return "";
        }
    }
}
