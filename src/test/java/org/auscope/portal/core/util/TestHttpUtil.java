package org.auscope.portal.core.util;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for HttpUtil
 * @author Josh Vote (CSIRO)
 *
 */
public class TestHttpUtil extends PortalTestClass {
    /**
     * Ensure whitespace is encoded using %20 instead of '+'
     * @throws Exception
     */
    @Test
    public void testWhitespaceEscaping() throws Exception {
        final List<NameValuePair> params = Arrays.asList(new BasicNameValuePair("param1", "value with space"), new BasicNameValuePair("param2", "valuewithoutspace"));;
        final String hostUrl = "http://example.com/path";
        URI uri = HttpUtil.parseURI(hostUrl, params);
        Assert.assertEquals(hostUrl + "?param1=value%20with%20space&param2=valuewithoutspace", uri.toString());
    }
}
