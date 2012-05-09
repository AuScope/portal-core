package org.auscope.portal.server.web.view.knownlayer;

import org.auscope.portal.csw.record.AbstractCSWOnlineResource;
import org.auscope.portal.csw.record.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.csw.record.CSWRecord;

/**
 * Used for selecting individual WMS's
 *
 * @author Josh Vote
 */
public class WMSSelector implements KnownLayerSelector {

    /** The layer name. */
    private String layerName;

    /** The related layer names. */
    private String[] relatedLayerNames;

    /**
     * @param layerName The layerName that identifies which WMS this KnownLayer is identifying
     */
    public WMSSelector(String layerName) {
       this.layerName = layerName;
    }

    /**
     * Gets the layerName that identifies which WMS this KnownLayer is identifying.
     *
     * @return the layerName
     */
    public String getLayerName() {
        return layerName;
    }

    /**
     * Sets the layerName that identifies which WMS this KnownLayer is identifying.
     *
     * @param layerName the layerName to set
     */
    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    /**
     * Gets all related layer names (layers that are related to this WMS but should not be used for display).
     *
     * @return the related layer names
     */
    public String[] getRelatedLayerNames() {
        return relatedLayerNames;
    }

    /**
     * Sets all related layer names (layers that are related to this WMS but should not be used for display).
     *
     * @param relatedLayerNames the new related layer names
     */
    public void setRelatedLayerNames(String[] relatedLayerNames) {
        this.relatedLayerNames = relatedLayerNames;
    }

    /**
     * Compares records based on related layer names
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        AbstractCSWOnlineResource[] wmsResources = record.getOnlineResourcesByType(OnlineResourceType.WMS);

        //Check for strong association to begin with
        for (AbstractCSWOnlineResource onlineResource : wmsResources) {
            if (layerName.equals(onlineResource.getName())) {
                return RelationType.Belongs;
            }
        }

        //next we check for a weaker relation (does the type name exist in
        //the list of related feature type names?)
        if (relatedLayerNames != null) {
            for (String relatedLayer : relatedLayerNames) {
                for (AbstractCSWOnlineResource onlineResource : wmsResources) {
                    if (onlineResource.getName().equals(relatedLayer)) {
                        return RelationType.Related;
                    }
                }
            }
        }

        return RelationType.NotRelated;
    }
}
