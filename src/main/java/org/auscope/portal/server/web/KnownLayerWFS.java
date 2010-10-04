package org.auscope.portal.server.web;

import java.awt.Dimension;
import java.awt.Point;

/**
 * An extension of KnownLayer that specializes into representing a collection WFS's
 * @author vot002
 *
 */
public class KnownLayerWFS extends KnownLayer {
    private String featureTypeName;
    private String proxyUrl;
    private String iconUrl;
    private Point iconAnchor;
    private Point infoWindowAnchor;
    private Dimension iconSize;
    private boolean disableBboxFiltering;
    
    /**
     * @param featureTypeName The feature type name used to identify members of this layer
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param proxyUrl The URL that filter requests should be made through
     * @param iconUrl The Icon that will be used to render this feature on the map 
     */
    public KnownLayerWFS(String featureTypeName, String title, 
            String description, String proxyUrl, String iconUrl) {
        this.id = "KnownLayerWFS-" + featureTypeName;
        this.featureTypeName = featureTypeName;
        this.title = title;
        this.description = description;
        this.proxyUrl = proxyUrl;
        this.iconUrl = iconUrl;
        this.disableBboxFiltering = false;
    }
    
    /**
     * @param featureTypeName The feature type name used to identify members of this layer
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param proxyUrl The URL that filter requests should be made through
     * @param iconUrl The Icon that will be used to render this feature on the map 
     * @param iconAnchor  The pixel coordinate relative to the top left corner of 
     *  the icon image at which this icon is anchored to the map.
     * @param infoWindowAnchor  The pixel coordinate relative to the top left corner of
     *  the icon image at which the info window is anchored to the map.
     * @param iconSize  The size of the icon in pixels
     */
    public KnownLayerWFS(String featureTypeName, String title, 
            String description, String proxyUrl, String iconUrl, Point iconAnchor,
            Point infoWindowAnchor, Dimension iconSize) {
        this(featureTypeName, title, description, proxyUrl, iconUrl, iconAnchor, 
             infoWindowAnchor, iconSize, false);
    }    
    
    /**
     * @param featureTypeName The feature type name used to identify members of this layer
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param proxyUrl The URL that filter requests should be made through
     * @param iconUrl The Icon that will be used to render this feature on the map 
     * @param iconAnchor  The pixel coordinate relative to the top left corner of 
     *  the icon image at which this icon is anchored to the map.
     * @param infoWindowAnchor  The pixel coordinate relative to the top left corner of
     *  the icon image at which the info window is anchored to the map.
     * @param iconSize  The size of the icon in pixels
     * @param disableBboxFiltering if true, the GUI will be instructed NOT to use to bounding box filters for this WFS collection
     */
    public KnownLayerWFS(String featureTypeName, String title, 
            String description, String proxyUrl, String iconUrl, Point iconAnchor,
            Point infoWindowAnchor, Dimension iconSize, boolean disableBboxFiltering) {
        this(featureTypeName, title, description, proxyUrl, iconUrl);
        this.iconAnchor = iconAnchor;
        this.infoWindowAnchor = infoWindowAnchor;
        this.iconSize = iconSize;
        this.disableBboxFiltering = disableBboxFiltering;
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

    public String getProxyUrl() {
        return proxyUrl;
    }

    public String getIconUrl() {
        return iconUrl;
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
     * 
     * @return size in pixels of the icon.
     */
    public boolean getDisableBboxFiltering() {
        return this.disableBboxFiltering;
    }
}
