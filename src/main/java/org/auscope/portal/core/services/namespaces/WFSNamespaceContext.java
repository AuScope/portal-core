package org.auscope.portal.core.services.namespaces;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

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
    }
}