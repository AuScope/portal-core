package org.auscope.portal.server.domain.wcs;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a "stupid" representation of the spatial elements of a <gml:Envelope> from a WCS DescribeCoverage response
 * @author vot002
 *
 */
public class SimpleEnvelope implements SpatialDomain  {

    private String srsName;
    private String type;
    private double southBoundLatitude;
    private double northBoundLatitude;
    private double eastBoundLongitude;
    private double westBoundLongitude;

    public SimpleEnvelope(Node node, XPath xPath) throws XPathExpressionException {
        //get our list of gml:Points and parse our spatial bounds
        NodeList tempNodeList = (NodeList)xPath.evaluate("gml:pos", node, XPathConstants.NODESET);
        if (tempNodeList.getLength() != 2)
            throw new XPathExpressionException(String.format("%1$s:%2$s does not have 2 gml:pos nodes", node.getNamespaceURI(), node.getLocalName()));
        String[] southEastPoints = tempNodeList.item(0).getTextContent().split(" ");
        String[] northWestPoints = tempNodeList.item(1).getTextContent().split(" ");
        if (southEastPoints.length < 2 || northWestPoints.length < 2)
            throw new XPathExpressionException("wcs:lonLatEnvelope gml:pos elements don't contain enough Lon/Lat pairs");

        eastBoundLongitude = Double.parseDouble(southEastPoints[0]);
        southBoundLatitude = Double.parseDouble(southEastPoints[1]);
        westBoundLongitude = Double.parseDouble(northWestPoints[0]);
        northBoundLatitude = Double.parseDouble(northWestPoints[1]);

        //Get our SRS name (can be null)
        srsName = (String) xPath.evaluate("@srsName", node, XPathConstants.STRING);

        type = node.getLocalName();
    }

    /**
     * Gets the SRS Name of the ordinates this envelope is representing (Can be null/empty)
     * @return
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * Gets the southBoundLatitude of the bounding box
     * @return
     */
    public double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    /**
     * Gets the northBoundLatitude of the bounding box
     * @return
     */
    public double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    /**
     * Gets the eastBoundLongitude of the bounding box
     * @return
     */
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * Gets the westBoundLongitude of the bounding box
     * @return
     */
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }



    @Override
    public String getType() {
        return type;
    }
}
