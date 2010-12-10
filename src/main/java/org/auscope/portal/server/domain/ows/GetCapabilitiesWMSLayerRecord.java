package org.auscope.portal.server.domain.ows;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    private CSWGeographicBoundingBox bbox;
    private String[] childLayerSRS;
    
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
        
        String latLonBoundingBox = "LatLonBoundingBox";
        tempNode = (Node)xPath.evaluate(latLonBoundingBox, node, XPathConstants.NODE);
        if (tempNode != null) {
        	String minx = (String)xPath.evaluate("@minx", tempNode, XPathConstants.STRING);
        	String maxx = (String)xPath.evaluate("@maxx", tempNode, XPathConstants.STRING);
        	String miny = (String)xPath.evaluate("@miny", tempNode, XPathConstants.STRING);
        	String maxy = (String)xPath.evaluate("@maxy", tempNode, XPathConstants.STRING);
        	
        	//Attempt to parse our bounding box
        	try {
	        	bbox = new CSWGeographicBoundingBox(Double.parseDouble(minx), 
	        			Double.parseDouble(maxx), 
	        			Double.parseDouble(miny), 
	        			Double.parseDouble(maxy));
        	} catch (Exception ex) { }
        }
        
        String layerSRSExpression = "SRS";
        NodeList nodes = (NodeList)xPath.evaluate( layerSRSExpression
                , node
                , XPathConstants.NODESET );
		childLayerSRS = new String[nodes.getLength()];
		for(int i =0; i< nodes.getLength(); i++){
			Node childSRSNode = nodes.item(i);
			childLayerSRS[i] = childSRSNode != null ? childSRSNode.getTextContent() : "";
		}
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
    
    public CSWGeographicBoundingBox getBoundingBox() {
    	return bbox;
    }
    
    public String[] getChildLayerSRS() throws XPathExpressionException {
        return childLayerSRS;
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
