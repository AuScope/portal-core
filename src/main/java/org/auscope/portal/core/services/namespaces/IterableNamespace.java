package org.auscope.portal.core.services.namespaces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang.NotImplementedException;

/**
 * An abstract implementation of NamespaceContext that simplifies the namespace into a HashMap.
 *
 * Also exposes additional utility methods
 *
 * @author Josh Vote
 *
 */
public abstract class IterableNamespace implements NamespaceContext {

    /**
     * Map of namespaces keyed by their prefix
     */
    protected Map<String, String> map = new HashMap<>();

    /**
     * Looks up the namespace URI for a given prefix. Will return XMLConstants.NULL_NS_URI on failure
     *
     * @param prefix
     *            The prefix whose namespace will be looked up
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("No prefix provided!");

        if (map.containsKey(prefix)) {
            return map.get(prefix);
        } else {
            return XMLConstants.NULL_NS_URI;
        }
    }

    /**
     * Not implemented
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    @Override
    public String getPrefix(String namespace) {
        throw new NotImplementedException();
    }

    /**
     * Not implemented
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    @Override
    public Iterator getPrefixes(String namespace) {
        throw new NotImplementedException();
    }

    /**
     * Returns an iterator to every prefix in this namespace
     * 
     * @return
     */
    public Iterator<String> getPrefixIterator() {
        return map.keySet().iterator();
    }
}
