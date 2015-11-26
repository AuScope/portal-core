package org.auscope.portal.core.services.namespaces;

/**
 * Represents the namespace context of a generic WCS response to a DescribeCoverage request.
 * 
 * @author Josh Vote
 *
 */
public class WCSNamespaceContext extends IterableNamespace {
    public WCSNamespaceContext() {
        map.put("wcs", "http://www.opengis.net/wcs");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("ows", "http://www.opengis.net/ows");
    }
}
