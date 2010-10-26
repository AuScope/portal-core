package org.auscope.portal.csw;

import java.io.Serializable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

/**
 * A simple class representing an gmd:EX_GeographicBoundingBox
 * @author VOT002
 *
 */
public class CSWGeographicBoundingBox implements Serializable, CSWGeographicElement{
    double westBoundLongitude;
    double eastBoundLongitude;
    double southBoundLatitude;
    double northBoundLatitude;
    
    public CSWGeographicBoundingBox() {
        
    }
    
    public CSWGeographicBoundingBox(double westBoundLongitude,
            double eastBoundLongitude, double southBoundLatitude,
            double northBoundLatitude) {
        this.westBoundLongitude = westBoundLongitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.southBoundLatitude = southBoundLatitude;
        this.northBoundLatitude = northBoundLatitude;
    }
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }
    public void setWestBoundLongitude(double westBoundLongitude) {
        this.westBoundLongitude = westBoundLongitude;
    }
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }
    public void setEastBoundLongitude(double eastBoundLongitude) {
        this.eastBoundLongitude = eastBoundLongitude;
    }
    public double getSouthBoundLatitude() {
        return southBoundLatitude;
    }
    public void setSouthBoundLatitude(double southBoundLatitude) {
        this.southBoundLatitude = southBoundLatitude;
    }
    public double getNorthBoundLatitude() {
        return northBoundLatitude;
    }
    public void setNorthBoundLatitude(double northBoundLatitude) {
        this.northBoundLatitude = northBoundLatitude;
    }
    
    /**
     * Creates a bounding box by parsing a gmd:EX_GeographicBoundingBox node and its children
     * (Will use an instance of CSWNamespaceContext as the namespace to use in XPath extraction)
     * @param node should represent a gmd:EX_GeographicBoundingBox node
     * @return
     */
    public static CSWGeographicBoundingBox fromGeographicBoundingBoxNode(Node node) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new CSWNamespaceContext());
        
        return fromGeographicBoundingBoxNode(node, xPath);
    }
    
    /**
     * Creates a bounding box by parsing a gmd:EX_GeographicBoundingBox node and its children
     * 
     * @param node should represent a gmd:EX_GeographicBoundingBox node
     * @param xpath will be used to extract elements from node
     * @return
     */
    public static CSWGeographicBoundingBox fromGeographicBoundingBoxNode(Node node, XPath xPath) throws Exception {
        
        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();
        
        Node tempNode = (Node)xPath.evaluate("gmd:westBoundLongitude/gco:Decimal", node, XPathConstants.NODE);
        if (tempNode == null)
            throw new Exception("westBoundLongitude DNE");
        bbox.westBoundLongitude = Double.parseDouble(tempNode.getTextContent());
        
        tempNode = (Node)xPath.evaluate("gmd:eastBoundLongitude/gco:Decimal", node, XPathConstants.NODE);
        if (tempNode == null)
            throw new Exception("eastBoundLongitude DNE");
        bbox.eastBoundLongitude = Double.parseDouble(tempNode.getTextContent());
        
        tempNode = (Node)xPath.evaluate("gmd:southBoundLatitude/gco:Decimal", node, XPathConstants.NODE);
        if (tempNode == null)
            throw new Exception("southBoundLatitude DNE");
        bbox.southBoundLatitude = Double.parseDouble(tempNode.getTextContent());
        
        tempNode = (Node)xPath.evaluate("gmd:northBoundLatitude/gco:Decimal", node, XPathConstants.NODE);
        if (tempNode == null)
            throw new Exception("northBoundLatitude DNE");
        bbox.northBoundLatitude = Double.parseDouble(tempNode.getTextContent());
        
        return bbox;
    }
}
