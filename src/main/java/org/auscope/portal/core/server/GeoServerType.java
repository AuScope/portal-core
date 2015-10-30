/**
 * 
 */
package org.auscope.portal.core.server;

/**
 * There is a need to know which server a WFS, WMS etc.. is coming from as we've found bugs in implementations of the geoservers and have to adjust for these.
 * 
 * @author Brooke Smith
 *
 */
public enum GeoServerType {
    GeoServer, ArcGis;
    
    public static GeoServerType parseUrl(String serviceUrl) {
        if (serviceUrl.toUpperCase().contains("WFSSERVER")) {
            return GeoServerType.ArcGis;
        }
        return GeoServerType.GeoServer;
    }
}
