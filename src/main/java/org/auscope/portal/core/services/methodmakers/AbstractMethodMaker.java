package org.auscope.portal.core.services.methodmakers;

/**
 * Contains utilities common to all MethodMaker classes
 * @author Josh Vote
 *
 */
public abstract class AbstractMethodMaker {
    /**
     * Concatenates a path element onto the end of url
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
}
