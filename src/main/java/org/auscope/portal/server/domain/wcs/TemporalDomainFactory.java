package org.auscope.portal.server.domain.wcs;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

public class TemporalDomainFactory {

    /**
     * Generates a SimpleTimePosition or SimpleTimePeriod instance from a child of a  <wcs:temporalDomain> element
     * @param node must be referencing a child from a <wcs:temporalDomain> node.
     * @return
     */
    public static TemporalDomain parseFromNode(Node node) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new WCSNamespaceContext());
        
        if (node.getLocalName().equals("timePosition")) {
            return new SimpleTimePosition(node, xPath);
        } else if (node.getLocalName().equals("timePeriod")) {
            return new SimpleTimePeriod(node, xPath);
        } else {
            throw new IllegalArgumentException("Unable to parse " + node.getLocalName());
        }
    }
}
