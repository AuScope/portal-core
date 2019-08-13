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
import org.auscope.portal.core.server.OgcServiceProviderType;
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
    public HttpRequestBase makeMethod(String serviceUrl, OgcServiceProviderType serverType) {
        return this.makeMethod(serviceUrl, null, ResultType.Results, 1000, 1, null, serverType);
    }

    /**
     * Generates a method that performs a CSW GetRecords request with the specified filter
     *
     * @param filter
     *            [Optional] The filter to constrain our request
     * @return
     */
    public HttpRequestBase makeMethod(String serviceUrl, CSWGetDataRecordsFilter filter, ResultType resultType,
            int maxRecords, OgcServiceProviderType serverType) {
        return this.makeMethod(serviceUrl, filter, resultType, maxRecords, 1, null, serverType);
    }

    private String getCSWQueryElement(OgcServiceProviderType serverType) {
        switch (serverType) {
        case PyCSW:
            return "<csw:Query typeNames=\"csw:Record\" >";
        default:
        	return 	"<csw:Query typeNames=\"gmd:MD_Metadata\"  xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" >";
        }
    	
    }
    
    private static String decorateFilterString(String filter, OgcServiceProviderType serverType) {
    	if (serverType == OgcServiceProviderType.GeoServer ) {
    		return filter.replace("<ogc:PropertyName>ows:BoundingBox</ogc:PropertyName>", "<ogc:PropertyName>BoundingBox</ogc:PropertyName>");
    	} 
    	if (serverType == OgcServiceProviderType.PyCSW ) {
    		return filter.replace("<gml:Envelope srsName=\"WGS:84\">", "<gml:Envelope srsName=\"urn:ogc:def:crs:OGC:1.3:CRS84\">");
    	} 
    	return filter;
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
            int maxRecords, int startPosition, String cqlText, OgcServiceProviderType serverType) {
        HttpPost httpMethod = new HttpPost(serviceUrl);

        String filterString = null;
        if (filter != null) {
            filterString = filter.getFilterStringAllRecords();
            if (filterString != null) 
            	filterString = decorateFilterString(filterString, serverType);
        }

        // We should be using a library for this call...
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" version=\"2.0.2\" outputFormat=\"application/xml\" outputSchema=\"http://www.isotc211.org/2005/gmd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"");
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
        sb.append(getCSWQueryElement(serverType));

        sb.append("<csw:ElementSetName>full</csw:ElementSetName>");

        boolean hasFilter = filterString != null && filterString.length() > 0;
        boolean hasCql = cqlText != null && cqlText.length() > 0;

        if (hasFilter || hasCql) {
            sb.append("<csw:Constraint version=\"1.1.0\">");
            if (hasFilter) {
            	log.debug("filterString=" + filterString);
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

        log.info("CSW GetRecords Request: " + sb.toString());
        log.info("CSW GetRecords Url: " + serviceUrl);
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
    public HttpRequestBase makeGetMethod(String serviceUrl, ResultType resultType, int maxRecords, int startPosition, OgcServiceProviderType serverType)
            throws URISyntaxException {
        HttpGet method = new HttpGet();

        URIBuilder builder = new URIBuilder(serviceUrl);
        
        builder.setParameter("service", "CSW");
        if (serverType != OgcServiceProviderType.PyCSW ) {
        	builder.setParameter("constraint_language_version", "1.1.0");
        }
        builder.setParameter("request", "GetRecords");
        builder.setParameter("version", "2.0.2");
        builder.setParameter("outputSchema", "http://www.isotc211.org/2005/gmd");
        builder.setParameter("typeNames", "gmd:MD_Metadata");
        builder.setParameter("constraintLanguage", "FILTER");
        builder.setParameter("elementSetName", "full");
        builder.setParameter("startPosition", Integer.toString(startPosition));
        builder.setParameter("maxRecords", Integer.toString(maxRecords));
        builder.setParameter("resultType", "results");

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

        //log.debug(queryStr);

        return method;
    }
}
