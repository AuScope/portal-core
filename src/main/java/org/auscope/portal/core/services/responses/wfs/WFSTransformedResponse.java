package org.auscope.portal.core.services.responses.wfs;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Represents a WFS response transformed by a XSLT
 * 
 * @author Josh Vote
 *
 */
public class WFSTransformedResponse {
    /** The original WFS response as returned by the service */
    private String gml;
    /** The gml string after being transformed by a XSLT */
    private String transformed;
    /** The method used to make the request */
    private HttpRequestBase method;
    /** success flag */
    private Boolean success;
    
    /**
     * Creates a new instance of this class
     * 
     * @param gml
     *            The original WFS response as returned by the service
     * @param transformed
     *            The gml string after being transformed by a XSLT
     * @param method
     *            the method used to make the request
     */
    public WFSTransformedResponse(String gml, String transformed, HttpRequestBase method) {
        this(gml, transformed, method, true);
    }
    
    /**
     * Creates a new instance of this class
     * 
     * @param gml
     *            The original WFS response as returned by the service
     * @param transformed
     *            The gml string after being transformed by a XSLT
     * @param method
     *            the method used to make the request
     */
    public WFSTransformedResponse(String gml, String transformed, HttpRequestBase method, Boolean success) {
        this.gml = gml;
        this.transformed = transformed;
        this.method = method;
        this.success = success;
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
     * Gets the gml string after being transformed by a XSLT
     * 
     * @return
     */
    public String getTransformed() {
        return transformed;
    }

    /**
     * Gets the method used to make the request
     * 
     * @return
     */
    public HttpRequestBase getMethod() {
        return method;
    }
    
    /**
     * Gets the success value
     * @return
     */
    public Boolean getSuccess() {
        return this.success;
    }
}
