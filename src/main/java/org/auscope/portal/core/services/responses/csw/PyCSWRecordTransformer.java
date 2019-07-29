package org.auscope.portal.core.services.responses.csw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PyCSWRecordTransformer extends CSWRecordTransformer{

    public static final String TEMPLATE_FILE = "MD_MetadataTemplate.xml";
    protected final Log logger = LogFactory.getLog(getClass());
    protected static final String[] FIXED_DIMENSION_NAMES = {"time", "longitude", "lon", "latitude", "lat", "transverse_mercator", "crs"}; 

    protected static final String DATETIMEFORMATSTRING = "yyyy-MM-dd'T'HH:mm:ss";
    protected static final String DATEFORMATSTRING = "yyyy-MM-dd";

    protected enum Scope {
        service, dataset
    }

    protected static final CSWNamespaceContext nc = new CSWNamespaceContext();
    private static final String SERVICEIDENTIFICATIONPATH = "gmd:identificationInfo/srv:SV_ServiceIdentification";
    private static final String DATAIDENTIFICATIONPATH = "gmd:identificationInfo/gmd:MD_DataIdentification";
    private static final String TITLEEXPRESSION = "/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";

    private static final String DATESTAMPEXPRESSION = "gmd:dateStamp/gco:Date";
    private static final String DATETIMESTAMPEXPRESSION = "gmd:dateStamp/gco:DateTime";
    private static final String SCOPEEXPRESSION = "gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue";
    private static final String ABSTRACTEXPRESSION = "/gmd:abstract/gco:CharacterString";

    private static final String CONTACTEXPRESSION = "gmd:contact/gmd:CI_ResponsibleParty";
    private static final String RESOURCEPROVIDEREXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[./gmd:role[./gmd:CI_RoleCode[@codeListValue = 'resourceProvider']]]/gmd:organisationName/gco:CharacterString";
    private static final String FILEIDENTIFIEREXPRESSION = "gmd:fileIdentifier/gco:CharacterString";
    private static final String PARENTIDENTIFIEREXPRESSION = "gmd:parentIdentifier/gco:CharacterString";
    private static final String ONLINEDATASETTRANSFERSEXPRESSION = "gmd:distributionInfo/gmd:MD_Distribution/descendant::gmd:onLine";
    private static final String ONLINETRANSFERSEXPRESSION = "gmd:identificationInfo/srv:SV_ServiceIdentification/descendant::srv:connectPoint";
    private static final String BBOXEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
    private static final String KEYWORDLISTEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString";
    private static final String DATASETURIEXPRESSION = "gmd:dataSetURI/gco:CharacterString";
    private static final String SUPPLEMENTALINFOEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:supplementalInformation/gco:CharacterString";
    private static final String LANGUAGEEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gco:CharacterString";
    private static final String OTHERCONSTRAINTSEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/(gmd:otherConstraints/gco:CharacterString | gmd:useLimitation/gco:CharacterString | gmd:accessConstraints/gmd:MD_RestrictionCode[(text())]/@codeList | gmd:reference/gmd:CI_Citation/gmd:title/gco:CharacterString)";  
    private static final String USELIMITCONSTRAINTSEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString";
    private static final String ACCESSCONSTRAINTSEXPRESSION = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode[(text())]/@codeList";    
    private static final String DATAQUALITYSTATEMENTEXPRESSION = "gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString";
    private static final String LAYERNAME = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString";
    private static final String THREDDSLAYERNAME = "gmd:contentInfo/gmi:MI_CoverageDescription/gmd:dimension/gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString";
    //private static final String LAYERNAME = "gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString";

    private static final String SCALEDENOMINATOR = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer";

	public PyCSWRecordTransformer() throws PortalServiceException {
		super();
	}

    public PyCSWRecordTransformer(Node mdMetadataNode) {
        super(mdMetadataNode);
    }
    
    /**
     * Writes to an existing CSWRecord instance with data parsed from the internal template of this class
     *
     * Throws an exception if the internal template cannot be parsed correctly
     *
     * @return
     * @throws XPathExpressionException
     */
    @Override
    protected CSWRecord transformToCSWRecord(CSWRecord record) throws XPathExpressionException {
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

        transformDate(record);

        String tlname = getThreddsLayerName();
        
        //There can be multiple gmd:onLine elements (which contain a number of fields we want)
        List<AbstractCSWOnlineResource> srvlist = transformSrvNodes(record, ONLINETRANSFERSEXPRESSION, tlname);
        List<AbstractCSWOnlineResource> datasetlist = transformSrvNodes(record, ONLINEDATASETTRANSFERSEXPRESSION, tlname);
        srvlist.addAll(datasetlist);
        removeDuplicateOnlineResources(srvlist);
        record.setOnlineResources(srvlist.toArray(new AbstractCSWOnlineResource[srvlist.size()]));

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
            record.setDescriptiveKeywords(keywords.toArray(new String[keywords.size()])); // correct!
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
    
    protected List<AbstractCSWOnlineResource> transformSrvNodes(CSWRecord record, String expression, String threddsLayerName) 
			throws XPathExpressionException{
    	NodeList  tempNodeList = evalXPathNodeList(this.mdMetadataNode, expression);
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

    protected static boolean isFixedLayerName(String lname) {
    	for (int i= 0; i < FIXED_DIMENSION_NAMES.length; i++) {
			if (FIXED_DIMENSION_NAMES[i].compareTo(lname) == 0 ) {
				return true;
			}
		}
    	return false;
    }
    
    protected String getThreddsLayerName() throws XPathExpressionException {
    	NodeList tempNodeList = evalXPathNodeList(this.mdMetadataNode, THREDDSLAYERNAME);
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
