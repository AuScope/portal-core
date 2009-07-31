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
     * @return String
     */
    public String getFilterString();
}
