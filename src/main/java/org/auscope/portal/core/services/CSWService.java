package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformerFactory;
import org.auscope.portal.core.services.responses.ows.OWSException;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The CSWService class provides functionality to make requests to CSW service endpoints and parse their responses.
 * 
 * @author Adam
 *
 */
public class CSWService {
    private final Log log = LogFactory.getLog(getClass());

    private CSWMethodMakerGetDataRecords methodMaker;
    private CSWServiceItem endpoint;
    private HttpServiceCaller serviceCaller;
    private boolean forceGetMethods;
    private CSWRecordTransformerFactory transformerFactory;

    /**
     * Creates a new instance with a new CSWRecordTransformerFactory instance
     * 
     * @param endpoint
     * @param serviceCaller
     * @param forceGetMethods
     */
    public CSWService(CSWServiceItem endpoint, HttpServiceCaller serviceCaller, boolean forceGetMethods) {
        this(endpoint, serviceCaller, forceGetMethods, new CSWRecordTransformerFactory());
    }

    /**
     * Creates a new instance with a configurable CSWRecordTransformerFactory instance
     * 
     * @param endpoint
     * @param serviceCaller
     * @param forceGetMethods
     * @param transformerFactory
     */
    public CSWService(CSWServiceItem endpoint, HttpServiceCaller serviceCaller, boolean forceGetMethods,
            CSWRecordTransformerFactory transformerFactory) {
        this.endpoint = endpoint;
        this.serviceCaller = serviceCaller;
        this.forceGetMethods = forceGetMethods;
        this.methodMaker = new CSWMethodMakerGetDataRecords();
        this.transformerFactory = transformerFactory;
    }

    public CSWGetRecordResponse queryCSWEndpoint(int startPosition, int maxQueryLength, int maxNumberOfAttempts,
            long timeBetweenAttempts) throws IOException, OWSException {

        int numberOfAttempts=maxNumberOfAttempts;
        try {
            while (numberOfAttempts > 0) {
                return this.queryCSWEndpoint(startPosition, maxQueryLength, null);
            }
            //
            // This code should be unreachable as the catch clause should throw an exception before.
            //
            throw new IOException("CSWService#queryCSWEndpoint() aborted after " + maxNumberOfAttempts+" failed attempts");
        } catch (java.io.IOException e) {
            log.warn("Attempt to query CSW end point failed. Number of attempts left:" + --numberOfAttempts);
            if (numberOfAttempts > 0) {
                try {
                    Thread.sleep(timeBetweenAttempts);
                } catch (InterruptedException e1) {
                    log.info("queryCSWEndpoint interrupted. Abroting query");
                    return null;
                }
                return queryCSWEndpoint(startPosition, maxQueryLength, numberOfAttempts, timeBetweenAttempts);
            } else {
                throw e;
            }
        }

    }

    public CSWGetRecordResponse queryCSWEndpoint(int startPosition, int maxQueryLength, CSWGetDataRecordsFilter filter) throws IOException, OWSException {
        log.trace(String.format("%1$s - requesting startPosition %2$s", this.endpoint.getServiceUrl(), startPosition));

        String cswServiceUrl = this.endpoint.getServiceUrl();

        // Request our set of records
        HttpRequestBase method = null;
   
        // If cqlText is not null means we want to perform filter on the query
        if (this.forceGetMethods && this.endpoint.getCqlText() == null && filter == null) {
            try {
                method = this.methodMaker.makeGetMethod(cswServiceUrl, ResultType.Results, maxQueryLength, startPosition);
            } catch (URISyntaxException e) {
                throw new IOException(e.getMessage(), e);
            }
        } else {
            method = this.methodMaker.makeMethod(cswServiceUrl, filter, ResultType.Results, maxQueryLength,
                    startPosition, this.endpoint.getCqlText());
        }

        try (InputStream responseStream = this.serviceCaller.getMethodResponseAsStream(method)) {
            log.trace(String.format("%1$s - Response received", this.endpoint.getServiceUrl()));

            // Parse the response into newCache (remember that maps are NOT
            // thread safe)
            Document responseDocument = DOMUtil.buildDomFromStream(responseStream);
            OWSExceptionParser.checkForExceptionResponse(responseDocument);

            return new CSWGetRecordResponse(this.endpoint, responseDocument, transformerFactory);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}