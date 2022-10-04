package org.auscope.portal.core.server.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.io.ByteArrayInputStream;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jmock.Expectations;

import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.auscope.portal.core.test.PortalTestClass;


/**
 * Test that HttpServiceCaller will redirect after receiving HTTP Moved & Redirect error codes
 * @throws IOException
 */
@RunWith(Parameterized.class)
public class TestHttpServiceCallerWithRedirects extends PortalTestClass {

    private WFSGetFeatureMethodMaker methodMaker;
    private HttpServiceCaller httpServiceCaller;
    private static final String SERVICE_URL = "http://localhost?";
    private static final String SERVICE_URL_HTTPS = "https://localhost?";
    private static final String FEATURE_TYPE = "gh:SomeType";
    private static final String FILTER_STRING = "<filter></filter>";
    private int redirectCode;

    @Before
    public void setUp() {

        httpServiceCaller = new HttpServiceCaller(9000);
        methodMaker = new WFSGetFeatureMethodMaker();
        methodMaker.setNamespaces(new ErmlNamespaceContext());
    }

    public TestHttpServiceCallerWithRedirects(int rCode) {
        this.redirectCode = rCode;
    }

    // Test is performed once for each HTTP error code
    @Parameterized.Parameters(name = "Testing HTTPServiceCaller redirect via HTTP code {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{HttpStatus.SC_MOVED_PERMANENTLY},
                                                {308},
                                                {HttpStatus.SC_MOVED_TEMPORARILY},
                                                {HttpStatus.SC_TEMPORARY_REDIRECT}});
    }
    @Test
    public void testHttpServiceCallerRequestWithRedirect() throws IOException {
        HttpPost method = (HttpPost) methodMaker.makePostMethod(SERVICE_URL, FEATURE_TYPE, FILTER_STRING, 0);
        String dummyJSONResponse = "<xml>This is a test xml response</xml>";
        final InputStream dummyJSONResponseIS = new ByteArrayInputStream(dummyJSONResponse.getBytes());
        final HttpClient client = context.mock(HttpClient.class);

        context.checking(new Expectations() {
            {
                // The first time execute() is called it will return 'redirectcode' error
                oneOf(client).execute(with(any(HttpRequestBase.class)));
                will(returnValue(new org.auscope.portal.core.server.http.download.MyHttpResponse(dummyJSONResponseIS, redirectCode, SERVICE_URL_HTTPS)));

                // The second time execute() is called it will return a normal 200 code
                oneOf(client).execute(with(any(HttpRequestBase.class)));
                will(returnValue(new org.auscope.portal.core.server.http.download.MyHttpResponse(dummyJSONResponseIS)));
            }
        });
        Assert.assertEquals(dummyJSONResponse, httpServiceCaller.getMethodResponseAsString(method, client));
    }
}
