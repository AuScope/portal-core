package org.auscope.portal.core.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import org.apache.commons.lang.ArrayUtils;
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
import org.auscope.portal.core.util.FileIOUtil;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;



/**
 * A service for creating a cache of all keywords at a CSW.
 *
 * The cache will be periodically refreshed by crawling through all CSW records
 *
 * @author Josh Vote
 *
 */
public class CSWCacheService {

    /**
     * Any records containing keywords prefixed by this value we be merged with other records containing the same keyword.
     *
     * This is AuScope's workaround for a lack of functionality for supporting related or associated services.
     *
     * See also - https://jira.csiro.au/browse/SISS-1292
     */
    public static final String KEYWORD_MERGE_PREFIX = "association:";

    /**
     * The maximum number of records that will be requested from a CSW at a given time.
     *
     * If a CSW has more records than this value then multiple requests will be made
     */
    public static final int MAX_QUERY_LENGTH = 500;

    /**
     * The frequency in which the cache updates (in milliseconds).
     */
    public static final long CACHE_UPDATE_FREQUENCY_MS = 1000L * 60L * 5L; //Set to 5 minutes

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
            @SuppressWarnings("rawtypes") ArrayList cswServiceList) {
        this(executor, serviceCaller, cswServiceList, new CSWRecordTransformerFactory());
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
     */
    public CSWCacheService(Executor executor,
            HttpServiceCaller serviceCaller,
            @SuppressWarnings("rawtypes") ArrayList cswServiceList,
            CSWRecordTransformerFactory transformerFactory) {
        this.updateRunning = false;
        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.keywordCache = new HashMap<>();
        this.keywordsByRegistry = new HashMap<String, Set<String>>();
        this.recordCache = new ArrayList<>();
        this.cswRecordCache = new HashMap<String, Map<String, CSWRecord>>();
        this.transformerFactory = transformerFactory;
        this.cswServiceList = new CSWServiceItem[cswServiceList.size()];
        for (int i = 0; i < cswServiceList.size(); i++) {
            this.cswServiceList[i] = (CSWServiceItem) cswServiceList.get(i);
        }
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
     * Called by the update thread whenever an update finishes (successful or not)
     *
     * if newKeywordCache is NOT null it will update the internal cache. if newRecordCache is NOT null it will update the internal cache.
     */
    private synchronized void updateFinished(Map<String, Set<CSWRecord>> newKeywordCache, List<CSWRecord> newRecordCache, Map<String, Set<String>> newKeywordByEndpointCache) {
        this.updateRunning = false;
        if (newKeywordCache != null) {
            this.keywordCache = newKeywordCache;
        }
        if (newRecordCache != null) {
            this.recordCache = newRecordCache;
        }
        if (newKeywordByEndpointCache != null) {
            this.keywordsByRegistry = newKeywordByEndpointCache;
        }

        this.lastCacheUpdate = new Date();

        log.info(String.format("Keyword cache updated! Cache now has '%1$d' unique keyword names",
                this.keywordCache.size()));
        log.info(String.format("Record cache updated! Cache now has '%1$d' records", this.recordCache.size()));
    }

    /**
     * Starts an update of the internal caches if enough time has elapsed since the last update
     */
    private void updateCacheIfRequired() {
        if (lastCacheUpdate == null ||
                (new Date().getTime() - lastCacheUpdate.getTime()) > CACHE_UPDATE_FREQUENCY_MS) {
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
     * running default to make 3 attemps at 15 seconds interval if fail to connect.
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
        private void addToKeywordCache(CSWServiceItem endpoint, String keyword, CSWRecord record, Map<String, Set<CSWRecord>> cache, Map<String, Set<String>> cacheByEndpoints) {
            if (keyword == null || keyword.isEmpty()) {
                return;
            }

            Set<CSWRecord> existingRecsWithKeyword = cache.get(keyword);
            if (existingRecsWithKeyword == null) {
                existingRecsWithKeyword = new HashSet<>();
                cache.put(keyword, existingRecsWithKeyword);
            }

            existingRecsWithKeyword.add(record);

            Set<String> keywordsForEndpoint = cacheByEndpoints.get(endpoint.getId());
            if (keywordsForEndpoint == null) {
                keywordsForEndpoint = new HashSet<String>();
                cacheByEndpoints.put(endpoint.getId(), keywordsForEndpoint);
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
        private void mergeRecords(CSWServiceItem endpoint, CSWRecord destination, CSWRecord source, Map<String, Set<CSWRecord>> cache, Map<String, Set<String>> cacheByEndpoints) {
            //Merge onlineresources
            AbstractCSWOnlineResource[] merged = (AbstractCSWOnlineResource[]) ArrayUtils.addAll(
                    destination.getOnlineResources(), source.getOnlineResources());
            destination.setOnlineResources(merged);

            //Merge keywords (get rid of duplicates)
            Set<String> keywordSet = new HashSet<>();
            keywordSet.addAll(Arrays.asList(destination.getDescriptiveKeywords()));
            keywordSet.addAll(Arrays.asList(source.getDescriptiveKeywords()));
            destination.setDescriptiveKeywords(keywordSet.toArray(new String[keywordSet.size()]));

            for (String sourceKeyword : source.getDescriptiveKeywords()) {
                addToKeywordCache(endpoint, sourceKeyword, destination, cache, cacheByEndpoints);
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

                        //Firstly we may possibly merge this
                        //record into an existing record IF particular keywords
                        //are present. In this case, record will be discarded (its contents
                        //already found their way into an existing record)
                        //Hence we need to perform this step first
                        for (String keyword : record.getDescriptiveKeywords()) {
                            if (keyword == null || keyword.isEmpty()) {
                                continue;
                            }

                            //If we have an 'association keyword', look for existing records
                            //to merge this record's contents in to.
                            if (keyword.startsWith(KEYWORD_MERGE_PREFIX)) {
                                Set<CSWRecord> existingRecs = newKeywordCache.get(keyword);
                                if (existingRecs != null && !existingRecs.isEmpty()) {
                                    mergeRecords(endpoint, existingRecs.iterator().next(), record, newKeywordCache, newKeywordByEndpointCache);
                                    recordMerged = true;
                                }
                            }
                        }

                        //If the record was NOT merged into an existing record we then update the record cache
                        if (!recordMerged) {
                            //Update the keyword cache
                            for (String keyword : record.getDescriptiveKeywords()) {
                                addToKeywordCache(endpoint, keyword, record, newKeywordCache, newKeywordByEndpointCache);
                            }

                            //Add record to record list
                            newRecordCache.add(record);
                        }
                    }
                }
            }
        }

        @Override
        public void run() {

            // Before querying the endpoint, see if there is a serialised cache of endpoint
            // saved to disk, deserialise it and retain in memory in case there is a failure.
            Map<String, CSWRecord> serialisedCSWRecordMap = new HashMap<>();
            if(new File(FileIOUtil.getTempDirURL() + endpoint.getId() +".ser").exists()) {
                Kryo kryo = new Kryo();
                kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
                com.esotericsoftware.kryo.io.Input input = null;
                try {
                    input = new com.esotericsoftware.kryo.io.Input(new FileInputStream(FileIOUtil.getTempDirURL() + endpoint.getId() + ".ser"));
                    serialisedCSWRecordMap = kryo.readObject(input, HashMap.class);
                    input.close();
                } catch (Exception e) {
                    threadLog.warn(String.format("Failed deserialising cached registry: %1$s, %2$s", FileIOUtil.getTempDirURL(), endpoint.getId() + ".ser"), e);
                }
            } else {
                threadLog.warn(String.format("No saved registry found on disk on: %1$s, %2$s", FileIOUtil.getTempDirURL(), endpoint.getId() + ".ser"));
            }

            //Query the endpoint and cache
            try {
                String cswServiceUrl = this.endpoint.getServiceUrl();

                if (this.endpoint.getNoCache()) {
                    // Create the dummy CSWResource - to avoid confusion: this is a CSW End point, NOT a CSW record.
                    // If we're not caching the responses we need to add this endpoint as a fake CSW record so that we can query it later:
                    synchronized (newRecordCache) {
                        CSWRecord record = new CSWRecord(this.endpoint.getId());
                        record.setNoCache(true);
                        record.setServiceName(this.endpoint.getTitle());

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

                        record.setOnlineResources(new AbstractCSWOnlineResource[] {cswResource});
                        newRecordCache.add(record);
                    }
                }
                else {
                    int startPosition = 1;

                    // Request page after page of CSWRecords until we've iterated the entire store
                    HashMap<String, CSWRecord> cswRecordMap = new HashMap<>();
                    do {
                        CSWGetRecordResponse response = this.cswService.queryCSWEndpoint(startPosition,
                                MAX_QUERY_LENGTH, this.connectionAttempts, this.timeBtwConnectionAttempts);
                        for (CSWRecord rec : response.getRecords()) {
                            cswRecordMap.put(rec.getFileIdentifier(), rec);
                        }

                        threadLog.trace(String.format("%1$s - Response parsed!", this.endpoint.getServiceUrl()));

                        //Prepare to request next 'page' of records (if required)
                        if (response.getNextRecord() > response.getRecordsMatched() ||
                                response.getNextRecord() <= 0) {
                            startPosition = -1; //we are done in this case
                        } else {
                            startPosition = response.getNextRecord();
                        }
                    } while (startPosition > 0);

                    //Iterate the cswRecordMap resolving parent/children relationships
                    //children will NOT be removed from the map
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

                    // Cache the contents for this endpoint so we can use it in
                    // case of errors later.
                    synchronized(this.cswRecordsCache) {
                        this.cswRecordsCache.put(endpoint.getId(), cswRecordMap);

                        // If there are records returned, serialise the cache and saved it to disk
                        if (cswRecordMap.size() > 0) {
	                        Kryo kryo = new Kryo();
	                        com.esotericsoftware.kryo.io.Output output = new com.esotericsoftware.kryo.io.Output(new FileOutputStream( FileIOUtil.getTempDirURL() + endpoint.getId() +".ser"));
	                        kryo.writeObject(output, cswRecordMap);
	                        output.close();
	                        threadLog.info(endpoint.getServiceUrl() + " has been serialized.");
                        }
                    }
                }
            } catch (Exception ex) {
                threadLog.warn(String.format("Error updating keyword cache for '%1$s': %2$s", this.endpoint.getServiceUrl(),
                        ex));
                threadLog.warn("Exception: ", ex);
                threadLog.info("Falling back on cached results for this endpoint.");
            } finally {
                // Update the cache using the new records, if successfully
                // retrieved, or the cached version if not.
                Map<String, CSWRecord> cswRecordMap = this.cswRecordsCache.get(endpoint.getId());
                if (cswRecordMap != null) {
                    updateAppCache(cswRecordMap);
                } else if (serialisedCSWRecordMap != null) {
                    threadLog.info(String.format("Endpoint failed for CSW %1$s, falling back to saved cached on disk:" + FileIOUtil.getTempDirURL() + endpoint.getId() + ".ser", this.endpoint.getServiceUrl()));
                    updateAppCache(serialisedCSWRecordMap);
                } else {
                    threadLog.warn(String.format("No cached results available for failed CSW %1$s", this.endpoint.getServiceUrl()));
                }

                // Finish up
                attemptCleanup();
            }
        }
    }
    
}
