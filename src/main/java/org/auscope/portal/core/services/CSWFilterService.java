package org.auscope.portal.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.auscope.portal.core.server.http.DistributedHTTPServiceCaller;
import org.auscope.portal.core.server.http.DistributedHTTPServiceCallerException;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.custom.CustomRegistryInt;
import org.auscope.portal.core.services.methodmakers.CSWMethodMaker;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords;
import org.auscope.portal.core.services.methodmakers.CSWMethodMakerGetDataRecords.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.responses.csw.CSWGetCapabilities;
import org.auscope.portal.core.services.responses.csw.CSWGetDomainResponse;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformerFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Provides methods for accessing filtered data from multiple CSW services
 *
 * @author Josh Vote
 */
public class CSWFilterService {

    private final Log log = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;
    private Executor executor;
    private CSWServiceItem[] cswServiceList;
    private CSWRecordTransformerFactory transformerFactory;

    /**
     * Creates a new instance of a CSWFilterService. This constructor is normally autowired by the spring framework.
     *
     * @param executor
     *            A thread executor that will be used to manage multiple simultaneous CSW requests
     * @param serviceCaller
     *            Will be involved in actually making a HTTP request
     * @param cswServiceList
     *            Must be an untyped array of CSWServiceItem objects (for bean autowiring) representing CSW URL endpoints
     */
    public CSWFilterService(Executor executor,
            HttpServiceCaller serviceCaller,
            @SuppressWarnings("rawtypes") ArrayList cswServiceList) {
        this(executor, serviceCaller, cswServiceList, new CSWRecordTransformerFactory());
    }

    /**
     * Creates a new instance of a CSWFilterService. This constructor is normally autowired by the spring framework.
     *
     * @param executor
     *            A thread executor that will be used to manage multiple simultaneous CSW requests
     * @param serviceCaller
     *            Will be involved in actually making a HTTP request
     * @param cswServiceList
     *            Must be an untyped array of CSWServiceItem objects (for bean autowiring) representing CSW URL endpoints
     */
    public CSWFilterService(Executor executor,
            HttpServiceCaller serviceCaller,
            @SuppressWarnings("rawtypes") ArrayList cswServiceList,
            CSWRecordTransformerFactory transformerFactory) {
        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.cswServiceList = new CSWServiceItem[cswServiceList.size()];
        this.transformerFactory = transformerFactory;
        for (int i = 0; i < cswServiceList.size(); i++) {
            this.cswServiceList[i] = (CSWServiceItem) cswServiceList.get(i);
        }
    }

    /**
     * Makes a CSW request to the specified service
     * 
     * @param serviceItem
     *            The CSW service to call
     * @param filter
     *            An optional filter to apply to each of the subset requests
     * @param maxRecords
     *            The max records PER SERVICE that will be requested
     * @param resultType
     *            The type of response that is required from the CSW
     * @param startIndex
     *            The first record index to start filtering from (for pagination). Set to 1 for the first record
     * @return
     */
    private CSWGetRecordResponse callSingleService(CSWServiceItem serviceItem, CSWGetDataRecordsFilter filter,
            int maxRecords, int startIndex, ResultType resultType) throws PortalServiceException {
        log.trace(String.format("serviceItem='%1$s' maxRecords=%2$s resultType='%3$s' filter='%4$s'", serviceItem,
                maxRecords, resultType, filter));
        CSWMethodMakerGetDataRecords methodMaker = new CSWMethodMakerGetDataRecords();
        HttpRequestBase method = methodMaker.makeMethod(serviceItem.getServiceUrl(), filter, resultType, maxRecords, startIndex,
                    null, serviceItem.getServerType());

        try (InputStream responseStream = serviceCaller.getMethodResponseAsStream(method)) {
            Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
            return new CSWGetRecordResponse(serviceItem, responseDoc, transformerFactory);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Generates a DistributedHTTPServiceCaller initialised to each and every serviceUrl in the cswServiceList and begins making CSW requests to each of them.
     *
     * The DistributedHTTPServiceCaller will be given 'Additional Information' in the form of CSWServiceItem objects
     *
     * @param filter
     *            An optional filter to apply to each of the subset requests
     * @param maxRecords
     *            The max records PER SERVICE that will be requested
     * @param resultType
     *            The type of response that is required from the CSW
     * @param startIndex
     *            The first record index to start filtering from (for pagination). Set to 1 for the first record
     * @return
     */
    private DistributedHTTPServiceCaller callAllServices(CSWGetDataRecordsFilter filter, int maxRecords,
            int startIndex, ResultType resultType) throws DistributedHTTPServiceCallerException {
        List<HttpRequestBase> requestMethods = new ArrayList<>();
        List<Object> additionalInfo = new ArrayList<>();

        //Create various HTTP Methods for making each and every CSW request
        for (CSWServiceItem serviceItem : cswServiceList) {
            log.trace(String.format("serviceItem='%1$s' maxRecords=%2$s resultType='%3$s' filter='%4$s'",
                    serviceItem, maxRecords, resultType, filter));
            CSWMethodMakerGetDataRecords methodMaker = new CSWMethodMakerGetDataRecords();
            requestMethods.add(methodMaker.makeMethod(serviceItem.getServiceUrl(), filter, resultType, maxRecords,
                    startIndex, null, serviceItem.getServerType()));
            additionalInfo.add(serviceItem);
        }

        DistributedHTTPServiceCaller dsc = new DistributedHTTPServiceCaller(requestMethods, additionalInfo,
                serviceCaller);
        dsc.beginCallingServices(executor);

        return dsc;
    }

    /**
     * Returns the list of internal CSWServiceItems that powers this service
     * 
     * @return
     */
    public CSWServiceItem[] getCSWServiceItems() {
        return Arrays.copyOf(this.cswServiceList, this.cswServiceList.length);
    }

    /**
     * Makes a request to each and every CSW service (on seperate threads) before parsing the responses and collating them into a response array.
     *
     * Any exceptions thrown by the service calls (on seperate threads) will be rethrown as DistributedHTTPServiceCallerException
     *
     * Any exceptions generated during the parsing of a CSWResponse will be thrown as per normal
     *
     * @param filter
     *            An optional filter to apply to each of the subset requests
     * @param maxRecords
     *            The max records PER SERVICE that will be requested
     * @throws DistributedHTTPServiceCallerException
     *             If an underlying service call returns an exception
     * @return
     */
    public CSWGetRecordResponse[] getFilteredRecords(CSWGetDataRecordsFilter filter, int maxRecords)
            throws PortalServiceException {
        List<CSWGetRecordResponse> responses = new ArrayList<>();

        //Call our services and start iterating the responses
        DistributedHTTPServiceCaller dsc = callAllServices(filter, maxRecords, 1, ResultType.Results);
        while (dsc.hasNext()) {
            try (InputStream responseStream = dsc.next()) {
                CSWServiceItem origin = (CSWServiceItem) dsc.getLastAdditionalInformation();
                Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
                responses.add(new CSWGetRecordResponse(origin, responseDoc, transformerFactory));
            } catch (Exception ex) {
                throw new PortalServiceException("Error parsing response document", ex);
            }
        }
        return responses.toArray(new CSWGetRecordResponse[responses.size()]);
    }

    /**
     * Makes a request to the specified CSW service (on this thread) before parsing and returning the response
     *
     * If serviceId does not match an existing CSWService an exception will be thrown
     *
     * @param filter
     *            An optional filter to apply to each of the subset requests
     * @param maxRecords
     *            The max records PER SERVICE that will be requested
     * @param startPosition
     *            1 based index to begin searching from
     * @return
     * @throws PortalServiceException 
     */
    public CSWGetRecordResponse getFilteredRecords(String serviceId, CSWGetDataRecordsFilter filter, int maxRecords,
            int startPosition) throws PortalServiceException {
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
     * Makes a request to the specified CSW service (on this thread) before parsing and returning the response
     *
     * If serviceId does not match an existing CSWService an exception will be thrown
     *
     * @param filter
     *            An optional filter to apply to each of the subset requests
     * @param maxRecords
     *            The max records PER SERVICE that will be requested
     * @param startPosition
     *            1 based index to begin searching from
     * @return
     * @throws PortalServiceException 
     */
    public CSWGetRecordResponse getFilteredRecords(CustomRegistryInt registry, CSWGetDataRecordsFilter filter,
            int maxRecords, int startPosition) throws PortalServiceException {
        if (registry == null) {
            throw new IllegalArgumentException(String.format("CustomRegistry required"));
        }
        //Lookup the service to call
        CSWServiceItem cswServiceItem = new CSWServiceItem(registry.getId(), registry.getServiceUrl(),
                registry.getRecordInformationUrl(), registry.getTitle());

        return callSingleService(cswServiceItem, filter, maxRecords, startPosition, ResultType.Results);
    }

    /**
     * Makes a request to each and every CSW service (on seperate threads) before parsing the responses and returning the total count of records that will match
     * the specified filter.
     *
     * Any exceptions thrown by the service calls (on seperate threads) will be rethrown as DistributedHTTPServiceCallerException
     *
     * Any exceptions generated during the parsing of a CSWResponse will be thrown as per normal
     *
     * @param filter
     *            An optional filter to apply to each of the subset requests
     * @param maxRecords
     *            The max records PER SERVICE that will be requested
     * @throws DistributedHTTPServiceCallerException
     *             If an underlying service call returns an exception
     * @return
     * @throws IOException 
     */
    public int getFilteredRecordsCount(CSWGetDataRecordsFilter filter, int maxRecords) throws DistributedHTTPServiceCallerException, IOException {
        int count = 0;

        //Call our services and start iterating the responses
        DistributedHTTPServiceCaller dsc = callAllServices(filter, maxRecords, 1, ResultType.Hits);
        while (dsc.hasNext()) {
            try (InputStream responseStream = dsc.next()) {
                CSWServiceItem origin = (CSWServiceItem) dsc.getLastAdditionalInformation();
                Document responseDoc = DOMUtil.buildDomFromStream(responseStream);
                CSWGetRecordResponse response = new CSWGetRecordResponse(origin, responseDoc, transformerFactory);
                count += response.getRecordsMatched();
            } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        return count;
    }

    /**
     * Makes a request to the specified CSW service (on this thread) before parsing and returning the response
     *
     * If serviceId does not match an existing CSWService an exception will be thrown
     *
     * @param filter
     *            An optional filter to apply to each of the subset requests
     * @param maxRecords
     *            The max records PER SERVICE that will be requested
     * @return
     * @throws PortalServiceException 
     */
    public int getFilteredRecordsCount(String serviceId, CSWGetDataRecordsFilter filter, int maxRecords) throws PortalServiceException {
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

    public CSWGetCapabilities getCapabilities(String cswServiceUrl) throws URISyntaxException, IOException {
        CSWGetCapabilities getCap = null;
        HttpGet method = new HttpGet(cswServiceUrl);
        URIBuilder builder = new URIBuilder(cswServiceUrl);
        // test
        // request=GetCapabilities&service=CSW&acceptVersions=2.0.2&acceptFormats=application%2Fxml
        builder.addParameter("request", "GetCapabilities");
        builder.addParameter("service", "CSW");
        builder.addParameter("acceptVersions", "2.0.2");
        builder.addParameter("acceptFormats", "application/xml");
        method.setURI(builder.build());
        getCap = new CSWGetCapabilities(this.serviceCaller.getMethodResponseAsStream(method));
        return getCap;
    }

    public CSWGetCapabilities getCapabilitiesByServiceId(String serviceId) throws IllegalArgumentException, IOException, URISyntaxException {
        CSWServiceItem serviceItem = getServiceItemById(serviceId);
        return getCapabilities(serviceItem.getServiceUrl());
    }

    public CSWGetDomainResponse getDomainResponse(String serviceId, String propertyName) throws URISyntaxException, IOException {
        CSWGetDomainResponse getDomain = null;

        CSWServiceItem serviceItem = getServiceItemById(serviceId);

        String serviceUrl = serviceItem.getServiceUrl();

        CSWMethodMaker methodMaker = new CSWMethodMaker();
        HttpRequestBase method = methodMaker.getDomain(serviceUrl,propertyName);

        getDomain = new CSWGetDomainResponse(this.serviceCaller.getMethodResponseAsStream(method));
        return getDomain;

    }

    private CSWServiceItem getServiceItemById(String serviceId) {
        CSWServiceItem serviceItem = null;
        for (CSWServiceItem cswServiceItem: cswServiceList) {
            if (cswServiceItem.getId().equals(serviceId)) {
                serviceItem = cswServiceItem;
            }

        }
        if (serviceItem == null) {
            throw new IllegalArgumentException(String.format("serviceId '%1$s' does not exist", serviceId));
        }
        return serviceItem;
    }
}
