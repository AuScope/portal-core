package org.auscope.portal.core.services.methodmakers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    public HttpMethodBase makeMethod(String serviceUrl) throws Exception {
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
    public HttpMethodBase makeMethod(String serviceUrl, CSWGetDataRecordsFilter filter, ResultType resultType, int maxRecords) throws UnsupportedEncodingException {
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
    public HttpMethodBase makeMethod(String serviceUrl, CSWGetDataRecordsFilter filter, ResultType resultType, int maxRecords, int startPosition) throws UnsupportedEncodingException {
        PostMethod httpMethod = new PostMethod(serviceUrl);

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
        httpMethod.setRequestEntity(new StringRequestEntity(sb.toString(),"text/xml", "ISO-8859-1"));

        return httpMethod;
    }

    /**
     * Generates a HTTP Get method that performs a CSW GetRecords request
     *
     * @return
     * @throws UnsupportedEncodingException If the PostMethod body cannot be encoded ISO-8859-1
     */
    public HttpMethodBase makeGetMethod(String serviceUrl, ResultType resultType, int maxRecords, int startPosition) throws UnsupportedEncodingException {
        GetMethod method = new GetMethod(serviceUrl);


        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new NameValuePair("service", "CSW"));
        params.add(new NameValuePair("constraint_language_version", "1.1.0"));
        params.add(new NameValuePair("request", "GetRecords"));
        params.add(new NameValuePair("outputSchema", "csw:IsoRecord"));
        params.add(new NameValuePair("typeNames", "csw:IsoRecord"));
        params.add(new NameValuePair("constraintLanguage", "FILTER"));
        params.add(new NameValuePair("namespace", "csw:http://www.opengis.net/cat/csw"));
        params.add(new NameValuePair("elementSetName", "full"));
        params.add(new NameValuePair("startPosition", Integer.toString(startPosition)));
        params.add(new NameValuePair("maxRecords", Integer.toString(maxRecords)));

        if (resultType != null) {
            switch (resultType) {
            case Hits:
                params.add(new NameValuePair("resultType", "hits"));
                break;
            case Results:
                params.add(new NameValuePair("resultType", "results"));
                break;
            default:
                log.error("Request type invalid - sending unconstrained request");
                break;
            }
        }

        //attach params to the method
        method.setQueryString(params.toArray(new NameValuePair[params.size()]));

        String queryStr = method.getName()
                        + " query sent to GeoNetwork: \n\t"
                        + serviceUrl + "?" + method.getQueryString();

        log.debug(queryStr);

        return method;
    }
}
