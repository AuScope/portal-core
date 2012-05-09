package org.auscope.portal.server.domain.geodesy;

/**
 * Represents a single geodesy:station_observations feature
 * @author Josh Vote
 */
public class GeodesyObservation {
    /** The ID of the station that generated this observation */
    private String stationId;
    /** The date of this observation */
    private String date;
    /** Where the observation data can be downloaded from */
    private String url;

    /**
     * Constructor
     * @param stationId The ID of the station that generated this observation
     * @param date The date of this observation
     * @param url Where the observation data can be downloaded from
     */
    public GeodesyObservation(String stationId, String date, String url) {
        this.stationId = stationId;
        this.date = date;
        this.url = url;
    }

    /**
     * Gets the ID of the station that generated this observation
     * @return
     */
    public String getStationId() {
        return stationId;
    }

    /**
     * Gets the date of this observation
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets where the observation data can be downloaded from
     * @return
     */
    public String getUrl() {
        return url;
    }
}
