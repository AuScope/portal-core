package org.auscope.portal.core.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.xml.xpath.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewGetCapabilitiesFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.auscope.portal.core.view.knownlayer.KnownLayerGrouping;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.ui.ModelMap;

/**
 * A service class performing that groups CSWRecord objects (from a CSWCacheService) according to a configured list of KnownLayers
 *
 * @author Josh Vote
 *
 */
public class KnownLayerService {
    private final Log logger = LogFactory.getLog(getClass());
    private List<KnownLayer> knownLayers;
    private CSWCacheService cswCacheService;
    private WMSService wmsService;
    private SearchService searchService;

    private GoogleCloudMonitoringCachedService stackDriverService = null;

    /** Used for converting data to something the view can understand */
    private ViewKnownLayerFactory viewKnownLayerFactory;
    private ViewGetCapabilitiesFactory viewGetCapabilitiesFactory;
    private ViewCSWRecordFactory viewCSWRecordFactory;
    
    @Autowired(required = false)
    public void setCachedService(GoogleCloudMonitoringCachedService service) {
        this.stackDriverService = service;
    }

    /**
     * Creates a new instance of this class from an untyped list. All objects in knownTypes that can be cast into a KnownLayer will be included in the internal
     * known layer list
     *
     * @param knownTypes
     *            A list of objects, only KnownLayer subclasses will be used
     * @param cswCacheService
     *            An instance of CSWCacheService
     */
    public KnownLayerService(@SuppressWarnings("rawtypes") List knownTypes,
            ViewKnownLayerFactory viewFactory,
            ViewCSWRecordFactory viewCSWRecordFactory,
            ViewGetCapabilitiesFactory viewGetCapabilitiesFactory, WMSService wmsService, SearchService searchService) {
        this.knownLayers = new ArrayList<>();
        for (Object obj : knownTypes) {
            if (obj instanceof KnownLayer) {
                this.knownLayers.add((KnownLayer) obj);
            }
        }

        this.viewKnownLayerFactory = viewFactory;
        this.viewCSWRecordFactory = viewCSWRecordFactory;
        this.viewGetCapabilitiesFactory = viewGetCapabilitiesFactory;
        this.wmsService = wmsService;
        this.searchService = searchService;
    }
    
    @Lazy
    @Autowired
    public void setCSWCacheService(CSWCacheService cswCacheService) {
    	this.cswCacheService = cswCacheService;
    }
    
    public CSWCacheService getCSWCacheService() {
    	return this.cswCacheService;
    }

    /**
     * Builds a KnownLayerGrouping by iterating the current CSW Cache Service record set and applying each of those records to one or more Known Layer objects.
     *
     * The resulting bundle of grouped/ungrouped records and known layers will then be returned.
     *
     * This overload does not impose a filter for the type of class that is allowed and therefore won't exclude anything from the list.
     *
     * @return An instance of KnownLayerGrouping that encapsulates all the known layers, as well as any unmapped records and the original record list.
     */
    public KnownLayerGrouping groupKnownLayerRecords() {
        return groupKnownLayerRecords((Class<?>[])null);
    }

    /**
     * Builds a KnownLayerGrouping by iterating the current CSW Cache Service record set and applying each of those records to one or more Known Layer objects.
     *
     * The resulting bundle of grouped/ungrouped records and known layers will then be returned.
     *
     * This overload allows you explicitly state which classes should be included.
     *
     * @param classFilters
     *            An array of classes that should be included. You can use this to restrict the output to only include items that are of a particular subclass
     *            of KnownLayer.
     * @return An instance of KnownLayerGrouping that encapsulates the known layers you've selected, as well as any unmapped records and the original record
     *         list.
     */
    public <T extends KnownLayer> KnownLayerGrouping groupKnownLayerRecords(Class<?>... classFilters) {
        List<CSWRecord> originalRecordList = this.cswCacheService.getRecordCache();
        List<KnownLayerAndRecords> knownLayerAndRecords = new ArrayList<>();
        Map<String, Object> mappedRecordIDs = new HashMap<>();

        // Figure out what records belong to which known layers (could be multiple)
        for (KnownLayer knownLayer : knownLayers) {
            // We have to do this part regardless of the classFilters because
            // if not, the results for unmappedRecords will be incorrect.
            // (I.e.: they'll include related features from things that have
            // been put in Research Data tab).
            // GPT-103 - the problem is in the creation of the belonging and related records - the order is not the same as input
            KnownLayerSelector selector = knownLayer.getKnownLayerSelector();
            List<CSWRecord> relatedRecords = new ArrayList<>();
            List<CSWRecord> belongingRecords = new ArrayList<>();
            List<GetCapabilitiesRecord> capabilitiesRecords = new ArrayList<>();
            // Used to ensure we only send a 'GetCapabilites' request once to each service
            List<String> capDoneList = new ArrayList<>();

            // For each record, mark it as being added to a known layer (if appropriate)
            // We also need to mark the record as being mapped using mappedRecordIDs
            for (CSWRecord record : originalRecordList) {
                try {
                    switch (selector.isRelatedRecord(record)) {
                    case Related:
                        addToListConsiderWMSSelectors(relatedRecords, record, knownLayer);
                        mappedRecordIDs.put(record.getFileIdentifier(), null);
                        break;
                    case Belongs:
                        addToListConsiderWMSSelectors(belongingRecords, record, knownLayer);
                        mappedRecordIDs.put(record.getFileIdentifier(), null);

                        // Look for services for which require a GetCapabilitiesRecord
                        AbstractCSWOnlineResource[] onlineResourceList = record.getOnlineResourcesByType(AbstractCSWOnlineResource.OnlineResourceType.WMS);
                        if (onlineResourceList.length > 0) {
                            for (AbstractCSWOnlineResource onlineRes: onlineResourceList) {
                                // So far only GSKY services require us to fetch a 'GetCapabilities' response 
                                if (onlineRes.getApplicationProfile().contains("GSKY")) {
                                    URL linkage = onlineRes.getLinkage();
                                    String url = linkage.getProtocol() + "://" + linkage.getHost() + linkage.getPath();
                                    if (!capDoneList.contains(url)) {
                                        try {
                                            // Send a 'GetCapabilities' request to the service
                                            GetCapabilitiesRecord capabilitiesRec = wmsService.getWmsCapabilities(url, "1.3.0");

                                            // Only collect the 'GetCapabilities' record if it contains a valid 'timeExtent' value in its WMS layers
                                            for (GetCapabilitiesWMSLayerRecord wmsCapRec: capabilitiesRec.getLayers()) {
                                                String[] timeExtArr = wmsCapRec.getTimeExtent();
                                                if (timeExtArr != null && timeExtArr.length > 0) {
                                                    capabilitiesRecords.add(capabilitiesRec);
                                                    break;
                                                }
                                            }
                                        } catch (XPathException e) {
                                            logger.warn(String.format("Unable to retrieve WMS GetCapabilities for '%1$s'", url));
                                            logger.warn(e);
                                        }
                                        capDoneList.add(url);
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        // Ignore metadata CSW records - they have no named online resources and no geographic element BBOXes
                        // To ignore we must add it to the mapped record ids, so it does not get counted as an unmapped record
                        if (!record.hasNamedOnlineResources() && !record.hasGeographicElements()) {
                            mappedRecordIDs.put(record.getFileIdentifier(), null);
                        }
                    }
                } catch (PortalServiceException e) {
                    logger.error("Expecting data to line up", e);
                }
            }

            // The include flag will indicate whether or not this particular layer
            // should be included in the output.
            boolean include = false;

            // If no filters have been set then we just check that the KnownLayer is not a derived type:
            if (classFilters == null) {
                include = knownLayer.getClass().equals(KnownLayer.class);
            } else {
                // Otherwise we have to see if this particular known layer matches
                // any of the filters:
                for (Class<?> classFilter : classFilters) {
                    if (classFilter.isAssignableFrom(knownLayer.getClass())) {
                        include = true;
                        break;
                    }
                }
            }

            // If the include flag got set then we can add this record:
            if (include) {
                knownLayerAndRecords.add(new KnownLayerAndRecords(knownLayer, belongingRecords, relatedRecords, capabilitiesRecords));
            }
        }

        // Finally work out which records do NOT belong to a known layer
        List<CSWRecord> unmappedRecords = new ArrayList<>();
        for (CSWRecord record : originalRecordList) {
            if (!mappedRecordIDs.containsKey(record.getFileIdentifier())) {
                unmappedRecords.add(record);
            }
        }

        // Index collated layers and records for searching
        this.searchService.indexKnownLayersAndRecords(knownLayerAndRecords);
        
        return new KnownLayerGrouping(knownLayerAndRecords, unmappedRecords, originalRecordList);
    }

    /**
     * GPT-103 - Conjunction Layers (from GPT-40) - the order is all out of wack in relatedRecords and belongsRecords from the Conjunction order given in
     * auscope_known_layers due to the order being from the List<CSWRecords> originalRecordList (which came from the GeoNetwork Server). So have to restore the
     * order.
     *
     * This method will use the order of the layers in the WMSSelectors.wmsSelectors layer IFF WMSSelectors.layersMode == AND
     *
     * @param listToUpdate
     *            - add record to this list at the index specified by the order of record.getName() in WMSSelectors.wmsSelectors list items. The size of the
     *            array if (knownLayer.getKnownLayerSelector() instanceof WMSSelectors) will be the number of layers defined in the WMSSelectors. If not
     *            (knownLayer.getKnownLayerSelector() instanceof WMSSelectors) then the size will be the number of items that have been added over time.
     * @param record
     *            - to add to listToUpdate
     * @param knownLayer
     *            - that the record belonged to
     * @throws PortalServiceException
     *             when knownLayer.getKnownLayerSelector() instanceof WMSSelectors but the record.getLayerName() to be part of WMSSelector list in
     *             WMSSelectors
     */
    void addToListConsiderWMSSelectors(List<CSWRecord> listToUpdate, CSWRecord record, KnownLayer knownLayer)
            throws PortalServiceException {
        KnownLayerSelector selector = knownLayer.getKnownLayerSelector();

        if (knownLayer.getKnownLayerSelector() instanceof WMSSelectors) {

            // WMSSelectors are used to construct conjuncted (AND) and disjuncted (OR) WMSSelector layers
            List<String> layerNames = getWMSelectorsLayerNames(((WMSSelectors) selector).getWmsSelectors());

            // Use Array internally - Lists don't work so well when adding to indexed positions
            CSWRecord[] arrayToUpdate = Arrays.copyOf(listToUpdate.toArray(new CSWRecord[0]), layerNames.size());

            int indexInWMSSelectorsList = layerNames.indexOf(record.getLayerName());
            if (indexInWMSSelectorsList == -1) {
                // listToUpdate.add(record);
                throw new PortalServiceException(
                        "Expecting record.getLayerName() to be part of WMSSelector list in WMSSelectors - gsn(): "+ record.getLayerName() + ", layerNames: "+ layerNames);
            } else {
                // Keep the order in the bean definition list
                arrayToUpdate[indexInWMSSelectorsList] = record;
                listToUpdate.clear();
                listToUpdate.addAll(Arrays.asList(arrayToUpdate));
            }
        } else {
            listToUpdate.add(record);
        }
    }

    /**
     * @param wmsSelectors
     * @return layerNames from the selectors
     */
    private static List<String> getWMSelectorsLayerNames(List<WMSSelector> wmsSelectors) {
        List<String> layerNames = new ArrayList<>();
        for (WMSSelector wmsSelector : wmsSelectors) {
            layerNames.add(wmsSelector.getLayerName());
        }
        return layerNames;
    }

    private List<ModelMap> knownLayersCache = new ArrayList<>();

    /***
     * Returns the list of known layers. This list is populated by {@link KnownLayerService#updateKnownLayersCache()}. 
     * {@link KnownLayerService#updateKnownLayersCache()} is threadsafe and can be called directly or from a background
     * thread
     * 
     * @return cached version of known layers
     */
    public List<ModelMap> getKnownLayersCache() {
        synchronized (knownLayersCache) {
            return new ArrayList<>(knownLayersCache);
        }
    }

    /***
     * Updated the list of known layers depending on their status as obtained from StackDriver. This method is
     * threadsafe and can be called directly or from a background thread.
     * 
     * To access the results of the update, call {@link KnownLayerService#getKnownLayersCache()}
     */
    public void updateKnownLayersCache() {
        logger.trace("Updating service status for KnownLayers. Current size: "+knownLayers.size());
        ArrayList<ModelMap> newKnownLayersCache = new ArrayList<>();
        
        KnownLayerGrouping knownLayerGrouping = groupKnownLayerRecords();
        List<KnownLayerAndRecords> knownLayers = knownLayerGrouping.getKnownLayers();
        for (KnownLayerAndRecords knownLayerAndRecords : knownLayers) {
            KnownLayer kl = knownLayerAndRecords.getKnownLayer();
            if (kl.isHidden()) {
                continue; //any hidden layers will NOT be sent to the view
            }
            ModelMap viewKnownLayer = viewKnownLayerFactory.toView(knownLayerAndRecords.getKnownLayer());

            List<ModelMap> viewMappedRecords = new ArrayList<>();

            Set<String> onlineResourceEndpoints = new HashSet<>();
            ArrayList<String> layerNames = new ArrayList<>();
            for (CSWRecord rec : knownLayerAndRecords.getBelongingRecords()) {

                
                if (rec != null) {
                    for (AbstractCSWOnlineResource onlineResource : rec.getOnlineResources()) {
                        if (onlineResource.getLinkage() != null) {
                            onlineResourceEndpoints.add(onlineResource.getLinkage().getHost());
                        }
                        layerNames.add(onlineResource.getName());
                    }
                    viewMappedRecords.add(viewCSWRecordFactory.toView(rec));
                }
            }

            List<ModelMap> viewRelatedRecords = new ArrayList<>();
            for (CSWRecord rec : knownLayerAndRecords.getRelatedRecords()) {
                if (rec != null) {
                    viewRelatedRecords.add(viewCSWRecordFactory.toView(rec));
                }
            }

            // Add in capability records, but only for the relevant layer
            List<ModelMap> viewCapabilityRecords = new ArrayList<>();
            String layerName = null;
            if (layerNames.size() > 0) {
                layerName = layerNames.get(0);
            }
            for (GetCapabilitiesRecord rec : knownLayerAndRecords.getCapabilitiesRecords()) {
                viewCapabilityRecords.add(viewGetCapabilitiesFactory.toView(rec, layerName));
            }
            viewKnownLayer.put("cswRecords", viewMappedRecords);
            viewKnownLayer.put("relatedRecords", viewRelatedRecords);
            viewKnownLayer.put("capabilityRecords", viewCapabilityRecords);

            if (stackDriverService != null && kl.getStackdriverServiceGroup() != null) {
                try {
                    Map<String, List<ServiceStatusResponse>> response = stackDriverService.getStatuses(kl.getStackdriverServiceGroup());
                    List<String> failingHosts = new ArrayList<String>();
                    for (Entry<String, List<ServiceStatusResponse>> entry : response.entrySet()) {
                        for (ServiceStatusResponse status : entry.getValue()) {
                            if (!status.isUp()) {
                                if (onlineResourceEndpoints.contains(entry.getKey())) {
                                    failingHosts.add(entry.getKey());
                                    break;
                                }
                            }
                        }
                    }

                    if (!failingHosts.isEmpty()) {
                        viewKnownLayer.put("stackdriverFailingHosts", failingHosts);
                    }
                } catch (PortalServiceException ex) {
                    logger.error("Error updating stackdriver host info for " + kl.getName() + " :" + ex.getMessage());
                }
            }

            newKnownLayersCache.add(viewKnownLayer);
        }
        
        synchronized (knownLayersCache) {
            knownLayersCache.clear();
            knownLayersCache.addAll(newKnownLayersCache);
        }
        logger.info("Finished updating service status for KnownLayers. New size: "+knownLayers.size());
    }
  
}
