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
	 * Search index
	 * 
	 * @param searchFields fields to search
	 * @param query query for searching
	 * @return List of layer IDs matching search
	 */
	@RequestMapping("/searchLayersAndRecords.do")
	public ModelAndView searchLayersAndRecords(@RequestParam("searchFields") String[] searchFields, @RequestParam("query") String query) {
		ArrayList<String> idResults = new ArrayList<String>();
		try {
			List<Document> docs = this.searchService.searchIndex(searchFields, query);
			for(Document d: docs) {
				String id = d.get("type").equals("layer") ? d.get("id") : d.get("layerId");
				if(!idResults.contains(id)) {
					idResults.add(id);
				}
			}
		} catch(ParseException pe) {
			return generateJSONResponseMAV(false, "Error parsing search results: " + pe.getLocalizedMessage(), null);
		} catch(IOException ioe) {
			return generateJSONResponseMAV(false, "Error searching: " + ioe.getLocalizedMessage(), null);
		}
		return generateHTMLResponseMAV(true, idResults.toArray(), null);
	}
	
}
