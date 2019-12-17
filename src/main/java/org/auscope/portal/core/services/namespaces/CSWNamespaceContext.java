package org.auscope.portal.core.services.namespaces;

/**
 * A simple implementation of <a href="http://java.sun.com/javase/6/docs/api/javax/xml/namespace/NamespaceContext.html"> NamespaceContext </a>. Instances are
 * immutable.
 *
 * @version $Id$
 */
public class CSWNamespaceContext extends IterableNamespace {

    public CSWNamespaceContext() {
        map.put("gmd", "http://www.isotc211.org/2005/gmd");
        map.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
        map.put("gco", "http://www.isotc211.org/2005/gco");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("srv", "http://www.isotc211.org/2005/srv");
        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("gmx", "http://www.isotc211.org/2005/gmx");
        map.put("gts", "http://www.isotc211.org/2005/gts");
        map.put("geonet", "http://www.fao.org/geonetwork");
        map.put("gmi", "http://www.isotc211.org/2005/gmi");
    }
}
