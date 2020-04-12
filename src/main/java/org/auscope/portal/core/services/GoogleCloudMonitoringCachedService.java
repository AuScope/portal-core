package org.auscope.portal.core.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.auscope.portal.core.services.methodmakers.GoogleCloudMonitoringMethodMaker;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;

/**
 * A caching extension built around GoogleCloudMonitoringService, inspired by Nagios4CachedService.
 *
 * @author Josh Vote (CSIRO)
 * @author Rini Angreani (CSIRO)
 *
 */
public class GoogleCloudMonitoringCachedService extends GoogleCloudMonitoringService {

    public static final long DEFAULT_TTL_SECONDS = 60 * 15; //15 Minutes

    /** How long a cache entry can exist before it requires an update*/
    private long ttlSeconds = DEFAULT_TTL_SECONDS;
    private ConcurrentHashMap<String, CacheEntry> cache; //cache entries keyed by hostgroup

    public GoogleCloudMonitoringCachedService(GoogleCloudMonitoringMethodMaker methodMaker) {
        super(methodMaker);
        cache = new ConcurrentHashMap<String, CacheEntry>();
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * See parent implementation for details on method.
     *
     * This method is synchronised to avoid swarms of requests going out when the cache entries expire. The cache lookups should be super
     * fast so unless we are servicing tens of thousands of simultaneous requests, this should hold up fine.
     * @param checkIds
     *
     *
     * @see GoogleCloudMonitoringService.getStatuses
     */
    @Override
	public synchronized Map<String, List<ServiceStatusResponse>> getStatuses(Set<String> checkIds) throws PortalServiceException {

    	CacheEntry cacheEntry = cache.get(this.getProjectId());

    	if (cacheEntry != null) {

       		// check if within cache period
            long differenceSeconds = (new Date().getTime() - cacheEntry.getCreated().getTime()) / 1000l;
           	if (differenceSeconds < ttlSeconds) {

           		// throw exception if within cache period
               	if(cacheEntry.getPortalServiceException() != null){
               		throw cacheEntry.getPortalServiceException();
               	}

          		Set<String> currentCheckIds = cacheEntry.getCheckIds();
           		// check if the check ids are already cached, if not query the ones that haven't been queried
           		if (!currentCheckIds.containsAll(checkIds)) {
           			checkIds.addAll(currentCheckIds);
                    return generateCache(true, checkIds);
           		}
            	// return cache
            	return cacheEntry.responses;
             }
           	 // cache expired, generate new cache
             return generateCache(false, checkIds);
    	}
    	// cache is empty, generate new cache
   		return generateCache(false, checkIds);
    }

    private Map<String, List<ServiceStatusResponse>> generateCache(boolean retainCacheValue, Set<String> checkIds) throws PortalServiceException {
    	Map<String, List<ServiceStatusResponse>> responses;
        CacheEntry cacheEntry;
        String projectId = this.getProjectId();
		try{
        	responses = super.getStatuses(checkIds);
        } catch(PortalServiceException pse){
        	cacheEntry = new CacheEntry(new Date(), pse); // This could potentially recycle the CacheEntry objects instead
            cache.put(projectId, cacheEntry);
        	throw pse;
        }
		if (retainCacheValue) {
			// merge entries that aren't in the new results
			// so we don't have to requery them again
			Map<String, List<ServiceStatusResponse>> oldResponse = cache.get(projectId).getResponse();
			for (Entry<String, List<ServiceStatusResponse>> entry : oldResponse.entrySet()) {
				for (ServiceStatusResponse status : entry.getValue()) {
					if (!checkIds.contains(status.getCheckId())) {
						responses.entrySet().add(entry);
					}
				}
			}
			cache.get(projectId).getCheckIds();
		}

		cacheEntry = new CacheEntry(new Date(), responses, checkIds); // This could potentially recycle the CacheEntry objects instead
        cache.put(projectId, cacheEntry);

        return responses;
	}


	public class CacheEntry {
        private Date created;
        private Map<String, List<ServiceStatusResponse>> responses;
        private PortalServiceException portalServiceException;
		private Set<String> checkIds;

        public CacheEntry(Date created, Map<String, List<ServiceStatusResponse>> responses, Set<String> checkIds) {
            super();
            this.created = created;
            this.responses = responses;
            this.portalServiceException = null;
            this.checkIds = checkIds;
        }

        public CacheEntry(Date created, PortalServiceException ex) {
            super();
            this.created = created;
            this.setPortalServiceException(ex);
            this.responses = null;
            this.checkIds = null;
        }

        public Date getCreated() {
            return created;
        }
        public void setCreated(Date created) {
            this.created = created;
        }
        public Map<String, List<ServiceStatusResponse>> getResponse() {
            return responses;
        }
        public void setResponse(Map<String, List<ServiceStatusResponse>> responses) {
            this.responses = responses;
        }

        public Set<String> getCheckIds() {
        	return this.checkIds;
        }

		public PortalServiceException getPortalServiceException() {
			return portalServiceException;
		}
		public void setPortalServiceException(PortalServiceException exception) {
			this.portalServiceException = exception;
		}
    }
}
