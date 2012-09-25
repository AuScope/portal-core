package org.auscope.portal.core.cloud;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for StagedFile
 * @author Joshua
 *
 */
public class TestStagedFile extends PortalTestClass {
	/**
	 * Tests equals and hashcode align
	 * @throws Exception
	 */
	@Test
	public void testEquality() throws Exception {
		CloudJob c1 = new CloudJob(123);
		CloudJob c2 = new CloudJob(456);
		
		StagedFile f1 = new StagedFile(c1, "f1");
		StagedFile f2 = new StagedFile(c1, "f1");
		StagedFile f3 = new StagedFile(c2, "f1");
		StagedFile f4 = new StagedFile(c1, "f2");
		
		Assert.assertTrue(equalsWithHashcode(f1, f1));
		Assert.assertTrue(equalsWithHashcode(f1, f2));
		Assert.assertFalse(f1.equals(f3));
		Assert.assertFalse(f1.equals(f4));
	}
}
