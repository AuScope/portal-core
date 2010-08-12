package org.auscope.portal.server.web;

import java.awt.Dimension;
import java.awt.Point;

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
    private Point iconAnchor;
    private Point infoWindowAnchor;
    private Dimension iconSize;

	public KnownFeatureTypeDefinition(String featureTypeName, String displayName, 
    		String description, String proxyUrl, String iconUrl) {
        this.featureTypeName = featureTypeName;
        this.displayName = displayName;
        this.description = description;
        this.proxyUrl = proxyUrl;
        this.iconUrl = iconUrl;
    }
    
    /**
     * @param featureTypeName
     * @param displayName
     * @param description
     * @param proxyUrl
     * @param iconUrl
     * @param iconAnchor  The pixel coordinate relative to the top left corner of 
     * 	the icon image at which this icon is anchored to the map.
     * @param infoWindowAnchor  The pixel coordinate relative to the top left corner of
     * 	the icon image at which the info window is anchored to the map.
     * @param iconSize	The size of the icon in pixels
     */
    public KnownFeatureTypeDefinition(String featureTypeName, String displayName, 
    		String description, String proxyUrl, String iconUrl, Point iconAnchor,
    		Point infoWindowAnchor,	Dimension iconSize) {
        this.featureTypeName = featureTypeName;
        this.displayName = displayName;
        this.description = description;
        this.proxyUrl = proxyUrl;
        this.iconUrl = iconUrl;
        this.iconAnchor = iconAnchor;
        this.infoWindowAnchor = infoWindowAnchor;
        this.iconSize = iconSize;
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
    
}
