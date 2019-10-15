package org.auscope.portal.core.services.responses.wcs;

import java.io.Serializable;
import java.text.ParseException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CoverageOfferingBrief implements Serializable {

	private static final long serialVersionUID = 1545107506827553704L;
	
	private String name;
	private String description;
	private String label;
	private SimpleEnvelope lonLatEnvelope;
	private SimpleTimePosition[] timePositions;

	public CoverageOfferingBrief(String name, String description, String label, SimpleEnvelope lonLatEnvelope,
			SimpleTimePosition[] timePositions) {
		this.name = name;
		this.description = description;
		this.label = label;
		this.lonLatEnvelope = lonLatEnvelope;
		this.timePositions = timePositions;
	}
	
	/**
	 * Construct a CoverageOfferingBrief from an XML CoverageOfferingBrief DOM Node
	 * 
	 * @param node the XML node
	 * @throws XPathExpressionException
	 * @throws ParseException 
	 * @throws DOMException 
	 */
	public CoverageOfferingBrief(Node node) throws XPathExpressionException, DOMException, ParseException {
		WCSNamespaceContext nc = new WCSNamespaceContext();
		Node tempNode = null;
		NodeList tempNodeList = null;
        
        tempNode = (Node)DOMUtil.compileXPathExpr("name").evaluate(node, XPathConstants.NODE);
        name = getTextContentOrEmptyString(tempNode);
        
		tempNode = (Node)DOMUtil.compileXPathExpr("description").evaluate(node, XPathConstants.NODE);
        description = getTextContentOrEmptyString(tempNode);

        tempNode = (Node)DOMUtil.compileXPathExpr("label").evaluate(node, XPathConstants.NODE);
        label = getTextContentOrEmptyString(tempNode);
        
        //Parse our spatial domain (currently only looking at lonLatEvelope)
        tempNode = (Node) DOMUtil.compileXPathExpr("lonLatEnvelope").evaluate(node, XPathConstants.NODE);
        if (tempNode != null) {
            lonLatEnvelope = new SimpleEnvelope(tempNode, nc);
            tempNodeList = (NodeList)DOMUtil.compileXPathExpr("gml:timePosition", nc).evaluate(tempNode, XPathConstants.NODESET);
            timePositions = new SimpleTimePosition[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                timePositions[i] = new SimpleTimePosition(tempNodeList.item(i));
            }
        }
	}
	
	/**
     * Gets the text content or empty string.
     *
     * @param node
     *            the node
     * @return the text content or empty string
     */
    private static String getTextContentOrEmptyString(Node node) {
        if (node != null) {
            return node.getTextContent();
        } else {
            return "";
        }
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public SimpleEnvelope getLonLatEnvelope() {
		return lonLatEnvelope;
	}

	public void setLonLatEnvelope(SimpleEnvelope lonLatEnvelope) {
		this.lonLatEnvelope = lonLatEnvelope;
	}

	public SimpleTimePosition[] getTimePositions() {
		return timePositions;
	}

	public void setTimePositions(SimpleTimePosition[] timePositions) {
		this.timePositions = timePositions;
	}

}
