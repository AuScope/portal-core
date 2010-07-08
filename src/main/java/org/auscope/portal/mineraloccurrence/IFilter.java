package org.auscope.portal.mineraloccurrence;

/**
 * User: Mathew Wyatt
 * Date: 23/03/2009
 * Time: 1:58:36 PM
 */
public interface IFilter {

    /**
     * The implementation of this method should return a valid
     * ogc:Filter xml blob - http://www.opengeospatial.org/standards/filter
     *
     * The filter should be designed to fetch all appropriate records for a given filter
     *
     * @return String
     */
    public String getFilterStringAllRecords();
    
    
    /**
     * The implementation of this method should return a valid
     * ogc:Filter xml blob - http://www.opengeospatial.org/standards/filter
     *
     * The filter should be designed to fetch all appropriate records that pass the filter AND
     * appear within the the specified bounding box
     *
     * @param bboxSrs The srs that the other parameters are using
     * @param lowerCornerPoints The point(s) representing the lower corner of the bounding box
     * @param upperCornerPoints The point(s) representing the upper corner of the bounding box
     * @return String
     */
    public String getFilterStringBoundingBox(FilterBoundingBox bbox);
}
