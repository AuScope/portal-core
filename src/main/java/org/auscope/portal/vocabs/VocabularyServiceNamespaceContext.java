package org.auscope.portal.vocabs;

import javax.xml.namespace.NamespaceContext;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * User: Michael Stegherr
 * Date: 07/09/2009
 * Time: 1:56:38 AM
 */
public class VocabularyServiceNamespaceContext implements NamespaceContext {
    private Map<String, String> map = new HashMap<String, String>() {{
                put("dc", "http://purl.org/dc/elements/1.1/");
                put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
                put("xlink", "http://www.w3.org/1999/xlink");
                put("owl", "http://www.w3.org/2002/07/owl#");
                put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                put("skos", "http://www.w3.org/2004/02/skos/core#");
            }};

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