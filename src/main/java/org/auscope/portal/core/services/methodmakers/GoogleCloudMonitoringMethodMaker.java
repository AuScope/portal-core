package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;

import org.apache.http.client.utils.URIBuilder;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;

/**
 * Method maker for generating Google StackDriver monitoring service
 * @author Rini Angreani (CSIRO)
 *
 */
public class GoogleCloudMonitoringMethodMaker extends AbstractMethodMaker {
	private HttpRequestFactory factory;

	public GoogleCloudMonitoringMethodMaker() {
		factory = new ApacheHttpTransport().createRequestFactory();
	}
    /**
     * Generates a timeseries query for uptime check metrics
     * @param projectName Unique google cloud monitoring project id
     * @param serviceCheckIds Part of check id related to this layer to match with uptime config e.g. wfsgetfeatureboreholeview
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpRequest getTimeSeriesUptimeCheck(String projectId, String[] serviceCheckIds) throws URISyntaxException, IOException {
    	// Make our request
    	// Request: GET https://monitoring.googleapis.com/v3/{name}/timeSeries
    	// {name} = projects/geoanalytics-tooling
    	String queryUrl = "https://monitoring.googleapis.com/v3/projects/" + projectId  + "/timeSeries";
        URIBuilder builder = new URIBuilder(queryUrl);

    	// filter = metrics.type="monitoring.googleapis.com/uptime_check/check_passed"
        StringBuilder filter = new StringBuilder("metric.type=\"monitoring.googleapis.com/uptime_check/check_passed\"");
        filter.append(" AND ");
        // check for service type
        // check_id example: "auscope-nt-web-services-wfsgetfeatureboreholeview". The last part tells us it's a boreholeview
        StringBuilder orFilter = new StringBuilder("(");
        for (int i = 0; i < serviceCheckIds.length; i++) {
            orFilter.append("metric.labels.check_id = has_substring(\"");
            orFilter.append(serviceCheckIds[i]);
            orFilter.append("\")");
            if (i < serviceCheckIds.length - 1) {
            	orFilter.append(" OR ");
            }
        }
        orFilter.append(")");
        filter.append(orFilter);
        builder.addParameter("filter", filter.toString());

    	// start_time and end_time in timestamp eg. 2020-03-02T15:01:23.045123456Z
        // allow a minute to get results
        Instant end = Instant.now();
        Instant start = end.minusSeconds(60);
        builder.addParameter("interval.startTime", start.toString());
        builder.addParameter("interval.endTime", end.toString());

        return factory.buildGetRequest(new GenericUrl(builder.build()));
    }
}
