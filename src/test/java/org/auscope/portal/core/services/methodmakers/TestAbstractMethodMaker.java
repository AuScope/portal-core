package org.auscope.portal.core.services.methodmakers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestAbstractMethodMaker extends PortalTestClass {
    private class TestableAbstractMethodMaker extends AbstractMethodMaker {
        // empty
    }

    /**
     * Simple tests for a few edge cases
     */
    @Test
    public void testExtractQueryParams() {
        List<NameValuePair> empty = new ArrayList<>();
        TestableAbstractMethodMaker mm = new TestableAbstractMethodMaker();

        //Test no values
        List<NameValuePair> actual = mm.extractQueryParams("http://example.org");
        List<NameValuePair> expected = empty;
        Assert.assertEquals(expected, actual);

        //Test no values (with query char)
        actual = mm.extractQueryParams("http://example.org?");
        expected = empty;
        Assert.assertEquals(expected, actual);

        //Test no values (with query char + junk)
        actual = mm.extractQueryParams("http://example.org?&&");
        expected = empty;
        Assert.assertEquals(expected, actual);

        //Test single
        actual = mm.extractQueryParams("http://example.org?param1=test1");
        expected = new ArrayList<>();
        expected.add(new BasicNameValuePair("param1", "test1"));
        Assert.assertEquals(expected, actual);

        //Test single (with junk)
        actual = mm.extractQueryParams("http://example.org?&param1=test1&&");
        expected = new ArrayList<>();
        expected.add(new BasicNameValuePair("param1", "test1"));
        Assert.assertEquals(expected, actual);

        //Test many (with junk)
        actual = mm.extractQueryParams("http://example.org?&param1=test1&&param2=test2&");
        expected = new ArrayList<>();
        expected.add(new BasicNameValuePair("param1", "test1"));
        expected.add(new BasicNameValuePair("param2", "test2"));

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests the various edge cases with the URL concatenation
     */
    @Test
    public void testUrlPathConcat() {
        TestableAbstractMethodMaker mm = new TestableAbstractMethodMaker();

        Assert.assertEquals("http://example.org", mm.urlPathConcat("http://example.org"));
        Assert.assertEquals("http://example.org/", mm.urlPathConcat("http://example.org/"));

        Assert.assertEquals("http://example.org/test/path", mm.urlPathConcat("http://example.org", "test", "path"));
        Assert.assertEquals("http://example.org/test/path", mm.urlPathConcat("http://example.org/", "test", "/path"));
        Assert.assertEquals("http://example.org/test/path", mm.urlPathConcat("http://example.org/", "/test", "/path"));
        Assert.assertEquals("http://example.org/test/path", mm.urlPathConcat("http://example.org", "/test", "path"));
        Assert.assertEquals("http://example.org/test/path/",
                mm.urlPathConcat("http://example.org/", "/test/", "/path/"));

        Assert.assertEquals("http://example.org/test/path",
                mm.urlPathConcat("http://example.org", "", null, "/test/", "/path"));

        Assert.assertEquals("http://example.org/existing/path/value/test/",
                mm.urlPathConcat("http://example.org", "existing/path/value", "/test/"));
    }
}
