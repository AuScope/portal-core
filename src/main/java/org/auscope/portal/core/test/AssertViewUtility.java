package org.auscope.portal.core.test;

import java.util.List;

import org.junit.Assert;
import org.springframework.ui.ModelMap;

/**
 * Utility classes for testing views and modelmaps
 *
 * @author vot002
 *
 */
public class AssertViewUtility {

    private static void assertEqual(String message, Object expected, Object actual) {
        if (expected instanceof ModelMap) {
            Assert.assertTrue(message, expected instanceof ModelMap);

            assertModelMapsEqual((ModelMap) expected, (ModelMap) actual);
        } else if (expected instanceof List) {
            Assert.assertTrue(message, actual instanceof List);

            assertListsEqual((List<?>) expected, (List<?>) actual);
        } else {
            Assert.assertEquals(message, expected, actual);
        }
    }

    /**
     * Runs a number of assertions on the list (does a deep comparison)
     *
     * @param expected
     * @param actual
     */
    public static void assertListsEqual(List<?> expected, List<?> actual) {
        Assert.assertEquals(String.format("Sizes vary. Expected %1$s, got %2$s", expected, actual), expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEqual(String.format("Mismatch at index: %1$s. Expected %2$s but got %3$s", i, expected.get(i), actual.get(i)), expected.get(i), actual.get(i));
        }
    }

    /**
     * Runs a number of assertions on the specified model maps (does a deep comparison)
     *
     * @param expected
     * @param actual
     */
    public static void assertModelMapsEqual(ModelMap expected, ModelMap actual) {
        Assert.assertEquals(String.format("Sizes vary. Expected %1$s, got %2$s", expected.keySet(), actual.keySet()), expected.size(), actual.size());

        for (String key : expected.keySet()) {
            Object expectedChild = expected.get(key);
            Object actualChild = actual.get(key);

            assertEqual(String.format("Mismatch at key: %1$s. Expected %2$s but got %3$s", key, expectedChild, actualChild), expectedChild, actualChild);
        }
    }
}
