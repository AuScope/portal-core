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
import org.auscope.portal.core.util.ResourceUtil;
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

    private GoogleCloudMonitoringCachedService service;


    @Before
    public void setup() {
        service = new GoogleCloudMonitoringCachedService(new GoogleCloudMonitoringMethodMaker());

        // loaded from auscope portal env.properties
        service.setProjectId("auscope-siss");

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

    	HttpRequest request = new GoogleCloudMonitoringMethodMaker().getTimeSeriesUptimeCheck(service.getProjectId(),
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
    public void testGetStatusesSuccessful() throws IOException, PortalServiceException, URISyntaxException {
    	final String responseString = ResourceUtil.loadResourceAsString(
    			"org/auscope/portal/core/test/responses/stackdriver/stackdriver-timeseries-success.json");

    	// test responses
        Map<String, List<ServiceStatusResponse>> responses = service.parseResponses(responseString);

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
    public void testGetStatusesError() throws IOException, URISyntaxException {
    	final String responseString = ResourceUtil.loadResourceAsString(
    			"org/auscope/portal/core/test/responses/stackdriver/stackdriver-timeseries-fail.json");
    	boolean exceptionThrown = false;
    	try {
    		service.parseResponses(responseString);
    	} catch (PortalServiceException e) {
    		exceptionThrown = true;
    		assertEquals("Response reports failure:400 - The provided filter matches more than one metric. "
    				+ "TimeSeries data are limited to a single metric per request.", e.getCause().getMessage());
    	}
    	assertEquals(true, exceptionThrown);
    }
}
