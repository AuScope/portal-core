package org.auscope.portal.core.services.methodmakers.filter;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.OgcServiceProviderType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
     * @param bboxSrs
     *            the bbox srs
     * @param lowerCornerPoints
     *            the lower corner points
     * @param upperCornerPoints
     *            the upper corner points
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
     * @param bboxSrs
     *            the new bbox srs
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
     * @param lowerCornerPoints
     *            the new lower corner points
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
     * @param upperCornerPoints
     *            the new upper corner points
     */
    public void setUpperCornerPoints(double[] upperCornerPoints) {
        this.upperCornerPoints = upperCornerPoints;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FilterBoundingBox [bboxSrs=" + bboxSrs + ", lowerCornerPoints="
                + Arrays.toString(lowerCornerPoints) + ", upperCornerPoints="
                + Arrays.toString(upperCornerPoints) + "]";
    }

    /**
     * Compares this FilterBoundingBox with another instance. The comparison is made directly on doubles so this method may yield inaccurate results.
     *
     * @param bbox
     *            the bbox
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
     * Utility method for creating a new FilterBoundingBox from lat/long coordinate pairs. Take into account the geoserver providing the service.
     * 
     * @param crs
     * @param northBoundLatitude
     * @param southBoundLatitude
     * @param eastBoundLongitude
     * @param westBoundLongitude
     * @param ogcServiceProviderType
     *            - GPT-74 - Need to change output depending on server {@link #parseFromValues(String, double, double, double, double, OgcServiceProviderType)}
     * @return
     */
    public static FilterBoundingBox parseFromValues(String crs,
            double northBoundLatitude,
            double southBoundLatitude,
            double eastBoundLongitude,
            double westBoundLongitude, OgcServiceProviderType ogcServiceProviderType) {

        if (eastBoundLongitude < -120) {
            eastBoundLongitude = 180 + (180 + eastBoundLongitude);
        }

        Double eastWestMin = Math.min(westBoundLongitude, eastBoundLongitude);
        Double eastWestMax = Math.max(westBoundLongitude, eastBoundLongitude);
        Double northSouthMin = Math.min(northBoundLatitude, southBoundLatitude);
        Double northSouthMax = Math.max(northBoundLatitude, southBoundLatitude);

        if (ogcServiceProviderType == OgcServiceProviderType.ArcGis) {
            return new FilterBoundingBox(crs,
                    new double[] {northSouthMin, eastWestMin},
                    new double[] {northSouthMax, eastWestMax});
        } else {
            return new FilterBoundingBox(crs,
                    new double[] {eastWestMin, northSouthMin},
                    new double[] {eastWestMax, northSouthMax});

        }

    }

    /**
     * Utility method for creating a new FilterBoundingBox from lat/long coordinate pairs. Take into account the geoserver providing the service.
     * This one exists for backwards compatibility.
     * 
     * @param crs
     * @param northBoundLatitude
     * @param southBoundLatitude
     * @param eastBoundLongitude
     * @param westBoundLongitude
     * @param OgcServiceProviderType
     *            - GPT-74 - Need to change output depending on server {@link #parseFromValues(String, double, double, double, double, OgcServiceProviderType)}
     * @return
     */
    public static FilterBoundingBox parseFromValues(String crs,
            double northBoundLatitude,
            double southBoundLatitude,
            double eastBoundLongitude,
            double westBoundLongitude) {
        return parseFromValues(crs, northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, OgcServiceProviderType.GeoServer);
    }

    /**
     * TODO: Temporary workaround for AUS-2309. Should replace parseFromJSON above in v2.11.1.
     *
     * @param obj
     *            the obj
     * @param OgcServiceProviderType
     *            - GPT-74 - Need to change output depending on server {@link #parseFromValues(String, double, double, double, double, OgcServiceProviderType)}
     * @return the filter bounding box
     */
    public static FilterBoundingBox parseFromJSON(JSONObject obj, OgcServiceProviderType OgcServiceProviderType) {
        // Our filter bbox can come in a couple of formats
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

            Double eastBound = obj.getDouble("eastBoundLongitude");

            // VT:Special case only for making GeoServer request as if the east of a bounding box hits -180, Geoserver treats it as west.
            // VT:Therefore we want east to always be positive
            if (eastBound < -120) {
                eastBound = 180 + (180 + eastBound);
            }

            return parseFromValues(obj.getString("crs"),
                    obj.getDouble("northBoundLatitude"),
                    obj.getDouble("southBoundLatitude"),
                    eastBound,
                    obj.getDouble("westBoundLongitude"), OgcServiceProviderType);
        }

        throw new IllegalArgumentException("obj cannot be decoded");

    }

    /**
     * TODO: Temporary workaround for AUS-2309. Should replace parseFromJSON above in v2.11.1.
     * 
     * This version is for backwards compatibility.
     *
     * @param obj
     *            the obj
     * @return the filter bounding box
     */
    public static FilterBoundingBox parseFromJSON(JSONObject json) {
        return parseFromJSON(json, OgcServiceProviderType.GeoServer);
    }
    
    // TODO: remove after AUS:2309 is fixed
    /**
     * 
     * @param json
     * @param OgcServiceProviderType
     *            - GPT-74 - Need to change output depending on server {@link #parseFromValues(String, double, double, double, double, OgcServiceProviderType)}
     * @return
     */
    public static FilterBoundingBox attemptParseFromJSON(String json, OgcServiceProviderType OgcServiceProviderType) {
        FilterBoundingBox bbox = null;
        try {
            if (json != null && !json.isEmpty()) {
                JSONObject obj = JSONObject.fromObject(json);
                bbox = FilterBoundingBox.parseFromJSON(obj, OgcServiceProviderType);
                log.debug("bbox=" + bbox.toString());
            } else {
                log.debug("No bbox string, null will be returned");
            }
        } catch (Exception ex) {
            log.warn("Couldnt parse bounding box filter (Invalid Values): " + ex);
        }

        return bbox;
    }
    
    /**
     * Method for backwards compatibility.
     * 
     * @param json
     * @return
     */
    public static FilterBoundingBox attemptParseFromJSON(String json) {
       return  attemptParseFromJSON(json, OgcServiceProviderType.GeoServer);
    }

    /**
     * Return the object as a Json string made up of North, South, East and West lower and upper corners
     * 
     * @return
     */
    public String toJsonNewsFormat(OgcServiceProviderType ogcServiceProviderType) {
        StringBuffer json = new StringBuffer("{");

        json.append("'crs':'" + getBboxSrs() + "'");
        json.append(",");

        if (OgcServiceProviderType.ArcGis.equals(ogcServiceProviderType)) {
            json.append("'northBoundLatitude':'").append(getUpperCornerPoints()[0]);
            json.append(",");
            json.append("'eastBoundLatitude':'").append(getUpperCornerPoints()[1]);
            json.append(",");
            json.append("'southBoundLatitude':'").append(getLowerCornerPoints()[0]);
            json.append(",");
            json.append("'westBoundLatitude':'").append(getLowerCornerPoints()[1]);
        } else {
            json.append("'eastBoundLatitude':'").append(getUpperCornerPoints()[0]);
            json.append(",");
            json.append("'northBoundLatitude':'").append(getUpperCornerPoints()[1]);
            json.append(",");
            json.append("'westBoundLatitude':'").append(getLowerCornerPoints()[0]);
            json.append(",");
            json.append("'southBoundLatitude':'").append(getLowerCornerPoints()[1]);
        }

        json.append("}");

        return json.toString();
    }

    /**
     * Return the object as a Json string made up of North, South, East and West lower and upper corners
     * 
     * @return
     */
    public String toJsonCornersFormat() {
        StringBuffer json = new StringBuffer("{");

        json.append("'bboxSrs':'" + getBboxSrs() + "'");
        json.append(",");

        json.append("'lowerCornerPoints':[");
        for (double d : getLowerCornerPoints()) {
            json.append(d).append(",");
        }
        json.append("],");
        json.append("'upperCornerPoints':[");
        for (double d : getUpperCornerPoints()) {
            json.append(d).append(",");
        }
        json.append("]");

        json.append("}");

        return json.toString();
    }
}
