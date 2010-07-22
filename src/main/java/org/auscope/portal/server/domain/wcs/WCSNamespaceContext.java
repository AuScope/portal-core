package org.auscope.portal.server.domain.wcs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * Represents the namespace context of a generic WCS response to a DescribeCoverage request.
 * @author vot002
 *
 */
public class WCSNamespaceContext implements NamespaceContext {
    private Map<String, String> map = new HashMap<String, String>();
    
    public WCSNamespaceContext() {    
        map.put("wcs", "http://www.opengis.net/wcs");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("ows", "http://www.opengis.net/ows");
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
