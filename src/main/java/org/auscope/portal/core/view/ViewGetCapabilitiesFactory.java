package org.auscope.portal.core.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathException;

import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.springframework.ui.ModelMap;

/**
 * A factory class containing methods for generating view representations of the GetCapabilitiesRecord
 *
 * @author 
 *
 */
public class ViewGetCapabilitiesFactory {

    /**
     * Converts a GetCapabilitiesRecord to a ModelMap view
     * @param k the GetCapabilitiesRecord to be converted
     * @param layerName if not null, it will only include this layer name in the WMS layer list
     *                  else all layers will be included
     * @return ModelMap view of the GetCapabilitiesRecord
     */
    public ModelMap toView(GetCapabilitiesRecord k, String layerName) {
        ModelMap obj = new ModelMap();

        obj.put("serviceType", k.getServiceType());
        obj.put("organisation", k.getOrganisation());
        obj.put("mapUrl", k.getMapUrl());
        obj.put("metadataUrl", k.getMetadataUrl());
        obj.put("isWFS", k.isWFS());
        obj.put("isWMS", k.isWMS());
        obj.put("version", k.getVersion());
        
        List<Map<String, Object>> layers = new ArrayList<>();
        if (k.getLayers() != null) {
            for (GetCapabilitiesWMSLayerRecord rec : k.getLayers()) {
                try {
                    if (layerName == null || rec.getName().equals(layerName)) {
                        layers.add(this.toView(rec));
                    }
                } catch (XPathException xe) {
                    
                }
            }
        }
        obj.put("layers", layers);

        obj.put("layerSRS", k.getLayerSRS());
        obj.put("mapFormats", k.getGetMapFormats());
        obj.put("applicationProfile", k.getApplicationProfile());
        return obj;
    }


    private ModelMap toView(GetCapabilitiesWMSLayerRecord rec) {
        ModelMap obj = new ModelMap();
        try {
            obj.put("name", rec.getName());
            obj.put("title", rec.getTitle());
            obj.put("abstract", rec.getAbstract());
            obj.put("metadataUrl", rec.getMetadataURL());
            obj.put("legendUrl", rec.getLegendURL());
            obj.put("timeExtent", rec.getTimeExtent());
            CSWGeographicBoundingBox bbox = rec.getBoundingBox();
            obj.put("bbox", this.toView(bbox));
            obj.put("srs", rec.getChildLayerSRS());
        } catch (XPathException xpath) {
        }
        return obj;
    }


    private ModelMap toView(CSWGeographicBoundingBox bbox)  {
        ModelMap obj = new ModelMap();
        obj.put("type", "bbox");
        obj.put("eastBoundLongitude", bbox.getEastBoundLongitude());
        obj.put("westBoundLongitude", bbox.getWestBoundLongitude());
        obj.put("northBoundLatitude", bbox.getNorthBoundLatitude());
        obj.put("southBoundLatitude", bbox.getSouthBoundLatitude());
        return obj;
    }
}
