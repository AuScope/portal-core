package org.auscope.portal.core.services.responses.wcs;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple (partial) implementation of the entire AxisDescription (Doesn't parse attributes)
 *
 * @author vot002
 *
 */
public class AxisDescriptionImpl implements AxisDescription {

    private static final long serialVersionUID = 1L;
    private String description;
    private String name;
    private String label;
    private ValueEnumType[] values;

    public AxisDescriptionImpl(Node node, WCSNamespaceContext nc) throws XPathExpressionException {
        Node tempNode;

        //optional
        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:description", nc).evaluate(node, XPathConstants.NODE);
        if (tempNode != null)
            description = tempNode.getTextContent();

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:name", nc).evaluate(node, XPathConstants.NODE);
        name = tempNode.getTextContent();

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:label", nc).evaluate(node, XPathConstants.NODE);
        label = tempNode.getTextContent();

        NodeList tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:values/wcs:*", nc).evaluate(node, XPathConstants.NODESET);
        values = new ValueEnumType[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            values[i] = ValueEnumTypeFactory.parseFromNode(tempNodeList.item(i), nc);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ValueEnumType[] getValues() {
        return values;
    }
}
