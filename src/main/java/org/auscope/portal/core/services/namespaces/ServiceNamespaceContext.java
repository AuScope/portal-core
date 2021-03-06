package org.auscope.portal.core.services.namespaces;

import java.util.Map;

/**
 * A namespace context that collects all the namespaces defined in a GetCapabilities document
 */
public class ServiceNamespaceContext extends IterableNamespace {

    /**
     * Adds a prefix and namespaceUri to the namespace context
     *
     * @param prefix Prefix for the namespace
     * @param namespaceUri URI for the namespace
     */
    public void setNamespace(String prefix, String namespaceUri) {
        this.map.put(prefix, namespaceUri);
    }

    /**
     * Returns the namespace map to be serialised for output
     *
     * @return The namespace map
     */
    public Map<String, String> getMap() { return map; }
}
