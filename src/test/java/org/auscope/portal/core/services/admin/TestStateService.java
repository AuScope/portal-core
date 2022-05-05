package org.auscope.portal.core.services.admin;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for State Service
 *
 */
public class TestStateService extends PortalTestClass {

    /**
     * Tests saving and fetching to/from database
     * @throws Exception 
     */
    @Test
    public void testSaveLoad() throws Exception {
        final String id = "1";
        final String state = "ABCDEF";

        // Create service with in memory database
        StateService stateService = new StateService();

        // Save new state value
        Assert.assertTrue(stateService.save(id, state));

        // Retrieve it
        String retrievedState = stateService.fetch(id);

        // Check retrieved value
        Assert.assertEquals(retrievedState, state);
    }

    /**
     * Tests saving and fetching to/from database, with mistakes
     * @throws Exception 
     */
    @Test
    public void testSaveLoadMistake() throws Exception {
        final String id = "1";
        final String wrongId = "3";
        final String state = "ABCDEF";
        final String state2 = "ABCDEFG";

        // Create service with in memory database
        StateService stateService = new StateService();

        // Save
        Assert.assertTrue(stateService.save(id, state));

        // Test saving same value again
        Assert.assertTrue(stateService.save(id, state));

        // Test saving another value with same id
        Assert.assertFalse(stateService.save(id, state2));

        // Test saving null values
        Assert.assertFalse(stateService.save(id, null));
        Assert.assertFalse(stateService.save(null, null));
        Assert.assertFalse(stateService.save(null, state));

        // Test fetching null value
        Assert.assertEquals(stateService.fetch(null), "");

        // Request unknown id, check for empty value
        String errorState = stateService.fetch(wrongId);
        Assert.assertEquals(errorState, "");

    }


}
