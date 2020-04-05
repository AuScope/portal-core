package org.auscope.portal.core.services;

import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.methodmakers.GoogleCloudMonitoringMethodMaker;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Google StackDriver monitoring - adapted from Nagios code and tests.
 *
 * @author Josh Vote (CSIRO)
 * @author Rini Angreani (CSIRO)
 *
 */
public class TestGoogleCloudMonitoringService extends PortalTestClass {

    private GoogleCloudMonitoringMethodMaker mockMethodMaker =  context.mock(GoogleCloudMonitoringMethodMaker.class);
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);


    private final String user = "example-user";
    private final String password = "example-pwd";
    private final String projectId = "geoanalytics-tooling";

    private GoogleCloudMonitoringService service;


    @Before
    public void setup() {
        service = new GoogleCloudMonitoringService(mockMethodMaker);
    }

    private ServiceStatusResponse getByName(String name, List<ServiceStatusResponse> list) {
        for (ServiceStatusResponse r : list) {
            if (r.getServiceName().equals(name)) {
                return r;
            }
        }

        return null;
    }

    @Test
    public void testSuccessfulResponsePassword() throws Exception {
//        final String hostGroup = "foo";
//        final String serviceGroup = "bar";
//        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/nagios/nagios4-status-servicelist-success.json");
//
//        context.checking(new Expectations() {
//            {
//                oneOf(mockMethodMaker).getTimeSeriesUptimeCheck(this.projectId));
//                will(returnValue(mockMethod));
//
//                oneOf(mockServiceCaller).getMethodResponseAsString(with(mockMethod), with(any(CredentialsProvider.class)));
//                will(returnValue(responseString));
//            }
//        });
//
//        service.setUserName(user);
//        service.setPassword(password);
//
//        //Do main result parsing in testSuccessfulResponseNoPassword
//        Map<String, List<ServiceStatusResponse>> result = service.getStatuses(hostGroup, serviceGroup);
//        Assert.assertNotNull(result);
    }

    @Test
    public void testSuccessfulResponseNoPassword() throws Exception {
        final String hostGroup = "foo";
        final String serviceGroup = "bar";
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/nagios/nagios4-status-servicelist-success.json");

//        context.checking(new Expectations() {
//            {
//                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, serviceGroup, null);
//                will(returnValue(mockMethod));
//
//                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider) null);
//                will(returnValue(responseString));
//            }
//        });
//
//        Map<String, List<ServiceStatusResponse>> result = service.getStatuses(hostGroup, serviceGroup);
//        Assert.assertNotNull(result);
//        Assert.assertEquals(2, result.size());
//
//        Assert.assertTrue(result.containsKey("auscope-portal-2"));
//        Assert.assertTrue(result.containsKey("portal.auscope.org"));
//
//        List<ServiceStatusResponse> statuses = result.get("auscope-portal-2");
//        Assert.assertEquals(9, statuses.size());
//        Assert.assertEquals(Status.ok, getByName("AuScope Google Map Client", statuses).getStatus());
//        Assert.assertEquals(Status.ok, getByName("Disk Check", statuses).getStatus());
//        Assert.assertEquals(Status.warning, getByName("Load Check", statuses).getStatus());
//        Assert.assertEquals(Status.ok, getByName("time-check", statuses).getStatus());
//
//        statuses = result.get("portal.auscope.org");
//        Assert.assertEquals(9, statuses.size());
//        Assert.assertEquals(Status.ok, getByName("AuScope Google Map Client", statuses).getStatus());
//        Assert.assertEquals(Status.pending, getByName("Auscope Portal GeoNetwork Data CSW", statuses).getStatus());
//        Assert.assertEquals(Status.unknown, getByName("Load Check", statuses).getStatus());
//        Assert.assertEquals(Status.critical, getByName("PING", statuses).getStatus());
    }

    @Test(expected=PortalServiceException.class)
    public void testConnectionError() throws Exception {
        final String hostGroup = "foo";
        final String serviceGroup = "bar";

//        context.checking(new Expectations() {
//            {
//                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, serviceGroup, null);
//                will(returnValue(mockMethod));
//
//                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider)null);
//                will(throwException(new IOException()));
//            }
//        });
//
//        service.getStatuses(hostGroup, serviceGroup);
    }

    @Test(expected=PortalServiceException.class)
    public void testFailureResponse() throws Exception {
        final String hostGroup = "foo";
        final String serviceGroup = "bar";
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/nagios/nagios4-status-servicelist-failure.json");

//        context.checking(new Expectations() {
//            {
//                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, serviceGroup, null);
//                will(returnValue(mockMethod));
//
//                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider) null);
//                will(returnValue(responseString));
//            }
//        });
//
//        service.getStatuses(hostGroup, serviceGroup);
    }
}
