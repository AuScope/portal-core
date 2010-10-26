package org.auscope.portal.mineraloccurrence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * User: Mathew Wyatt
 * Date: 24/03/2009
 * @version $Id$
 */
public class MineralOccurrenceNamespaceContext implements NamespaceContext {
    
    private Map<String, String> map = new HashMap<String, String>();
    
    public MineralOccurrenceNamespaceContext() {    
        map.put("gmd", "http://www.isotc211.org/2005/gmd");
        map.put("srv", "http://www.isotc211.org/2005/srv");
        map.put("er", "urn:cgi:xmlns:GGIC:EarthResource:1.1");
        map.put("gco", "http://www.isotc211.org/2005/gco");
        map.put("xs", "http://www.w3.org/2001/XMLSchema");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        map.put("gml", "http://www.opengis.net/gml");    
        map.put("ows", "http://www.opengis.net/ows");
};

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