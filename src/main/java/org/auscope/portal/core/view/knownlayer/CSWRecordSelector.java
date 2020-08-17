package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * matches groups of CSWRecords based upon various parameters
 * 
 * @author Josh Vote
 *
 */
public class CSWRecordSelector implements KnownLayerSelector {

    private String recordId;
    private String descriptiveKeyword;
    private String serviceName;

    /**
     * @param descriptiveKeyword
     *            the descriptive keyword used to identify CSW records
     */
    public CSWRecordSelector() {
        this.descriptiveKeyword = null;
        this.recordId = null;
        this.serviceName = null;
    }

    /**
     * Matches a CSWRecord by an exact matching record id
     * 
     * @return record id string
     */
    public String getRecordId() {
        return recordId;
    }

    /**
     * Matches a CSWRecord by an exact matching record id
     * 
     * @param recordId record id string
     * @return
     */
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    /**
     * Gets the descriptive keyword used to identify CSW records
     * 
     * @return the descriptiveKeyword
     */
    public String getDescriptiveKeyword() {
        return descriptiveKeyword;
    }

    /**
     * Sets the descriptive keyword used to identify CSW records
     * 
     * @param descriptiveKeyword
     *            the descriptiveKeyword to set
     */
    public void setDescriptiveKeyword(String descriptiveKeyword) {
        this.descriptiveKeyword = descriptiveKeyword;
    }

    /**
     * Gets the service name (title) used to identify CSW records
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name (title) used to identify CSW records
     *
     * @param serviceName the service name string to search for in CSW records
     *
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * The relationship is defined as whether the record has a particular descriptive keyword,
     * record id or service name
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        if (recordId != null && recordId.equals(record.getFileIdentifier())) {
            return RelationType.Belongs;
        }

        if (descriptiveKeyword != null && record.containsKeyword(descriptiveKeyword)) {
            return RelationType.Belongs;
        }

	if (serviceName != null && serviceName.equals(record.getServiceName())) {
            return RelationType.Belongs;
	}

        return RelationType.NotRelated;
    }
}
