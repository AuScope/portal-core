package org.auscope.portal.core.services.responses.sos;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * Simplified view of a response from a SOS
 * @author Florence Tan
 */
public class SOSResponse {
    /** The response sos*/
    private String sos;

    /** The method used to make the request*/
    private HttpMethodBase method;

    /**
     * Creates a new instance of this class
     * @param sos The original SOS response as returned by the service
     * @param method the method used to make the request
     */
    public SOSResponse(String sos, HttpMethodBase method) {
        this.sos = sos;
        this.method = method;
    }

    /**
     * Gets the original SOS response as returned by the service
     * @return
     */
    public String getSos() {
        return sos;
    }

    /**
     * Gets the method used to make the request
     * @return
     */
    public HttpMethodBase getMethod() {
        return method;
    }
}
