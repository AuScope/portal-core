package org.auscope.portal.csw;

import java.net.URL;

/**
 * Represents a <gmd:CI_OnlineResource> element in a CSW response
 * @author vot002
 *
 */
public class CSWOnlineResourceImpl extends CSWOnlineResource {
    private URL linkage;
    private String protocol;
    private String name;
    private String description;
    
    public CSWOnlineResourceImpl(URL linkage, String protocol, String name,
            String description) {
        super();
        this.linkage = linkage;
        this.protocol = protocol;
        this.name = name;
        this.description = description;
    }
    
    public URL getLinkage() {
        return linkage;
    }
    public String getProtocol() {
        return protocol;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    
}
