package org.auscope.portal.core.services.methodmakers;

import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

/**
 * Method maker for generating Nagios 4 JSON queries
 * @author Josh Vote (CSIRO)
 *
 */
public class Nagios4MethodMaker extends AbstractMethodMaker {
    /**
     * Generates a servicelist query for status information that can be optionally filtered
     * @param serviceUrl The base Nagios 4 URL
     * @param hostGroup If not null, will filter all services to those within the named host group
     * @param hostName If not null, will filter all services to those belonging to the named host
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase statusServiceListJSON(String serviceUrl, String hostGroup, String hostName) throws URISyntaxException {
        String queryUrl = urlPathConcat(serviceUrl, "cgi-bin/statusjson.cgi");

        URIBuilder builder = new URIBuilder(queryUrl);
        builder.addParameter("query", "servicelist");
        builder.addParameter("formatoptions", "enumerate");
        if (StringUtils.isNotEmpty(hostGroup)) {
            builder.addParameter("hostgroup", hostGroup);
        }
        if (StringUtils.isNotEmpty(hostName)) {
            builder.addParameter("hostname", hostName);
        }

        return new HttpGet(builder.build());
    }
}
