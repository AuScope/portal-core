package org.auscope.portal.core.services.responses.wms;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    /** The time extent, if present */
    private String[] timeExtent;

    /** The child layer srs. */
    private String[] childLayerSRS;

    /**
     * Instantiates a new gets the capabilities wms layer record.
     *
     * @param node
     *            the node
     */
    public GetCapabilitiesWMSLayer_1_3_0(Node node) {
        this.node = node;
    }

    // --- Attribute Setters and Getters - now modified to also be retrievers

    /**
     * Gets the name.
     *
     * @return the name
     * @throws XPathException
     *             the x path expression exception
     */
    @Override
    public String getName() throws XPathException {
        if (name == null) {
            Node tempNode = (Node) DOMUtil.compileXPathExpr("Name").evaluate(node, XPathConstants.NODE);
            name = tempNode != null ? tempNode.getTextContent() : "";
        }
        return name;
    }

    /**
     * Gets the title.
     *
     * @return the title
     * @throws XPathException
     *             the x path expression exception
     */
    @Override
    public String getTitle() throws XPathException {
        if (title == null) {
            Node tempNode = (Node) DOMUtil.compileXPathExpr("Title").evaluate(node, XPathConstants.NODE);
            title = tempNode != null ? tempNode.getTextContent() : "";
        }
        return title;
    }

    /**
     * Gets the legendURL.
     *
     * @return the legendURL
     * @throws XPathException
     *             the x path expression exception
     */
    @Override
    public String getLegendURL() throws XPathException {
        if (legendURL == null) {
            Node tempNode = (Node) DOMUtil.compileXPathExpr("Style/LegendURL/OnlineResource").evaluate(node, XPathConstants.NODE);
            legendURL = tempNode != null ? tempNode.getAttributes().getNamedItem("xlink:href").getNodeValue() : "";
        }
        return legendURL;
    }

    /**
     * Gets the abstract.
     *
     * @return the abstract
     * @throws XPathException
     *             the x path expression exception
     */
    @Override
    public String getAbstract() throws XPathException {
        if (description == null) {
            Node tempNode = (Node) DOMUtil.compileXPathExpr("Abstract").evaluate(node, XPathConstants.NODE);
            description = tempNode != null ? tempNode.getTextContent() : "";
        }
        return description;
    }

    /**
     * Gets the metadataURL.
     *
     * @return the metadataURL
     * @throws XPathException
     *             the x path expression exception
     */
    @Override
    public String getMetadataURL() throws XPathException {

        // look for the metadataURL in the nested OnlineResource element
        Node tempNode = (Node) DOMUtil.compileXPathExpr("MetadataURL/OnlineResource").evaluate(node, XPathConstants.NODE);
        metadataURL = tempNode != null ? tempNode.getAttributes().getNamedItem("xlink:href").getNodeValue() : "";

        // iff not there, use the text in the Layer's MetadataURL node directly
        if (StringUtils.isBlank(metadataURL)) {
            tempNode = (Node) DOMUtil.compileXPathExpr("MetadataURL").evaluate(node, XPathConstants.NODE);
            metadataURL = tempNode != null ? tempNode.getTextContent() : "";
        }

        return metadataURL;
    }

    /**
     * Gets the bounding box.
     *
     * @return the bounding box
     * @throws XPathException
     */
    @Override
    public CSWGeographicBoundingBox getBoundingBox() {
        if (bbox == null) {
            Node tempNode;
            try {
                tempNode = (Node) DOMUtil.compileXPathExpr("EX_GeographicBoundingBox").evaluate(node, XPathConstants.NODE);
                if (tempNode != null) {
                    String minx = (String) DOMUtil.compileXPathExpr("westBoundLongitude").evaluate(tempNode, XPathConstants.STRING);
                    String maxx = (String) DOMUtil.compileXPathExpr("eastBoundLongitude").evaluate(tempNode, XPathConstants.STRING);
                    String miny = (String) DOMUtil.compileXPathExpr("southBoundLatitude").evaluate(tempNode, XPathConstants.STRING);
                    String maxy = (String) DOMUtil.compileXPathExpr("northBoundLatitude").evaluate(tempNode, XPathConstants.STRING);

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
            } catch (XPathException ex) {
                log.error("Format error", ex);
                throw new RuntimeException(ex);
            }
        }
        return bbox;
    }
    
    /**
     * Gets the time extent as an array of Strings. Currently this only
     * supports time dimensions as a comma separated list of dates.
     * 
     * Note: MapServer may use 'min/max/res', or comma delimited list of same, not supported yet.
     * 
     * @return the time extent
     * @throws XPathExpressionException
     *             the x path expression exception 
     */
    @Override
    public String[] getTimeExtent() throws XPathException {
        // WMS version 1.3.0 places its time values in a 'Dimension' element
    	if(timeExtent == null) {
    		Node tempNode = (Node) DOMUtil.compileXPathExpr("Dimension[@name='time']").evaluate(node, XPathConstants.NODE);
            String timeStr = tempNode != null ? tempNode.getTextContent() : null;
            if(timeStr != null) {
	            timeExtent = timeStr.split(",");
            }
        }
    	return timeExtent;
    }

    /**
     * Gets the child layer srs.
     *
     * @return the child layer srs
     * @throws XPathException
     *             the x path expression exception
     */
    @Override
    public String[] getChildLayerSRS() throws XPathException {
        if (childLayerSRS == null) {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("CRS").evaluate(node, XPathConstants.NODESET);
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
        } catch (XPathException ex) {
            log.error("Format error", ex);
            throw new RuntimeException(ex);
        }
        return buf.toString();
    }

}
