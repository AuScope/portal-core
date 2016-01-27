package org.auscope.portal.core.services.responses.wfs;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Simplified view of a response from a WFS
 *
 * @author Josh Vote
 */
public class WFSResponse {
    /** The response data */
    private String data;

    /** The method used to make the request */
    private HttpRequestBase method;

    /**
     * Creates a new instance of this class
     *
     * @param data
     *            The original WFS response as returned by the service
     * @param method
     *            the method used to make the request
     */
    public WFSResponse(String data, HttpRequestBase method) {
        this.data = data;
        this.method = method;
    }

    /**
     * Gets the original WFS response as returned by the service
     *
     * @return
     */
    public String getData() {
        return data;
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
