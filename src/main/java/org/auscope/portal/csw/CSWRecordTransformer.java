package org.auscope.portal.csw;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.record.CSWContact;
import org.auscope.portal.csw.record.CSWGeographicBoundingBox;
import org.auscope.portal.csw.record.CSWGeographicElement;
import org.auscope.portal.csw.record.CSWOnlineResource;
import org.auscope.portal.csw.record.CSWOnlineResourceFactory;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.csw.record.CSWResponsibleParty;
import org.auscope.portal.csw.record.CSWResponsiblePartyFactory;
import org.auscope.portal.server.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * A class for providing methods to transform between CSWRecord and a DOM ANZLIC representation.
 * @author Josh Vote
 */
public class CSWRecordTransformer {
    public static final String TEMPLATE_FILE = "MD_MetadataTemplate.xml";
    protected final Log logger = LogFactory.getLog(getClass());

    private Document document;
    private Node mdMetadataNode;

    private static final String dateFormatString = "yyyy-MM-dd'T'HH:mm:ss";

    private static final CSWNamespaceContext nc = new CSWNamespaceContext();
    private static final String dateStampExpression = "gmd:dateStamp/gco:DateTime";
    private static final String serviceTitleExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
    private static final String dataIdentificationAbstractExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString";
    private static final String contactExpression = "gmd:contact/gmd:CI_ResponsibleParty";
    private static final String resourceProviderExpression =  "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[./gmd:role[./gmd:CI_RoleCode[@codeListValue = 'resourceProvider']]]/gmd:organisationName/gco:CharacterString";
    private static final String fileIdentifierExpression = "gmd:fileIdentifier/gco:CharacterString";
    private static final String onlineTransfersExpression = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine";
    private static final String bboxExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
    private static final String keywordListExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString";
    private static final String supplementalInfoExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:supplementalInformation/gco:CharacterString";
    private static final String languageExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gco:CharacterString";
    private static final String otherConstraintsExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString";
    private static final String dataQualityStatementExpression = "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString";

    /**
     * Creates a new instance of this class and generates an empty document that will be
     * used for constructing DOM.
     * @throws Exception
     */
    public CSWRecordTransformer() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Build an empty document and a simple mdMetadataNode template
        this.document = builder.newDocument();
        Element mdMetadataNode = createChildNode(document, nc.getNamespaceURI("gmd"), "MD_Metadata");

        Iterator<String> prefixIterator = nc.getPrefixIterator();
        while (prefixIterator.hasNext()) {
            String prefix = prefixIterator.next();
            mdMetadataNode.setAttributeNS("", prefix, nc.getNamespaceURI(prefix));
        }

        this.mdMetadataNode = mdMetadataNode;
    }

    /**
     * Creates a new instance of this class which will draw from the specified
     * gmd:MD_Metadata Node representation as a template
     * @param rootNode
     */
    public CSWRecordTransformer(Node mdMetadataNode) {
        this.document = mdMetadataNode.getOwnerDocument();
        this.mdMetadataNode = mdMetadataNode;
    }

    /**
     * Helper method for creating child XML elements using the template document
     * @param parent The node who will be the parent of this node
     * @param namespaceUri The URI of this node
     * @param name The name of this node
     * @return
     */
    private Element createChildNode(Node parent, String namespaceUri, String name) {
        Element child = this.document.createElementNS(namespaceUri, name);

        parent.appendChild(child);

        return child;
    }

    /**
     * Helper method for appending a child element containing a single gco:DateTime element
     * @param parent
     * @param namespaceUri
     * @param name
     * @param value
     */
    private void appendChildDate(Node parent, String namespaceUri, String name, Date value) {
        Element child = createChildNode(parent, namespaceUri, name);
        Node characterStr = createChildNode(child, nc.getNamespaceURI("gco"), "DateTime");

        if (value == null) {
            child.setAttributeNS(nc.getNamespaceURI("gco"), "nilReason", "missing");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
            characterStr.setTextContent(sdf.format(value));
        }
    }

    /**
     * Helper method for appending a child element containing a single gco:Decimal element
     * @param parent
     * @param namespaceUri
     * @param name
     * @param value
     */
    private void appendChildDecimal(Node parent, String namespaceUri, String name, double value) {
        Element child = createChildNode(parent, namespaceUri, name);
        Node characterStr = createChildNode(child, nc.getNamespaceURI("gco"), "Decimal");
        characterStr.setTextContent(Double.toString(value));
    }

    /**
     * Helper method for appending a child element containing a single gco:CharacterString element
     * @param parent where it will be appended
     * @param namespaceUri
     * @param name
     * @param value
     */
    private void appendChildCharacterString(Node parent, String namespaceUri, String name, String value) {
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
     * @param parent
     * @param namespaceUri
     * @param name
     * @param geoEl
     */
    private void appendChildExtent(Node parent, String namespaceUri, String name, CSWGeographicBoundingBox bbox) {
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
     * @param parent
     * @param namespaceUri
     * @param name
     * @param resource
     */
    private void appendChildOnlineResource(Node parent, String namespaceUri, String name, CSWOnlineResource onlineResource) {
        Node child = createChildNode(parent, namespaceUri, name);
        Node ciOnlineResource = createChildNode(child, nc.getNamespaceURI("gmd"), "CI_OnlineResource");

        //Add linkage
        Node linkage = createChildNode(ciOnlineResource, nc.getNamespaceURI("gmd"), "linkage");
        Node url = createChildNode(linkage, nc.getNamespaceURI("gmd"), "URL");
        url.setTextContent(onlineResource.getLinkage().toString());

        //Add protocol
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "protocol", onlineResource.getProtocol());

        //Add application profile
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "applicationProfile", onlineResource.getApplicationProfile());

        //Add name
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "name", onlineResource.getName());

        //Add description
        appendChildCharacterString(ciOnlineResource, nc.getNamespaceURI("gmd"), "description", onlineResource.getDescription());

        parent.appendChild(child);
    }

    /**
     * Helper method for appending a child element containing a single gmd:CI_Contact element
     * @param contact the CSWContact to append
     * @return
     */
    private void appendChildContact(Node parent, String namespaceUri, String name, CSWContact contact) {
        Node child = createChildNode(parent, namespaceUri, name);
        Element ciContactNode = createChildNode(child, nc.getNamespaceURI("gmd"), "CI_Contact");

        Element phone = createChildNode(ciContactNode, nc.getNamespaceURI("gmd"), "phone");
        Element telephone = createChildNode(phone, nc.getNamespaceURI("gmd"), "CI_Telephone");
        appendChildCharacterString(telephone, nc.getNamespaceURI("gmd"), "voice", contact.getTelephone());
        appendChildCharacterString(telephone, nc.getNamespaceURI("gmd"), "facsimile", contact.getFacsimile());

        Element address = createChildNode(ciContactNode, nc.getNamespaceURI("gmd"), "address");
        Element ciAddress = createChildNode(address, nc.getNamespaceURI("gmd"), "CI_Address");
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "deliveryPoint", contact.getAddressDeliveryPoint());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "city", contact.getAddressCity());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "administrativeArea", contact.getAddressAdministrativeArea());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "postalCode", contact.getAddressPostalCode());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "country", contact.getAddressCountry());
        appendChildCharacterString(ciAddress, nc.getNamespaceURI("gmd"), "electronicMailAddress", contact.getAddressEmail());

        if (contact.getOnlineResource() != null) {
            appendChildOnlineResource(ciContactNode, nc.getNamespaceURI("gmd"), "onlineResource", contact.getOnlineResource());
        }
    }

    /**
     * Transforms the specified CSWResponsibleParty back into a CI_ResponsibleParty element represented by a Node
     * @param rp the CSWResponsibleParty to transform
     * @param rpNode a CI_ResponsibleParty element which will be populated with rp
     * @return
     */
    private void appendChildResponsibleParty(Node parent, String namespaceUri, String name, CSWResponsibleParty rp) {
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
        roleCode.setAttributeNS("", "codeList", "http://www.isotc211.org/2005/resources/codelist/codeList.xml#CI_RoleCode");
        roleCode.setAttributeNS("", "codeListValue", "pointOfContact");
    }


    /**
     * Transforms the specified CSWRecord back into a MD_Metadata element represented by Node.
     *
     * The transformation is built from the internal template specified at construction time.
     *
     * The internal template will NOT be modified by this function
     * @param record
     * @return
     * @throws XPathExpressionException
     */
    public Node transformToNode(CSWRecord record) throws XPathExpressionException {
        Node root = this.mdMetadataNode.cloneNode(false);

        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "fileIdentifier", record.getFileIdentifier());
        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "language", record.getLanguage());

        //Hardcode our character set code
        Node characterSet = createChildNode(root, nc.getNamespaceURI("gmd"), "characterSet");
        Element mdCharacterSetCode = createChildNode(characterSet, nc.getNamespaceURI("gmd"), "MD_CharacterSetCode");
        mdCharacterSetCode.setAttributeNS("", "codeListValue", "utf8");
        mdCharacterSetCode.setAttributeNS("", "codeList", "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_CharacterSetCode");

        CSWResponsibleParty responsibleParty = record.getContact();
        if (responsibleParty != null) {
            appendChildResponsibleParty(root, nc.getNamespaceURI("gmd"), "contact", responsibleParty);
        }

        appendChildDate(root, nc.getNamespaceURI("gmd"), "dateStamp", record.getDate());
        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "metadataStandardName", "ISO 19115:2003/19139");
        appendChildCharacterString(root, nc.getNamespaceURI("gmd"), "metadataStandardVersion", "1.0");

        //We manually construct our mdDataIdentification element as CSWRecords aren't that normalised yet...
        Node identificationInfo = createChildNode(root, nc.getNamespaceURI("gmd"), "identificationInfo");
        Node mdDataIdentification = createChildNode(identificationInfo, nc.getNamespaceURI("gmd"), "MD_DataIdentification");

        //DataIdentification -> citation (manually built)
        Node citation = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"), "citation");
        Node ciCitation = createChildNode(citation, nc.getNamespaceURI("gmd"), "CI_Citation");
        appendChildCharacterString(ciCitation, nc.getNamespaceURI("gmd"), "title", record.getServiceName());
        Node ciCitationDate = createChildNode(ciCitation, nc.getNamespaceURI("gmd"), "date");
        Node ciCitationCIDate = createChildNode(ciCitationDate, nc.getNamespaceURI("gmd"), "CI_Date");
        appendChildDate(ciCitationCIDate, nc.getNamespaceURI("gmd"), "date", record.getDate());
        Node ciCitationCIDateType = createChildNode(ciCitationCIDate, nc.getNamespaceURI("gmd"), "dateType");
        Element ciCitationCIDateTypeCode = createChildNode(ciCitationCIDateType, nc.getNamespaceURI("gmd"), "CI_DateTypeCode");
        ciCitationCIDateTypeCode.setAttributeNS("", "codeListValue", "creation");
        ciCitationCIDateTypeCode.setAttributeNS("", "codeList", "http://www.isotc211.org/2005/resources/codelist/codeList.xml#CI_DateTypeCode");

        //DataIdentification -> abstract
        appendChildCharacterString(mdDataIdentification, nc.getNamespaceURI("gmd"), "abstract", record.getDataIdentificationAbstract());

        //DataIdentification -> status
        Node dataIdStatus = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"), "status");
        Element dataIdStatusCode = createChildNode(dataIdStatus, nc.getNamespaceURI("gmd"), "MD_ProgressCode");
        dataIdStatusCode.setAttributeNS("", "codeListValue", "completed");
        dataIdStatusCode.setAttributeNS("", "codeList", "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_ProgressCode");

        //DataIdentification -> resourceConstraints
        Node dataIdResourceConstraints = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"), "resourceConstraints");
        Node dataIdLegalConstraints = createChildNode(dataIdResourceConstraints, nc.getNamespaceURI("gmd"), "MD_LegalConstraints");
        String[] legalConstraints = record.getConstraints();
        if (legalConstraints != null) {
            for (String constraint : legalConstraints) {
                appendChildCharacterString(dataIdLegalConstraints, nc.getNamespaceURI("gmd"), "otherConstraints", constraint);
            }
        }

        //DataIdentification -> pointOfContact
        if (responsibleParty != null) {
            appendChildResponsibleParty(mdDataIdentification, nc.getNamespaceURI("gmd"), "pointOfContact", responsibleParty);
        }

        //DataIdentification -> descriptiveKeywords
        Node dataIdDescriptiveKeywords = createChildNode(mdDataIdentification, nc.getNamespaceURI("gmd"), "descriptiveKeywords");
        Node dataIdMDKeywords = createChildNode(dataIdDescriptiveKeywords, nc.getNamespaceURI("gmd"), "MD_Keywords");
        String[] keywords = record.getDescriptiveKeywords();
        if (keywords != null) {
            for (String keyword : keywords) {
                appendChildCharacterString(dataIdMDKeywords, nc.getNamespaceURI("gmd"), "keyword", keyword);
            }
        }
        Node dataIdMDKeywordsType = createChildNode(dataIdMDKeywords, nc.getNamespaceURI("gmd"), "type");
        Element dataIdMDKeywordsTypeCode = createChildNode(dataIdMDKeywordsType, nc.getNamespaceURI("gmd"), "MD_KeywordTypeCode");
        dataIdMDKeywordsTypeCode.setAttributeNS("", "codeListValue", "theme");
        dataIdMDKeywordsTypeCode.setAttributeNS("", "codeList", "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_KeywordTypeCode");

        //DataIdentification -> language
        appendChildCharacterString(mdDataIdentification, nc.getNamespaceURI("gmd"), "language", record.getLanguage());

        //DataIdentification -> extent
        CSWGeographicElement[] geoEls = record.getCSWGeographicElements();
        if (geoEls != null) {
            for (CSWGeographicElement geoEl : geoEls) {
                if (geoEl instanceof CSWGeographicBoundingBox) {
                    appendChildExtent(mdDataIdentification, nc.getNamespaceURI("gmd"), "extent", (CSWGeographicBoundingBox) geoEl);
                }
            }
        }

        //DataIdentification -> supplementalInformation
        appendChildCharacterString(mdDataIdentification, nc.getNamespaceURI("gmd"), "supplementalInformation", record.getSupplementalInformation());

        //Online resources
        CSWOnlineResource[] onlineResources = record.getOnlineResources();
        if (onlineResources != null && onlineResources.length > 0) {
            Node distrInfo = createChildNode(root, nc.getNamespaceURI("gmd"), "distributionInfo");
            Node mdDistribution = createChildNode(distrInfo, nc.getNamespaceURI("gmd"), "MD_Distribution");
            Node transferOptions = createChildNode(mdDistribution, nc.getNamespaceURI("gmd"), "transferOptions");
            Node mdDigitalTransferOptions = createChildNode(transferOptions, nc.getNamespaceURI("gmd"), "MD_DigitalTransferOptions");

            for (CSWOnlineResource onlineResource : onlineResources) {
                appendChildOnlineResource(mdDigitalTransferOptions, nc.getNamespaceURI("gmd"), "onLine", onlineResource);
            }
        }

        //Data Quality (partially hardcoded)
        Node dataQualityInfo = createChildNode(root, nc.getNamespaceURI("gmd"), "dataQualityInfo");
        Node dqDataQuality = createChildNode(dataQualityInfo, nc.getNamespaceURI("gmd"), "DQ_DataQuality");
        Node dataQualityScope = createChildNode(dqDataQuality, nc.getNamespaceURI("gmd"), "scope");
        Node dqDataQualityScope = createChildNode(dataQualityScope, nc.getNamespaceURI("gmd"), "DQ_Scope");
        Node dataQualityScopeLevel = createChildNode(dqDataQualityScope, nc.getNamespaceURI("gmd"), "level");
        Element dataQualityScopeLevelCode = createChildNode(dataQualityScopeLevel, nc.getNamespaceURI("gmd"), "MD_ScopeCode");
        dataQualityScopeLevelCode.setAttributeNS("", "codeListValue", "dataset");
        dataQualityScopeLevelCode.setAttributeNS("", "codeList", "http://www.isotc211.org/2005/resources/codelist/codeList.xml#MD_ScopeCode");
        if (record.getDataQualityStatement() != null && !record.getDataQualityStatement().isEmpty()) {
            Node dqLineage = createChildNode(dqDataQuality, nc.getNamespaceURI("gmd"), "lineage");
            Node dqLILineage = createChildNode(dqLineage, nc.getNamespaceURI("gmd"), "LI_Lineage");
            appendChildCharacterString(dqLILineage, nc.getNamespaceURI("gmd"), "statement", record.getDataQualityStatement());
        }

        return root;
    }


    /**
     * Helper method for evaluating an xpath string on a particular node and returning the result
     * as a string (or null)
     * @param node
     * @param xPath A valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    private String evalXPathString(Node node, String xPath) throws XPathExpressionException {
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (String) expression.evaluate(node, XPathConstants.STRING);
    }

    /**
     * Helper method for evaluating an xpath string on a particular node and returning the result
     * as a (possible empty) list of matching nodes
     * @param node
     * @param xPath A valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    private NodeList evalXPathNodeList(Node node, String xPath) throws XPathExpressionException {
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (NodeList) expression.evaluate(node, XPathConstants.NODESET);
    }

    /**
     * Helper method for evaluating an xpath string on a particular node and returning the result
     * as a (possible empty) DOM node
     * @param node
     * @param xPath A valid XPath expression
     * @return
     * @throws XPathExpressionException
     */
    private Node evalXPathNode(Node node, String xPath) throws XPathExpressionException {
        XPathExpression expression = DOMUtil.compileXPathExpr(xPath, nc);
        return (Node) expression.evaluate(node, XPathConstants.NODE);
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
        CSWRecord record = new CSWRecord("", "", "", "", new CSWOnlineResource[0], new CSWGeographicElement[0]);

        NodeList tempNodeList1 = null;

        //Parse our simple strings
        record.setServiceName(evalXPathString(this.mdMetadataNode, serviceTitleExpression));
        record.setDataIdentificationAbstract(evalXPathString(this.mdMetadataNode, dataIdentificationAbstractExpression));
        record.setFileIdentifier(evalXPathString(this.mdMetadataNode, fileIdentifierExpression));
        record.setSupplementalInformation(evalXPathString(this.mdMetadataNode, supplementalInfoExpression));
        record.setLanguage(evalXPathString(this.mdMetadataNode, languageExpression));
        record.setDataQualityStatement(evalXPathString(this.mdMetadataNode, dataQualityStatementExpression));

        String resourceProvider = (String) evalXPathString(this.mdMetadataNode, resourceProviderExpression);
        if (resourceProvider == null || resourceProvider.isEmpty()) {
            resourceProvider = "Unknown";
        }
        record.setResourceProvider(resourceProvider);

        String dateStampString = evalXPathString(this.mdMetadataNode, dateStampExpression);
        if (dateStampString != null && !dateStampString.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormatString);
                record.setDate(sdf.parse(dateStampString));
            } catch (Exception ex) {
                logger.debug(String.format("Unable to parse date for serviceName='%1$s' %2$s",record.getServiceName(), ex));
            }
        }

        //There can be multiple gmd:onLine elements (which contain a number of fields we want)
        tempNodeList1 = (NodeList)evalXPathNodeList(this.mdMetadataNode, onlineTransfersExpression);
        List<CSWOnlineResource> resources = new ArrayList<CSWOnlineResource>();
        for (int i = 0; i < tempNodeList1.getLength(); i++) {
            try {
                Node onlineNode = tempNodeList1.item(i);
                resources.add(CSWOnlineResourceFactory.parseFromNode(onlineNode));
            } catch (IllegalArgumentException ex) {
                logger.debug(String.format("Unable to parse online resource for serviceName='%1$s' %2$s",record.getServiceName(), ex));
            }
        }
        record.setOnlineResources(resources.toArray(new CSWOnlineResource[resources.size()]));

        //Parse our bounding boxes (if they exist). If any are unparsable, don't worry and just continue
        tempNodeList1 = (NodeList)evalXPathNodeList(this.mdMetadataNode, bboxExpression);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0) {
            List<CSWGeographicElement> elList = new ArrayList<CSWGeographicElement>();
            for (int i = 0; i < tempNodeList1.getLength(); i++) {
                try {
                    Node geographyNode = tempNodeList1.item(i);
                    elList.add(CSWGeographicBoundingBox.fromGeographicBoundingBoxNode(geographyNode));
                } catch (Exception ex) {
                    logger.debug(String.format("Unable to parse CSWGeographicBoundingBox resource for serviceName='%1$s' %2$s",record.getServiceName(), ex));
                }
            }
            record.setCSWGeographicElements(elList.toArray(new CSWGeographicElement[elList.size()]));
        }

        //Parse the descriptive keywords
        tempNodeList1 = (NodeList) evalXPathNodeList(this.mdMetadataNode, keywordListExpression);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0 ) {
            List<String> keywords = new ArrayList<String>();
            Node keyword;
            for (int j=0; j<tempNodeList1.getLength(); j++) {
                keyword = tempNodeList1.item(j);
                keywords.add(keyword.getTextContent());
            }
            record.setDescriptiveKeywords(keywords.toArray(new String[keywords.size()]));
        }

        Node tempNode = evalXPathNode(this.mdMetadataNode, contactExpression);
        if (tempNode != null) {
            try {
                CSWResponsibleParty respParty = CSWResponsiblePartyFactory.generateResponsiblePartyFromNode(tempNode);
                record.setContact(respParty);
            } catch (Exception ex) {
                logger.debug(String.format("Unable to parse contact for serviceName='%1$s' %2$s",record.getServiceName(), ex));
            }
        }

        //Parse any legal constraints
        tempNodeList1 = (NodeList) evalXPathNodeList(this.mdMetadataNode, otherConstraintsExpression);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0) {
            List<String> constraintsList = new ArrayList<String>();
            Node constraint;
            for (int j = 0; j < tempNodeList1.getLength(); j++) {
                constraint = tempNodeList1.item(j);
                constraintsList.add(constraint.getTextContent());
            }
            record.setConstraints(constraintsList.toArray(new String[constraintsList.size()]));
        }

        return record;
    }
}
