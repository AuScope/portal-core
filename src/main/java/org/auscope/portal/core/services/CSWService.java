package org.auscope.portal.core.services;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;

/**
 * The CSWService class provides functionality to 
 * make requests to CSW service endpoints and parse their
 * responses.
 * @author Adam
 *
 */
public class CSWService {
    private final Log log = LogFactory.getLog(getClass());
    
    private CSWMethodMakerGetDataRecords methodMaker;
    private CSWServiceItem endpoint;
    private HttpServiceCaller serviceCaller;
    private boolean forceGetMethods;
    
    public CSWService(CSWServiceItem endpoint, HttpServiceCaller serviceCaller, boolean forceGetMethods) {
        this.endpoint = endpoint;
        this.serviceCaller = serviceCaller; 
        this.forceGetMethods = forceGetMethods;
        
        this.methodMaker = new CSWMethodMakerGetDataRecords();
    }
    
    public CSWGetRecordResponse queryCSWEndpoint(int startPosition, int maxQueryLength) throws Exception {
        return this.queryCSWEndpoint(startPosition, maxQueryLength, null);
    }
    
    public CSWGetRecordResponse queryCSWEndpoint(int startPosition, int maxQueryLength, CSWGetDataRecordsFilter filter) throws Exception {
        log.trace(String.format("%1$s - requesting startPosition %2$s", this.endpoint.getServiceUrl(), startPosition));

        String cswServiceUrl = this.endpoint.getServiceUrl();
        
        // Request our set of records
        HttpMethodBase method = null;
        
        // If cqlText is not null means we want to perform filter on the query
        if (this.forceGetMethods && this.endpoint.getCqlText() == null && filter == null) {
            method = this.methodMaker.makeGetMethod(cswServiceUrl, ResultType.Results, maxQueryLength, startPosition);
        } else {
            method = this.methodMaker.makeMethod(cswServiceUrl, filter, ResultType.Results, maxQueryLength, startPosition, this.endpoint.getCqlText());
        }

        InputStream responseStream = this.serviceCaller.getMethodResponseAsStream(method);

        log.trace(String.format("%1$s - Response received", this.endpoint.getServiceUrl()));

        // Parse the response into newCache (remember that maps are NOT thread safe)
        Document responseDocument = DOMUtil.buildDomFromStream(responseStream);
        OWSExceptionParser.checkForExceptionResponse(responseDocument);
        return new CSWGetRecordResponse(this.endpoint, responseDocument);
    }
}