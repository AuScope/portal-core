package org.auscope.portal.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.SearchFacet;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.search.FacetedMultiSearchResponse;
import org.auscope.portal.core.services.responses.search.FacetedSearchResponse;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An wrapper around CSWFilterService that allows local/client side
 * filtering at the expense of a more limited API (no pagination)
 * and slower performance.
 * @author Josh Vote (CSIRO)
 *
 */
@Service
public class LocalCSWFilterService {

    public static final int DEFAULT_PAGE_SIZE = 100;

    private CSWFilterService filterService;
    private final Log log = LogFactory.getLog(getClass());
    private int pageSize = DEFAULT_PAGE_SIZE;
    private Executor executor;

    @Autowired
    public LocalCSWFilterService(CSWFilterService filterService, Executor executor) {
        this.filterService = filterService;
        this.executor = executor;
    }

    /**
     * The amount of records requested from a CSW in a batch for filtering operations. This is always static
     * as local filtering means that we can't ever know how many we'll need. Defaults to DEFAULT_PAGE_SIZE
     * @param pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * The amount of records requested from a CSW in a batch for filtering operations. This is always static
     * as local filtering means that we can't ever know how many we'll need. Defaults to DEFAULT_PAGE_SIZE
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Iterates facets and for each item either a) Adds the facet to remoteFilter or b) returns the facet in the return list for use
     * in a local filter later.
     * @param facets
     * @param remoteFilter
     * @return
     */
    private List<SearchFacet<? extends Object>> seperateFacets(List<SearchFacet<? extends Object>> facets, CSWGetDataRecordsFilter remoteFilter) {
        List<SearchFacet<? extends Object>> localFacets = new ArrayList<SearchFacet<? extends Object>>();
        for (SearchFacet<? extends Object> facet : facets) {
            switch(facet.getField()) {
            case "anytext":
                remoteFilter.setAnyText((String)facet.getValue());
                break;
            case "bbox":
                remoteFilter.setSpatialBounds((FilterBoundingBox)facet.getValue());
                break;
            case "keyword":
                String kw = (String)facet.getValue();
                String[] keywords = ArrayUtils.add(remoteFilter.getKeywords(), kw);
                remoteFilter.setKeywords(keywords);
                remoteFilter.setKeywordMatchType(KeywordMatchType.All);
                break;
            case "datefrom":
                remoteFilter.setModifiedDateFrom((DateTime)facet.getValue());
                break;
            case "dateto":
                remoteFilter.setModifiedDateTo((DateTime)facet.getValue());
                break;
            default:
                localFacets.add(facet);
                break;
            }
        }
        return localFacets;
    }

    /**
     * Tests a CSWRecord against a given search facet
     * @param record
     * @param facet
     * @return true if the record passes the facet.
     */
    private boolean recordPassesSearchFacet(CSWRecord record, SearchFacet<? extends Object> facet) {
        switch(facet.getField()) {
        case "servicetype":
            OnlineResourceType type = (OnlineResourceType) facet.getValue();
            return record.getOnlineResourcesByType(type).length > 0;
        default:
            log.error("Unable to local filter on field: " + facet.getField());
            return false;
        }
    }

    /**
     * Enumerates CSWRecords from source testing each against the search facets. Any passing record is copied to sink.
     * @param source
     * @param sink
     * @param facets
     * @param maxRecords At most this many records will be copied
     * @return The 0 based index of the LAST examined record from source or -1
     */
    private int performLocalFilter(List<CSWRecord> source, List<CSWRecord> sink, List<SearchFacet<? extends Object>> facets, int maxRecords) {
        int lastIndex = -1;
        int recordsCopied = 0;

        for (int i = 0; i < source.size(); i++) {
            CSWRecord record = source.get(i);
            lastIndex = i;
            boolean allPassing = true;
            for (SearchFacet<? extends Object> facet : facets) {
                if (!recordPassesSearchFacet(record, facet)) {
                    allPassing = false;
                    break;
                }
            }

            if (allPassing) {
                sink.add(record);
                if (++recordsCopied >= maxRecords) {
                    break;
                }
            }
        }

        return lastIndex;
    }

    /**
     * Given a specific service to query with a set of local/remote search facets. Perform a full filter until maxRecords are received or the remote CSW runs out of records. This can result
     * in many calls to the remote CSW if the local portion of the filter is quite specific and the remote portion of very permissive.
     * @param serviceId
     * @param serviceItem the CSWServiceItem for a custom registry (null if registered registry ID is supplied)
     * @param facets
     * @param startIndex
     * @param maxRecords
     * @return
     * @throws PortalServiceException
     */
    public FacetedSearchResponse getFilteredRecords(String serviceId, CSWServiceItem serviceItem, List<SearchFacet<? extends Object>> facets, int startIndex, int maxRecords) throws PortalServiceException {
        // Build our remote filter and keep track of what facets need to be done locally
        CSWGetDataRecordsFilter remoteFilter = new CSWGetDataRecordsFilter();
        List<SearchFacet<? extends Object>> localFacets = seperateFacets(facets, remoteFilter);

        // Keep getting pages of data until we serve up enough records
        FacetedSearchResponse result = new FacetedSearchResponse();
        result.setStartIndex(startIndex);
        result.setRecords(new ArrayList<CSWRecord>(maxRecords));
        int recordsRemaining, currentStartIndex = startIndex;
        int recordsMatched = 0;
        while((recordsRemaining = (maxRecords - result.getRecords().size())) > 0) {

            // If we are dealing with a purely remote filter we can just request the exact number of records
            int recsToRequest = this.pageSize;
            if (localFacets.size() == 0) {
                recsToRequest = Math.min(this.pageSize, recordsRemaining);
            }

            
            // If this starts spamming requests we can always look at upping the page size every iteration.
            CSWGetRecordResponse cswResponse = null;
            if(serviceItem != null) {
            	cswResponse = filterService.getFilteredRecords(serviceItem, remoteFilter, recsToRequest, currentStartIndex);
            } else if (serviceId != null) {
            	cswResponse = filterService.getFilteredRecords(serviceId, remoteFilter, recsToRequest, currentStartIndex);
            } else {
            	throw new PortalServiceException("No registered service ID or user defined service item was provided.");
            }
            
            // Update macthed record count
            recordsMatched = cswResponse.getRecordsMatched();

            // Filter our response and copy passing records to our result list
            int lastIndex;
            if (localFacets.size() == 0) {
                result.getRecords().addAll(cswResponse.getRecords());
                lastIndex = cswResponse.getRecords().size() - 1;
            } else {
                lastIndex = performLocalFilter(cswResponse.getRecords(), result.getRecords(), localFacets, recordsRemaining);
            }

            if (lastIndex < 0) {
                //If we got an empty response then we are done.
                result.setNextIndex(0);
                break;
            } else {
                currentStartIndex += lastIndex + 1;
                result.setNextIndex(currentStartIndex);
            }

            //If there are no more records to retrieve, abort early
            if (cswResponse.getNextRecord() <= 0) {
                result.setNextIndex(0);
                break;
            }
        }
        result.setRecordsMatched(recordsMatched);
        return result;
    }
    
    /**
     * Notifies all runners to terminate if they haven't already. Returns the count of the runner with the MOST records
     * @param allRunners
     * @return
     * @throws PortalServiceException
     */
    private int cleanupConcurrentFilteredRecords(ArrayList<FilterRunner> allRunners, Object lock) throws PortalServiceException {
        int maxDepth = -1;
        synchronized(lock) {
            for (FilterRunner runner : allRunners) {
                if (runner.error != null) {
                    log.error("Error accessing CSW records from service " + runner.serviceId + " : " + runner.error.getMessage());
                    log.debug("Exception", runner.error);
                }

                if (runner.state != FilterRunnerState.Terminated) {
                    synchronized(runner) {
                        runner.requestTerminate = true;
                        runner.notifyAll();
                    }
                }

                if (runner.records.size() > maxDepth) {
                    maxDepth = runner.records.size();
                }
            }
        }

        return maxDepth;
    }

    /**
     * Given a set of services to query with a set of local/remote search facets. Perform a full filter until maxRecords are received or the remote CSW runs out of records. This can result
     * in many calls to the remote CSW if the local portion of the filter is quite specific and the remote portion of very permissive.
     *
     * In order to fulfill the multiple registries, maxRecords will be shared between the various serviceIds. If a registry is unable to fulfill their portion of the records then their portion
     * will be handed off to the other services for fulfillment.
     *
     * The result set will be interleaved so that the records will be returned CSW1, CSW2, CS3, CS1, CSW2 etc..
     *
     * This method is synchronous but utilises the internal executor to concurrently call multiple services
     *
     * @param serviceIds
     * @param serviceItems a list of CSWServiceItems for custom registries, will be null if only registered registires are used
     * @param facets
     * @param startIndexes
     * @param maxRecords
     * @return
     * @throws PortalServiceException
     */
    public FacetedMultiSearchResponse getFilteredRecords(String[] serviceIds, CSWServiceItem[] serviceItems, List<SearchFacet<? extends Object>> facets, Map<String, Integer> startIndexes, int maxRecords) throws PortalServiceException {
        //Distribute our max records evenly across the service Ids (this will only be a suggested amount, we may need to redistribute)
        HashMap<String, Integer> fulfillment = new HashMap<String, Integer>();
        for (int i = 0; i < serviceIds.length; i++) {
            int fulfillmentCount = maxRecords / serviceIds.length;
            if (i < maxRecords % serviceIds.length) {
                fulfillmentCount++;
            }
            fulfillment.put(serviceIds[i], fulfillmentCount);
        }

        // Fire off our requests concurrently
        HashMap<String, FilterRunner> runners = new HashMap<String, FilterRunner>();
        Object lock = runners; //this doubles as our lock object for this request
         
        for (int i = 0; i < serviceIds.length; i++) {
        	// User may have specified a custom registry
        	CSWServiceItem cswItem = null;
        	if (serviceItems != null && serviceItems.length >= i) {
        		cswItem = serviceItems[i];
        	}
            FilterRunner runner = new FilterRunner(this, serviceIds[i], cswItem, fulfillment.get(serviceIds[i]), facets, startIndexes.get(serviceIds[i]), lock);
            runners.put(serviceIds[i], runner);
            this.executor.execute(runner);
        }
        
        ArrayList<FilterRunner> allRunners = new ArrayList<FilterRunner>(runners.values());

        // Check our runner statuses repeatedly, waking up when they tell us to
        while(true) {
            // Check our state inside the lock so don't end up with the state changing underneath us
            synchronized(lock) {
                boolean stillWaiting = false;
                for (FilterRunner runner : allRunners) {
                    if (!runner.isFulfilled() &&
                        runner.state != FilterRunnerState.Terminated) {
                        //A running unfulfilled runner requires us to wait for it to finish
                        stillWaiting = true;
                    } else if (!runner.isFulfilled() &&
                               runner.state == FilterRunnerState.Terminated) {
                        //Redistribute remaining fulfillment to other runners/services if we have a
                        //service run dry of records
                        int remainingFulfillment = runner.currentFulfillment - runner.records.size();
                        runner.currentFulfillment = runner.records.size();
                        ArrayList<FilterRunner> availRunners = new ArrayList<FilterRunner>();
                        for (FilterRunner availRunner : allRunners) {
                            if (availRunner.state != FilterRunnerState.Terminated) {
                                availRunners.add(availRunner);
                            }
                        }

                        if (!availRunners.isEmpty()) {
                            stillWaiting = true;
                            for (int i = 0; i < availRunners.size(); i++) {
                                int additionalFulfillment = remainingFulfillment / availRunners.size();
                                if (i < remainingFulfillment % availRunners.size()) {
                                    additionalFulfillment++;
                                }

                                FilterRunner availRunner = availRunners.get(i);
                                synchronized(availRunner) {
                                    availRunner.currentFulfillment += additionalFulfillment;
                                    availRunner.notifyAll();
                                }
                            }
                        }
                    }
                }

                if (!stillWaiting) {
                    break;
                }

                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    log.error("Interrupted:", e);
                    cleanupConcurrentFilteredRecords(allRunners, lock);
                    throw new PortalServiceException("Interrupted:", e);
                }
            }
        }

        // Cleanup runners, check for errors
        int maxDepth = cleanupConcurrentFilteredRecords(allRunners, lock);

        // Build our response object from the finished runner states (also cleanup our runners if some are paused)
        // make sure we interleave the results "fairly"
        FacetedMultiSearchResponse response = new FacetedMultiSearchResponse();
        for (int depth = 0; depth < maxDepth && response.getRecords().size() < maxRecords; depth++) {
            for (FilterRunner runner : allRunners) {
                if (depth < runner.records.size()) {
                    response.getRecords().add(runner.records.get(depth));
                    response.getRecordsMatched().put(runner.serviceId, runner.recordsMatched);
                    if (response.getRecords().size() == maxRecords) {
                        break;
                    }
                }
            }
        }
        for (FilterRunner runner : allRunners) {
            response.getNextIndexes().put(runner.serviceId, runner.currentNextIndex);
            response.getStartIndexes().put(runner.serviceId, runner.currentStartIndex);
        }

        return response;
    }
        
    /**
     * Returns the list of internal CSWServiceItems that powers this service (passes straight through to underlying CSWFilterService
     *
     * @return
     */
    public CSWServiceItem[] getCSWServiceItems() {
        return filterService.getCSWServiceItems();
    }

    private enum FilterRunnerState {
        Running,
        Terminated
    }

    /**
     * Handles calling getFilteredRecords on a seperate thread. If the underlying CSW runs out of records, this will terminate. If
     * the request for records is fulfilled then this will instead pause using Object.wait(). Notify at the parent level
     * to trigger an updated request for fulfillment.
     * @author Josh Vote (CSIRO)
     *
     */
    private class FilterRunner implements Runnable {

        private final Log log = LogFactory.getLog(getClass());

        public Object lock;
        public LocalCSWFilterService parent;
        public String serviceId;
        public CSWServiceItem serviceItem;	// Only present for custom registries
        public int currentFulfillment;
        public List<SearchFacet<? extends Object>> facets;
        public int currentStartIndex;
        public int currentNextIndex;
        public int recordsMatched;
        public FilterRunnerState state;
        public List<CSWRecord> records;
        public volatile boolean requestTerminate = false;
        public Throwable error;

        public FilterRunner(LocalCSWFilterService parent, String serviceId, CSWServiceItem serviceItem, int currentFulfillment,
                List<SearchFacet<? extends Object>> facets, int currentStartIndex, Object lock) {
            super();
            this.parent = parent;
            this.serviceId = serviceId;
            this.serviceItem = serviceItem;
            this.currentFulfillment = currentFulfillment;
            this.facets = facets;
            this.currentStartIndex = currentStartIndex;
            this.currentNextIndex = currentStartIndex;
            this.recordsMatched = 0;
            this.state = FilterRunnerState.Running;
            this.records = new ArrayList<CSWRecord>();
            this.lock = lock;
        }

        public boolean isFulfilled() {
            return this.records.size() >= this.currentFulfillment;
        }

        public void run() {
            try {
                while(!requestTerminate) {
                    if (isFulfilled()) {
                        synchronized(this) {
                            this.wait(); //wait for the parent to notify this to look for more due to an increase in fulfillment
                        }
                    } else {
                    	FacetedSearchResponse response;
                   		response = parent.getFilteredRecords(serviceId, serviceItem, facets, currentNextIndex, currentFulfillment - this.records.size());
                        synchronized(this.lock) {
                            this.records.addAll(response.getRecords());
                            this.currentNextIndex =  response.getNextIndex();
                            this.recordsMatched = response.getRecordsMatched();

                            if (response.getNextIndex() <= 0) {
                                //Having no more records means it's pointless to continue hitting this CSW
                                break;
                            } else {
                                this.lock.notifyAll();
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                log.error("Unable to access filtered records: " + e.getMessage());
                log.debug("Exception", e);
                this.error = e;
            }

            synchronized(this.lock) {
                this.state = FilterRunnerState.Terminated;
                this.lock.notifyAll(); //notify parent that we finished
            }
        }
    }
}
