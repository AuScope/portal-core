package org.auscope.portal.core.services.methodmakers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;

/**
 * Contains utilities common to all MethodMaker classes
 * @author Josh Vote
 *
 */
public abstract class AbstractMethodMaker {
    /**
     * Concatenates a path element onto the end of url
     *
     * For example
     * urlPathConcat("http://test.com", "path") will return "http://test.com/path"
     * urlPathConcat("http://test.com/", "path") will return "http://test.com/path"
     *
     * @param url The base URL (which must be ending in a path)
     * @param newPath The path to concat
     * @return
     */
    protected String urlPathConcat(String url, String newPath) {
        if (url.charAt(url.length() - 1) != '/') {
            url = url + "/";
        }

        return url + newPath;
    }

    /**
     * Returns a list of NameValuePair objects representing the
     * URL query parameters of url (if any)
     * @param url
     * @return
     */
    protected List<NameValuePair> extractQueryParams(String url) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        String[] parts = url.split("\\?");
        if (parts.length != 2) {
            return params;
        }

        String[] queryParams = parts[1].split("&");
        for (String queryParam : queryParams) {
            String[] kvp = queryParam.split("=");
            if (kvp.length == 2) {
                params.add(new NameValuePair(kvp[0], kvp[1]));
            }
        }

        return params;
    }
}
