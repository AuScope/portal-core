package org.auscope.portal.core.server.http.download;

import org.auscope.portal.core.services.namespaces.IterableNamespace;

public class NumberOfFeatureNamespace extends IterableNamespace {

    public NumberOfFeatureNamespace() {
        map.put("gmd", "http://www.isotc211.org/2005/gmd");
        map.put("srv", "http://www.isotc211.org/2005/srv");
        map.put("er", "urn:cgi:xmlns:GGIC:EarthResource:1.1");
        map.put("gco", "http://www.isotc211.org/2005/gco");
        map.put("xs", "http://www.w3.org/2001/XMLSchema");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("ows", "http://www.opengis.net/ows");
    }
}
