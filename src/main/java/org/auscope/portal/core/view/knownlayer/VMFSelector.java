package org.auscope.portal.core.view.knownlayer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.json.JSONArray;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector.RelationType;

public class VMFSelector implements KnownLayerSelector {
    

    /** The layer name. */
    private String layerName;
    /**
     * The service endpoint that the instance of the selector is concerned with.
     */
    private URL serviceEndpoint;
    /**
     * GeoJson polygon used to cookie cut area of interest
     */
    private JSONArray polygonGeoJson;
    

    /**
     * @param serviceEndpoint
     *            The serviceEndpoint that identifies which VMF this KnownLayer is identifying
     */
    public VMFSelector(String layerName, String serviceEndpoint, JSONArray polygonGeoJson) throws MalformedURLException {
            this.layerName = layerName;
            this.serviceEndpoint = new URL(serviceEndpoint);
            this.polygonGeoJson = polygonGeoJson;
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
     * The relationship is defined as whether the record has a particular layer name
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        

        if (layerName.equals(record.getLayerName())) {
            return RelationType.Belongs;
        }

        return RelationType.NotRelated;
    }    

}
