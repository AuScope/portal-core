package org.auscope.portal.server.web.service;

import java.util.Collection;

import org.auscope.portal.mineraloccurrence.Commodity;

/**
 * Service for operations on Commodity objects.
 * 
 * @author Jarek Sanders
 * @version $Id$
 */
public interface CommodityService {
    public Collection<Commodity> get(String serviceURL, String commodityName) 
    throws Exception;
}
