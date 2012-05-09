package org.auscope.portal.server.domain.wcs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.ows.OWSExceptionParser;
import org.auscope.portal.server.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a single <CoverageOffering> element from a WCS DescribeCoverage response.
 * (This is only a limited subset of the actual DescribeCoverage response).
 *
 * @author vot002
 * @version $Id$
 */
public class DescribeCoverageRecord implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
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
    private SpatialDomain[] spatialDomain;

    /** The temporal domain. */
    private TemporalDomain[] temporalDomain;

    /** The range set. */
    private RangeSet rangeSet;

    private static final String XPATHALLCHILDREN = "./*";

    /**
     * Gets the text content or empty string.
     *
     * @param node the node
     * @return the text content or empty string
     */
    private String getTextContentOrEmptyString(Node node) {
        if (node != null) {
            return node.getTextContent();
        } else {
            return "";
        }
    }

    /**
     * Generates a record from a given XML <CoverageOffering> Node.
     *
     * @param node the node
     * @param xPath Should be configured with the WCSNamespaceContext
     * @throws Exception the exception
     */
    private DescribeCoverageRecord(Node node, XPath xPath) throws Exception {
        Node tempNode = null;
        NodeList tempNodeList = null;

        tempNode = (Node) xPath.evaluate("wcs:description", node, XPathConstants.NODE);
        description = getTextContentOrEmptyString(tempNode);

        tempNode = (Node) xPath.evaluate("wcs:name", node, XPathConstants.NODE);
        name = getTextContentOrEmptyString(tempNode);

        tempNode = (Node) xPath.evaluate("wcs:label", node, XPathConstants.NODE);
        label = getTextContentOrEmptyString(tempNode);

        //We will get a list of <requestResponseCRSs> OR a list
        //of <requestCRSs> and <responseCRSs>
        //Lets parse one or the other
        tempNodeList = (NodeList) xPath.evaluate("wcs:supportedCRSs/wcs:requestResponseCRSs", node, XPathConstants.NODESET);
        if (tempNodeList.getLength() > 0) {
            supportedRequestCRSs = new String[tempNodeList.getLength()];
            supportedResponseCRSs = new String[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                supportedRequestCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
                supportedResponseCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
            }
        } else {
            tempNodeList = (NodeList) xPath.evaluate("wcs:supportedCRSs/wcs:requestCRSs", node, XPathConstants.NODESET);
            supportedRequestCRSs = new String[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                supportedRequestCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
            }

            tempNodeList = (NodeList) xPath.evaluate("wcs:supportedCRSs/wcs:responseCRSs", node, XPathConstants.NODESET);
            supportedResponseCRSs = new String[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                supportedResponseCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
            }
        }

        tempNodeList = (NodeList)xPath.evaluate("wcs:supportedFormats/wcs:formats", node, XPathConstants.NODESET);
        supportedFormats = new String[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            supportedFormats[i] = getTextContentOrEmptyString(tempNodeList.item(i));
        }

        tempNodeList = (NodeList) xPath.evaluate("wcs:supportedInterpolations/wcs:interpolationMethod", node, XPathConstants.NODESET);
        supportedInterpolations = new String[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            supportedInterpolations[i] = getTextContentOrEmptyString(tempNodeList.item(i));
        }

        tempNodeList = (NodeList) xPath.evaluate("wcs:supportedCRSs/wcs:nativeCRSs", node, XPathConstants.NODESET);
        nativeCRSs = new String[tempNodeList.getLength()];
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            nativeCRSs[i] = getTextContentOrEmptyString(tempNodeList.item(i));
        }


        //Parse our spatial domain (only grab gml:Envelopes and wcs:EnvelopeWithTimePeriod
        tempNode = (Node) xPath.evaluate("wcs:domainSet/wcs:spatialDomain", node, XPathConstants.NODE);
        if (tempNode != null) {
            List<SpatialDomain> parsableItems = new ArrayList<SpatialDomain>();
            tempNodeList = (NodeList) xPath.evaluate(XPATHALLCHILDREN, tempNode, XPathConstants.NODESET);

            //Attempt to parse spatial domains (we don't support every type so we may get some exceptions)
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                try {
                    parsableItems.add(SpatialDomainFactory.parseFromNode(tempNodeList.item(i)));
                } catch (IllegalArgumentException ex) {
                    logger.debug("Unsupported spatial domain - Skipping: " + ex.getMessage());
                }
            }

            spatialDomain = parsableItems.toArray(new SpatialDomain[parsableItems.size()]);
        }

        //Get the temporal range (which is optional)
        tempNode = (Node) xPath.evaluate("wcs:domainSet/wcs:temporalDomain", node, XPathConstants.NODE);
        if (tempNode != null) {
            tempNodeList = (NodeList)xPath.evaluate(XPATHALLCHILDREN, tempNode, XPathConstants.NODESET);
            temporalDomain = new TemporalDomain[tempNodeList.getLength()];
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                temporalDomain[i] = TemporalDomainFactory.parseFromNode(tempNodeList.item(i));
            }
        }

        tempNode = (Node) xPath.evaluate("wcs:rangeSet/wcs:RangeSet", node, XPathConstants.NODE);
        rangeSet = new RangeSetImpl(tempNode, xPath);
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
     * Gets the Unordered list of identifiers of the CRSs in which the server stores this data,
     * that is, the CRS(s) in which data can be obtained without any distortion or degradation.
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
     * Possible values...
     * + nearest neighbor (default)
     *
     * These are defined in ISO 19123 (Schema for Coverage Geometry and Functions), Annex B.
     * + bilinear
     * + bicubic
     * + lost area
     * + barycentric
     *
     * No interpolation is available; requests must be for locations that are among the original domain locations.
     * +none
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
    public SpatialDomain[] getSpatialDomain() {
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
     * Parses the XML string response from a DescribeCoverage request into a list of DescribeCoverageRecords.
     *
     * @param inXml the in xml
     * @return the describe coverage record[]
     * @throws Exception the exception
     */
    public static DescribeCoverageRecord[] parseRecords(String inXml) throws Exception {
        Document doc = DOMUtil.buildDomFromString(inXml);

        //This is to make sure we actually receive a valid response
        OWSExceptionParser.checkForExceptionResponse(doc);

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new WCSNamespaceContext());

        String serviceTitleExpression = "/wcs:CoverageDescription/wcs:CoverageOffering";

        NodeList nodes = (NodeList) xPath.evaluate(serviceTitleExpression, doc, XPathConstants.NODESET);

        DescribeCoverageRecord[] records = new DescribeCoverageRecord[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++ ) {
            records[i] = new DescribeCoverageRecord(nodes.item(i), xPath);
        }

        return records;
    }
}
