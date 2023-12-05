package org.auscope.portal.core.util;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.springframework.data.elasticsearch.core.geo.GeoJsonLineString;
import org.springframework.data.elasticsearch.core.geo.GeoJsonPolygon;
import org.springframework.data.geo.Point;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Util class for GeoJsonPolygon objects.
 *
 */
public class GeoJsonPolygonUtils {
	
	protected static final CSWNamespaceContext nc = new CSWNamespaceContext();
	
	/**
	 * 
	 * @param polygon
	 * @return
	 */
	public static double getNorthernMostPoint(GeoJsonPolygon polygon) {
		double northernMostPoint = -90.0;
		
		for(GeoJsonLineString lineString: polygon.getCoordinates()) {
			for(Point point: lineString.getCoordinates()) {
				if (point.getY() > northernMostPoint) {
					northernMostPoint = point.getY();
				}
			}
		}
		if(northernMostPoint == -90.0) {
			northernMostPoint = 90.0;
		} else if(northernMostPoint > 90.0) {
			northernMostPoint = 90.0;
		}
			
		return northernMostPoint;
	}
	
	/**
	 * 
	 * @param polygon
	 * @return
	 */
	public static double getSouthernMostPoint(GeoJsonPolygon polygon) {
		double southernMostPoint = 90.0;
		
		for(GeoJsonLineString lineString: polygon.getCoordinates()) {
			for(Point point: lineString.getCoordinates()) {
				if (point.getY() < southernMostPoint) {
					southernMostPoint = point.getY();
				}
			}
		}
		
		if(southernMostPoint == 90.0) {
			southernMostPoint = -90.0;
		} else if(southernMostPoint < -90.0) {
			southernMostPoint = -90.0;
		}
		
		return southernMostPoint;
	}
	
	/**
	 * 
	 * @param polygon
	 * @return
	 */
	public static double getWesternMostPoint(GeoJsonPolygon polygon) {
		double westernMostPoint = 180.0;
		
		for(GeoJsonLineString lineString: polygon.getCoordinates()) {
			for(Point point: lineString.getCoordinates()) {
				if (point.getX() < westernMostPoint) {
					westernMostPoint = point.getX();
				}
			}
		}
		if(westernMostPoint == 180.0) {
			westernMostPoint = -180.0;
		} else if(westernMostPoint < -180.0) {
			westernMostPoint = -180.0;
		}
			
		return westernMostPoint;
	}
	
	/**
	 * 
	 * @param polygon
	 * @return
	 */
	public static double getEasternMostPoint(GeoJsonPolygon polygon) {
		double easternMostPoint = -180.0;
		
		for(GeoJsonLineString lineString: polygon.getCoordinates()) {
			for(Point point: lineString.getCoordinates()) {
				if (point.getX() > easternMostPoint) {
					easternMostPoint = point.getX();
				}
			}
		}
		if(easternMostPoint == -180.0) {
			easternMostPoint = 180.0;
		} else if(easternMostPoint > 180.0) {
			easternMostPoint = 180.0;
		}
		
		return easternMostPoint;
	}
	
	/**
     * Helper method for evaluating an xpath string on a particular node and returning the result as a (possible empty) list of matching nodes
     *
     * @param node
     * @param xPath
     *            A valid XPath expression
     * @return
     * @throws XPathException
     */
    protected static NodeList evalXPathNodeList(Node node, String xPath) throws XPathException {
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (NodeList) expression.evaluate(node, XPathConstants.NODESET);
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

}
