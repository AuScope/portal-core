package org.auscope.portal.mineraloccurrence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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



// TODO: Auto-generated Javadoc
/**
 * This class is a wrapper for er:Mine XML feature.
 *
 * @version $Id$
 */
public class Mine {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

    /** The mine node. */
    private Node mineNode;

    /**
     * Instantiates a new mine.
     *
     * @param mineNode the mine node
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws SAXException the sAX exception
     * @throws ParserConfigurationException the parser configuration exception
     * @throws XPathExpressionException the x path expression exception
     */
    public Mine(Node mineNode) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        this.mineNode = mineNode;
    }


    /**
     * Gets the mine name preffered or first name in the list..
     *
     * @return the mine name preffered
     * @throws XPathExpressionException the x path expression exception
     */
    public String getMineNamePreffered() throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

        XPathExpression expr = xPath.compile("er:mineName/er:MineName/er:isPreferred");
        NodeList prefferedNodes = (NodeList) expr.evaluate(mineNode, XPathConstants.NODESET);

        expr = xPath.compile("er:mineName/er:MineName/er:mineName");
        NodeList nameNodes = (NodeList) expr.evaluate(mineNode, XPathConstants.NODESET);

        for (int i = 0; i < prefferedNodes.getLength(); i++) {
            if (prefferedNodes.item(i).getTextContent().equals("true"))
                return nameNodes.item(i).getTextContent();
        }

        return nameNodes.item(0).getTextContent();
    }

    /**
     * Gets the mine name uri.
     *
     * @return the mine name uri
     */
    public String getMineNameURI() {

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

            XPathExpression expr = xPath.compile("er:occurrence/er:MiningFeatureOccurrence/er:specification");
            Node result = (Node)expr.evaluate(mineNode, XPathConstants.NODE);
            return result.getAttributes().getNamedItem("xlink:href").getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets the related activities.
     *
     * @return the related activities
     */
    public List<String> getRelatedActivities() {
        List<String> result = new ArrayList<String>();
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

            XPathExpression expr = xPath.compile("er:relatedActivity/er:MiningActivity/er:occurrence/@xlink:href");
            Object relatedNodes = expr.evaluate(mineNode, XPathConstants.NODESET);
            NodeList nodes = (NodeList) relatedNodes;
            String search  = "urn:cgi";
            String s;
            int j;

            for (int i = 0; i < nodes.getLength(); i++) {
                s = nodes.item(i).getNodeValue();
                j = s.indexOf(search);
                if (j!=-1)
                    result.add(s.substring(j));
            }

            return result;
        } catch (Exception e) {
            return result;
        }
    }


    /**
     * Gets the related mining activities.
     *
     * @return the related mining activities
     */
    public List<String> getRelatedMiningActivities() {
        List<String> result = new ArrayList<String>();
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());

            // Deal with local pointer reference eg. xlink:href="#er.mine.361023
            XPathExpression expr = xPath.compile("er:occurrence/er:MiningFeatureOccurrence/er:specification[starts-with(@xlink:href,'#']");
            Object relatedNodes = expr.evaluate(mineNode, XPathConstants.NODESET);
            NodeList nodes = (NodeList) relatedNodes;

            for (int i = 0; i < nodes.getLength(); i++) {
                log.debug(i + " : " + nodes.item(i).getNodeValue());
                result.add(nodes.item(i).getNodeValue());
            }

            return result;
        } catch (Exception e) {
            return result;
        }
    }

}
