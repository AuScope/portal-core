package org.auscope.portal.server.web;

/**
 * Extends KnownLayer and specializes into identifying groups of CSWRecords based upon the inclusion
 * of a descriptive keyword.
 * @author vot002
 *
 */
public class KnownLayerKeywords extends KnownLayer {
    protected String descriptiveKeyword;
    
    /**
     * @param title a descriptive title of this layer
     * @param description an extended description of this layer
     * @param descriptiveKeyword the descriptive keyword used to identify CSW records 
     */
    public KnownLayerKeywords(String title, String description, String descriptiveKeyword) {
        this.id = "KnownLayerKeywords-" + descriptiveKeyword;
        this.title = title;
        this.description = description;
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
    
    
}
