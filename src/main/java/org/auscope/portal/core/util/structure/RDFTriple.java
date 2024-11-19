package org.auscope.portal.core.util.structure;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

public class RDFTriple {
    public Resource subject;
    public Property predicate;
    public Object object;
    public String language;

    public RDFTriple(Resource subject, Property predicate, RDFNode object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public RDFTriple(Resource subject, Property predicate, String object, String language) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.language = language;
    }
}
