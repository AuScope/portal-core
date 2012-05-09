package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.auscope.portal.HttpMethodBaseMatcher;
import org.auscope.portal.HttpMethodBaseMatcher.HttpMethodType;
import org.auscope.portal.PortalTestClass;
import org.auscope.portal.Util;
import org.auscope.portal.server.domain.admin.AdminDiagnosticResponse;
import org.auscope.portal.server.domain.admin.EndpointAndSelector;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.web.SISSVocMethodMaker;
import org.auscope.portal.server.web.controllers.VocabController;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for Admin Service
 * @author Josh Vote
 *
 */
public class TestAdminService extends PortalTestClass {
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private HttpClient mockClient = context.mock(HttpClient.class);
    private AdminService adminService = new AdminService(mockServiceCaller);

    /**
     * Tests that each URL is tested once
     * @throws Exception
     */
    @Test
    public void testExternalConnectivity() throws Exception {
        final URL[] urls = new URL[] {
            new URL("http://example.fake/path"),
            new URL("https://example2.fake.secure/path"),
        };

        //Ensure all of our requests get called once
        context.checking(new Expectations() {{
            for (int i = 0; i < urls.length; i++) {
                oneOf(mockServiceCaller).getHttpClient();
                will(returnValue(mockClient));

                oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(HttpMethodType.GET, urls[i].toString(), null)), with(mockClient));
                will(returnValue(""));
            }
        }});

        //Make our request - the service should create info about each URL queried
        AdminDiagnosticResponse response = adminService.externalConnectivity(urls);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.getDetails().size());
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    /**
     * Tests that each failing URL fails in a predictable way
     * @throws Exception
     */
    @Test
    public void testExternalConnectivityErrors() throws Exception {
        final URL[] urls = new URL[] {
            new URL("http://example.fake/path"),
            new URL("https://example2.fake.secure/path"),
        };

        //Ensure all of our requests get called once and fail
        context.checking(new Expectations() {{
            for (int i = 0; i < urls.length; i++) {
                oneOf(mockServiceCaller).getHttpClient();
                will(returnValue(mockClient));

                oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(HttpMethodType.GET, urls[i].toString(), null)), with(mockClient));
                will(throwException(new ConnectException()));
            }
        }});

        //Make our request - the service should create a warning about failing HTTPS and an error about HTTP
        AdminDiagnosticResponse response = adminService.externalConnectivity(urls);
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getDetails().size());
        Assert.assertEquals(1, response.getWarnings().size());
        Assert.assertEquals(1, response.getErrors().size());
    }

    /**
     * Tests CSW connectivity method for correct usage of the service caller
     * and handling of error responses
     * @throws Exception
     */
    @Test
    public void testCSWConnectivityErrors() throws Exception {
        final List<CSWServiceItem> items = Arrays.asList(
            new CSWServiceItem("id-1", "http://example.fake/thisWillWork"),
            new CSWServiceItem("id-2", "http://example2.fake/thisWillReturnInvalidCount"),
            new CSWServiceItem("id-3", "http://example3.fake/thieWillReturnOWSError"),
            new CSWServiceItem("id-4", "http://example4.fake/thisWillFailToConnect"));
        final InputStream owsError = getClass().getResourceAsStream("/OWSExceptionSample1.xml");
        final InputStream cswBadCountResponse = getClass().getResourceAsStream("/cswRecordResponse.xml");
        final InputStream cswResponse = getClass().getResourceAsStream("/cswRecordResponse_SingleRecord.xml");

        //We have 4 requests, 1 will fail, 1 will return error, 1 returns an invalid count and 1 succeeds
        context.checking(new Expectations() {{
            exactly(items.size()).of(mockServiceCaller).getHttpClient();
            will(returnValue(mockClient));

            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, items.get(0).getServiceUrl(), null)), with(mockClient));
            will(returnValue(cswResponse));

            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, items.get(1).getServiceUrl(), null)), with(mockClient));
            will(returnValue(cswBadCountResponse));

            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, items.get(2).getServiceUrl(), null)), with(mockClient));
            will(returnValue(owsError));

            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, items.get(3).getServiceUrl(), null)), with(mockClient));
            will(throwException(new ConnectException()));
        }});

        AdminDiagnosticResponse response = adminService.cswConnectivity(items);
        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getDetails().size());
        Assert.assertEquals(1, response.getWarnings().size());
        Assert.assertEquals(2, response.getErrors().size());
    }

    /**
     * Tests that the vocab diagnostics correctly handle responses
     * @throws Exception
     */
    @Test
    public void testVocab() throws Exception {
        final String vocabUrl = "http://fake.vocab/url";
        final String vocabResponse = Util.loadXML("src/test/resources/SISSVocResponse.xml");
        final String repoInfoResponse = Util.loadXML("src/test/resources/SISSVocRepositoryInfoResponse.xml");
        final SISSVocMethodMaker methodMaker = new SISSVocMethodMaker();


        //Our vocab test fires off 2 requests
        context.checking(new Expectations() {{
            exactly(2).of(mockServiceCaller).getHttpClient();
            will(returnValue(mockClient));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getRepositoryInfoMethod(vocabUrl).getURI().toString(), null)), with(mockClient));
            will(returnValue(repoInfoResponse));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getConceptByLabelMethod(vocabUrl, VocabController.COMMODITY_REPOSITORY, "*").getURI().toString(), null)), with(mockClient));
            will(returnValue(vocabResponse));
        }});

        //Our only restriction is asserting no errors
        AdminDiagnosticResponse response = adminService.vocabConnectivity(vocabUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    /**
     * Tests that the vocab diagnostics correctly handle responses with malformed XML
     * @throws Exception
     */
    @Test
    public void testVocabMalformedXML() throws Exception {
        final String vocabUrl = "http://fake.vocab/url";
        final String vocabResponse = "<invalid></xml>";
        final String repoInfoResponse = "<invalid></xml>";
        final SISSVocMethodMaker methodMaker = new SISSVocMethodMaker();


        //Our vocab test fires off 2 requests
        context.checking(new Expectations() {{
            exactly(2).of(mockServiceCaller).getHttpClient();
            will(returnValue(mockClient));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getRepositoryInfoMethod(vocabUrl).getURI().toString(), null)), with(mockClient));
            will(returnValue(repoInfoResponse));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getConceptByLabelMethod(vocabUrl, VocabController.COMMODITY_REPOSITORY, "*").getURI().toString(), null)), with(mockClient));
            will(returnValue(vocabResponse));
        }});

        //Our only restriction is asserting errors
        AdminDiagnosticResponse response = adminService.vocabConnectivity(vocabUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(2, response.getErrors().size());
    }

    /**
     * Tests that the vocab diagnostics correctly handle responses with bad XML
     * @throws Exception
     */
    @Test
    public void testVocabBadXML() throws Exception {
        final String vocabUrl = "http://fake.vocab/url";
        final String vocabResponse = "<validButUnknownXml/>";
        final String repoInfoResponse = "<validButUnknownXml/>";
        final SISSVocMethodMaker methodMaker = new SISSVocMethodMaker();


        //Our vocab test fires off 2 requests
        context.checking(new Expectations() {{
            exactly(2).of(mockServiceCaller).getHttpClient();
            will(returnValue(mockClient));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getRepositoryInfoMethod(vocabUrl).getURI().toString(), null)), with(mockClient));
            will(returnValue(repoInfoResponse));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getConceptByLabelMethod(vocabUrl, VocabController.COMMODITY_REPOSITORY, "*").getURI().toString(), null)), with(mockClient));
            will(returnValue(vocabResponse));
        }});

        //Our only restriction is asserting errors (the first test will succeed because we don't test its validity)
        AdminDiagnosticResponse response = adminService.vocabConnectivity(vocabUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(1, response.getErrors().size());
    }

    /**
     * Tests that the vocab diagnostics correctly handle connection errors
     * @throws Exception
     */
    @Test
    public void testVocabConnectionErrors() throws Exception {
        final String vocabUrl = "http://fake.vocab/url";
        final SISSVocMethodMaker methodMaker = new SISSVocMethodMaker();


        //Our vocab test fires off 2 requests
        context.checking(new Expectations() {{
            exactly(2).of(mockServiceCaller).getHttpClient();
            will(returnValue(mockClient));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getRepositoryInfoMethod(vocabUrl).getURI().toString(), null)), with(mockClient));
            will(throwException(new ConnectException()));

            oneOf(mockServiceCaller).getMethodResponseAsString(with(aHttpMethodBase(null, methodMaker.getConceptByLabelMethod(vocabUrl, VocabController.COMMODITY_REPOSITORY, "*").getURI().toString(), null)), with(mockClient));
            will(throwException(new ConnectException()));
        }});

        //Our only restriction is asserting errors
        AdminDiagnosticResponse response = adminService.vocabConnectivity(vocabUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(2, response.getErrors().size());
    }

    /**
     * Tests that WFS connectivity correctly calls services/handles responses
     * @throws Exception
     */
    @Test
    public void testWFSConnectivity() throws Exception {
        final List<EndpointAndSelector> endpoints = Arrays.asList(
            new EndpointAndSelector("http://endpoint1.url/wfs", "wfs:type1"), //will fail to connect
            new EndpointAndSelector("http://endpoint1.url/wfs", "wfs:type2"), //will be skipped for sharing same endpoint as #1
            new EndpointAndSelector("http://endpoint2.url/wfs", "wfs:type1"), //will return OWS exception
            new EndpointAndSelector("http://endpoint2.url/wfs", "wfs:type2")); //will return success
        final FilterBoundingBox bbox = new FilterBoundingBox("srs", new double[] {1,2}, new double[] {3,4});

        context.checking(new Expectations() {{
            exactly(5).of(mockServiceCaller).getHttpClient();
            will(returnValue(mockClient));

            //This will fail to connect and cause the second request AND other endpoint to be skipped
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, Pattern.compile(endpoints.get(0).getEndpoint() + ".*"), null)), with(mockClient));
            will(throwException(new ConnectException()));

            //Return OWS error
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, Pattern.compile(endpoints.get(2).getEndpoint() + ".*"), null)), with(mockClient));
            will(returnValue(TestAdminService.class.getResourceAsStream("/OWSExceptionSample1.xml")));
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, Pattern.compile(endpoints.get(2).getEndpoint() + ".*"), null)), with(mockClient));
            will(returnValue(TestAdminService.class.getResourceAsStream("/OWSExceptionSample1.xml")));

            //Return success
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, Pattern.compile(endpoints.get(3).getEndpoint() + ".*"), null)), with(mockClient));
            will(returnValue(TestAdminService.class.getResourceAsStream("/YilgarnGeochemGetFeatureResponse.xml")));
            oneOf(mockServiceCaller).getMethodResponseAsStream(with(aHttpMethodBase(null, Pattern.compile(endpoints.get(3).getEndpoint() + ".*"), null)), with(mockClient));
            will(returnValue(TestAdminService.class.getResourceAsStream("/YilgarnGeochemGetFeatureResponse.xml")));
        }});

        AdminDiagnosticResponse response = adminService.wfsConnectivity(endpoints, bbox);
        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getDetails().size()); //always 1 info block
        Assert.assertEquals(0, response.getWarnings().size());
        Assert.assertEquals(6, response.getErrors().size());
    }
}
