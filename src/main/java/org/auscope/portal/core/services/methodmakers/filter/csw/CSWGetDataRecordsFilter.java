package org.auscope.portal.core.services.methodmakers.filter.csw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.auscope.portal.core.services.methodmakers.filter.AbstractFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.joda.time.DateTime;

/**
 * Represents a OGC:Filter that will fetch matching records from a CS/W.
 *
 * @author Josh Vote
 *
 */
public class CSWGetDataRecordsFilter extends AbstractFilter {

    /**
     * How the list of keywords will be used to match records.
     */
    public enum KeywordMatchType {

        /** Any record that matches ANY of the specified keywords will pass. */
        Any,

        /** Any record that matches EACH AND EVERY keyword in the specified list will pass. */
        All
    }

    /** The any text. */
    private String anyText;
    
    /** The title. */
    private String title = null;
    
    /** The abstract. */
    private String abstract_ = null;
    
    /** The metadata change date's lower bound. */
    private DateTime metadataChangeDateFrom = null;
    
    /** The metadata change date's upper bound. */
    private DateTime metadataChangeDateTo = null;
    
    /** The temporal extent's lower bound. */
    private DateTime temporalExtentFrom = null;
    
    /** The temporal extent's upper bound. */
    private DateTime temporalExtentTo = null;

    /** The spatial bounds. */
    private FilterBoundingBox spatialBounds;

    /** The keywords. */
    private String[] keywords;

    /** The capture platform. */
    private String capturePlatform;

    /** The sensor. */
    private String sensor;

    /** The keyword match type. */
    private KeywordMatchType keywordMatchType;

    /**
     * Generates a new filter generator for the specified fields.
     *
     * @param anyText [Optional] The text used to query the 'AnyText' attribute
     * @param spatialBounds
     *            [Optional] The spatial bounds to filter by
     */
    public CSWGetDataRecordsFilter(String anyText, FilterBoundingBox spatialBounds) {
        this(anyText, spatialBounds, null, null, null, null);
    }    
    
    /**
     * Generates a new filter generator for the specified fields.
     *
     * @param anyText [Optional] The text used to query the 'AnyText' attribute
     * @param spatialBounds
     *            [Optional] The spatial bounds to filter by
     * @param keywords
     *            [Optional] A list of keywords which must ALL be satisfied for
     *            a record to be included
     * @param capturePlatform
     *            [Optional] A capture platform filter that must be specified
     *            for a record to be included
     * @param sensor
     *            [Optional] A sensor filter that must be specified for a record
     *            to be included
     */
    public CSWGetDataRecordsFilter(String anyText, FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor) {
        this(anyText, spatialBounds, keywords, capturePlatform, sensor, null);
    }

    /**
     * Generates a new filter generator for the specified fields.
     *
     * @param anyText [Optional] The text used to query the 'AnyText' attribute
     * @param spatialBounds
     *            [Optional] The spatial bounds to filter by
     * @param keywords
     *            [Optional] A list of keywords which must ALL be satisfied for
     *            a record to be included
     * @param capturePlatform
     *            [Optional] A capture platform filter that must be specified
     *            for a record to be included
     * @param sensor
     *            [Optional] A sensor filter that must be specified for a record
     *            to be included
     * @param keywordMatchType [Optional] How the list of keywords will be matched (defaults to All)
     */
    public CSWGetDataRecordsFilter(String anyText, FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor,
            KeywordMatchType keywordMatchType) {
        this.anyText = anyText;
        this.spatialBounds = spatialBounds;
        this.keywords = keywords;
        this.capturePlatform = capturePlatform;
        this.sensor = sensor;
        this.keywordMatchType = keywordMatchType;
    }

    /**
     * Utility method for generating the body of a filter fragment.
     * @return a filter fragment
     */
    private String generateFilterFragment() {
        List<String> fragments = new ArrayList<String>();

        if (anyText != null && !anyText.isEmpty()) {
            fragments.add(this.generatePropertyIsLikeFragment("anytext", this.anyText));
        }
        
        if (title != null && !title.isEmpty()) {
            fragments.add(this.generatePropertyIsLikeFragment("title", this.title));
        }
        
        if (abstract_ != null && !abstract_.isEmpty()) {
            fragments.add(this.generatePropertyIsLikeFragment("abstract", this.abstract_));
        }

        if (spatialBounds != null) {
            fragments.add(this.generateBboxFragment(spatialBounds, "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement"));
        }

        if (keywords != null && keywords.length > 0) {
            List<String> keywordFragments = new ArrayList<String>();
            for (String keyword : keywords) {
                if (keyword != null && !keyword.isEmpty()) {
                    //keywordFragments.add(this.generatePropertyIsEqualToFragment("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString", keyword));
                    keywordFragments.add(this.generatePropertyIsEqualToFragment("keyword", keyword));
                }
            }

            if (keywordMatchType == null || keywordMatchType == KeywordMatchType.All) {
                fragments.add(this.generateAndComparisonFragment(keywordFragments.toArray(new String[keywordFragments.size()])));
            } else {
                fragments.add(this.generateOrComparisonFragment(keywordFragments.toArray(new String[keywordFragments.size()])));
            }
        }

        if (capturePlatform != null && !capturePlatform.isEmpty()) {
            fragments.add(this.generatePropertyIsEqualToFragment("capturePlatform", capturePlatform));
        }

        if (sensor != null && !sensor.isEmpty()) {
            fragments.add(this.generatePropertyIsEqualToFragment("sensor", sensor));
        }
        
        if (temporalExtentFrom != null ) {
            fragments.add(this.generatePropertyIsGreaterThanOrEqualTo("TempExtent_begin", temporalExtentFrom.toString()));
        }
        
        if (temporalExtentTo != null ) {
            fragments.add(this.generatePropertyIsLessThanOrEqualTo("TempExtent_end", temporalExtentTo.toString()));
        }
        
        if (metadataChangeDateFrom != null ) {
            fragments.add(this.generatePropertyIsGreaterThanOrEqualTo("changeDate", metadataChangeDateFrom.toString()));
        }
        
        if (metadataChangeDateTo != null ) {
            fragments.add(this.generatePropertyIsLessThanOrEqualTo("changeDate", metadataChangeDateTo.toString()));
        }

        return this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()]));
    }

    /**
     * Returns an ogc:filter fragment that will fetch all WFS, WMS and WCS
     * records from a CSW.
     *
     * @return the filter string all records
     */
    @Override
    public String getFilterStringAllRecords() {
        // This is a bit of a hack - unfortunately the NamespaceContext class is
        // unsuitable here
        // as it contains no methods to iterate the contained list of
        // Namespaces
        HashMap<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("xmlns:ogc", "http://www.opengis.net/ogc");
        return this.generateFilter(this.generateFilterFragment(), namespaces);
    }

    /**
     * Not implemented.
     *
     * @param bbox the bbox
     * @return the filter string bounding box
     */
    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        throw new NotImplementedException();
    }

    /**
     * Gets the spatial bounds.
     *
     * @return the spatial bounds
     */
    public FilterBoundingBox getSpatialBounds() {
        return spatialBounds;
    }

    /**
     * Gets the keywords.
     *
     * @return the keywords
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * Gets the capture platform.
     *
     * @return the capture platform
     */
    public String getCapturePlatform() {
        return capturePlatform;
    }

    /**
     * Gets the sensor.
     *
     * @return the sensor
     */
    public String getSensor() {
        return sensor;
    }

    /**
     * Gets the keyword match type.
     *
     * @return the keyword match type
     */
    public KeywordMatchType getKeywordMatchType() {
        return keywordMatchType;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CSWGetDataRecordsFilter [spatialBounds=" + spatialBounds
                + ", keywords=" + Arrays.toString(keywords)
                + ", capturePlatform=" + capturePlatform + ", sensor=" + sensor
                + ", keywordMatchType=" + keywordMatchType + "]";
    }

    /** Gets the title. */
    public String getTitle() {
        return this.title;
    }
    
    /** Gets the abstract. */
    public String getAbstract() {
        return this.abstract_;
    }
    
    /** Gets the metadata change date's lower bound. */
    public DateTime getMetadataChangeDateFrom() {
        return this.metadataChangeDateFrom;
    }
    
    /** Gets the metadata change date's upper bound. */
    public DateTime getMetadataChangeDateTo() {
        return this.metadataChangeDateTo;
    }
    
    /** Gets the temporal extent's lower bound. */
    public DateTime getTemporalExtentFrom() {
        return this.temporalExtentFrom;
    }
    
    /** Gets the temporal extent's upper bound. */
    public DateTime getTemporalExtentTo() {
        return this.temporalExtentTo;
    }
    
    /** Sets the title. */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /** Sets the abstract. */
    public void setAbstract(String abstract_) {
        this.abstract_ = abstract_;
    }
    
    /** Sets the metadata change date's boundary. */
    public void setMetadataChangeDate(DateTime metadataChangeDateFrom, DateTime metadataChangeDateTo) {
        this.metadataChangeDateFrom = metadataChangeDateFrom;
        this.metadataChangeDateTo = metadataChangeDateTo;
    }
        
    /** Sets the temporal extent's boundary. */
    public void setTemporalExtent(DateTime temporalExtentFrom, DateTime temporalExtentTo) {
        this.temporalExtentFrom = temporalExtentFrom;
        this.temporalExtentTo = temporalExtentTo;
    }
}
