package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * matches groups of CSWRecords based upon various parameters
 * 
 * @author Josh Vote
 *
 */
public class CSWRecordSelector implements KnownLayerSelector {

    private String[] recordIds;
    private String[] descriptiveKeywords;
    private String[] serviceNames;

    /**
     * @param descriptiveKeyword
     *            the descriptive keyword used to identify CSW records
     */
    public CSWRecordSelector() {
        this.descriptiveKeywords = new String[0];
        this.recordIds = new String[0];
        this.serviceNames = new String[0];
    }

    /**
     * Matches a CSWRecord by an exact matching record id
     * 
     * @return record id string
     */
    public String[] getRecordIds() {
        return recordIds;
    }

    /**
     * Matches a CSWRecord by an exact matching record id
     * 
     * @param recordId record id string
     * @return
     */
    public void setRecordIds(String[] recordIds) {
        this.recordIds = recordIds;
    }

    /**
     * Gets the descriptive keyword used to identify CSW records
     * 
     * @return the descriptiveKeyword
     */
    public String[] getDescriptiveKeywords() {
        return descriptiveKeywords;
    }

    /**
     * Sets the descriptive keyword used to identify CSW records
     * 
     * @param descriptiveKeyword
     *            the descriptiveKeyword to set
     */
    public void setDescriptiveKeywords(String[] descriptiveKeywords) {
        this.descriptiveKeywords = descriptiveKeywords;
    }

    /**
     * Gets the service name (title) used to identify CSW records
     *
     * @return the service name
     */
    public String[] getServiceNames() {
        return serviceNames;
    }

    /**
     * Sets the service name (title) used to identify CSW records
     *
     * @param serviceName the service name string to search for in CSW records
     *
     */
    public void setServiceNames(String[] serviceNames) {
        this.serviceNames = serviceNames;
    }

    /**
     * The relationship is defined as whether the record has a particular descriptive keyword,
     * record id or service name
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        for (String recordId: recordIds) {
            if (recordId.equals(record.getFileIdentifier())) {
                return RelationType.Belongs;
            }
        }
        for (String descriptiveKeyword: descriptiveKeywords) {
            if (record.containsKeyword(descriptiveKeyword)) {
                return RelationType.Belongs;
            }
        }
        for (String serviceName: serviceNames) {
            if (serviceName.equals(record.getServiceName())) {
                return RelationType.Belongs;
            }
        }
        return RelationType.NotRelated;
    }
}
