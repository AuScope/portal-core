package org.auscope.portal.vocabs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * User: Michael Stegherr
 * Date: 07/09/2009
 * @version $Id$
 */
public class VocabularyServiceNamespaceContext implements NamespaceContext {
    
    private Map<String, String> map = new HashMap<String, String>();
    
    public VocabularyServiceNamespaceContext() {
        map.put("dc", "http://purl.org/dc/elements/1.1/");
        map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        map.put("xlink", "http://www.w3.org/1999/xlink");
        map.put("owl", "http://www.w3.org/2002/07/owl#");
        map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        map.put("skos", "http://www.w3.org/2004/02/skos/core#");        
    }

    public String getNamespaceURI(String s) {
        return map.get(s);
    }

    public String getPrefix(String s) {
        return null;
    }

    public Iterator<String> getPrefixes(String s) {
        return null;
    }
}