package org.auscope.portal.core.services.responses.wcs;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestResolution extends PortalTestClass {

    /**
     * Simple Equality tests
     */
    @Test
    public void testEquals() {
        Resolution r1 = new Resolution(1.1, 2.1);
        Resolution r2 = new Resolution(1.1, 2.1);
        Resolution r3 = new Resolution(2.1, 1.1);
        Resolution r4 = new Resolution(1.1, 1.1);
        Resolution r5 = new Resolution(2.1, 2.1);

        Assert.assertTrue(equalsWithHashcode(r1, r2));
        Assert.assertTrue(equalsWithHashcode(r2, r1));

        Assert.assertFalse(r1.equals(r3));
        Assert.assertFalse(r1.equals(r4));
        Assert.assertFalse(r1.equals(r5));

        Assert.assertFalse(r2.equals(r3));
        Assert.assertFalse(r2.equals(r4));
        Assert.assertFalse(r2.equals(r5));

        Assert.assertFalse(r3.equals(r4));
        Assert.assertFalse(r3.equals(r5));

        Assert.assertFalse(r4.equals(r5));
    }
}
