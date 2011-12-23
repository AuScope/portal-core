package org.auscope.portal.server.domain.filter;

import java.io.Serializable;
import java.util.Arrays;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is only temporary - it is intended to be overridden by the geotools ogc filter library.
 *
 * @author VOT002
 */
public class FilterBoundingBox implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant log. */
    protected static final Log log = LogFactory.getLog(FilterBoundingBox.class);

    /** The bbox srs. */
    private String bboxSrs;

    /** The lower corner points. */
    private double[] lowerCornerPoints;

    /** The upper corner points. */
    private double[] upperCornerPoints;

    /**
     * Instantiates a new filter bounding box.
     */
    public FilterBoundingBox() {

    }

    /**
     * Instantiates a new filter bounding box.
     *
     * @param bboxSrs the bbox srs
     * @param lowerCornerPoints the lower corner points
     * @param upperCornerPoints the upper corner points
     */
    public FilterBoundingBox(String bboxSrs, double[] lowerCornerPoints,
            double[] upperCornerPoints) {
        this.bboxSrs = bboxSrs;
        this.lowerCornerPoints = lowerCornerPoints;
        this.upperCornerPoints = upperCornerPoints;
    }

    /**
     * Gets the bbox srs.
     *
     * @return the bbox srs
     */
    public String getBboxSrs() {
        return bboxSrs;
    }

    /**
     * Sets the bbox srs.
     *
     * @param bboxSrs the new bbox srs
     */
    public void setBboxSrs(String bboxSrs) {
        this.bboxSrs = bboxSrs;
    }

    /**
     * Gets the lower corner points.
     *
     * @return the lower corner points
     */
    public double[] getLowerCornerPoints() {
        return lowerCornerPoints;
    }

    /**
     * Sets the lower corner points.
     *
     * @param lowerCornerPoints the new lower corner points
     */
    public void setLowerCornerPoints(double[] lowerCornerPoints) {
        this.lowerCornerPoints = lowerCornerPoints;
    }

    /**
     * Gets the upper corner points.
     *
     * @return the upper corner points
     */
    public double[] getUpperCornerPoints() {
        return upperCornerPoints;
    }

    /**
     * Sets the upper corner points.
     *
     * @param upperCornerPoints the new upper corner points
     */
    public void setUpperCornerPoints(double[] upperCornerPoints) {
        this.upperCornerPoints = upperCornerPoints;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FilterBoundingBox [bboxSrs=" + bboxSrs + ", lowerCornerPoints="
                + Arrays.toString(lowerCornerPoints) + ", upperCornerPoints="
                + Arrays.toString(upperCornerPoints) + "]";
    }

    /**
     * Compares this FilterBoundingBox with another instance. The comparison is made directly on doubles so
     * this method may yield inaccurate results.
     *
     * @param bbox the bbox
     * @return true, if successful
     */
    public boolean equals(FilterBoundingBox bbox) {
        if (this.bboxSrs != bbox.bboxSrs) {
            return false;
        }

        if (this.bboxSrs != null && bbox.bboxSrs != null && !this.bboxSrs.equals(bbox.bboxSrs)) {
            return false;
        }

        if (!Arrays.equals(this.lowerCornerPoints, bbox.lowerCornerPoints)) {
            return false;
        }
        if (!Arrays.equals(this.upperCornerPoints, bbox.upperCornerPoints)) {
            return false;
        }

        return true;
    }

    /**
     * Attempts to parse a FilterBoundingbox.
     *
     * @param obj the obj
     * @return the filter bounding box
     * @throws Exception the exception
     */
    public static FilterBoundingBox parseFromJSON(JSONObject obj) throws Exception {
        //Our filter bbox can come in a couple of formats
        if (obj.containsKey("lowerCornerPoints") && obj.containsKey("upperCornerPoints")) {
            FilterBoundingBox result = new FilterBoundingBox(obj.getString("bboxSrs"), null, null);

            JSONArray lowerCornerPoints = obj.getJSONArray("lowerCornerPoints");
            JSONArray upperCornerPoints = obj.getJSONArray("upperCornerPoints");

            result.lowerCornerPoints = new double[lowerCornerPoints.size()];
            result.upperCornerPoints = new double[upperCornerPoints.size()];

            for (int i = 0; i < lowerCornerPoints.size(); i++) {
                result.lowerCornerPoints[i] = lowerCornerPoints.getDouble(i);
            }

            for (int i = 0; i < upperCornerPoints.size(); i++) {
                result.upperCornerPoints[i] = upperCornerPoints.getDouble(i);
            }

            return result;
        } else if (obj.containsKey("eastBoundLongitude") && obj.containsKey("westBoundLongitude") &&
                obj.containsKey("northBoundLatitude") && obj.containsKey("southBoundLatitude")) {
            FilterBoundingBox result = new FilterBoundingBox(obj.getString("crs"), null, null);

            double eastBoundLongitude = obj.getDouble("eastBoundLongitude");
            double westBoundLongitude = obj.getDouble("westBoundLongitude");
            double northBoundLatitude = obj.getDouble("northBoundLatitude");
            double southBoundLatitude = obj.getDouble("southBoundLatitude");

            double adjustedWestBoundLong = westBoundLongitude;
            double adjustedEastBoundLong = eastBoundLongitude;

            //this is so we can fetch data when our bbox is crossing the anti meridian
            //Otherwise our bbox wraps around the WRONG side of the planet
            if (adjustedWestBoundLong <= 0 && adjustedEastBoundLong >= 0 ||
                adjustedWestBoundLong >= 0 && adjustedEastBoundLong <= 0) {
                adjustedWestBoundLong = (westBoundLongitude < 0) ? (180 - westBoundLongitude) : westBoundLongitude;
                adjustedEastBoundLong = (eastBoundLongitude < 0) ? (180 - eastBoundLongitude) : eastBoundLongitude;
            }

            result.lowerCornerPoints = new double[] {Math.min(adjustedWestBoundLong, adjustedEastBoundLong), Math.min(northBoundLatitude, southBoundLatitude)};
            result.upperCornerPoints = new double[] {Math.max(adjustedWestBoundLong, adjustedEastBoundLong), Math.max(northBoundLatitude, southBoundLatitude)};

            return result;
        }

        throw new IllegalArgumentException("obj cannot be decoded");

    }

    /**
     * Convenience method to parse a bbox from a JSON string. Returns null if the parsing fails
     *
     * @param json the json
     * @return the filter bounding box
     */
    public static FilterBoundingBox attemptParseFromJSON(String json) {
        FilterBoundingBox bbox = null;
        try {
            if (json != null && json != "") {
                JSONObject obj = JSONObject.fromObject(json);
                bbox = FilterBoundingBox.parseFromJSON(obj);
                log.debug("bbox=" + bbox.toString());
            } else {
                log.debug("No bbox string, null will be returned");
            }
        } catch (Exception ex) {
            log.warn("Couldnt parse bounding box filter (Invalid Values): " + ex);
        }

        return bbox;
    }
}
