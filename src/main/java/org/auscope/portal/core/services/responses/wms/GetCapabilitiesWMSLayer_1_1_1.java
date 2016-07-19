package org.auscope.portal.core.services.responses.wms;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents WMS Layer node within GetCapabilites WMS response.
 *
 * @author JarekSanders
 * @version $Id$
 *
 */
public class GetCapabilitiesWMSLayer_1_1_1 implements GetCapabilitiesWMSLayerRecord {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

    private Node node;

    /** The name. */
    private String name;

    /** The title. */
    private String title;

    /** The description. */
    private String description;

    /** The legendURL. */
    private String legendURL;

    /** The metadataURL. */
    private String metadataURL;

    /** The bbox. */
    private CSWGeographicBoundingBox bbox;

    /** The child layer srs. */
    private String[] childLayerSRS;

    private XPath xPath;

    private XPath getXPath() {
        if (xPath == null) {
            xPath = XPathFactory.newInstance().newXPath();
        }
        return xPath;
    }

    /**
     * Instantiates a new gets the capabilities wms layer record.
     *
     * @param node
     *            the node
     * @throws XPathExpressionException
     *             the xpath expression exception
     */
    public GetCapabilitiesWMSLayer_1_1_1(Node node) throws XPathExpressionException {
        XPath path = XPathFactory.newInstance().newXPath();

        this.node = node;

        String layerNameExpression = "Name";
        Node tempNode = (Node) path.evaluate(layerNameExpression, node, XPathConstants.NODE);
        name = tempNode != null ? tempNode.getTextContent() : "";

        String layerTitleExpression = "Title";
        tempNode = (Node) path.evaluate(layerTitleExpression, node, XPathConstants.NODE);
        title = tempNode != null ? tempNode.getTextContent() : "";

        String layerAbstractExpression = "Abstract";
        tempNode = (Node) path.evaluate(layerAbstractExpression, node, XPathConstants.NODE);
        description = tempNode != null ? tempNode.getTextContent() : "";

        String layerLegendURLExpression = "Style/LegendURL/OnlineResource";
        tempNode = (Node) path.evaluate(layerLegendURLExpression, node, XPathConstants.NODE);
        legendURL = tempNode != null ? tempNode.getAttributes().getNamedItem("xlink:href").getNodeValue() : "";

        metadataURL = getMetadataURL();

        String latLonBoundingBox = "LatLonBoundingBox";
        tempNode = (Node) path.evaluate(latLonBoundingBox, node, XPathConstants.NODE);
        if (tempNode != null) {
            String minx = (String) path.evaluate("@minx", tempNode, XPathConstants.STRING);
            String maxx = (String) path.evaluate("@maxx", tempNode, XPathConstants.STRING);
            String miny = (String) path.evaluate("@miny", tempNode, XPathConstants.STRING);
            String maxy = (String) path.evaluate("@maxy", tempNode, XPathConstants.STRING);

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
        NodeList nodes = (NodeList) path.evaluate(layerSRSExpression,
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
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    @Override
    public String getName() throws XPathExpressionException {
        return name;
    }

    /**
     * Gets the title.
     *
     * @return the title
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    @Override
    public String getTitle() throws XPathExpressionException {
        return title;
    }

    /**
     * Gets the abstract.
     *
     * @return the abstract
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    @Override
    public String getAbstract() throws XPathExpressionException {
        return description;
    }

    /**
     * Gets the legendURL.
     *
     * @return the legendURL
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    @Override
    public String getLegendURL() throws XPathExpressionException {
        return legendURL;
    }


    /**
     * Gets the metadataURL.
     *
     * @return the metadataURL
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    @Override
    public String getMetadataURL() throws XPathExpressionException {

        // look for the metadataURL in the nested OnlineResource element
        Node tempNode = (Node) getXPath().evaluate("MetadataURL/OnlineResource", node, XPathConstants.NODE);
        metadataURL = tempNode != null ? tempNode.getAttributes().getNamedItem("xlink:href").getNodeValue() : "";

        // iff not there, use the text in the Layer's MetadataURL node directly
        if (StringUtils.isBlank(this.metadataURL)) {
            tempNode = (Node) getXPath().evaluate("MetadataURL", node, XPathConstants.NODE);
            metadataURL = tempNode != null ? tempNode.getTextContent() : "";
        }

        return metadataURL;
    }

    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     */
    @Override
    public CSWGeographicBoundingBox getBoundingBox() {
        return bbox;
    }

    /**
     * Gets the child layer srs.
     *
     * @return the child layer srs
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    @Override
    public String[] getChildLayerSRS() throws XPathExpressionException {
        return childLayerSRS;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String seperator = ",";

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
