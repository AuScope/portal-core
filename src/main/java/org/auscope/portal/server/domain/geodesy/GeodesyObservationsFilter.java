package org.auscope.portal.server.domain.geodesy;

import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * Class for generating WFS filters for a Geodesy data service
 * @author Josh Vote
 *
 */
public class GeodesyObservationsFilter extends AbstractFilter {

    /** The station ID to limit the filter to */
    private String stationId;
    /** The start date in the form 'YYYY-mm-DD' */
    private String startDate;
    /** The end date in the form 'YYYY-mm-DD' */
    private String endDate;


    /**
     * Constructor
     * @param stationId The station ID to limit the filter to
     * @param startDate The start date in the form 'YYYY-mm-DD'
     * @param endDate The end date in the form 'YYYY-mm-DD'
     */
    public GeodesyObservationsFilter(String stationId, String startDate,
            String endDate) {
        super();
        this.stationId = stationId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * See parent class definition
     */
    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generatePropertyIsEqualToFragment("station_id", this.stationId),
                        this.generatePropertyIsGreaterThanOrEqualTo("ob_date", this.startDate),
                        this.generatePropertyIsLessThanOrEqualTo("ob_date", this.endDate)));
    }

    /**
     * Not implemented, returns getFilterStringAllRecords();
     */
    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        return this.getFilterStringAllRecords(); //not implemented
    }

}
