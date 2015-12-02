package org.auscope.portal.core.services.responses.csw;

import java.net.URL;

/**
 * Represents a <gmd:CI_OnlineResource> element in a CSW response
 * 
 * @author vot002
 *
 */
public class CSWOnlineResourceImpl extends AbstractCSWOnlineResource {
    private URL linkage;
    private String protocol;
    private String name;
    private String description;
    private String applicationProfile;

    public CSWOnlineResourceImpl(URL linkage, String protocol, String name,
            String description) {
        this(linkage, protocol, name, description, "");
    }

    public CSWOnlineResourceImpl(URL linkage, String protocol, String name,
            String description, String applicationProfile) {
        super();
        this.linkage = linkage;
        this.protocol = protocol;
        this.name = name;
        this.description = description;
        this.applicationProfile = applicationProfile;
    }

    @Override
    public String getApplicationProfile() {
        return applicationProfile;
    }

    @Override
    public URL getLinkage() {
        return linkage;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * provide the protocol version if possible. eg WMS 1.1.1 vs 1.3
     * 
     * @return version if possible
     */
    @Override
    public String getVersion() {
        //VT: Currently only cater to WMS. Can be expanded in the future for others such as wfs
        if (this.getType() == OnlineResourceType.WMS) {
            if (this.getProtocol().contains("1.3.0")) {
                return "1.3.0";
            } else {
                return "1.1.1";//VT:Default to 1.1.1 for wms.
            }
        } else {
            return "";
        }
    }

}
