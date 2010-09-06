package org.auscope.portal.server.web.service;

import java.util.Collection;

import org.auscope.portal.mineraloccurrence.Commodity;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * Service for operations on Commodity objects.
 * 
 * @author Jarek Sanders
 * @version $Id$
 */
public interface CommodityService {
    /**
     * Will return EVERY feature
     * @param serviceURL
     * @param commodityName
     * @return
     * @throws Exception
     */
    public Collection<Commodity> getAll(String serviceURL, String commodityName, int maxFeatures) throws Exception;
    
    /**
     * Will return (maximum 200) features in the specified bounding box
     * @param serviceURL
     * @param commodityName
     * @param bboxSrs
     * @param lowerCornerPoints
     * @param upperCornerPoints
     * @return
     * @throws Exception
     */
    public Collection<Commodity> getVisible(String serviceURL, String commodityName, FilterBoundingBox bbox, int maxFeatures) throws Exception;
}
