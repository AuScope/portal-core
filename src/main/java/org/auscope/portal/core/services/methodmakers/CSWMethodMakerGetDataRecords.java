package org.auscope.portal.core.services.methodmakers;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.BasicHttpParams;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;

/**
 * Class for generating methods for interacting with a Coverage Service for the Web (CS/W)
 */
public class CSWMethodMakerGetDataRecords extends AbstractMethodMaker {

    /**
     * The different types of ways CSW records can be requested
     */
    public enum ResultType {
        /**
         * Get a full CSW response
         */
        Results,
        /**
         * The CSW response will contain only the header element with a number of records matched
         */
        Hits
    }

    private final Log log = LogFactory.getLog(getClass());

    /**
     * Generates a method that performs a CSW GetRecords request for a maximum of 1000 records
     * @return
     * @throws Exception
     */
    public HttpRequestBase makeMethod(String serviceUrl) throws Exception {
        return this.makeMethod(serviceUrl, null, ResultType.Results, 1000, 1);
    }

    /**
     * Generates a method that performs a CSW GetRecords request
     * with the specified filter
     *
     * @param filter [Optional] The filter to constrain our request
     * @return
     * @throws UnsupportedEncodingException If the PostMethod body cannot be encoded ISO-8859-1
     */
    public HttpRequestBase makeMethod(String serviceUrl, CSWGetDataRecordsFilter filter, ResultType resultType, int maxRecords) throws UnsupportedEncodingException {
        return this.makeMethod(serviceUrl, filter, resultType, maxRecords, 1);
    }

    /**
     * Generates a method that performs a CSW GetRecords request
     * with the specified filter
     *
     * @param filter [Optional] The filter to constrain our request
     * @return
     * @throws UnsupportedEncodingException If the PostMethod body cannot be encoded ISO-8859-1
     */
    public HttpRequestBase makeMethod(String serviceUrl, CSWGetDataRecordsFilter filter, ResultType resultType, int maxRecords, int startPosition) throws UnsupportedEncodingException {
        HttpPost httpMethod = new HttpPost(serviceUrl);

        String filterString = null;
        if (filter != null) {
            filterString = filter.getFilterStringAllRecords();
        }

        // We should be using a library for this call...
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" constraint_language_version=\"1.1.0\" outputFormat=\"application/xml\" outputSchema=\"csw:IsoRecord\" typeNames=\"csw:Record\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"");
        sb.append(String.format(" maxRecords=\"%1$s\"", maxRecords));
        if (resultType != null) {
            switch (resultType) {
            case Hits:
                sb.append(" resultType=\"hits\"");
                break;
            case Results:
                sb.append(" resultType=\"results\"");
                break;
            default:
                log.error("Request type invalid - sending unconstrained request");
                break;
            }
        }
        if (startPosition >= 0) {
            sb.append(" startPosition=\"" + startPosition + "\"");
        }
        sb.append(">");
        sb.append("<csw:Query typeNames=\"csw:Record\">");
        sb.append("<csw:ElementSetName>full</csw:ElementSetName>");
        if (filterString != null && filterString.length() > 0) {
            sb.append("<csw:Constraint version=\"1.1.0\">");
            sb.append(filterString);
            sb.append("</csw:Constraint>");
        }
        sb.append("</csw:Query>");
        sb.append("</csw:GetRecords>");

        log.trace("CSW GetRecords Request: " + sb.toString());

        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        httpMethod.setEntity(new StringEntity(sb.toString(),  ContentType.create("text/xml", Charset.defaultCharset())));
        return httpMethod;
    }

    /**
     * Generates a HTTP Get method that performs a CSW GetRecords request
     *
     * @return
     * @throws UnsupportedEncodingException If the PostMethod body cannot be encoded ISO-8859-1
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, ResultType resultType, int maxRecords, int startPosition) {
        HttpGet method = new HttpGet(serviceUrl);

        BasicHttpParams params = new BasicHttpParams();

        params.setParameter("service", "CSW");
        params.setParameter("constraint_language_version", "1.1.0");
        params.setParameter("request", "GetRecords");
        params.setParameter("outputSchema", "csw:IsoRecord");
        params.setParameter("typeNames", "csw:IsoRecord");
        params.setParameter("constraintLanguage", "FILTER");
        params.setParameter("namespace", "csw:http://www.opengis.net/cat/csw");
        params.setParameter("elementSetName", "full");
        params.setIntParameter("startPosition", startPosition);
        params.setIntParameter("maxRecords", maxRecords);

        if (resultType != null) {
            switch (resultType) {
            case Hits:
                params.setParameter("resultType", "hits");
                break;
            case Results:
                params.setParameter("resultType", "results");
                break;
            default:
                log.error("Request type invalid - sending unconstrained request");
                break;
            }
        }

        //attach params to the method
        method.setParams(params);

        log.debug(method.getMethod() + " query sent to GeoNetwork: \n\t" + serviceUrl + "?" + method.getURI().getQuery());

        return method;
    }
}
