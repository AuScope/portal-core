package org.auscope.portal.core.services.responses.wcs;

import java.io.Serializable;
import java.text.ParseException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.namespaces.WCSNamespaceContext;
import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a single <CoverageOffering> element from a WCS DescribeCoverage response. (This is only a limited subset of the actual DescribeCoverage response).
 *
 * @author vot002
 * @version $Id$
 */
public class DescribeCoverageRecord implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    @SuppressWarnings("unused")
    private final Log logger = LogFactory.getLog(DescribeCoverageRecord.class);

    /** The description. */
    private String description;

    /** The name. */
    private String name;

    /** The label. */
    private String label;

    /** The supported request crs. */
    private String[] supportedRequestCRSs;

    /** The supported response cr ss. */
    private String[] supportedResponseCRSs;

    /** The supported formats. */
    private String[] supportedFormats;

    /** The supported interpolations. */
    private String[] supportedInterpolations;

    /** The native crs. */
    private String[] nativeCRSs;

    /** The spatial domain. */
    private SpatialDomain spatialDomain;

    /** The temporal domain. */
    private TemporalDomain[] temporalDomain;

    /** The range set. */
    private RangeSet rangeSet;

    private static final String XPATHALLCHILDREN = "./*";

    public DescribeCoverageRecord(String description, String name,
            String label, String[] supportedRequestCRSs,
            String[] supportedResponseCRSs, String[] supportedFormats,
            String[] supportedInterpolations, String[] nativeCRSs,
            SpatialDomain spatialDomain, TemporalDomain[] temporalDomain,
            RangeSet rangeSet) {
        super();
        this.description = description;
        this.name = name;
        this.label = label;
        this.supportedRequestCRSs = supportedRequestCRSs;
        this.supportedResponseCRSs = supportedResponseCRSs;
        this.supportedFormats = supportedFormats;
        this.supportedInterpolations = supportedInterpolations;
        this.nativeCRSs = nativeCRSs;
        this.spatialDomain = spatialDomain;
        this.temporalDomain = temporalDomain;
        this.rangeSet = rangeSet;
    }

    /**
     * Gets the text content or empty string.
     *
     * @param node
     *            the node
     * @return the text content or empty string
     */
    private static String getTextContentOrEmptyString(Node node) {
        if (node != null) {
            return node.getTextContent();
        } else {
            return "";
        }
    }

    /**
     * Generates a record from a given XML <CoverageOffering> Node.
     *
     * @param node
     *            the node
     * @param xPath
     *            Should be configured with the WCSNamespaceContext
     * @throws XPathExpressionException 
     * @throws ParseException 
     * @throws DOMException 
     */
    private DescribeCoverageRecord(Node node, WCSNamespaceContext nc) throws XPathExpressionException, DOMException, ParseException {
        Node tempNode = null;
        NodeList tempNodeList = null;

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:description", nc).evaluate(node, XPathConstants.NODE);
        description = getTextContentOrEmptyString(tempNode);

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:name", nc).evaluate(node, XPathConstants.NODE);
        name = getTextContentOrEmptyString(tempNode);

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:label", nc).evaluate(node, XPathConstants.NODE);
        label = getTextContentOrEmptyString(tempNode);

        //We will get a list of <requestResponseCRSs> OR a list
        //of <requestCRSs> and <responseCRSs>
        //Lets parse one or the other
        tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:supportedCRSs/wcs:requestResponseCRSs", nc).evaluate(node, XPathConstants.NODESET);
        if (tempNodeList.getLength() > 0) {
            supportedRequestCRSs = new String[tempNodeList.getLength()];
            supportedResponseCRSs = new String[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                supportedRequestCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
                supportedResponseCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
            }
        } else {
            tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:supportedCRSs/wcs:requestCRSs", nc).evaluate(node, XPathConstants.NODESET);
            supportedRequestCRSs = new String[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                supportedRequestCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
            }

            tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:supportedCRSs/wcs:responseCRSs", nc).evaluate(node, XPathConstants.NODESET);
            supportedResponseCRSs = new String[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                supportedResponseCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
            }
        }

        tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:supportedFormats/wcs:formats", nc).evaluate(node, XPathConstants.NODESET);
        supportedFormats = new String[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            supportedFormats[i] = getTextContentOrEmptyString(tempNodeList.item(i));
        }

        tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:supportedInterpolations/wcs:interpolationMethod", nc).evaluate(node, XPathConstants.NODESET);
        supportedInterpolations = new String[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            supportedInterpolations[i] = getTextContentOrEmptyString(tempNodeList.item(i));
        }

        tempNodeList = (NodeList) DOMUtil.compileXPathExpr("wcs:supportedCRSs/wcs:nativeCRSs", nc).evaluate(node, XPathConstants.NODESET);
        nativeCRSs = new String[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            nativeCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
        }

        //Parse our spatial domain (only grab gml:Envelopes and wcs:EnvelopeWithTimePeriod
        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:domainSet/wcs:spatialDomain", nc).evaluate(node, XPathConstants.NODE);
        if (tempNode != null) {
            spatialDomain = new SpatialDomain(tempNode, nc);
        }

        //Get the temporal range (which is optional)
        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:domainSet/wcs:temporalDomain", nc).evaluate(node, XPathConstants.NODE);
        if (tempNode != null) {
            tempNodeList = (NodeList) DOMUtil.compileXPathExpr(XPATHALLCHILDREN, nc).evaluate(tempNode, XPathConstants.NODESET);
            temporalDomain = new TemporalDomain[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                temporalDomain[i] = TemporalDomainFactory.parseFromNode(tempNodeList.item(i));
            }
        }

        tempNode = (Node) DOMUtil.compileXPathExpr("wcs:rangeSet/wcs:RangeSet", nc).evaluate(node, XPathConstants.NODE);
        rangeSet = new RangeSetImpl(tempNode, nc);
    }

    /**
     * Gets the description of a coverage.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the name of a coverage.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the label of a coverage.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets a list of CRS's that can be used to make requests about this coverage.
     *
     * @return the supported request cr ss
     */
    public String[] getSupportedRequestCRSs() {
        return supportedRequestCRSs;
    }

    /**
     * Gets a list of CRS's that the requested coverage can be returned in.
     *
     * @return the supported response cr ss
     */
    public String[] getSupportedResponseCRSs() {
        return supportedResponseCRSs;
    }

    /**
     * Gets a list of supported coverage download formats.
     *
     * @return the supported formats
     */
    public String[] getSupportedFormats() {
        return supportedFormats;
    }

    /**
     * Gets the Unordered list of identifiers of the CRSs in which the server stores this data, that is, the CRS(s) in which data can be obtained without any
     * distortion or degradation.
     *
     * Can be empty
     *
     * @return the native cr ss
     */
    public String[] getNativeCRSs() {
        return nativeCRSs;
    }

    /**
     * Represents the list of interpolation methods that are available
     *
     * Can be empty in which case assume it is "nearest neighbour"
     *
     * Possible values... + nearest neighbor (default)
     *
     * These are defined in ISO 19123 (Schema for Coverage Geometry and Functions), Annex B. + bilinear + bicubic + lost area + barycentric
     *
     * No interpolation is available; requests must be for locations that are among the original domain locations. +none
     *
     * @return the supported interpolations
     */
    public String[] getSupportedInterpolations() {
        return supportedInterpolations;
    }

    /**
     * Gets the spatial domain.
     *
     * @return the spatial domain
     */
    public SpatialDomain getSpatialDomain() {
        return spatialDomain;
    }

    /**
     * Gets a (possibly empty) list of all elements that form this coverages temporal domain
     *
     * The standard dictates that there will be EITHER gml:timePosition OR wcs:timePeriod elements.
     *
     * There will be at least one WCSSpatialDomain or WCSTemporal
     *
     * @return the temporal domain
     */
    public TemporalDomain[] getTemporalDomain() {
        return temporalDomain;
    }

    /**
     * Gets the range set that defines what values can be found in a coverage.
     *
     * @return the range set
     */
    public RangeSet getRangeSet() {
        return rangeSet;
    }

    /**
     * Parses the XML response from a DescribeCoverage request into a list of DescribeCoverageRecords.
     *
     * @param doc
     *            the input xml
     * @return the describe coverage record[]
     * @throws OWSException 
     * @throws XPathExpressionException 
     * @throws ParseException 
     * @throws DOMException 
     */
    public static DescribeCoverageRecord[] parseRecords(Document doc) throws OWSException, XPathExpressionException, DOMException, ParseException {
        //This is to make sure we actually receive a valid response
        OWSExceptionParser.checkForExceptionResponse(doc);

        WCSNamespaceContext nc = new WCSNamespaceContext();
        XPathExpression xPath = DOMUtil.compileXPathExpr("/wcs:CoverageDescription/wcs:CoverageOffering", nc);
        NodeList nodes = (NodeList) xPath.evaluate(doc, XPathConstants.NODESET);

        DescribeCoverageRecord[] records = new DescribeCoverageRecord[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            records[i] = new DescribeCoverageRecord(nodes.item(i), nc);
        }

        return records;
    }
}
