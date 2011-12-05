package org.auscope.portal.server.domain.wfs;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * A transformed representation of a Web Feature Service Response.
 *
 * It contains the original GML, the transformed HTML and some other related information
 * @author Josh Vote
 *
 */
public class WFSHTMLResponse {
    /** The original WFS response as returned by the service */
    private String gml;
    /** The gml string after being transformed by a GML->HTML XSLT */
    private String html;
    /** The method used to make the request*/
    private HttpMethodBase method;


    /**
     * Creates a new instance of this class
     * @param gml The original WFS response as returned by the service
     * @param html The html string after being transformed by a GML->HTML XSLT
     * @param method the method used to make the request
     */
    public WFSHTMLResponse(String gml, String html, HttpMethodBase method) {
        this.gml = gml;
        this.html = html;
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
     * Gets the gml string after being transformed by a GML->HTML XSLT
     * @return
     */
    public String getHtml() {
        return html;
    }

    /**
     * Gets the method used to make the request
     * @return
     */
    public HttpMethodBase getMethod() {
        return method;
    }
}
