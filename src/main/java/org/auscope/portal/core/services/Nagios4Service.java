package org.auscope.portal.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.Nagios4MethodMaker;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse.Status;

/**
 * Service class for accessing the REST API of a Nagios 4 instance
 * @author Josh Vote (CSIRO)
 *
 */
public class Nagios4Service {
    private HttpServiceCaller serviceCaller;
    private Nagios4MethodMaker methodMaker;
    private String serviceUrl;
    private String userName;
    private String password;

    public Nagios4Service(String serviceUrl, HttpServiceCaller serviceCaller, Nagios4MethodMaker methodMaker) {
        super();
        this.serviceUrl = serviceUrl;
        this.serviceCaller = serviceCaller;
        this.methodMaker = methodMaker;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Generates a CredentialsProvider loaded with config from this service (or null if none setup)
     * @return
     */
    protected CredentialsProvider generateCredentials() {
        if (userName == null || password == null) {
            return null;
        }
        BasicCredentialsProvider credentials = new BasicCredentialsProvider();
        credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
        return credentials;
    }

    /**
     * Gets a (possibly filtered) view of all services, keyed by their host name.
     * @param hostGroup If not null, results will be filtered to services belonging to this host group
     * @param hostName
     * @return
     * @throws PortalServiceException
     */
    public Map<String, List<ServiceStatusResponse>> getStatuses(String hostGroup) throws PortalServiceException {

        //Make our request
        HttpRequestBase method = null;
        String responseString = null;
        try {
            method = methodMaker.statusServiceListJSON(serviceUrl, hostGroup, null);
            responseString = serviceCaller.getMethodResponseAsString(method, generateCredentials());
        } catch (Exception ex) {
            throw new PortalServiceException(method, "Unable to access remote Nagios4 service", ex);
        }

        //Parse our response
        HashMap<String, List<ServiceStatusResponse>> parsedResponses = new HashMap<String, List<ServiceStatusResponse>>();
        try {
            JSONObject responseObj = JSONObject.fromObject(responseString);

            JSONObject result = responseObj.getJSONObject("result");
            if (!result.getString("type_text").equalsIgnoreCase("Success")) {
                String message = result.getString("message");
                throw new PortalServiceException("Response reports failure:" + result.getString("type_text") + " - " + message);
            }

            JSONObject data = responseObj.getJSONObject("data");
            JSONObject serviceList = data.getJSONObject("servicelist");
            for (Object hostName : serviceList.keySet()) {
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
            throw new PortalServiceException("Unable to parse Nagios 4 response", ex);
        }

        return parsedResponses;
    }
}
