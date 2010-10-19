package org.auscope.portal.server.web.view;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.auscope.portal.server.web.KnownLayer;
import org.auscope.portal.server.web.KnownLayerKeywords;
import org.auscope.portal.server.web.KnownLayerWFS;
import org.auscope.portal.server.web.KnownLayerWMS;
import org.springframework.stereotype.Repository;
import org.springframework.ui.ModelMap;

/**
 * A factory class containing methods for generating view representations of the KnownFeatureTypeDefinition 
 * @author vot002
 *
 */
@Repository
public class ViewKnownLayerFactory {
	
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
	
	public ModelMap toView(KnownLayerKeywords k) {
	    ModelMap obj = new ModelMap();
        
        obj.put("type", "KnownLayerKeywords");
        obj.put("title", k.getTitle());
        obj.put("description",k.getDescription());
        obj.put("id", k.getId());
        obj.put("descriptiveKeyword", k.getDescriptiveKeyword());
        obj.put("iconUrl", k.getIconUrl());
        
        Point iconAnchor =  k.getIconAnchor(); 
        if (iconAnchor != null) {
            obj.put("iconAnchor", toView(iconAnchor));
        }
        
        Dimension iconSize = k.getIconSize();
        if (iconSize != null) {
            obj.put("iconSize", toView(iconSize));
        }
        
        return obj;
	}
	
	public ModelMap toView(KnownLayerWFS k) {
	    ModelMap obj = new ModelMap();
        
	    obj.put("type", "KnownLayerWFS");
        obj.put("title", k.getTitle());
        obj.put("description",k.getDescription());
        obj.put("id", k.getId());
        obj.put("featureTypeName", k.getFeatureTypeName());
        obj.put("proxyUrl", k.getProxyUrl());
        obj.put("iconUrl", k.getIconUrl());
        obj.put("disableBboxFiltering", k.getDisableBboxFiltering());
        
        
        Point iconAnchor =  k.getIconAnchor(); 
        if (iconAnchor != null) {
            obj.put("iconAnchor", toView(iconAnchor));
        }
        
        Point infoWindowAnchor = k.getInfoWindowAnchor();
        if (infoWindowAnchor != null) {
            obj.put("infoWindowAnchor", toView(infoWindowAnchor));
        }
        
        Dimension iconSize = k.getIconSize();
        if (iconSize != null) {
            obj.put("iconSize", toView(iconSize));
        }
        
        return obj;
    }
	
	public ModelMap toView(KnownLayerWMS k) {
	    ModelMap obj = new ModelMap();
        
        obj.put("type", "KnownLayerWMS");
        obj.put("title", k.getTitle());
        obj.put("description",k.getDescription());
        obj.put("id", k.getId());
        obj.put("layerName", k.getLayerName());
        obj.put("styleName", k.getStyleName());
        
        return obj;
    }
	
	
	
	/**
	 * Converts a KnownFeatureTypeDefinition into its view equivalent
	 * @param k
	 * @return
	 */
	public ModelMap toView(KnownLayer k) {
		if (k instanceof KnownLayerWFS) {
		    return toView((KnownLayerWFS) k);
		} else if (k instanceof KnownLayerWMS) {
		    return toView((KnownLayerWMS) k);
		} else if (k instanceof KnownLayerKeywords) {
            return toView((KnownLayerKeywords) k);
        } else {
            ModelMap obj = new ModelMap();
            
            obj.put("type", "KnownLayer");
            obj.put("title", k.getTitle());
            obj.put("description",k.getDescription());
            obj.put("id", k.getId());
            
            return obj;
        }
	}
}
