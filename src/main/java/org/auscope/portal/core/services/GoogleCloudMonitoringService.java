package org.auscope.portal.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.auscope.portal.core.services.methodmakers.GoogleCloudMonitoringMethodMaker;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.ServiceAccountCredentials;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Service class for accessing the REST API of a Google StackDriver instance
 * @author Rini Angreani (CSIRO)
 *
 */
public class GoogleCloudMonitoringService {
	// See list of scopes here: https://developers.google.com/identity/protocols/oauth2/scopes#clouddebuggerv2
    private static final String SCOPE = "https://www.googleapis.com/auth/monitoring.read";

	private GoogleCloudMonitoringMethodMaker methodMaker;
	private String privateKey;
	private String clientId;
	private String privateKeyId;
	private String clientEmail;
	private String tokenUri;
	private String projectId;

    public GoogleCloudMonitoringService(GoogleCloudMonitoringMethodMaker methodMaker) {
        super();
        this.methodMaker = methodMaker;
    }

    public void setClientId(String clientId) {
    	this.clientId = clientId;
    }

    public void setPrivateKey(String privateKey) {
    	this.privateKey = privateKey;
    }

    public void setPrivateKeyId(String privateKeyId) {
    	this.privateKeyId = privateKeyId;
    }

    public void setClientEmail(String clientEmail) {
    	this.clientEmail = clientEmail;
    }

    public void setTokenUri(String tokenUri) {
    	this.tokenUri = tokenUri;
    }

    public void setProjectId(String projectId) {
    	this.projectId = projectId;
    }

    public String getProjectId() {
    	return this.projectId;
    }

    public PrivateKey createPrivateKey(String key) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    	// Read in the key into a String
        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(key));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }

        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");

        byte [] pkcs8EncodedBytes = Base64.decodeBase64(pkcs8Pem);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    /**
     * Gets a (possibly filtered) view of all services, keyed by their host name.
     * @param
     * @return
     * @throws PortalServiceException
     */
    public Map<String, List<ServiceStatusResponse>> getStatuses(List<String> checkIds) throws PortalServiceException {

        // Make our request
    	// Request: GET https://monitoring.googleapis.com/v3/{name}/timeSeries
    	// {name} = projects%2Fgeoanalytics-tooling
    	// filter = metrics.type="monitoring.googleapis.com/uptime_check/check_passed"
    	// start_time and end_time in timestamp eg. 2020-03-02T15:01:23.045123456Z
        HttpRequest request = null;
        String responseString = null;
        try {
            request = methodMaker.getTimeSeriesUptimeCheck(this.projectId, checkIds);

            request = this.setAuthorization(request);

            responseString = request.execute().parseAsString();
        } catch (Exception ex) {
            throw new PortalServiceException(request.getUrl().toString(), "Unable to access Google Cloud Monitoring service", ex);
        }

        //Parse our response
        HashMap<String, List<ServiceStatusResponse>> parsedResponses = new HashMap<String, List<ServiceStatusResponse>>();
        try {
            JSONObject result = JSONObject.fromObject(responseString);
            // Check for errors, e.g.
            //{
            //	  "error": {
            //	    "code": 400,
            //	    "message": "The provided filter matches more than one metric. TimeSeries data are limited to a single metric per request.",
            //	    "status": "INVALID_ARGUMENT"
            //	  }
            //	}
            if (result.containsKey("error")) {
            	JSONObject error = result.getJSONObject("error");
                String message = error.getString("message");
                throw new PortalServiceException("Response reports failure:" + error.getString("code") + " - " + message);
            }

            JSONArray serviceList = result.getJSONArray("timeSeries");

            List<ServiceStatusResponse> statusResponses;
            for (Object service : serviceList) {
            	JSONObject serviceJSON = ((JSONObject)service);
            	// get the latest timeseries interval i.e. last 10 seconds
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

                String serviceName = labels.getString("check_id");
                String hostName = labels.getString("checked_resource_id");

                statusResponses = parsedResponses.get(hostName);
                if (statusResponses == null) {
                	// initiate if it hasn't been created yet
                    statusResponses = new ArrayList<ServiceStatusResponse>();
                }
                statusResponses.add(new ServiceStatusResponse(passedCheck, serviceName));
                // update entry for this host with this service status
                parsedResponses.put(hostName, statusResponses);
            }


        } catch (Exception ex) {
            throw new PortalServiceException("Unable to parse Google Cloud Monitoring response", ex);
        }

        return parsedResponses;
    }


    /**
     * Set a GoogleCredentials access token loaded with config from this service
     *
     * @return
     * @throws PortalServiceException
     */
	private HttpRequest setAuthorization(HttpRequest request) throws PortalServiceException {

		ServiceAccountCredentials.Builder creds = ServiceAccountCredentials.newBuilder();
		creds.setClientId(this.clientId);
		creds.setClientEmail(this.clientEmail);
		try {
			creds.setPrivateKey(createPrivateKey(this.privateKey));
		} catch (Exception e) {
			throw new PortalServiceException("Failed to read Google stackdriver private key from env.properties.");
		}
		creds.setPrivateKeyId(this.privateKeyId);
		creds.setProjectId(this.projectId);
		creds.setScopes(Collections.singletonList(SCOPE));
		creds.setTokenServerUri(URI.create(this.tokenUri));

		ServiceAccountCredentials credential = creds.build();
		AccessToken token = null;
		try {
			token = credential.refreshAccessToken();
		} catch (IOException e) {
			throw new PortalServiceException("Failed to get access token from Google stackdriver service account.");
		}

		// set access token in the request header
		HttpHeaders authorization = new HttpHeaders();
		authorization.setAuthorization("Bearer " + token.getTokenValue());
		return request.setHeaders(authorization);
	}
}
