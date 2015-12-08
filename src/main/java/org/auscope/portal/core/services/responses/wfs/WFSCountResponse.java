package org.auscope.portal.core.services.responses.wfs;

/**
 * Represents a Web Feature Service GetFeature response for a request that specified responseType=hits
 * 
 * @author Josh Vote
 *
 */
public class WFSCountResponse {
    /** the number of features matched */
    private int numberOfFeatures;

    /**
     * Creates a new instance
     * 
     * @param numberOfFeatures
     *            the number of features matched
     */
    public WFSCountResponse(int numberOfFeatures) {
        this.numberOfFeatures = numberOfFeatures;
    }

    /**
     * Gets the number of features matched
     * 
     * @return
     */
    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }
}
