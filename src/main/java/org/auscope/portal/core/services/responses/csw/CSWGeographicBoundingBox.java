package org.auscope.portal.core.services.responses.csw;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.springframework.data.elasticsearch.annotations.GeoShapeField;
import org.springframework.data.elasticsearch.annotations.GeoShapeField.Orientation;
import org.springframework.data.elasticsearch.core.geo.GeoJsonPolygon;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple class representing an gmd:EX_GeographicBoundingBox.
 *
 * @author VOT002
 */
public class CSWGeographicBoundingBox implements Serializable, CSWGeographicElement {

    protected static final Log logger = LogFactory.getLog(new CSWGeographicBoundingBox().getClass());
    
    // The Constant serialVersionUID.
    private static final long serialVersionUID = 1L;

    // The west bound longitude.
    private double westBoundLongitude;

    // The east bound longitude.
    private double eastBoundLongitude;

    // The south bound latitude.
    private double southBoundLatitude;

    // The north bound latitude.
    private double northBoundLatitude;
    
    /** Non-OGC fields below **/
    // The GeoJsonPolyogn instance of the bounding box, used for indexing geometry in Elasticsearch.
    // Note: I don't think the annotation fields are being used correctly by Spring ES Data, but
    // have added an explicit mapping to EalsticsearchService's @PostConstruct method
    @GeoShapeField(
    		ignoreMalformed = true,
    		coerce = true,
    		orientation = Orientation.ccw,
    		ignoreZValue = true
    )
    private GeoJsonPolygon boundingPolygon;

    // True iff this was constructed with missing source coords and a global default had to be substituted
    private boolean missingSourceCoords;

    /**
     * Instantiates a new CSW geographic bounding box.
     */
    public CSWGeographicBoundingBox() {
        this.missingSourceCoords = false;
    }

    /**
     * Instantiates a new CSW geographic bounding box.
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
        this.missingSourceCoords = Double.isNaN(westBoundLongitude) || Double.isNaN(eastBoundLongitude) || 
                              Double.isNaN(southBoundLatitude) || Double.isNaN(northBoundLatitude);
        this.westBoundLongitude = Double.isNaN(westBoundLongitude) ? -180 : westBoundLongitude;
        this.eastBoundLongitude = Double.isNaN(eastBoundLongitude) ? 180 : eastBoundLongitude;
        this.southBoundLatitude = Double.isNaN(southBoundLatitude) ? -90 : southBoundLatitude;
        this.northBoundLatitude = Double.isNaN(northBoundLatitude) ? 90 : northBoundLatitude;
    }

    /**
     * Gets the missing source coordinates field
     * 
     * @return true iff this was constructed with missing source coords and a global default had to be substituted
     */
    public boolean hasMissingCoords() {
        return missingSourceCoords;
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
        this.missingSourceCoords = this.missingSourceCoords || Double.isNaN(westBoundLongitude);
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
        this.missingSourceCoords = this.missingSourceCoords || Double.isNaN(eastBoundLongitude);
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
        this.missingSourceCoords = this.missingSourceCoords || Double.isNaN(southBoundLatitude);
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
        this.missingSourceCoords = this.missingSourceCoords || Double.isNaN(northBoundLatitude);
        this.northBoundLatitude = Double.isNaN(northBoundLatitude) ? 90 : northBoundLatitude;
    }
    
    /**
     * Get the GeoJsonPolygon instance of the bounding box
     * 
     * @return the GeoJsonPolygon instance of the bounding box
     */
    @Override
    public GeoJsonPolygon getBoundingPolygon() {
    	return boundingPolygon;
    }
    
    /**
     * Set the GeoJsonPolygon instance of the bounding box
     * 
     * @param boundingPolygon
     */
    @Override
    public void setBoundingPolygon(GeoJsonPolygon boundingPolygon) {
    	this.boundingPolygon = boundingPolygon;
    }
    
    /**
     * Sets the bounding GeoJsonPolygon from lat/lon points
     * 
     * @param westBoundLongitude the west bound longitude
     * @param eastBoundLongitude the east bound longitude
     * @param southBoundLatitude the south bound latitude
     * @param northBoundLatitude the north bound latitude
     */
    @Override
    public void setBoundingPolygon(double westBoundLongitude, double eastBoundLongitude, double southBoundLatitude, double northBoundLatitude) {
    	// Normalize bounds in case inputs arrive swapped
	    double minLon = Math.min(westBoundLongitude, eastBoundLongitude);
	    double maxLon = Math.max(westBoundLongitude, eastBoundLongitude);
	    double minLat = Math.min(southBoundLatitude, northBoundLatitude);
	    double maxLat = Math.max(southBoundLatitude, northBoundLatitude);

	    // GeoPoint(lat, lon)
	    GeoPoint bottomLeft  = new GeoPoint(minLat, minLon);
	    GeoPoint bottomRight = new GeoPoint(minLat, maxLon);
	    GeoPoint topRight    = new GeoPoint(maxLat, maxLon);
	    GeoPoint topLeft     = new GeoPoint(maxLat, minLon);

	    // Closed counter-clockwise outer ring
	    this.boundingPolygon = GeoJsonPolygon.of(
	        bottomLeft, bottomRight, topRight, topLeft, bottomLeft
	    );
    }

    /**
     * Creates a bounding box by parsing a gmd:EX_GeographicBoundingBox node and its children.
     *
     * @param node
     *            should represent a gmd:EX_GeographicBoundingBox node
     * @return the cSW geographic bounding box
     */
    public static CSWGeographicBoundingBox fromGeographicBoundingBoxNode(Node node) throws XPathException {
       
        // Some servers have the lat/lon values within nested gco:Decimal elements, others just have the number (e.g. geoserver 2.15)
        // Do a test to see which format is used
        NodeList nodeList = evalXPathNodeList(node, "gmd:westBoundLongitude/gco:Decimal");
        
        CSWNamespaceContext nc = new CSWNamespaceContext();

        XPathExpression westBoundLongitudeExpr;
        XPathExpression eastBoundLongitudeExpr;
        XPathExpression northBoundLatitudeExpr;
        XPathExpression southBoundLatitudeExpr;
        
        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();
        
        if (nodeList.getLength() > 0) {
            westBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:westBoundLongitude/gco:Decimal", nc);
            eastBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:eastBoundLongitude/gco:Decimal",  nc);
            northBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:northBoundLatitude/gco:Decimal", nc);
            southBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:southBoundLatitude/gco:Decimal", nc);
            
        } else {
            // not only is the encoding not include the gco:Decimal element, but the north/south east/west coords are swapped!!!!
            // this is a hacky workaround!!!!
            /*try {
                logger.info("found dodgy bbox node: " + DOMUtil.buildStringFromDom(node, true));
            } catch (Exception e) {}
            */
            westBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:southBoundLatitude", nc);
            eastBoundLongitudeExpr = DOMUtil.compileXPathExpr("gmd:northBoundLatitude",  nc);
            northBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:eastBoundLongitude", nc);
            southBoundLatitudeExpr = DOMUtil.compileXPathExpr("gmd:westBoundLongitude", nc);
            
            //logger.info("extracted values: " + west + "," + east + "," + south + "," + north);
        }

        bbox.setWestBoundLongitude((Double) westBoundLongitudeExpr.evaluate(node, XPathConstants.NUMBER));
        bbox.setEastBoundLongitude((Double) eastBoundLongitudeExpr.evaluate(node, XPathConstants.NUMBER));
        bbox.setSouthBoundLatitude((Double) southBoundLatitudeExpr.evaluate(node, XPathConstants.NUMBER));
        bbox.setNorthBoundLatitude((Double) northBoundLatitudeExpr.evaluate(node, XPathConstants.NUMBER));

        return bbox;
    }
    
    /**
     * Helper method for evaluating an xpath string on a particular node and returning the result as a (possible empty) list of matching nodes
     *
     * @param node
     * @param xPath
     *            A valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    static protected NodeList evalXPathNodeList(Node node, String xPath) throws XPathException {
        CSWNamespaceContext nc = new CSWNamespaceContext();
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (NodeList) expression.evaluate(node, XPathConstants.NODESET);
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
