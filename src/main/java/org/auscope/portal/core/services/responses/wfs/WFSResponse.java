package org.auscope.portal.core.services.responses.wfs;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Simplified view of a response from a WFS
 * 
 * @author Josh Vote
 */
public class WFSResponse {
    /** The response gml */
    private String gml;

    /** The method used to make the request */
    private HttpRequestBase method;

    /**
     * Creates a new instance of this class
     * 
     * @param gml
     *            The original WFS response as returned by the service
     * @param method
     *            the method used to make the request
     */
    public WFSResponse(String gml, HttpRequestBase method) {
        this.gml = gml;
        this.method = method;
    }

    /**
     * Gets the original WFS response as returned by the service
     * 
     * @return
     */
    public String getGml() {
        return gml;
    }

    /**
     * Gets the method used to make the request
     * 
     * @return
     */
    public HttpRequestBase getMethod() {
        return method;
    }
}
