package org.auscope.portal.core.services.methodmakers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * Contains utilities common to all MethodMaker classes
 * 
 * @author Josh Vote
 *
 */
public abstract class AbstractMethodMaker {
    public final String defaultFeature_count = "10";

    /**
     * Concatenates one or more path elements onto the end of url
     *
     * For example urlPathConcat("http://test.com", "path") will return "http://test.com/path" urlPathConcat("http://test.com/", "/test", "path") will return
     * "http://test.com/test/path"
     *
     * @param url
     *            The base URL (which must be ending in a path)
     * @param newPathElements
     *            one or more path elemetns to concat The path to concat
     * @return
     */
    protected String urlPathConcat(String url, String... newPathElements) {
        StringBuilder sb = new StringBuilder(url);

        for (String pathEl : newPathElements) {
            if (pathEl == null || pathEl.isEmpty()) {
                continue;
            }

            if (sb.charAt(sb.length() - 1) != '/') {
                if (pathEl.charAt(0) != '/') {
                    sb.append('/');
                }
            } else {
                if (pathEl.charAt(0) == '/') {
                    pathEl = pathEl.substring(1);
                }
            }

            sb.append(pathEl);
        }

        return sb.toString();
    }

    /**
     * Returns a list of NameValuePair objects representing the URL query parameters of url (if any)
     * 
     * @param url
     * @return
     */
    protected List<NameValuePair> extractQueryParams(String url) {
        List<NameValuePair> params = new ArrayList<>();

        String[] parts = url.split("\\?");
        if (parts.length != 2) {
            return params;
        }

        String[] queryParams = parts[1].split("&");
        for (String queryParam : queryParams) {
            String[] kvp = queryParam.split("=");
            if (kvp.length == 2) {
                params.add(new BasicNameValuePair(kvp[0], kvp[1]));
            }
        }

        return params;
    }
}
