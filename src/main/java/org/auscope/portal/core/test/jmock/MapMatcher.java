package org.auscope.portal.core.test.jmock;

import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for matching the values of a map based on EVERY value in the map being present in the match
 * 
 * @author Josh Vote
 *
 * @param <K>
 * @param <V>
 */
public class MapMatcher<K, V> extends TypeSafeMatcher<Map<K, V>> {

    Map<K, V> valuesToMatch;

    public MapMatcher(Map<K, V> valuesToMatch) {
        this.valuesToMatch = valuesToMatch;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("a Map with values='%1$s", valuesToMatch));
    }

    @Override
    public boolean matchesSafely(Map<K, V> map) {
        if (valuesToMatch == null || map == null) {
            return valuesToMatch == map;
        }

        if (valuesToMatch.size() != map.size()) {
            return false;
        }

        for (K key : valuesToMatch.keySet()) {
            if (!valuesToMatch.get(key).equals(map.get(key))) {
                return false;
            }
        }

        return true;
    }

}
