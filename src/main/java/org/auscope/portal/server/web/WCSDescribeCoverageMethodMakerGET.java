package org.auscope.portal.server.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

@Repository
public class WCSDescribeCoverageMethodMakerGET implements
        IWCSDescribeCoverageMethodMaker {

    private final Log logger = LogFactory.getLog(getClass());

    @Override
    public HttpMethodBase makeMethod(String serviceUrl, String layerName) throws Exception {
        GetMethod httpMethod = new GetMethod(serviceUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        //Do some simple error checking to align with WCS standard
        if (serviceUrl == null || serviceUrl.isEmpty())
            throw new IllegalArgumentException("You must specify a serviceUrl");
        if (layerName == null || layerName.isEmpty())
            throw new IllegalArgumentException("You must specify a layerName");

        params.add(new NameValuePair("service", "WCS"));
        params.add(new NameValuePair("version", "1.0.0"));
        params.add(new NameValuePair("request", "DescribeCoverage"));
        params.add(new NameValuePair("coverage", layerName));

        httpMethod.setQueryString(params.toArray(new NameValuePair[params.size()]));

        logger.debug(String.format("url='%1$s' query='%2$s'", serviceUrl, httpMethod.getQueryString()));

        return httpMethod;
    }

}
