package org.auscope.portal.core.services.responses.csw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.auscope.portal.core.services.csw.CSWRecordsFilterVisitor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


/**
 * Represents a parsed gmd:MD_Metadata element that is received as part of an OGC CSW transaction.
 * When KnownLayers are built the associated CSWRecords will be updated with their names and descriptions to facilitate searching.
 *
 * @author Mathew Wyatt
 * @author Joshua Vote
 * @version $Id$
 */
//@Document(indexName = "auscope-api-cswrecord")
@Document(indexName = "#{@environment.getProperty('spring.data.elasticsearch.cswRecordIndex')}")
public class CSWRecord {
	
    /** The service name. */
    private String serviceName;

    /** The online resources. */
    @Field(type = FieldType.Object)
    private List<AbstractCSWOnlineResource> onlineResources;

    /** The resource provider. */
    private String resourceProvider;

    /** The file identifier. */
    @Id
    private String fileIdentifier;

    /** The parent identifier. */
    private String parentIdentifier;

    /** The record info url. */
    private String recordInfoUrl;

    /** The csw geographic elements. */
    @Field(type = FieldType.Object)
    private CSWGeographicElement[] cswGeographicElements;

    /** The descriptive keywords. */
    private String[] descriptiveKeywords;

    /** The URIs from which file downloads will be available in some records. */
    private String[] dataSetURIs;

    /** The data identification abstract. */
    private String dataIdentificationAbstract;

    /** The supplemental information. */
    private String supplementalInformation;

    /** The language. */
    private String language;

    /** The constraints. */
    private String[] constraints;
    
    /** The use limit constraints. */
    private String[] useLimitConstraints;
    
    /** The access constraints. */
    private String[] accessConstraints;

    /** The contact. */
    @Field(type = FieldType.Object)
    private CSWResponsibleParty contact;
    
    /** The funder. */
    @Field(type = FieldType.Object)
    private CSWResponsibleParty funder;

    /** The date. */
    private Date date;

    /** The data quality statement. */
    private String dataQualityStatement;
    
    /** The temporal extent of the record **/
    private CSWTemporalExtent temporalExtent;

    /** The csw child records */
    @Transient
    private List<CSWRecord> childRecords = new ArrayList<>();

    @Transient
    private boolean noCache;

    private boolean service;

    private String layerName = "";

    /** The maximum scale for the layer to appear */
    private Double maxScale;

    /** The minimum scale for the layer to appear */
    private Double minScale;
    
    /** Non-OGC Fields below included for indexing/searching **/
    // The ID (from application-registries.yaml) of the service provider
    private String serviceId;
    
    // The IDs of any associated KnownLayers
    private List<String> knownLayerIds;
    
    // The names of any associated known layers (for searching)
    private List<String> knownLayerNames;
    
    // The descriptions of any associated known layers (for searching)
    private List<String> knownLayerDescriptions;
    

    /**
     * Instantiates a new empty CSWRecord
     * 
     */
    public CSWRecord() {
        this(null, null, null, null, null, null, "");
    }
    
    /**
     * Instantiates a new CSW record.
     *
     * @param fileIdentifier
     *            the file identifier
     * 
     */
    public CSWRecord(String fileIdentifier) {
        this(null, fileIdentifier, null, null, null, null, "");
    }

    /**
     * Instantiates a new CSW record.
     *
     * @param serviceName
     *            the service name
     * @param fileIdentifier
     *            the file identifier
     * @param recordInfoUrl
     *            the record info url
     * @param dataIdentificationAbstract
     *            the data identification abstract
     * @param onlineResources
     *            the online resources
     * @param cswGeographicsElements
     *            the csw geographics elements
     */
    public CSWRecord(String serviceName, String fileIdentifier,
            String recordInfoUrl, String dataIdentificationAbstract,
            List<AbstractCSWOnlineResource> onlineResources, CSWGeographicElement[] cswGeographicsElements) {
        this(serviceName, fileIdentifier, recordInfoUrl, dataIdentificationAbstract, onlineResources, cswGeographicsElements, "");
    }

    /**
     * Instantiates a new CSW record.
     *
     * @param serviceName
     *            the service name
     * @param fileIdentifier
     *            the file identifier
     * @param recordInfoUrl
     *            the record info url
     * @param dataIdentificationAbstract
     *            the data identification abstract
     * @param onlineResources
     *            the online resources
     * @param cswGeographicsElements
     *            the csw geographics elements
     */
    public CSWRecord(String serviceName, String fileIdentifier,
            String recordInfoUrl, String dataIdentificationAbstract,
            List<AbstractCSWOnlineResource> onlineResources, CSWGeographicElement[] cswGeographicsElements, String layerName) {
        this.serviceName = serviceName;
        this.fileIdentifier = fileIdentifier;
        this.recordInfoUrl = recordInfoUrl;
        this.dataIdentificationAbstract = dataIdentificationAbstract;
        this.onlineResources = onlineResources;
        this.cswGeographicElements = cswGeographicsElements;
        this.supplementalInformation = "";
        this.descriptiveKeywords = new String[0];
        this.dataSetURIs = new String[0];
        this.constraints = new String[0];
        this.useLimitConstraints = new String[0];
        this.accessConstraints = new String[0];
        this.noCache = false;
        this.layerName = layerName;
    }

    /**
     * Sets the record info url.
     *
     * @param recordInfoUrl
     *            the new record info url
     */
    public void setRecordInfoUrl(String recordInfoUrl) {
        this.recordInfoUrl = recordInfoUrl;
    }

    /**
     * Gets the record info url.
     *
     * @return the record info url
     */
    public String getRecordInfoUrl() {
        return recordInfoUrl;
    }

    /**
     * Sets the noCache variable
     *
     * @param recordInfoUrl
     *            the new record info url
     */
    public void setNoCache(boolean nocache) {
        this.noCache = nocache;
    }

    /**
     * Gets the noCache variable
     *
     * @return the record info url
     */
    public boolean getNoCache() {
        return this.noCache;
    }

    /**
     * Gets the file identifier.
     *
     * @return the file identifier
     */
    public String getFileIdentifier() {
        return fileIdentifier;
    }

    /**
     * Get the parent identifier.
     *
     */
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    /**
     * Gets the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the online resources.
     *
     * @return the online resources
     */
    public List<AbstractCSWOnlineResource> getOnlineResources() {
        return onlineResources;
    }
    
    /**
     * Check for an online resource with a name
     *
     * @return true if there exists an online resource with a name 
     */
    public boolean hasNamedOnlineResources() {
        boolean hasName = false;
        if (onlineResources != null) {
        	for (int i = 0; i < onlineResources.size(); i++) {
                if (!onlineResources.get(i).getName().equals("")) {
                    hasName = true;
                    break;
                }
            }
        }
        return hasName;
    }

    /**
     * Gets the resource provider.
     *
     * @return the resource provider
     */
    public String getResourceProvider() {
        return resourceProvider;
    }

    /**
     * Gets the data identification abstract.
     *
     * @return the data identification abstract
     */
    public String getDataIdentificationAbstract() {
        return dataIdentificationAbstract;
    }

    /**
     * Set the CSWGeographicElement that bounds this record.
     *
     * @param cswGeographicElements
     *            the new cSW geographic elements
     */
    public void setCSWGeographicElements(CSWGeographicElement[] cswGeographicElements) {
        this.cswGeographicElements = cswGeographicElements;
    }

    /**
     * gets the CSWGeographicElement that bounds this record (or null if it DNE).
     *
     * @return the cSW geographic elements
     */
    public CSWGeographicElement[] getCSWGeographicElements() {
        return cswGeographicElements;
    }
    
    /**
     * Checks if there are any BBOXes for this record
     *
     * @return true if there are any BBOXes in the geographic elements
     */
    public boolean hasGeographicElements() {
        return (cswGeographicElements!=null && cswGeographicElements.length>0);
    }

    /**
     * Returns the descriptive keywords for this record.
     *
     * @return descriptive keywords
     */
    public String[] getDescriptiveKeywords() {
        return descriptiveKeywords;
    }

    /**
     * Returns the dataset URIs for this record.
     *
     * @return array of URIs
     */
    public String[] getDataSetURIs() {
        return dataSetURIs;
    }

    /**
     * Gets the constraints.
     *
     * @return the constraints
     */
    public String[] getConstraints() {
        return constraints;
    }

    /**
     * Sets the constraints.
     *
     * @param constraints
     *            the new constraints
     */
    public void setConstraints(String[] constraints) {
        this.constraints = constraints;
    }
    
    /**
     * Gets the use limit constraints.
     *
     * @return the useLimitConstraints
     */
    public String[] getUseLimitConstraints() {
        return useLimitConstraints;
    }

    /**
     * Sets the use limit constraints.
     *
     * @param useLimitConstraints
     *            the new useLimitConstraints
     */
    public void setUseLimitConstraints(String[] useLimitConstraints) {
        this.useLimitConstraints = useLimitConstraints;
    }
    
    
    /**
     * Gets the access constraints.
     *
     * @return the accessConstraints
     */
    public String[] getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * Sets the access constraints.
     *
     * @param accessConstraints
     *            the new accessConstraints
     */
    public void setAccessConstraints(String[] accessConstraints) {
        this.accessConstraints = accessConstraints;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName
     *            the new service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets the online resources.
     *
     * @param onlineResources
     *            the new online resources
     */
    public void setOnlineResources(List<AbstractCSWOnlineResource> onlineResources) {
        this.onlineResources = onlineResources;
    }

    /**
     * Sets the resource provider.
     *
     * @param resourceProvider
     *            the new resource provider
     */
    public void setResourceProvider(String resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    /**
     * Sets the file identifier.
     *
     * @param fileIdentifier
     *            the new file identifier
     */
    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    /**
     * Sets the parent identifier.
     *
     * @param parentFileIdentifier
     *            the new parent file identifier
     */
    public void setParentIdentifier(String parentIdentifier) {
        this.parentIdentifier = parentIdentifier;
    }

    /**
     * Sets the descriptive keywords.
     *
     * @param descriptiveKeywords
     *            the new descriptive keywords
     */
    public void setDescriptiveKeywords(String[] descriptiveKeywords) {
        this.descriptiveKeywords = descriptiveKeywords;
    }

    /**
     * Sets the dataset URIs
     *
     * @param dataSetURIs
     *            the dataSetURIs to set
     */
    public void setDataSetURIs(String[] dataSetURIs) {
        this.dataSetURIs = dataSetURIs;
    }

    /**
     * Sets the data identification abstract.
     *
     * @param dataIdentificationAbstract
     *            the new data identification abstract
     */
    public void setDataIdentificationAbstract(String dataIdentificationAbstract) {
        this.dataIdentificationAbstract = dataIdentificationAbstract;
    }

    /**
     * Gets the supplemental information.
     *
     * @return the supplemental information
     */
    public String getSupplementalInformation() {
        return supplementalInformation;
    }

    /**
     * Sets the supplemental information.
     *
     * @param supplementalInformation
     *            the new supplemental information
     */
    public void setSupplementalInformation(String supplementalInformation) {
        this.supplementalInformation = supplementalInformation;
    }

    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     *
     * @param language
     *            the new language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the contact.
     *
     * @return the contact
     */
    public CSWResponsibleParty getContact() {
        return contact;
    }

    /**
     * Sets the contact.
     *
     * @param contact
     *            the new contact
     */
    public void setContact(CSWResponsibleParty contact) {
        this.contact = contact;
    }
    
    /**
     * Gets the funder.
     *
     * @return the funder
     */
    public CSWResponsibleParty getFunder() {
        return funder;
    }

    /**
     * Sets the funder.
     *
     * @param funder
     *            the new funder
     */
    public void setFunder(CSWResponsibleParty funder) {
        this.funder = funder;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date.
     *
     * @param date
     *            the new date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Gets the data quality statement.
     *
     * @return the data quality statement
     */
    public String getDataQualityStatement() {
        return dataQualityStatement;
    }

    /**
     * Sets the data quality statement.
     *
     * @param dataQualityStatement
     *            the new data quality statement
     */
    public void setDataQualityStatement(String dataQualityStatement) {
        this.dataQualityStatement = dataQualityStatement;
    }
    
    /**
     * Gets the temporal extent
     * 
     * @return the temporal extent
     */
    public CSWTemporalExtent getTemporalExtent() {
    	return this.temporalExtent;
    }
    
    /**
     * Sets the temporal extent.
     * 
     * @param temporalExtent
     * 			the new temporal extent
     */
    public void setTemporalExtent(CSWTemporalExtent temporalExtent) {
    	this.temporalExtent = temporalExtent;
    }

    /**
     * Sets the child records of this record.
     *
     * @param childRecords
     *            an array of child records for this csw record
     */
    public void addChildRecord(CSWRecord childRecord) {
        this.childRecords.add(childRecord);
    }

    /**
     * Gets the child records.
     *
     * @return the csw child records
     */
    public CSWRecord[] getChildRecords() {
        return childRecords.toArray(new CSWRecord[childRecords.size()]);
    }

    /**
     * Checks to see if this record has any child metadata records.
     *
     * @return true if this record has any child metadata records
     */
    public boolean hasChildRecords() {
        return childRecords.size() > 0;
    }

    /**
     * @return the layerName
     */
    public String getLayerName() {
        return layerName;
    }

    /**
     * @param layerName the layerName to set
     */
    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
    
    /**
     * Get the service ID
     * @return service ID as String
     */
    public String getServiceId() {
    	return serviceId;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CSWRecord [serviceName=" + serviceName + ", onlineResources="
                + onlineResources + ", resourceProvider="
                + resourceProvider + ", fileIdentifier=" + fileIdentifier
                + ", recordInfoUrl=" + recordInfoUrl
                + ", cswGeographicElements="
                + Arrays.toString(cswGeographicElements)
                + ", descriptiveKeywords="
                + Arrays.toString(descriptiveKeywords)
                + ", datasetURIs="
                + Arrays.toString(dataSetURIs)
                + ", dataIdentificationAbstract=" + dataIdentificationAbstract
                + ", supplementalInformation=" + supplementalInformation
                + ", language=" + language + ", constraints="
                + Arrays.toString(constraints) + ", use limit constraints="+ Arrays.toString(useLimitConstraints) + ", access constraints=" + Arrays.toString(accessConstraints)+ ", contact=" + contact
                + (this.funder != null ? "funder: " + funder + ", " : "")
                + ", date=" + date + ", childRecords="
                + childRecords + ", layerName=" + layerName + "]";
    }

    /**
     * Returns a filtered list of online resource protocols that match at least one of the specified types.
     *
     * @param types
     *            The list of types you want to filter by
     * @return the online resources by type
     */
    public List<AbstractCSWOnlineResource> getOnlineResourcesByType(AbstractCSWOnlineResource.OnlineResourceType... types) {
        List<AbstractCSWOnlineResource> result = new ArrayList<>();

        for (AbstractCSWOnlineResource r : onlineResources) {
            if (r == null) {
                continue;
            }
            boolean matching = false;
            AbstractCSWOnlineResource.OnlineResourceType typeToMatch = r.getType();
            for (AbstractCSWOnlineResource.OnlineResourceType type : types) {
                if (typeToMatch == type) {
                    matching = true;
                    break;
                }
            }

            if (matching) {
                result.add(r);
            }
        }

        return result;
    }

    /**
     * Returns a filtered list of online resource protocols that match at least one of the specified types and is accepted by the visitor. Using a
     * CSWRecordsFilterVisitor will open up alot of other filter opportunity in the future without clutering up the code. eg if we need to filter by the
     * onlineResource description, we will just need a visitor that implements CSWRecordsFilterVisitor and apply the appropriate logic.
     *
     * @param types
     *            The list of types you want to filter by
     * @param visitor
     *            visitor to action on the AbstractCSWOnlineResource
     * @return the online resources by type
     */
    public List<AbstractCSWOnlineResource> getOnlineResourcesByType(
            CSWRecordsFilterVisitor visitor,
            AbstractCSWOnlineResource.OnlineResourceType... types) {
        List<AbstractCSWOnlineResource> result = new ArrayList<>();

        for (AbstractCSWOnlineResource r : onlineResources) {
            if (r == null) {
                continue;
            }
            boolean matching = false;
            AbstractCSWOnlineResource.OnlineResourceType typeToMatch = r
                    .getType();
            for (AbstractCSWOnlineResource.OnlineResourceType type : types) {
                if (typeToMatch == type) {
                    matching = true;
                    break;
                }
            }

            if (matching && r.accept(visitor)) {
                result.add(r);
            }
        }

        return result;
    }

    /**
     * Returns true if this CSW Record contains at least 1 onlineResource with ANY of the specified types.
     *
     * @param types
     *            the types
     * @return true, if successful
     */
    public boolean containsAnyOnlineResource(AbstractCSWOnlineResource.OnlineResourceType... types) {

        if (onlineResources == null) {
            return false;
        }

        for (AbstractCSWOnlineResource r : onlineResources) {
            if (r != null) {
                AbstractCSWOnlineResource.OnlineResourceType typeToMatch = r.getType();
                for (AbstractCSWOnlineResource.OnlineResourceType type : types) {
                    if (typeToMatch == type) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns true if this record contains the given descriptive keyword, false otherwise.
     *
     * @param str
     *            the str
     * @return true if this record contains the given descriptive keyword, false otherwise.
     */
    public boolean containsKeyword(String str) {
        return Arrays.asList(descriptiveKeywords).contains(str);
    }

    /**
     * Tests equality of a CSWRecord based on file identifier
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof CSWRecord) {
            return this.getFileIdentifier().equals(((CSWRecord) o).getFileIdentifier());
        } else {
            return super.equals(o);
        }
    }

    public boolean isService() {
        return service;
    }

    public void setService(boolean service) {
        this.service = service;
    }

    /**
     * Creates a hashcode based on this record's file identifier
     */
    @Override
    public int hashCode() {
        return this.fileIdentifier.hashCode();
    }

    public Double getMinScale() {
        return minScale;
    }

    public void setMinScale(Double minScale) {
        this.minScale = minScale;
    }

    public Double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(Double maxScale) {
        this.maxScale = maxScale;
    }
    
    /**
     * Set the service ID
     * @param serviceId service ID as String
     */
    public void setServiceId(String serviceId) {
    	this.serviceId = serviceId;
    }
    
    /**
     * Get IDs of the associated KnownLayers (if the record has any)
     * @return the IDs of the KnownLayers
     */
    public List<String> getKnownLayerIds() {
    	return knownLayerIds;
    }
    
    /**
     * Set the ID of the associated KnownLayer
     * @param knownLayerId ID of associated KnownLayer
     */
    public void setKnownLayerIds(List<String> knownLayerIds) {
    	this.knownLayerIds = knownLayerIds;
    }
    
    /**
     * Adds a KnownLayer ID if not already present in the list
     * @param knownLayerId the KnownLayer ID
     * @return true if the name was unique and added to the list, false otherwise
     */
    public boolean addKnownLayerId(String knownLayerId) {
    	if(knownLayerIds == null) {
    		knownLayerIds = new ArrayList<String>();
    	}
    	if(!knownLayerIds.contains(knownLayerId)) {
    		knownLayerIds.add(knownLayerId);
    		return true;
    	}
    	return false;
    }
    
    /**
     * Get the associated KnownLayer names
     * @return KnownLayer names
     */
    public List<String> getKnownLayerNames() {
    	return knownLayerNames;
    }
    
    /**
     * Set the associated KnownLayer names
     * @param knownLayerNames List of KnownLayer names
     */
    public void setKnownLayerNames(List<String> knownLayerNames) {
    	this.knownLayerNames = knownLayerNames;
    }
    
    /**
     * Add a KnownLayer name
     * @param knownLayerName the KnownLayer name
     */
    public void addKnownLayerName(String knownLayerName) {
    	if(knownLayerNames == null) {
    		knownLayerNames = new ArrayList<String>();
    	}
   		knownLayerNames.add(knownLayerName);
    }
    
    /**
     * Gets associated KnownLayer descriptions
     * @return List of KnownLayer descriptions
     */
    public List<String> getKnownLayerDescriptions() {
    	return knownLayerDescriptions;
    }
    
    /**
     * Set associated KnownLayer descriptions
     * @param knownLayerDescriptions KnownLayer descriptions
     */
    public void setKnownLayerDescriptions(List<String> knownLayerDescriptions) {
    	this.knownLayerDescriptions = knownLayerDescriptions;
    }
    
    /**
     * Add an associated KnownLayer description
     * @param knownLayerDescription the KnownLayer description
     */
    public void addKnownLayerDescription(String knownLayerDescription) {
    	if(knownLayerDescriptions == null) {
    		knownLayerDescriptions = new ArrayList<String>();
    	}
   		knownLayerDescriptions.add(knownLayerDescription);
    }

}
