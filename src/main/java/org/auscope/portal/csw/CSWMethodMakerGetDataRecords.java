package org.auscope.portal.csw;

import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: Mathew Wyatt
 * Date: 02/07/2009
 * @version $Id$
 */
public class CSWMethodMakerGetDataRecords {

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

    protected final Log log = LogFactory.getLog(getClass());
    private String serviceUrl;

    public CSWMethodMakerGetDataRecords(String serviceUrl) {
        //pretty hard to do a GetFeature query without a serviceURL, so we had better check that we have one
        if(serviceUrl == null || serviceUrl.isEmpty()) {
            throw new IllegalArgumentException("serviceUrl parameter can not be null or empty.");
        }

        this.serviceUrl = serviceUrl;
    }

    /**
     * Generates a method that performs a CSW GetRecords request for a maximum of 1000 records
     * @return
     * @throws Exception
     */
    public HttpMethodBase makeMethod() throws Exception {
        return this.makeMethod(null, ResultType.Results, 1000, 1);
    }

    /**
     * Generates a method that performs a CSW GetRecords request
     * with the specified filter
     *
     * @param filter [Optional] The filter to constrain our request
     * @return
     * @throws UnsupportedEncodingException If the PostMethod body cannot be encoded ISO-8859-1
     */
    public HttpMethodBase makeMethod(CSWGetDataRecordsFilter filter, ResultType resultType, int maxRecords) throws UnsupportedEncodingException {
        return this.makeMethod(filter, resultType, maxRecords, 1);
    }

    /**
     * Generates a method that performs a CSW GetRecords request
     * with the specified filter
     *
     * @param filter [Optional] The filter to constrain our request
     * @return
     * @throws UnsupportedEncodingException If the PostMethod body cannot be encoded ISO-8859-1
     */
    public HttpMethodBase makeMethod(CSWGetDataRecordsFilter filter, ResultType resultType, int maxRecords, int startPosition) throws UnsupportedEncodingException {
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
}
