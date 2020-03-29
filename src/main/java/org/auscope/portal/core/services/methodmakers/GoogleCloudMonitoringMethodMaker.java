package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;

import org.apache.http.client.utils.URIBuilder;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * Method maker for generating Google StackDriver monitoring service
 * @author Rini Angreani (CSIRO)
 *
 */
public class GoogleCloudMonitoringMethodMaker extends AbstractMethodMaker {
	private HttpRequestFactory factory;

	public GoogleCloudMonitoringMethodMaker() {
		factory = new NetHttpTransport().createRequestFactory();
	}
    /**
     * Generates a timeseries query for uptime check metrics
     * @param projectName Unique google cloud monitoring project id
     * @param serviceName Service name related to this layer to match with uptime config e.g. wfsgetfeatureboreholeview
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpRequest getTimeSeriesUptimeCheck(String projectName, String serviceName) throws URISyntaxException, IOException {
    	// Make our request
    	// Request: GET https://monitoring.googleapis.com/v3/{name}/timeSeries
    	// {name} = projects/geoanalytics-tooling
    	String queryUrl = "https://monitoring.googleapis.com/v3/projects/" + projectName  + "/timeSeries";
        URIBuilder builder = new URIBuilder(queryUrl);

    	// filter = metrics.type="monitoring.googleapis.com/uptime_check/check_passed"
        StringBuilder filter = new StringBuilder("metrics.type=\"monitoring.googleapis.com/uptime_check/check_passed\"");
        filter.append(" AND ");
        // check for service type
        // check_id example: "auscope-nt-web-services-wfsgetfeatureboreholeview". The last part tells us it's a boreholeview
        filter.append("metric.labels.check_id = ends_with(\"").append(serviceName).append("\"");
        builder.addParameter("filter", filter.toString());

    	// start_time and end_time in timestamp eg. 2020-03-02T15:01:23.045123456Z
        // appears you need at least 7 seconds interval
        // starting from now
        Instant start = Instant.now();
        Instant end = start.plusSeconds(7);
        builder.addParameter("interval.startTime", start.toString());
        builder.addParameter("interval.endTime", end.toString());

        return factory.buildGetRequest(new GenericUrl(builder.build()));
    }
}
