package org.auscope.portal.core.services.responses.wcs;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;

/**
 * Represents a <wcs:interval> element from a WCS DescribeCoverage response
 *
 * @author vot002
 */
public class Interval implements ValueEnumType {

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    private String type;

    private Double min;
    private Double max;
    private Double resolution;

    public Interval(Node node, WCSNamespaceContext nc) throws XPathExpressionException {
        type = node.getLocalName();

        Node tempNode = (Node) DOMUtil.compileXPathExpr("wcs:min", nc).evaluate(node, XPathConstants.NODE);
        if (tempNode != null)
            min = new Double(tempNode.getTextContent());

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:max", nc).evaluate(node, XPathConstants.NODE);
        if (tempNode != null)
            max = new Double(tempNode.getTextContent());

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:resolution", nc).evaluate(node, XPathConstants.NODE);
        if (tempNode != null)
            resolution = new Double(tempNode.getTextContent());
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * Represents the minimum value on this interval (can be null)
     *
     * @return
     */
    public Double getMin() {
        return min;
    }

    /**
     * Represents the maximum value on this interval (can be null)
     *
     * @return
     */
    public Double getMax() {
        return max;
    }

    /**
     * Represents the resolution of this interval (can be null)
     *
     * @return
     */
    public Double getResolution() {
        return resolution;
    }

}
