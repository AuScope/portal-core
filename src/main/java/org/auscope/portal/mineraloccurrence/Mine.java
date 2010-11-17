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



/**
 * This class is a wrapper for er:Mine XML feature  
 * 
 * @version $Id$
 */
public class Mine {
    
    protected final Log log = LogFactory.getLog(getClass());
 // private Document mineDocument;
    private Node mineNode;
    private XPath xPath;

    public Mine(Node mineNode) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        this.mineNode = mineNode;

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());
    }
    
    
    /*
     * Find a prefered name or the first name you have in the list.
     */
    public String getMineNamePreffered() throws XPathExpressionException {
        XPathExpression expr = xPath.compile("er:mineName/er:MineName/er:isPreferred");
        NodeList prefferedNodes = (NodeList)expr.evaluate(mineNode, XPathConstants.NODESET);

        expr = xPath.compile("er:mineName/er:MineName/er:mineName");
        NodeList nameNodes = (NodeList)expr.evaluate(mineNode, XPathConstants.NODESET);

        for (int i = 0; i < prefferedNodes.getLength(); i++) {
            if(prefferedNodes.item(i).getTextContent().equals("true"))
                return nameNodes.item(i).getTextContent();
        }

        return nameNodes.item(0).getTextContent();
    }

    public String getMineNameURI() {

        try {
            XPathExpression expr = xPath.compile("er:occurrence/er:MiningFeatureOccurrence/er:specification");
            Node result = (Node)expr.evaluate(mineNode, XPathConstants.NODE);
            return result.getAttributes().getNamedItem("xlink:href").getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public List<String> getRelatedActivities() {
        List<String> result = new ArrayList<String>();
        try {
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
                    //log.debug((i+1) + " : " + s.substring(j));
            }

            return result;
        } catch (Exception e) {
            return result;
        }
    }

    
    public List<String> getRelatedMiningActivities() {
        List<String> result = new ArrayList<String>();
        try {
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
