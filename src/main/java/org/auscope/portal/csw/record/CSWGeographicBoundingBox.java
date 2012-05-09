package org.auscope.portal.csw.record;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.server.util.DOMUtil;
import org.w3c.dom.Node;

/**
 * A simple class representing an gmd:EX_GeographicBoundingBox.
 *
 * @author VOT002
 */
public class CSWGeographicBoundingBox implements Serializable, CSWGeographicElement {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The west bound longitude. */
    private double westBoundLongitude;

    /** The east bound longitude. */
    private double eastBoundLongitude;

    /** The south bound latitude. */
    private double southBoundLatitude;

    /** The north bound latitude. */
    private double northBoundLatitude;

    /**
     * Instantiates a new cSW geographic bounding box.
     */
    public CSWGeographicBoundingBox() {
    }

    /**
     * Instantiates a new cSW geographic bounding box.
     *
     * @param westBoundLongitude the west bound longitude
     * @param eastBoundLongitude the east bound longitude
     * @param southBoundLatitude the south bound latitude
     * @param northBoundLatitude the north bound latitude
     */
    public CSWGeographicBoundingBox(double westBoundLongitude,
            double eastBoundLongitude, double southBoundLatitude,
            double northBoundLatitude) {
        this.westBoundLongitude = westBoundLongitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.southBoundLatitude = southBoundLatitude;
        this.northBoundLatitude = northBoundLatitude;
    }

    /**
     * Gets the west bound longitude.
     *
     * @return the west bound longitude
     */
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }

    /**
     * Sets the west bound longitude.
     *
     * @param westBoundLongitude the new west bound longitude
     */
    public void setWestBoundLongitude(double westBoundLongitude) {
        this.westBoundLongitude = westBoundLongitude;
    }

    /**
     * Gets the east bound longitude.
     *
     * @return the east bound longitude
     */
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * Sets the east bound longitude.
     *
     * @param eastBoundLongitude the new east bound longitude
     */
    public void setEastBoundLongitude(double eastBoundLongitude) {
        this.eastBoundLongitude = eastBoundLongitude;
    }

    /**
     * Gets the south bound latitude.
     *
     * @return the south bound latitude
     */
    public double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    /**
     * Sets the south bound latitude.
     *
     * @param southBoundLatitude the new south bound latitude
     */
    public void setSouthBoundLatitude(double southBoundLatitude) {
        this.southBoundLatitude = southBoundLatitude;
    }

    /**
     * Gets the north bound latitude.
     *
     * @return the north bound latitude
     */
    public double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    /**
     * Sets the north bound latitude.
     *
     * @param northBoundLatitude the new north bound latitude
     */
    public void setNorthBoundLatitude(double northBoundLatitude) {
        this.northBoundLatitude = northBoundLatitude;
    }

    /**
     * Creates a bounding box by parsing a gmd:EX_GeographicBoundingBox node and its children.
     *
     * @param node should represent a gmd:EX_GeographicBoundingBox node
     * @return the cSW geographic bounding box
     * @throws Exception the exception
     */
    public static CSWGeographicBoundingBox fromGeographicBoundingBoxNode(Node node) throws XPathExpressionException {
        CSWNamespaceContext nc = new CSWNamespaceContext();

        XPathExpression westBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:westBoundLongitude/gco:Decimal", nc);
        XPathExpression eastBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:eastBoundLongitude/gco:Decimal", nc);
        XPathExpression northBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:northBoundLatitude/gco:Decimal", nc);
        XPathExpression southBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:southBoundLatitude/gco:Decimal", nc);


        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();

        bbox.westBoundLongitude = (Double) westBoundLongitudeExpr.evaluate(node, XPathConstants.NUMBER);
        bbox.eastBoundLongitude = (Double) eastBoundLongitudeExpr.evaluate(node, XPathConstants.NUMBER);
        bbox.southBoundLatitude = (Double) southBoundLatitudeExpr.evaluate(node, XPathConstants.NUMBER);
        bbox.northBoundLatitude = (Double) northBoundLatitudeExpr.evaluate(node, XPathConstants.NUMBER);

        return bbox;
    }
}
