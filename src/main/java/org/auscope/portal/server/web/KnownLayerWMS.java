package org.auscope.portal.server.web;

/**
 * An extension of KnownLayer that specializes it into representing a single WMS
 * @author vot002
 *
 */
public class KnownLayerWMS extends KnownLayer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String layerName;
    protected String styleName;
    protected String[] relatedLayerNames;

    /**
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param layerName The layerName that identifies which WMS this KnownLayer is identifying
     */
    public KnownLayerWMS(String title, String description, String layerName) {
       this.id = "KnownLayerWMS-" + layerName;
       this.title = title;
       this.description = description;
       this.layerName = layerName;
    }

    /**
     * @param title The descriptive title of this layer
     * @param description The extended description of this layer
     * @param layerName The layerName that identifies which WMS this KnownLayer is identifying
     * @param styleName The styleName that should be used when referencing this WMS
     */
    public KnownLayerWMS(String title, String description, String layerName, String styleName) {
       this(title, description, layerName);
       this.styleName = styleName;
    }

    /**
     * Gets the layerName that identifies which WMS this KnownLayer is identifying
     * @return the layerName
     */
    public String getLayerName() {
        return layerName;
    }

    /**
     * Sets the layerName that identifies which WMS this KnownLayer is identifying
     * @param layerName the layerName to set
     */
    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    /**
     * Gets the optional style name used when requesting this WMS (can be null)
     *
     * @return the styleName
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * Sets the optional style name used when requesting this WMS (can be null)
     *
     * @param styleName the styleName to set (can be null)
     */
    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    /**
     * Gets all related layer names (layers that are related to this WMS but should not be used for display)
     * @return
     */
    public String[] getRelatedLayerNames() {
        return relatedLayerNames;
    }

    /**
     * Sets all related layer names (layers that are related to this WMS but should not be used for display)
     * @param relatedLayerNames
     */
    public void setRelatedLayerNames(String[] relatedLayerNames) {
        this.relatedLayerNames = relatedLayerNames;
    }


}
