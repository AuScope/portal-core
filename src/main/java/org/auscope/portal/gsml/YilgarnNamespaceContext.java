package org.auscope.portal.gsml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class YilgarnNamespaceContext implements NamespaceContext{
private Map<String, String> map = new HashMap<String, String>();

    public YilgarnNamespaceContext() {
        map.put("ogc", "http://www.opengis.net/ogc");
        map.put("omx", "http://www.opengis.net/omx/1.0");
        map.put("sa", "http://www.opengis.net/sampling/1.0");
        map.put("om", "http://www.opengis.net/om/1.0");
        map.put("swe", "http://www.opengis.net/swe/1.0.1");
        map.put("ansir", "http://mdu-data.arrc.csiro.au/schema/urn:cgi:xmlns:DI4SMB:ansir:01");
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("highp", "urn:cgi:xmlns:DI4SMB:HighP:0.1");
        map.put("topp", "http://www.openplans.org/topp");
        map.put("geonetwork", "urn:cgi:xmlns:DI4SMB:geonetwork");
        map.put("sml", "http://www.opengis.net/sensorML/1.0.1");
        map.put("ragingspot", "http://mdu-data-2.arrc.csiro.au/schema/urn:cgi:xmlns:auscope:ragingspot");
        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        map.put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
        map.put("ows", "http://www.opengis.net/ows");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("xlink", "http://www.w3.org/1999/xlink");
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("No prefix provided!");

        if (map.containsKey(prefix))
            return map.get(prefix);
        else
            return XMLConstants.NULL_NS_URI;
    }

    public String getPrefix(String namespaceURI) {
        return null;
    }

    public Iterator<String> getPrefixes(String namespaceURI) {
        return null;
    }
}
