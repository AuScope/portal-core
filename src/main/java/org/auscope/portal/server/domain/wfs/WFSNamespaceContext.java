package org.auscope.portal.server.domain.wfs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * A namespace context implementation for WFS 1.1.0
 *
 * @author Josh Vote
 *
 */
public class WFSNamespaceContext implements NamespaceContext {
    /** The map. */
    private Map<String, String> map = new HashMap<String, String>();

    /**
     * Instantiates a new instance preloaded with all namespace contexts
     */
    public WFSNamespaceContext() {
        map.put("wfs", "http://www.opengis.net/wfs");
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("No prefix provided!");
        }

        if (map.containsKey(prefix)) {
            return map.get(prefix);
        } else {
            return XMLConstants.NULL_NS_URI;
        }
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String namespaceURI) {
        return null;
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator<String> getPrefixes(String namespaceURI) {
        return null;
    }

}