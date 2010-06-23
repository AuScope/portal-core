package org.auscope.portal.server.domain.ows;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

/**
 * This class represents WMS Layer node within GetCapabilites WMS response.
 * 
 * @author JarekSanders
 * @version $Id$
 *
 */
public class GetCapabilitiesWMSLayerRecord {

    // ----------------------------------------------------- Instance variables
    private String name;
    private String title;
    private String description;

    // ----------------------------------------------------------- Constructors
    public GetCapabilitiesWMSLayerRecord(Node node) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        
        String layerNameExpression = "Name";
        Node tempNode = (Node)xPath.evaluate(layerNameExpression, node, XPathConstants.NODE);
        name = tempNode != null ? tempNode.getTextContent() : "";

        String layerTitleExpression = "Title";
        tempNode = (Node)xPath.evaluate(layerTitleExpression, node, XPathConstants.NODE);
        title = tempNode != null ? tempNode.getTextContent() : "";
        
        String layerAbstractExpression = "Abstract";
        tempNode = (Node)xPath.evaluate(layerAbstractExpression, node, XPathConstants.NODE);
        description = tempNode != null ? tempNode.getTextContent() : "";
        
    }
    
    
    // ------------------------------------------ Attribute Setters and Getters
    
    public String getName() throws XPathExpressionException {
        return name;
    }

    public String getTitle() throws XPathExpressionException {
        return title;
    }
    
    public String getAbstract() throws XPathExpressionException {
        return description;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append(",");
        buf.append(title);
        buf.append(",");
        buf.append(description);
        buf.append(",");
        return buf.toString(); 
    }    
    
}
