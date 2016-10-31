package org.auscope.portal.core.services.csw;

import java.net.MalformedURLException;
import java.net.URL;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;

/**
 *
 * Concrete implementing class of CSWRecordsFilterVisitor. This class filters AbstractCSWOnlineResource by its host. Eg www.example.com/123 and
 * www.example.com/3344 will match and return.
 * 
 * @author Victor.Tey@csiro.au
 *
 */
public class CSWRecordsHostFilter implements CSWRecordsFilterVisitor {

    //the URL of the host name we want to filter on.
    URL filterUrl;

    public CSWRecordsHostFilter(URL url) {
        this.filterUrl = url;
    }

    public CSWRecordsHostFilter(String url) {
        try {
            this.filterUrl = ((url == null || url.equals("")) ? null : new URL(url));
        } catch (MalformedURLException mue) {
            this.filterUrl = null;
        }
    }

    @Override
    public boolean visit(AbstractCSWOnlineResource resource) {
        if (filterUrl == null) {
            return true;
        } else {
            return resource.getLinkage().getHost().equalsIgnoreCase(filterUrl.getHost());
        }
    }

}
