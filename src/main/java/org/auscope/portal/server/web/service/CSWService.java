package org.auscope.portal.server.web.service;

import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGetRecordResponse;
import org.auscope.portal.csw.CSWMethodMakerGetDataRecords;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWOnlineResource.OnlineResourceType;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWThreadExecutor;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.auscope.portal.server.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;


/**
 * Provides some utility methods for accessing data from a CSW service
 *
 * @version $Id$
 * TODO: create an interface, as this implementation does things like caching,
 * 		 which is not desirable in all cases
 */
@Service
public class CSWService {
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * A utility class that groups useful information about a single cache for a single service URL
     */
    private class UrlCache implements Runnable{
    	private CSWRecord[] cache;
    	private long lastTimeUpdated;
    	private CSWServiceItem serviceItem;
    	private String lastCSWresponse;

    	//These are cached for the run method
    	private HttpServiceCaller serviceCaller;
        private Util util;

        //This isn't perfect, but it does the job we need it to
        //This will stop multiple updates on different threads from running at the same time
        private volatile boolean updateInProgress;

    	public UrlCache(CSWServiceItem serviceItem, HttpServiceCaller serviceCaller, Util util) {
    		this.serviceItem = serviceItem;
    		this.cache = new CSWRecord[0];
    		this.serviceCaller = serviceCaller;
    		this.util = util;
    	}

    	public synchronized void setCache(CSWRecord[] cache, String lastCSWresponse) {
    		this.cache = cache;
    		this.lastCSWresponse = lastCSWresponse;
    		this.lastTimeUpdated = System.currentTimeMillis();
    	}

    	public synchronized CSWRecord[] getCache() {
    		return this.cache;
    	}

    	public synchronized long getLastTimeUpdated() {
    		return this.lastTimeUpdated;
    	}

    	public synchronized String getlastCSWresponse() {
    		return this.lastCSWresponse;
    	}
    	public boolean getUpdateInProgress(){
    		return updateInProgress;
    	}

    	public void setUpdateInProgress(boolean updateInProgress){
    		this.updateInProgress = updateInProgress;
    	}


    	public void run() {
    		try {
            	//This section is all "thread safe" because it uses all local variables (or rather its methods do)
        		//It might be useful to mark this behaviour in the definition of the objects
                ICSWMethodMaker getRecordsMethod = new CSWMethodMakerGetDataRecords(this.serviceItem.getServiceUrl());
                HttpClient newClient = serviceCaller.getHttpClient();
                String methodResponse =  serviceCaller.getMethodResponseAsString(getRecordsMethod.makeMethod(), newClient);

                // TODO: Stupid CSW response are timestamped.
                if (methodResponse.equals(this.getlastCSWresponse())) {
                	throw new Exception(String.format("Cache identical - not rebuilding - '%1$s'"));
                }
                else {
                	log.info(String.format("Update required for serviceName='%1$s'",this.serviceItem.getServiceUrl()));
		            Document document = util.buildDomFromString(methodResponse);
		            CSWRecord[] tempRecords = new CSWGetRecordResponse(document).getCSWRecords();
		            //These records should also have a link back to their provider
		            if (serviceItem.getRecordInformationUrl() != null && serviceItem.getRecordInformationUrl().length() > 0) {
		                for (CSWRecord record : tempRecords) {
		                    if (record.getFileIdentifier() != null && record.getFileIdentifier().length() > 0) {
		                        String recordInfoUrl = serviceItem.getRecordInformationUrl().replace(
		                        		serviceItem.PLACEHOLDER_RECORD_ID, record.getFileIdentifier());
		                        record.setRecordInfoUrl(recordInfoUrl);
		                    }
		                }
		            }

		            //This is where we need to avoid race conditions
		            this.setCache(tempRecords, methodResponse);
		            log.info(String.format("Update completed for serviceName='%1$s'",this.serviceItem.getServiceUrl()));
                }

            } catch (Exception e) {
                log.error(e);
            }
            finally {
            	//It is possible that another thread can startup before this thread exits completely
            	//But it won't be a problem. The main issue is to stop MULTIPLE updates firing at once
            	//and hammering an external resource, at this point all communications with the external
            	//source have finished.
            	this.updateInProgress = false;
                log.info("Update completed for "+this.serviceItem.getServiceUrl());
            }
    	}
    }

    /**
     * Each element in this list represents a cache retrieved from a single serviceURL
     */
    private UrlCache[]  cache;
    private HttpServiceCaller serviceCaller;
    private CSWThreadExecutor executor;
    private Util util;
    private static final int UPDATE_INTERVAL = 600000;

    @Autowired
    public CSWService(CSWThreadExecutor executor,
                      HttpServiceCaller serviceCaller,
                      Util util,
                      @Qualifier(value = "cswServiceList") ArrayList cswServiceList) throws Exception {

        this.executor = executor;
        this.serviceCaller = serviceCaller;
        this.util = util;

        this.cache = new UrlCache[cswServiceList.size()];
    	for (int i = 0; i < cswServiceList.size(); i++) {
    		cache[i] = new UrlCache((CSWServiceItem) cswServiceList.get(i), serviceCaller, util);
    	}
    }

    /**
     * Starts a new update thread for each service URL that has no records OR hasn't been updated in the last UPDATE_INTERVAL
     * @param force if set every cache will be forced to update
     * @throws Exception
     */
    public void updateRecordsInBackground(boolean force) throws Exception {
    	//Update every service URL
    	for (int i = 0; i < cache.length; i++) {
    		UrlCache currentCache = cache[i];

            // Update the cache if it's not already updating
    		if (!currentCache.getUpdateInProgress()) {
    			//Update cache each UPDATE_INTERVAL mins.
    			if (force || (System.currentTimeMillis() - currentCache.getLastTimeUpdated() > UPDATE_INTERVAL)) {
	            	currentCache.setUpdateInProgress(true);
	                executor.execute(currentCache);
	            }
    		}
    	}
    }
    
    /**
     * Starts a new update thread for each service URL that has no records OR hasn't been updated in the last UPDATE_INTERVAL
     * @throws Exception
     */
    public void updateRecordsInBackground() throws Exception {
        this.updateRecordsInBackground(false);
    }


    /**
     * Returns every record in this cache (Even records with empty service Url's)

     * @return
     * @throws Exception
     */
    public CSWRecord[] getAllRecords() throws Exception {
    	return getFilteredRecords(null);
    }

    /**
     * Returns on WMS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWMSRecords() throws Exception {
        return getFilteredRecords(OnlineResourceType.WMS);
    }


    /**
     * Returns only WCS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWCSRecords() throws Exception {
        return getFilteredRecords(OnlineResourceType.WCS);
    }

    /**
     * Returns only WFS data records
     * @return
     * @throws Exception
     */
    public CSWRecord[] getWFSRecords() throws Exception {
        return getFilteredRecords(OnlineResourceType.WFS);
    }

    /**
     * Returns a filtered list of records from this cache
     * @param types
     * @return
     * @throws Exception
     */
    private synchronized CSWRecord[] getFilteredRecords(
    		CSWOnlineResource.OnlineResourceType... types) throws Exception {

        ArrayList<CSWRecord> records = new ArrayList<CSWRecord>();

        //Iterate EVERY record for EVERY service URL
        for (int i = 0; i < cache.length; i++)
        {
	    	for(CSWRecord rec : cache[i].getCache()) {
            	if ((types == null || rec.containsAnyOnlineResource(types))) {
            		records.add(rec);
                }
            }
        }

        return records.toArray(new CSWRecord[records.size()]);
    }
}
