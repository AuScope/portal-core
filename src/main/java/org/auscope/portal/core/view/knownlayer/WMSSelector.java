package org.auscope.portal.core.view.knownlayer;

import java.util.Arrays;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWRecord;

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

    private String[] serviceEndpoints;
    private boolean includeEndpoints;

    /**
     * @param layerName
     *            The layerName that identifies which WMS this KnownLayer is identifying
     */
    public WMSSelector(String layerName) {
        this.layerName = layerName;
    }

    /**
     * @param layerName
     *            The layerName that identifies which WMS this KnownLayer is identifying
     * @param serviceEndpoints
     *            A list of the end points that will either be included or excluded from the WMS, depending on the value of includeEndpoints
     * @param includeEndpoints
     *            A flag indicating whether the listed service end points will be included or excluded from the WMS
     */
    public WMSSelector(String layerName, String[] serviceEndpoints, boolean includeEndpoints) {
        this.layerName = layerName;
        this.serviceEndpoints = serviceEndpoints;
        this.includeEndpoints = includeEndpoints;
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
     * @param layerName
     *            the layerName to set
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
     * @param relatedLayerNames
     *            the new related layer names
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
                //OK we have a match, check we don't explicitly/implicitly exclude it
                //based on its URL
                if (serviceEndpoints != null && serviceEndpoints.length > 0) {
                    boolean matched = false;
                    for (String url : serviceEndpoints) {
                        if (url.equals(onlineResource.getLinkage().toString())) {
                            matched = true;
                            break;
                        }
                    }

                    //Our list of endpoints will be saying either
                    //'Include only this list of urls'
                    //'Exclude any of these urls'
                    if ((includeEndpoints && matched) ||
                            (!includeEndpoints && !matched)) {
                        return RelationType.Belongs;
                    }
                } else {
                    //Otherwise this knownlayer makes no restrictions on URL
                    return RelationType.Belongs;
                }
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

    public String[] getServiceEndpoints() {
        return serviceEndpoints;
    }

    public boolean includeEndpoints() {
        return includeEndpoints;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "WMSSelector [layerName=" + layerName + ", relatedLayerNames=" + Arrays.toString(relatedLayerNames)
                + ", serviceEndpoints=" + Arrays.toString(serviceEndpoints) + ", includeEndpoints=" + includeEndpoints
                + "]";
    }
}
