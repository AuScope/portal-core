package org.auscope.portal.core.services.responses.csw;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformer.Scope;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class for providing methods to transform between CSWRecord and a DOM ANZLIC representation.
 *
 * @author Josh Vote
 */
public class CSWRecordTransformer {
    public static final String TEMPLATE_FILE = "MD_MetadataTemplate.xml";
    protected final Log logger = LogFactory.getLog(getClass());

    protected Document document;
    protected Node mdMetadataNode;
    protected OgcServiceProviderType serverType = OgcServiceProviderType.Default;

    protected static final String DATETIMEFORMATSTRING = "yyyy-MM-dd'T'HH:mm:ss";
    protected static final String DATEFORMATSTRING = "yyyy-MM-dd";

    protected enum Scope {
        service, dataset
    }

    protected static final CSWNamespaceContext nc = new CSWNamespaceContext();
    protected static final String SERVICEIDENTIFICATIONPATH = "gmd:identificationInfo/srv:SV_ServiceIdentification";
    protected static final String DATAIDENTIFICATIONPATH = "gmd:identificationInfo/gmd:MD_DataIdentification";
    protected static final String TITLEEXPRESSION = "/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";

    protected static final String DATESTAMPEXPRESSION = "gmd:dateStamp/gco:Date";
    protected static final String DATETIMESTAMPEXPRESSION = "gmd:dateStamp/gco:DateTime";
    protected static final String SCOPEEXPRESSION = "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";
    protected static final String ABSTRACTEXPRESSION = "/gmd:abstract/gco:CharacterString";

    protected static final String CONTACTEXPRESSION = "gmd:contact/gmd:CI_ResponsibleParty";
    protected static final String RESOURCEPROVIDEREXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[./gmd:role[./gmd:CI_RoleCode[@codeListValue = 'resourceProvider']]]/gmd:organisationName/gco:CharacterString";
    protected static final String FILEIDENTIFIEREXPRESSION = "gmd:fileIdentifier/gco:CharacterString";
    protected static final String PARENTIDENTIFIEREXPRESSION = "gmd:parentIdentifier/gco:CharacterString";
    protected static final String ONLINETRANSFERSEXPRESSION = "gmd:distributionInfo/gmd:MD_Distribution/descendant::gmd:onLine";
    protected static final String BBOXEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
    protected static final String KEYWORDLISTEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString";
    protected static final String DATASETURIEXPRESSION = "gmd:dataSetURI/gco:CharacterString";
    protected static final String SUPPLEMENTALINFOEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:supplementalInformation/gco:CharacterString";
    protected static final String LANGUAGEEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gco:CharacterString";
    protected static final String OTHERCONSTRAINTSEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/(gmd:otherConstraints/gco:CharacterString | gmd:useLimitation/gco:CharacterString | gmd:accessConstraints/gmd:MD_RestrictionCode[(text())]/@codeList | gmd:reference/gmd:CI_Citation/gmd:title/gco:CharacterString)";  
    protected static final String USELIMITCONSTRAINTSEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString";
    protected static final String ACCESSCONSTRAINTSEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode[(text())]/@codeList";    
    protected static final String DATAQUALITYSTATEMENTEXPRESSION = "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString";
    protected static final String LAYERNAME = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString";

    protected static final String SCALEDENOMINATOR = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer";

    private static final String ONLINEDATASETTRANSFERSEXPRESSION = "gmd:distributionInfo/gmd:MD_Distribution/descendant::gmd:onLine";
    
    /**
     * Creates a new instance of this class and generates an empty document that will be used for constructing DOM.
     * @throws ParserConfigurationException 
     */
    public CSWRecordTransformer() throws PortalServiceException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new PortalServiceException(e.getMessage(),e);
        }

        //Build an empty document and a simple mdMetadataNode template
        this.document = builder.newDocument();
        Element mdMetadata = createChildNode(document, nc.getNamespaceURI("gmd"), "MD_Metadata");

        Iterator<String> prefixIterator = nc.getPrefixIterator();
        while (prefixIterator.hasNext()) {
            String prefix = prefixIterator.next();
            mdMetadata.setAttributeNS("", prefix, nc.getNamespaceURI(prefix));
        }

        this.mdMetadataNode = mdMetadata;
    }

    /**
     * Creates a new instance of this class which will draw from the specified gmd:MD_Metadata Node representation as a template
     *
     * @param rootNode
     */
    public CSWRecordTransformer(Node mdMetadataNode,OgcServiceProviderType serverType) {
        this.document = mdMetadataNode.getOwnerDocument();
        this.mdMetadataNode = mdMetadataNode;
        this.serverType = serverType;
    }

    /**
     * Helper method for creating child XML elements using the template document
     *
     * @param parent
     *            The node who will be the parent of this node
     * @param namespaceUri
     *            The URI of this node
     * @param name
     *            The name of this node
     * @return
     */
    protected Element createChildNode(Node parent, String namespaceUri, String name) {
        Element child = this.document.createElementNS(namespaceUri, name);

        parent.appendChild(child);

        return child;
    }

    /**
     * Helper method for appending a child element containing a single gco:DateTime element
     *
     * @param parent
     * @param namespaceUri
     * @param name
     * @param value
     */
    protected void appendChildDate(Node parent, String namespaceUri, String name, Date value) {
        Element child = createChildNode(parent, namespaceUri, name);
        Node characterStr = createChildNode(child, nc.getNamespaceURI("gco"), "DateTime");

        if (value == null) {
            child.setAttributeNS(nc.getNamespaceURI("gco"), "nilReason", "missing");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMATSTRING);
            characterStr.setTextContent(sdf.format(value));
        }
    }

    /**
     * Helper method for appending a child element containing a single gco:Decimal element
     *
     * @param parent
     * @param namespaceUri
     * @param name
     * @param value
     */
    protected void appendChildDecimal(Node parent, String namespaceUri, String name,
            double value) {
        Element child = createChildNode(parent, namespaceUri, name);
        Node characterStr = createChildNode(child, nc.getNamespaceURI("gco"), "Decimal");
        characterStr.setTextContent(Double.toString(value));
    }

    /**
     * Helper method for appending a child element containing a single gco:CharacterString element
     *
     * @param parent
     *            where it will be appended
     * @param namespaceUri
     * @param name
     * @param value
     */
    protected void appendChildCharacterString(Node parent, String namespaceUri, String name,
            String value) {
        Element child = createChildNode(parent, namespaceUri, name);
        Node characterStr = createChildNode(child, nc.getNamespaceURI("gco"), "CharacterString");
        if (value == null || value.isEmpty()) {
            child.setAttributeNS(nc.getNamespaceURI("gco"), "nilReason", "missing");
        } else {
            characterStr.setTextContent(value);
        }
    }

    /**
     * Helper method for appending a child element containing a single gmd:EX_Extent element
     *
     * @param parent
     * @param namespaceUri
     * @param name
     * @param geoEl
     */
    protected void appendChildExtent(Node parent, String namespaceUri, String name,
            CSWGeographicBoundingBox bbox) {
        Node child = createChildNode(parent, namespaceUri, name);
        Node exExtent = createChildNode(child, nc.getNamespaceURI("gmd"), "EX_Extent");

        Node geoEl = createChildNode(exExtent, nc.getNamespaceURI("gmd"), "geographicElement");
        Node geoBbox = createChildNode(geoEl, nc.getNamespaceURI("gmd"), "EX_GeographicBoundingBox");

        appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "westBoundLongitude", bbox.getWestBoundLongitude());
        appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "eastBoundLongitude", bbox.getEastBoundLongitude());
        appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "southBoundLatitude", bbox.getSouthBoundLatitude());
        appendChildDecimal(geoBbox, nc.getNamespaceURI("gmd"), "northBoundLatitude", bbox.getNorthBoundLatitude());
    }

    /**
     * Helper method for appending a child element containing a single gmd:CI_Citation element
     *
     * @param parent
     * @param namespaceUri
     * @param name
     * @param resource
     */
    protected void appendChildOnlineResource(Node parent, String namespaceUri, String name,
            AbstractCSWOnlineResource onlineResource) {
        Node child = createChildNode(parent, namespaceUri, name);
        Node ciOnlineResource = createChildNode(child, nc.getNamespaceURI("gmd"), "CI_OnlineResource");

        //Add linkage
        Node linkage = createChildNode(ciOnlineResource, nc.getNamespaceURI("gmd"), "linkage");
        Node url = createChildNode(linkage, nc.getNamespaceURI("gmd"), "URL");
        url.setTextContent(onlineResource.getLinkage().toString());

        //Add protocol
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "protocol",
                onlineResource.getProtocol());

        //Add application profile
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "applicationProfile",
                onlineResource.getApplicationProfile());

        //Add name
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "name", onlineResource.getName());

        //Add description
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "description",
                onlineResource.getDescription());

        parent.appendChild(child);
    }

    /**
     * Helper method for appending a child element containing a single gmd:CI_Contact element
     *
     * @param contact
     *            the CSWContact to append
     * @return
     */
    private void appendChildContact(Node parent, String namespaceUri, String name,
            CSWContact contact) {
        Node child = createChildNode(parent, namespaceUri, name);
        Element ciContactNode = createChildNode(child, nc.getNamespaceURI("gmd"), "CI_Contact");

        Element phone = createChildNode(ciContactNode, nc.getNamespaceURI("gmd"), "phone");
        Element telephone = createChildNode(phone, nc.getNamespaceURI("gmd"), "CI_Telephone");
        appendChildCharacterString(telephone, nc.getNamespaceURI("gmd"), "voice", contact.getTelephone());
        appendChildCharacterString(telephone, nc.getNamespaceURI("gmd"), "facsimile", contact.getFacsimile());

        Element address = createChildNode(ciContactNode, nc.getNamespaceURI("gmd"), "address");
        Element ciAddress = createChildNode(address, nc.getNamespaceURI("gmd"), "CI_Address");
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "deliveryPoint",
                contact.getAddressDeliveryPoint());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "city", contact.getAddressCity());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "administrativeArea",
                contact.getAddressAdministrativeArea());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "postalCode", contact.getAddressPostalCode());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "country", contact.getAddressCountry());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "electronicMailAddress",
                contact.getAddressEmail());

        if (contact.getOnlineResource() != null) {
            appendChildOnlineResource(ciContactNode, nc.getNamespaceURI("gmd"), "onlineResource",
                    contact.getOnlineResource());
        }
    }

    /**
     * Transforms the specified CSWResponsibleParty back into a CI_ResponsibleParty element represented by a Node
     *
     * @param rp
     *            the CSWResponsibleParty to transform
     * @param rpNode
     *            a CI_ResponsibleParty element which will be populated with rp
     * @return
     */
    private void appendChildResponsibleParty(Node parent, String namespaceUri, String name,
            CSWResponsibleParty rp) {
        Node child = createChildNode(parent, namespaceUri, name);
        Node rpNode = createChildNode(child, nc.getNamespaceURI("gmd"), "CI_ResponsibleParty");

        appendChildCharacterString(rpNode, nc.getNamespaceURI("gmd"), "individualName", rp.getIndividualName());
        appendChildCharacterString(rpNode, nc.getNamespaceURI("gmd"), "organisationName", rp.getOrganisationName());
        appendChildCharacterString(rpNode, nc.getNamespaceURI("gmd"), "positionName", rp.getPositionName());

        //Add our contact info
        if (rp.getContactInfo() != null) {
            appendChildContact(rpNode, nc.getNamespaceURI("gmd"), "contactInfo", rp.getContactInfo());
        }

        //Add our constant role node
        Element role = createChildNode(rpNode, nc.getNamespaceURI("gmd"), "role");
        Element roleCode = createChildNode(role, nc.getNamespaceURI("gmd"), "CI_RoleCode");
        roleCode.setAttributeNS("", "codeList",
                "http://www.isotc211.org/2005/resources/codelist/codeList.xml#CI_RoleCode");
        roleCode.setAttributeNS("", "codeListValue", "pointOfContact");
    }

    /**
     * Transforms the specified CSWRecord back into a MD_Metadata element represented by Node.
     *
     * The transformation is built from the internal template specified at construction time.
     *
     * The internal template will NOT be modified by this function
     *
     * @param record
     * @return
     */
    public Node transformToNode(CSWRecord record) {
        Node root = this.mdMetadataNode.cloneNode(false);

        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "fileIdentifier", record.getFileIdentifier());
        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "language", record.getLanguage());

        //Hardcode our character set code
        Node characterSet = createChildNode(root, nc.getNamespaceURI("gmd"), "characterSet");
        Element mdCharacterSetCode = createChildNode(characterSet, nc.getNamespaceURI("gmd"), "MD_CharacterSetCode");
        mdCharacterSetCode.setAttributeNS("", "codeListValue", "utf8");
        mdCharacterSetCode.setAttributeNS("", "codeList",
                "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_CharacterSetCode");

        CSWResponsibleParty responsibleParty = record.getContact();
        if (responsibleParty != null) {
            appendChildResponsibleParty(root, nc.getNamespaceURI("gmd"), "contact", responsibleParty);
        }

        appendChildDate(root, nc.getNamespaceURI("gmd"), "dateStamp", record.getDate());
        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "metadataStandardName", "ISO 19115:2003/19139");
        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "metadataStandardVersion", "1.0");

        //We manually construct our mdDataIdentification element as CSWRecords aren't that normalised yet...
        Node identificationInfo = createChildNode(root, nc.getNamespaceURI("gmd"), "identificationInfo");
        Node mdDataIdentification = createChildNode(identificationInfo, nc.getNamespaceURI("gmd"),
                "MD_DataIdentification");

        //DataIdentification -> citation (manually built)
        Node citation = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"), "citation");
        Node ciCitation = createChildNode(citation, nc.getNamespaceURI("gmd"), "CI_Citation");
        appendChildCharacterString(ciCitation, nc.getNamespaceURI("gmd"), "title", record.getServiceName());
        Node ciCitationDate = createChildNode(ciCitation, nc.getNamespaceURI("gmd"), "date");
        Node ciCitationCIDate = createChildNode(ciCitationDate, nc.getNamespaceURI("gmd"), "CI_Date");
        appendChildDate(ciCitationCIDate, nc.getNamespaceURI("gmd"), "date", record.getDate());
        Node ciCitationCIDateType = createChildNode(ciCitationCIDate, nc.getNamespaceURI("gmd"), "dateType");
        Element ciCitationCIDateTypeCode = createChildNode(ciCitationCIDateType, nc.getNamespaceURI("gmd"),
                "CI_DateTypeCode");
        ciCitationCIDateTypeCode.setAttributeNS("", "codeListValue", "creation");
        ciCitationCIDateTypeCode.setAttributeNS("", "codeList",
                "http://www.isotc211.org/2005/resources/codelist/codeList.xml#CI_DateTypeCode");

        //DataIdentification -> abstract
        appendChildCharacterString(mdDataIdentification, nc.getNamespaceURI("gmd"), "abstract",
                record.getDataIdentificationAbstract());

        //DataIdentification -> status
        Node dataIdStatus = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"), "status");
        Element dataIdStatusCode = createChildNode(dataIdStatus, nc.getNamespaceURI("gmd"), "MD_ProgressCode");
        dataIdStatusCode.setAttributeNS("", "codeListValue", "completed");
        dataIdStatusCode.setAttributeNS("", "codeList",
                "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_ProgressCode");

        //DataIdentification -> resourceConstraints
        Node dataIdResourceConstraints = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"),
                "resourceConstraints");
        Node dataIdLegalConstraints = createChildNode(dataIdResourceConstraints, nc.getNamespaceURI("gmd"),
                "MD_LegalConstraints");
        String[] legalConstraints = record.getConstraints();
        if (legalConstraints != null) {
            for (String constraint : legalConstraints) {            	
                appendChildCharacterString(dataIdLegalConstraints, nc.getNamespaceURI("gmd"), "otherConstraints",
                        constraint);
            }
        }
    	//added code to include tag for gmd:useLimitation 
        String[] useLimitations = record.getUseLimitConstraints();
        if (useLimitations != null) {
            for (String useLimitation : useLimitations) {

                appendChildCharacterString(dataIdLegalConstraints, nc.getNamespaceURI("gmd"), "useLimitation",
                		useLimitation);
            }
        }
       //added code to include tag for gmd:accessConstraints
        String[] accessConstraints = record.getAccessConstraints();
        if (accessConstraints != null) {
            for (String accessConstraint : accessConstraints) {             
                appendChildCharacterString(dataIdLegalConstraints, nc.getNamespaceURI("gmd"), "accessConstraints",
                		accessConstraint);
            }
        }

        //DataIdentification -> pointOfContact
        if (responsibleParty != null) {
            appendChildResponsibleParty(mdDataIdentification, nc.getNamespaceURI("gmd"), "pointOfContact",
                    responsibleParty);
        }

        //DataIdentification -> descriptiveKeywords
        Node dataIdDescriptiveKeywords = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"),
                "descriptiveKeywords");
        Node dataIdMDKeywords = createChildNode(dataIdDescriptiveKeywords, nc.getNamespaceURI("gmd"), "MD_Keywords");
        String[] keywords = record.getDescriptiveKeywords();
        if (keywords != null) {
            for (String keyword : keywords) {
                appendChildCharacterString(dataIdMDKeywords, nc.getNamespaceURI("gmd"), "keyword", keyword);
            }
        }
        Node dataIdMDKeywordsType = createChildNode(dataIdMDKeywords, nc.getNamespaceURI("gmd"), "type");
        Element dataIdMDKeywordsTypeCode = createChildNode(dataIdMDKeywordsType, nc.getNamespaceURI("gmd"),
                "MD_KeywordTypeCode");
        dataIdMDKeywordsTypeCode.setAttributeNS("", "codeListValue", "theme");
        dataIdMDKeywordsTypeCode.setAttributeNS("", "codeList",
                "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_KeywordTypeCode");

        //MD_Metadata -> dataSetURI
        String[] datasetURIs = record.getDataSetURIs();
        if (datasetURIs != null) {
            for (String datasetURI : datasetURIs) {
                appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "dataSetURI", datasetURI);
            }
        }

        //DataIdentification -> language
        appendChildCharacterString(mdDataIdentification, nc.getNamespaceURI("gmd"), "language", record.getLanguage());

        //DataIdentification -> extent
        CSWGeographicElement[] geoEls = record.getCSWGeographicElements();
        if (geoEls != null) {
            for (CSWGeographicElement geoEl : geoEls) {
                if (geoEl instanceof CSWGeographicBoundingBox) {
                    appendChildExtent(mdDataIdentification, nc.getNamespaceURI("gmd"), "extent",
                            (CSWGeographicBoundingBox) geoEl);
                }
            }
        }

        //DataIdentification -> supplementalInformation
        appendChildCharacterString(mdDataIdentification, nc.getNamespaceURI("gmd"), "supplementalInformation",
                record.getSupplementalInformation());

        //Online resources
        AbstractCSWOnlineResource[] onlineResources = record.getOnlineResources();
        if (onlineResources != null && onlineResources.length > 0) {
            Node distrInfo = createChildNode(root, nc.getNamespaceURI("gmd"), "distributionInfo");
            Node mdDistribution = createChildNode(distrInfo, nc.getNamespaceURI("gmd"), "MD_Distribution");
            Node transferOptions = createChildNode(mdDistribution, nc.getNamespaceURI("gmd"), "transferOptions");
            Node mdDigitalTransferOptions = createChildNode(transferOptions, nc.getNamespaceURI("gmd"),
                    "MD_DigitalTransferOptions");

            for (AbstractCSWOnlineResource onlineResource : onlineResources) {
                appendChildOnlineResource(mdDigitalTransferOptions, nc.getNamespaceURI("gmd"), "onLine", onlineResource);
            }
        }

        //Data Quality (partially hardcoded)
        Node dataQualityInfo = createChildNode(root, nc.getNamespaceURI("gmd"), "dataQualityInfo");
        Node dqDataQuality = createChildNode(dataQualityInfo, nc.getNamespaceURI("gmd"), "DQ_DataQuality");
        Node dataQualityScope = createChildNode(dqDataQuality, nc.getNamespaceURI("gmd"), "scope");
        Node dqDataQualityScope = createChildNode(dataQualityScope, nc.getNamespaceURI("gmd"), "DQ_Scope");
        Node dataQualityScopeLevel = createChildNode(dqDataQualityScope, nc.getNamespaceURI("gmd"), "level");
        Element dataQualityScopeLevelCode = createChildNode(dataQualityScopeLevel, nc.getNamespaceURI("gmd"),
                "MD_ScopeCode");
        dataQualityScopeLevelCode.setAttributeNS("", "codeListValue", "dataset");
        dataQualityScopeLevelCode.setAttributeNS("", "codeList",
                "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_ScopeCode");
        if (record.getDataQualityStatement() != null && !record.getDataQualityStatement().isEmpty()) {
            Node dqLineage = createChildNode(dqDataQuality, nc.getNamespaceURI("gmd"), "lineage");
            Node dqLILineage = createChildNode(dqLineage, nc.getNamespaceURI("gmd"), "LI_Lineage");
            appendChildCharacterString(dqLILineage, nc.getNamespaceURI("gmd"), "statement",
                    record.getDataQualityStatement());
        }

        return root;
    }

    /**
     * Helper method for evaluating an xpath string on a particular node and returning the result as a string (or null)
     *
     * @param node
     * @param xPath
     *            A valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    protected static String evalXPathString(Node node, String xPath) throws XPathExpressionException {
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (String) expression.evaluate(node, XPathConstants.STRING);
    }

    /**
     * Helper method for evaluating an xpath string on a particular node and returning the result as a (possible empty) list of matching nodes
     *
     * @param node
     * @param xPath
     *            A valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    protected NodeList evalXPathNodeList(Node node, String xPath) throws XPathExpressionException {
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (NodeList) expression.evaluate(node, XPathConstants.NODESET);
    }

    /**
     * Helper method for evaluating an xpath string on a particular node and returning the result as a (possible empty) DOM node
     *
     * @param node
     * @param xPath
     *            A valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    protected Node evalXPathNode(Node node, String xPath) throws XPathExpressionException {
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (Node) expression.evaluate(node, XPathConstants.NODE);
    }

    /**
     * Iterates through the record's online resource list and removes any pairs of online resources that match on: 1) URL (sans query string) 2) name 3)
     * protocol
     */
    protected static List<AbstractCSWOnlineResource> removeDuplicateOnlineResources(List<AbstractCSWOnlineResource> resources) {
        for (int i = 0; i < resources.size(); i++) {
            AbstractCSWOnlineResource resource = resources.get(i);
            boolean foundMatching = false;
            for (int j = i + 1; j < resources.size() && !foundMatching; j++) {
                AbstractCSWOnlineResource cmp = resources.get(j);

                //Do the easy check first
                if (resource.getName().equals(cmp.getName()) &&
                        resource.getType() == cmp.getType()) {
                    //Then test the URL host + path
                    if (resource.getLinkage() != null && cmp.getLinkage() != null) {
                        String resourceUrl = resource.getLinkage().toString().split("\\?")[0];
                        String cmpUrl = cmp.getLinkage().toString().split("\\?")[0];

                        if (resourceUrl.equals(cmpUrl)) {
                            resources.remove(j--);
                        }
                    }
                }
            }
        }

        return resources;
    }

    /**
     * Creates a new CSWRecord instance parsed from the internal template of this class
     *
     * Throws an exception if the internal template cannot be parsed correctly
     *
     * @return
     * @throws XPathExpressionException
     */
    public CSWRecord transformToCSWRecord() throws XPathExpressionException {
        logger.info("------- calling transformToCSWRecord from CSWRecordTransformer");

        return transformToCSWRecord(new CSWRecord("", "", "", "", new AbstractCSWOnlineResource[0],
                new CSWGeographicElement[0]));
    }
        
    /**
     * Writes to an existing CSWRecord instance with data parsed from the internal template of this class
     *
     * Throws an exception if the internal template cannot be parsed correctly
     *
     * @return
     * @throws XPathExpressionException
     */
    protected CSWRecord transformToCSWRecord(CSWRecord record) throws XPathExpressionException {
    	if (this.serverType == OgcServiceProviderType.PyCSW) {
    		return new PyCSWHelper().transform(record);
    	} else if (this.serverType == OgcServiceProviderType.GeoServer) {
    		return new GeoServerHelper().transform(record);
    	}
        logger.info("--------- start Normal.transform ");
   	
        NodeList tempNodeList = null;

        //Parse our simple strings
        Node scopeNode = evalXPathNode(this.mdMetadataNode, SCOPEEXPRESSION);
        String recordType = scopeNode != null ? scopeNode.getNodeValue() : null;

        String identificationPath = null;
        if (Scope.service.toString().equals(recordType)) {
            identificationPath = SERVICEIDENTIFICATIONPATH;
            record.setService(true);
        } else {
            identificationPath = DATAIDENTIFICATIONPATH;
        }

        record.setServiceName(evalXPathString(this.mdMetadataNode, identificationPath + TITLEEXPRESSION));
        record.setDataIdentificationAbstract(evalXPathString(this.mdMetadataNode, identificationPath + ABSTRACTEXPRESSION));

        record.setFileIdentifier(evalXPathString(this.mdMetadataNode, FILEIDENTIFIEREXPRESSION));
        record.setParentIdentifier(evalXPathString(this.mdMetadataNode, PARENTIDENTIFIEREXPRESSION));
        record.setSupplementalInformation(evalXPathString(this.mdMetadataNode, SUPPLEMENTALINFOEXPRESSION));
        record.setLanguage(evalXPathString(this.mdMetadataNode, LANGUAGEEXPRESSION));
        record.setDataQualityStatement(evalXPathString(this.mdMetadataNode, DATAQUALITYSTATEMENTEXPRESSION));
        record.setLayerName(evalXPathString(this.mdMetadataNode, LAYERNAME));

        String resourceProvider = evalXPathString(this.mdMetadataNode, RESOURCEPROVIDEREXPRESSION);
        if (resourceProvider == null || resourceProvider.isEmpty()) {
            resourceProvider = "Unknown";
        }
        record.setResourceProvider(resourceProvider);

        String dateStampString = evalXPathString(this.mdMetadataNode, DATETIMESTAMPEXPRESSION);
        if (dateStampString != null && !dateStampString.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMATSTRING);
                record.setDate(sdf.parse(dateStampString));
            } catch (Exception ex) {
                logger.debug(String.format("Unable to parse date for serviceName='%1$s' %2$s", record.getServiceName(),
                        ex));
            }
        }

        //There can be multiple gmd:onLine elements (which contain a number of fields we want)
        tempNodeList = evalXPathNodeList(this.mdMetadataNode, ONLINETRANSFERSEXPRESSION);
        List<AbstractCSWOnlineResource> resources = new ArrayList<>();
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            try {
                Node onlineNode = tempNodeList.item(i);
                resources.add(CSWOnlineResourceFactory.parseFromNode(onlineNode, null)); // no layer name found only from Thredds server.
            } catch (IllegalArgumentException ex) {
                logger.debug(String.format("Unable to parse online resource for serviceName='%1$s' %2$s",
                        record.getServiceName(), ex));
            }
        }
        removeDuplicateOnlineResources(resources);
        record.setOnlineResources(resources.toArray(new AbstractCSWOnlineResource[resources.size()]));

        //Parse our bounding boxes (if they exist). If any are unparsable, don't worry and just continue
        tempNodeList = evalXPathNodeList(this.mdMetadataNode, BBOXEXPRESSION);
        if (tempNodeList != null && tempNodeList.getLength() > 0) {
            List<CSWGeographicElement> elList = new ArrayList<>();
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                try {
                    Node geographyNode = tempNodeList.item(i);
                    elList.add(CSWGeographicBoundingBox.fromGeographicBoundingBoxNode(geographyNode));
                } catch (Exception ex) {
                    logger.debug(String.format(
                            "Unable to parse CSWGeographicBoundingBox resource for serviceName='%1$s' %2$s",
                            record.getServiceName(), ex));
                }
            }
            record.setCSWGeographicElements(elList.toArray(new CSWGeographicElement[elList.size()]));
        }

        //Parse the descriptive keywords
        tempNodeList = evalXPathNodeList(this.mdMetadataNode, KEYWORDLISTEXPRESSION);
        if (tempNodeList != null && tempNodeList.getLength() > 0) {
            List<String> keywords = new ArrayList<>();
            Node keyword;
            for (int j = 0; j < tempNodeList.getLength(); j++) {
                keyword = tempNodeList.item(j);
                keywords.add(keyword.getTextContent());
            }
            record.setDescriptiveKeywords(keywords.toArray(new String[keywords.size()]));
        }

        //Parse the dataset URIs
        tempNodeList = evalXPathNodeList(this.mdMetadataNode, DATASETURIEXPRESSION);
        if (tempNodeList != null && tempNodeList.getLength() > 0) {
            List<String> datasetURIs = new ArrayList<>();
            Node datasetURI;
            for (int j = 0; j < tempNodeList.getLength(); j++) {
                datasetURI = tempNodeList.item(j);
                datasetURIs.add(datasetURI.getTextContent());
            }
            record.setDataSetURIs(datasetURIs.toArray(new String[datasetURIs.size()]));
        }

        Node tempNode = evalXPathNode(this.mdMetadataNode, CONTACTEXPRESSION);
        if (tempNode != null) {
            try {
                CSWResponsibleParty respParty = CSWResponsiblePartyFactory.generateResponsiblePartyFromNode(tempNode);
                record.setContact(respParty);
            } catch (Exception ex) {
                logger.debug(String.format("Unable to parse contact for serviceName='%1$s' %2$s",
                        record.getServiceName(), ex));
            }
        }

        //Parse any legal constraints
        tempNodeList = evalXPathNodeList(this.mdMetadataNode, OTHERCONSTRAINTSEXPRESSION);
        if (tempNodeList != null && tempNodeList.getLength() > 0) {
            List<String> constraintsList = new ArrayList<>();
            Node constraint;
            for (int j = 0; j < tempNodeList.getLength(); j++) {
                constraint = tempNodeList.item(j);
                constraintsList.add(constraint.getTextContent());
            }
            record.setConstraints(constraintsList.toArray(new String[constraintsList.size()]));
        }
        
        // added code to parse use limit constraints
        tempNodeList = evalXPathNodeList(this.mdMetadataNode, USELIMITCONSTRAINTSEXPRESSION);
        if (tempNodeList != null && tempNodeList.getLength() > 0) {
            List<String> useLimitConstraintsList = new ArrayList<>();
            Node useLimitConstraint;
            for (int j = 0; j < tempNodeList.getLength(); j++) {
            	useLimitConstraint = tempNodeList.item(j);
                useLimitConstraintsList.add(useLimitConstraint.getTextContent());
            }
            record.setUseLimitConstraints(useLimitConstraintsList.toArray(new String[useLimitConstraintsList.size()]));
        }
        
       //added code to parse access constraints
        tempNodeList = evalXPathNodeList(this.mdMetadataNode, ACCESSCONSTRAINTSEXPRESSION);
        if (tempNodeList != null && tempNodeList.getLength() > 0) {
            List<String> accessConstraintsList = new ArrayList<>();
            Node accessConstraint;
            for (int j = 0; j < tempNodeList.getLength(); j++) {
            	accessConstraint = tempNodeList.item(j);
                accessConstraintsList.add(accessConstraint.getTextContent());
            }
            record.setAccessConstraints(accessConstraintsList.toArray(new String[accessConstraintsList.size()]));
        }

        tempNodeList = evalXPathNodeList(this.mdMetadataNode, SCALEDENOMINATOR);
        if (tempNodeList != null && tempNodeList.getLength() > 0) {

            List<Double> scaleRange = new ArrayList<>();
            Node scaleDenominator;
            for (int j = 0; j < tempNodeList.getLength(); j++) {
                scaleDenominator = tempNodeList.item(j);
                try {
                   scaleRange.add(Double.parseDouble(scaleDenominator.getTextContent()));
                } catch (Exception ex) {
                    logger.debug(String.format("Unable to parse scale denominator for serviceName='%1$s' %2$s",
                            record.getServiceName(), ex));
                }
            }

            if (!scaleRange.isEmpty()) {
                record.setMinScale(Collections.min(scaleRange));
                if (scaleRange.size() > 1) {
                    record.setMaxScale(Collections.max(scaleRange));
                }
            }
        }
        
        return record;
    }
    
    private class PyCSWHelper {
    	private final String[] FIXED_DIMENSION_NAMES = {"time", "longitude", "lon", "latitude", "lat", "transverse_mercator", "crs"}; 
        private final String THREDDSLAYERNAME = "gmd:contentInfo/gmi:MI_CoverageDescription/gmd:dimension/gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString";
        private final String PYCSW_ONLINETRANSFERSEXPRESSION = "gmd:identificationInfo/srv:SV_ServiceIdentification/descendant::srv:connectPoint";
        
        public CSWRecord transform(CSWRecord record) throws XPathExpressionException {
            logger.info("--------- start PyCSWHelper.transform ");
        	
            NodeList tempNodeList = null;

            //Parse our simple strings
            Node scopeNode = evalXPathNode(mdMetadataNode, SCOPEEXPRESSION);
            String recordType = scopeNode != null ? scopeNode.getNodeValue() : null;

            String identificationPath = null;
            if (Scope.service.toString().equals(recordType)) {
                identificationPath = SERVICEIDENTIFICATIONPATH;
                record.setService(true);
            } else {
                identificationPath = DATAIDENTIFICATIONPATH;
            }
            record.setServiceName(evalXPathString(mdMetadataNode, identificationPath + TITLEEXPRESSION));
            
            record.setDataIdentificationAbstract(evalXPathString(mdMetadataNode, identificationPath + ABSTRACTEXPRESSION));

            record.setFileIdentifier(evalXPathString(mdMetadataNode, FILEIDENTIFIEREXPRESSION)); 
            
            record.setParentIdentifier(evalXPathString(mdMetadataNode, PARENTIDENTIFIEREXPRESSION));
            
            record.setSupplementalInformation(evalXPathString(mdMetadataNode, SUPPLEMENTALINFOEXPRESSION));

            record.setLanguage(evalXPathString(mdMetadataNode, LANGUAGEEXPRESSION));
            
            record.setDataQualityStatement(evalXPathString(mdMetadataNode, DATAQUALITYSTATEMENTEXPRESSION));
            
            record.setLayerName(evalXPathString(mdMetadataNode, LAYERNAME));

            String resourceProvider = evalXPathString(mdMetadataNode, RESOURCEPROVIDEREXPRESSION);
            if (resourceProvider == null || resourceProvider.isEmpty()) {
                resourceProvider = "Unknown";
            }
            record.setResourceProvider(resourceProvider);

            transformDate(record, mdMetadataNode, logger);

            String tlname = getThreddsLayerName();
            
            //There can be multiple gmd:onLine elements (which contain a number of fields we want)
            List<AbstractCSWOnlineResource> srvlist = transformSrvNodes(record, 
            			PYCSW_ONLINETRANSFERSEXPRESSION, tlname);
            List<AbstractCSWOnlineResource> datasetlist = transformSrvNodes(record, 
            			ONLINEDATASETTRANSFERSEXPRESSION, tlname);
            
            srvlist.addAll(datasetlist);
            removeDuplicateOnlineResources(srvlist);
            record.setOnlineResources(srvlist.toArray(new AbstractCSWOnlineResource[srvlist.size()]));

            //Parse our bounding boxes (if they exist). If any are unparsable, don't worry and just continue
            tempNodeList = evalXPathNodeList(mdMetadataNode, BBOXEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<CSWGeographicElement> elList = new ArrayList<>();
                for (int i = 0; i < tempNodeList.getLength(); i++) {
                    try {
                        Node geographyNode = tempNodeList.item(i);
                        elList.add(CSWGeographicBoundingBox.fromGeographicBoundingBoxNode(geographyNode));
                    } catch (Exception ex) {
                        logger.info(String.format(
                                "Unable to parse CSWGeographicBoundingBox resource for serviceName='%1$s' %2$s",
                                record.getServiceName(), ex));
                    }
                }
                record.setCSWGeographicElements(elList.toArray(new CSWGeographicElement[elList.size()]));
            }

            //Parse the descriptive keywords
            tempNodeList = evalXPathNodeList(mdMetadataNode, KEYWORDLISTEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> keywords = new ArrayList<>();
                Node keyword;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    keyword = tempNodeList.item(j);
                    keywords.add(keyword.getTextContent());
                }
                record.setDescriptiveKeywords(keywords.toArray(new String[keywords.size()])); // correct!
            }

            //Parse the dataset URIs
            tempNodeList = evalXPathNodeList(mdMetadataNode, DATASETURIEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> datasetURIs = new ArrayList<>();
                Node datasetURI;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    datasetURI = tempNodeList.item(j);
                    datasetURIs.add(datasetURI.getTextContent());
                }
                record.setDataSetURIs(datasetURIs.toArray(new String[datasetURIs.size()]));
            }

            Node tempNode = evalXPathNode(mdMetadataNode, CONTACTEXPRESSION);
            if (tempNode != null) {
                try {
                    CSWResponsibleParty respParty = CSWResponsiblePartyFactory.generateResponsiblePartyFromNode(tempNode);
                    record.setContact(respParty);
                } catch (Exception ex) {
                    logger.debug(String.format("Unable to parse contact for serviceName='%1$s' %2$s",
                            record.getServiceName(), ex));
                }
            }

            //Parse any legal constraints
            tempNodeList = evalXPathNodeList(mdMetadataNode, OTHERCONSTRAINTSEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> constraintsList = new ArrayList<>();
                Node constraint;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    constraint = tempNodeList.item(j);
                    constraintsList.add(constraint.getTextContent());
                }
                record.setConstraints(constraintsList.toArray(new String[constraintsList.size()]));
            }
            
            // added code to parse use limit constraints
            tempNodeList = evalXPathNodeList(mdMetadataNode, USELIMITCONSTRAINTSEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> useLimitConstraintsList = new ArrayList<>();
                Node useLimitConstraint;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                	useLimitConstraint = tempNodeList.item(j);
                    useLimitConstraintsList.add(useLimitConstraint.getTextContent());
                }
                record.setUseLimitConstraints(useLimitConstraintsList.toArray(new String[useLimitConstraintsList.size()]));
            }
            
           //added code to parse access constraints
            tempNodeList = evalXPathNodeList(mdMetadataNode, ACCESSCONSTRAINTSEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> accessConstraintsList = new ArrayList<>();
                Node accessConstraint;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                	accessConstraint = tempNodeList.item(j);
                    accessConstraintsList.add(accessConstraint.getTextContent());
                }
                record.setAccessConstraints(accessConstraintsList.toArray(new String[accessConstraintsList.size()]));
            }

            tempNodeList = evalXPathNodeList(mdMetadataNode, SCALEDENOMINATOR);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {

                List<Double> scaleRange = new ArrayList<>();
                Node scaleDenominator;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    scaleDenominator = tempNodeList.item(j);
                    try {
                       scaleRange.add(Double.parseDouble(scaleDenominator.getTextContent()));
                    } catch (Exception ex) {
                        logger.debug(String.format("Unable to parse scale denominator for serviceName='%1$s' %2$s",
                                record.getServiceName(), ex));
                    }
                }

                if (!scaleRange.isEmpty()) {
                    record.setMinScale(Collections.min(scaleRange));
                    if (scaleRange.size() > 1) {
                        record.setMaxScale(Collections.max(scaleRange));
                    }
                }
            }
            
            return record;
        }
        
        private List<AbstractCSWOnlineResource> transformSrvNodes(CSWRecord record, String expression, String threddsLayerName) 
    			throws XPathExpressionException{
        	NodeList  tempNodeList = evalXPathNodeList(mdMetadataNode, expression);
        	List<AbstractCSWOnlineResource> resources = new ArrayList<>();
        	for (int i = 0; i < tempNodeList.getLength(); i++) {
        		try {
        			Node onlineNode = tempNodeList.item(i);
        			resources.add(CSWOnlineResourceFactory.parseFromNode(onlineNode, threddsLayerName));
        		} catch (IllegalArgumentException ex) {
        			logger.debug(String.format("Unable to parse online resource for serviceName='%1$s' %2$s",
        					record.getServiceName(), ex));
        		}
        	}
        	return resources;
        }

        private boolean isFixedLayerName(String lname) {
        	for (int i= 0; i < FIXED_DIMENSION_NAMES.length; i++) {
    			if (FIXED_DIMENSION_NAMES[i].compareTo(lname) == 0 ) {
    				return true;
    			}
    		}
        	return false;
        }
        
        private String getThreddsLayerName() throws XPathExpressionException {
        	NodeList tempNodeList = evalXPathNodeList(mdMetadataNode, THREDDSLAYERNAME);
        	if (tempNodeList != null && tempNodeList.getLength() > 0) {
        		for (int i = 0; i < tempNodeList.getLength(); i++) {
        			String name = tempNodeList.item(i).getTextContent();
        			if (!isFixedLayerName(name)) {
        				return name;
        			}
        		}
        	} 
        	return null;
        }

    }
    

    private class GeoServerHelper {
        private final String GEOSERVER_ONLINETRANSFERSEXPRESSION = "gmd:identificationInfo/srv:SV_ServiceIdentification/descendant::srv:connectPoint";
        
        public CSWRecord transform(CSWRecord record) throws XPathExpressionException {
            NodeList tempNodeList = null;

            //Parse our simple strings
            Node scopeNode = evalXPathNode(mdMetadataNode, SCOPEEXPRESSION);
            String recordType = scopeNode != null ? scopeNode.getNodeValue() : null;

            String identificationPath = null;
            if (Scope.service.toString().equals(recordType)) {
                identificationPath = SERVICEIDENTIFICATIONPATH;
                record.setService(true);
            } else {
                identificationPath = DATAIDENTIFICATIONPATH;
            }
            record.setServiceName(evalXPathString(mdMetadataNode, identificationPath + TITLEEXPRESSION));
            
            record.setDataIdentificationAbstract(evalXPathString(mdMetadataNode, identificationPath + ABSTRACTEXPRESSION));

            record.setFileIdentifier(evalXPathString(mdMetadataNode, FILEIDENTIFIEREXPRESSION)); 
            
            record.setParentIdentifier(evalXPathString(mdMetadataNode, PARENTIDENTIFIEREXPRESSION));
            
            record.setSupplementalInformation(evalXPathString(mdMetadataNode, SUPPLEMENTALINFOEXPRESSION));

            record.setLanguage(evalXPathString(mdMetadataNode, LANGUAGEEXPRESSION));
            
            record.setDataQualityStatement(evalXPathString(mdMetadataNode, DATAQUALITYSTATEMENTEXPRESSION));
            
            record.setLayerName(evalXPathString(mdMetadataNode, LAYERNAME));

            String resourceProvider = evalXPathString(mdMetadataNode, RESOURCEPROVIDEREXPRESSION);
            if (resourceProvider == null || resourceProvider.isEmpty()) {
                resourceProvider = "Unknown";
            }
            record.setResourceProvider(resourceProvider);

            transformDate(record, mdMetadataNode, logger);

            //There can be multiple gmd:onLine elements (which contain a number of fields we want)
            List<AbstractCSWOnlineResource> srvlist = transformSrvNodes(record, GEOSERVER_ONLINETRANSFERSEXPRESSION);
            List<AbstractCSWOnlineResource> datasetlist = transformSrvNodes(record, ONLINEDATASETTRANSFERSEXPRESSION);
            srvlist.addAll(datasetlist);
            removeDuplicateOnlineResources(srvlist);
            record.setOnlineResources(srvlist.toArray(new AbstractCSWOnlineResource[srvlist.size()]));

            //Parse our bounding boxes (if they exist). If any are unparsable, don't worry and just continue
            tempNodeList = evalXPathNodeList(mdMetadataNode, BBOXEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<CSWGeographicElement> elList = new ArrayList<>();
                for (int i = 0; i < tempNodeList.getLength(); i++) {
                    try {
                        Node geographyNode = tempNodeList.item(i);
                        elList.add(CSWGeographicBoundingBox.fromGeographicBoundingBoxNode(geographyNode));
                    } catch (Exception ex) {
                        logger.debug(String.format(
                                "Unable to parse CSWGeographicBoundingBox resource for serviceName='%1$s' %2$s",
                                record.getServiceName(), ex));
                    }
                }
                record.setCSWGeographicElements(elList.toArray(new CSWGeographicElement[elList.size()]));
            }

            //Parse the descriptive keywords
            tempNodeList = evalXPathNodeList(mdMetadataNode, KEYWORDLISTEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> keywords = new ArrayList<>();
                Node keyword;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    keyword = tempNodeList.item(j);
                    keywords.add(keyword.getTextContent());
                }
                record.setDescriptiveKeywords(keywords.toArray(new String[keywords.size()])); // correct!
            }

            //Parse the dataset URIs
            tempNodeList = evalXPathNodeList(mdMetadataNode, DATASETURIEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> datasetURIs = new ArrayList<>();
                Node datasetURI;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    datasetURI = tempNodeList.item(j);
                    datasetURIs.add(datasetURI.getTextContent());
                }
                record.setDataSetURIs(datasetURIs.toArray(new String[datasetURIs.size()]));
            }

            Node tempNode = evalXPathNode(mdMetadataNode, CONTACTEXPRESSION);
            if (tempNode != null) {
                try {
                    CSWResponsibleParty respParty = CSWResponsiblePartyFactory.generateResponsiblePartyFromNode(tempNode);
                    record.setContact(respParty);
                } catch (Exception ex) {
                    logger.debug(String.format("Unable to parse contact for serviceName='%1$s' %2$s",
                            record.getServiceName(), ex));
                }
            }

            //Parse any legal constraints
            tempNodeList = evalXPathNodeList(mdMetadataNode, OTHERCONSTRAINTSEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> constraintsList = new ArrayList<>();
                Node constraint;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    constraint = tempNodeList.item(j);
                    constraintsList.add(constraint.getTextContent());
                }
                record.setConstraints(constraintsList.toArray(new String[constraintsList.size()]));
            }
            
            // added code to parse use limit constraints
            tempNodeList = evalXPathNodeList(mdMetadataNode, USELIMITCONSTRAINTSEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> useLimitConstraintsList = new ArrayList<>();
                Node useLimitConstraint;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                	useLimitConstraint = tempNodeList.item(j);
                    useLimitConstraintsList.add(useLimitConstraint.getTextContent());
                }
                record.setUseLimitConstraints(useLimitConstraintsList.toArray(new String[useLimitConstraintsList.size()]));
            }
            
           //added code to parse access constraints
            tempNodeList = evalXPathNodeList(mdMetadataNode, ACCESSCONSTRAINTSEXPRESSION);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {
                List<String> accessConstraintsList = new ArrayList<>();
                Node accessConstraint;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                	accessConstraint = tempNodeList.item(j);
                    accessConstraintsList.add(accessConstraint.getTextContent());
                }
                record.setAccessConstraints(accessConstraintsList.toArray(new String[accessConstraintsList.size()]));
            }

            tempNodeList = evalXPathNodeList(mdMetadataNode, SCALEDENOMINATOR);
            if (tempNodeList != null && tempNodeList.getLength() > 0) {

                List<Double> scaleRange = new ArrayList<>();
                Node scaleDenominator;
                for (int j = 0; j < tempNodeList.getLength(); j++) {
                    scaleDenominator = tempNodeList.item(j);
                    try {
                       scaleRange.add(Double.parseDouble(scaleDenominator.getTextContent()));
                    } catch (Exception ex) {
                        logger.debug(String.format("Unable to parse scale denominator for serviceName='%1$s' %2$s",
                                record.getServiceName(), ex));
                    }
                }

                if (!scaleRange.isEmpty()) {
                    record.setMinScale(Collections.min(scaleRange));
                    if (scaleRange.size() > 1) {
                        record.setMaxScale(Collections.max(scaleRange));
                    }
                }
            }
            
            return record;
        }
        
        private List<AbstractCSWOnlineResource> transformSrvNodes(CSWRecord record, String expression) throws XPathExpressionException{
        	NodeList  tempNodeList = evalXPathNodeList(mdMetadataNode, expression);
            List<AbstractCSWOnlineResource> resources = new ArrayList<>();
            for (int i = 0; i < tempNodeList.getLength(); i++) {
                try {
                    Node onlineNode = tempNodeList.item(i);
                    resources.add(CSWOnlineResourceFactory.parseFromNode(onlineNode, null)); // no name extracted from Thredds layer info
                } catch (IllegalArgumentException ex) {
                    logger.debug(String.format("Unable to parse online resource for serviceName='%1$s' %2$s",
                            record.getServiceName(), ex));
                }
            }
            return resources;
        }
    	
    }
    
    public static void transformDate(CSWRecord record, Node metaNode, Log logger) throws XPathExpressionException {
        String dateStampString = evalXPathString(metaNode, DATETIMESTAMPEXPRESSION);
        if (dateStampString != null && !dateStampString.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATETIMEFORMATSTRING);
                record.setDate(sdf.parse(dateStampString));
            } catch (Exception ex) {
                logger.debug(String.format("Unable to parse date for serviceName='%1$s' %2$s", record.getServiceName(),
                        ex));
            }
        } else {
        	dateStampString = evalXPathString(metaNode, DATESTAMPEXPRESSION);
        	if (dateStampString != null && !dateStampString.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMATSTRING);
                    record.setDate(sdf.parse(dateStampString));
                } catch (Exception ex) {
                    logger.debug(String.format("Unable to parse date for serviceName='%1$s' %2$s", record.getServiceName(),
                            ex));
                }
            } 
        }    	
    }
}

