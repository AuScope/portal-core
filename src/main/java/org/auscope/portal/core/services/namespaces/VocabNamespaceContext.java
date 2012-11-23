package org.auscope.portal.core.services.namespaces;


/**
 * A namespace context for vocabularies
 * @author Josh Vote
 *
 */
public class VocabNamespaceContext extends IterableNamespace {

    public VocabNamespaceContext() {
        map.put("dc", "http://purl.org/dc/elements/1.1/");
        map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        map.put("owl", "http://www.w3.org/2002/07/owl#");
        map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        map.put("skos", "http://www.w3.org/2004/02/skos/core#");
        map.put("sparql", "http://www.w3.org/2005/sparql-results#");
        map.put("api", "http://purl.org/linked-data/api/vocab#");
        map.put("xhv", "http://www.w3.org/1999/xhtml/vocab#");
        map.put("os", "http://a9.com/-/spec/opensearch/1.1/");
        map.put("dcterms", "http://purl.org/dc/terms/");
    }
}
