package org.auscope.portal.core.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.Nagios4MethodMaker;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse.Status;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Nagios4Service
 * @author Josh Vote (CSIRO)
 *
 */
public class TestNagios4Service extends PortalTestClass {

    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private Nagios4MethodMaker mockMethodMaker =  context.mock(Nagios4MethodMaker.class);
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);

    private final String user = "example-user";
    private final String password = "example-pwd";
    private final String serviceUrl = "http://example.com/nagios";

    private Nagios4Service service;


    @Before
    public void setup() {
        service = new Nagios4Service(serviceUrl, mockServiceCaller, mockMethodMaker);
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
        final String hostGroup = "foo";
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/nagios/nagios4-status-servicelist-success.json");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, null);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(with(mockMethod), with(any(CredentialsProvider.class)));
                will(returnValue(responseString));
            }
        });

        service.setUserName(user);
        service.setPassword(password);

        //Do main result parsing in testSuccessfulResponseNoPassword
        Map<String, List<ServiceStatusResponse>> result = service.getStatuses(hostGroup);
        Assert.assertNotNull(result);
    }

    @Test
    public void testSuccessfulResponseNoPassword() throws Exception {
        final String hostGroup = "foo";
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/nagios/nagios4-status-servicelist-success.json");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, null);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider) null);
                will(returnValue(responseString));
            }
        });

        Map<String, List<ServiceStatusResponse>> result = service.getStatuses(hostGroup);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());

        Assert.assertTrue(result.containsKey("auscope-portal-2"));
        Assert.assertTrue(result.containsKey("portal.auscope.org"));

        List<ServiceStatusResponse> statuses = result.get("auscope-portal-2");
        Assert.assertEquals(9, statuses.size());
        Assert.assertEquals(Status.ok, getByName("AuScope Google Map Client", statuses).getStatus());
        Assert.assertEquals(Status.ok, getByName("Disk Check", statuses).getStatus());
        Assert.assertEquals(Status.warning, getByName("Load Check", statuses).getStatus());
        Assert.assertEquals(Status.ok, getByName("time-check", statuses).getStatus());

        statuses = result.get("portal.auscope.org");
        Assert.assertEquals(9, statuses.size());
        Assert.assertEquals(Status.ok, getByName("AuScope Google Map Client", statuses).getStatus());
        Assert.assertEquals(Status.pending, getByName("Auscope Portal GeoNetwork Data CSW", statuses).getStatus());
        Assert.assertEquals(Status.unknown, getByName("Load Check", statuses).getStatus());
        Assert.assertEquals(Status.critical, getByName("PING", statuses).getStatus());
    }

    @Test(expected=PortalServiceException.class)
    public void testConnectionError() throws Exception {
        final String hostGroup = "foo";

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, null);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider)null);
                will(throwException(new IOException()));
            }
        });

        service.getStatuses(hostGroup);
    }

    @Test(expected=PortalServiceException.class)
    public void testFailureResponse() throws Exception {
        final String hostGroup = "foo";
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/core/test/responses/nagios/nagios4-status-servicelist-failure.json");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, null);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider) null);
                will(returnValue(responseString));
            }
        });

        service.getStatuses(hostGroup);
    }
}
