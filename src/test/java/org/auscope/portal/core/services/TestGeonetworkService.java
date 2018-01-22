package org.auscope.portal.core.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.auscope.portal.core.server.http.HttpClientResponse;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.GeonetworkMethodMaker;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class TestGeonetworkService extends PortalTestClass {

    private HttpServiceCaller serviceCaller;
    private GeonetworkMethodMaker gnMethodMaker;
    private GeonetworkService service;

    private final String endpoint = "http://example.org/gn";
    private final String userName = "un";
    private final String password = "pwd";

    @Before
    public void setup() {
        serviceCaller = context.mock(HttpServiceCaller.class);
        gnMethodMaker = context.mock(GeonetworkMethodMaker.class);
        CSWServiceItem item = new CSWServiceItem("fake-id", endpoint + "/srv/en/csw");
        item.setUserName(userName);
        item.setPassword(password);
        service = new GeonetworkService(serviceCaller, gnMethodMaker, item);
    }

    @Test
    public void testSuccessfulRequest() throws Exception {
        final String sessionCookie = "sessionCookie";
        final HttpRequestBase insertRecordMethod = context.mock(HttpRequestBase.class, "insertRecordMethod");
        final HttpRequestBase recordMetadataShowMethod = context
                .mock(HttpRequestBase.class, "recordMetadataShowMethod");
        final HttpRequestBase recordMetadataGetMethod = context.mock(HttpRequestBase.class, "recordMetadataGetMethod");
        final HttpRequestBase recordPublicMethod = context.mock(HttpRequestBase.class, "recordPublicMethod");
        final HttpRequestBase loginMethod = context.mock(HttpRequestBase.class, "loginMethod");
        final HttpRequestBase logoutMethod = context.mock(HttpRequestBase.class, "logoutMethod");
        final HttpEntity mockEntity = context.mock(HttpEntity.class);

        final String uuid = "4cda9dc3-9a0e-40cd-a3a9-64db5ce3c031";
        final String recordId = "21569";
        final String insertResponse = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNCSWInsertResponse.xml");
        final InputStream loginResponse = ResourceUtil
                .loadResourceAsStream("org/auscope/portal/core/test/responses/geonetwork/GNLoginLogoutSuccessResponse.xml");
        final String logoutResponse = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNLoginLogoutSuccessResponse.xml");
        final String recordPublicResponse = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNRecordPublicResponse.xml");
        final String recordGetMetadata = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNMetadataGetXMLResponse.xml");

        final CSWRecord record = new CSWRecord("a", "b", "c", "", new CSWOnlineResourceImpl[0],
                new CSWGeographicElement[0]);
        final URI responseUri = new URI("http://foo.bar.baz");

        try (final HttpClientResponse mockLoginResponse = context.mock(HttpClientResponse.class)) {

            context.checking(new Expectations() {
                {
                    allowing(gnMethodMaker).makeInsertRecordMethod(with(any(String.class)), with(any(String.class)),
                            with(any(String.class)));
                    will(returnValue(insertRecordMethod));
                    allowing(gnMethodMaker).makeRecordMetadataGetMethod(endpoint, uuid, sessionCookie);
                    will(returnValue(recordMetadataGetMethod));
                    allowing(gnMethodMaker).makeRecordMetadataShowMethod(endpoint, uuid, sessionCookie);
                    will(returnValue(recordMetadataShowMethod));
                    allowing(gnMethodMaker).makeRecordPublicMethod(endpoint, recordId, sessionCookie);
                    will(returnValue(recordPublicMethod));
                    allowing(gnMethodMaker).makeUserLoginMethod(endpoint, userName, password);
                    will(returnValue(loginMethod));
                    allowing(gnMethodMaker).makeUserLogoutMethod(endpoint, sessionCookie);
                    will(returnValue(logoutMethod));

                    allowing(mockLoginResponse).getFirstHeader("Set-Cookie");
                    will(returnValue(new BasicHeader("Set-Cookie", sessionCookie)));

                    oneOf(serviceCaller).getMethodResponseAsString(insertRecordMethod);
                    will(returnValue(insertResponse));
                    oneOf(serviceCaller).getMethodResponseAsString(recordMetadataGetMethod);
                    will(returnValue(recordGetMetadata));
                    oneOf(serviceCaller).getMethodResponseAsString(recordPublicMethod);
                    will(returnValue(recordPublicResponse));
                    oneOf(serviceCaller).getMethodResponseAsHttpResponse(loginMethod);
                    will(returnValue(mockLoginResponse));
                    oneOf(mockLoginResponse).getEntity();
                    will(returnValue(mockEntity));
                    oneOf(mockEntity).getContent();
                    will(returnValue(loginResponse));
                    oneOf(serviceCaller).getMethodResponseAsString(logoutMethod);
                    will(returnValue(logoutResponse));

                    allowing(recordMetadataShowMethod).getURI();
                    will(returnValue(responseUri));

                    allowing(mockLoginResponse).close();
                }
            });

            Assert.assertEquals(responseUri.toString(), service.makeCSWRecordInsertion(record));
        }
    }

    @Test(expected = Exception.class)
    public void testBadLoginRequest() throws Exception {
        final HttpRequestBase loginMethod = context.mock(HttpRequestBase.class, "loginMethod");
        final HttpEntity mockEntity = context.mock(HttpEntity.class);
        final InputStream loginResponse = new ByteArrayInputStream("<html>The contents doesn't matter as a failed GN login returns a static page</html>".getBytes());

        final CSWRecord record = new CSWRecord("a", "b", "c", "", new CSWOnlineResourceImpl[0],
                new CSWGeographicElement[0]);

        try (final HttpClientResponse mockLoginResponse = context.mock(HttpClientResponse.class)) {
            context.checking(new Expectations() {
                {
                    allowing(gnMethodMaker).makeUserLoginMethod(endpoint, userName, password);
                    will(returnValue(loginMethod));

                    oneOf(serviceCaller).getMethodResponseAsHttpResponse(loginMethod);
                    will(returnValue(mockLoginResponse));
                    oneOf(mockLoginResponse).getEntity();
                    will(returnValue(mockEntity));
                    oneOf(mockEntity).getContent();
                    will(returnValue(loginResponse));

                    allowing(mockLoginResponse).close();
                }
            });

            service.makeCSWRecordInsertion(record);
        }
    }
}
