package org.auscope.portal.csw;

import java.net.URL;

/**
 * Represents a <gmd:CI_OnlineResource> element in a CSW response
 * @author vot002
 *
 */
public abstract class CSWOnlineResource {

    /**
     * A simplification of the protocol
     * @author vot002
     *
     */
    public enum OnlineResourceType {
        /**
         * The type couldnt be determined or is unsupported
         */
        Unsupported,
        /**
         * OGC Web Coverage Service
         */
        WCS,
        /**
         * OGC Web Map Service
         */
        WMS,
        /**
         * OGC Web Feature Service
         */
        WFS,
        /**
         * OpenDAP
         */
        OPeNDAP,

        /**
         * A generic web link
         */
        WWW
    }

    /**
     * Gets the URL location of this online resource
     * @return
     */
    public abstract URL getLinkage();
    /**
     * Gets the protocol of this online resource
     * @return
     */
    public abstract String getProtocol();
    /**
     * Gets the name of this online resource
     * @return
     */
    public abstract String getName();
    /**
     * Gets a description of this online resource
     * @return
     */
    public abstract String getDescription();

    /**
     * Gets a simplification of the protocol that this online resource represents.
     * @return
     */
    public OnlineResourceType getType() {
        String lowerProtocol = getProtocol();
        if (lowerProtocol == null)
            return OnlineResourceType.Unsupported;

        lowerProtocol = lowerProtocol.toLowerCase();
        if (lowerProtocol.contains("wfs")) {
            return OnlineResourceType.WFS;
        } else if (lowerProtocol.contains("wms")) {
            return OnlineResourceType.WMS;
        } else if (lowerProtocol.contains("wcs")) {
            return OnlineResourceType.WCS;
        } else if (lowerProtocol.contains("www:link-1.0-http--link") || lowerProtocol.contains("www:download-1.0-http--download")) {
            //Dap is currently hacked in
            String name = getDescription();
            if ((name != null) && name.equals("HACK-OPENDAP")) {
                return OnlineResourceType.OPeNDAP;
            }

            return OnlineResourceType.WWW;
        }

        return OnlineResourceType.Unsupported;
    }
}
