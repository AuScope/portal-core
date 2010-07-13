package org.auscope.portal.csw;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

/**
 * User: Mathew Wyatt
 * Date: 11/02/2009
 * Time: 11:58:21 AM
 */
//TODO: refactor into data and service records
public class CSWRecord {
	//removed this link as it was not used and contained a massive chunk of memory 
    //private Node recordNode; 
    private String serviceName;
    private String serviceUrl;
    private String onlineResourceName;
    private String onlineResourceDescription;
    private String onlineResourceProtocol;
    private String contactOrganisation;
    private String fileIdentifier;
    private String recordInfoUrl;
    private CSWGeographicElement cswGeographicElement;


    private String dataIdentificationAbstract;


    public CSWRecord(Node node) throws XPathExpressionException {

        XPath xPath = XPathFactory.newInstance().newXPath();
        Node tempNode = null;
        NodeList tempNodeList = null;
        xPath.setNamespaceContext(new CSWNamespaceContext());
        

        String serviceTitleExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(serviceTitleExpression, node, XPathConstants.NODE);
        serviceName = tempNode != null ? tempNode.getTextContent() : "";

        String dataIdentificationAbstractExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(dataIdentificationAbstractExpression, node, XPathConstants.NODE);
        dataIdentificationAbstract = tempNode != null ? tempNode.getTextContent() : "";

        String contactOrganisationExpression = "gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(contactOrganisationExpression, node, XPathConstants.NODE);
        contactOrganisation = tempNode != null ? tempNode.getTextContent() : "";

        String fileIdentifierExpression = "gmd:fileIdentifier/gco:CharacterString";
        tempNode = (Node)xPath.evaluate(fileIdentifierExpression, node, XPathConstants.NODE);
        fileIdentifier = tempNode != null ? tempNode.getTextContent() : "";
        
        //There can be multiple gmd:onLine elements (which contain a number of fields we want), take the first one that can be treated as WMS/WFS
        String onlineTransfersExpression = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine";
        String serviceUrlExpression = "gmd:CI_OnlineResource/gmd:linkage/gmd:URL";
        String onlineResourceProtocolExpression = "gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString";
        String onlineResourceNameExpression = "gmd:CI_OnlineResource/gmd:name/gco:CharacterString";
        String onlineResourceDescriptionExpression = "gmd:CI_OnlineResource/gmd:description/gco:CharacterString";
        serviceUrl = "";
        onlineResourceProtocol = "";
        onlineResourceName = "";
        onlineResourceDescription  = "";
        tempNodeList = (NodeList)xPath.evaluate(onlineTransfersExpression, node, XPathConstants.NODESET);
        for (int i = 0; i < tempNodeList.getLength(); i++) {
        	Node onlineNode = tempNodeList.item(i);
        	
        	//The current (bad) strategy is to find the first onlineResourceProtocol that contains the text "wms" or "wfs" 
        	tempNode = (Node)xPath.evaluate(onlineResourceProtocolExpression, onlineNode, XPathConstants.NODE);
        	String recordTypeString = (tempNode == null || tempNode.getTextContent() == null) ? "" : tempNode.getTextContent().toLowerCase();   
        	if (recordTypeString.contains("wms") ||
        		recordTypeString.contains("wfs")) {
        
        		tempNode = (Node)xPath.evaluate(onlineResourceProtocolExpression, onlineNode, XPathConstants.NODE);
                onlineResourceProtocol = tempNode != null ? tempNode.getTextContent() : "";
                
                tempNode = (Node)xPath.evaluate(onlineResourceNameExpression, onlineNode, XPathConstants.NODE);
                onlineResourceName = tempNode != null ? tempNode.getTextContent() : "";

                tempNode = (Node)xPath.evaluate(onlineResourceDescriptionExpression, onlineNode, XPathConstants.NODE);
                onlineResourceDescription = tempNode != null ? tempNode.getTextContent() : "";
                
                tempNode = (Node)xPath.evaluate(serviceUrlExpression, onlineNode, XPathConstants.NODE);
                serviceUrl = tempNode != null ? tempNode.getTextContent() : "";
                
                break;
        	}
        }
        
        //Parse our bounding box (if it exists). If it's unparsable, don't worry and just continue
        String bboxExpression = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox";
        tempNode = (Node)xPath.evaluate(bboxExpression, node, XPathConstants.NODE);
        if (tempNode != null) {
            try {
                cswGeographicElement = CSWGeographicBoundingBox.fromGeographicBoundingBoxNode(tempNode, xPath);
            } catch (Exception ex) { }
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

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getOnlineResourceName() {
        return onlineResourceName;
    }

    public String getOnlineResourceDescription() {
        return onlineResourceDescription;
    }

    public String getOnlineResourceProtocol() {
        return onlineResourceProtocol;
    }

    public String getContactOrganisation() {
        return contactOrganisation;
    }

    public String getDataIdentificationAbstract() {
        return dataIdentificationAbstract;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(serviceName);
        buf.append(",");
        buf.append(serviceUrl);
        buf.append(",");
        buf.append(onlineResourceName);
        buf.append(",");
        buf.append(onlineResourceDescription);
        buf.append(",");
        buf.append(onlineResourceProtocol);
        buf.append(",");
        buf.append(dataIdentificationAbstract);
        buf.append(",");
        return buf.toString(); 
    }

    /**
     * Set the CSWGeographicElement that bounds this record
     * @param cswGeographicElement (can be null)
     */
    public void setCSWGeographicElement(CSWGeographicElement cswGeographicElement) {
        this.cswGeographicElement = cswGeographicElement;
    }

    /**
     * gets the  CSWGeographicElement that bounds this record (or null if it DNE)
     * @return
     */
    public CSWGeographicElement getCSWGeographicElement() {
        return cswGeographicElement;
    }
}
