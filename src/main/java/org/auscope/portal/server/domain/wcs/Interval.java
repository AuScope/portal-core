package org.auscope.portal.server.domain.wcs;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;

/**
 * Represents a <wcs:interval> element from a WCS DescribeCoverage response
 * @author vot002
 */
public class Interval implements ValueEnumType {

    private String type;
    
    private Double min;
    private Double max;
    private Double resolution;
    
    public Interval(Node node, XPath xPath) throws Exception {
        type = node.getLocalName();
        
        Node tempNode = (Node) xPath.evaluate("wcs:min", node, XPathConstants.NODE);
        if (tempNode != null) 
            min = new Double(tempNode.getTextContent());
        
        tempNode = (Node) xPath.evaluate("wcs:max", node, XPathConstants.NODE);
        if (tempNode != null) 
            max = new Double(tempNode.getTextContent());
        
        tempNode = (Node) xPath.evaluate("wcs:resolution", node, XPathConstants.NODE);
        if (tempNode != null) 
            resolution = new Double(tempNode.getTextContent());
    }
    
    public String getType() {
        return type;
    }

    /** 
     * Represents the minimum value on this interval (can be null)
     * @return
     */
    public Double getMin() {
        return min;
    }

    /**
     * Represents the maximum value on this interval (can be null)
     * @return
     */
    public Double getMax() {
        return max;
    }

    /**
     * Represents the resolution of this interval (can be null)
     * @return
     */
    public Double getResolution() {
        return resolution;
    }
    
}
