package org.auscope.portal.core.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.GoogleCloudMonitoringMethodMaker;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.HttpRequest;

/**
 * Unit tests for Google StackDriver monitoring.
 *
 * @author Rini Angreani (CSIRO)
 *
 */
public class TestGoogleCloudMonitoringService extends PortalTestClass {

    private GoogleCloudMonitoringMethodMaker mockMethodMaker =  context.mock(GoogleCloudMonitoringMethodMaker.class);

    private GoogleCloudMonitoringCachedService service;

    private List<String> serviceCheckIds;

	private String projectId = "auscope-siss";


    @Before
    public void setup() {
        service = new GoogleCloudMonitoringCachedService(mockMethodMaker);

        service.setClientEmail("serviceaccount@email.com");
        service.setClientId("12345");
        service.setPrivateKey("-----BEGIN PRIVATE KEY-----\\nsecretprivatekeystring\\n-----END PRIVATE KEY-----");
        service.setPrivateKeyId("privatekeyid");
        service.setProjectId(this.projectId);
        service.setTokenUri("https://oauth2.googleapis.com/token");

        serviceCheckIds = new ArrayList<String>();
    	serviceCheckIds.add("wfsgetfeatureminoccview");
    	serviceCheckIds.add("wfsgetcaps");
    }

    @Test
    public void testGoogleCloudMonitoringMethodMaker() throws URISyntaxException, IOException {

    	HttpRequest request = mockMethodMaker.getTimeSeriesUptimeCheck(this.projectId, serviceCheckIds);
    	String[] urlParts = request.getUrl().toString().split("&");
    	assertEquals(urlParts.length, 3);

    	String queryBeforeInterval = "https://monitoring.googleapis.com/v3/projects/auscope-siss/timeSeries?"
    			+ "filter=metric.type%3D%22monitoring.googleapis.com/uptime_check/check_passed%22%20"
    			+ "AND%20(metric.labels.check_id%20%3D%20has_substring(%22wfsgetfeatureminoccview%22)%20"
    			+ "OR%20metric.labels.check_id%20%3D%20has_substring(%22wfsgetcaps%22))";
    	assertEquals(queryBeforeInterval, urlParts[0]);

    	String intervalStart = urlParts[1];
    	assertEquals(intervalStart.split("=")[0], "interval.startTime");
    	Instant start = Instant.parse(intervalStart.split("=")[1]);

    	String intervalEnd = urlParts[2];
    	assertEquals(intervalEnd.split("=")[0], "interval.endTime");
    	Instant end = Instant.parse(intervalEnd.split("=")[1]);

       	// it's hard to get the correct time as it's done inside the method maker, but we can check the interval
    	assertEquals(start, end.minusSeconds(600));
    }

    @Test
    public void testSetAuthorization() throws URISyntaxException, IOException, PortalServiceException {
    	HttpRequest request = service.setAuthorization(
    			mockMethodMaker.getTimeSeriesUptimeCheck(this.projectId, serviceCheckIds));
    	assertEquals("Bearer : ", request.getHeaders().getAuthorization());
    }

    @Test
    public void testGetStatusesSuccessful() throws IOException {
    	final String responseString = ResourceUtil.loadResourceAsString(
    			"org/auscope/portal/core/test/responses/stackdriver/stackdriver-timeseries-success.json");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, serviceGroup, null);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider) null);
                will(returnValue(responseString));
            }
        });
        // test responses and then test caching

        // cache still valid

        // cache expires

        // cache doesn't exist
    }

    @Test
    public void testGetStatusesError() throws IOException {
    	final String responseString = ResourceUtil.loadResourceAsString(
    			"org/auscope/portal/core/test/responses/stackdriver/stackdriver-timeseries-success.json");

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).statusServiceListJSON(serviceUrl, hostGroup, serviceGroup, null);
                will(returnValue(mockMethod));

                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod, (CredentialsProvider) null);
                will(returnValue(responseString));
            }
        });
    }
}
