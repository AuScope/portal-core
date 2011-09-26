package org.auscope.portal.csw.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a parsed gmd:MD_Metadata element that is received as part of an OGC CSW transaction
 *
 * @author Mathew Wyatt
 * @author Joshua Vote
 */
public class CSWRecord {

    private static final Log logger = LogFactory.getLog(CSWRecord.class);

    private String serviceName;
    private CSWOnlineResource[] onlineResources;
    private String resourceProvider;
    private String fileIdentifier;
    private String recordInfoUrl;
    private CSWGeographicElement[] cswGeographicElements;
    private String[] descriptiveKeywords;
    private String dataIdentificationAbstract;
    private String supplementalInformation;
    private String language;
    private String[] constraints;
    private CSWResponsibleParty contact;
    private Date date;
    private String dataQualityStatement;

    public CSWRecord(String serviceName, String fileIdentifier,
            String recordInfoUrl, String dataIdentificationAbstract,
            CSWOnlineResource[] onlineResources, CSWGeographicElement[] cswGeographicsElements) {
        this.serviceName = serviceName;
        this.fileIdentifier = fileIdentifier;
        this.recordInfoUrl = recordInfoUrl;
        this.dataIdentificationAbstract = dataIdentificationAbstract;
        this.onlineResources = onlineResources;
        this.cswGeographicElements = cswGeographicsElements;
        this.supplementalInformation = "";
        this.descriptiveKeywords = new String[0];
        this.constraints = new String[0];
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

    public void setConstraints(String[] constraints) {
        this.constraints = constraints;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setOnlineResources(CSWOnlineResource[] onlineResources) {
        this.onlineResources = onlineResources;
    }

    public void setResourceProvider(String resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public void setDescriptiveKeywords(String[] descriptiveKeywords) {
        this.descriptiveKeywords = descriptiveKeywords;
    }

    public void setDataIdentificationAbstract(String dataIdentificationAbstract) {
        this.dataIdentificationAbstract = dataIdentificationAbstract;
    }

    public String getSupplementalInformation() {
        return supplementalInformation;
    }

    public void setSupplementalInformation(String supplementalInformation) {
        this.supplementalInformation = supplementalInformation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public CSWResponsibleParty getContact() {
        return contact;
    }

    public void setContact(CSWResponsibleParty contact) {
        this.contact = contact;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDataQualityStatement() {
        return dataQualityStatement;
    }

    public void setDataQualityStatement(String dataQualityStatement) {
        this.dataQualityStatement = dataQualityStatement;
    }

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

        if (onlineResources == null) {
            return false;
        }

        for (CSWOnlineResource r : onlineResources) {
            if (r != null) {
                CSWOnlineResource.OnlineResourceType typeToMatch = r.getType();
                for (CSWOnlineResource.OnlineResourceType type : types) {
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
     * @return true if this record contains the given descriptive keyword, false otherwise.
     */
    public boolean containsKeyword(String str) {
        return Arrays.asList(descriptiveKeywords).contains(str);
    }
}
