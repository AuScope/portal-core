package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGetRecordResponse;
import org.auscope.portal.csw.CSWMethodMakerGetDataRecords;
import org.auscope.portal.csw.CSWMethodMakerGetDataRecords.ResultType;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.csw.record.CSWOnlineResource;
import org.auscope.portal.csw.record.CSWOnlineResource.OnlineResourceType;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.domain.ows.OWSExceptionParser;
import org.auscope.portal.server.util.DOMUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

/**
 * A service for creating a cache of all keywords at a CSW.
 *
 * The cache will be periodically refreshed by crawling
 * through all CSW records
 *
 * @author Josh Vote
 *
 */
@Service
public class CSWCacheService {

    /**
     * The maximum number of records that will be requested from a CSW at a given time.
     *
     * If a CSW has more records than this value then multiple requests will be made
     */
    public static final int MAX_QUERY_LENGTH = 1000;

    /**
     * The frequency in which the cache updates (in milli seconds).
     */
    public static final long CACHE_UPDATE_FREQUENCY_MS = 1000L * 60L * 5L; //Set to 5 minutes

    protected final Log log = LogFactory.getLog(getClass());


    protected Map<String, Integer> keywordCache;
    protected List<CSWRecord> recordCache;
    protected HttpServiceCaller serviceCaller;
    protected CSWThreadExecutor executor;
    protected CSWServiceItem[] cswServiceList;
    protected boolean updateRunning;  //don't set this variable directly
    protected Date lastCacheUpdate;


    /**
     * Creates a new instance of a CSWKeywordCacheService. This constructor is normally autowired
     * by the spring framework.
     *
     * @param executor A thread executor that will be used to manage multiple simultaneous CSW requests
     * @param serviceCaller Will be involved in actually making a HTTP request
     * @param cswServiceList Must be an untyped array of CSWServiceItem objects (for bean autowiring) representing CSW URL endpoints
     * @throws Exception
     */
    @Autowired
    public CSWCacheService(CSWThreadExecutor executor,
                      HttpServiceCaller serviceCaller,
                      @Qualifier(value = "cswServiceList") ArrayList cswServiceList) throws Exception {
        this.updateRunning = false;
        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.keywordCache = new HashMap<String, Integer>();
        this.recordCache = new ArrayList<CSWRecord>();
        this.cswServiceList = new CSWServiceItem[cswServiceList.size()];
        for (int i = 0; i < cswServiceList.size(); i++) {
            this.cswServiceList[i] = (CSWServiceItem) cswServiceList.get(i);
        }
    }

    /**
     * Get's whether the currently running thread is OK to start a cache update
     *
     * If true is returned, ensure that the calling thread makes a call to updateFinished
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
     * if newKeywordCache is NOT null it will update the internal cache.
     * if newRecordCache is NOT null it will update the internal cache.
     */
    private synchronized void updateFinished(Map<String, Integer> newKeywordCache, List<CSWRecord> newRecordCache) {
        this.updateRunning = false;
        if (newKeywordCache != null) {
            this.keywordCache = newKeywordCache;
        }
        if (newRecordCache != null) {
            this.recordCache = newRecordCache;
        }

        this.lastCacheUpdate = new Date();

        log.info(String.format("Keyword cache updated! Cache now has '%1$d' unique keyword names", this.keywordCache.size()));
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
     * Returns an unmodifiable Map of keyword names to integer counts
     *
     * This function may trigger a cache update to begin on a seperate thread.
     * @return
     */
    public synchronized Map<String, Integer> getKeywordCache() {
        updateCacheIfRequired();

        return Collections.unmodifiableMap(this.keywordCache);
    }

    /**
     * Returns an unmodifiable List of CSWRecords
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
     * The update will occur on a seperate thread so this function will return immediately
     * with true if an update has started or false if an update is already running
     */
    public boolean updateCache() {
        if (!okToUpdate()) {
            return false;
        }

        //This will be our new cache
        Map<String, Integer> newKeywordCache = new HashMap<String, Integer>();
        List<CSWRecord> newRecordCache = new ArrayList<CSWRecord>();

        //Create our worker threads (ensure they are all aware of eachother)
        CSWCacheUpdateThread[] updateThreads = new CSWCacheUpdateThread[cswServiceList.length];
        for (int i = 0; i < updateThreads.length; i++) {
            updateThreads[i] = new CSWCacheUpdateThread(this, updateThreads, cswServiceList[i], newKeywordCache, newRecordCache, serviceCaller);
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
     * @return
     * @throws Exception
     */
    public List<CSWRecord> getWMSRecords() throws Exception {
        return getFilteredRecords(OnlineResourceType.WMS);
    }


    /**
     * Returns only WCS data records
     * @return
     * @throws Exception
     */
    public List<CSWRecord> getWCSRecords() throws Exception {
        return getFilteredRecords(OnlineResourceType.WCS);
    }

    /**
     * Returns only WFS data records
     * @return
     * @throws Exception
     */
    public List<CSWRecord> getWFSRecords() throws Exception {
        return getFilteredRecords(OnlineResourceType.WFS);
    }

    /**
     * Returns a filtered list of records from this cache
     * @param types
     * @return
     * @throws Exception
     */
    private synchronized List<CSWRecord> getFilteredRecords(
            CSWOnlineResource.OnlineResourceType... types) throws Exception {

        ArrayList<CSWRecord> records = new ArrayList<CSWRecord>();

        //Iterate EVERY record for EVERY service URL
        for(CSWRecord rec : recordCache) {
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
        protected final Log log = LogFactory.getLog(getClass());

        private CSWCacheService parent;
        private CSWCacheUpdateThread[] siblings; //this is also used as a shared locking object
        private CSWServiceItem endpoint;
        private Map<String, Integer> newKeywordCache;
        private List<CSWRecord> newRecordCache;
        private HttpServiceCaller serviceCaller;
        private boolean finishedExecution;

        public CSWCacheUpdateThread(CSWCacheService parent,
                CSWCacheUpdateThread[] siblings, CSWServiceItem endpoint,
                Map<String, Integer> newKeywordCache, List<CSWRecord> newRecordCache, HttpServiceCaller serviceCaller) {
            super();
            this.parent = parent;
            this.siblings = siblings;
            this.endpoint = endpoint;
            this.newKeywordCache = newKeywordCache;
            this.newRecordCache = newRecordCache;
            this.serviceCaller = serviceCaller;
            this.finishedExecution = false;
        }

        /**
         * This is synchronized on the siblings object
         * @return
         */
        private boolean isFinishedExecution() {
            synchronized(siblings) {
                return finishedExecution;
            }
        }

        /**
         * This is synchronized on the siblings object
         * @param finishedExecution
         */
        private void setFinishedExecution(boolean finishedExecution) {
            synchronized(siblings) {
                this.finishedExecution = finishedExecution;
            }
        }

        /**
         * When our threads finish they check whether sibling threads have finished yet
         * The last thread to finish has to update the parent
         * To avoid race conditions we ensure that checking the termination condition
         * is a synchronized operation
         *
         * This function is synchronized on the siblings object
         */
        private void attemptCleanup() {
            synchronized(siblings) {
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
                    parent.updateFinished(newKeywordCache, newRecordCache);
                }
            }
        }

        @Override
        public void run() {
            try {
                CSWMethodMakerGetDataRecords methodMaker = new CSWMethodMakerGetDataRecords(this.endpoint.getServiceUrl());
                int startPosition = 1;

                //Request page after page of CSWRecords until we've iterated the entire store
                do {
                    log.trace(String.format("%1$s - requesting startPosition %2$s", this.endpoint.getServiceUrl(), startPosition));

                    //Request our set of records
                    HttpMethodBase method = methodMaker.makeMethod(null, ResultType.Results, MAX_QUERY_LENGTH, startPosition);
                    InputStream responseStream = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());

                    log.trace(String.format("%1$s - Response received", this.endpoint.getServiceUrl()));

                    //Parse the response into newCache (remember that maps are NOT thread safe)
                    Document responseDocument = DOMUtil.buildDomFromStream(responseStream);
                    OWSExceptionParser.checkForExceptionResponse(responseDocument);
                    CSWGetRecordResponse response = new CSWGetRecordResponse(endpoint, responseDocument);
                    synchronized(newKeywordCache) {
                        synchronized(newRecordCache) {
                            for (CSWRecord record : response.getRecords()) {
                                //Update records
                                newRecordCache.add(record);

                                //Update keywords
                                for (String keyword : record.getDescriptiveKeywords()) {
                                    if (keyword == null || keyword.isEmpty()) {
                                        continue;
                                    }

                                    Integer count = newKeywordCache.get(keyword);
                                    if (count == null) {
                                        count = new Integer(1);
                                        newKeywordCache.put(keyword, count);
                                    } else {
                                        count = count + 1;
                                        newKeywordCache.put(keyword, count);
                                    }
                                }
                            }
                        }
                    }

                    log.trace(String.format("%1$s - Response parsed!", this.endpoint.getServiceUrl()));

                    //Prepare to request next 'page' of records (if required)
                    if (response.getNextRecord() > response.getRecordsMatched() ||
                        response.getNextRecord() <= 0) {
                        startPosition = -1; //we are done in this case
                    } else {
                        startPosition = response.getNextRecord();
                    }
                } while (startPosition > 0);
            } catch (Exception ex) {
                log.warn("Error updating keyword cache", ex);
            } finally {
                attemptCleanup();
            }
        }
    }
}
