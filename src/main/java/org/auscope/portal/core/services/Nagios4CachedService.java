package org.auscope.portal.core.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.Nagios4MethodMaker;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse;

/**
 * A caching extension built around Nagios4Service.
 *
 * @author Josh Vote (CSIRO)
 *
 */
public class Nagios4CachedService extends Nagios4Service {

    public static final long DEFAULT_TTL_SECONDS = 60 * 15; //15 Minutes

    /** How long a cache entry can exist before it requires an update*/
    private long ttlSeconds = DEFAULT_TTL_SECONDS;
    private ConcurrentHashMap<String, CacheEntry> cache; //cache entries keyed by hostgroup

    public Nagios4CachedService(String serviceUrl, HttpServiceCaller serviceCaller, Nagios4MethodMaker methodMaker) {
        super(serviceUrl, serviceCaller, methodMaker);
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
     *
     *
     * @see Nagios4Service.getStatuses
     */
    @Override
    public synchronized Map<String, List<ServiceStatusResponse>> getStatuses(String hostGroup) throws PortalServiceException {
        CacheEntry cacheEntry = cache.get(hostGroup);

        if (cacheEntry != null) {
        	if(cacheEntry.getPortalServiceException() != null){
        		throw cacheEntry.getPortalServiceException();
        	}
            long differenceSeconds = (new Date().getTime() - cacheEntry.getCreated().getTime()) / 1000l;
            if (differenceSeconds < ttlSeconds) {
                return cacheEntry.getResponse();
            }
        } 

        // I don't want a swarm of requests going out if the cache needs updating and there's lots of incoming requests
        // This could allow better throughput by only synchronizing requests with the same hostname
        // but I feel that's over engineering it
        Map<String, List<ServiceStatusResponse>> response = null;
        try{
        	response = super.getStatuses(hostGroup);
        } catch(PortalServiceException pse){
        	cacheEntry = new CacheEntry(new Date(), pse); // This could potentially recycle the CacheEntry objects instead
            cache.put(hostGroup, cacheEntry);
        	throw pse;
        }
        
        cacheEntry = new CacheEntry(new Date(), response); // This could potentially recycle the CacheEntry objects instead
        cache.put(hostGroup, cacheEntry);

        return response;
    }


    public class CacheEntry {
        private Date created;
        private Map<String, List<ServiceStatusResponse>> response;
        private PortalServiceException portalServiceException;
        
        public CacheEntry(Date created, Map<String, List<ServiceStatusResponse>> response) {
            super();
            this.created = created;
            this.response = response;
            this.portalServiceException = null;
        }
        
        public CacheEntry(Date created, PortalServiceException ex) {
            super();
            this.created = created;
            this.setPortalServiceException(ex);
            this.response = null;
        }
        
        public Date getCreated() {
            return created;
        }
        public void setCreated(Date created) {
            this.created = created;
        }
        public Map<String, List<ServiceStatusResponse>> getResponse() {
            return response;
        }
        public void setResponse(Map<String, List<ServiceStatusResponse>> response) {
            this.response = response;
        }
        
		public PortalServiceException getPortalServiceException() {
			return portalServiceException;
		}
		public void setPortalServiceException(PortalServiceException exception) {
			this.portalServiceException = exception;
		}
    }
}
