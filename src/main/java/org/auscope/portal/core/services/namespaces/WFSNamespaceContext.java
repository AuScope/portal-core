package org.auscope.portal.core.services.namespaces;

/**
 * A namespace context implementation for WFS 1.1.0
 *
 * @author Josh Vote
 *
 */
public class WFSNamespaceContext extends IterableNamespace {
    /**
     * Instantiates a new instance preloaded with all namespace contexts
     */
    public WFSNamespaceContext() {
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("ogc", "http://www.opengis.net/ogc");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("ows", "http://www.opengis.net/ows");
    }
}