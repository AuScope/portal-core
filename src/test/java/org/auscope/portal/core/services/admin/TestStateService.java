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
        StateService stateService = new StateService(1000);

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
        StateService stateService = new StateService(1000);

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

    @Test 
    public void testDatabaseLimit() throws Exception {
        final String id1 = "1";
        final String id2 = "2";
        final String id3 = "3";
        final String id4 = "4";
        final String id5 = "5";

        final String state1 = "ABCDEF1";
        final String state2 = "ABCDEF2";
        final String state3 = "ABCDEF3";
        final String state4 = "ABCDEF4";
        final String state5 = "ABCDEF5";

        // Create service with in memory database, but with a four state limit 
        StateService stateService = new StateService(4);

        // Save 4 new state values
        Assert.assertTrue(stateService.save(id1, state1));
        Assert.assertTrue(stateService.save(id2, state2));
        Assert.assertTrue(stateService.save(id3, state3));
        Assert.assertTrue(stateService.save(id4, state4));

        // Test them
        Assert.assertEquals(stateService.fetch(id1), state1);
        Assert.assertEquals(stateService.fetch(id2), state2);
        Assert.assertEquals(stateService.fetch(id3), state3);
        Assert.assertEquals(stateService.fetch(id4), state4);

        // Add a 5th state
        Assert.assertTrue(stateService.save(id5, state5));

        // Test it 
        Assert.assertEquals(stateService.fetch(id5), state5);

        // Test the first 4, except first one should be missing because of the limit
        Assert.assertEquals(stateService.fetch(id1), "");
        Assert.assertEquals(stateService.fetch(id2), state2);
        Assert.assertEquals(stateService.fetch(id3), state3);
        Assert.assertEquals(stateService.fetch(id4), state4);
    }

}
