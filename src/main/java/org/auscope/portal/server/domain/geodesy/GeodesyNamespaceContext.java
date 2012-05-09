package org.auscope.portal.server.domain.geodesy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.auscope.portal.server.domain.wfs.WFSNamespaceContext;

public class GeodesyNamespaceContext implements NamespaceContext {
    private Map<String, String> map = new HashMap<String, String>();

    public GeodesyNamespaceContext() {
        map.put("ogc", "http://www.opengis.net/ogc");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("ngcp", "http://www.auscope.org/ngcp");
        map.put("geodesy", "http://www.auscope.org/geodesy");
    }

    public String getNamespaceURI(String s) {
        return map.get(s);
    }

    public String getPrefix(String s) {
        return null;
    }

    public Iterator<String> getPrefixes(String s) {
        return null;
    }
}
