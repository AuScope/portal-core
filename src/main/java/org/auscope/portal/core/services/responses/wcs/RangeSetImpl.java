package org.auscope.portal.core.services.responses.wcs;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RangeSetImpl implements RangeSet {

    private static final long serialVersionUID = 1L;
    private String description;
    private String label;
    private String name;
    private ValueEnumType[] nullValues;
    private AxisDescription[] axisDescriptions;

    public RangeSetImpl(Node node, WCSNamespaceContext nc) throws XPathExpressionException {
        Node tempNode = (Node) DOMUtil.compileXPathExpr("wcs:description", nc).evaluate(node, XPathConstants.NODE);
        if (tempNode != null)
            description = tempNode.getTextContent();

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:name", nc).evaluate(node, XPathConstants.NODE);
        name = tempNode.getTextContent();

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:label", nc).evaluate(node, XPathConstants.NODE);
        label = tempNode.getTextContent();

        NodeList tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:axisDescription/wcs:AxisDescription", nc).evaluate(node, XPathConstants.NODESET);
        axisDescriptions = new AxisDescription[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            axisDescriptions[i] = new AxisDescriptionImpl(tempNodeList.item(i), nc);
        }

        tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:nullValues/wcs:*", nc).evaluate(node, XPathConstants.NODESET);
        nullValues = new ValueEnumType[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            nullValues[i] = ValueEnumTypeFactory.parseFromNode(tempNodeList.item(i), nc);
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
