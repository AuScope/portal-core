package org.auscope.portal.core.view;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.core.view.knownlayer.KnownLayer;
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

        String group = "Others";
        if (k.getGroup() != null && !k.getGroup().isEmpty()) {
            group = k.getGroup();
        }
        obj.put("group", group);

        return obj;
    }

    private ModelMap toView(Dimension d) {
        ModelMap obj = new ModelMap();

        obj.put("width", d.getWidth());
        obj.put("height", d.getHeight());

        return obj;
    }

    private ModelMap toView(Point p) {
        ModelMap obj = new ModelMap();

        obj.put("x", p.getX());
        obj.put("y", p.getY());

        return obj;
    }
}
