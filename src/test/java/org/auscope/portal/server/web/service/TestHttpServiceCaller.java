package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ConnectException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.auscope.portal.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for HttpServiceCaller
 * @author Josh Vote
 *
 */
public class TestHttpServiceCaller extends PortalTestClass{

    private HttpClient mockClient = context.mock(HttpClient.class);
    private HttpMethodBase mockMethod = context.mock(HttpMethodBase.class);
    private HttpConnectionManagerParams mockParams = context.mock(HttpConnectionManagerParams.class);
    private HttpServiceCaller serviceCaller = new HttpServiceCaller();

    @Before
    public void setup() {
        serviceCaller.setClientParams(mockParams);
    }

    /**
     * Sanity test for fetching stream
     */
    @Test
    public void testGetStream() throws Exception {
        final URI uri = new URI("http://service.test/wfs", true);
        final InputStream mockIs = context.mock(InputStream.class);

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(uri));
            oneOf(mockClient).setHttpConnectionManager(with(any(HttpConnectionManager.class)));
            oneOf(mockClient).executeMethod(mockMethod);will(returnValue(HttpStatus.SC_OK));
            oneOf(mockMethod).getResponseBodyAsStream();will(returnValue(mockIs));
        }});

        InputStream response = serviceCaller.getMethodResponseAsStream(mockMethod, mockClient);
        Assert.assertSame(mockIs, response);
    }

    /**
     * Sanity test for fetching stream and reading it into a string
     */
    @Test
    public void testGetString() throws Exception {
        final URI uri = new URI("http://service.test/wfs", true);
        final String responseString = "This is a response stream.";
        final InputStream responseStream = new ByteArrayInputStream(responseString.getBytes());

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(uri));
            oneOf(mockClient).setHttpConnectionManager(with(any(HttpConnectionManager.class)));
            oneOf(mockClient).executeMethod(mockMethod);will(returnValue(HttpStatus.SC_OK));
            oneOf(mockMethod).getResponseBodyAsStream();will(returnValue(responseStream));

            oneOf(mockMethod).releaseConnection(); //this must occur
        }});

        String actualResponse = serviceCaller.getMethodResponseAsString(mockMethod, mockClient);
        Assert.assertEquals(responseString, actualResponse);
    }

    /**
     * Sanity test for fetching stream and reading it into a string
     */
    @Test(expected=ConnectException.class)
    public void testGetConnectError() throws Exception {
        final URI uri = new URI("http://service.test/wfs", true);

        context.checking(new Expectations() {{
            allowing(mockMethod).getURI();will(returnValue(uri));
            allowing(mockMethod).getStatusLine();
            oneOf(mockClient).setHttpConnectionManager(with(any(HttpConnectionManager.class)));
            oneOf(mockClient).executeMethod(mockMethod);will(returnValue(HttpStatus.SC_SERVICE_UNAVAILABLE));
        }});

        serviceCaller.getMethodResponseAsString(mockMethod, mockClient);
    }
}
