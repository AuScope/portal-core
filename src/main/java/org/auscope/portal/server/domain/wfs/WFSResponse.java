package org.auscope.portal.server.domain.wfs;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * Simplified view of a response from a WFS
 * @author Josh Vote
 */
public class WFSResponse {
    /** The response gml*/
    private String gml;

    /** The method used to make the request*/
    private HttpMethodBase method;

    /**
     * Creates a new instance of this class
     * @param gml The original WFS response as returned by the service
     * @param method the method used to make the request
     */
    public WFSResponse(String gml, HttpMethodBase method) {
        this.gml = gml;
        this.method = method;
    }

    /**
     * Gets the original WFS response as returned by the service
     * @return
     */
    public String getGml() {
        return gml;
    }

    /**
     * Gets the method used to make the request
     * @return
     */
    public HttpMethodBase getMethod() {
        return method;
    }
}
