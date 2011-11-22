package org.auscope.portal.csw.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.service.CSWRecordsFilterVisitor;

// TODO: Auto-generated Javadoc
/**
 * Represents a parsed gmd:MD_Metadata element that is received as part of an OGC CSW transaction.
 *
 * @author Mathew Wyatt
 * @author Joshua Vote
 * @version $Id$
 */
public class CSWRecord {

    /** The Constant logger. */
    private static final Log logger = LogFactory.getLog(CSWRecord.class);

    /** The service name. */
    private String serviceName;

    /** The online resources. */
    private AbstractCSWOnlineResource[] onlineResources;

    /** The resource provider. */
    private String resourceProvider;

    /** The file identifier. */
    private String fileIdentifier;

    /** The record info url. */
    private String recordInfoUrl;

    /** The csw geographic elements. */
    private CSWGeographicElement[] cswGeographicElements;

    /** The descriptive keywords. */
    private String[] descriptiveKeywords;

    /** The data identification abstract. */
    private String dataIdentificationAbstract;

    /** The supplemental information. */
    private String supplementalInformation;

    /** The language. */
    private String language;

    /** The constraints. */
    private String[] constraints;

    /** The contact. */
    private CSWResponsibleParty contact;

    /** The date. */
    private Date date;

    /** The data quality statement. */
    private String dataQualityStatement;

    /**
     * Instantiates a new cSW record.
     *
     * @param serviceName the service name
     * @param fileIdentifier the file identifier
     * @param recordInfoUrl the record info url
     * @param dataIdentificationAbstract the data identification abstract
     * @param onlineResources the online resources
     * @param cswGeographicsElements the csw geographics elements
     */
    public CSWRecord(String serviceName, String fileIdentifier,
            String recordInfoUrl, String dataIdentificationAbstract,
            AbstractCSWOnlineResource[] onlineResources, CSWGeographicElement[] cswGeographicsElements) {
        this.serviceName = serviceName;
        this.fileIdentifier = fileIdentifier;
        this.recordInfoUrl = recordInfoUrl;
        this.dataIdentificationAbstract = dataIdentificationAbstract;
        this.onlineResources = onlineResources;
        this.cswGeographicElements = cswGeographicsElements;
        this.supplementalInformation = "";
        this.descriptiveKeywords = new String[0];
        this.constraints = new String[0];

        logger.trace(this.toString());
    }

    /**
     * Sets the record info url.
     *
     * @param recordInfoUrl the new record info url
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
     * Gets the file identifier.
     *
     * @return the file identifier
     */
    public String getFileIdentifier() {
        return fileIdentifier;
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
    public AbstractCSWOnlineResource[] getOnlineResources() {
        return onlineResources;
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
     * @param cswGeographicElements the new cSW geographic elements
     */
    public void setCSWGeographicElements(CSWGeographicElement[] cswGeographicElements) {
        this.cswGeographicElements = cswGeographicElements;
    }

    /**
     * gets the  CSWGeographicElement that bounds this record (or null if it DNE).
     *
     * @return the cSW geographic elements
     */
    public CSWGeographicElement[] getCSWGeographicElements() {
        return cswGeographicElements;
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
     * @param constraints the new constraints
     */
    public void setConstraints(String[] constraints) {
        this.constraints = constraints;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName the new service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets the online resources.
     *
     * @param onlineResources the new online resources
     */
    public void setOnlineResources(AbstractCSWOnlineResource[] onlineResources) {
        this.onlineResources = onlineResources;
    }

    /**
     * Sets the resource provider.
     *
     * @param resourceProvider the new resource provider
     */
    public void setResourceProvider(String resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    /**
     * Sets the file identifier.
     *
     * @param fileIdentifier the new file identifier
     */
    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    /**
     * Sets the descriptive keywords.
     *
     * @param descriptiveKeywords the new descriptive keywords
     */
    public void setDescriptiveKeywords(String[] descriptiveKeywords) {
        this.descriptiveKeywords = descriptiveKeywords;
    }

    /**
     * Sets the data identification abstract.
     *
     * @param dataIdentificationAbstract the new data identification abstract
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
     * @param supplementalInformation the new supplemental information
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
     * @param language the new language
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
     * @param contact the new contact
     */
    public void setContact(CSWResponsibleParty contact) {
        this.contact = contact;
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
     * @param date the new date
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
     * @param dataQualityStatement the new data quality statement
     */
    public void setDataQualityStatement(String dataQualityStatement) {
        this.dataQualityStatement = dataQualityStatement;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CSWRecord [serviceName=" + serviceName + ", onlineResources="
                + Arrays.toString(onlineResources) + ", resourceProvider="
                + resourceProvider + ", fileIdentifier=" + fileIdentifier
                + ", recordInfoUrl=" + recordInfoUrl
                + ", cswGeographicElements="
                + Arrays.toString(cswGeographicElements)
                + ", descriptiveKeywords="
                + Arrays.toString(descriptiveKeywords)
                + ", dataIdentificationAbstract=" + dataIdentificationAbstract
                + ", supplementalInformation=" + supplementalInformation
                + ", language=" + language + ", constraints="
                + Arrays.toString(constraints) + ", contact=" + contact
                + ", date=" + date + "]";
    }

    /**
     * Returns a filtered list of online resource protocols that match at least one of the specified types.
     *
     * @param types The list of types you want to filter by
     * @return the online resources by type
     */
    public AbstractCSWOnlineResource[] getOnlineResourcesByType(AbstractCSWOnlineResource.OnlineResourceType... types) {
        List <AbstractCSWOnlineResource> result = new ArrayList<AbstractCSWOnlineResource>();

        for (AbstractCSWOnlineResource r : onlineResources) {
            if (r == null){
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

        return result.toArray(new AbstractCSWOnlineResource[result.size()]);
    }

    /**
     * Returns a filtered list of online resource protocols that match at least
     * one of the specified types and is accepted by the visitor.
     *
     * @param types
     *            The list of types you want to filter by
     * @param visitor visitor to action on the AbstractCSWOnlineResource
     * @return the online resources by type
     */
    public AbstractCSWOnlineResource[] getOnlineResourcesByType(
            CSWRecordsFilterVisitor visitor,
            AbstractCSWOnlineResource.OnlineResourceType... types) {
        List<AbstractCSWOnlineResource> result = new ArrayList<AbstractCSWOnlineResource>();

        for (AbstractCSWOnlineResource r : onlineResources) {
            if (r == null){
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

        return result.toArray(new AbstractCSWOnlineResource[result.size()]);
    }

    /**
     * Returns true if this CSW Record contains at least 1 onlineResource with ANY of the specified types.
     *
     * @param types the types
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
     * @param str the str
     * @return true if this record contains the given descriptive keyword, false otherwise.
     */
    public boolean containsKeyword(String str) {
        return Arrays.asList(descriptiveKeywords).contains(str);
    }

}
