package org.auscope.portal.mineraloccurrence;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 4:52:06 PM
 */
public class Mine {
    //private Document mineDocument;
    private Node mineNode;
    private XPath xPath;

    public Mine(Node mineNode) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        this.mineNode = mineNode;

        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();
        xPath.setNamespaceContext(new MineralOccurrenceNamespaceContext());
    }

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
        
}
