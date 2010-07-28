package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.csw.CSWGetRecordResponse;
import org.auscope.portal.csw.CSWMethodMakerGetDataRecords;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.auscope.portal.csw.CSWOnlineResource.OnlineResourceType;

import org.auscope.portal.server.util.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.w3c.dom.Document;


/**
 * Provides some utility methods for accessing data from a CSW service
 *
 * @version $Id$
 * TODO: create an interface, as this implementation does things like caching,
 * TODO: which is not desirable in all cases
 */
@Service
public class CSWService {
    protected final Log log = LogFactory.getLog(getClass());

    private CSWRecord[] dataRecords = new CSWRecord[0];
    private HttpServiceCaller serviceCaller;
    private String serviceUrl;
    private CSWThreadExecutor executor;        
    private Util util;
    private volatile long lastUpdated = 0;
    private static final int UPDATE_INTERVAL = 300000;
    
    /*
     * This is used to prevent multiple updates running concurrently
     * (Any updates that trigger when an update is already running will be ignored)
     */
    private final Lock lock = new ReentrantLock();

    
    @Autowired
    public CSWService(CSWThreadExecutor executor,
                      HttpServiceCaller serviceCaller,
                      Util util) throws Exception {

        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.util = util;        
    }

    /**
     * ServiceURL setter
     * @param serviceUrl
     */
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Checks to see if the time interval from the last update has passed. If so, then update the cached records.
     * @throws Exception
     */
    public void updateRecordsInBackground() throws Exception {
        // Update the cache if older that 5 mins or there are no records
        if (System.currentTimeMillis() - lastUpdated > UPDATE_INTERVAL || dataRecords.length == 0) {
            executor.execute(new Runnable() {
                public void run() {
                    updateCSWRecords();
                }
            });
            lastUpdated = System.currentTimeMillis();
        }
    }

    /**
     * Updates the cached data records from the CSW service.
     */
    public void updateCSWRecords() {
        //If an update is already running, don't bother continuing
        if (!lock.tryLock()) {
            log.trace("Update is already running - update skipped");
            return;
        }
        
        log.trace("Update Starting");
        
        try {
            
            ICSWMethodMaker getRecordsMethod = new CSWMethodMakerGetDataRecords(serviceUrl);
            
            log.debug(getRecordsMethod.makeMethod().getQueryString());
            Document document = util.buildDomFromString(serviceCaller.getMethodResponseAsString(getRecordsMethod.makeMethod(), serviceCaller.getHttpClient()));

            CSWRecord[] tempRecords = new CSWGetRecordResponse(document).getCSWRecords();
            
            setDatarecords(tempRecords);
        } catch (Exception e) {
            log.error("Error parsing CSW record list",e);
        } finally {
            lock.unlock();
            log.trace("Update completed");
        }
    }

    /**
     * Assigns the cached data records.
     * @param records
     */
    private synchronized void setDatarecords(CSWRecord[] records) {
        this.dataRecords = records;
    }

    /**
     * Returns the entire cached record set
     * @return
     * @throws Exception
     */
    public CSWRecord[] getDataRecords() throws Exception {
        return dataRecords;
    }

    /**
     * Returns on WMS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWMSRecords() throws Exception {
        return getFilteredDataRecords(OnlineResourceType.WMS);
    }
    

    /**
     * Returns only WCS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWCSRecords() throws Exception {
        return getFilteredDataRecords(OnlineResourceType.WCS);
    }

    /**
     * Returns only WFS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWFSRecords() throws Exception {
        return getFilteredDataRecords(OnlineResourceType.WFS);
    }

    /**
     * Returns only WFS data records for a given feature typeName
     * @param featureTypeName
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWFSRecordsForTypename(String featureTypeName) throws Exception {
        CSWRecord[] records = getDataRecords();
        List<CSWRecord> filteredRecords = new ArrayList<CSWRecord>();
        
        for (CSWRecord rec : records) {
            CSWOnlineResource[] wfsResources = rec.getOnlineResourcesByType(OnlineResourceType.WFS);
            
            for (CSWOnlineResource res : wfsResources) {
                if (res.getName().equals(featureTypeName)) {
                    filteredRecords.add(rec);
                    break;
                }
            }
        }
        
        return filteredRecords.toArray(new CSWRecord[filteredRecords.size()]);
    }
    
    
    /**
     * Gets all records thta have at least one of the specifed types as an online resource
     * @param types
     * @return
     * @throws Exception
     */
    private CSWRecord[] getFilteredDataRecords(CSWOnlineResource.OnlineResourceType... types) throws Exception {
        CSWRecord[] records = getDataRecords();
        List<CSWRecord> filteredRecords = new ArrayList<CSWRecord>();
        
        for (CSWRecord rec : records) {
            if (rec.containsAnyOnlineResource(types)) {
                filteredRecords.add(rec);
            }
        }
        
        return filteredRecords.toArray(new CSWRecord[filteredRecords.size()]);
    }
}
