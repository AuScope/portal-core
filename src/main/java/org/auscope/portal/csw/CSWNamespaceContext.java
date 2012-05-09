package org.auscope.portal.csw;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * A simple implementation of <a
 * href="http://java.sun.com/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html">
 * NamespaceContext </a>. Instances are immutable.
 *
 * @version $Id$
 */
public class CSWNamespaceContext implements NamespaceContext {

    private Map<String, String> map;

    public CSWNamespaceContext() {
        map = new HashMap<String, String>();
        map.put("gmd", "http://www.isotc211.org/2005/gmd");
        map.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
        map.put("gco", "http://www.isotc211.org/2005/gco");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("srv", "http://www.isotc211.org/2005/srv");
        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("gts", "http://www.isotc211.org/2005/gts");
        map.put("geonet", "http://www.fao.org/geonetwork");
    };

    /**
     * Returns an iterator to every prefix in this namespace
     * @return
     */
    public Iterator<String> getPrefixIterator() {
        return map.keySet().iterator();
    }

    /**
     * This method returns the uri for all prefixes needed.
     * @param prefix
     * @return uri
     */
    public String getNamespaceURI(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("No prefix provided!");

        if (map.containsKey(prefix))
            return map.get(prefix);
        else
            return XMLConstants.NULL_NS_URI;

    }

    public String getPrefix(String namespaceURI) {
        // Not needed in this context.
        return null;
    }

    public Iterator<String> getPrefixes(String namespaceURI) {
        // Not needed in this context.
        return null;
    }
}
