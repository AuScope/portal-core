package org.auscope.portal.core.services.responses.wcs;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.w3c.dom.Node;

public class ValueEnumTypeFactory {
    /**
     * Generates a SimpleTimePosition or SimpleTimePeriod instance from a child of a <wcs:temporalDomain> element
     *
     * @param node
     *            must be referencing a child from a <wcs:temporalDomain> node.
     * @return
     */
    public static ValueEnumType parseFromNode(Node node, WCSNamespaceContext nc) throws Exception {
        if (node.getLocalName().equals("singleValue")) {
            return new SingleValue(node);
        } else if (node.getLocalName().equals("interval")) {
            return new Interval(node, nc);
        } else {
            throw new IllegalArgumentException("Unable to parse " + node.getLocalName());
        }
    }
}
