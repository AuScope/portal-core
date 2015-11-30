package org.auscope.portal.core.services.responses.wfs;

import java.util.Map;

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
    
    /** A map of abstracts that we can look up by feature name */
    private Map<String, String> featureAbstracts;
    
    /** A map of metaDataURLs that we can look up by feature name */
    private Map<String, String> metadataURLs;
    
    
    public WFSGetCapabilitiesResponse() {
        this(null, null, null, null);
    }

    /**
     * 
     * @param getFeatureOutputFormats
     *            Supported output formats for GetFeature operations
     * @param featureTypes
     *            A list of feature type names (no additional metadata included)
     */
    public WFSGetCapabilitiesResponse(
            String[] getFeatureOutputFormats,
            String[] featureTypes,
            Map<String, String> featureAbstracts, 
            Map<String, String> metadataURLs) {
        super();
        this.getFeatureOutputFormats = getFeatureOutputFormats;
        this.featureTypes = featureTypes;
        this.featureAbstracts = featureAbstracts;
        this.metadataURLs = metadataURLs;
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
    
    /**
     * A list of feature abstracts (no additional metadata included)
     * 
     * @return
     */
    public Map<String, String> getFeatureAbstracts() {
        return featureAbstracts;
    }

    /**
     * A list of feature abstracts (no additional metadata included)
     * 
     * @param featureAbstracts
     */
    public void setFeatureAbstracts(Map<String, String> featureAbstracts) {
        this.featureAbstracts = featureAbstracts;
    }    
    
    /**
     * A list of feature abstracts (no additional metadata included)
     * 
     * @return
     */
    public Map<String, String> getMetadataURLs() {
        return metadataURLs;
    }

    /**
     * A list of feature abstracts (no additional metadata included)
     * 
     * @param featureAbstracts
     */
    public void setMetadataURLs(Map<String, String> metadataURLs) {
        this.metadataURLs = metadataURLs;
    }    
}
