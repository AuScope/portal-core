package org.auscope.portal.core.services.methodmakers;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.SortType;

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
     * 
     * @return
     */
    public HttpRequestBase makeMethod(String serviceUrl) {
        return this.makeMethod(serviceUrl, null, ResultType.Results, 1000, 1, null);
    }

    /**
     * Generates a method that performs a CSW GetRecords request with the specified filter
     *
     * @param filter
     *            [Optional] The filter to constrain our request
     * @return
     */
    public HttpRequestBase makeMethod(String serviceUrl, CSWGetDataRecordsFilter filter, ResultType resultType,
            int maxRecords) {
        return this.makeMethod(serviceUrl, filter, resultType, maxRecords, 1, null);
    }

    /**
     * Generates a method that performs a CSW GetRecords request with the specified filter
     *
     * @param filter
     *            [Optional] The filter to constrain our request
     * @return
     * @throws UnsupportedEncodingException
     *             If the PostMethod body cannot be encoded ISO-8859-1
     */
    public HttpRequestBase makeMethod(String serviceUrl, CSWGetDataRecordsFilter filter, ResultType resultType,
            int maxRecords, int startPosition, String cqlText) {
        HttpPost httpMethod = new HttpPost(serviceUrl);

        String filterString = null;
        if (filter != null) {
            filterString = filter.getFilterStringAllRecords();
        }

        // We should be using a library for this call...
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" version=\"2.0.2\" constraint_language_version=\"1.1.0\" outputFormat=\"application/xml\" outputSchema=\"http://www.isotc211.org/2005/gmd\" typeNames=\"gmd:MD_Metadata\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"");
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
        sb.append("<csw:Query typeNames=\"gmd:MD_Metadata\">");
        sb.append("<csw:ElementSetName>full</csw:ElementSetName>");

        boolean hasFilter = filterString != null && filterString.length() > 0;
        boolean hasCql = cqlText != null && cqlText.length() > 0;

        if (hasFilter || hasCql) {
            sb.append("<csw:Constraint version=\"1.1.0\">");
            if (hasFilter) {
                sb.append(filterString);
            }
            if (hasCql) {
                sb.append("<csw:CqlText>" + cqlText + "</csw:CqlText>");
            }
            sb.append("</csw:Constraint>");
        }
        
        if (filter != null) {
            SortType sortType = filter.getSortType();
            if (sortType != null && sortType != SortType.serviceDefault) {
                sb.append("<ogc:SortBy><ogc:SortProperty>");
                switch (sortType) {
                case title:
                    sb.append("<ogc:PropertyName>title</ogc:PropertyName>");
                    sb.append("<ogc:SortOrder>ASC</ogc:SortOrder>");
                    break;
                case publicationDate:
                    sb.append("<ogc:PropertyName>publicationDate</ogc:PropertyName>");
                    sb.append("<ogc:SortOrder>DESC</ogc:SortOrder>");
                    break;
                default:
                    break;
                }
                sb.append("</ogc:SortProperty> </ogc:SortBy>");
            }
        }
        
        sb.append("</csw:Query>");
        sb.append("</csw:GetRecords>");

        log.trace("CSW GetRecords Request: " + sb.toString());

        // If this does not work, try params: "text/xml; charset=ISO-8859-1"
        httpMethod.setEntity(new StringEntity(sb.toString(), ContentType.create("text/xml", "ISO-8859-1")));

        return httpMethod;
    }

    /**
     * Generates a HTTP Get method that performs a CSW GetRecords request
     *
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeGetMethod(String serviceUrl, ResultType resultType, int maxRecords, int startPosition)
            throws URISyntaxException {
        HttpGet method = new HttpGet();

        URIBuilder builder = new URIBuilder(serviceUrl);

        builder.setParameter("service", "CSW");
        builder.setParameter("constraint_language_version", "1.1.0");
        builder.setParameter("request", "GetRecords");
        builder.setParameter("version", "2.0.2");
        builder.setParameter("outputSchema", "csw:IsoRecord");
        builder.setParameter("typeNames", "gmd:MD_Metadata");
        builder.setParameter("constraintLanguage", "FILTER");
        builder.setParameter("namespace", "csw:http://www.opengis.net/cat/csw");
        builder.setParameter("elementSetName", "full");
        builder.setParameter("startPosition", Integer.toString(startPosition));
        builder.setParameter("maxRecords", Integer.toString(maxRecords));

        if (resultType != null) {
            switch (resultType) {
            case Hits:
                builder.setParameter("resultType", "hits");
                break;
            case Results:
                builder.setParameter("resultType", "results");
                break;
            default:
                log.error("Request type invalid - sending unconstrained request");
                break;
            }
        }

        //attach params to the method
        method.setURI(builder.build());

        String queryStr = method.getURI().getHost()
                + " query sent to GeoNetwork: \n\t"
                + serviceUrl + "?" + method.getURI().getQuery();

        log.debug(queryStr);

        return method;
    }
}
