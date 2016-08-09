package org.auscope.portal.core.services.responses.wcs;

import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.w3c.dom.Node;

public class ValueEnumTypeFactory {
    /**
     * Generates a SimpleTimePosition or SimpleTimePeriod instance from a child of a <wcs:temporalDomain> element
     *
     * @param node
     *            must be referencing a child from a <wcs:temporalDomain> node.
     * @return
     * @throws XPathExpressionException 
     */
    public static ValueEnumType parseFromNode(Node node, WCSNamespaceContext nc) throws XPathExpressionException {
        if (node.getLocalName().equals("singleValue")) {
            return new SingleValue(node);
        } else if (node.getLocalName().equals("interval")) {
            return new Interval(node, nc);
        } else {
            throw new IllegalArgumentException("Unable to parse " + node.getLocalName());
        }
    }
}
