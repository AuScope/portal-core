package org.auscope.portal.core.configuration;

import java.io.UnsupportedEncodingException;

/**
 * This class represents a ServiceConfigurationItem which highlights detailed specification of a service. Current only used for capturing services that provides
 * paging service as this detail cannot be found anywhere. This service can be expanded in the future.
 *
 * @author tey006
 *
 */

public class ServiceConfigurationItem {

    String id, url;
    boolean paging;

    public ServiceConfigurationItem(String id, String url, boolean paging) {
        this.id = id;
        this.url = url;
        this.paging = paging;
    }

    /**
     * Check url starts with input parameter; This is a bit tricky to match because portal sometimes works as a proxy. We can fine tune this to better suit our
     * cases as it comes along.
     * 
     * @param url
     *            - the url to match
     * @return true input contains url
     */
    public boolean matchUrl(String url) {
        try {
            return java.net.URLDecoder.decode(url, "UTF-8").contains(this.url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @return true if this service supports paging else false;
     */
    public boolean doesPaging() {
        return paging;
    }

}
