package org.auscope.portal.core.services.namespaces;

/**
 * Used for testing OGC filters
 * 
 * @author vot002
 *
 */
public class OGCNamespaceContext extends IterableNamespace {
    public OGCNamespaceContext() {
        map.put("ogc", "http://www.opengis.net/wcs");
    }
}
