package org.auscope.portal.core.services.namespaces;

/**
 * Extensions of the WFS namespace for the purposes of adding Earth
 * resource markup language specialisations.
 * @author Josh Vote
 *
 */
public class ErmlNamespaceContext extends WFSNamespaceContext {
    public ErmlNamespaceContext() {
        super();
        map.put("er", "urn:cgi:xmlns:GGIC:EarthResource:1.1");
        map.put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
    }
}
