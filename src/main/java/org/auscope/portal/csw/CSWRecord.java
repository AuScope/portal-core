package org.auscope.portal.csw;

import org.isotc211.x2005.gco.CharacterStringPropertyType;
import org.isotc211.x2005.gmd.AbstractMDIdentificationType;
import org.isotc211.x2005.gmd.CICitationPropertyType;
import org.isotc211.x2005.gmd.MDDigitalTransferOptionsPropertyType;
import org.isotc211.x2005.gmd.MDDigitalTransferOptionsType;
import org.isotc211.x2005.gmd.MDIdentificationPropertyType;
import org.isotc211.x2005.gmd.MDMetadataDocument;

/**
 * User: Michael Stegherr
 * Date: 04/03/2009
 * Time: 05:10:41 PM
 */
public class CSWRecord {
    private MDMetadataDocument metadataDoc;

    public CSWRecord(MDMetadataDocument doc) {
        this.metadataDoc = doc;
    }

    public String getServiceName() {
    	// XPath:
    	// gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString
    	
    	// currently we only have one identificationInfo tag, so grab the first one (0) 
    	MDIdentificationPropertyType ident = metadataDoc.getMDMetadata().getIdentificationInfoArray(0);
    	AbstractMDIdentificationType abstractMDIdent = ident.getAbstractMDIdentification();
    	CICitationPropertyType citation = abstractMDIdent.getCitation();
    	CharacterStringPropertyType title = citation.getCICitation().getTitle();
    	String characterString = title.getCharacterString();
    	    	
    	return characterString;
    }

    public String getServiceUrl() {
    	MDDigitalTransferOptionsPropertyType[] transferOptionsArray =
    		metadataDoc.getMDMetadata().getDistributionInfo().getMDDistribution().getTransferOptionsArray();
    	
    	MDDigitalTransferOptionsType digitalTransferOptions = null;
    	for( int i=0; i<transferOptionsArray.length; i++ )
    	{
    		if( transferOptionsArray[i].isSetMDDigitalTransferOptions() )
    		{
    			digitalTransferOptions = transferOptionsArray[i].getMDDigitalTransferOptions();
    			break;
    		}
    	}
    	
    	// TODO: add check, if it's the correct onLine element
    	if (!digitalTransferOptions.isNil())
    	{
    		String url = digitalTransferOptions.getOnLineArray(0).getCIOnlineResource().getLinkage().getURL().toString(); 
    		return url;
    	}
    	else return "";
    }

    public MDMetadataDocument getMDDocument() {
        return this.metadataDoc;
    }
}
