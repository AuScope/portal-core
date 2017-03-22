package org.auscope.portal.core.view;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.auscope.portal.core.view.knownlayer.SelectorsMode;
import org.auscope.portal.core.view.knownlayer.WMSSelectors;
import org.springframework.ui.ModelMap;

/**
 * A factory class containing methods for generating view representations of the KnownLayer
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

        obj.put("feature_count", k.getFeature_count());
        obj.put("order", k.getOrder());
        obj.put("singleTile", k.getSingleTile());
        obj.put("staticLegendUrl", k.getStaticLegendUrl());

        String group = "Others";
        if (k.getGroup() != null && !k.getGroup().isEmpty()) {
            group = k.getGroup();
        }
        obj.put("group", group);

        // LayersMode is from GA GPT-41 where Layers can have Layers and they can be 'OR'd or 'AND'd.
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

        if(k.getFilterCollection()!=null){
            obj.put("filterCollection",k.getFilterCollection());
        }

        if (k.getNagiosHostGroup() != null) {
            obj.put("nagiosHostGroup", k.getNagiosHostGroup());
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
