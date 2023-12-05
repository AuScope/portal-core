package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.ESSearchService;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.es.CSWRecordSearchResponse;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ESSearchController extends BasePortalController {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	ESSearchService esSearchService;
	
	@Autowired
	ViewCSWRecordFactory viewCSWRecordFactory;
	
	/**
	 * Search the CSW record index
	 * @param query
	 * @param queryFields
	 * @param cswRecordsPageNo
	 * @param ogcServices
	 * @param spatialRelation
	 * @param westBoundLongitude
	 * @param eastBoundLongitude
	 * @param southBoundLatitude
	 * @param northBoundLatitude
	 * @return
	 */
    @RequestMapping("/searchCSWRecords.do")
    public ModelAndView searchKnownLayersAndCSWRecords(
    		@RequestParam(value = "query") String query,
    		@RequestParam(value = "fields", required = false) List<String> queryFields,
    		@RequestParam(value = "page", required = false) Integer page,
    		@RequestParam(value = "pageSize", required = false) Integer pageSize,
    		@RequestParam(value = "ogcServices", required = false) List<String> ogcServices,
    		@RequestParam(value = "spatialRelation", required = false) String spatialRelation,
    		@RequestParam(value = "westBoundLongitude", required = false) Double westBoundLongitude,
    		@RequestParam(value = "eastBoundLongitude", required = false) Double eastBoundLongitude,
    		@RequestParam(value = "southBoundLatitude", required = false) Double southBoundLatitude,
    		@RequestParam(value = "northBoundLatitude", required = false) Double northBoundLatitude) {
    	CSWRecordSearchResponse response = this.esSearchService.searchCSWRecords(
    			query, queryFields, page, pageSize, ogcServices, spatialRelation, westBoundLongitude, eastBoundLongitude,
    			southBoundLatitude, northBoundLatitude);
    	
    	ModelMap modelMap = new ModelMap();
    	modelMap.put("totalCSWRecordHits", response.getTotalCSWRecordHits());
    	
    	List<ModelMap> cswRecords = new ArrayList<ModelMap>(response.getCSWRecords().size());
    	for (CSWRecord record: response.cswRecords) {
    		cswRecords.add(viewCSWRecordFactory.toView(record));
    	}
    	modelMap.put("cswRecords", cswRecords);

    	modelMap.put("knownLayerIds", response.getKnownLayerIds());
    	
        return generateJSONResponseMAV(true, modelMap, "");
    }
    
    /**
     * 
     * @param query
     * @return
     */
    @RequestMapping("/getIndexSuggestions.do")
    public ModelAndView prefixQueryCSWRecords2(@RequestParam(value = "query") String query) {
    	List<String> result = esSearchService.getPrefixHighlights(query);
    	/*
    	try {
	    	CompletableFuture<List<String>> records = this.esSearchService.getIndexSuggestions(query);
	    	CompletableFuture.allOf(records);
	   		result = records.get();
    	} catch(Exception e) {
    		log.error("Error retrieving suggestions: " + e.getLocalizedMessage());
    		return generateJSONResponseMAV(false, null, e.getLocalizedMessage());
    	}
    	*/
    	return generateJSONResponseMAV(true, result.toArray(new String[result.size()]), "");
    }
    
    @RequestMapping("/suggestTerms.do")
    public ModelAndView suggestTerms(@RequestParam(value = "query") String query) {
    	List<String> result = esSearchService.suggestTerms(query);
    	return generateJSONResponseMAV(true, result.toArray(new String[result.size()]), "");
    }
    
}
