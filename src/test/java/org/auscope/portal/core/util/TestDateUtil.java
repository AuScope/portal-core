package org.auscope.portal.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for DateUtil.
 * 
 * @author Richard Goh
 */
public class TestDateUtil extends PortalTestClass {
    Date d1 = null;
    Date d2 = null;

    @Before
    public void setup() {
        Calendar cal1 = new GregorianCalendar(2013, 2, 5, 12, 00, 00);
        Calendar cal2 = new GregorianCalendar(2013, 2, 5, 12, 00, 45);
        d1 = cal1.getTime();
        d2 = cal2.getTime();
    }

    /**
     * Tests that the convert date to string method succeeds.
     */
    @Test
    public void testFormatDate() {
        Assert.assertEquals("Tue, 5 Mar 2013 12:00:00",
                DateUtil.formatDate(d1, "EEE, d MMM yyyy HH:mm:ss"));
    }

    /**
     * Tests that the get time difference between 2 dates without regard of their offsets succeeds.
     */
    @Test
    public void testGetTimeDifference() {
        long[] timeDiff = DateUtil.getTimeDifference(d1, d2);
        Assert.assertEquals(0, timeDiff[0]);
        Assert.assertEquals(0, timeDiff[1]);
        Assert.assertEquals(0, timeDiff[2]);
        Assert.assertEquals(45, timeDiff[3]);

        timeDiff = DateUtil.getTimeDifference(d2, d1);
        Assert.assertEquals(0, timeDiff[0]);
        Assert.assertEquals(0, timeDiff[1]);
        Assert.assertEquals(0, timeDiff[2]);
        Assert.assertEquals(45, timeDiff[3]);
    }

    /**
     * Tests that the overloaded get time difference between 2 dates succeeds.
     */
    @Test
    public void testGetTimeDifference_TimeField() {
        Assert.assertEquals(45,
                DateUtil.getTimeDifference(d1, d2, DateUtil.TimeField.SECOND));
        Assert.assertEquals(0,
                DateUtil.getTimeDifference(d1, d2, DateUtil.TimeField.MINUTE));
        Assert.assertEquals(0,
                DateUtil.getTimeDifference(d1, d2, DateUtil.TimeField.HOUR));
        Assert.assertEquals(0,
                DateUtil.getTimeDifference(d1, d2, DateUtil.TimeField.DAY));
    }
}