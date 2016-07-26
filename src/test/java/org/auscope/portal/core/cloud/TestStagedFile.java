package org.auscope.portal.core.cloud;

import java.io.File;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for StagedFile
 * 
 * @author Joshua
 *
 */
public class TestStagedFile extends PortalTestClass {
    /**
     * Tests equals and hashcode align
     */
    @Test
    public void testEquality() {
        CloudJob c1 = new CloudJob(123);
        CloudJob c2 = new CloudJob(456);

        File mockFile = context.mock(File.class);

        StagedFile f1 = new StagedFile(c1, "f1", mockFile);
        StagedFile f2 = new StagedFile(c1, "f1", mockFile);
        StagedFile f3 = new StagedFile(c2, "f1", mockFile);
        StagedFile f4 = new StagedFile(c1, "f2", mockFile);

        Assert.assertTrue(equalsWithHashcode(f1, f1));
        Assert.assertTrue(equalsWithHashcode(f1, f2));
        Assert.assertFalse(f1.equals(f3));
        Assert.assertFalse(f1.equals(f4));
    }
}
