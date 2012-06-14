package org.auscope.portal.core.services;

import junit.framework.Assert;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.GeonetworkCredentials;
import org.auscope.portal.core.services.methodmakers.GeonetworkMethodMaker;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

public class TestGeonetworkService extends PortalTestClass {

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private HttpServiceCaller serviceCaller;
    private GeonetworkMethodMaker gnMethodMaker;
    private GeonetworkCredentials gnDetails;
    private GeonetworkService service;

    @Before
    public void setup() {
        serviceCaller = context.mock(HttpServiceCaller.class);
        gnMethodMaker = context.mock(GeonetworkMethodMaker.class);
        gnDetails = new GeonetworkCredentials("http://example.org/gn", "user-name", "pass-word");
        service = new GeonetworkService(serviceCaller, gnMethodMaker, gnDetails);
    }

    @Test
    public void testSuccessfulRequest() throws Exception {
        final String sessionCookie = "sessionCookie";
        final HttpMethodBase insertRecordMethod = context.mock(HttpMethodBase.class, "insertRecordMethod");
        final HttpMethodBase recordMetadataShowMethod = context.mock(HttpMethodBase.class, "recordMetadataShowMethod");
        final HttpMethodBase recordMetadataGetMethod = context.mock(HttpMethodBase.class, "recordMetadataGetMethod");
        final HttpMethodBase recordPublicMethod = context.mock(HttpMethodBase.class, "recordPublicMethod");
        final HttpMethodBase loginMethod = context.mock(HttpMethodBase.class, "loginMethod");
        final HttpMethodBase logoutMethod = context.mock(HttpMethodBase.class, "logoutMethod");

        final String uuid = "4cda9dc3-9a0e-40cd-a3a9-64db5ce3c031";
        final String recordId = "21569";
        final String insertResponse = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNCSWInsertResponse.xml");
        final String loginResponse = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNLoginLogoutSuccessResponse.xml");
        final String logoutResponse = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNLoginLogoutSuccessResponse.xml");
        final String recordPublicResponse = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNRecordPublicResponse.xml");
        final String recordGetMetadata = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/geonetwork/GNMetadataGetXMLResponse.xml");

        final CSWRecord record = new CSWRecord("a", "b", "c", "", new CSWOnlineResourceImpl[0], new CSWGeographicElement[0]);
        final URI responseUri = new URI("http://foo.bar.baz", false);

        context.checking(new Expectations() {{
            allowing(gnMethodMaker).makeInsertRecordMethod(with(any(String.class)), with(any(String.class)), with(any(String.class)));will(returnValue(insertRecordMethod));
            allowing(gnMethodMaker).makeRecordMetadataGetMethod(gnDetails.getUrl(), uuid, sessionCookie);will(returnValue(recordMetadataGetMethod));
            allowing(gnMethodMaker).makeRecordMetadataShowMethod(gnDetails.getUrl(), uuid, sessionCookie);will(returnValue(recordMetadataShowMethod));
            allowing(gnMethodMaker).makeRecordPublicMethod(gnDetails.getUrl(), recordId, sessionCookie);will(returnValue(recordPublicMethod));
            allowing(gnMethodMaker).makeUserLoginMethod(gnDetails.getUrl(), gnDetails.getUser(), gnDetails.getPassword());will(returnValue(loginMethod));
            allowing(gnMethodMaker).makeUserLogoutMethod(gnDetails.getUrl(), sessionCookie);will(returnValue(logoutMethod));

            allowing(loginMethod).getResponseHeader("Set-Cookie");will(returnValue(new Header("Set-Cookie", sessionCookie)));

            oneOf(serviceCaller).getMethodResponseAsString(insertRecordMethod);will(returnValue(insertResponse));
            oneOf(serviceCaller).getMethodResponseAsString(recordMetadataGetMethod);will(returnValue(recordGetMetadata));
            oneOf(serviceCaller).getMethodResponseAsString(recordPublicMethod);will(returnValue(recordPublicResponse));
            oneOf(serviceCaller).getMethodResponseAsString(loginMethod);will(returnValue(loginResponse));
            oneOf(serviceCaller).getMethodResponseAsString(logoutMethod);will(returnValue(logoutResponse));

            allowing(recordMetadataShowMethod).getURI();will(returnValue(responseUri));
        }});

        Assert.assertEquals(responseUri.toString(), service.makeCSWRecordInsertion(record));
    }

    @Test(expected=Exception.class)
    public void testBadLoginRequest() throws Exception {
        final HttpMethodBase loginMethod = context.mock(HttpMethodBase.class, "loginMethod");
        final String loginResponse = "<html>The contents doesn't matter as a failed GN login returns a static page</html>";

        final CSWRecord record = new CSWRecord("a", "b", "c", "", new CSWOnlineResourceImpl[0], new CSWGeographicElement[0]);

        context.checking(new Expectations() {{
            allowing(gnMethodMaker).makeUserLoginMethod(gnDetails.getUrl(), gnDetails.getUser(), gnDetails.getPassword());will(returnValue(loginMethod));

            oneOf(serviceCaller).getMethodResponseAsString(loginMethod);will(returnValue(loginResponse));
        }});

        service.makeCSWRecordInsertion(record);
    }
}
