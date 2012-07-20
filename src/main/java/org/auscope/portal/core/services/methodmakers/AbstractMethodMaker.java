package org.auscope.portal.core.services.methodmakers;

import org.apache.http.params.BasicHttpParams;

/**
 * Contains utilities common to all MethodMaker classes
 * @author Josh Vote
 *
 */
public abstract class AbstractMethodMaker {
    /**
     * Concatenates one or more path elements onto the end of url
     *
     * For example
     * urlPathConcat("http://test.com", "path") will return "http://test.com/path"
     * urlPathConcat("http://test.com/", "/test", "path") will return "http://test.com/test/path"
     *
     * @param url The base URL (which must be ending in a path)
     * @param newPathElements one or more path elemetns to concat The path to concat
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
     * Returns a BasicHttpParams initialised to every URL parameter found in url (if any)
     * @param url The url to check for params
     * @return
     */
    protected BasicHttpParams extractQueryParams(String url) {
        BasicHttpParams params = new BasicHttpParams();

        String[] parts = url.split("\\?");
        if (parts.length != 2) {
            return params;
        }

        String[] queryParams = parts[1].split("&");
        for (String queryParam : queryParams) {
            String[] kvp = queryParam.split("=");
            if (kvp.length == 2) {
                params.setParameter(kvp[0], kvp[1]);
            }
        }

        return params;
    }
}
