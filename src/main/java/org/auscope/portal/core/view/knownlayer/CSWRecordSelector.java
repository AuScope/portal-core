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

    /**
     * @param descriptiveKeyword
     *            the descriptive keyword used to identify CSW records
     */
    public CSWRecordSelector() {
        this.descriptiveKeyword = null;
        this.recordId = null;
    }

    /**
     * Matches a CSWRecord by an exact matching record id
     * 
     * @return
     */
    public String getRecordId() {
        return recordId;
    }

    /**
     * Matches a CSWRecord by an exact matching record id
     * 
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
     * The relationship is defined on whether the record has a particular descriptive keyword
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        if (recordId != null && recordId.equals(record.getFileIdentifier())) {
            return RelationType.Belongs;
        }

        if (descriptiveKeyword != null && record.containsKeyword(descriptiveKeyword)) {
            return RelationType.Belongs;
        }

        return RelationType.NotRelated;
    }
}
