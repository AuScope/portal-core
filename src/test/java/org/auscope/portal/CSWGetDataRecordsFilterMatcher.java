package org.auscope.portal;

import java.util.Arrays;

import org.auscope.portal.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

/**
 * A JUnit matcher for matching instances of CSWGetDataRecordsFilter
 * @author Josh Vote
 *
 */
public class CSWGetDataRecordsFilterMatcher extends TypeSafeMatcher<CSWGetDataRecordsFilter> {

    private KeywordMatchType keywordMatchType;
    private FilterBoundingBox spatialBounds;
    private String[] keywords;
    private String capturePlatform;
    private String sensor;


    /**
     * Matches a CSWGetDataRecordsFilter based on one or more components
     * @param spatialBounds If not null, the comparison spatial bounds
     * @param keywords If not null, the comparison keyword list
     * @param capturePlatform If not null, the comparison capture platform
     * @param sensor If not null, the comparison sensor
     * @param keywordMatchType if not null, the comparison match type
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

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("a CSWGetDataRecordsFilter with spatialBounds='%1$s' keywords='%2$s' capturePlatform='%3$s sensor='%4$s' keywordMatchType='%5$s'", spatialBounds, Arrays.toString(keywords), capturePlatform, sensor, keywordMatchType));

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

        return matches;
    }

}
