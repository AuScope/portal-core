package org.auscope.portal.core.services.methodmakers;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.auscope.portal.core.util.HttpUtil;

import java.net.URISyntaxException;
import java.util.List;

public class CSWMethodMaker extends AbstractMethodMaker {

    /**
     *
     * @param cswUrl
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getCapabilities(String cswUrl) throws URISyntaxException {
        List<NameValuePair> existingParam = this.extractQueryParams(cswUrl);

        existingParam.add(new BasicNameValuePair("service", "CSW"));
        existingParam.add(new BasicNameValuePair("request", "GetCapabilities"));
        existingParam.add(new BasicNameValuePair("version", "2.0.2"));
        HttpGet method = new HttpGet();
        method.setURI(HttpUtil.parseURI(cswUrl, existingParam));

        return method;
    }

    /**
     *
     * @param cswUrl
     * @param propertyName
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase getDomain(String cswUrl, String propertyName) throws URISyntaxException {
        List<NameValuePair> existingParam = this.extractQueryParams(cswUrl);

        existingParam.add(new BasicNameValuePair("service", "CSW"));
        existingParam.add(new BasicNameValuePair("request", "GetDomain"));
        existingParam.add(new BasicNameValuePair("version", "2.0.2"));
        existingParam.add(new BasicNameValuePair("propertyName", propertyName));

        HttpGet method = new HttpGet();
        method.setURI(HttpUtil.parseURI(cswUrl, existingParam));

        return method;
    }
}
