package org.auscope.portal.server.web.view;

import java.util.List;

import org.junit.Assert;
import org.springframework.ui.ModelMap;

/**
 * Utility classes for testing views and modelmaps
 * @author vot002
 *
 */
public class AssertViewUtility {

	private static void assertEqual(Object expected, Object actual) {
    	if (expected instanceof ModelMap) {
			Assert.assertTrue(expected instanceof ModelMap);
			
			assertModelMapsEqual((ModelMap) expected, (ModelMap) actual);
		} else if (expected instanceof List) {
			Assert.assertTrue(actual instanceof List);
			
			assertListsEqual((List) expected, (List) actual);
		} else {
			Assert.assertEquals(expected, actual);
		}
    }
    
	/**
	 * Runs a number of assertions on the list (does a deep comparison)
	 * @param expected
	 * @param actual
	 */
	public static void assertListsEqual(List expected, List actual) {
    	Assert.assertEquals(expected.size(), actual.size());
    	
    	for (int i = 0; i < expected.size(); i++) {
    		assertEqual(expected.get(i), actual.get(i));
    	}
    }
    
	
	/**
	 * Runs a number of assertions on the specified model maps (does a deep comparison)
	 * @param expected
	 * @param actual
	 */
	public static void assertModelMapsEqual(ModelMap expected, ModelMap actual) {
    	
    	Assert.assertEquals(expected.size(), actual.size());
    	
    	for (String key : expected.keySet()) {
    		Object expectedChild = expected.get(key);
    		Object actualChild = actual.get(key);
    		
    		assertEqual(expectedChild, actualChild);
    	}
    }
}
