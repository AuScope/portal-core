package org.auscope.portal.core.view.knownlayer;

import java.util.List;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * Used for selecting individual KML layers using the name of the layer
 *
 */
public class KMLSelector implements KnownLayerSelector {

    /** The URL of the KML file to be displayed */
    private String layerName;

    /**
     * @param serviceEndpoint
     *            The serviceEndpoint that identifies which KML this KnownLayer is identifying
     */
    public KMLSelector(String layerName) {
            this.layerName = layerName;
    }

    /**
     * Gets the layerName that identifies which WMS this KnownLayer is identifying.
     *
     * @return the service end point
     */
    public String getLayerName() {
        return this.layerName;
    }

    /**
     * Sets the layerName that identifies which WMS this KnownLayer is identifying.
     *
     * @param layerName
     *            the service end point to set
     */
    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }


    /**
     * Returns a RelationType enum that indicates the relationship between the record provided and the service endpoint that this IRISSelector was instantiated
     * with.
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        List<AbstractCSWOnlineResource> onlineResources = record.getOnlineResourcesByType(OnlineResourceType.KML);
        if (onlineResources.size() > 0) {
            for (AbstractCSWOnlineResource onlineResource : onlineResources) {
                if (this.layerName.equals(onlineResource.getName())) {
                    return RelationType.Belongs;
                }
            }
        }
        return RelationType.NotRelated;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "KMLSelector [layerName=" + this.layerName + "]";
    }
}
