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
public enum OgcServiceProviderType {
    GeoServer, ArcGis, PyCSW, 
    Default; // for geonetwork or other CSW servers.
    
    public static OgcServiceProviderType parseUrl(String serviceUrl) {
        if (serviceUrl.toUpperCase().contains("WFSSERVER")) {
            return OgcServiceProviderType.ArcGis;
        }
        return OgcServiceProviderType.GeoServer;
    }
}
