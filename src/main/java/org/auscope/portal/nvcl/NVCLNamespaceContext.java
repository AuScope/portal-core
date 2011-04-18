package org.auscope.portal.nvcl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * Namespace context for NVCL feature types
 * @author Josh Vote
 *
 */
public class NVCLNamespaceContext implements NamespaceContext {

    public static final String PUBLISHED_DATASETS_TYPENAME = "nvcl:ScannedBoreholeCollection";
    
    private Map<String, String> map = new HashMap<String, String>();
    
    public NVCLNamespaceContext() {    
        map.put("ogc", "http://www.opengis.net/ogc");
        map.put("sa", "http://www.opengis.net/sampling/1.0");
        map.put("om", "http://www.opengis.net/om/1.0");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        map.put("gml", "http://www.opengis.net/gml");    
        map.put("ows", "http://www.opengis.net/ows");
        map.put("nvcl", "http://www.auscope.org/nvcl");
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
