package org.auscope.portal.core.services.responses.csw;

import java.util.Arrays;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Document(indexName = "#{@environment.getProperty('spring.data.elasticsearch.cswSuggestionIndex')}")
@Setting(settingPath = "autocomplete-analyzer.json")
public class CSWSuggestion {
	
	// ID is somewhat redundant as term is saved in Completion field but will ensure words only appear once in index 
	@Id
	private String id;
	
	@CompletionField(analyzer = "autocomplete_index", searchAnalyzer = "autocomplete_search")
	public Completion suggestionCompletion;
	
	public CSWSuggestion(String term, Integer count) {
		this.id = term;
		// Manually create Completion and set weight to be the occurrence count so
		// suggestion queries prioritise word occurrence 
		this.suggestionCompletion = new Completion(Arrays.asList(term));
		this.suggestionCompletion.setWeight(count);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Completion getSuggestionCompletion() {
		return suggestionCompletion;
	}

	public void setSuggestionCompletion(Completion suggestionCompletion) {
		this.suggestionCompletion = suggestionCompletion;
	}

}
