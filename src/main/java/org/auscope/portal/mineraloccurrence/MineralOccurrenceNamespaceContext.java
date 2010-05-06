package org.auscope.portal.mineraloccurrence;

import javax.xml.namespace.NamespaceContext;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * User: Mathew Wyatt
 * Date: 24/03/2009
 * Time: 8:43:38 AM
 */
public class MineralOccurrenceNamespaceContext implements NamespaceContext {
    private Map<String, String> map = new HashMap<String, String>() {{
                put("gmd", "http://www.isotc211.org/2005/gmd");
                put("srv", "http://www.isotc211.org/2005/srv");
                put("er", "urn:cgi:xmlns:GGIC:EarthResource:1.1");
                put("gco", "http://www.isotc211.org/2005/gco");
                put("xs", "http://www.w3.org/2001/XMLSchema");
                put("xlink", "http://www.w3.org/1999/xlink");
                put("wfs", "http://www.opengis.net/wfs");
                put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
                put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                put("gml", "http://www.opengis.net/gml");
                
            }};

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