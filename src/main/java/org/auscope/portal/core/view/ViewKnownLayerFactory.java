package org.auscope.portal.core.view;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.auscope.portal.core.view.knownlayer.SelectorsMode;
import org.auscope.portal.core.view.knownlayer.WMSSelectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ui.ModelMap;

/**
 * A factory class containing methods for generating view representations of the
 * KnownLayer
 *
 * @author Josh Vote
 *
 */
public class ViewKnownLayerFactory {

    public ModelMap toView(KnownLayer k) {
        ModelMap obj = new ModelMap();

        obj.put("name", k.getName());
        obj.put("hidden", k.isHidden());
        obj.put("description", k.getDescription());
        obj.put("id", k.getId());
        obj.put("proxyUrl", k.getProxyUrl());
        obj.put("proxyGetFeatureInfoUrl", k.getProxyGetFeatureInfoUrl());
        obj.put("proxyCountUrl", k.getProxyCountUrl());
        obj.put("proxyStyleUrl", k.getProxyStyleUrl());
        obj.put("proxyDownloadUrl", k.getProxyDownloadUrl());

        if (k.getIconUrl() != null) {
            obj.put("iconUrl", k.getIconUrl());
        }

        if (k.getPolygonColor() != null && k.getPolygonColor().length() > 0) {
            obj.put("polygonColor", k.getPolygonColor());
        }

        if (k.getIconAnchor() != null) {
            obj.put("iconAnchor", toView(k.getIconAnchor()));
        }

        if (k.getIconSize() != null) {
            obj.put("iconSize", toView(k.getIconSize()));
        }

        obj.put("mapStyles", k.getMapStyles());
        obj.put("feature_count", k.getFeature_count());
        obj.put("order", k.getOrder());
        obj.put("singleTile", k.getSingleTile());
        obj.put("legendImg", k.getLegendImg());

        String group = "Others";
        if (k.getGroup() != null && !k.getGroup().isEmpty()) {
            group = k.getGroup();
        }

        obj.put("group", group);

        // add a geojson object to support the VMF layer - Indigenous
        if (k.getEndPoint() != null) {
            ModelMap geoObj = new ModelMap();
            geoObj.put("endPoint", k.getEndPoint());
            if (k.getPolygon() != null) {
                JSONArray polygon = k.getPolygon();
                List<Object> coords = new ArrayList<>();
                for (int i = 0; i < polygon.length(); i++) {
                    JSONArray coordNode = (JSONArray) polygon.get(i);

                    List<Double> coord = new ArrayList<>();
                    coord.add((Double) coordNode.get(0));
                    coord.add((Double) coordNode.get(1));
                    coords.add(coord);
                }
                geoObj.put("polygon", coords);
            }
            obj.put("geojson", geoObj);
        }

        // add a geojson object to support the bbox;
        if (k.getBBox() != null) {
            ModelMap geoObj = new ModelMap();
            JSONArray bbox = k.getBBox();
            List<Object> coords = new ArrayList<>();
            for (int i = 0; i < bbox.length(); i++) {
                JSONArray coordNode = (JSONArray) bbox.get(i);

                List<Double> coord = new ArrayList<>();
                coord.add((Double) coordNode.get(0));
                coord.add((Double) coordNode.get(1));
                coords.add(coord);
            }
            geoObj.put("bbox", coords);
            obj.put("geojson", geoObj);
        }
        // LayersMode is from GA GPT-41 where Layers can have Layers and they can be
        // 'OR'd or 'AND'd.
        if (k.getKnownLayerSelector() != null) {
            KnownLayerSelector knownLayerSelector = k.getKnownLayerSelector();
            if (knownLayerSelector instanceof WMSSelectors) {
                WMSSelectors wmsSelectors = (WMSSelectors) knownLayerSelector;
                obj.put("layerMode", wmsSelectors.getLayersMode());
            } else {
                obj.put("layerMode", SelectorsMode.NA);
            }
        } else {
            obj.put("layerMode", SelectorsMode.NA);
        }

        if (k.getFilterCollection() != null) {
            obj.put("filterCollection", k.getFilterCollection());
        }

        if (k.getStackdriverServiceGroup() != null) {
            obj.put("stackdriverServiceGroup", k.getStackdriverServiceGroup());
        }
        if (k.getSupportsCsvDownloads()) {
            obj.put("supportsCsvDownloads", k.getSupportsCsvDownloads());
        }

        if (k.getServerType() != null) {
            obj.put("serverType", k.getServerType());
        }

        return obj;
    }

    private static ModelMap toView(Dimension d) {
        ModelMap obj = new ModelMap();

        obj.put("width", d.getWidth());
        obj.put("height", d.getHeight());

        return obj;
    }

    private static ModelMap toView(Point p) {
        ModelMap obj = new ModelMap();

        obj.put("x", p.getX());
        obj.put("y", p.getY());

        return obj;
    }
}
