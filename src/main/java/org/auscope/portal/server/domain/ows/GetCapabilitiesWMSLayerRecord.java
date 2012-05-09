package org.auscope.portal.server.domain.ows;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.record.CSWGeographicBoundingBox;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * This class represents WMS Layer node within GetCapabilites WMS response.
 *
 * @author JarekSanders
 * @version $Id$
 *
 */
public class GetCapabilitiesWMSLayerRecord {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

    /** The name. */
    private String name;

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The bbox. */
    private CSWGeographicBoundingBox bbox;

    /** The child layer srs. */
    private String[] childLayerSRS;


    /**
     * Instantiates a new gets the capabilities wms layer record.
     *
     * @param node the node
     * @throws XPathExpressionException the xpath expression exception
     */
    public GetCapabilitiesWMSLayerRecord(Node node) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        String layerNameExpression = "Name";
        Node tempNode = (Node) xPath.evaluate(layerNameExpression, node, XPathConstants.NODE);
        name = tempNode != null ? tempNode.getTextContent() : "";

        String layerTitleExpression = "Title";
        tempNode = (Node) xPath.evaluate(layerTitleExpression, node, XPathConstants.NODE);
        title = tempNode != null ? tempNode.getTextContent() : "";

        String layerAbstractExpression = "Abstract";
        tempNode = (Node) xPath.evaluate(layerAbstractExpression, node, XPathConstants.NODE);
        description = tempNode != null ? tempNode.getTextContent() : "";

        String latLonBoundingBox = "LatLonBoundingBox";
        tempNode = (Node) xPath.evaluate(latLonBoundingBox, node, XPathConstants.NODE);
        if (tempNode != null) {
            String minx = (String) xPath.evaluate("@minx", tempNode, XPathConstants.STRING);
            String maxx = (String) xPath.evaluate("@maxx", tempNode, XPathConstants.STRING);
            String miny = (String) xPath.evaluate("@miny", tempNode, XPathConstants.STRING);
            String maxy = (String) xPath.evaluate("@maxy", tempNode, XPathConstants.STRING);

            //Attempt to parse our bounding box
            try {
                bbox = new CSWGeographicBoundingBox(Double.parseDouble(minx),
                        Double.parseDouble(maxx),
                        Double.parseDouble(miny),
                        Double.parseDouble(maxy));
            } catch (NumberFormatException e) {
                log.debug("Unable to parse the bounding box.");
            }

        }

        String layerSRSExpression = "SRS";
        NodeList nodes = (NodeList) xPath.evaluate(layerSRSExpression,
                node,
                XPathConstants.NODESET);
        childLayerSRS = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childSRSNode = nodes.item(i);
            childLayerSRS[i] = childSRSNode != null ? childSRSNode.getTextContent() : "";
        }
    }


    // ------------------------------------------ Attribute Setters and Getters

    /**
     * Gets the name.
     *
     * @return the name
     * @throws XPathExpressionException the x path expression exception
     */
    public String getName() throws XPathExpressionException {
        return name;
    }

    /**
     * Gets the title.
     *
     * @return the title
     * @throws XPathExpressionException the x path expression exception
     */
    public String getTitle() throws XPathExpressionException {
        return title;
    }

    /**
     * Gets the abstract.
     *
     * @return the abstract
     * @throws XPathExpressionException the x path expression exception
     */
    public String getAbstract() throws XPathExpressionException {
        return description;
    }

    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     */
    public CSWGeographicBoundingBox getBoundingBox() {
        return bbox;
    }

    /**
     * Gets the child layer srs.
     *
     * @return the child layer srs
     * @throws XPathExpressionException the x path expression exception
     */
    public String[] getChildLayerSRS() throws XPathExpressionException {
        return childLayerSRS;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final String seperator = ",";

        StringBuffer buf = new StringBuffer();
        buf.append(name);
        buf.append(seperator);
        buf.append(title);
        buf.append(seperator);
        buf.append(description);
        buf.append(seperator);
        return buf.toString();
    }

}
