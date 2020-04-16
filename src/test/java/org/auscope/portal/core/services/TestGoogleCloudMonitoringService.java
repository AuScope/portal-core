package org.auscope.portal.core.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.auscope.portal.core.services.methodmakers.GoogleCloudMonitoringMethodMaker;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;
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


    @Before
    public void setup() {
        service = new GoogleCloudMonitoringCachedService(mockMethodMaker);

        // loaded from auscope portal env.properties
        service.setClientEmail("serviceaccount@email.com");
        service.setClientId("12345");
        service.setPrivateKey("-----BEGIN PRIVATE KEY-----\\nsecretprivatekeystring\\n-----END PRIVATE KEY-----");
        service.setPrivateKeyId("privatekeyid");
        service.setProjectId("auscope-siss");
        service.setTokenUri("https://oauth2.googleapis.com/token");

        // loaded from auscope portal applicationContext.xml
        HashMap<String, List<String>> servicesMap = new HashMap<String, List<String>>();
        servicesMap.put("EarthResourcesLayers", Arrays.asList(
        		new String[] {"wfsgetfeatureminoccview", "wfsgetcaps"}));
        servicesMap.put("TenementsLayers", Arrays.asList(
        		new String[] {"wfsgetfeaturetenements", "wfsgetcaps"}));
        service.setServicesMap(servicesMap);
    }

    @Test
    public void testGoogleCloudMonitoringMethodMaker() throws URISyntaxException, IOException {

    	HttpRequest request = mockMethodMaker.getTimeSeriesUptimeCheck(service.getProjectId(),
    			service.getServicesMap().get("EarthResourcesLayers"));
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
    			mockMethodMaker.getTimeSeriesUptimeCheck(service.getProjectId(),
    			    service.getServicesMap().get("EarthResourcesLayers")));
    	// make sure it generates expected access token
    	assertEquals("Bearer : ", request.getHeaders().getAuthorization());

    	// but then it will fail because this login details is fake
    	// so may as well test authorization failure
    	boolean portalServiceExceptionThrown = false;
    	try {
    	    service.getStatuses("EarthResourcesLayers");
    	} catch (PortalServiceException e) {
    		assertEquals(e.getCause(), null);
    		portalServiceExceptionThrown = true;
    	}
    	assertEquals(true, portalServiceExceptionThrown);
    }

    @Test
    public void testGetStatusesSuccessful() throws IOException, PortalServiceException {
    	final String responseString = ResourceUtil.loadResourceAsString(
    			"org/auscope/portal/core/test/responses/stackdriver/stackdriver-timeseries-success.json");

        context.checking(new Expectations() {
            {
                oneOf(service.request).execute().toString();
                will(returnValue(responseString));
            }
        });
        // test responses and then test caching
        Map<String, List<ServiceStatusResponse>> responses = service.getStatuses("EarthResourcesLayers");
        Set<String> failingHosts = new HashSet<String>();
        for (Entry<String, List<ServiceStatusResponse>> entry : responses.entrySet()) {
        	for (ServiceStatusResponse status : entry.getValue()) {
        		if (!status.isUp()) {
        			failingHosts.add(entry.getKey());
        		}
        	}
        }
        // check only GSWA failed
        assertEquals(1, failingHosts.size());
        assertEquals("geossdi.dmp.wa.gov.au", failingHosts.iterator().next());
        // check it's caching correctly, not all services are down
        String serviceUp  = null;
        for (ServiceStatusResponse status : responses.get("geossdi.dmp.wa.gov.au")) {
        	if (status.isUp()) {
        		serviceUp = status.getServiceName();
        	}
        }
        assertEquals("auscope-wa-web-services-wfsgetfeatureboreholeview", serviceUp);
    }

    @Test
    public void testGetStatusesError() throws IOException {
    	final String responseString = ResourceUtil.loadResourceAsString(
    			"org/auscope/portal/core/test/responses/stackdriver/stackdriver-timeseries-fail.json");

    	context.checking(new Expectations() {
            {
                oneOf(service.request).execute().toString();
                will(returnValue(responseString));
            }
        });

    	boolean exceptionThrown = false;
    	try {
    	    service.getStatuses("EarthResourcesLayers");
    	} catch (PortalServiceException e) {
    		exceptionThrown = true;
    		assertEquals("Response reports failure:400 - The provided filter matches more than one metric.\n"
    				+ "TimeSeries data are limited to a single metric per request.", true);
    	}
    	assertEquals(true, exceptionThrown);
    }
}
