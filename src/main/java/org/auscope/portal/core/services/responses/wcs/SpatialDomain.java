package org.auscope.portal.core.services.responses.wcs;

import java.io.Serializable;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents any of the items that belong as children to a <wcs:spatialDomain> element in a DescribeCoverage response.
 *
 * @author Josh Vote
 *
 */
public class SpatialDomain implements Serializable {
    public SimpleEnvelope[] envelopes;
    public RectifiedGrid rectifiedGrid;

    /**
     *
     * @param envelopes
     *            The envelopes (if any) associated with this spatial domain). Can be null
     * @param rectifiedGrid
     *            Returns the RectifiedGrid (if any) associated with this spatial domain. Can be null.
     */
    public SpatialDomain(SimpleEnvelope[] envelopes, RectifiedGrid rectifiedGrid) {
        super();
        this.envelopes = envelopes;
        this.rectifiedGrid = rectifiedGrid;
    }

    /**
     * Creates a new spatial domain
     *
     * @param envelope
     * @param rectifiedGrid
     * @throws XPathExpressionException
     */
    public SpatialDomain(Node node, WCSNamespaceContext nc) throws XPathExpressionException {
        NodeList envelopeNodes = (NodeList) DOMUtil.compileXPathExpr(
                "wcs:Envelope | gml:Envelope | wcs:EnvelopeWithTimePeriod", nc).evaluate(node, XPathConstants.NODESET);
        this.envelopes = new SimpleEnvelope[envelopeNodes.getLength()];
        for (int i = 0; i < envelopeNodes.getLength(); i++) {
            this.envelopes[i] = new SimpleEnvelope(envelopeNodes.item(i), nc);
        }

        Node gridNode = (Node) DOMUtil.compileXPathExpr("gml:RectifiedGrid", nc).evaluate(node, XPathConstants.NODE);
        if (gridNode != null) {
            this.rectifiedGrid = new RectifiedGrid(gridNode);
        }
    }

    /**
     * The envelopes (if any) associated with this spatial domain). Can be null
     *
     * @return
     */
    public SimpleEnvelope[] getEnvelopes() {
        return envelopes;
    }

    /**
     * Returns the RectifiedGrid (if any) associated with this spatial domain. Can be null.
     *
     * @return
     */
    public RectifiedGrid getRectifiedGrid() {
        return rectifiedGrid;
    }
}
