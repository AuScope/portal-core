package org.auscope.portal.server.web;

import java.awt.Dimension;
import java.awt.Point;

/**
 * An extension of KnownLayer that specializes into representing a collection WFS's
 * @author vot002
 *
 */
public class KnownLayerWFS extends AbstractKnownLayer {

    private static final long serialVersionUID = 1L;
    private String featureTypeName;
    private String proxyFetchUrl;
    private String proxyCountUrl;
    private String iconUrl;
    private String[] serviceEndpoints;
    private boolean includeEndpoints;
    private Point iconAnchor;
    private Point infoWindowAnchor;
    private Dimension iconSize;
    private boolean disableBboxFiltering;
    private String[] relatedFeatureTypeNames;


    /**
     * @param featureTypeName The feature type name used to identify members of this layer
     * @param title The descriptive title of this layer
     */
    public KnownLayerWFS(String featureTypeName, String title) {
        this.id = "KnownLayerWFS-" + featureTypeName + "-" + title.replace(" ", "_");
        this.featureTypeName = featureTypeName;
    }

    /**
     * @param featureTypeName The feature type name used to identify members of this layer
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param proxyFetchUrl The URL that filter requests should be made through for the entire record set
     * @param proxyCountUrl The URL that filter requests should be made through for the count of records
     * @param iconUrl The Icon that will be used to render this feature on the map
     */
    public KnownLayerWFS(String featureTypeName, String title,
            String description, String proxyFetchUrl, String proxyCountUrl, String iconUrl) {
        this(featureTypeName, title);
        this.title = title;
        this.description = description;
        this.proxyFetchUrl = proxyFetchUrl;
        this.proxyCountUrl = proxyCountUrl;
        this.iconUrl = iconUrl;
        this.disableBboxFiltering = false;
    }

    /**
     * @param featureTypeName The feature type name used to identify members of this layer
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param proxyFetchUrl The URL that filter requests should be made through for the entire record set
     * @param proxyCountUrl The URL that filter requests should be made through for the count of records
     * @param iconUrl The Icon that will be used to render this feature on the map
     * @param iconAnchor  The pixel coordinate relative to the top left corner of
     *  the icon image at which this icon is anchored to the map.
     * @param infoWindowAnchor  The pixel coordinate relative to the top left corner of
     *  the icon image at which the info window is anchored to the map.
     * @param iconSize  The size of the icon in pixels
     */
    public KnownLayerWFS(String featureTypeName, String title,
            String description, String proxyFetchUrl, String proxyCountUrl, String iconUrl, Point iconAnchor,
            Point infoWindowAnchor, Dimension iconSize) {
        this(featureTypeName, title, description, proxyFetchUrl, proxyCountUrl, iconUrl, null, false, iconAnchor,
             infoWindowAnchor, iconSize, false);
    }

    /**
     * @param featureTypeName The feature type name used to identify members of this layer
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param proxyFetchUrl The URL that filter requests should be made through for the entire record set
     * @param proxyCountUrl The URL that filter requests should be made through for the count of records
     * @param iconUrl The Icon that will be used to render this feature on the map
     * @param serviceEndpoints A list of the end points that will either be included or
     *  excluded from the WFS, depending on the value of includeEndpoints
     * @param includeEndpoints A flag indicating whether the listed service end points
     *  will be included or excluded from the WFS
     * @param iconAnchor  The pixel coordinate relative to the top left corner of
     *  the icon image at which this icon is anchored to the map.
     * @param infoWindowAnchor  The pixel coordinate relative to the top left corner of
     *  the icon image at which the info window is anchored to the map.
     * @param iconSize  The size of the icon in pixels
     * @param disableBboxFiltering if true, the GUI will be instructed NOT to use to bounding box filters for this WFS collection
     */
    public KnownLayerWFS(String featureTypeName, String title,
            String description, String proxyFetchUrl, String proxyCountUrl, String iconUrl, String[] serviceEndpoints,
            boolean includeEndpoints, Point iconAnchor,
            Point infoWindowAnchor, Dimension iconSize, boolean disableBboxFiltering) {
        this(featureTypeName, title, description, proxyFetchUrl, proxyCountUrl, iconUrl);
        this.iconAnchor = iconAnchor;
        this.infoWindowAnchor = infoWindowAnchor;
        this.iconSize = iconSize;
        this.disableBboxFiltering = disableBboxFiltering;
        this.serviceEndpoints = serviceEndpoints;
        this.includeEndpoints = includeEndpoints;
    }

    public String getFeatureTypeName() {
        return featureTypeName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getProxyFetchUrl() {
        return proxyFetchUrl;
    }

    public String getProxyCountUrl() {
        return proxyCountUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String[] getServiceEndpoints() {
        return serviceEndpoints;
    }

    public boolean includeEndpoints() {
        return includeEndpoints;
    }

    /**
     * Gets the pixel coordinates relative to the top left corner of the icon
     * image at which this icon is anchored to the map.
     * Can be null.
     *
     * @return pixel coordinates at which this icon is anchored to the map.
     */
    public Point getIconAnchor() {
        if (iconAnchor == null) {
            return null;
        } else {
            return new Point(iconAnchor);
        }
    }

    /**
     * Gets the pixel coordinates relative to the top left corner of the icon
     * image at which the info window is anchored to the map.
     * Can be null.
     *
     * @return pixel coordinates at which the info window is anchored to the map.
     */
    public Point getInfoWindowAnchor() {
        if (infoWindowAnchor == null) {
            return null;
        } else {
            return new Point(infoWindowAnchor);
        }
    }

    /**
     * Gets the size in pixels of the icon.
     * Can be null.
     *
     * @return size in pixels of the icon.
     */
    public Dimension getIconSize() {
        if (iconSize == null) {
            return null;
        } else {
        return iconSize;
        }
    }

    /**
     * Gets whether bounding box filtering should be disabled for this collection
     * of services
     */
    public boolean getDisableBboxFiltering() {
        return this.disableBboxFiltering;
    }

    /**
     * Sets whether bounding box filtering should be disabled for this collection
     * of services
     */
    public void setDisableBboxFiltering(boolean disableBboxFiltering) {
        this.disableBboxFiltering = disableBboxFiltering;
    }

    /**
     * @return the relatedFeatureTypeNames
     */
    public String[] getRelatedFeatureTypeNames() {
        return relatedFeatureTypeNames;
    }

    /**
     * @param relatedFeatureTypeNames the relatedFeatureTypeNames to set
     */
    public void setRelatedFeatureTypeNames(String[] relatedFeatureTypeNames) {
        this.relatedFeatureTypeNames = relatedFeatureTypeNames;
    }


}
