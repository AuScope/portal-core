package org.auscope.portal.core.services.namespaces;

/**
 * A namespace context for vocabularies
 * 
 * @author Josh Vote
 *
 */
public class VocabNamespaceContext extends IterableNamespace {

    public static final String SKOS_NAMESPACE = "http://www.w3.org/2004/02/skos/core#";
    public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";
    public static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    public static final String DCTERMS_NAMESPACE = "http://purl.org/dc/terms/";
    public static final String GTS_NAMESPACE = "http://resource.geosciml.org/ontology/timescale/gts#";

    public VocabNamespaceContext() {
        map.put("dc", DC_NAMESPACE);
        map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        map.put("owl", OWL_NAMESPACE);
        map.put("rdf", RDF_NAMESPACE);
        map.put("skos", SKOS_NAMESPACE);
        map.put("sparql", "http://www.w3.org/2005/sparql-results#");
        map.put("api", "http://purl.org/linked-data/api/vocab#");
        map.put("xhv", "http://www.w3.org/1999/xhtml/vocab#");
        map.put("os", "http://a9.com/-/spec/opensearch/1.1/");
        map.put("dcterms", DCTERMS_NAMESPACE);
        map.put("gts", GTS_NAMESPACE);
    }
}
