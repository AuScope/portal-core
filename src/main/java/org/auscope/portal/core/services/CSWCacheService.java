package org.auscope.portal.core.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessResourceFailureException;


/**
 * A service for creating a cache of all keywords at a CSW.
 *
 * The cache will be periodically refreshed by crawling through all CSW records
 *
 * @author Josh Vote
 *
 */
public class CSWCacheService {
	
	@Value("${spring.data.elasticsearch.manualUpdateOnly:false}")
    private boolean manualUpdateOnly;

    /**
     * The frequency in which the cache updates (in milliseconds).
     */
	public static final long CACHE_UPDATE_FREQUENCY_MS = 1000L * 60L * 60L * 24L; //Set to 1 day

    private final Log log = LogFactory.getLog(getClass());

    /** A map of the records keyed by their keywords. For the full (non duplicate) set of CSWRecords see recordCache */
    protected Map<String, Set<CSWRecord>> keywordCache;

    protected Map<String, Set<String>> keywordsByRegistry;
    /** A list of records representing the most recent snapshot of all CSW's */
    protected List<CSWRecord> recordCache;

    /**
     * A cache of records from each csw endpoint, to be used in case of failures.
     *
     * Map of endpoint id to csw records.
     */
    protected Map<String, Map<String, CSWRecord>> cswRecordCache;

    protected HttpServiceCaller serviceCaller;
    protected Executor executor;
    protected CSWServiceItem[] cswServiceList;
    protected CSWRecordTransformerFactory transformerFactory;

    // An array of CSWServiceItems that have noCache==true. These ones will only be loaded when explicitly requested.
    // It is useful for CSWServiceItems (i.e. endpoints) that have too many records to load at once.
    protected CSWServiceItem[] deferredCacheCSWServiceList;

    protected boolean updateRunning; //don't set this variable directly
    /** If true, this class will force the usage of HTTP GetMethods instead of POST methods (where possible). Useful workaround for some CSW services */
    protected boolean forceGetMethods = false;
    protected Date lastCacheUpdate;
    
    // Provides access to CSWRecord index
    protected ElasticsearchService elasticsearchService;

    // KnownLayerService needs to be informed when indexing is finished, must be @Lazy loaded to avoid circular dependencies   
    @Autowired
    @Lazy
    private KnownLayerService knownLayerService;
    
    /**
     * Creates a new instance of a CSWKeywordCacheService. This constructor is normally autowired by the spring framework.
     *
     * @param executor
     *            A thread executor that will be used to manage multiple simultaneous CSW requests
     * @param serviceCaller
     *            Will be involved in actually making a HTTP request
     * @param cswServiceList
     *            Must be an untyped array of CSWServiceItem objects (for bean autowiring) representing CSW URL endpoints
     */
    public CSWCacheService(Executor executor,
            HttpServiceCaller serviceCaller,
            @SuppressWarnings("rawtypes") ArrayList cswServiceList,
            ElasticsearchService elasticsearchService) {
        this(executor, serviceCaller, cswServiceList, new CSWRecordTransformerFactory(), elasticsearchService);
    }

    /**
     * Creates a new instance of a CSWKeywordCacheService. This constructor is normally autowired by the spring framework.
     *
     * @param executor
     *            A thread executor that will be used to manage multiple simultaneous CSW requests
     * @param serviceCaller
     *            Will be involved in actually making a HTTP request
     * @param cswServiceList
     *            Must be an untyped array of CSWServiceItem objects (for bean autowiring) representing CSW URL endpoints
     * @param cacheDirectory
     */
    public CSWCacheService(Executor executor,
            HttpServiceCaller serviceCaller,
            @SuppressWarnings("rawtypes") ArrayList cswServiceList,
            CSWRecordTransformerFactory transformerFactory,
            ElasticsearchService elasticsearchService) {
        this.updateRunning = false;
        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.keywordCache = new HashMap<>();
        this.keywordsByRegistry = new HashMap<String, Set<String>>();
        this.recordCache = new ArrayList<>();
        this.cswRecordCache = new HashMap<String, Map<String, CSWRecord>>();
        this.transformerFactory = transformerFactory;
        this.elasticsearchService = elasticsearchService;
        this.cswServiceList = new CSWServiceItem[cswServiceList.size()];
        for (int i = 0; i < cswServiceList.size(); i++) {
            this.cswServiceList[i] = (CSWServiceItem) cswServiceList.get(i);
        }
        // Restore recordCache from index
        log.info("CSW record cache restoring");
        try {
        	this.recordCache = elasticsearchService.getAllCSWRecords();
        } catch(Exception e) {
        	log.error("Error retrieving CSW records: " + e.getLocalizedMessage());
        	this.updateRunning = false;
        }
        if (this.recordCache.size() > 0) {
        	log.info("CSW record cache restored: " + recordCache.size() + " records");
        } else {
        	log.info("CSW record cache empty");
        }
    }
    
    // KnownLayerService needs to be informed when indexing is finished, must be @Lazy loaded to avoid circular dependencies
    @Lazy
    @Autowired
    public void setKnownLayerService(KnownLayerService knownLayerService) {
    	this.knownLayerService = knownLayerService;
    }
    
    public KnownLayerService getKnownLayerService() {
    	return this.knownLayerService;
    }
    
    /**
     * Does this cache service force the usage of HTTP Get Methods
     *
     * @return
     */
    public boolean isForceGetMethods() {
        return forceGetMethods;
    }

    /**
     * Sets whether this cache service force the usage of HTTP Get Methods
     *
     * @param forceGetMethods
     */
    public void setForceGetMethods(boolean forceGetMethods) {
        this.forceGetMethods = forceGetMethods;
    }

    /**
     * Gets whether the currently running thread is OK to start a cache update
     *
     * If true is returned, ensure that the calling thread makes a call to updateFinished
     *
     * @return
     */
    private synchronized boolean okToUpdate() {
        if (this.updateRunning) {
            return false;
        }

        this.updateRunning = true;
        return true;
    }
    
    /**
     * Checks if a cache update is already running.
     *
     * Used by the health monitor.
     *
     * @return boolean true if update is already running. Otherwise false.
     */

    public boolean getUpdateRunning() {
        return this.updateRunning;
    }

    /**
     * Called by the update thread whenever an update finishes (successful or not)
     *
     * if newKeywordCache is NOT null it will update the internal cache. if newRecordCache is NOT null it will update the internal cache.
     */
    private synchronized void updateFinished(Map<String, Set<CSWRecord>> newKeywordCache, List<CSWRecord> newRecordCache, Map<String, Set<String>> newKeywordByEndpointCache) {
        if (newKeywordCache != null) {
            this.keywordCache = newKeywordCache;
        }
        if (newRecordCache != null) {
            this.recordCache = newRecordCache;
        }
        if (newKeywordByEndpointCache != null) {
            this.keywordsByRegistry = newKeywordByEndpointCache;
        }

        // Index CSWRecords and completion terms from newRecordCache
        try {
	        elasticsearchService.indexCSWRecords(newRecordCache);
	        elasticsearchService.indexCompletionTerms(newRecordCache);
        } catch(DataAccessResourceFailureException e) {
        	log.error(e.getLocalizedMessage());
        	this.updateRunning = false;
        }
        
        // Inform KnownLayerService that there are (potentially) new CSWRecords
        knownLayerService.updateKnownLayersCache(true);
        
        this.updateRunning = false;
        this.lastCacheUpdate = new Date();

        log.info(String.format("Keyword cache updated! Cache now has '%1$d' unique keyword names",
                this.keywordCache.size()));
        log.info(String.format("Record cache updated! Cache now has '%1$d' records", this.recordCache.size()));
    }

    /**
     * Starts an update of the internal caches if enough time has elapsed since the last update
     */
    private void updateCacheIfRequired() {
        if (!manualUpdateOnly && (lastCacheUpdate == null || (new Date().getTime() - lastCacheUpdate.getTime()) > CACHE_UPDATE_FREQUENCY_MS)) {
            updateCache();
        }
    }

    /**
     * Returns an unmodifiable Map of keyword names to matching CSWRecords
     *
     * This function may trigger a cache update to begin on a seperate thread.
     *
     * @return
     */
    public synchronized Map<String, Set<CSWRecord>> getKeywordCache() {
        updateCacheIfRequired();

        return Collections.unmodifiableMap(this.keywordCache);
    }

    /**
     * Gets the set of keywords cached from a particular endpoint.
     * @param endpointId The CSWServiceItem ID of the endpoint to check
     * @return An unmodifiable set on success or NULL otherwise
     */
    public synchronized Set<String> getKeywordsForEndpoint(String endpointId) {
        Set<String> keywords = this.keywordsByRegistry.get(endpointId);
        if (keywords == null) {
            return null;
        }
        return Collections.unmodifiableSet(keywords);
    }

    /**
     * Returns an unmodifiable List of CSWRecords
     *
     * @return
     */
    public synchronized List<CSWRecord> getRecordCache() {
        updateCacheIfRequired();

        return Collections.unmodifiableList(this.recordCache);
    }

    /**
     * Updates the internal keyword/record cache by querying all known CSW's
     *
     * If an update is already running this function will have no effect
     *
     * The update will occur on a separate thread so this function will return immediately with true if an update has started or false if an update is already
     * running default to make 3 attempts at 15 seconds interval if fail to connect.
     */
    public boolean updateCache() {
        return updateCache(3, 15000);
    }

    /**
     * Updates the internal keyword/record cache by querying all known CSW's
     *
     * If an update is already running this function will have no effect
     *
     * The update will occur on a separate thread so this function will return immediately with true if an update has started or false if an update is already
     * running
     *
     * @param connectionAttempts
     *            - number of attempts to try connecting
     * @param timeBtwConnectionAttempts
     *            - length of time in millisecond between each attempt to connect.
     * @return
     */
    public boolean updateCache(int connectionAttempts, long timeBtwConnectionAttempts) {
        if (!okToUpdate()) {
            return false;
        }
        
        //This will be our new cache
        Map<String, Set<CSWRecord>> newKeywordCache = new HashMap<>();
        Map<String, Set<String>> newKeywordByEndpointCache = new HashMap<>();
        List<CSWRecord> newRecordCache = new ArrayList<>();

        //Create our worker threads (ensure they are all aware of each other)
        CSWCacheUpdateThread[] updateThreads = new CSWCacheUpdateThread[cswServiceList.length];
        for (int i = 0; i < updateThreads.length; i++) {
            updateThreads[i] =
                new CSWCacheUpdateThread(this,
                                         updateThreads,
                                         cswServiceList[i],
                                         newKeywordCache,
                                         newKeywordByEndpointCache,
                                         newRecordCache,
                                         this.cswRecordCache,
                                         serviceCaller,
                                         connectionAttempts,
                                         timeBtwConnectionAttempts);
        }

        //Fire off our worker threads, the last one to finish will update the
        //internal cache and call 'updateFinished'
        for (CSWCacheUpdateThread thread : updateThreads) {
            this.executor.execute(thread);
        }

        return true;
    }

    /**
     * Returns on WMS data records
     *
     * @return
     */
    public List<CSWRecord> getWMSRecords() {
        return getFilteredRecords(OnlineResourceType.WMS);
    }

    /**
     * Returns only WCS data records
     *
     * @return
     */
    public List<CSWRecord> getWCSRecords() {
        return getFilteredRecords(OnlineResourceType.WCS);
    }

    /**
     * Returns only WFS data records
     *
     * @return
     */
    public List<CSWRecord> getWFSRecords() {
        return getFilteredRecords(OnlineResourceType.WFS);
    }

    /**
     * Returns a filtered list of records from this cache
     *
     * @param types
     * @return
     */
    private synchronized List<CSWRecord> getFilteredRecords(
            AbstractCSWOnlineResource.OnlineResourceType... types) {

        ArrayList<CSWRecord> records = new ArrayList<>();

        //Iterate EVERY record for EVERY service URL
        for (CSWRecord rec : recordCache) {
            if ((types == null || rec.containsAnyOnlineResource(types))) {
                records.add(rec);
            }
        }

        return Collections.unmodifiableList(records);
    }

    /**
     * Our worker class for updating our CSW cache
     */
    private class CSWCacheUpdateThread extends Thread {
        private final Log threadLog = LogFactory.getLog(getClass());

        private CSWCacheService parent;
        private CSWCacheUpdateThread[] siblings; //this is also used as a shared locking object
        private CSWServiceItem endpoint;
        private Map<String, Set<CSWRecord>> newKeywordCache;
        private Map<String, Set<String>> newKeywordByEndpointCache;
        private List<CSWRecord> newRecordCache;
        private boolean finishedExecution;
        private CSWService cswService;
        private int connectionAttempts;
        private long timeBtwConnectionAttempts;
        private Map<String, Map<String, CSWRecord>> cswRecordsCache;

        public CSWCacheUpdateThread(CSWCacheService parent,
                                    CSWCacheUpdateThread[] siblings,
                                    CSWServiceItem endpoint,
                                    Map<String, Set<CSWRecord>> newKeywordCache,
                                    Map<String, Set<String>> newKeywordByEndpointCache,
                                    List<CSWRecord> newRecordCache,
                                    Map<String, Map<String, CSWRecord>> cswRecordsCache,
                                    HttpServiceCaller serviceCaller,
                                    int connectionAttempts,
                                    long timeBtwConnectionAttempts) {
            super();
            this.parent = parent;
            this.siblings = siblings;
            this.endpoint = endpoint;
            this.newKeywordCache = newKeywordCache;
            this.newKeywordByEndpointCache = newKeywordByEndpointCache;
            this.newRecordCache = newRecordCache;
            this.cswRecordsCache = cswRecordsCache;
            this.finishedExecution = false;
            this.connectionAttempts = connectionAttempts;
            this.timeBtwConnectionAttempts = timeBtwConnectionAttempts;
            this.cswService = new CSWService(this.endpoint, serviceCaller, this.parent.forceGetMethods,
                    this.parent.transformerFactory);
        }

        /**
         * This is synchronized on the siblings object
         *
         * @return
         */
        private boolean isFinishedExecution() {
            synchronized (siblings) {
                return finishedExecution;
            }
        }

        /**
         * This is synchronized on the siblings object
         *
         * @param finishedExecution
         */
        private void setFinishedExecution(boolean finishedExecution) {
            synchronized (siblings) {
                this.finishedExecution = finishedExecution;
            }
        }

        /**
         * When our threads finish they check whether sibling threads have finished yet The last thread to finish has to update the parent To avoid race
         * conditions we ensure that checking the termination condition is a synchronized operation
         *
         * This function is synchronized on the siblings object
         */
        private void attemptCleanup() {
            synchronized (siblings) {
                this.setFinishedExecution(true);

                //This is all synchronized so nothing can finish execution until we release
                //the lock on siblings
                boolean cleanupRequired = true;
                for (CSWCacheUpdateThread sibling : siblings) {
                    if (!sibling.isFinishedExecution()) {
                        cleanupRequired = false;
                        break;
                    }
                }

                //Last thread to finish tells our parent we've terminated
                if (cleanupRequired) {
                    parent.updateFinished(newKeywordCache, newRecordCache, newKeywordByEndpointCache);
                }
            }
        }

        /**
         * adds record to keyword cache if it DNE
         *
         * @param keyword
         * @param record
         */
        private void addToKeywordCache(CSWServiceItem cswService, String keyword, CSWRecord record, Map<String, Set<CSWRecord>> cache, Map<String, Set<String>> cacheByEndpoints) {
            if (keyword == null || keyword.isEmpty()) {
                return;
            }

            Set<CSWRecord> existingRecsWithKeyword = cache.get(keyword);
            if (existingRecsWithKeyword == null) {
                existingRecsWithKeyword = new HashSet<>();
                cache.put(keyword, existingRecsWithKeyword);
            }

            existingRecsWithKeyword.add(record);

            Set<String> keywordsForEndpoint = cacheByEndpoints.get(cswService.getId());
            if (keywordsForEndpoint == null) {
                keywordsForEndpoint = new HashSet<String>();
                cacheByEndpoints.put(cswService.getId(), keywordsForEndpoint);
            }
            keywordsForEndpoint.add(keyword);
        }

        /**
         * Merges the contents of source into destination
         *
         * @param destination
         *            Will received source's contents
         * @param source
         *            Will have it's contents merged into destination
         * @param cache
         *            will be updated with destination referenced by source's keywords
         */
        private void mergeRecords(CSWServiceItem cswService, CSWRecord destination, CSWRecord source, Map<String, Set<CSWRecord>> cache, Map<String, Set<String>> cacheByEndpoints) {
            // Merge OnlineResources using "HashSet" to weed out duplicates
            Set<AbstractCSWOnlineResource> targetSet = new HashSet<AbstractCSWOnlineResource>();
            targetSet.addAll(destination.getOnlineResources());
            targetSet.addAll(source.getOnlineResources());
            destination.setOnlineResources(new ArrayList<AbstractCSWOnlineResource>(targetSet));
            
            // Merge constraints, accessConstraints and useLimitConstraints (no dupes)
            Set<String> constraintSet = new HashSet<>();
            constraintSet.addAll(Arrays.asList(destination.getConstraints()));
            constraintSet.addAll(Arrays.asList(source.getConstraints()));
            destination.setConstraints(constraintSet.toArray(new String[constraintSet.size()]));
            constraintSet = new HashSet<>();
            constraintSet.addAll(Arrays.asList(destination.getAccessConstraints()));
            constraintSet.addAll(Arrays.asList(source.getAccessConstraints()));
            destination.setAccessConstraints(constraintSet.toArray(new String[constraintSet.size()]));
            constraintSet = new HashSet<>();
            constraintSet.addAll(Arrays.asList(destination.getUseLimitConstraints()));
            constraintSet.addAll(Arrays.asList(source.getUseLimitConstraints()));
            destination.setUseLimitConstraints(constraintSet.toArray(new String[constraintSet.size()]));

            // Merge keywords (get rid of duplicates)
            Set<String> keywordSet = new HashSet<>();
            keywordSet.addAll(Arrays.asList(destination.getDescriptiveKeywords()));
            keywordSet.addAll(Arrays.asList(source.getDescriptiveKeywords()));
            destination.setDescriptiveKeywords(keywordSet.toArray(new String[keywordSet.size()]));

            for (String sourceKeyword : source.getDescriptiveKeywords()) {
                addToKeywordCache(cswService, sourceKeyword, destination, cache, cacheByEndpoints);
            }
        }

        /*
         * After retrieving the current set of records from the endpoint, this
         * will update the application cache.
         */
        private void updateAppCache(Map<String, CSWRecord> cswRecordMap) {
            //After parent/children have been linked, begin the keyword merging and extraction
            synchronized (newKeywordCache) {
                synchronized (newRecordCache) {
                    for (CSWRecord record : cswRecordMap.values()) {
                        boolean recordMerged = false;

                        // We will merge WMS or WFS records into an existing record if the endpoint urls and
                        // layer names match. In this case, this record will be discarded after its
                        // content has been merged.

                        // Loop through the Online Resources of the new record looking for candidates to merge
                        for (AbstractCSWOnlineResource wXSOnlineRes : record
                                .getOnlineResourcesByType(OnlineResourceType.WFS, OnlineResourceType.WMS)) {

                            if (StringUtils.isEmpty(record.getLayerName())) {
                                break;
                            }
                            
                            // Skip null or empty urls and layer names
                            if (wXSOnlineRes.getLinkage() == null
                                    || StringUtils.isEmpty(wXSOnlineRes.getLinkage().toString())) {
                                        continue;
                                    }
                            String longRecURL = wXSOnlineRes.getLinkage().toString();
                            String recURL;

                            // Trim off any parameters from URL for comparison
                            try {
                                URL url = new URL(longRecURL);
                                recURL = url.getHost() + url.getPath();
                            } catch (MalformedURLException e) {
                                continue;
                            }
                            // Trim interface name from url for comparison
                            recURL = StringUtils.substring(recURL, 0, recURL.lastIndexOf('/'));

                            // Loop through existing records
                            for (CSWRecord existingRec : newRecordCache) {
                            	
                            	/*
                            	if (record.getLayerName().equals("gsmlp:BoreholeView") && existingRec.getLayerName().equals("gsmlp:BoreholeView") &&
                            			(record.getFileIdentifier().equals("20f0650cc4cb09a1aaa06b7077c584130f9a502e") || record.getFileIdentifier().equals("49a7dce44a3520e465a5ce103941791908de692c"))) {
                            		System.out.println("CSWCacheServiuce: SA Borehole: " + existingRec.getFileIdentifier());
                            	}
                            	*/
                            	
                                // Loop through online resources of each record
                                if (StringUtils.isEmpty(existingRec.getLayerName())) {
                                    continue;
                                }

                                String existingRecLayerName = existingRec.getLayerName();
                                String recLayerName = record.getLayerName();
 
                                // MapServer uses layer names without namespaces in WMS getcaps. So, if either
                                // the new or existing record's layername doesn't include a namepaces then we
                                // trim all namespaces for comparison.
                                if (!existingRecLayerName.contains(":") || !recLayerName.contains(":")) {
                                    recLayerName = recLayerName.substring(recLayerName.indexOf(':') + 1,
                                            recLayerName.length());
                                    existingRecLayerName = existingRecLayerName.substring(
                                            existingRecLayerName.indexOf(':') + 1, existingRecLayerName.length());
                                }
                                if (!recLayerName.equals(existingRecLayerName)) {
                                    continue;
                                }

                                for (AbstractCSWOnlineResource existingRes : existingRec
                                        .getOnlineResourcesByType(OnlineResourceType.WFS, OnlineResourceType.WMS)) {
                                    // Skip null or empty urls and layernames
                                    if (existingRes.getLinkage() == null
                                            || StringUtils.isEmpty(existingRes.getLinkage().toString())) {
                                                continue;
                                            }

                                    String existingURL = existingRes.getLinkage().toString();

                                    // Trim off any parameters from URL for comparison.
                                    // Harvested geonetwork records often contain parameters in their URLs
                                    try {
                                        URL url = new URL(existingURL);
                                        existingURL = url.getHost() + url.getPath();
                                    } catch (MalformedURLException e) {
                                        continue;
                                    }
                                    
                                    // Trim interface name from url for comparison
                                    existingURL = StringUtils.substring(existingURL, 0, existingURL.lastIndexOf('/'));
                                    
                                    // Compare Layer Names and URLs
                                    if (recURL.equals(existingURL)) {
                                        threadLog.debug("Merging CSW records " + record.getRecordInfoUrl() + " and "
                                                + existingRec.getRecordInfoUrl());
                                        mergeRecords(this.endpoint, existingRec, record, newKeywordCache,
                                                newKeywordByEndpointCache);
                                        recordMerged = true;
                                        break;
                                    }
                                }
                                if (recordMerged == true)
                                    break;
                            }
                            if (recordMerged == true)
                                break;
                        }


                        //If the record was NOT merged into an existing record we then update the record cache
                        if (!recordMerged) {
                            // Update the keyword cache
                            for (String keyword : record.getDescriptiveKeywords()) {
                                addToKeywordCache(this.endpoint, keyword, record, newKeywordCache, newKeywordByEndpointCache);
                            }

                            // Add record to record list
                            newRecordCache.add(record);
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            // Query the endpoint and cache
            try {
                String cswServiceUrl = this.endpoint.getServiceUrl();
                threadLog.info("Updating CSW cache for: " + cswServiceUrl);
                if (this.endpoint.getNoCache()) {
                    // Create the dummy CSWResource - to avoid confusion: this is a CSW End point, NOT a CSW record.
                    // If we're not caching the responses we need to add this endpoint as a fake CSW record so that we can query it later:
                    synchronized (newRecordCache) {
                        CSWRecord record = new CSWRecord(this.endpoint.getId());
                        record.setNoCache(true);
                        record.setServiceName(this.endpoint.getTitle());
                        record.setServiceId(this.endpoint.getId());

                        record.setRecordInfoUrl(this.endpoint.getRecordInformationUrl());

                        CSWOnlineResourceImpl cswResource = new CSWOnlineResourceImpl(
                                new URL(cswServiceUrl),
                                OnlineResourceType.CSWService.toString(), // Set the protocol to CSWService.
                                this.endpoint.getTitle(),
                                "A link to a CSW end point.");

                        record.setConstraints(this.endpoint.getDefaultConstraints());

                        // Add the DefaultAnyTextFilter to the record so that we can use it in conjunction
                        // with whatever the user enters in the filter form.
                        record.setDescriptiveKeywords(new String[] {this.endpoint.getDefaultAnyTextFilter()});

                        List<AbstractCSWOnlineResource> onlineResources = new ArrayList<AbstractCSWOnlineResource>();
                        onlineResources.add(cswResource);
                        record.setOnlineResources(onlineResources);
                        
                        newRecordCache.add(record);
                    }
                }
                else {
                    int startPosition = 1;

                    // Request page after page of CSWRecords until we've iterated the entire store
                    HashMap<String, CSWRecord> cswRecordMap = new HashMap<>();
                    do {
                        CSWGetRecordResponse response = this.cswService.queryCSWEndpoint(startPosition,
                               this.endpoint.getPageSize(), this.connectionAttempts, this.timeBtwConnectionAttempts);
                        for (CSWRecord rec : response.getRecords()) {
                        	rec.setServiceId(this.endpoint.getId());
                        	if(StringUtils.isNotBlank(rec.getFileIdentifier())) {
                        		cswRecordMap.put(rec.getFileIdentifier(), rec);
                        	}
                        }

                        threadLog.trace(String.format("%1$s - Response parsed!", this.endpoint.getServiceUrl()));

                        // Prepare to request next 'page' of records (if required)
                        if (response.getNextRecord() > response.getRecordsMatched() ||
                                response.getNextRecord() <= 0) {
                            startPosition = -1; //we are done in this case
                        } else {
                            startPosition = response.getNextRecord();
                        }
                    } while (startPosition > 0);

                    // Iterate the cswRecordMap resolving parent/children relationships
                    // children will NOT be removed from the map
                    for (Iterator<String> i = cswRecordMap.keySet().iterator(); i.hasNext();) {
                        CSWRecord next = cswRecordMap.get(i.next());

                        String parentId = next.getParentIdentifier();
                        if (parentId != null && !parentId.isEmpty()) {
                            CSWRecord parentRecord = cswRecordMap.get(parentId);
                            if (parentRecord == null) {
                                threadLog.debug(String.format(
                                        "Record '%1$s' is an orphan referencing non existent parent '%2$s'",
                                        next.getFileIdentifier(), parentId));
                            } else {
                                parentRecord.addChildRecord(next);
                            }
                        }
                    }

                    // Store the contents for this endpoint so we can use it in case of errors later
                    synchronized(this.cswRecordsCache) {
                        // If there are records returned, serialise the cache and saved it to disk
                        if (cswRecordMap != null && cswRecordMap.size() > 0) {
                        	this.cswRecordsCache.put(this.endpoint.getId(), cswRecordMap);
	                        threadLog.info(this.endpoint.getServiceUrl() + " has been serialized.");
                        }
                    }
                }
            } catch (Exception ex) {
                threadLog.warn(String.format("Error updating keyword cache for '%1$s': %2$s", this.endpoint.getServiceUrl(), ex));
                threadLog.warn("Exception: ", ex);
                threadLog.info("Falling back on cached results for this endpoint.");
            } finally {
                // Update the cache using the new records, if successfully
                // retrieved, or the cached version if not.
                Map<String, CSWRecord> cswRecordMap = this.cswRecordsCache.get(this.endpoint.getId());
                if (cswRecordMap != null && cswRecordMap.size() > 0) {
                    updateAppCache(cswRecordMap);
                } else {
                    threadLog.warn(String.format("No cached results available for failed CSW %1$s", this.endpoint.getServiceUrl()));
                }

                // Finish up
                attemptCleanup();
            }
        }
    }
    
}
