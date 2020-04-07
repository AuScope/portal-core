package org.auscope.portal.core.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.namespaces.ServiceNamespaceContext;
import org.auscope.portal.core.util.DOMUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import javax.xml.namespace.NamespaceContext;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A service to cache namespaces for a set of service URLs. Service URLs to cache are determined on a per call
 * basis. Updating the cache
 */
@Service
public class NamespaceService {

    protected HttpServiceCaller httpServiceCaller;

    protected WFSGetFeatureMethodMaker wfsMethodMaker;

    public static final long CACHE_UPDATE_FREQUENCY_MS = 1000L * 60L * 60L * 24L; //Set to 24 hours

    protected Date lastCacheUpdate;

    private ConcurrentMap <String, NamespaceContext> namespaceCache;
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    public NamespaceService(HttpServiceCaller httpServiceCaller, WFSGetFeatureMethodMaker wfsMethodMaker) {
        namespaceCache = new ConcurrentHashMap<>();
        this.httpServiceCaller = httpServiceCaller;
        this.wfsMethodMaker = wfsMethodMaker;
    }

    /**
     * Returns the URI for the namespace for the given service URL and prefix
     *
     * @param serviceUrl The service URL for the namespace
     * @param prefix The prefix of the namespace
     * @return The uri for the namespace
     */
    public String getNamespaceURI(String serviceUrl, String prefix) {
        NamespaceContext namespaceContext = this.namespaceCache.get(serviceUrl);
        if (namespaceContext == null) {
            namespaceContext = getOnlineNameSpace(serviceUrl);
            namespaceCache.put(serviceUrl, namespaceContext);
        }

        String namespaceUri = namespaceContext.getNamespaceURI(prefix);

        if (lastCacheUpdate == null ||
                (new Date().getTime() - lastCacheUpdate.getTime()) > CACHE_UPDATE_FREQUENCY_MS) {
            updateNamespaces();
            this.lastCacheUpdate = new Date();
        }
        return namespaceUri;

    }

    /**
     *  Updates the namespace cache for all cached service URLs
     */
    public void updateNamespaces() {
            Set<String> serviceUrls = this.namespaceCache.keySet();
                for (String serviceUrl : serviceUrls) {
                updateNamespace(serviceUrl);
            }

    }

    /**
     * An asynchronous call to update the namespace for the given service URL
     *
     * @param serviceUrl The service URL to update the namespace cache
     */
    private void updateNamespace(String serviceUrl) {
        new Thread(() -> {
            log.info("Updating the namespaces for " + serviceUrl);
            NamespaceContext namespaceContext = getOnlineNameSpace(serviceUrl);
            namespaceCache.put(serviceUrl, namespaceContext);
        }).start();
    }

    /**
     * Direct call to the service URL to build the namespace cache from the service GetCapabilities document
     *
     * @param serviceUrl The service URL to generate the namespace cache
     * @return
     */
    private NamespaceContext getOnlineNameSpace(String serviceUrl) {
        HttpRequestBase method = null;
        ServiceNamespaceContext namespace = new ServiceNamespaceContext();
        try {

            /**
             * The below is to deal with the situation where a WMS url of the form http://service.url/ows?SERVICE=WMS
             * is passed to the portal. The @{@link WFSGetFeatureMethodMaker} class adds the parameter 'service' to
             * the GetCapabiltities request, but this does not overwrite the uppercase parameter
             *
             */
            URIBuilder builder = new URIBuilder(serviceUrl);
            builder.clearParameters();
            String wfsUrl = builder.toString();


            method = wfsMethodMaker.makeGetCapabilitiesMethod(wfsUrl);
            String responseString = httpServiceCaller.getMethodResponseAsString(method);
            Document responseDoc = DOMUtil.buildDomFromString(responseString);
            Element elem = responseDoc.getDocumentElement();
            NamedNodeMap namespaces = elem.getAttributes();
            for (int i = 0; i < namespaces.getLength(); i++) {
                Attr attribute = (Attr) namespaces.item(i);
                if (attribute.getNamespaceURI() != null && attribute.getNamespaceURI().equals("http://www.w3.org/2000/xmlns/")) {
                    namespace.setNamespace(attribute.getLocalName(), attribute.getValue());

                }
            }
            this.lastCacheUpdate = new Date();
        } catch (Exception ex) {
            log.warn(String.format("Get onlineGsmlpNameSpace for '%s' failed", serviceUrl));
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return namespace;

    }

    /**
     * Clears the namespace cache
     */
    public void clearCache() {
        this.namespaceCache.clear();
    }

    /**
     * Returns the namespace cache for the service
     *
     * @return Current namespace cache
     */
    public ConcurrentMap<String, NamespaceContext> getNamespaceCache() {
        return namespaceCache;
    }
}
