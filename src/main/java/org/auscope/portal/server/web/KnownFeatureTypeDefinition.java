package org.auscope.portal.server.web;

/**
 * User: Mathew Wyatt
 * Date: 27/08/2009
 * Time: 11:08:17 AM
 */
public class KnownFeatureTypeDefinition {
    private String featureTypeName;
    private String displayName;
    private String description;
    private String proxyUrl;
    private String iconUrl;

    public KnownFeatureTypeDefinition(String featureTypeName, String displayName, String description, String proxyUrl, String iconUrl) {
        this.featureTypeName = featureTypeName;
        this.displayName = displayName;
        this.description = description;
        this.proxyUrl = proxyUrl;
        this.iconUrl = iconUrl;
    }

    public String getFeatureTypeName() {
        return featureTypeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
