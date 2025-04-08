package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.ElasticsearchService;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.es.CSWRecordSearchResponse;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ElasticsearchController extends BasePortalController {
	
	@Autowired
	ElasticsearchService elasticsearchService;
	
	@Autowired
	ViewCSWRecordFactory viewCSWRecordFactory;
	

	/**
	 * Search the CSW record index.
	 * If using spatial search, the spatial relation parameter (spatialRelation) must be set and EITHER
	 * the four parameters representing the bounding box must be specified OR a list of points supplied
	 * of the form "latitude longitude" e.g. "-25.885 129.286".
	 * 
	 * @param query the search term
	 * @param queryFields the fields to search (optional)
	 * @param page the page number (optional)
	 * @param pageSize the page size (optional)
	 * @param ogcServices OGC services to search to filter search (optional)
	 * @param spatialRelation the spatial relation type (e.g. "Intersects") (optional)
	 * @param westBoundLongitude west longitude bound of bounding box for spatial searching (optional)
	 * @param eastBoundLongitude east longitude bound of bounding box for spatial searching (optional)
	 * @param southBoundLatitude south latitude bound of bounding box for spatial searching (optional)
	 * @param northBoundLatitude south latitude bound of bounding box for spatial searching (optional)
	 * @param points a list of points representing a polygon for spatial searching (optional)
	 * @return a list of CSWRecord objects and KnownLayer IDs matching the search criteria
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
    		@RequestParam(value = "northBoundLatitude", required = false) Double northBoundLatitude,
    		@RequestParam(value = "points", required = false) List<Point> points) {
    	CSWRecordSearchResponse response = this.elasticsearchService.searchCSWRecords(
    			query, queryFields, page, pageSize, ogcServices, spatialRelation, westBoundLongitude, eastBoundLongitude,
    			southBoundLatitude, northBoundLatitude, points);
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
    
    @RequestMapping("/suggestTerms.do")
    public ModelAndView suggestTerms(@RequestParam(value = "query") String query) {
    	List<String> result = elasticsearchService.suggestTerms(query);
    	return generateJSONResponseMAV(true, result.toArray(new String[result.size()]), "");
    }
    
}
