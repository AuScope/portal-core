package org.auscope.portal.core.services.methodmakers.filter.csw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.methodmakers.filter.AbstractFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.joda.time.DateTime;

/**
 * Represents a OGC:Filter that will fetch matching records from a CSW.
 *
 * @author Josh Vote
 *
 */
public class CSWGetDataRecordsFilter extends AbstractFilter {
    private final Log log = LogFactory.getLog(getClass());

    /**
     * How the list of keywords will be used to match records.
     */
    public enum KeywordMatchType {

        /** Any record that matches ANY of the specified keywords will pass. */
        Any,

        /** Any record that matches EACH AND EVERY keyword in the specified list will pass. */
        All
    }

    public enum Type {

        /** Any record that matches ANY of the specified keywords will pass. */
        service,

        /** Any record that matches EACH AND EVERY keyword in the specified list will pass. */
        dataset,

        all
    }

    /**
     * The different ways of sorting the returned CSW records
     */
    public enum SortType {
        /**
         * Use the service's default ordering
         */
        serviceDefault,

        /**
         * Sort by title, ascending alphabetically
         */
        title,

        /**
         * Sort by publication date, descending (newest first)
         */
        publicationDate;

        /**
         * gets a SortType enum based upon the string that is passed in. If the
         * string is not valid then return a default.
         *
         * @param value
         *            the value to test
         * @return a SortType enum if the value was okay
         */
        public static SortType getByStringValue(String value) {
            SortType sortType = serviceDefault;

            if (value != null) {
                if ("title".equals(value)) {
                    sortType = title;
                } else if ("publicationDate".equals(value)) {
                    sortType = publicationDate;
                }
            }
            return sortType;
        }
    }

    /** The any text. */
    private String anyText;

    /** The title. */
    private String title = null;
    
    /** File Identifier */
    private String fileIdentifier = null;

    /** The abstract. */
    private String abstract_ = null;

    /** A field for title OR abstract, if we want to search on either */
    private String titleOrAbstract = null;

    /** The Author's surname */
    private String authorSurname = null;
    
    /** The type of online resource - WFS or WMS */
    private String onlineResourceType = null;

    /** Date on which the record was created or updated within the catalogue. */
    private DateTime modifiedDateFrom = null;

    /** Date on which the record was created or updated within the catalogue. */
    private DateTime modifiedDateTo = null;

    /** The publication date's lower bound. */
    private DateTime publicationDateFrom = null;

    /** The publication date's upper bound. */
    private DateTime publicationDateTo = null;

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

    private String alternateIdentifier;
    
    /** The keyword match type. */
    private KeywordMatchType keywordMatchType;

    private Type type;

    private SortType sortType;

    private String basicSearchTerm;
    
    /**
     * Default constructor for creating a filter in a factory method manner.
     * Create an empty filter and set the fields manually.
     */
    public CSWGetDataRecordsFilter() {
    }

    /**
     * Generates a new filter generator for the specified fields.
     *
     * @param anyText
     *            [Optional] The text used to query the 'AnyText' attribute
     * @param spatialBounds
     *            [Optional] The spatial bounds to filter by
     */
    public CSWGetDataRecordsFilter(String anyText, FilterBoundingBox spatialBounds) {
        this(anyText, spatialBounds, null, null, null, null, null, null, null);
    }

    /**
     * Generates a new filter generator for the specified fields.
     *
     * @param anyText
     *            [Optional] The text used to query the 'AnyText' attribute
     * @param spatialBounds
     *            [Optional] The spatial bounds to filter by
     * @param keywords
     *            [Optional] A list of keywords which must ALL be satisfied for a record to be included
     * @param capturePlatform
     *            [Optional] A capture platform filter that must be specified for a record to be included
     * @param sensor
     *            [Optional] A sensor filter that must be specified for a record to be included
     */
    public CSWGetDataRecordsFilter(String anyText, FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor) {
        this(anyText, spatialBounds, keywords, capturePlatform, sensor, null, null, null, null);
    }

    /**
     * Generates a new filter generator for the specified fields.
     *
     * @param anyText
     *            [Optional] The text used to query the 'AnyText' attribute
     * @param spatialBounds
     *            [Optional] The spatial bounds to filter by
     * @param keywords
     *            [Optional] A list of keywords which must ALL be satisfied for a record to be included
     * @param capturePlatform
     *            [Optional] A capture platform filter that must be specified for a record to be included
     * @param sensor
     *            [Optional] A sensor filter that must be specified for a record to be included
     * @param keywordMatchType
     *            [Optional] How the list of keywords will be matched (defaults to All)
     */
    public CSWGetDataRecordsFilter(String anyText, FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor,
            KeywordMatchType keywordMatchType, String dataIdentificationAbstract, String title, Type type) {
        this.anyText = anyText;
        this.spatialBounds = spatialBounds;
        this.keywords = keywords;
        this.capturePlatform = capturePlatform;
        this.sensor = sensor;
        this.keywordMatchType = keywordMatchType;
        this.abstract_ = dataIdentificationAbstract;
        this.title = title;
        this.type = type;
    }

    /**
     * Utility method for generating the body of a filter fragment.
     *
     * @return a filter fragment
     */
    private String generateFilterFragment() {
        List<String> fragments = new ArrayList<>();

        // if it is a basic search we'll use the basicSearchTerm field on some specific fields
        if (basicSearchTerm != null && !basicSearchTerm.isEmpty()) {
            fragments.add(generateOrComparisonFragment(
                    this.generatePropertyIsLikeFragment("title", "*" + this.basicSearchTerm + "*"),
                    this.generatePropertyIsLikeFragment("abstract", "*" + this.basicSearchTerm + "*"),
                    this.generatePropertyIsLikeFragment("keywords", "*" + this.basicSearchTerm + "*"),
                    this.generatePropertyIsLikeFragment("orgName", "*" + this.basicSearchTerm + "*"))
                    );
        }

        // advanced search
        else {

            if (anyText != null && !anyText.isEmpty()) {
                fragments.add(this.generatePropertyIsLikeFragment("anytext", this.anyText));
            }

            if (title != null && !title.isEmpty()) {
                fragments.add(this.generatePropertyIsLikeFragment("title", this.title));
            }
            
            if (fileIdentifier != null && !fileIdentifier.isEmpty()) {
                fragments.add(this.generatePropertyIsLikeFragment("identifier", this.fileIdentifier));
            }

            if (titleOrAbstract != null && !titleOrAbstract.isEmpty()) {
                fragments.add(generateOrComparisonFragment(
                        this.generatePropertyIsLikeFragment("title", "*" + this.titleOrAbstract + "*"),
                        this.generatePropertyIsLikeFragment("abstract", "*" + this.titleOrAbstract + "*")));
            }

            if (authorSurname != null && !authorSurname.isEmpty()) {
                fragments.add(generateAndComparisonFragment(
                        this.generatePropertyIsLikeFragment("authorSurname", "*" + authorSurname + "*")));
            }
            
            if (onlineResourceType != null && !onlineResourceType.isEmpty()) {
                fragments.add(generateAndComparisonFragment(
                        this.generatePropertyIsLikeFragment("onlineResourceType", "OGC:" + onlineResourceType + "*")));
            }

            if (publicationDateFrom != null) {
                fragments.add(
                        this.generatePropertyIsGreaterThanOrEqualTo("publicationDate", publicationDateFrom.toString()));
            }

            if (publicationDateTo != null) {
                fragments
                .add(this.generatePropertyIsLessThanOrEqualTo("publicationDate", publicationDateTo.toString()));
            }

            if (type != null && type != Type.all) {
                if (type == Type.dataset) {
                    fragments.add(this.generatePropertyIsEqualToFragment("type", "dataset"));
                } else {
                    fragments.add(this.generatePropertyIsLikeFragment("type", "service"));
                }
            }

            if (abstract_ != null && !abstract_.isEmpty()) {
                fragments.add(this.generatePropertyIsLikeFragment("abstract", this.abstract_));
            }

            if (spatialBounds != null) {
                fragments.add(this.generateBboxFragment(spatialBounds,
                        "ows:BoundingBox"));
            }

            if (keywords != null && keywords.length > 0) {
                List<String> keywordFragments = new ArrayList<>();
                for (String keyword : keywords) {
                    if (keyword != null && !keyword.isEmpty()) {
                        // keywordFragments.add(this.generatePropertyIsEqualToFragment("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString",
                        // keyword));
                        keywordFragments.add(this.generatePropertyIsLikeFragment("keyword", "*" + keyword + "*"));
                    }
                }

                if (keywordMatchType == null || keywordMatchType == KeywordMatchType.All) {
                    fragments.add(this.generateAndComparisonFragment(
                            keywordFragments.toArray(new String[keywordFragments.size()])));
                } else {
                    fragments.add(this.generateOrComparisonFragment(
                            keywordFragments.toArray(new String[keywordFragments.size()])));
                }
            }

            if (capturePlatform != null && !capturePlatform.isEmpty()) {
                fragments.add(this.generatePropertyIsEqualToFragment("capturePlatform", capturePlatform));
            }

            if (sensor != null && !sensor.isEmpty()) {
                fragments.add(this.generatePropertyIsEqualToFragment("sensor", sensor));
            }
            
            if (alternateIdentifier != null && !alternateIdentifier.isEmpty()) {
                fragments.add(this.generatePropertyIsEqualToFragment("AlternateIdentifier", alternateIdentifier));
            }

            if (temporalExtentFrom != null) {
                fragments.add(
                        this.generatePropertyIsGreaterThanOrEqualTo("TempExtent_begin", temporalExtentFrom.toString()));
            }

            if (temporalExtentTo != null) {
                fragments.add(this.generatePropertyIsLessThanOrEqualTo("TempExtent_end", temporalExtentTo.toString()));
            }

            if (metadataChangeDateFrom != null) {
                fragments.add(
                        this.generatePropertyIsGreaterThanOrEqualTo("changeDate", metadataChangeDateFrom.toString()));
            }

            if (metadataChangeDateTo != null) {
                fragments.add(this.generatePropertyIsLessThanOrEqualTo("changeDate", metadataChangeDateTo.toString()));
            }

            if (modifiedDateFrom != null) {
                fragments.add(this.generatePropertyIsGreaterThanOrEqualTo("modified", modifiedDateFrom.toString()));
            }

            if (modifiedDateTo != null) {
                fragments.add(this.generatePropertyIsLessThanOrEqualTo("modified", modifiedDateTo.toString()));
            }
        }

        String fragment = this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()]));

        log.trace(fragment);

        return fragment;
    }

    /**
     * Returns an ogc:filter fragment that will fetch all WFS, WMS and WCS records from a CSW.
     *
     * @return the filter string all records
     */
    @Override
    public String getFilterStringAllRecords() {
        // This is a bit of a hack - unfortunately the NamespaceContext class is
        // unsuitable here
        // as it contains no methods to iterate the contained list of
        // Namespaces
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("xmlns:ogc", "http://www.opengis.net/ogc");
        return this.generateFilter(this.generateFilterFragment(), namespaces);
    }

    /**
     * Not implemented.
     *
     * @param bbox
     *            the bbox
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
                + ", capturePlatform=" + capturePlatform
                + ", sensor=" + sensor
                + ", metadataChangeDateFrom=" + metadataChangeDateFrom
                + ", metadataChangeDateTo=" + metadataChangeDateTo
                + ", temporalExtentFrom=" + temporalExtentFrom
                + ", temporalExtentTo=" + temporalExtentTo
                + ", keywordMatchType=" + keywordMatchType
                + ", titleOrAbstract=" + titleOrAbstract
                + ", authorSurname=" + authorSurname
                + ", publicationDateFrom=" + publicationDateFrom
                + ", publicationDateTo=" + publicationDateTo + "]";

    }

    /** Gets the title. */
    public String getTitle() {
        return this.title;
    }
    
    /** Gets the file identifier. */
    public String getFileIdentifier() {
        return this.fileIdentifier;
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
    
    /** Sets the file identifier. */
    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    /** Sets the abstract. */
    public void setAbstract(String abstract_) {
        this.abstract_ = abstract_;
    }

    /**
     * @return the titleOrAbstract
     */
    public String getTitleOrAbstract() {
        return titleOrAbstract;
    }

    /**
     * @param titleOrAbstract the titleOrAbstract to set
     */
    public void setTitleOrAbstract(String titleOrAbstract) {
        this.titleOrAbstract = titleOrAbstract;
    }

    /**
     * Date on which the record was created or updated within the catalogue.
     * @return
     */
    public DateTime getModifiedDateFrom() {
        return modifiedDateFrom;
    }

    /**
     * Date on which the record was created or updated within the catalogue.
     * @return
     */
    public void setModifiedDateFrom(DateTime modifiedDateFrom) {
        this.modifiedDateFrom = modifiedDateFrom;
    }

    /**
     * Date on which the record was created or updated within the catalogue.
     * @return
     */
    public DateTime getModifiedDateTo() {
        return modifiedDateTo;
    }

    /**
     * Date on which the record was created or updated within the catalogue.
     * @return
     */
    public void setModifiedDateTo(DateTime modifiedDateTo) {
        this.modifiedDateTo = modifiedDateTo;
    }

    /**
     * @return the authorSurname
     */
    public String getAuthorSurname() {
        return authorSurname;
    }

    /**
     * @param authorSurname the authorSurname to set
     */
    public void setAuthorSurname(String authorSurname) {
        this.authorSurname = authorSurname;
    }

    /**
     * @return the publicationDateFrom
     */
    public DateTime getPublicationDateFrom() {
        return publicationDateFrom;
    }

    /**
     * @param publicationDateFrom the publicationDateFrom to set
     */
    public void setPublicationDateFrom(DateTime publicationDateFrom) {
        this.publicationDateFrom = publicationDateFrom;
    }

    /**
     * @return the publicationDateTo
     */
    public DateTime getPublicationDateTo() {
        return publicationDateTo;
    }

    /**
     * @param publicationDateTo the publicationDateTo to set
     */
    public void setPublicationDateTo(DateTime publicationDateTo) {
        this.publicationDateTo = publicationDateTo;
    }

    /** Sets metadataChangeDateFrom. */
    public void setMetadataChangeDateFrom(DateTime metadataChangeDateFrom) {
        this.metadataChangeDateFrom = metadataChangeDateFrom;
    }

    /** Sets metadataChangeDateTo. */
    public void setMetadataChangeDateTo(DateTime metadataChangeDateTo) {
        this.metadataChangeDateTo = metadataChangeDateTo;
    }

    /** Sets temporalExtentFrom. */
    public void setTemporalExtentFrom(DateTime temporalExtentFrom) {
        this.temporalExtentFrom = temporalExtentFrom;
    }

    /** Sets temporalExtentTo. */
    public void setTemporalExtentTo(DateTime temporalExtentTo) {
        this.temporalExtentTo = temporalExtentTo;
    }

    /**
     * @param anyText the anyText to set
     */
    public void setAnyText(String anyText) {
        this.anyText = anyText;
    }

    /**
     * @param abstract_ the abstract_ to set
     */
    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
    }

    /**
     * @param spatialBounds the spatialBounds to set
     */
    public void setSpatialBounds(FilterBoundingBox spatialBounds) {
        this.spatialBounds = spatialBounds;
    }

    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    /**
     * @param capturePlatform the capturePlatform to set
     */
    public void setCapturePlatform(String capturePlatform) {
        this.capturePlatform = capturePlatform;
    }

    /**
     * @param sensor the sensor to set
     */
    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    /**
     * @param keywordMatchType the keywordMatchType to set
     */
    public void setKeywordMatchType(KeywordMatchType keywordMatchType) {
        this.keywordMatchType = keywordMatchType;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    public SortType getSortType() {
        return sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    public String getBasicSearchTerm() {
        return basicSearchTerm;
    }

    public void setBasicSearchTerm(String basicSearchTerm) {
        this.basicSearchTerm = basicSearchTerm;
    }

    public String getAlternateIdentifier() {
        return alternateIdentifier;
    }

    public void setAlternateIdentifier(String alternateIdentifier) {
        this.alternateIdentifier = alternateIdentifier;
    }
    
    public String getOnlineResourceType() {
        return onlineResourceType;
    }
    
    public void setOnlineResourceType(String onlineResourceType) {
        this.onlineResourceType = onlineResourceType;
    }
}
