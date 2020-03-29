package org.auscope.portal.core.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.methodmakers.GoogleCloudMonitoringMethodMaker;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Service class for accessing the REST API of a Google StackDriver instance
 * @author Rini Angreani (CSIRO)
 *
 */
public class GoogleCloudMonitoringService {
    private GoogleCloudMonitoringMethodMaker methodMaker;
    private String jsonKeyPath;

    public GoogleCloudMonitoringService(GoogleCloudMonitoringMethodMaker methodMaker) {
        super();
        this.methodMaker = methodMaker;
    }

    public void setJsonKeyPath(String jsonKeyPath) {
        this.jsonKeyPath = jsonKeyPath;
    }


    /**
     * Generates a GoogleCredentials loaded with config from this service (or null if none setup)
     *
     * @return
     */
    public GoogleCredentials generateCredentials() throws IOException {
    	  if (jsonKeyPath == null) {
    		  return null;
    	  }
    	  // You can specify a credential file by providing a path to GoogleCredentials.
    	  GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonKeyPath));
    	  return credentials;
    }

    /**
     * Gets a (possibly filtered) view of all services, keyed by their host name.
     * @param hostName
     * @return
     * @throws PortalServiceException
     */
    public Map<String, List<ServiceStatusResponse>> getStatuses(String hostName) throws PortalServiceException {

        // Make our request
    	// Request: GET https://monitoring.googleapis.com/v3/{name}/timeSeries
    	// {name} = projects%2Fgeoanalytics-tooling
    	// filter = metrics.type="monitoring.googleapis.com/uptime_check/check_passed"
    	// start_time and end_time in timestamp eg. 2020-03-02T15:01:23.045123456Z
        HttpRequest request = null;
        String responseString = null;
        try {
            request = methodMaker.getTimeSeriesUptimeCheck(projectName, serviceName);
            GoogleCredentials credentials = generateCredentials();
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
            requestInitializer.initialize(request);

            responseString = request.execute().getContentEncoding();
        } catch (Exception ex) {
            throw new PortalServiceException(request.getUrl().toString(), "Unable to access Google Cloud Monitoring service", ex);
        }

        //Parse our response
        HashMap<String, List<ServiceStatusResponse>> parsedResponses = new HashMap<String, List<ServiceStatusResponse>>();
        try {
            JSONObject responseObj = JSONObject.fromObject(responseString);

            JSONObject result = responseObj.getJSONObject("result");
            // Check for errors, e.g.
            //{
            //	  "error": {
            //	    "code": 400,
            //	    "message": "The provided filter matches more than one metric. TimeSeries data are limited to a single metric per request.",
            //	    "status": "INVALID_ARGUMENT"
            //	  }
            //	}
            if (result.getString("error") != null) {
            	JSONObject error = result.getJSONObject("error");
                String message = error.getString("message");
                throw new PortalServiceException("Response reports failure:" + error.getString("code") + " - " + message);
            }

            JSONArray serviceList = result.getJSONArray("timeSeries");

            List<ServiceStatusResponse> statusResponses = new ArrayList<ServiceStatusResponse>();

            for (Object service : serviceList) {
            	JSONObject serviceJSON = ((JSONObject)service);
                Object point = serviceJSON.getJSONArray("points").get(0);

                boolean passedCheck = ((JSONObject) point).getJSONObject("value").getBoolean("boolValue");

//                "metric": {
//                    "labels": {
//                      "checker_location": "eur-belgium",
//                      "check_id": "auscope-nt-web-services-wfsgetfeatureboreholeview",
//                      "checked_resource_id": "geology.data.nt.gov.au"
//                    },
//                    "type": "monitoring.googleapis.com/uptime_check/check_passed"
//                  }
                // get host
                JSONObject metric = serviceJSON.getJSONObject("metric");
                JSONObject labels = metric.getJSONObject("labels");
                String host = labels.getString("checked_resource_id");



                String checkId = labels.getString("check_id");


                statusResponses.add(new ServiceStatusResponse(passedCheck, checkId));
                parsedResponses.put(hostName.toString(), statusResponses);

                ArrayList<ServiceStatusResponse> statusResponses = new ArrayList<ServiceStatusResponse>();
                JSONObject hostResponse = serviceList.getJSONObject(hostName.toString());
                for (Object serviceName : hostResponse.keySet()) {
                    String rawStatus = hostResponse.getString(serviceName.toString());
                    Status status = Enum.valueOf(Status.class, rawStatus);
                    statusResponses.add(new ServiceStatusResponse(status, serviceName.toString()));
                }
                parsedResponses.put(hostName.toString(), statusResponses);
            }
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse Google Cloud Monitoring response", ex);
        }

        return parsedResponses;
    }
}
