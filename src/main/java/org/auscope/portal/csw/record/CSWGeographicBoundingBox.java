package org.auscope.portal.csw.record;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.server.util.DOMUtil;
import org.w3c.dom.Node;

/**
 * A simple class representing an gmd:EX_GeographicBoundingBox
 * @author VOT002
 *
 */
public class CSWGeographicBoundingBox implements Serializable, CSWGeographicElement{

	private static final long serialVersionUID = 1L;
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
     *
     * @param node should represent a gmd:EX_GeographicBoundingBox node
     * @return
     */
    public static CSWGeographicBoundingBox fromGeographicBoundingBoxNode(Node node) throws Exception {
        CSWNamespaceContext nc = new CSWNamespaceContext();

        XPathExpression westBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:westBoundLongitude/gco:Decimal", nc);
        XPathExpression eastBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:eastBoundLongitude/gco:Decimal", nc);
        XPathExpression northBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:northBoundLatitude/gco:Decimal", nc);
        XPathExpression southBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:southBoundLatitude/gco:Decimal", nc);


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
