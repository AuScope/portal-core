package org.auscope.portal.core.services.responses.wcs;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestTimeConstraints extends PortalTestClass {
    /**
     * Simple tests for testing equality
     */
    @Test
    public void testEquality() {
        TimeConstraint t1 = new TimeConstraint("constraint");
        TimeConstraint t2 = new TimeConstraint("constraint");
        TimeConstraint t3 = new TimeConstraint("0-42L");
        TimeConstraint t4 = new TimeConstraint("0-43-");

        Assert.assertTrue(equalsWithHashcode(t1, t1));
        Assert.assertTrue(equalsWithHashcode(t1, t2));
        Assert.assertTrue(equalsWithHashcode(t2, t1));

        Assert.assertFalse(t1.equals(t3));
        Assert.assertFalse(t1.equals(t4));
        Assert.assertFalse(t3.equals(t4));
    }
}
