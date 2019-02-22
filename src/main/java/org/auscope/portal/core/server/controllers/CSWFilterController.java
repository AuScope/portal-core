package org.auscope.portal.core.server.controllers;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.auscope.portal.core.server.controllers.BaseCSWController;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.custom.CustomRegistry;
import org.auscope.portal.core.services.csw.custom.CustomRegistryInt;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.SortType;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.core.services.responses.csw.CSWGetCapabilities;
import org.auscope.portal.core.services.responses.csw.CSWGetDomainResponse;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static org.auscope.portal.core.util.DateUtil.stringYearToDate;

/**
 * A controller class for marshalling access to the underling CSWFilterService
 * 
 * @author Josh Vote
 */
@Controller
public class CSWFilterController extends BaseCSWController {
    public static final int DEFAULT_MAX_RECORDS = 100;
    private static final char SINGLE_CHAR_WILDCARD = '#';
    private CSWFilterService cswFilterService;
    protected static ConcurrentHashMap<String, Set> catalogueKeywordCache;

    @Autowired
    private ConversionService converter;

    static {
        catalogueKeywordCache = new ConcurrentHashMap<String, Set>();
    }

    /**
     * Creates a new CSWFilterController with the specified dependencies.
     * 
     * @param cswFilterService
     *            Used to make filtered CSW requests
     * @param viewCSWRecordFactory
     *            Used to transform CSWRecords for the view
     */
    @Autowired
    public CSWFilterController(CSWFilterService cswFilterService,
            ViewCSWRecordFactory viewCSWRecordFactory,
            ViewKnownLayerFactory viewKnownLayerFactory) {
        super(viewCSWRecordFactory, viewKnownLayerFactory);
        this.cswFilterService = cswFilterService;

    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
    	if(binder.getConversionService() == null)
    		binder.setConversionService(converter);
    }

    /**
     * Attempts to parse a FilterBoundingBox from the given coords (if they exist). Returns null on failure
     * 
     * @return
     */
    private FilterBoundingBox attemptParseBBox(Double westBoundLongitude, Double eastBoundLongitude,
            Double northBoundLatitude, Double southBoundLatitude) {
        FilterBoundingBox filterBbox = null;
        if (westBoundLongitude != null && eastBoundLongitude != null &&
                northBoundLatitude != null && southBoundLatitude != null) {
            filterBbox = new FilterBoundingBox("",
                    new double[] {eastBoundLongitude, southBoundLatitude},
                    new double[] {westBoundLongitude, northBoundLatitude});
        }

        return filterBbox;
    }

    /**
     * Gets a list of CSWServiceItem objects that the portal is using for sources of CSWRecords.
     * 
     * @return
     */
    @RequestMapping("/getCSWServices.do")
    public ModelAndView getCSWServices() {
        List<ModelMap> convertedServiceItems = new ArrayList<ModelMap>();

        //Simplify our service items for the view
        for (CSWServiceItem item : this.cswFilterService.getCSWServiceItems()) {
            //VT: skip the loop if we intend to hide this from catalogue.
            if (item.getHideFromCatalogue()) {
                continue;
            }
            ModelMap map = new ModelMap();

            map.put("title", item.getTitle());
            map.put("id", item.getId());
            map.put("url", item.getServiceUrl());

            if (item.getId().toLowerCase().contains("auscope")) {
                map.put("selectedByDefault", true);
            }
            convertedServiceItems.add(map);
        }

        return generateJSONResponseMAV(true, convertedServiceItems, "");
    }


    /**
     *
     * @param serviceId
     * @throws Exception
     */
    private void generateKeywordCache(String serviceId) throws Exception {
        Set<String> keywordList = new HashSet<String>();
        final String getDomain = "GetDomain";
        final String keywordTerm = "Subject";
        CSWGetCapabilities cswGetCapabilities = cswFilterService.getCapabilitiesByServiceId(serviceId);

        if (cswGetCapabilities.getOperations().contains(getDomain)){
            CSWGetDomainResponse response;
            response = cswFilterService.getDomainResponse(serviceId, keywordTerm);
            keywordList = response.getDomainValues();
        } else {
            int startPosition = 1;
            do {
                CSWGetRecordResponse response = cswFilterService.getFilteredRecords(serviceId, null, DEFAULT_MAX_RECORDS, startPosition);
                for (CSWRecord record : response.getRecords()) {

                    keywordList.addAll(Arrays.asList(record.getDescriptiveKeywords()));

                }
                // Prepare to request next 'page' of records (if required)
                if (response.getNextRecord() > response.getRecordsMatched() || response.getNextRecord() <= 0) {
                    startPosition = -1; // we are done in this case
                } else {
                    startPosition = response.getNextRecord();
                }
            } while (startPosition > 1);
        }

        catalogueKeywordCache.put(serviceId,keywordList);
    }

    /**
     * Returns getCapabilities result. For the moment we only require the title but more can be added on as needed.
     * 
     * @param cswServiceUrl
     * @return
     * @throws Exception
     */
    @RequestMapping("/getCSWGetCapabilities.do")
    public ModelAndView getCSWGetCapabilities(
            @RequestParam(value = "cswServiceUrl", required = true) String cswServiceUrl) throws Exception {

        CSWGetCapabilities cswGetCapabilities = cswFilterService.getCapabilities(cswServiceUrl);
        ModelMap modelMap = new ModelMap();
        if (cswGetCapabilities.getTitle() != null && !cswGetCapabilities.getTitle().isEmpty()) {
            modelMap.put("title", cswGetCapabilities.getTitle());
        } else {
            URI uri = new URI(cswServiceUrl);
            modelMap.put("title", uri.getHost());

        }
        modelMap.put("operations",cswGetCapabilities.getOperations());

        return generateJSONResponseMAV(true, modelMap, "success");
    }

    /**
     * Gets a list of CSWRecord view objects filtered by the specified values from all internal CSW's
     *
     *            [Optional] A sensor filter
     * @param startPosition
     *            [Optional] 0 based index indicating what index to start reading records from (only applicable if cswServiceId is specified)
     * @return
     */
    @RequestMapping("/getFilteredCSWRecords.do")
    public ModelAndView getFilteredCSWRecords(
            @RequestParam(value = "key", required = false) String[] keys,
            @RequestParam(value = "value", required = false) String[] values,
            @RequestParam(value = "limit", required = false) Integer maxRecords,
            @RequestParam(value = "start", required = false, defaultValue = "1") Integer startPosition,
            @RequestParam(value = "customregistries", required = false) CustomRegistryInt customRegistries) {

        if (startPosition != null) {
            startPosition++;
        }

        HashMap<String, String> parameters = this.arrayPairtoMap(keys, values);
        String cswServiceId = parameters.get("cswServiceId");

        CSWGetDataRecordsFilter filter = this.getFilter(parameters);

        //Then make our requests to all of CSW's
        List<CSWRecord> records = null;
        int matchedResults = 0;
        try {
            //We may be requesting from all CSW's or just a specific one
            if (customRegistries != null && !customRegistries.isEmpty()) {
                records = new ArrayList<CSWRecord>();
                CSWGetRecordResponse response = cswFilterService.getFilteredRecords(customRegistries, filter,
                        maxRecords == null ? DEFAULT_MAX_RECORDS : maxRecords, startPosition);

                records = response.getRecords();
                matchedResults = response.getRecordsMatched();

            } else {
                CSWGetRecordResponse response = null;
                //VT: if it returns an exception, try finding it in the customRegistry

                    response = cswFilterService.getFilteredRecords(cswServiceId, filter,
                            maxRecords == null ? DEFAULT_MAX_RECORDS : maxRecords, startPosition);

                records = response.getRecords();
                matchedResults = response.getRecordsMatched();
            }

            return generateJSONResponseMAV(records.toArray(new CSWRecord[records.size()]), matchedResults);
        } catch (Exception ex) {
            log.warn(String.format("Error fetching filtered records for filter '%1$s'", filter), ex);
            return generateJSONResponseMAV(false, null, "Error fetching filtered records");
        }
    }

    /**
     * Get CSW Keywords related to the registry
     * @param cswServiceIds
     * @param keyword
     * @return Set of keywords without counts, for quicker performance
     */
    @RequestMapping("/getFilteredCSWKeywords.do")
    public ModelAndView getFilteredCSWKeywords(
            @RequestParam(value = "cswServiceIds", required = false) String[] cswServiceIds,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) {

        try {
            if (cswServiceIds == null) {
                return generateJSONResponseMAV(true);
            }

            for (String serviceId : cswServiceIds) {
                if (!catalogueKeywordCache.containsKey(serviceId)) {
                    try {
                        this.generateKeywordCache(serviceId);
                    } catch (IllegalArgumentException ex) {
                        //VT: if the key does not exist, it does not matter.
                        log.info(String.format("serviceId '%s' DNE", serviceId));
                    }
                }
            }

            List<ModelMap> returnedKeywords = new ArrayList<ModelMap>();
            Set<String> keywordSet = new HashSet<String>();
            //VT: this is to append the results from the different registries
            for (String serviceId : cswServiceIds) {
                if (catalogueKeywordCache.get(serviceId) != null) {
                    keywordSet.addAll(catalogueKeywordCache.get(serviceId));
                }
            }
            //VT: if no keyword is found, just return.
            if (keywordSet.isEmpty()) {
                return generateJSONResponseMAV(true);
            }

            //VT: Put the accumulated results into a ModalMap.
            for (String k : keywordSet) {
                if (!k.toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
                ModelMap modelMap = new ModelMap();
                modelMap.put("keyword", k);
                returnedKeywords.add(modelMap);
            }

            return generateJSONResponseMAV(true, returnedKeywords, "");
        } catch (Exception e) {
            log.warn(String.format("Error updating keyword cache %s", e));
            log.warn("Exception: ", e);
            e.printStackTrace();
            return generateJSONResponseMAV(false, null, "Error Generating keyword");
        }
    }

    private CSWGetDataRecordsFilter getFilter(HashMap<String, String> parameters) {

        String anyText = parameters.get("anyText");
        Double westBoundLongitude = null;
        Double eastBoundLongitude = null;
        Double northBoundLatitude = null;
        Double southBoundLatitude = null;

        if (parameters.get("west") != null && parameters.get("west").length() > 0) {
            westBoundLongitude = Double.parseDouble(parameters.get("west"));
        }
        if (parameters.get("east") != null && parameters.get("east").length() > 0) {
            eastBoundLongitude = Double.parseDouble(parameters.get("east"));
        }
        if (parameters.get("north") != null && parameters.get("north").length() > 0) {
            northBoundLatitude = Double.parseDouble(parameters.get("north"));
        }
        if (parameters.get("south") != null && parameters.get("south").length() > 0) {
            southBoundLatitude = Double.parseDouble(parameters.get("south"));
        }

        CSWGetDataRecordsFilter.Type type = CSWGetDataRecordsFilter.Type.all;
        if (parameters.get("type") != null) {
            if (parameters.get("type").toLowerCase().equals("dataset")) {
                type = CSWGetDataRecordsFilter.Type.dataset;
            } else if (parameters.get("type").toLowerCase().equals("service")) {
                type = CSWGetDataRecordsFilter.Type.service;
            }
        }

        String[] keywords = null;
        if (parameters.get("keywords") != null) {
            keywords = parameters.get("keywords").split(",");
            for (int i = 0; i < keywords.length; i++) {
                if (keywords[i] != null) {
                    keywords[i] = keywords[i].replace(' ', SINGLE_CHAR_WILDCARD);
                }
            }
        }
        KeywordMatchType keywordMatchType = null;

        String capturePlatform = parameters.get("capturePlatform");
        String sensor = parameters.get("sensor");
        String abstrac = parameters.get("abstract");
        String title = parameters.get("title");
        String fileIdentifier = parameters.get("fileIdentifier");


        if (parameters.get("keywordMatchType") != null) {
            if (parameters.get("keywordMatchType").toLowerCase().equals("any")) {
                keywordMatchType = KeywordMatchType.Any;
            } else {
                keywordMatchType = KeywordMatchType.All;
            }
        }

        // AusGIN parameters
        String titleOrAbstract = parameters.get("titleOrAbstract");
        String authorSurname = parameters.get("authorSurname");
        String onlineResourceType = parameters.get("onlineResourceType");
        String publicationDateFrom = parameters.get("publicationDateFrom");
        String publicationDateTo = parameters.get("publicationDateTo");
        String basicSearchTerm = parameters.get("basicSearchTerm");
        String sortType = parameters.get("sortType");

        //Firstly generate our filter
        FilterBoundingBox filterBbox = attemptParseBBox(westBoundLongitude, eastBoundLongitude,
                northBoundLatitude, southBoundLatitude);
        CSWGetDataRecordsFilter filter = new CSWGetDataRecordsFilter(anyText, filterBbox, keywords, capturePlatform,
                sensor, keywordMatchType, abstrac, title, type) ;

        // Populate filter with AusGIN parameters
        filter.setTitleOrAbstract(titleOrAbstract != null ? titleOrAbstract : null);
        filter.setAuthorSurname(authorSurname != null ? authorSurname : null);
        filter.setBasicSearchTerm(basicSearchTerm != null ? basicSearchTerm : null);
        filter.setOnlineResourceType(onlineResourceType != null ? onlineResourceType : null);
        filter.setPublicationDateFrom(
                publicationDateFrom != null ? stringYearToDate(publicationDateFrom.trim(), false) : null);
        filter.setPublicationDateTo(publicationDateTo != null ? stringYearToDate(publicationDateTo.trim(), true) : null);
        filter.setSortType(SortType.getByStringValue(sortType));
        
        filter.setFileIdentifier(fileIdentifier);

        log.debug(String.format("filter '%1$s'", filter));
        return filter;
    }

    private HashMap<String, String> arrayPairtoMap(String[] keys, String[] values) {
        HashMap<String, String> results = new HashMap<String, String>();
        if (keys == null) {
            return results;
        }
        for (int i = 0; i < keys.length; i++) {
            results.put(keys[i], values[i]);
        }
        return results;
    }

    /**
     * Gets a list of CSWRecord view objects filtered by the specified values from all internal CSW's
     * 
     * @param cswServiceId
     *            [Optional] The ID of a CSWService to query (if omitted ALL CSWServices will be queried)
     * @param westBoundLongitude
     *            [Optional] Spatial bbox constraint
     * @param eastBoundLongitude
     *            [Optional] Spatial bbox constraint
     * @param northBoundLatitude
     *            [Optional] Spatial bbox constraint
     * @param southBoundLatitude
     *            [Optional] Spatial bbox constraint
     * @param keywords
     *            [Optional] One or more keywords to filter by
     * @param keywordMatchType
     *            [Optional] how the keyword list will be matched against records
     * @param capturePlatform
     *            [Optional] A capture platform filter
     * @param sensor
     *            [Optional] A sensor filter
     * @return
     */
    @RequestMapping("/getFilteredCSWRecordsCount.do")
    public ModelAndView getFilteredCSWRecordsCount(
            @RequestParam(value = "cswServiceId", required = true) String cswServiceId,
            @RequestParam(value = "anyText", required = false) String anyText,
            @RequestParam(value = "westBoundLongitude", required = false) Double westBoundLongitude,
            @RequestParam(value = "eastBoundLongitude", required = false) Double eastBoundLongitude,
            @RequestParam(value = "northBoundLatitude", required = false) Double northBoundLatitude,
            @RequestParam(value = "southBoundLatitude", required = false) Double southBoundLatitude,
            @RequestParam(value = "keyword", required = false) String[] keywords,
            @RequestParam(value = "keywordMatchType", required = false) KeywordMatchType keywordMatchType,
            @RequestParam(value = "capturePlatform", required = false) String capturePlatform,
            @RequestParam(value = "sensor", required = false) String sensor,
            @RequestParam(value = "maxRecords", required = false) Integer maxRecords) {

        //Firstly generate our filter
        FilterBoundingBox filterBbox = attemptParseBBox(westBoundLongitude, eastBoundLongitude,
                northBoundLatitude, southBoundLatitude);
        CSWGetDataRecordsFilter filter = new CSWGetDataRecordsFilter(anyText, filterBbox, keywords, capturePlatform,
                sensor, keywordMatchType, null, null, CSWGetDataRecordsFilter.Type.dataset);
        log.debug(String.format("filter '%1$s'", filter));

        //Then make our requests to all of CSW's
        int count = 0;
        int maxRecordsInt = maxRecords == null ? 0 : maxRecords;
        try {
            if (cswServiceId == null || cswServiceId.isEmpty()) {
                count = cswFilterService.getFilteredRecordsCount(filter, maxRecordsInt);
            } else {
                count = cswFilterService.getFilteredRecordsCount(cswServiceId, filter, maxRecordsInt);
            }
        } catch (Exception ex) {
            log.warn(String.format("Error fetching filtered record count for filter '%1$s'", filter), ex);
            return generateJSONResponseMAV(false, null, "Error fetching filtered record count");
        }

        return generateJSONResponseMAV(true, count, "");
    }

}
