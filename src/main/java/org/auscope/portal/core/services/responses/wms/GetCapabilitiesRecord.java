package org.auscope.portal.core.services.responses.wms;

import java.util.ArrayList;

/**
 * This class represents response to GetCapabilites query.
 *
 * @version $Id$
 */
public interface GetCapabilitiesRecord {

    // ------------------------------------------ Attribute Setters and Getters

    /**
     * Checks if is wFS.
     *
     * @return true, if is wFS
     */
    public boolean isWFS();

    /**
     * Checks if is wMS.
     *
     * @return true, if is wMS
     */
    public boolean isWMS();

    /**
     * Gets the service type.
     *
     * @return the service type
     */
    public String getServiceType();

    /**
     * Gets the organisation.
     *
     * @return the organisation
     */
    public String getOrganisation();

    /**
     * Gets the URL that the GetCapabilities response has defined to be used for GetMap requests.
     *
     * @return the map url
     */
    public String getMapUrl();

    /**
     * Gets the Metadata URL in the base layer of this record.
     *
     * @return the MetadataURL element
     */
    public String getMetadataUrl();

    /**
     * Gets the layers.
     *
     * @return the layers
     */
    public ArrayList<GetCapabilitiesWMSLayerRecord> getLayers();

    /**
     * Gets the layer srs.
     *
     * @return the layer srs
     */
    public String[] getLayerSRS();

    /**
     * Returns an array of MIME strings representing the valid format for the GetMap operation
     *
     * @return
     */
    public String[] getGetMapFormats();

    public String getVersion();

    // ------------------------------------------------------ Protected Methods

}
