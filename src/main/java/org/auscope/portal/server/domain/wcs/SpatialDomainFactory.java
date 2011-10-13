package org.auscope.portal.server.domain.wcs;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

public class SpatialDomainFactory {
    /**
     * Generates a SimpleEnvelope instance from a <gml:Envelope> element
     * @param node must be referencing a <gml:Envelope> (or derivative) node.
     * @return
     */
    public static SpatialDomain parseFromNode(Node node) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new WCSNamespaceContext());

        if (node.getLocalName().equals("Envelope"))
            return new SimpleEnvelope(node, xPath);
        else if (node.getLocalName().equals("EnvelopeWithTimePeriod"))
            return new SimpleEnvelope(node, xPath);
        else
            throw new IllegalArgumentException("unable to parse " + node.getLocalName());
    }
}
