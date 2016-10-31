package org.auscope.portal.core.util;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for TextUtil.
 * 
 * @author Richard Goh
 */
public class TestTextUtil extends PortalTestClass {
    /**
     * Tests that the get last N lines of multi-line text succeeds.
     */
    @Test
    public void testTail() {
        String txt = "This is line 1." + System.getProperty("line.separator");
        txt += "This is line 2." + System.getProperty("line.separator");
        txt += "This is line 3." + System.getProperty("line.separator");
        txt += "This is line 4." + System.getProperty("line.separator");
        txt += "This is line 5." + System.getProperty("line.separator");
        txt += "This is line 6." + System.getProperty("line.separator");
        txt += "This is line 7." + System.getProperty("line.separator");
        txt += "This is line 8." + System.getProperty("line.separator");

        String expected = "This is line 7." + System.getProperty("line.separator");
        expected += "This is line 8." + System.getProperty("line.separator");

        String actual = TextUtil.tail(txt, 2);
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests that the get last N lines of multi-line text succeeds when the text being parsed is empty or null.
     */
    @Test
    public void testTail_EmptyText() {
        String txt = null;
        String actual = TextUtil.tail(txt, 5);
        Assert.assertEquals("", actual);

        txt = "";
        actual = TextUtil.tail(txt, 5);
        Assert.assertEquals("", actual);
    }
}