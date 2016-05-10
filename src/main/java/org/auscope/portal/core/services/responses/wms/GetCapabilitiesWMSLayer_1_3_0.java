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

// TODO: Auto-generated Javadoc
/**
 * This class represents WMS Layer node within GetCapabilites WMS response.
 *
 * @author VictorTey
 * @version
 *
 */
public class GetCapabilitiesWMSLayer_1_3_0 implements GetCapabilitiesWMSLayerRecord {

    private Node node;

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

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
    public GetCapabilitiesWMSLayer_1_3_0(Node node) throws XPathExpressionException {
        this.node = node;
    }

    // --- Attribute Setters and Getters - now modified to also be retrievers

    /**
     * Gets the name.
     *
     * @return the name
     * @throws XPathExpressionException
     *             the x path expression exception
     */
    @Override
    public String getName() throws XPathExpressionException {
        if (name == null) {
            Node tempNode = (Node) getXPath().evaluate("Name", node, XPathConstants.NODE);
            name = tempNode != null ? tempNode.getTextContent() : "";
        }
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
        if (title == null) {
            Node tempNode = (Node) xPath.evaluate("Title", node, XPathConstants.NODE);
            title = tempNode != null ? tempNode.getTextContent() : "";
        }
        return title;
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
        if (legendURL == null) {
            Node tempNode = (Node) xPath.evaluate("Style/LegendURL/OnlineResource", node, XPathConstants.NODE);
            legendURL = tempNode != null ? tempNode.getAttributes().getNamedItem("xlink:href").getNodeValue() : "";
        }
        return legendURL;
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
        if (description == null) {
            Node tempNode = (Node) xPath.evaluate("Abstract", node, XPathConstants.NODE);
            description = tempNode != null ? tempNode.getTextContent() : "";
        }
        return description;
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
        if (StringUtils.isBlank(metadataURL)) {
            tempNode = (Node) getXPath().evaluate("MetadataURL", node, XPathConstants.NODE);
            metadataURL = tempNode != null ? tempNode.getTextContent() : "";
        }       
        
        return metadataURL;
    }

    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     * @throws XPathExpressionException 
     */
    @Override
    public CSWGeographicBoundingBox getBoundingBox() {
        if (bbox == null) {
            Node tempNode;
            try {
                tempNode = (Node) getXPath().evaluate("EX_GeographicBoundingBox", node, XPathConstants.NODE);
                if (tempNode != null) {
                    String minx = (String) getXPath().evaluate("westBoundLongitude", tempNode, XPathConstants.STRING);
                    String maxx = (String) getXPath().evaluate("eastBoundLongitude", tempNode, XPathConstants.STRING);
                    String miny = (String) getXPath().evaluate("southBoundLatitude", tempNode, XPathConstants.STRING);
                    String maxy = (String) getXPath().evaluate("northBoundLatitude", tempNode, XPathConstants.STRING);
                    
                    // Attempt to parse our bounding box
                    try {
                        bbox = new CSWGeographicBoundingBox(Double.parseDouble(minx),
                                Double.parseDouble(maxx),
                                Double.parseDouble(miny),
                                Double.parseDouble(maxy));
                    } catch (NumberFormatException e) {
                        log.debug("Unable to parse the bounding box.");
                    }
                    
                }
            } catch (XPathExpressionException ex) {
                log.error("Format error", ex);
                throw new RuntimeException(ex);
            }
        }
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
        if (childLayerSRS == null) {
            NodeList nodes = (NodeList) xPath.evaluate("CRS", node, XPathConstants.NODESET);
            childLayerSRS = new String[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childSRSNode = nodes.item(i);
                childLayerSRS[i] = childSRSNode != null ? childSRSNode.getTextContent() : "";
            }
        }
        return childLayerSRS;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final String seperator = ",";

        StringBuffer buf = new StringBuffer();
        try {
            buf.append(getName());
            buf.append(seperator);
            buf.append(getTitle());
            buf.append(seperator);
            buf.append(getAbstract());
            buf.append(seperator);
        } catch (XPathExpressionException ex) {
            log.error("Format error", ex);
            throw new RuntimeException(ex);
        }
        return buf.toString();
    }

}
