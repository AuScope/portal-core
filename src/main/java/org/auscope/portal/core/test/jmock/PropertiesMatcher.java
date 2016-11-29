package org.auscope.portal.core.test.jmock;

import java.util.Properties;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher for comparing the contents of 2 java.util.Properties objects
 * 
 * @author Josh Vote
 *
 */
public class PropertiesMatcher extends TypeSafeMatcher<Properties> {

    private Properties comparison;
    private boolean matchAll;

    /**
     * Create a new matcher
     * 
     * @param properties
     *            Match will succeed if the comparison properties has exactly the same set of properties
     */
    public PropertiesMatcher(Properties properties) {
        this(properties, true);
    }

    /**
     * Create a new matcher
     * 
     * @param properties
     *            Match will succeed if the comparison properties matches according to matchAll
     * @param matchAll
     *            if true, the comparison properties must be EXACTLY the same, otherwise it need only contain all of values in properties
     */
    public PropertiesMatcher(Properties properties, boolean matchAll) {
        this.comparison = properties;
        this.matchAll = matchAll;
    }

    @Override
    public void describeTo(Description description) {
        if (matchAll) {
            description.appendText(String.format("a Properties with '%1$s'", comparison));
        } 
    }

    @Override
    public boolean matchesSafely(Properties toMatch) {
        if (toMatch == null && comparison != null ||
                toMatch != null && comparison == null) {
            return false;
        }

        if (toMatch == null && comparison == null) {
            return true;
        }

        @SuppressWarnings("null")
        Set<String> toMatchNames = toMatch.stringPropertyNames();
        Set<String> comparisonNames = comparison.stringPropertyNames();
        if (matchAll && toMatchNames.size() != comparisonNames.size()) {
            return false;
        }

        if (matchAll && (!toMatchNames.containsAll(comparisonNames) || !comparisonNames.containsAll(toMatchNames))) {
            return false;
        }

        for (String property : comparisonNames) {
            if (!comparison.getProperty(property).equals(toMatch.getProperty(property))) {
                return false;
            }
        }

        return true;
    }

}
