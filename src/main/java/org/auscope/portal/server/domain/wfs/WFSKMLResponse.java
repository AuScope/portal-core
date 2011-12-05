package org.auscope.portal.server.domain.wfs;

import org.apache.commons.httpclient.HttpMethodBase;

/**
 * A transformed representation of a Web Feature Service Response.
 *
 * It contains the original GML, the transformed KML and some other related information
 * @author Josh Vote
 *
 */
public class WFSKMLResponse {
    /** The original WFS response as returned by the service */
    private String gml;
    /** The gml string after being transformed by a GML->KML XSLT */
    private String kml;
    /** The method used to make the request*/
    private HttpMethodBase method;


    /**
     * Creates a new instance of this class
     * @param gml The original WFS response as returned by the service
     * @param kml The gml string after being transformed by a GML->KML XSLT
     * @param method the method used to make the request
     */
    public WFSKMLResponse(String gml, String kml, HttpMethodBase method) {
        this.gml = gml;
        this.kml = kml;
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
     * Gets the gml string after being transformed by a GML->KML XSLT
     * @return
     */
    public String getKml() {
        return kml;
    }

    /**
     * Gets the method used to make the request
     * @return
     */
    public HttpMethodBase getMethod() {
        return method;
    }



}
