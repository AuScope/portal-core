package org.auscope.portal.server.domain.wcs;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple (partial) implementation of the entire AxisDescription
 * (Doesn't parse attributes)
 * @author vot002
 *
 */
public class AxisDescriptionImpl implements AxisDescription {

	private static final long serialVersionUID = 1L;
	private String description;
    private String name;
    private String label;
    private ValueEnumType[] values;


    public AxisDescriptionImpl(Node node, XPath xPath) throws Exception {
        Node tempNode;

        //optional
        tempNode = (Node) xPath.evaluate("wcs:description", node, XPathConstants.NODE);
        if (tempNode != null)
            description = tempNode.getTextContent();

        tempNode = (Node) xPath.evaluate("wcs:name", node, XPathConstants.NODE);
        name = tempNode.getTextContent();

        tempNode = (Node) xPath.evaluate("wcs:label", node, XPathConstants.NODE);
        label = tempNode.getTextContent();

        NodeList tempNodeList = (NodeList) xPath.evaluate("wcs:values/wcs:*", node, XPathConstants.NODESET);
        values = new ValueEnumType[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            values[i] = ValueEnumTypeFactory.parseFromNode(tempNodeList.item(i));
        }
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public ValueEnumType[] getValues() {
        return values;
    }
}
