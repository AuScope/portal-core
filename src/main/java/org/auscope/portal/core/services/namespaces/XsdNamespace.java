package org.auscope.portal.core.services.namespaces;

public class XsdNamespace extends IterableNamespace {
    public XsdNamespace() {
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("ogc", "http://www.opengis.net/ogc");
        map.put("xsd", "http://www.w3.org/2001/XMLSchema");
        map.put("ows", "http://www.opengis.net/ows");
    }
}
