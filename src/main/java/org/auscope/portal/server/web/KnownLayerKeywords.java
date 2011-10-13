package org.auscope.portal.server.web;

import java.awt.Dimension;
import java.awt.Point;

/**
 * Extends KnownLayer and specializes into identifying groups of CSWRecords based upon the inclusion
 * of a descriptive keyword.
 * @author vot002
 *
 */
public class KnownLayerKeywords extends KnownLayer {

	private static final long serialVersionUID = 1L;
	protected String descriptiveKeyword;
    protected String iconUrl;
    protected Point iconAnchor;
    protected Dimension iconSize;

    /**
     * @param title a descriptive title of this layer
     * @param description an extended description of this layer
     * @param descriptiveKeyword the descriptive keyword used to identify CSW records
     */
    public KnownLayerKeywords(String title, String description, String descriptiveKeyword,
            String iconUrl, Point iconAnchor, Dimension iconSize) {
        this.id = "KnownLayerKeywords-" + descriptiveKeyword;
        this.title = title;
        this.description = description;
        this.descriptiveKeyword = descriptiveKeyword;
        this.iconUrl = iconUrl;
        this.iconAnchor = iconAnchor;
        this.iconSize = iconSize;
    }

    /**
     * Gets the descriptive keyword used to identify CSW records
     * @return the descriptiveKeyword
     */
    public String getDescriptiveKeyword() {
        return descriptiveKeyword;
    }

    /**
     *
     * @return
     */
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
     * Sets the descriptive keyword used to identify CSW records
     * @param descriptiveKeyword the descriptiveKeyword to set
     */
    public void setDescriptiveKeyword(String descriptiveKeyword) {
        this.descriptiveKeyword = descriptiveKeyword;
    }


}
