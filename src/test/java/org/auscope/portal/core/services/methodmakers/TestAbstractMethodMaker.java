package org.auscope.portal.core.services.methodmakers;

import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestAbstractMethodMaker extends PortalTestClass {
    private class TestableAbstractMethodMaker extends AbstractMethodMaker {

    }

    private NameValuePair[] toArray(List<NameValuePair> l) {
        Assert.assertNotNull(l);
        return l.toArray(new NameValuePair[l.size()]);
    }

    /**
     * Simple tests for a few edge cases
     */
    @Test
    public void testExtractQueryParams() {
        NameValuePair[] empty = new NameValuePair[0];
        TestableAbstractMethodMaker mm = new TestableAbstractMethodMaker();

        //Test no values
        NameValuePair[] actual = toArray(mm.extractQueryParams("http://example.org"));
        NameValuePair[] expected = empty;
        Assert.assertArrayEquals(expected, actual);

        //Test no values (with query char)
        actual = toArray(mm.extractQueryParams("http://example.org?"));
        expected = empty;
        Assert.assertArrayEquals(expected, actual);

        //Test no values (with query char + junk)
        actual = toArray(mm.extractQueryParams("http://example.org?&&"));
        expected = empty;
        Assert.assertArrayEquals(expected, actual);

        //Test single
        actual = toArray(mm.extractQueryParams("http://example.org?param1=test1"));
        expected = new NameValuePair[] {new NameValuePair("param1", "test1")};
        Assert.assertArrayEquals(expected, actual);

        //Test single (with junk)
        actual = toArray(mm.extractQueryParams("http://example.org?&param1=test1&&"));
        expected = new NameValuePair[] {new NameValuePair("param1", "test1")};
        Assert.assertArrayEquals(expected, actual);

        //Test many (with junk)
        actual = toArray(mm.extractQueryParams("http://example.org?&param1=test1&&param2=test2&"));
        expected = new NameValuePair[] {new NameValuePair("param1", "test1"), new NameValuePair("param2", "test2")};
        Assert.assertArrayEquals(expected, actual);
    }
}
