package org.auscope.portal.core.services.responses.wcs;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a "stupid" representation of the spatial elements of a <gml:Envelope> or <wcs:Envelope> or <gml:EnvelopeWithTimePeriod>
 * from a WCS DescribeCoverage response
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
    private String timePositionStart = null;
    private String timePositionEnd = null;

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
            double eastBoundLongitude, double westBoundLongitude,
            String timePositionStart, String timePositionEnd) {
        super();
        this.srsName = srsName;
        this.type = type;
        this.southBoundLatitude = southBoundLatitude;
        this.northBoundLatitude = northBoundLatitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.westBoundLongitude = westBoundLongitude;
        this.timePositionStart = timePositionStart;
        this.timePositionEnd = timePositionEnd;
    }

    /**
     * Constructor to parse the XML in 'node' and create a 'SimpleEnvelope' object
     * 
     * @param node can be one of: 'wcs:Envelope' or 'gml:Envelope' or 'gml:EnvelopeWithTimePeriod'
     * @param nc namespace context
     * @throws XPathException
     */
    public SimpleEnvelope(Node node, WCSNamespaceContext nc) throws XPathException {
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

        // Parse any time position nodes if parent was "gml:EnvelopeWithTimePeriod"
        if (type.equals("EnvelopeWithTimePeriod")) {
            tempNodeList = (NodeList) DOMUtil.compileXPathExpr("gml:timePosition", nc).evaluate(node, XPathConstants.NODESET);
            if (tempNodeList.getLength() != 2) {
                throw new XPathExpressionException(String.format("%1$s:%2$s does not have 2 gml:timePosition nodes",
                node.getNamespaceURI(), node.getLocalName()));
            }
            timePositionStart = tempNodeList.item(0).getTextContent();
            timePositionEnd = tempNodeList.item(1).getTextContent();
        }
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

    /**
     * Test if this object has time period information
     * 
     * @return true iff this envelope object has time period information
     */
    public boolean hasTimePeriod() {
        return type.equals("EnvelopeWithTimePeriod");
    }

    /**
     * Gets the envelope's start time position
     * will return null if none exists
     */
    public String getTimePositionStart() {
        return timePositionStart;
    }

    /**
     * Gets the envelope's end time position
     * will return null if none exists 
     */
    public String getTimePositionEnd() {
        return timePositionEnd;
    }
}
