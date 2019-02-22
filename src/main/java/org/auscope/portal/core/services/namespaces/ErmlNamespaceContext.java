package org.auscope.portal.core.services.namespaces;

/**
 * Extensions of the WFS namespace for the purposes of adding Earth resource markup language specialisations.
 * 
 * @author Josh Vote
 *
 */
public class ErmlNamespaceContext extends WFSNamespaceContext {
    public ErmlNamespaceContext() {
        super();
        map.put("er", "urn:cgi:xmlns:GGIC:EarthResource:1.1");
        map.put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
        map.put("erl", "http://xmlns.earthresourceml.org/earthresourceml-lite/1.0");
    }
    
    public ErmlNamespaceContext (String erVersion) {
        if ("2.0".equals(erVersion)) {
            map.put("er", "http://xmlns.earthresourceml.org/EarthResource/2.0");
            map.put("gsml", "http://xmlns.geosciml.org/GeoSciML-Core/3.2");
            map.put("gml", "http://www.opengis.net/gml/3.2");
        } else {
            new ErmlNamespaceContext();
        }
    }
}
