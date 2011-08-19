package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.csw.CSWGetRecordResponse;
import org.auscope.portal.csw.CSWMethodMakerGetDataRecords;
import org.auscope.portal.csw.CSWMethodMakerGetDataRecords.ResultType;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.web.DistributedHTTPServiceCaller;
import org.auscope.portal.server.web.DistributedHTTPServiceCallerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;


/**
 * Provides methods for accessing filtered data from multiple CSW services
 *
 * @author Josh Vote
 */
@Service
public class CSWFilterService {

    protected final Log log = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;
    private CSWThreadExecutor executor;
    private CSWServiceItem[] cswServiceList;

    /**
     * Creates a new instance of a CSWFilterService. This constructor is normally autowired
     * by the spring framework.
     *
     * @param executor A thread executor that will be used to manage multiple simultaneous CSW requests
     * @param serviceCaller Will be involved in actually making a HTTP request
     * @param cswServiceList Must be an untyped array of CSWServiceItem objects (for bean autowiring) representing CSW URL endpoints
     * @throws Exception
     */
    @Autowired
    public CSWFilterService(CSWThreadExecutor executor,
                      HttpServiceCaller serviceCaller,
                      @Qualifier(value = "cswServiceList") ArrayList cswServiceList) throws Exception {

        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.cswServiceList = new CSWServiceItem[cswServiceList.size()];
        for (int i = 0; i < cswServiceList.size(); i++) {
            this.cswServiceList[i] = (CSWServiceItem) cswServiceList.get(i);
        }
    }

    /**
     * Makes a CSW request to the specified service
     * @param serviceItem The CSW service to call
     * @param filter An optional filter to apply to each of the subset requests
     * @param maxRecords The max records PER SERVICE that will be requested
     * @param resultType The type of response that is required from the CSW
     * @param startIndex The first record index to start filtering from (for pagination). Set to 1 for the first record
     * @return
     * @throws Exception
     */
    private CSWGetRecordResponse callSingleService(CSWServiceItem serviceItem, CSWGetDataRecordsFilter filter, int maxRecords, int startIndex, ResultType resultType) throws Exception {
        log.trace(String.format("serviceItem='%1$s' maxRecords=%2$s resultType='%3$s' filter='%4$s'", serviceItem, maxRecords, resultType, filter));
        CSWMethodMakerGetDataRecords methodMaker = new CSWMethodMakerGetDataRecords(serviceItem.getServiceUrl());
        HttpMethodBase method = methodMaker.makeMethod(filter, resultType, maxRecords, startIndex);

        InputStream responseStream = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());
        Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
        return new CSWGetRecordResponse(serviceItem, responseDoc);
    }

    /**
     * Generates a DistributedHTTPServiceCaller initialised to each and every
     * serviceUrl in the cswServiceList and begins making CSW requests to each of them.
     *
     * The DistributedHTTPServiceCaller will be given 'Additional Information' in the form of CSWServiceItem objects
     *
     * @param filter An optional filter to apply to each of the subset requests
     * @param maxRecords The max records PER SERVICE that will be requested
     * @param resultType The type of response that is required from the CSW
     * @param startIndex The first record index to start filtering from (for pagination). Set to 1 for the first record
     * @return
     */
    private DistributedHTTPServiceCaller callAllServices(CSWGetDataRecordsFilter filter, int maxRecords, int startIndex, ResultType resultType) throws DistributedHTTPServiceCallerException {
        List<HttpMethodBase> requestMethods = new ArrayList<HttpMethodBase>();
        List<Object> additionalInfo = new ArrayList<Object>();

        //Create various HTTP Methods for making each and every CSW request
        for (CSWServiceItem serviceItem : cswServiceList) {
            try {
                log.trace(String.format("serviceItem='%1$s' maxRecords=%2$s resultType='%3$s' filter='%4$s'", serviceItem, maxRecords, resultType, filter));
                CSWMethodMakerGetDataRecords methodMaker = new CSWMethodMakerGetDataRecords(serviceItem.getServiceUrl());
                requestMethods.add(methodMaker.makeMethod(filter, resultType, maxRecords, startIndex));
                additionalInfo.add(serviceItem);
            } catch (UnsupportedEncodingException ex) {
                log.warn(String.format("Error generating HTTP method for serviceItem '%1$s'",serviceItem), ex);
            }
        }

        DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(requestMethods, additionalInfo, serviceCaller);
        dsc.beginCallingServices(executor);

        return dsc;
    }

    /**
     * Returns the list of internal CSWServiceItems that powers this service
     * @return
     */
    public CSWServiceItem[] getCSWServiceItems() {
        return Arrays.copyOf(this.cswServiceList, this.cswServiceList.length);
    }

    /**
     * Makes a request to each and every CSW service (on seperate threads) before parsing the responses
     * and collating them into a response array.
     *
     * Any exceptions thrown by the service calls (on seperate threads) will be rethrown as DistributedHTTPServiceCallerException
     *
     * Any exceptions generated during the parsing of a CSWResponse will be thrown as per normal
     *
     * @param filter An optional filter to apply to each of the subset requests
     * @param maxRecords The max records PER SERVICE that will be requested
     * @throws DistributedHTTPServiceCallerException If an underlying service call returns an exception
     * @return
     */
    public CSWGetRecordResponse[] getFilteredRecords(CSWGetDataRecordsFilter filter, int maxRecords) throws Exception {
        List<CSWGetRecordResponse> responses = new ArrayList<CSWGetRecordResponse>();

        //Call our services and start iterating the responses
        DistributedHTTPServiceCaller dsc = callAllServices(filter, maxRecords, 1, ResultType.Results);
        while (dsc.hasNext()) {
            InputStream responseStream = dsc.next();
            CSWServiceItem origin = (CSWServiceItem) dsc.getLastAdditionalInformation();
            Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
            responses.add(new CSWGetRecordResponse(origin, responseDoc));
        }

        return responses.toArray(new CSWGetRecordResponse[responses.size()]);
    }

    /**
     * Makes a request to the specified CSW service (on this thread) before parsing and returning the response
     *
     * If serviceId does not match an existing CSWService an exception will be thrown
     *
     * @param filter An optional filter to apply to each of the subset requests
     * @param maxRecords The max records PER SERVICE that will be requested
     * @param startPosition 1 based index to begin searching from
     * @return
     */
    public CSWGetRecordResponse getFilteredRecords(String serviceId, CSWGetDataRecordsFilter filter, int maxRecords, int startPosition) throws Exception {
        //Lookup the service to call
        CSWServiceItem cswServiceItem = null;
        for (CSWServiceItem serviceItem : cswServiceList) {
            if (serviceItem.equals(serviceId)) {
                cswServiceItem = serviceItem;
            }
        }
        if (cswServiceItem == null) {
            throw new IllegalArgumentException(String.format("serviceId '%1$s' DNE", serviceId));
        }

        return callSingleService(cswServiceItem, filter, maxRecords, startPosition, ResultType.Results);
    }

    /**
     * Makes a request to each and every CSW service (on seperate threads) before parsing the responses
     * and returning the total count of records that will match the specified filter.
     *
     * Any exceptions thrown by the service calls (on seperate threads) will be rethrown as DistributedHTTPServiceCallerException
     *
     * Any exceptions generated during the parsing of a CSWResponse will be thrown as per normal
     *
     * @param filter An optional filter to apply to each of the subset requests
     * @param maxRecords The max records PER SERVICE that will be requested
     * @throws DistributedHTTPServiceCallerException If an underlying service call returns an exception
     * @return
     */
    public int getFilteredRecordsCount(CSWGetDataRecordsFilter filter, int maxRecords) throws Exception {
        int count = 0;

        //Call our services and start iterating the responses
        DistributedHTTPServiceCaller dsc = callAllServices(filter, maxRecords, 1, ResultType.Hits);
        while (dsc.hasNext()) {
            InputStream responseStream = dsc.next();
            CSWServiceItem origin = (CSWServiceItem) dsc.getLastAdditionalInformation();
            Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
            CSWGetRecordResponse response = new CSWGetRecordResponse(origin, responseDoc);

            count += response.getRecordsMatched();
        }

        return count;
    }

    /**
     * Makes a request to the specified CSW service (on this thread) before parsing and returning the response
     *
     * If serviceId does not match an existing CSWService an exception will be thrown
     *
     * @param filter An optional filter to apply to each of the subset requests
     * @param maxRecords The max records PER SERVICE that will be requested
     * @return
     */
    public int getFilteredRecordsCount(String serviceId, CSWGetDataRecordsFilter filter, int maxRecords) throws Exception {
        //Lookup the service to call
        CSWServiceItem cswServiceItem = null;
        for (CSWServiceItem serviceItem : cswServiceList) {
            if (serviceItem.equals(serviceId)) {
                cswServiceItem = serviceItem;
            }
        }
        if (cswServiceItem == null) {
            throw new IllegalArgumentException(String.format("serviceId '%1$s' DNE", serviceId));
        }

        CSWGetRecordResponse response = callSingleService(cswServiceItem, filter, maxRecords, 1, ResultType.Hits);
        return response.getRecordsMatched();
    }
}
