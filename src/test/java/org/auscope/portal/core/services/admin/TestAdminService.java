package org.auscope.portal.core.services.admin;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.test.jmock.HttpMethodBaseMatcher.HttpMethodType;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for Admin Service
 * 
 * @author Josh Vote
 *
 */
public class TestAdminService extends PortalTestClass {
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private AdminService adminService = new AdminService(mockServiceCaller);

    /**
     * Tests that each URL is tested once
     * @throws IOException 
     */
    @Test
    public void testExternalConnectivity() throws IOException {
        final URL[] urls = new URL[] {
                new URL("http://example.fake/path"),
                new URL("https://example2.fake.secure/path"),
        };

        //Ensure all of our requests get called once
        context.checking(new Expectations() {
            {
                for (int i = 0; i < urls.length; i++) {
                    oneOf(mockServiceCaller).getMethodResponseAsString(
                            with(aHttpMethodBase(HttpMethodType.GET, urls[i].toString(), null)));
                    will(returnValue(""));
                }
            }
        });

        //Make our request - the service should create info about each URL queried
        AdminDiagnosticResponse response = adminService.externalConnectivity(urls);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.getDetails().size());
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    /**
     * Tests that each failing URL fails in a predictable way
     * @throws IOException 
     */
    @Test
    public void testExternalConnectivityErrors() throws IOException {
        final URL[] urls = new URL[] {
                new URL("http://example.fake/path"),
                new URL("https://example2.fake.secure/path"),
        };

        //Ensure all of our requests get called once and fail
        context.checking(new Expectations() {
            {
                for (int i = 0; i < urls.length; i++) {

                    oneOf(mockServiceCaller).getMethodResponseAsString(
                            with(aHttpMethodBase(HttpMethodType.GET, urls[i].toString(), null)));
                    will(throwException(new ConnectException()));
                }
            }
        });

        //Make our request - the service should create a warning about failing HTTPS and an error about HTTP
        AdminDiagnosticResponse response = adminService.externalConnectivity(urls);
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getDetails().size());
        Assert.assertEquals(1, response.getWarnings().size());
        Assert.assertEquals(1, response.getErrors().size());
    }

    /**
     * Tests CSW connectivity method for correct usage of the service caller and handling of error responses
     * @throws IOException 
     */
    @Test
    public void testCSWConnectivityErrors() throws IOException {
        final List<CSWServiceItem> items = Arrays.asList(
                new CSWServiceItem("id-1", "http://example.fake/thisWillWork"),
                new CSWServiceItem("id-2", "http://example2.fake/thisWillReturnInvalidCount"),
                new CSWServiceItem("id-3", "http://example3.fake/thieWillReturnOWSError"),
                new CSWServiceItem("id-4", "http://example4.fake/thisWillFailToConnect"));
        try (final HttpClientInputStream owsError = new HttpClientInputStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml"), null);
                final HttpClientInputStream cswBadCountResponse = new HttpClientInputStream(ResourceUtil
                        .loadResourceAsStream("org/auscope/portal/core/test/responses/csw/cswRecordResponse.xml"), null);
                final HttpClientInputStream cswResponse = new HttpClientInputStream(ResourceUtil
                        .loadResourceAsStream(
                                "org/auscope/portal/core/test/responses/csw/cswRecordResponse_SingleRecord.xml"), null)) {

            // We have 4 requests, 1 will fail, 1 will return error, 1 returns
            // an invalid count and 1 succeeds
            context.checking(new Expectations() {
                {
                    oneOf(mockServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, items.get(0).getServiceUrl(), null)));
                    will(returnValue(cswResponse));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, items.get(1).getServiceUrl(), null)));
                    will(returnValue(cswBadCountResponse));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, items.get(2).getServiceUrl(), null)));
                    will(returnValue(owsError));

                    oneOf(mockServiceCaller).getMethodResponseAsStream(
                            with(aHttpMethodBase(null, items.get(3).getServiceUrl(), null)));
                    will(throwException(new ConnectException()));
                }
            });

            AdminDiagnosticResponse response = adminService.cswConnectivity(items);
            Assert.assertNotNull(response);
            Assert.assertEquals(1, response.getDetails().size());
            Assert.assertEquals(1, response.getWarnings().size());
            Assert.assertEquals(2, response.getErrors().size());
        }
    }

    /**
     * Tests that WFS connectivity correctly calls services/handles responses
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Test
    public void testWFSConnectivity() throws IOException, URISyntaxException {
        final List<EndpointAndSelector> endpoints = Arrays.asList(
                new EndpointAndSelector("http://endpoint1.url/wfs", "wfs:type1"), //will fail to connect
                new EndpointAndSelector("http://endpoint1.url/wfs", "wfs:type2"), //will be skipped for sharing same endpoint as #1
                new EndpointAndSelector("http://endpoint2.url/wfs", "wfs:type1"), //will return OWS exception
                new EndpointAndSelector("http://endpoint2.url/wfs", "wfs:type2")); //will return success
        final FilterBoundingBox bbox = new FilterBoundingBox("srs", new double[] {1, 2}, new double[] {3, 4});

        context.checking(new Expectations() {
            {
                //This will fail to connect and cause the second request AND other endpoint to be skipped
                oneOf(mockServiceCaller).getMethodResponseAsStream(
                        with(aHttpMethodBase(null, Pattern.compile(endpoints.get(0).getEndpoint() + ".*"), null)));
                will(throwException(new ConnectException()));

                //Return OWS error
                oneOf(mockServiceCaller).getMethodResponseAsStream(
                        with(aHttpMethodBase(null, Pattern.compile(endpoints.get(2).getEndpoint() + ".*"), null)));
                will(returnValue(new HttpClientInputStream(ResourceUtil
                        .loadResourceAsStream("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml"), null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(
                        with(aHttpMethodBase(null, Pattern.compile(endpoints.get(2).getEndpoint() + ".*"), null)));
                will(returnValue(new HttpClientInputStream(ResourceUtil
                        .loadResourceAsStream("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml"), null)));

                //Return success
                oneOf(mockServiceCaller).getMethodResponseAsStream(
                        with(aHttpMethodBase(null, Pattern.compile(endpoints.get(3).getEndpoint() + ".*"), null)));
                will(returnValue(new HttpClientInputStream(ResourceUtil
                        .loadResourceAsStream("org/auscope/portal/core/test/responses/wfs/commodityGetFeatureResponse.xml"), null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(
                        with(aHttpMethodBase(null, Pattern.compile(endpoints.get(3).getEndpoint() + ".*"), null)));
                will(returnValue(new HttpClientInputStream(ResourceUtil
                        .loadResourceAsStream("org/auscope/portal/core/test/responses/wfs/commodityGetFeatureResponse.xml"), null)));
            }
        });

        AdminDiagnosticResponse response = adminService.wfsConnectivity(endpoints, bbox.toJsonCornersFormat());
        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getDetails().size()); //always 1 info block
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(6, response.getErrors().size());
    }
}
