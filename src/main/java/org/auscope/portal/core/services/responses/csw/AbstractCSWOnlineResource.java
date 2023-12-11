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
        NCSS,

        /**
         * DOI
         */
        DOI,

        /**
         * KML Layer
         */
        KML,

        /**
         * VMF Layer
         */
        VMF
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
     * provide the protocol version if possible. eg WMS 1.1.1 vs 1.3.0
     * 
     * @return version if possible
     */
    public abstract String getVersion();
    
    /**
     * provide the protocol request if possible
     * 
     * @return protocol request if possible
     */
    public abstract String getProtocolRequest();

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
        } else if (lowerProtocol.contains("doi")) {
            return OnlineResourceType.DOI;
        } else if (lowerProtocol.contains("kml")) {
            return OnlineResourceType.KML;
        } else if (lowerProtocol.contains("vmf")) {
            return OnlineResourceType.VMF;
        }
        
        return OnlineResourceType.Unsupported;
    }

    public boolean accept(CSWRecordsFilterVisitor visitor) {
        return visitor.visit(this);
    }

    /**
     * Override hashCode to support value-based comparison by "HashSet" and similar classes
     */
    @Override
    public int hashCode() {
        int result = 17;
        if (getLinkage() != null) {
            result = 31 * result + getLinkage().hashCode();
        }
        if (getProtocol() != null) {
            result = 31 * result + getProtocol().hashCode();
        }
        if (getName() != null) {
            result = 31 * result + getName().hashCode();
        }
        if (getDescription() != null) {
            result = 31 * result + getDescription().hashCode();
        }
        if (getApplicationProfile() != null) {
            result = 31 * result + getApplicationProfile().hashCode();
        }
        if (getVersion() != null) {
            result = 31 * result + getVersion().hashCode();
        }
        if (getProtocolRequest() != null) {
            result = 31 * result + getProtocolRequest().hashCode();
        }
        return result;
    }


    /**
     * Override equals() to compare two objects by value instead of by reference
     * @param o object
     *
     * @return true if two objects have same value, even if they are not the same object
     */
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        // Check if o is an instance of AbstractCSWOnlineResource
        if (!(o instanceof AbstractCSWOnlineResource)) {
            return false;
        }

        // Typecast o to AbstractCSWOnlineResource so that we can access data members
        AbstractCSWOnlineResource cswOnlineRes = (AbstractCSWOnlineResource) o;
        boolean result = true;
        // If both linkages are non-null they can be compared
        if (getLinkage() != null && cswOnlineRes.getLinkage() != null) {
            String linkage = getLinkage().toString();
            result = result && linkage.equals(cswOnlineRes.getLinkage().toString());
        // If either linkage is non-null then the other will be null - so they aren't the same
        } else if (getLinkage() != null || cswOnlineRes.getLinkage() != null) {
            return false;
        }

        // Compare the values of all the other data members
        result = result && getProtocol().equals(cswOnlineRes.getProtocol()) &&
               getName().equals(cswOnlineRes.getName()) &&
               getDescription().equals(cswOnlineRes.getDescription()) &&
               getApplicationProfile().equals(cswOnlineRes.getApplicationProfile()) &&
               getVersion().equals(cswOnlineRes.getVersion()) &&
               getProtocolRequest().equals(cswOnlineRes.getProtocolRequest());
        return result;
    }
}
