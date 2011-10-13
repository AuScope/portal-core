package org.auscope.portal.server.domain.wcs;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RangeSetImpl implements RangeSet {

	private static final long serialVersionUID = 1L;
	private String description;
    private String label;
    private String name;
    private ValueEnumType[] nullValues;
    private AxisDescription[] axisDescriptions;

    public RangeSetImpl(Node node, XPath xPath) throws Exception {

        Node tempNode = (Node) xPath.evaluate("wcs:description", node, XPathConstants.NODE);
        if (tempNode != null)
            description = tempNode.getTextContent();

        tempNode = (Node) xPath.evaluate("wcs:name", node, XPathConstants.NODE);
        name = tempNode.getTextContent();

        tempNode = (Node) xPath.evaluate("wcs:label", node, XPathConstants.NODE);
        label = tempNode.getTextContent();

        NodeList tempNodeList = (NodeList) xPath.evaluate("wcs:axisDescription/wcs:AxisDescription", node, XPathConstants.NODESET);
        axisDescriptions = new AxisDescription[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            axisDescriptions[i] = new AxisDescriptionImpl(tempNodeList.item(i), xPath);
        }

        tempNodeList = (NodeList) xPath.evaluate("wcs:nullValues/wcs:*", node, XPathConstants.NODESET);
        nullValues = new ValueEnumType[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            nullValues[i] = ValueEnumTypeFactory.parseFromNode(tempNodeList.item(i));
        }
    }

    @Override
    public AxisDescription[] getAxisDescriptions() {
        return axisDescriptions;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ValueEnumType[] getNullValues() {
        return nullValues;
    }

}
