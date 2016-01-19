package org.auscope.portal.core.services.responses.wcs;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a "stupid" representation of the spatial elements of a <gml:Envelope> from a WCS DescribeCoverage response
 *
 * @author vot002
 *
 */
public class SimpleEnvelope implements Serializable {

    private static final long serialVersionUID = 2L;
    private String srsName;
    private String type;
    private double southBoundLatitude;
    private double northBoundLatitude;
    private double eastBoundLongitude;
    private double westBoundLongitude;

    /**
     *
     * @param srsName
     *            Gets the SRS Name of the ordinates this envelope is representing (Can be null/empty)
     * @param type
     * @param southBoundLatitude
     * @param northBoundLatitude
     * @param eastBoundLongitude
     * @param westBoundLongitude
     */
    public SimpleEnvelope(String srsName, String type,
            double southBoundLatitude, double northBoundLatitude,
            double eastBoundLongitude, double westBoundLongitude) {
        super();
        this.srsName = srsName;
        this.type = type;
        this.southBoundLatitude = southBoundLatitude;
        this.northBoundLatitude = northBoundLatitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.westBoundLongitude = westBoundLongitude;
    }

    public SimpleEnvelope(Node node, WCSNamespaceContext nc) throws XPathExpressionException {
        //get our list of gml:Points and parse our spatial bounds
        NodeList tempNodeList = (NodeList) DOMUtil.compileXPathExpr("gml:pos", nc).evaluate(node, XPathConstants.NODESET);
        if (tempNodeList.getLength() != 2)
            throw new XPathExpressionException(String.format("%1$s:%2$s does not have 2 gml:pos nodes",
                    node.getNamespaceURI(), node.getLocalName()));
        String[] southWestPoints = tempNodeList.item(0).getTextContent().split(" ");
        String[] northEastPoints = tempNodeList.item(1).getTextContent().split(" ");
        if (southWestPoints.length < 2 || northEastPoints.length < 2)
            throw new XPathExpressionException("wcs:lonLatEnvelope gml:pos elements don't contain enough Lon/Lat pairs");

        eastBoundLongitude = Double.parseDouble(northEastPoints[0]);
        southBoundLatitude = Double.parseDouble(southWestPoints[1]);
        westBoundLongitude = Double.parseDouble(southWestPoints[0]);
        northBoundLatitude = Double.parseDouble(northEastPoints[1]);

        //Get our SRS name (can be null)
        srsName = (String) DOMUtil.compileXPathExpr("@srsName", new WCSNamespaceContext()).evaluate(node, XPathConstants.STRING);

        type = node.getLocalName();
    }

    /**
     * Gets the SRS Name of the ordinates this envelope is representing (Can be null/empty)
     *
     * @return
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * Gets the southBoundLatitude of the bounding box
     *
     * @return
     */
    public double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    /**
     * Gets the northBoundLatitude of the bounding box
     *
     * @return
     */
    public double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    /**
     * Gets the eastBoundLongitude of the bounding box
     *
     * @return
     */
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * Gets the westBoundLongitude of the bounding box
     *
     * @return
     */
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }

    public String getType() {
        return type;
    }
}
