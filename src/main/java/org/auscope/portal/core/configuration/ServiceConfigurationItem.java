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
    boolean isGml32;
    
    public ServiceConfigurationItem(String id, String url, boolean paging) {
        this.id = id;
        this.url = url;
        this.paging = paging;
        this.isGml32 = false;
    }
    
    public ServiceConfigurationItem(String id, String url, boolean paging, boolean isGml32) {
        this.id = id;
        this.url = url;
        this.paging = paging;
        this.isGml32 = isGml32;
    }

    /**
     * Check url starts with input parameter; This is a bit tricky to match because portal sometimes works as a proxy. We can fine tune this to better suit our
     * cases as it comes along.
     * 
     * @param myUrl
     *            - the url to match
     * @return true input contains url
     */
    public boolean matchUrl(String myUrl) {
        try {
            return java.net.URLDecoder.decode(myUrl, "UTF-8").contains(this.url);
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
    
    /**
     * 
     * @return true if this service is GML 3.2 based otherwise assumes GML 3.1.1
     */
    public boolean isGml32() {
        return isGml32;
    }

}
