package org.auscope.portal.core.view.knownlayer;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWRecord;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Used for selecting individual KMLs
 *
 */
public class KMLSelector implements KnownLayerSelector {

    /** The URL of the KML file to be displayed */
    private URL serviceEndpoint;

    /**
     * @param serviceEndpoint
     *            The serviceEndpoint that identifies which KML this KnownLayer is identifying
     */
    public KMLSelector(String serviceEndpoint) throws MalformedURLException {
        try {
            this.serviceEndpoint = new URL(serviceEndpoint);
        } catch (MalformedURLException muex) {
            
        }
        
    }

    /**
     * Gets the layerName that identifies which WMS this KnownLayer is identifying.
     *
     * @return the service end point
     */
    public URL getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    /**
     * Sets the layerName that identifies which WMS this KnownLayer is identifying.
     *
     * @param layerName
     *            the service end point to set
     */
    public void setServiceEndpoint(String serviceEndpoint) throws MalformedURLException {
        this.serviceEndpoint = new URL(serviceEndpoint);
    }


    /**
     * Returns a RelationType enum that indicates the relationship between the record provided and the service endpoint that this IRISSelector was instantiated
     * with.
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        AbstractCSWOnlineResource[] onlineResources = record.getOnlineResourcesByType(OnlineResourceType.KML);
        if (onlineResources.length > 0) {
            for (AbstractCSWOnlineResource onlineResource : onlineResources) {
                if (serviceEndpoint.sameFile(onlineResource.getLinkage())) {
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
        return "KMLSelector [serviceEndpoint=" + this.serviceEndpoint + "]";
    }
}
