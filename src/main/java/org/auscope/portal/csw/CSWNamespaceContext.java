package org.auscope.portal.csw;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * User: Mathew Wyatt
 * Date: 11/02/2009
 * Time: 3:34:51 PM
 */
public class CSWNamespaceContext implements NamespaceContext {
    private Map<String, String> map = new HashMap<String, String>() {{
                put("gmd", "http://www.isotc211.org/2005/gmd");
                put("srv", "http://www.isotc211.org/2005/srv");
                put("csw", "http://www.opengis.net/cat/csw/2.0.2");
                put("gco", "http://www.isotc211.org/2005/gco");
            }};

    public String getNamespaceURI(String s) {
        return map.get(s);  
    }

    public String getPrefix(String s) {
        return null;
    }

    public Iterator getPrefixes(String s) {
        return null;
    }
}
