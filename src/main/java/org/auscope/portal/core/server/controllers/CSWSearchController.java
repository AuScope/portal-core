package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.LocalCSWFilterService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.SearchFacet;
import org.auscope.portal.core.services.csw.SearchFacet.Comparison;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.search.FacetedMultiSearchResponse;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import org.json.JSONObject;

/**
 * Controller for handling search requests for a remote CSW
 * @author Josh Vote (CSIRO)
 *
 */
@Controller
public class CSWSearchController extends BaseCSWController {

    private LocalCSWFilterService filterService;
    private CSWCacheService cacheService;
    private CSWFilterService cswFilterService;
    private WMSService wmsService;

    @Autowired
    public CSWSearchController(ViewCSWRecordFactory viewCSWRecordFactory, LocalCSWFilterService filterService, CSWCacheService cacheService, WMSService wmsService, CSWFilterService cswFilterService) {
        super(viewCSWRecordFactory);
        this.filterService = filterService;
        this.cacheService = cacheService;
        this.wmsService = wmsService;
        this.cswFilterService = cswFilterService;
    }

    @ResponseStatus(value =  org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    /**
     * Returns the list of keywords available for searching at a particular registry
     * @param serviceId
     * @return
     */
    @RequestMapping("facetedKeywords.do")
    public ModelAndView facetedKeywords(@RequestParam("serviceId") String[] serviceIds) {
        final Set<String> keywords = new HashSet<String>();

        for (String serviceId : serviceIds) {
            Set<String> kwCache = this.cacheService.getKeywordsForEndpoint(serviceId);
            if (kwCache != null) {
                keywords.addAll(kwCache);
            }
        }

        return generateJSONResponseMAV(true, keywords, "");
    }

    /**
     * Parses and performs a faceted search on a CSW
     * @param start 1 based index to start record search from
     * @param limit
     * @param serviceId A list of service IDs
     * @param serviceTitle List of service titles for custom registries (optional)
     * @param serviceUrl List of service URLs for custom registries (optional)
     * @param recordUrl List of record URLs for custom registries (optional)
     * @param serviceType List of OGC service types for custom registries (optional)
     * @param rawFields A list of all fields to be searched on. Must match the other raw params in length.
     * @param rawValues A list of all values to be searched against. Must match the other raw params in length.
     * @param rawTypes A list of all value types to be searched on. Must match the other raw params in length.
     * @param rawComparisons A list of all comparisons between field/value to be searched against. Must match the other raw params in length.
     * @return
     */
    @RequestMapping("facetedCSWSearch.do")
    public ModelAndView facetedCSWSearch(
            @RequestParam(value="start", required=false, defaultValue="1") Integer[] starts,
            @RequestParam(value="limit", required=false, defaultValue="10") Integer limit,
            @RequestParam("serviceId") String[] serviceIds,
            @RequestParam(value="serviceTitle", required=false) String[] serviceTitles,
            @RequestParam(value="serviceUrl", required=false) String[] serviceUrls,
            @RequestParam(value="recordUrl", required=false) String[] recordUrls,
            @RequestParam(value="serviceType", required=false) String[] serviceTypes,
            @RequestParam(value="field", required=false) String[] rawFields,
            @RequestParam(value="value", required=false) String[] rawValues,
            @RequestParam(value="type", required=false) String[] rawTypes,
            @RequestParam(value="comparison", required=false) String[] rawComparisons) {

        if (rawFields == null) {
            rawFields = new String[0];
        }

        if (rawValues == null) {
            rawValues = new String[0];
        }

        if (rawTypes == null) {
            rawTypes = new String[0];
        }

        if (rawComparisons == null) {
            rawComparisons = new String[0];
        }

        if (rawFields.length != rawValues.length || rawFields.length != rawTypes.length || rawFields.length != rawComparisons.length) {
            throw new IllegalArgumentException("field/value/type/comparison lengths mismatch");
        }

        if (limit > 20) {
            throw new IllegalArgumentException("Limit too high (max 20)");
        }

        // If start Ids are not specified, assume they are all 1
        if (starts == null || starts.length == 0) {
            starts = new Integer[serviceIds.length];
            Arrays.fill(starts, 1);
        }

        // Build our mapping between service ids and start indexes
        if (starts.length != serviceIds.length) {
            throw new IllegalArgumentException("start/serviceId lengths mismatch");
        }
        Map<String, Integer> startIndexes = new HashMap<String, Integer>();
        for (int i = 0; i < starts.length; i++) {
            startIndexes.put(serviceIds[i], starts[i]);
        }

        // Parse our raw request info into a list of search facets
        List<SearchFacet<? extends Object>> facets = new ArrayList<SearchFacet<? extends Object>>();
        for (int i = 0; i < rawFields.length; i++) {
            Comparison cmp = null;
            switch(rawComparisons[i]) {
            case "gt":
                cmp = Comparison.GreaterThan;
                break;
            case "lt":
                cmp = Comparison.LessThan;
                break;
            case "eq":
                cmp = Comparison.Equal;
                break;
            default:
                throw new IllegalArgumentException("Unknown comparison type: " + rawComparisons[i]);
            }

            SearchFacet<? extends Object> newFacet = null;
            switch(rawTypes[i]) {
            case "servicetype":
                newFacet = new SearchFacet<OnlineResourceType>(Enum.valueOf(OnlineResourceType.class, rawValues[i]), rawFields[i], cmp);
                break;
            case "bbox":
                JSONObject jsonValue = new JSONObject(rawValues[i]);
                FilterBoundingBox bbox = FilterBoundingBox.parseFromValues("WGS:84", jsonValue.getDouble("northBoundLatitude"), jsonValue.getDouble("southBoundLatitude"), jsonValue.getDouble("eastBoundLongitude"), jsonValue.getDouble("westBoundLongitude"));

                if (bbox == null) {
                    throw new IllegalArgumentException("Unable to parse bounding box");
                }

                newFacet = new SearchFacet<FilterBoundingBox>(bbox, rawFields[i], cmp);
                break;
            case "date":
                DateTime value = new DateTime(Long.parseLong(rawValues[i]));
                newFacet = new SearchFacet<DateTime>(value, rawFields[i], cmp);
                break;
            case "string":
                newFacet = new SearchFacet<String>(rawValues[i], rawFields[i], cmp);
                break;
            }

            facets.add(newFacet);
        }
        
        // Construct CSWServiceItems for custom registries is required
        CSWServiceItem[] serviceItems = null;
    	if (serviceTitles != null && serviceUrls != null && recordUrls != null && serviceTypes != null) {
    		serviceItems = new CSWServiceItem[serviceIds.length];
    		for (int i = 0; i < serviceIds.length; i++) {
    			OgcServiceProviderType serviceType = OgcServiceProviderType.Default;
            	switch(serviceTypes[i].toLowerCase()) {
            		case "geoserver":
            			serviceType = OgcServiceProviderType.GeoServer;
            			break;
            		case "arcgis":
            			serviceType = OgcServiceProviderType.ArcGis;
            			break;
            		case "pycsw":
            			serviceType = OgcServiceProviderType.PyCSW;
            			break;
            		case "default":
            		default:
            			serviceType = OgcServiceProviderType.Default;
            			break;
            	}
    			CSWServiceItem item = new CSWServiceItem(serviceIds[i], serviceUrls[i], recordUrls[i], serviceTitles[i], serviceType, CSWServiceItem.DEF_PAGE_SZ);
    			serviceItems[i] = item;
    		}
    	}

        // Make our request and then convert the records for transport to the view
        FacetedMultiSearchResponse response;
        try {
            response = filterService.getFilteredRecords(serviceIds, serviceItems, facets, startIndexes, limit);
            workaroundMissingNCIMetadata(response.getRecords());
        } catch (Exception ex) {
            log.error("Unable to filter records from remote service", ex);
            return generateJSONResponseMAV(false);
        }

        List<ModelMap> viewRecords = new ArrayList<ModelMap>(response.getRecords().size());
        for (CSWRecord record : response.getRecords()) {
            viewRecords.add(viewCSWRecordFactory.toView(record));
        }
        int recordsMatched = 0;
        for(int serviceCount : response.getRecordsMatched().values()) {
        	recordsMatched += serviceCount;
        }
        ModelMap mm = new ModelMap();
        mm.put("startIndexes", response.getStartIndexes());
        mm.put("nextIndexes", response.getNextIndexes());
        mm.put("records", viewRecords);
        mm.put("recordsMatched", recordsMatched);

        return generateJSONResponseMAV(true, mm, "");
    }
        
    /**
     * gets csw record information based on file identifier and service id
     * @param fileIdentifier
     * @param serviceId
     * @return
     */
    @RequestMapping("/getCSWRecord.do")
    public ModelAndView getCSWRecord(
            @RequestParam(value = "fileIdentifier") String fileIdentifier,
            @RequestParam(value = "serviceId") String serviceId) {
    	
    	int startPosition = 1;
    	int maxRecords = 100;    	
    	CSWGetDataRecordsFilter filter = new CSWGetDataRecordsFilter();
    	filter.setFileIdentifier(fileIdentifier);
        List<CSWRecord> records = null;
        int matchedResults = 0;
        try {
                CSWGetRecordResponse response = null;
                response = cswFilterService.getFilteredRecords(serviceId, filter, maxRecords, startPosition);
                workaroundMissingNCIMetadata(response.getRecords());
                records = response.getRecords();
                matchedResults = response.getRecordsMatched();
                return generateJSONResponseMAV(records.toArray(new CSWRecord[records.size()]), matchedResults);
        } catch (Exception ex) {
            log.warn(String.format("Error fetching filtered records for filter '%1$s'", filter), ex);
            return generateJSONResponseMAV(false, null, "Error fetching filtered records");
        }
    }
    

    private boolean nameIsRewriteCandidate(String name) {
        return name.equalsIgnoreCase("Link to Web Map Service") ||
               name.equalsIgnoreCase("Link to Web Coverage Service") ||
               name.contains("S for dataset ") ||
               name.equals("WCS") ||
               name.equals("WMS");
    }

    /**
     * This is a workaround to address Online Resource "name" metadata lacking in records coming from NCI/GA.
     *
     *  Currently we receive a number of records where the Online Resource "name" element comes in the form
     *     + WCS for dataset UUID
     *     + WMS for dataset UUID
     *     + NCSS for dataset UUID
     *
     *  This is unhelpful and forces us to actually go back to the THREDDS instance for more info.
     *
     *  This method iterates the supplied records, searches for the above pattern in online resources and rewrites the online
     *  resources (using WMS GetCapabilities requests) if found. It will be assumed that all online resources matching the pattern
     *  can be rewritten to have the same layer/coverage/variable names.
     * @param records
     * @throws PortalServiceException
     */
    private void workaroundMissingNCIMetadata(List<CSWRecord> records) {
        for (CSWRecord record : records) {
            //Firstly figure out whether there are one or more "bad resources". We identify a bad resource
            //as a WMS with a name matching the pattern
            AbstractCSWOnlineResource layerNameSource = null;
            for (AbstractCSWOnlineResource wmsOr : record.getOnlineResourcesByType(OnlineResourceType.WMS)) {
                if (nameIsRewriteCandidate(wmsOr.getName())) {
                    layerNameSource = wmsOr;
                    break;
                }
            }
            if (layerNameSource == null) {
                continue;
            }

            //Now find our WMS resource that we will use to lookup all the layer/coverage/variable names
            List<String> layerNames = new ArrayList<String>();
            try {
                GetCapabilitiesRecord getCap = wmsService.getWmsCapabilities(layerNameSource.getLinkage().toString(), "1.3.0");
                for (GetCapabilitiesWMSLayerRecord layer : getCap.getLayers()) {
                    String name = layer.getName().trim();
                    if (!StringUtils.isEmpty(name)) {
                        layerNames.add(layer.getName());
                    }
                }
            } catch (Exception ex) {
                log.error("Unable to retrieve WMS capabilities. metadata will not be rewritten: " + ex.getMessage());
                log.debug("Exception:", ex);
                continue;
            }

            //Now seperate our online resources into those being rewritten and those being saved
            List<AbstractCSWOnlineResource> resourcesToSave = new ArrayList<AbstractCSWOnlineResource>();
            for (AbstractCSWOnlineResource or : record.getOnlineResources()) {
                switch (or.getType()) {
                case WCS:
                case WMS:
                case NCSS:
                    if (!nameIsRewriteCandidate(or.getName())) {
                        resourcesToSave.add(or);
                        break;
                    }

                    for (String layerName : layerNames) {
                        resourcesToSave.add(new CSWOnlineResourceImpl(or.getLinkage(), or.getProtocol(), layerName, or.getDescription()));
                    }

                    break;
                default:
                    resourcesToSave.add(or);
                }
            }

            record.setOnlineResources(resourcesToSave);
        }
    }

    /**
     * Gets a list of CSWServiceItem objects that the portal is using for sources of CSWRecords.
     *
     * @return
     */
    @RequestMapping("/getFacetedCSWServices.do")
    public ModelAndView getCSWServices() {
        List<ModelMap> convertedServiceItems = new ArrayList<ModelMap>();

        //Simplify our service items for the view
        for (CSWServiceItem item : this.filterService.getCSWServiceItems()) {
            //VT: skip the loop if we intend to hide this from catalogue.
            if (item.getHideFromCatalogue()) {
                continue;
            }
            ModelMap map = new ModelMap();

            map.put("title", item.getTitle());
            map.put("id", item.getId());
            map.put("url", item.getServiceUrl());
            map.put("type", item.getServerType());
            convertedServiceItems.add(map);
        }

        return generateJSONResponseMAV(true, convertedServiceItems, "");
    }
}
