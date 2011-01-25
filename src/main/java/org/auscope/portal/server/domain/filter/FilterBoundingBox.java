package org.auscope.portal.server.domain.filter;

import java.io.Serializable;
import java.util.Arrays;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is only temporary - it is intended to be overridden by the geotools ogc filter library
 * @author VOT002
 */
public class FilterBoundingBox implements Serializable {
    
    protected static final Log log = LogFactory.getLog(FilterBoundingBox.class);
    
    private String bboxSrs;
    private double[] lowerCornerPoints;
    private double[] upperCornerPoints;
    
    public FilterBoundingBox() {
        
    }
    
    public FilterBoundingBox(String bboxSrs, double[] lowerCornerPoints,
            double[] upperCornerPoints) {
        this.bboxSrs = bboxSrs;
        this.lowerCornerPoints = lowerCornerPoints;
        this.upperCornerPoints = upperCornerPoints;
    }
    public String getBboxSrs() {
        return bboxSrs;
    }
    public void setBboxSrs(String bboxSrs) {
        this.bboxSrs = bboxSrs;
    }
    public double[] getLowerCornerPoints() {
        return lowerCornerPoints;
    }
    public void setLowerCornerPoints(double[] lowerCornerPoints) {
        this.lowerCornerPoints = lowerCornerPoints;
    }
    public double[] getUpperCornerPoints() {
        return upperCornerPoints;
    }
    
    public void setUpperCornerPoints(double[] upperCornerPoints) {
        this.upperCornerPoints = upperCornerPoints;
    }
    
    @Override
    public String toString() {
        return "FilterBoundingBox [bboxSrs=" + bboxSrs + ", lowerCornerPoints="
                + Arrays.toString(lowerCornerPoints) + ", upperCornerPoints="
                + Arrays.toString(upperCornerPoints) + "]";
    }
    
    /**
     * Attempts to parse a FilterBoundingbox
     * @param bboxSrs
     * @param csvLowerCornerPoints a String of CSV seperated doubles
     * @param csvUpperCornerPoints a String of CSV seperated doubles
     * @return
     */
    public static FilterBoundingBox parseFromJSON(JSONObject obj) throws Exception {
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
    }
    
    /**
     * Convenience method to parse a bbox from a JSON string. Returns null if the parsing fails
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
