package org.auscope.portal.core.services.responses.csw;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.suggest.Completion;

//Document(indexName = "auscope-api-cswsuggestion")
@Document(indexName = "#{@environment.getProperty('spring.data.elasticsearch.cswSuggestionIndex')}")
@Setting(settingPath = "autocomplete-analyzer.json")
public class CSWSuggestion {
	
	@Id
	private String id;
	
	@Field(type = FieldType.Text, analyzer = "autocomplete_index", searchAnalyzer = "autocomplete_search", copyTo = {"suggestionCompletion"})
	private String suggestionTerm;
	
	@Field(type = FieldType.Integer)
	private Integer suggestionCount;
	
	@CompletionField(analyzer = "autocomplete_index", searchAnalyzer = "autocomplete_search")
	public Completion suggestionCompletion;
	
	public CSWSuggestion(String term, Integer count) {
		this.id = term;
		this.suggestionTerm = term;
		this.suggestionCount = count;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSuggestionTerm() {
		return suggestionTerm;
	}
	
	public void setSuggestionTerm(String suggestionTerm) {
		this.suggestionTerm = suggestionTerm;
	}
	
	public Integer getSuggestionCount() {
		return suggestionCount;
	}
	
	public void setSuggestionCount(Integer suggesitonCount) {
		this.suggestionCount = suggesitonCount;
		this.suggestionCompletion.setWeight(suggestionCount);
	}

	public Completion getSuggestionCompletion() {
		return suggestionCompletion;
	}

	public void setSuggestionCompletion(Completion suggestionCompletion) {
		this.suggestionCompletion = suggestionCompletion;
	}

}
