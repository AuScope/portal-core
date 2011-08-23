package org.auscope.portal.csw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.auscope.portal.server.domain.filter.AbstractFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;

/**
 * Represents a OGC:Filter that will fetch matching records from a CS/W.
 *
 * @author Josh Vote
 *
 */
public class CSWGetDataRecordsFilter extends AbstractFilter {

    /**
     * How the list of keywords will be used to match records
     */
    public enum KeywordMatchType {
        /**
         * Any record that matches ANY of the specified keywords will pass
         */
        Any,
        /**
         * Any record that matches EACH AND EVERY keyword in the specified list will pass
         */
        All
    }

    private String anyText;
    private FilterBoundingBox spatialBounds;
    private String[] keywords;
    private String capturePlatform;
    private String sensor;
    private KeywordMatchType keywordMatchType;

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
     * Utility method for generating the body of a filter fragment
     * @return
     */
    private String generateFilterFragment() {
        List<String> fragments = new ArrayList<String>();

        if (anyText != null && !anyText.isEmpty()) {
            fragments.add(this.generatePropertyIsLikeFragment("anytext", this.anyText));
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

        return this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()]));
    }

    /**
     * Returns an ogc:filter fragment that will fetch all WFS, WMS and WCS
     * records from a CSW
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
     * Not implemented
     */
    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        throw new NotImplementedException();
    }

    public FilterBoundingBox getSpatialBounds() {
        return spatialBounds;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String getCapturePlatform() {
        return capturePlatform;
    }

    public String getSensor() {
        return sensor;
    }

    public KeywordMatchType getKeywordMatchType() {
        return keywordMatchType;
    }

    @Override
    public String toString() {
        return "CSWGetDataRecordsFilter [spatialBounds=" + spatialBounds
                + ", keywords=" + Arrays.toString(keywords)
                + ", capturePlatform=" + capturePlatform + ", sensor=" + sensor
                + ", keywordMatchType=" + keywordMatchType + "]";
    }


}
