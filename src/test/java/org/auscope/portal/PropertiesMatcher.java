package org.auscope.portal;

import java.util.Properties;
import java.util.Set;

import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

/**
 * A matcher for comparing the contents of 2 java.util.Properties objects
 * @author Josh Vote
 *
 */
public class PropertiesMatcher extends TypeSafeMatcher<Properties> {

    private Properties comparison;

    /**
     * Create a new matcher
     * @param properties Match will succeed if the comparison properties has exactly the same set of properties
     */
    public PropertiesMatcher(Properties properties) {
        this.comparison = properties;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("a Properties with '%1$s'", comparison));

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

        Set<String> toMatchNames = toMatch.stringPropertyNames();
        Set<String> comparisonNames = comparison.stringPropertyNames();
        if (toMatchNames.size() != comparisonNames.size()) {
            return false;
        }

        if (!toMatchNames.containsAll(comparisonNames) || !comparisonNames.containsAll(toMatchNames)) {
            return false;
        }

        for (String property : toMatchNames) {
            if (!toMatch.getProperty(property).equals(comparison.getProperty(property))) {
                return false;
            }
        }

        return true;
    }

}
