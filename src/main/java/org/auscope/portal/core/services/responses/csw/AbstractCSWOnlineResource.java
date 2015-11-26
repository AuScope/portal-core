package org.auscope.portal.core.services.responses.csw;

import java.net.URL;

import org.auscope.portal.core.services.csw.CSWRecordsFilterVisitor;

/**
 * Represents a <gmd:CI_OnlineResource> element in a CSW response.
 * 
 * @author vot002
 *
 */
public abstract class AbstractCSWOnlineResource {

    /**
     * A simplification of the protocol.
     * 
     * @author vot002
     *
     */
    public enum OnlineResourceType {
        /**
         * The type couldn't be determined or is unsupported.
         */
        Unsupported,
        /**
         * OGC Web Coverage Service.
         */
        WCS,
        /**
         * OGC Web Map Service.
         */
        WMS,
        /**
         * OGC Web Feature Service.
         */
        WFS,
        /**
         * OpenDAP.
         */
        OPeNDAP,

        /**
         * A FTP (File Transfer Protocol) link
         */
        FTP,

        /**
         * A generic web link.
         */
        WWW,

        /**
         * A SOS Service
         */
        SOS,

        /**
         * IRIS Web Service
         */
        IRIS,

        /**
         * A CSW Service. e.g. a GeoNetwork /csw endpoint. Can be used a dummy resource for when you don't want to cache all CSW records on load.
         */
        CSWService,

        /**
         * A NetCDF Subset Service.
         */
        NCSS
    }

    /**
     * Gets the URL location of this online resource.
     * 
     * @return
     */
    public abstract URL getLinkage();

    /**
     * Gets the protocol of this online resource.
     * 
     * @return
     */
    public abstract String getProtocol();

    /**
     * Gets the name of this online resource.
     * 
     * @return
     */
    public abstract String getName();

    /**
     * Gets a description of this online resource.
     * 
     * @return
     */
    public abstract String getDescription();

    /**
     * Gets the application profile (if available) of this online resource.
     * 
     * @return
     */
    public abstract String getApplicationProfile();

    /**
     * provide the protocol version if possible. eg WMS 1.1.1 vs 1.3
     * 
     * @return version if possible
     */
    public abstract String getVersion();

    /**
     * Gets a simplification of the protocol that this online resource represents.
     * 
     * @return
     */
    public OnlineResourceType getType() {
        String lowerProtocol = getProtocol();
        if (lowerProtocol == null) {
            return OnlineResourceType.Unsupported;
        }

        lowerProtocol = lowerProtocol.toLowerCase();
        if (lowerProtocol.contains("wfs")) {
            return OnlineResourceType.WFS;
        } else if (lowerProtocol.contains("wms")) {
            return OnlineResourceType.WMS;
        } else if (lowerProtocol.contains("wcs")) {
            return OnlineResourceType.WCS;
        } else if (lowerProtocol.contains("www:link-1.0-http--link")
                || lowerProtocol.contains("www:download-1.0-http--download")) {
            //Dap is currently hacked in
            String name = getDescription();
            if ((name != null) && name.equals("HACK-OPENDAP")) {
                return OnlineResourceType.OPeNDAP;
            }

            return OnlineResourceType.WWW;
        } else if (lowerProtocol.contains("ogc:sos-")) {
            return OnlineResourceType.SOS;
        } else if (lowerProtocol.contains("www:download-1.0-ftp--download")) {
            return OnlineResourceType.FTP;
        } else if (lowerProtocol.contains("iris")) {
            return OnlineResourceType.IRIS;
        } else if (lowerProtocol.contains("cswservice")) {
            return OnlineResourceType.CSWService;
        } else if (lowerProtocol.contains("ncss")) {
            return OnlineResourceType.NCSS;
        }

        return OnlineResourceType.Unsupported;
    }

    public boolean accept(CSWRecordsFilterVisitor visitor) {
        return visitor.visit(this);
    }
}
