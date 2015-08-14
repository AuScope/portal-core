package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * matches groups of CSWRecords based upon various parameters
 * 
 * @author Victor Tey
 *
 */
public class CSWTitleFilterRecordSelector implements KnownLayerSelector {

    private String recordId;
    private String title;

    /**
     * @param descriptiveKeyword
     *            the descriptive keyword used to identify CSW records
     */
    public CSWTitleFilterRecordSelector() {
        this.title = null;
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
    public String getTitle() {
        return title;
    }

    /**
     * Sets the descriptive keyword used to identify CSW records
     * 
     * @param descriptiveKeyword
     *            the descriptiveKeyword to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The relationship is defined on whether the record has a particular descriptive keyword
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        if (recordId != null && recordId.equals(record.getFileIdentifier())) {
            return RelationType.Belongs;
        }

        if (title != null && record.getServiceName().contains(title)) {
            return RelationType.Belongs;
        }

        return RelationType.NotRelated;
    }
}
