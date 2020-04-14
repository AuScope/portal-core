package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

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
     * @param projectId Unique google cloud monitoring project id
     * @param checkIds Part of check ids related to a layer to match with uptimecheck config e.g. wfsgetfeatureboreholeview
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public HttpRequest getTimeSeriesUptimeCheck(String projectId, List<String> checkIds) throws URISyntaxException, IOException {
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
        Iterator<String> checkIdsIterator = checkIds.iterator();
        while (checkIdsIterator.hasNext()) {
            orFilter.append("metric.labels.check_id = has_substring(\"");
            orFilter.append(checkIdsIterator.next());
            orFilter.append("\")");
            if (checkIdsIterator.hasNext()) {
            	orFilter.append(" OR ");
            }
        }
        orFilter.append(")");
        filter.append(orFilter);
        builder.addParameter("filter", filter.toString());

    	// start_time and end_time in timestamp eg. 2020-03-02T15:01:23.045123456Z
        // one interval is 10s
        // since it takes a long time for the uptime checks to return a value (WFS queries),
        // and uptime checks are done every 10 minutes, we should check intervals
        // in the last 10 minutes for it to return results
        Instant end = Instant.now();
        Instant start = end.minusSeconds(600);
        builder.addParameter("interval.startTime", start.toString());
        builder.addParameter("interval.endTime", end.toString());

        return factory.buildGetRequest(new GenericUrl(builder.build()));
    }
}
