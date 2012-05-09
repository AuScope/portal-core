package org.auscope.portal.server.web.view.knownlayer;

import org.auscope.portal.csw.record.CSWRecord;

/**
 * matches groups of CSWRecords based upon the inclusion
 * of a descriptive keyword.
 * @author Josh Vote
 *
 */
public class CSWKeywordSelector implements KnownLayerSelector {

    private String descriptiveKeyword;

    /**
     * @param title a descriptive title of this layer
     * @param description an extended description of this layer
     * @param descriptiveKeyword the descriptive keyword used to identify CSW records
     */
    public CSWKeywordSelector(String descriptiveKeyword) {
        this.descriptiveKeyword = descriptiveKeyword;
    }

    /**
     * Gets the descriptive keyword used to identify CSW records
     * @return the descriptiveKeyword
     */
    public String getDescriptiveKeyword() {
        return descriptiveKeyword;
    }

    /**
     * Sets the descriptive keyword used to identify CSW records
     * @param descriptiveKeyword the descriptiveKeyword to set
     */
    public void setDescriptiveKeyword(String descriptiveKeyword) {
        this.descriptiveKeyword = descriptiveKeyword;
    }

    /**
     * The relationship is defined on whether the record has a particular
     * descriptive keyword
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        if (record.containsKeyword(descriptiveKeyword)) {
            return RelationType.Belongs;
        }

        return RelationType.NotRelated;
    }
}
