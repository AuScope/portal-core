package org.auscope.portal.core.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.auscope.portal.core.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SearchController extends BasePortalController {

	private SearchService searchService;
	
	
	@Autowired
	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}
	
	/**
	 * Search index based on supplied query and/or spatial relationship
	 * 
	 * @param searchFields document fields to search
	 * @param query text query
	 * @param spatialRelation "Intersects", "Contains" or "Within" (optional)
	 * @param southBoundLatitude south bounding box point (optional)
	 * @param westBoundLongitude west bounding box point (optional)
	 * @param eastBoundLongitude east bounding box point (optional)
	 * @param northBoundLatitude north bounding box point (optional)
	 * @return array of layer IDs for search results
	 */
	@RequestMapping("/searchLayersAndRecords.do")
	public ModelAndView searchLayersAndRecords(
			@RequestParam("searchFields") String[] searchFields, @RequestParam("query") String query, @RequestParam(value="spatialRelation", required=false) String spatialRelation,
			@RequestParam(value="southBoundLatitude", required=false) Double southBoundLatitude, @RequestParam(value="westBoundLongitude", required=false) Double westBoundLongitude,
			@RequestParam(value="eastBoundLongitude", required=false) Double eastBoundLongitude, @RequestParam(value="northBoundLatitude", required=false) Double northBoundLatitude) {
		ArrayList<String> idResults = new ArrayList<String>();
		try {
			List<Document> docs = this.searchService.searchIndex(searchFields, query, spatialRelation, southBoundLatitude, westBoundLongitude, northBoundLatitude, eastBoundLongitude);			
			for(Document d: docs) {
				String id = d.get("id");
				if(!idResults.contains(id)) {
					idResults.add(id);
				}
			}
		} catch(ParseException pe) {
			return generateJSONResponseMAV(false, null, pe.getLocalizedMessage());
		} catch(IOException ioe) {
			return generateJSONResponseMAV(false, null, ioe.getLocalizedMessage());
		}
		return generateHTMLResponseMAV(true, idResults.toArray(), null);
	}

	/**
	 * Search keywords
	 * 
	 * @return an array of keyword search results
	 */
	@RequestMapping("/getSearchKeywords.do")
	public ModelAndView searchKeywords() {
		List<String> keywordResults = new ArrayList<String>();
		try {
			keywordResults = this.searchService.getUniqueTerms("keyword");
		} catch(ParseException pe) {
			return generateJSONResponseMAV(false, pe.getLocalizedMessage(), null);
		} catch(IOException ioe) {
			return generateJSONResponseMAV(false, ioe.getLocalizedMessage(), null);
		}
		return generateHTMLResponseMAV(true, keywordResults.toArray(), null);
	}

	
}
