package org.auscope.portal.core.services.responses.csw;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
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
     * @param westBoundLongitude
     *            the west bound longitude
     * @param eastBoundLongitude
     *            the east bound longitude
     * @param southBoundLatitude
     *            the south bound latitude
     * @param northBoundLatitude
     *            the north bound latitude
     */
    public CSWGeographicBoundingBox(double westBoundLongitude,
            double eastBoundLongitude, double southBoundLatitude,
            double northBoundLatitude) {
        this.westBoundLongitude = Double.isNaN(westBoundLongitude) ? -180 : westBoundLongitude;
        this.eastBoundLongitude = Double.isNaN(eastBoundLongitude) ? 180 : eastBoundLongitude;
        this.southBoundLatitude = Double.isNaN(southBoundLatitude) ? -90 : southBoundLatitude;
        this.northBoundLatitude = Double.isNaN(northBoundLatitude) ? 90 : northBoundLatitude;
    }

    /**
     * Gets the west bound longitude.
     *
     * @return the west bound longitude
     */
    @Override
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }

    /**
     * Sets the west bound longitude.
     *
     * @param westBoundLongitude
     *            the new west bound longitude
     */
    @Override
    public void setWestBoundLongitude(double westBoundLongitude) {
        this.westBoundLongitude = Double.isNaN(westBoundLongitude) ? -180 : westBoundLongitude;
    }

    /**
     * Gets the east bound longitude.
     *
     * @return the east bound longitude
     */
    @Override
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * Sets the east bound longitude.
     *
     * @param eastBoundLongitude
     *            the new east bound longitude
     */
    @Override
    public void setEastBoundLongitude(double eastBoundLongitude) {
        this.eastBoundLongitude = Double.isNaN(eastBoundLongitude) ? 180 : eastBoundLongitude;
    }

    /**
     * Gets the south bound latitude.
     *
     * @return the south bound latitude
     */
    @Override
    public double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    /**
     * Sets the south bound latitude.
     *
     * @param southBoundLatitude
     *            the new south bound latitude
     */
    @Override
    public void setSouthBoundLatitude(double southBoundLatitude) {
        this.southBoundLatitude = Double.isNaN(southBoundLatitude) ? -90 : southBoundLatitude;
    }

    /**
     * Gets the north bound latitude.
     *
     * @return the north bound latitude
     */
    @Override
    public double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    /**
     * Sets the north bound latitude.
     *
     * @param northBoundLatitude
     *            the new north bound latitude
     */
    @Override
    public void setNorthBoundLatitude(double northBoundLatitude) {
        this.northBoundLatitude = Double.isNaN(northBoundLatitude) ? 90 : northBoundLatitude;
    }

    /**
     * Creates a bounding box by parsing a gmd:EX_GeographicBoundingBox node and its children.
     *
     * @param node
     *            should represent a gmd:EX_GeographicBoundingBox node
     * @return the cSW geographic bounding box
     */
    public static CSWGeographicBoundingBox fromGeographicBoundingBoxNode(Node node) throws XPathExpressionException {
        CSWNamespaceContext nc = new CSWNamespaceContext();

        XPathExpression westBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:westBoundLongitude/gco:Decimal", nc);
        XPathExpression eastBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:eastBoundLongitude/gco:Decimal", nc);
        XPathExpression northBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:northBoundLatitude/gco:Decimal", nc);
        XPathExpression southBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:southBoundLatitude/gco:Decimal", nc);

        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();

        bbox.setWestBoundLongitude((Double) westBoundLongitudeExpr.evaluate(node, XPathConstants.NUMBER));
        bbox.setEastBoundLongitude((Double) eastBoundLongitudeExpr.evaluate(node, XPathConstants.NUMBER));
        bbox.setSouthBoundLatitude((Double) southBoundLatitudeExpr.evaluate(node, XPathConstants.NUMBER));
        bbox.setNorthBoundLatitude((Double) northBoundLatitudeExpr.evaluate(node, XPathConstants.NUMBER));

        return bbox;
    }

    /**
     * Returns true if the specified bounding box intersects this bounding box
     *
     * Algorithm sourced from - http://tekpool.wordpress.com/2006/10/11/rectangle-intersection-determine-if-two-given-rectangles-intersect-each-other-or-not/
     *
     * @param bbox
     * @return
     */
    public boolean intersects(CSWGeographicBoundingBox bbox) {
        return intersects(bbox.getWestBoundLongitude(), bbox.getEastBoundLongitude(),
                bbox.getSouthBoundLatitude(), bbox.getNorthBoundLatitude());
    }

    /**
     * Returns true if the specified bounding box intersects this bounding box
     *
     * Algorithm sourced from - http://tekpool.wordpress.com/2006/10/11/rectangle-intersection-determine-if-two-given-rectangles-intersect-each-other-or-not/
     *
     * @return
     */
    public boolean intersects(double westBoundLong,
            double eastBoundLong, double southBoundLat,
            double northBoundLat) {

        //If a bbox wraps the international date line such that east is in fact less than west
        //We should split the wrapping bbox at the dateline for an easier comparison
        double bboxEast = eastBoundLong;
        double bboxWest = westBoundLong;
        double thisEast = this.eastBoundLongitude;
        double thisWest = this.westBoundLongitude;

        if (bboxEast < bboxWest) {
            CSWGeographicBoundingBox left = new CSWGeographicBoundingBox(bboxWest, 180, southBoundLat,
                    northBoundLat);
            CSWGeographicBoundingBox right = new CSWGeographicBoundingBox(-180, bboxEast, southBoundLat,
                    northBoundLat);

            return this.intersects(left) || this.intersects(right);
        }
        if (thisEast < thisWest) {
            CSWGeographicBoundingBox left = new CSWGeographicBoundingBox(thisWest, 180, this.southBoundLatitude,
                    this.northBoundLatitude);
            CSWGeographicBoundingBox right = new CSWGeographicBoundingBox(-180, thisEast, this.southBoundLatitude,
                    this.northBoundLatitude);

            return left.intersects(westBoundLong, eastBoundLong, southBoundLat, northBoundLat)
                    || right.intersects(westBoundLong, eastBoundLong, southBoundLat, northBoundLat);
        }

        return !(bboxWest > thisEast
                || bboxEast < thisWest
                || southBoundLat > this.northBoundLatitude
                || northBoundLat < this.southBoundLatitude);

    }
}
