package org.auscope.portal.core.test.jmock;

import java.util.Arrays;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A JUnit matcher for matching instances of CSWGetDataRecordsFilter
 * 
 * @author Josh Vote
 *
 */
public class CSWGetDataRecordsFilterMatcher extends TypeSafeMatcher<CSWGetDataRecordsFilter> {

    private KeywordMatchType keywordMatchType;
    private FilterBoundingBox spatialBounds;
    private String[] keywords;
    private String capturePlatform;
    private String sensor;
    private String titleOrAbstract;
    private String authorSurname;
    private String publicationDateFrom;
    private String publicationDateTo;        

    /**
     * Matches a CSWGetDataRecordsFilter based on one or more components
     * 
     * @param spatialBounds
     *            If not null, the comparison spatial bounds
     * @param keywords
     *            If not null, the comparison keyword list
     * @param capturePlatform
     *            If not null, the comparison capture platform
     * @param sensor
     *            If not null, the comparison sensor
     * @param keywordMatchType
     *            if not null, the comparison match type
     */
    public CSWGetDataRecordsFilterMatcher(FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor,
            KeywordMatchType keywordMatchType) {
        this.spatialBounds = spatialBounds;
        this.keywords = keywords;
        this.capturePlatform = capturePlatform;
        this.sensor = sensor;
        this.keywordMatchType = keywordMatchType;
    }
    
    /**
     * Matches a CSWGetDataRecordsFilter based on one or more components
     * 
     * @param spatialBounds
     *            If not null, the comparison spatial bounds
     * @param keywords
     *            If not null, the comparison keyword list
     * @param capturePlatform
     *            If not null, the comparison capture platform
     * @param sensor
     *            If not null, the comparison sensor
     * @param keywordMatchType
     *            if not null, the comparison match type
     * @param titleOrAbstract
     *            if not null, the titleOrAbstract to search on
     * @param authorSurname
     *            if not null, the authorSurname to search on
     * @param publicationDateFrom
     *            if not null, the publicationDateFrom to search on
     * @param publicationDateTo
     *            if not null, the publicationDateTo to search on                                            
     */
    public CSWGetDataRecordsFilterMatcher(FilterBoundingBox spatialBounds,
            String[] keywords, String capturePlatform, String sensor,
            KeywordMatchType keywordMatchType, String titleOrAbstract, String authorSurname,
            String publicationDateFrom, String publicationDateTo) {
        this.spatialBounds = spatialBounds;
        this.keywords = keywords;
        this.capturePlatform = capturePlatform;
        this.sensor = sensor;
        this.keywordMatchType = keywordMatchType;
        this.titleOrAbstract = titleOrAbstract;
        this.authorSurname = authorSurname;
        this.publicationDateFrom = publicationDateFrom;
        this.publicationDateTo = publicationDateTo;
        
    }

    @Override
    public void describeTo(Description description) {
        description
                .appendText(String
                        .format("a CSWGetDataRecordsFilter with spatialBounds='%1$s' keywords='%2$s' capturePlatform='%3$s' sensor='%4$s' keywordMatchType='%5$s', titleOrAbstract='%6$s'",
                                spatialBounds, Arrays.toString(keywords), capturePlatform, sensor, keywordMatchType, titleOrAbstract, authorSurname, publicationDateFrom, publicationDateTo));

    }

    @Override
    public boolean matchesSafely(CSWGetDataRecordsFilter filter) {
        boolean matches = true;

        if (sensor != null) {
            matches &= sensor.equals(filter.getSensor());
        }

        if (capturePlatform != null) {
            matches &= capturePlatform.equals(filter.getCapturePlatform());
        }

        if (keywords != null) {
            matches &= Arrays.equals(keywords, filter.getKeywords());
        }

        if (spatialBounds != null) {
            matches &= spatialBounds.equals(filter.getSpatialBounds());
        }

        if (keywordMatchType != null) {
            matches &= keywordMatchType == filter.getKeywordMatchType();
        }
        
        if (titleOrAbstract != null) {
            matches &= titleOrAbstract.equals(filter.getTitleOrAbstract());
        }

        if (authorSurname != null) {
            matches &= authorSurname.equals(filter.getAuthorSurname());
        }
        
        if (publicationDateFrom != null) {
            matches &= publicationDateFrom.equals(filter.getPublicationDateFrom());
        }
        
        if (publicationDateTo != null) {
            matches &= publicationDateTo.equals(filter.getPublicationDateTo());
        }
        return matches;
    }

}
