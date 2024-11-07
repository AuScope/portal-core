package org.auscope.portal.core.view.knownlayer;

import java.net.MalformedURLException;
import java.net.URL;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.json.JSONArray;

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
    /** The apikey -  https://api-docs.native-land.ca/get-and-use-your-api-key */
    private String apikey;
    /** The maps - There are 3 categories available: territories, languages, and treaties. */
    private String maps;

    /**
     * @param serviceEndpoint
     *            The serviceEndpoint that identifies which VMF this KnownLayer is identifying
     */
    public VMFSelector(String layerName, String serviceEndpoint, JSONArray polygonGeoJson, String apikey, String maps) throws MalformedURLException {
            this.layerName = layerName;
            this.serviceEndpoint = new URL(serviceEndpoint);
            this.polygonGeoJson = polygonGeoJson;
            this.apikey = apikey;
            this.maps = maps;
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
