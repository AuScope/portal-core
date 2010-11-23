package org.auscope.portal.csw;

import java.io.Serializable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
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
    
    private static final XPathExpression westBoundLongitudeExpr;
    private static final XPathExpression eastBoundLongitudeExpr;
    private static final XPathExpression northBoundLatitudeExpr;
    private static final XPathExpression southBoundLatitudeExpr;

    static {
        westBoundLongitudeExpr = CSWXPathUtil.attemptCompileXpathExpr("gmd:westBoundLongitude/gco:Decimal");
        eastBoundLongitudeExpr = CSWXPathUtil.attemptCompileXpathExpr("gmd:eastBoundLongitude/gco:Decimal");
        northBoundLatitudeExpr = CSWXPathUtil.attemptCompileXpathExpr("gmd:northBoundLatitude/gco:Decimal");
        southBoundLatitudeExpr = CSWXPathUtil.attemptCompileXpathExpr("gmd:southBoundLatitude/gco:Decimal");
    }

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
     *
     * @param node should represent a gmd:EX_GeographicBoundingBox node
     * @return
     */
    public static CSWGeographicBoundingBox fromGeographicBoundingBoxNode(Node node) throws Exception {

        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();

        String tempString = (String) westBoundLongitudeExpr.evaluate(node, XPathConstants.STRING);
        if (tempString.isEmpty())
            throw new Exception("westBoundLongitude DNE");
        bbox.westBoundLongitude = Double.parseDouble(tempString);

        tempString = (String) eastBoundLongitudeExpr.evaluate(node, XPathConstants.STRING);
        if (tempString.isEmpty())
            throw new Exception("eastBoundLongitude DNE");
        bbox.eastBoundLongitude = Double.parseDouble(tempString);

        tempString = (String) southBoundLatitudeExpr.evaluate(node, XPathConstants.STRING);
        if (tempString.isEmpty())
            throw new Exception("southBoundLatitude DNE");
        bbox.southBoundLatitude = Double.parseDouble(tempString);

        tempString = (String) northBoundLatitudeExpr.evaluate(node, XPathConstants.STRING);
        if (tempString.isEmpty())
            throw new Exception("northBoundLatitude DNE");
        bbox.northBoundLatitude = Double.parseDouble(tempString);

        return bbox;
    }
}
