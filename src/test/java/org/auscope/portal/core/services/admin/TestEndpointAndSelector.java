package org.auscope.portal.core.services.admin;

import java.util.Arrays;
import java.util.List;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for EndpointAndSelector
 * 
 * @author Josh Vote
 *
 */
public class TestEndpointAndSelector extends PortalTestClass {
    /**
     * Tests the equality operator works as expected
     */
    @Test
    public void testEquals() {
        //Our list of values to test (they should all be 'different'
        List<EndpointAndSelector> eas = Arrays.asList(
                new EndpointAndSelector("endpoint1", "selector1"), //our base comparison object
                null, //for null comparisons
                new EndpointAndSelector("endpoint1", "selector2"), //same endpoint, diff selector
                new EndpointAndSelector("endpoint3", "selector1")); //diff endpoint, same selector

        //Sanity test
        EndpointAndSelector notInList = new EndpointAndSelector("differentEndpoint", "differentSelector");
        Assert.assertFalse(eas.contains(notInList));

        //Test all equality permutations
        for (int i = 0; i < eas.size(); i++) {
            EndpointAndSelector lhs = eas.get(i);
            Assert.assertTrue(eas.contains(lhs));
            Assert.assertFalse(notInList.equals(lhs));

            for (int j = 0; j < eas.size(); j++) {
                EndpointAndSelector rhs = eas.get(j);
                Assert.assertTrue(eas.contains(rhs));

                if (i == j) {
                    Assert.assertEquals(lhs, rhs);
                } else {
                    if (lhs != null) {
                        Assert.assertFalse(lhs.equals(rhs));
                    }
                }
            }
        }
    }
}
