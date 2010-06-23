package org.auscope.portal.server.web.service;

import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;

/**
 * Service for operations on GetCapability objects
 * 
 * @author JarekSanders
 * @version $Id$
 */
public interface GetCapabilitiesService {
    public GetCapabilitiesRecord getWmsCapabilities(String serviceURL)
    throws Exception;
}
