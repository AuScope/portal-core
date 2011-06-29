package org.auscope.portal.csw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * User: Mathew Wyatt
 * Date: 11/02/2009
 * Time: 11:58:21 AM
 */
//TODO: refactor into data and service records
public class CSWRecord {

    private static final Log logger = LogFactory.getLog(CSWRecord.class);

    private String serviceName;
    private CSWOnlineResource[] onlineResources;
    private String contactOrganisation;
    private String resourceProvider;
    private String fileIdentifier;
    private String recordInfoUrl;
    private CSWGeographicElement[] cswGeographicElements;
    private String[] descriptiveKeywords;
    private String dataIdentificationAbstract;
    private String[] constraints;
    
    private static final XPathExpression serviceTitleExpression;
    private static final XPathExpression dataIdentificationAbstractExpression;
    private static final XPathExpression contactOrganisationExpression;
    private static final XPathExpression resourceProviderExpression;
    private static final XPathExpression fileIdentifierExpression;
    private static final XPathExpression onlineTransfersExpression;
    private static final XPathExpression bboxExpression;
    private static final XPathExpression keywordListExpression;
    private static final XPathExpression otherConstraintsExpression;
    
    /**
     * Initialise all of our XPathExpressions
     */
    static {
        
        serviceTitleExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        dataIdentificationAbstractExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString");
        contactOrganisationExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        resourceProviderExpression =  CSWXPathUtil.attemptCompileXpathExpr("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty[./gmd:role[./gmd:CI_RoleCode[@codeListValue = 'resourceProvider']]]/gmd:organisationName/gco:CharacterString");
        fileIdentifierExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:fileIdentifier/gco:CharacterString");
        onlineTransfersExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine");
        bboxExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox");
        keywordListExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
        otherConstraintsExpression = CSWXPathUtil.attemptCompileXpathExpr("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString");
    }
    
    public CSWRecord(String serviceName, String contactOrganisation,
    		String fileIdentifier, String recordInfoUrl, String dataIdentificationAbstract,
			CSWOnlineResource[] onlineResources, CSWGeographicElement[] cswGeographicsElements) {
    	this.serviceName = serviceName;
    	this.contactOrganisation = contactOrganisation;
    	this.fileIdentifier = fileIdentifier;
    	this.recordInfoUrl = recordInfoUrl;
    	this.dataIdentificationAbstract = dataIdentificationAbstract;
    	this.onlineResources = onlineResources;
    	this.cswGeographicElements = cswGeographicsElements;
    	this.descriptiveKeywords = new String[0];
    	this.constraints = new String[0];
    }

    public CSWRecord(Node node) throws XPathExpressionException {
        //Load default values before parsing
        this("","","","","", new CSWOnlineResource[0], new CSWGeographicElement[0]);
        
        NodeList tempNodeList1 = null;

        serviceName = (String)serviceTitleExpression.evaluate(node, XPathConstants.STRING);
        dataIdentificationAbstract = (String) dataIdentificationAbstractExpression.evaluate(node, XPathConstants.STRING);
        contactOrganisation = (String) contactOrganisationExpression.evaluate(node, XPathConstants.STRING);
        fileIdentifier = (String) fileIdentifierExpression.evaluate(node, XPathConstants.STRING);

        resourceProvider = (String) resourceProviderExpression.evaluate(node, XPathConstants.STRING);
        if (resourceProvider.equals("")) {
        	resourceProvider = "Unknown";
        }

        //There can be multiple gmd:onLine elements (which contain a number of fields we want)
        tempNodeList1 = (NodeList)onlineTransfersExpression.evaluate(node, XPathConstants.NODESET);
        List<CSWOnlineResource> resources = new ArrayList<CSWOnlineResource>();
        for (int i = 0; i < tempNodeList1.getLength(); i++) {
        	try {
        	    Node onlineNode = tempNodeList1.item(i);
        	    resources.add(CSWOnlineResourceFactory.parseFromNode(onlineNode));
        	} catch (IllegalArgumentException ex) {
        	    logger.debug(String.format("Unable to parse online resource for serviceName='%1$s' %2$s",serviceName, ex));
        	}
        }
        onlineResources = resources.toArray(new CSWOnlineResource[resources.size()]);

        //Parse our bounding boxes (if they exist). If any are unparsable, don't worry and just continue
        tempNodeList1 = (NodeList)bboxExpression.evaluate(node, XPathConstants.NODESET);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0) {
        	List<CSWGeographicElement> elList = new ArrayList<CSWGeographicElement>();
        	for (int i = 0; i < tempNodeList1.getLength(); i++) {
	            try {
	            	Node geographyNode = tempNodeList1.item(i);
	            	elList.add(CSWGeographicBoundingBox.fromGeographicBoundingBoxNode(geographyNode));
	            } catch (Exception ex) {
	            	logger.debug(String.format("Unable to parse CSWGeographicBoundingBox resource for serviceName='%1$s' %2$s",serviceName, ex));
	            }
        	}
        	cswGeographicElements = elList.toArray(new CSWGeographicElement[elList.size()]);
        }

        //Parse the descriptive keywords
        tempNodeList1 = (NodeList) keywordListExpression.evaluate(node, XPathConstants.NODESET);
        if (tempNodeList1 != null && tempNodeList1.getLength() > 0 ) {
        	List<String> keywords = new ArrayList<String>();
        	Node keyword;
        	for (int j=0; j<tempNodeList1.getLength(); j++) {
            	keyword = tempNodeList1.item(j);
            	keywords.add(keyword.getTextContent());
            }
            descriptiveKeywords = keywords.toArray(new String[keywords.size()]);
        }
        
        //Parse constraints   	
        tempNodeList1 = (NodeList) otherConstraintsExpression.evaluate(node, XPathConstants.NODESET);
        if(tempNodeList1 != null && tempNodeList1.getLength() > 0) {
        	List<String> constraintsList = new ArrayList<String>();
        	Node constraint;
        	for (int j=0; j<tempNodeList1.getLength(); j++) {
            	constraint = tempNodeList1.item(j);
            	constraintsList.add(constraint.getTextContent());
            }
        	constraints = constraintsList.toArray(new String[constraintsList.size()]);
        }       
    }

    public void setRecordInfoUrl(String recordInfoUrl) {
        this.recordInfoUrl = recordInfoUrl;
    }

    public String getRecordInfoUrl() {
        return recordInfoUrl;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public String getServiceName() {
        return serviceName;
    }

    public CSWOnlineResource[] getOnlineResources() {
        return onlineResources;
    }

    public String getContactOrganisation() {
        return contactOrganisation;
    }

    public String getResourceProvider() {
    	return resourceProvider;
    }

    public String getDataIdentificationAbstract() {
        return dataIdentificationAbstract;
    }

    /**
     * Set the CSWGeographicElement that bounds this record
     * @param cswGeographicElement (can be null)
     */
    public void setCSWGeographicElements(CSWGeographicElement[] cswGeographicElements) {
        this.cswGeographicElements = cswGeographicElements;
    }

    /**
     * gets the  CSWGeographicElement that bounds this record (or null if it DNE)
     * @return
     */
    public CSWGeographicElement[] getCSWGeographicElements() {
        return cswGeographicElements;
    }

    /**
     * Returns the descriptive keywords for this record
     * @return descriptive keywords
     */
    public String[] getDescriptiveKeywords() {
    	return descriptiveKeywords;
    }
    
    public String[] getConstraints() {
    	return constraints;
    }

    @Override
	public String toString() {
		return "CSWRecord [contactOrganisation=" + contactOrganisation
				+ ", resourceProvider=" + resourceProvider
				+ ", cswGeographicElements="
				+ Arrays.toString(cswGeographicElements)
				+ ", dataIdentificationAbstract=" + dataIdentificationAbstract
				+ ", fileIdentifier=" + fileIdentifier + ", onlineResources="
				+ Arrays.toString(onlineResources) + ", recordInfoUrl="
				+ recordInfoUrl + ", serviceName=" + serviceName 
				+ ", constraints=" + Arrays.toString(constraints) + "]";
	}

	/**
     * Returns a filtered list of online resource protocols that match at least one of the specified types
     *
     * @param types The list of types you want to filter by
     * @return
     */
    public CSWOnlineResource[] getOnlineResourcesByType(CSWOnlineResource.OnlineResourceType... types) {
        List <CSWOnlineResource> result = new ArrayList<CSWOnlineResource>();

        for (CSWOnlineResource r : onlineResources) {
            boolean matching = false;
            CSWOnlineResource.OnlineResourceType typeToMatch = r.getType();
            for (CSWOnlineResource.OnlineResourceType type : types) {
                if (typeToMatch == type) {
                    matching = true;
                    break;
                }
            }

            if (matching) {
                result.add(r);
            }
        }

        return result.toArray(new CSWOnlineResource[result.size()]);
    }

    /**
     * Returns true if this CSW Record contains at least 1 onlineResource with ANY of the specified types
     * @param types
     * @return
     */
    public boolean containsAnyOnlineResource(CSWOnlineResource.OnlineResourceType... types) {
        for (CSWOnlineResource r : onlineResources) {
            CSWOnlineResource.OnlineResourceType typeToMatch = r.getType();
            for (CSWOnlineResource.OnlineResourceType type : types) {
                if (typeToMatch == type) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if this record contains the given descriptive keyword, false otherwise.
     *
     * @param str
     * @return true if this record contains the given descriptive keyword, false otherwise.
     */
    public boolean containsKeyword(String str) {
		for(String keyword : descriptiveKeywords) {
			if(keyword.equals(str)) {
				return true;
			}
		}
    	return false;
    }
}
