package org.auscope.portal.core.util;

import java.net.URI;
import java.net.URISyntaxException;
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
     * @throws URISyntaxException 
     */
    @Test
    public void testWhitespaceEscaping() throws URISyntaxException  {
        final List<NameValuePair> params = Arrays.asList((NameValuePair) new BasicNameValuePair("param1", "value with space"), (NameValuePair) new BasicNameValuePair("param2", "valuewithoutspace"));
        final String hostUrl = "http://example.com/path";
        URI uri = HttpUtil.parseURI(hostUrl, params);
        Assert.assertEquals(hostUrl + "?param1=value%20with%20space&param2=valuewithoutspace", uri.toString());
    }
}
