package org.auscope.portal.core.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

	private Map<String, List<String>> servicesMap;

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

    public void setServicesMap(Map<String, List<String>> servicesMap) {
    	this.servicesMap = servicesMap;
    }

    public Map<String, List<String>> getServicesMap() {
    	return this.servicesMap;
    }

    /**
     * See parent implementation for details on method.
     *
     * This method is synchronised to avoid swarms of requests going out when the cache entries expire. The cache lookups should be super
     * fast so unless we are servicing tens of thousands of simultaneous requests, this should hold up fine.
     * @param serviceGroup
     *
     *
     * @see GoogleCloudMonitoringService.getStatuses
     */
	public synchronized Map<String, List<ServiceStatusResponse>> getStatuses(String serviceGroup) throws PortalServiceException {

    	CacheEntry cacheEntry = cache.get(serviceGroup);

    	if (cacheEntry != null) {

       		// check if within cache period
            long differenceSeconds = (new Date().getTime() - cacheEntry.getCreated().getTime()) / 1000l;
           	if (differenceSeconds < ttlSeconds) {

           		// throw exception if within cache period
               	if(cacheEntry.getPortalServiceException() != null){
               		throw cacheEntry.getPortalServiceException();
               	}
            	// return cache
            	return cacheEntry.responses;
             }
    	}
    	Map<String, List<ServiceStatusResponse>> responses;
    	try{
        	responses = super.getStatuses(servicesMap.get(serviceGroup));
        } catch(PortalServiceException pse){
        	cacheEntry = new CacheEntry(new Date(), pse); // This could potentially recycle the CacheEntry objects instead
            cache.put(serviceGroup, cacheEntry);
        	throw pse;
        }
		cacheEntry = new CacheEntry(new Date(), responses); // This could potentially recycle the CacheEntry objects instead
        cache.put(serviceGroup, cacheEntry);

        return responses;
    }

	public class CacheEntry {
        private Date created;
        private Map<String, List<ServiceStatusResponse>> responses;
        private PortalServiceException portalServiceException;

        public CacheEntry(Date created, Map<String, List<ServiceStatusResponse>> responses) {
            super();
            this.created = created;
            this.responses = responses;
            this.portalServiceException = null;
        }

        public CacheEntry(Date created, PortalServiceException ex) {
            super();
            this.created = created;
            this.setPortalServiceException(ex);
            this.responses = null;
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
		public PortalServiceException getPortalServiceException() {
			return portalServiceException;
		}
		public void setPortalServiceException(PortalServiceException exception) {
			this.portalServiceException = exception;
		}
    }
}
