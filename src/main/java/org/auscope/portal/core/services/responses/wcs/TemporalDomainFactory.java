package org.auscope.portal.core.services.responses.wcs;

import java.text.ParseException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class TemporalDomainFactory {

    /**
     * Generates a SimpleTimePosition or SimpleTimePeriod instance from a child of a <wcs:temporalDomain> element
     * 
     * @param node
     *            must be referencing a child from a <wcs:temporalDomain> node.
     * @return
     * @throws ParseException 
     * @throws DOMException 
     * @throws XPathExpressionException 
     */
    public static TemporalDomain parseFromNode(Node node) throws DOMException, ParseException, XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new WCSNamespaceContext());

        if (node.getLocalName().equals("timePosition")) {
            return new SimpleTimePosition(node);
        } else if (node.getLocalName().equals("timePeriod")) {
            return new SimpleTimePeriod(node, xPath);
        } else {
            throw new IllegalArgumentException("Unable to parse " + node.getLocalName());
        }
    }
}
