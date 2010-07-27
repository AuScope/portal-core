package org.auscope.portal.server.domain.ogc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * Used for testing OGC filters
 * @author vot002
 *
 */
public class OGCNamespaceContext implements NamespaceContext {
    private Map<String, String> map = new HashMap<String, String>();
    
    public OGCNamespaceContext() {    
        map.put("ogc", "http://www.opengis.net/wcs");
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