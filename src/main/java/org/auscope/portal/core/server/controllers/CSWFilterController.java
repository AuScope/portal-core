package org.auscope.portal.core.server.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
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
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.core.services.responses.csw.CSWGetCapabilities;
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

/**
 * A controller class for marshalling access to the underling CSWFilterService
 * 
 * @author Josh Vote
 */
@Controller
public class CSWFilterController extends BaseCSWController {
    public static final int DEFAULT_MAX_RECORDS = 100;
    private CSWFilterService cswFilterService;
    protected static ConcurrentHashMap<String, KeywordCacheEntity> catalogueKeywordCache;
    protected List<CustomRegistryInt> catalogueOnlyRegistries;
    @Autowired
    private ConversionService converter;

    static {
        catalogueKeywordCache = new ConcurrentHashMap<String, KeywordCacheEntity>();
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
            ViewKnownLayerFactory viewKnownLayerFactory, List<CustomRegistryInt> customRegistries) {
        super(viewCSWRecordFactory, viewKnownLayerFactory);
        this.cswFilterService = cswFilterService;
        this.catalogueOnlyRegistries = customRegistries;

    }

    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
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

        //Simplify our service items for the view
        for (CustomRegistryInt item : this.catalogueOnlyRegistries) {
            ModelMap map = new ModelMap();
            map.put("title", item.getTitle());
            map.put("id", item.getId());
            map.put("url", item.getServiceUrl());
            convertedServiceItems.add(map);
        }

        return generateJSONResponseMAV(true, convertedServiceItems, "");
    }

    private void generateKeywordCache(String cswServiceId) throws Exception {

        KeywordCacheEntity keywordCacheEntity = new KeywordCacheEntity();

        CSWGetRecordResponse response = null;

        int startPosition = 1;

        do {
            try {
                response = cswFilterService.getFilteredRecords(cswServiceId, null, DEFAULT_MAX_RECORDS, startPosition);
            } catch (IllegalArgumentException e) {
                response = cswFilterService.getFilteredRecords(this.getCustomRegistry(cswServiceId), null,
                        DEFAULT_MAX_RECORDS, startPosition);
            }

            for (CSWRecord record : response.getRecords()) {

                for (String recordKeyword : record.getDescriptiveKeywords()) {
                    if (!recordKeyword.startsWith("association:")) {
                        keywordCacheEntity.addTo(recordKeyword, 1);
                    }
                }

            }
            // Prepare to request next 'page' of records (if required)
            if (response.getNextRecord() > response.getRecordsMatched() || response.getNextRecord() <= 0) {
                startPosition = -1; // we are done in this case
            } else {
                startPosition = response.getNextRecord();
            }
        } while (startPosition > 0);
        this.catalogueKeywordCache.put(cswServiceId, keywordCacheEntity);

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

        return generateJSONResponseMAV(true, modelMap, "success");
    }

    /**
     * Get CSW keywords related to the registry
     */
    @RequestMapping("/getFilteredCSWKeywords.do")
    public ModelAndView getFilteredCSWKeywords(
            @RequestParam(value = "cswServiceIds", required = false) String[] cswServiceIds,
            @RequestParam(value = "keyword", required = true, defaultValue = "") String keyword) {

        try {

            if (cswServiceIds == null) {
                return generateJSONResponseMAV(true);
            }
            //VT: if the cache keyword has not been generated, generate it.
            for (String cswServiceId : cswServiceIds) {
                if (!CSWFilterController.catalogueKeywordCache.containsKey(cswServiceId)) {
                    try {
                        this.generateKeywordCache(cswServiceId);
                    } catch (IllegalArgumentException ex) {
                        //VT: if the key does not exist, it does not matter.
                        log.info(String.format("serviceId '%s' DNE", cswServiceId));
                    }
                }
            }

            List<ModelMap> resultModalMap = new ArrayList<ModelMap>();
            KeywordCacheEntity keywordCacheEntity = new KeywordCacheEntity();
            //VT: this is to append the results from the different registeries
            for (String cswServiceId : cswServiceIds) {
                if (this.catalogueKeywordCache.get(cswServiceId) != null) {
                    keywordCacheEntity.append(this.catalogueKeywordCache.get(cswServiceId));
                }
            }
            //VT: if no keyword is found, just return.
            if (keywordCacheEntity.getKeywordPair().keySet().size() <= 0) {
                return generateJSONResponseMAV(true);
            }

            //VT: Put the accumalated results into a ModalMap.
            for (String key : keywordCacheEntity.getKeywordPair().keySet()) {
                if (!key.toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
                ModelMap modelMap = new ModelMap();
                modelMap.put("keyword", key);
                modelMap.put("count", keywordCacheEntity.getKeywordPair().get(key));
                resultModalMap.add(modelMap);
            }

            return generateJSONResponseMAV(true, resultModalMap, "");
        } catch (Exception ex) {
            log.warn(String.format("Error updating keyword cache", ex));
            log.warn("Exception: ", ex);
            ex.printStackTrace();
            return generateJSONResponseMAV(false, null, "Error Generating keyword");
        }

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
            if (!customRegistries.isEmpty()) {
                records = new ArrayList<CSWRecord>();
                CSWGetRecordResponse response = cswFilterService.getFilteredRecords(customRegistries, filter,
                        maxRecords == null ? DEFAULT_MAX_RECORDS : maxRecords, startPosition);

                records = response.getRecords();
                matchedResults = response.getRecordsMatched();

            } else {
                CSWGetRecordResponse response = null;
                //VT: if it returns an exception, try finding it in the customRegistry
                try {
                    response = cswFilterService.getFilteredRecords(cswServiceId, filter,
                            maxRecords == null ? DEFAULT_MAX_RECORDS : maxRecords, startPosition);
                } catch (IllegalArgumentException e) {
                    response = cswFilterService.getFilteredRecords(this.getCustomRegistry(cswServiceId), filter,
                            maxRecords == null ? DEFAULT_MAX_RECORDS : maxRecords, startPosition);
                }
                records = response.getRecords();
                matchedResults = response.getRecordsMatched();
            }

            return generateJSONResponseMAV(records.toArray(new CSWRecord[records.size()]), matchedResults);
        } catch (Exception ex) {
            log.warn(String.format("Error fetching filtered records for filter '%1$s'", filter), ex);
            return generateJSONResponseMAV(false, null, "Error fetching filtered records");
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

        String[] keywords = null;
        if (parameters.get("keywords") != null) {
            keywords = parameters.get("keywords").split(",");
        }
        KeywordMatchType keywordMatchType = null;
        String capturePlatform = parameters.get("capturePlatform");
        String sensor = parameters.get("sensor");
        String abstrac = parameters.get("abstract");
        String title = parameters.get("title");

        if (parameters.get("keywordMatchType") != null) {
            if (parameters.get("keywordMatchType").toLowerCase().equals("any")) {
                keywordMatchType = KeywordMatchType.Any;
            } else {
                keywordMatchType = KeywordMatchType.All;
            }
        }

        //Firstly generate our filter
        FilterBoundingBox filterBbox = attemptParseBBox(westBoundLongitude, eastBoundLongitude,
                northBoundLatitude, southBoundLatitude);
        CSWGetDataRecordsFilter filter = new CSWGetDataRecordsFilter(anyText, filterBbox, keywords, capturePlatform,
                sensor, keywordMatchType, abstrac, title, CSWGetDataRecordsFilter.Type.dataset);
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

    private CustomRegistryInt getCustomRegistry(String id) {
        for (CustomRegistryInt cr : this.catalogueOnlyRegistries) {
            if (cr.getId().equals(id)) {
                return cr;
            }
        }
        return null;
    }

    protected class KeywordCacheEntity {
        private HashMap<String, Integer> keywordPair;

        protected KeywordCacheEntity(HashMap<String, Integer> keywordPair) {
            this.keywordPair = keywordPair;
        }

        protected KeywordCacheEntity() {
            this.keywordPair = new HashMap<String, Integer>();
        }

        protected void addTo(String keyword, int count) {
            if (keywordPair.containsKey(keyword)) {
                keywordPair.put(keyword, keywordPair.get(keyword) + count);
            } else {
                keywordPair.put(keyword, count);
            }
        }

        protected void addReplace(String keyword, int count) {
            keywordPair.put(keyword, count);
        }

        protected HashMap<String, Integer> getKeywordPair() {
            return keywordPair;
        }

        protected KeywordCacheEntity append(KeywordCacheEntity toAppend) {
            HashMap<String, Integer> appendKeywordPair = toAppend.getKeywordPair();
            for (String appendKey : appendKeywordPair.keySet()) {
                if (this.keywordPair.containsKey(appendKey)) {
                    this.keywordPair.put(appendKey, this.keywordPair.get(appendKey) + appendKeywordPair.get(appendKey));
                } else {
                    this.keywordPair.put(appendKey, appendKeywordPair.get(appendKey));
                }
            }
            return this;
        }

    }
}
