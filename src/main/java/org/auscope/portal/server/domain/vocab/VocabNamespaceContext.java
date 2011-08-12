package org.auscope.portal.server.domain.vocab;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * A namespace context for vocabularies
 * @author Josh Vote
 *
 */
public class VocabNamespaceContext implements NamespaceContext {

    private Map<String, String> map;

    public VocabNamespaceContext() {
        map = new HashMap<String, String>();

        map.put("dc", "http://purl.org/dc/elements/1.1/");
        map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        map.put("owl", "http://www.w3.org/2002/07/owl#");
        map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        map.put("skos", "http://www.w3.org/2004/02/skos/core#");

    };

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null)
            throw new IllegalArgumentException("No prefix provided!");

        if (map.containsKey(prefix))
            return map.get(prefix);
        else
            return XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        // Not needed
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        // Not needed
        return null;
    }

}
