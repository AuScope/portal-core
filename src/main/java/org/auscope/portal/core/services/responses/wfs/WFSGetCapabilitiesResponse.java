package org.auscope.portal.core.services.responses.wfs;

/**
 * A highly simplified representation of a Web Feature Service GetCapabilities response
 * 
 * This only includes a partial representation of the entire document for simplicity
 * 
 * @author Josh Vote
 *
 */
public class WFSGetCapabilitiesResponse {
    /** Supported output formats for GetFeature operations */
    private String[] getFeatureOutputFormats;
    /** A list of feature type names (no additional metadata included) */
    private String[] featureTypes;

    public WFSGetCapabilitiesResponse() {
        this(null, null);
    }

    /**
     * 
     * @param getFeatureOutputFormats
     *            Supported output formats for GetFeature operations
     * @param featureTypes
     *            A list of feature type names (no additional metadata included)
     */
    public WFSGetCapabilitiesResponse(String[] getFeatureOutputFormats,
            String[] featureTypes) {
        super();
        this.getFeatureOutputFormats = getFeatureOutputFormats;
        this.featureTypes = featureTypes;
    }

    /**
     * Supported output formats for GetFeature operations
     * 
     * @return
     */
    public String[] getGetFeatureOutputFormats() {
        return getFeatureOutputFormats;
    }

    /**
     * Supported output formats for GetFeature operations
     * 
     * @param getFeatureOutputFormats
     */
    public void setGetFeatureOutputFormats(String[] getFeatureOutputFormats) {
        this.getFeatureOutputFormats = getFeatureOutputFormats;
    }

    /**
     * A list of feature type names (no additional metadata included)
     * 
     * @return
     */
    public String[] getFeatureTypes() {
        return featureTypes;
    }

    /**
     * A list of feature type names (no additional metadata included)
     * 
     * @param featureTypes
     */
    public void setFeatureTypes(String[] featureTypes) {
        this.featureTypes = featureTypes;
    }
}
