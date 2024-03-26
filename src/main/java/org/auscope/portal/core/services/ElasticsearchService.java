package org.auscope.portal.core.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.repositories.CSWRecordRepository;
import org.auscope.portal.core.repositories.CSWSuggestionRepository;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWSuggestion;
import org.auscope.portal.core.services.responses.es.CSWRecordSearchResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.geo.GeoJsonPolygon;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import com.google.common.collect.Lists;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;


/**
 * Elasticsearch search service.
 * Contains methods for indexing CSWrecords and suggestions as well as querying those indices.
 */
public class ElasticsearchService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Value("${spring.data.elasticsearch.cluster-nodes}")
	private String elasticsearchNodesUrl;
	
	@Value("${spring.data.elasticsearch.apiKey:null}")
	private String apiKey;
	
	@Value("${spring.data.elasticsearch.port}")
	private Integer elasticsearchPort;
	
	@Value("${spring.data.elasticsearch.cswRecordIndex}")
    private String cswRecordIndex;
	
	@Value("${spring.data.elasticsearch.cswSuggestionIndex}")
    private String cswSuggestionIndex;
	
	private URL elasticsearchSuggestionUrl;
	
	// CSWRecord search fields
	public static final List<String> CSWRECORD_QUERY_FIELDS = Arrays.asList(new String[]{
		// Native CSWRecord fields
		"fileIdentifier", "serviceName", "descriptiveKeywords", "dataIdentificationAbstract", "knownLayerNames", "knownLayerDescriptions",
		// Nested onlineResources fields (OnlineResource)
		"onlineResources.name", "onlineResources.description"
	});
	
	@Autowired
	private CSWRecordRepository recordRepository;
	
	@Autowired
	private CSWSuggestionRepository suggestionRepository;
	
	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	// Service caller will be used for making suggestion Completion calls direct to Elasticsearch
	private HttpServiceCaller httpServiceCaller;
	
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;
	
	@Autowired
	ElasticsearchClient elasticsearchClient;

	
	public ElasticsearchService(HttpServiceCaller httpServiceCaller) {
		this.httpServiceCaller = httpServiceCaller;
	}
	
	/**
	 * Construct the elasticsearchUrl from the settings.
	 * E.g. https://elasticsearch-server.com/suggestion-index/_search
	 */
	@PostConstruct
	private void setElasticsearchUrl() {
		try {
			URIBuilder uriBuilder = new URIBuilder(elasticsearchNodesUrl);
			if (elasticsearchPort != null) {
				uriBuilder.setPort(elasticsearchPort);
			}
	    	uriBuilder.setPathSegments(cswSuggestionIndex, "_search");
	    	elasticsearchSuggestionUrl = uriBuilder.build().toURL();
		} catch(Exception e) {
			log.error("Unable to build Elasticsearch URL: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Index CSWRecords.
	 * Creates a bounding polygon for each record from the CSWGeographicElement.
	 * 
	 * @param cswRecords the CSWRecord list
	 */
	public void indexCSWRecords(final List<CSWRecord> cswRecords) throws DataAccessResourceFailureException {
		log.info("Indexing CSW records");
		// Check geographical bounds
		for (CSWRecord record: cswRecords) {
			if(record.getCSWGeographicElements() != null && record.getCSWGeographicElements().length > 0) {
				for (CSWGeographicElement bbox: record.getCSWGeographicElements()) {
					double southLat = bbox.getSouthBoundLatitude();
					double westLong = bbox.getWestBoundLongitude();
					double northLat = bbox.getNorthBoundLatitude();
					double eastLong = bbox.getEastBoundLongitude();
					// See if this is a point. We could technically store a GeoJsonPoint instead of a GeoJsonPolygon is we
					// make the bounding object extend from GeoJson, but the code doesn't deal with points, so extend to a poly
					if (southLat == northLat) {
						southLat = southLat - 0.001;
						northLat = northLat + 0.001;
					}
					if (westLong == eastLong) {
						eastLong = eastLong - 0.001;
						westLong = westLong + 0.001;
					}
					
					// Sanitise data, some values have been slightly beyond limits
					if (southLat < -90.0) {
						southLat = -90.0;
					}
					if (northLat > 90.0) {
						northLat = 90.0;
					}
					if (westLong < -180.0) {
						westLong = -180.0;
					}
					if (eastLong > 180.0) {
						eastLong = 180.0;
					}
					
					// Set the polygon from the bounds information
					bbox.setBoundingPolygon(westLong, eastLong, southLat, northLat);
				}
			}
		}
		try {
			this.updateCSWRecords(cswRecords);
		} catch(DataAccessResourceFailureException e) {
			throw e;
		}
		log.info("Indexing CSW records complete (" + cswRecords.size() + " records)");
	}

	/**
	 * Update the current CSWRecord in the index
	 * 
	 * @param cswRecord the CSWRecord instance
	 */
	public void updateCSWRecord(final CSWRecord cswRecord) throws DataAccessResourceFailureException {
		try {
			this.recordRepository.save(cswRecord);
		} catch(DataAccessResourceFailureException e) {
			throw e;
		}
	}
	
	/**
	 * Update a List of CSWRecords.
	 * Currently used by the KnownLayerService to update the KnownLayerIds lists of CSWRecords.
	 * Does not calculate bounding polygon information.
	 * 
	 * @param cswRecords List of CSWRecords
	 */
	public void updateCSWRecords(final List<CSWRecord> cswRecords) throws DataAccessResourceFailureException {
		try {
			List<List<CSWRecord>> batchRecords = Lists.partition(cswRecords, 100);
			for (List<CSWRecord> recordSet : batchRecords) {
				this.recordRepository.saveAll(recordSet);
			}
		} catch(DataAccessResourceFailureException e) {
			throw e;
		}
	}
	
	/**
	 * Retrieve all CSWRecords from Elasticsearch index.
	 * Uses paging to get over 10,000 record maximum when using finalAll() with a repository.
	 *  
	 * @return All CSWRecords in the index
	 */
	public List<CSWRecord> getAllCSWRecords() {
		List<CSWRecord> records = new ArrayList<CSWRecord>();
		IndexCoordinates index = IndexCoordinates.of(cswRecordIndex);
		Query query = NativeQuery.builder()
		    .withQuery(q -> q
		        .matchAll(ma -> ma))
		    .withFields("message")
		    .withPageable(PageRequest.of(0, 10))
		    .build();
		SearchScrollHits<CSWRecord> scroll = elasticsearchTemplate.searchScrollStart(1000, query, CSWRecord.class, index);
		String scrollId = scroll.getScrollId();
		while (scroll.hasSearchHits()) {
			for (SearchHit<CSWRecord> searchHit: scroll.getSearchHits()) {
				records.add(searchHit.getContent());
			}
			scrollId = scroll.getScrollId();
			scroll = elasticsearchTemplate.searchScrollContinue(scrollId, 1000, CSWRecord.class, index);
		}
		elasticsearchTemplate.searchScrollClear(scrollId);
		return records;
	}

	/**
	 * Construct a spatial bound criteria if bounds have been supplied
	 * 
	 * @param spatialField the filed containing the spatial object
	 * @param spatialRelation the relation (e.g. intersects)
	 * @param westBoundLongitude west bound
	 * @param eastBoundLongitude east bound
	 * @param southBoundLatitude south bound
	 * @param northBoundLatitude north bound
	 * @return the spatial Criteria object
	 */
	private Criteria createSpatialBoundsCriteria(
			String spatialField, String spatialRelation,
			final Double westBoundLongitude, final Double eastBoundLongitude, final Double southBoundLatitude,
			final Double northBoundLatitude) {
		
		Criteria spatialCriteria = null;
		
		if (northBoundLatitude != null && eastBoundLongitude != null && southBoundLatitude != null && westBoundLongitude != null) {
			// GeoJsonPolygon from supplied bounds
			GeoJsonPolygon boundsPoly = GeoJsonPolygon.of(
					new GeoPoint(northBoundLatitude, westBoundLongitude),
					new GeoPoint(southBoundLatitude, westBoundLongitude),
					new GeoPoint(southBoundLatitude, eastBoundLongitude),
					new GeoPoint(northBoundLatitude, eastBoundLongitude),
					new GeoPoint(northBoundLatitude, westBoundLongitude));

			switch(spatialRelation) {
				case "contains":
					spatialCriteria = new Criteria(spatialField).contains(boundsPoly);
					break;
				case "within":
					spatialCriteria = new Criteria(spatialField).within(boundsPoly);
					break;
				case "intersects":
				default:
					spatialCriteria = new Criteria(spatialField).intersects(boundsPoly);
					break;
			}
		}
		return spatialCriteria;
	}
	
	/**
	 * Query the CSW record index.
	 * 
	 * @param matchPhraseText the text to match
	 * @param queryFields the CSWRecord fields to include in search 
	 * @param page the current page
	 * @param pageSize the page size
	 * @param ogcServices the OGC services (e.g. OGC:WMS) to ensure results include (optional)
	 * @param spatialRelation the spatial relation type that will be used (e.g. intersects) (optional)
	 * @param westBoundLongitude west bounds for the spatial criteria (optional)
	 * @param eastBoundLongitude east bounds for the spatial criteria (optional)
	 * @param southBoundLatitude south bounds for the spatial criteria (optional)
	 * @param northBoundLatitude north bounds for the spatial criteria (optional)
	 * @return
	 */
	public CSWRecordSearchResponse searchCSWRecords(final String matchPhraseText,
			final List<String> queryFields, final Integer page, final Integer pageSize,
			final List<String> ogcServices, final String spatialRelation,
			final Double westBoundLongitude, final Double eastBoundLongitude,
			final Double southBoundLatitude, final Double northBoundLatitude) {
		// Fields to query
		List<String> cswRecordFields = new ArrayList<String>();
		if (queryFields == null || queryFields.size() == 0) {
			cswRecordFields.addAll(CSWRECORD_QUERY_FIELDS);
		} else {
			for (String field: queryFields) {
				if (CSWRECORD_QUERY_FIELDS.contains(field)) {
					cswRecordFields.add(field);
				}
			}
		}

		// Search Criteria and Query
		Criteria cswRecordCriteria = new Criteria();
		
		if (StringUtils.isNotBlank(matchPhraseText)) {
			Criteria cswFieldCriteria = new Criteria();
			//Criteria cswFieldCriteria = Criteria.or();
			for (String field: cswRecordFields) {
				if (field.equals("knownLayerNames") || field.equals("knownLayerDescriptions")) {
					cswFieldCriteria = cswFieldCriteria.or(field).contains(matchPhraseText).boost(2);
				} else {
					cswFieldCriteria = cswFieldCriteria.or(field).contains(matchPhraseText);
				}
			}
			cswRecordCriteria = cswFieldCriteria;			
		}
		
		// OGC services query
		if (ogcServices != null && ogcServices.size() > 0) {
			Criteria ogcServicesCriteria = new Criteria();
			for (String service: ogcServices) {
				ogcServicesCriteria = ogcServicesCriteria.or("onlineResources.protocol").contains(service.toLowerCase());
			}
			cswRecordCriteria = cswRecordCriteria.and(ogcServicesCriteria);
		}
		
		// CSWRecords spatial Criteria
		Criteria cswRecordSpatialCriteria = createSpatialBoundsCriteria("cswGeographicElements.boundingPolygon", spatialRelation,
				westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
		if (cswRecordSpatialCriteria != null) {
			cswRecordCriteria = cswRecordCriteria.and(cswRecordSpatialCriteria);
		}
		
		Query cswRecordQuery = new CriteriaQuery(cswRecordCriteria);
		Pageable pageable = PageRequest.of(0, 1000);
		if (page != null && pageSize != null) {
			pageable = PageRequest.of(page, pageSize);			
		}
		cswRecordQuery.setPageable(pageable);
		
		List<CSWRecord> recordResults = new ArrayList<CSWRecord>();
		SearchHits<CSWRecord> searchHits = null;
		try {
			searchHits = elasticsearchOperations.search(cswRecordQuery, CSWRecord.class, IndexCoordinates.of(cswRecordIndex));
		} catch(Exception e) {
			log.error("Error searching CSW records: " + e.getLocalizedMessage());
			return new CSWRecordSearchResponse(0, recordResults, new ArrayList<String>());
		}
		
		Set<String> knownLayerIds = new HashSet<String>();
		for (SearchHit<CSWRecord> hit: searchHits) {
			recordResults.add(hit.getContent());
			if (hit.getContent().getKnownLayerIds() != null) {
				knownLayerIds.addAll(hit.getContent().getKnownLayerIds());
			}
		}
		
		return new CSWRecordSearchResponse(searchHits.getTotalHits(), recordResults, new ArrayList<String>(knownLayerIds));
	}
	
	/**
	 * Make a completion query in order to suggest terms
	 *  
	 * @param query the start of the suggestion term
	 * @return a list of suggestion terms
	 */
	public List<String> suggestTerms(String prefix) {
		Set<String> terms = new HashSet<String>();
		HttpPost method = new HttpPost();
		try {
			String JSON_STRING = "{\"suggest\":{\"term-suggester\":{\"prefix\":\"" + prefix + "\",\"completion\":{\"skip_duplicates\":true,\"field\":\"suggestionCompletion\",\"size\":10}}}}";
		    HttpEntity stringEntity = new StringEntity(JSON_STRING, ContentType.APPLICATION_JSON);
		    method.setEntity(stringEntity);
		    method.setURI(elasticsearchSuggestionUrl.toURI());
		    // Add auth if required
		    if (StringUtils.isNotBlank(apiKey)) {
				method.setHeader("Authorization", "ApiKey " + apiKey);
			}
	        String jsonResponseString = httpServiceCaller.getMethodResponseAsString(method);
	        JSONObject jsonResponseObject = new JSONObject(jsonResponseString);
	        JSONObject jsonSuggest = jsonResponseObject.getJSONObject("suggest");
	        JSONArray jsonTermSuggesters = jsonSuggest.getJSONArray("term-suggester");
	        for (int i = 0; i < jsonTermSuggesters.length(); i++) {
	        	JSONObject termSuggester = jsonTermSuggesters.getJSONObject(i);
	        	JSONArray optionsArray = termSuggester.getJSONArray("options");
	        	for (int j = 0; j < optionsArray.length(); j++) {
	        		JSONObject termObject = optionsArray.getJSONObject(j);
	        		terms.add(termObject.getString("text"));
	        	}
	        }
		} catch(URISyntaxException urie) {
			log.error(urie.getLocalizedMessage());
		} catch (IOException ioe) {
			log.error(ioe.getLocalizedMessage());
		}
		
		return new ArrayList<String>(terms);
	}
	
	/**
	 * Adds unique words to a map created by splitting words on nun-alphanumeric characters from a longer string.
	 * The map is indexed by the lower-cased word and the value is the count of the word's appearance.
	 * 
	 * @param terms current map of unique words
	 * @param text the text to split
	 */
	private void addSplitWords(Map<String, Integer> terms, String text) {
		// Split on anything that isn't a letter, number or '-'
		String[] spaceSeparatedWords = text.split("[^a-zA-Z0-9-]+");
		for (String word: spaceSeparatedWords) {
			word = word.toLowerCase();
			terms.put(word, terms.containsKey(word) ? terms.get(word) + 1 : 1);
		}
	}

	/**
	 * Index the completion terms from the list of CSWRecords
	 * 
	 * @param cswRecords the list of CSWRecords to index search terms for
	 */
	public void indexCompletionTerms(List<CSWRecord> cswRecords) throws DataAccessResourceFailureException {
		log.info("Indexing CSW record suggestions");
		// Map of terms with appearance count
		Map<String, Integer> uniqueTerms = new HashMap<String, Integer>();
		for (CSWRecord record: cswRecords) {
			addSplitWords(uniqueTerms, record.getServiceName());
			addSplitWords(uniqueTerms, record.getDataIdentificationAbstract());
			for (String keywords: record.getDescriptiveKeywords()) {
				addSplitWords(uniqueTerms, keywords);
			}
			if (record.getKnownLayerNames() != null) {
				for (String layerName: record.getKnownLayerNames()) {
					addSplitWords(uniqueTerms, layerName);
				}
			}
			if (record.getKnownLayerDescriptions() != null) {
				for(String layerDescription: record.getKnownLayerDescriptions()) {
					addSplitWords(uniqueTerms, layerDescription);
				}
			}			
		}
		if (uniqueTerms.size() > 0) {
			List<CSWSuggestion> suggestionList = new ArrayList<CSWSuggestion>();
			for (Map.Entry<String, Integer> term : uniqueTerms.entrySet()) {
				if (StringUtils.isNotBlank(term.getKey()) && term.getKey().length() > 2 && term.getKey().length() < 100) {
					CSWSuggestion suggestion = new CSWSuggestion(term.getKey(), term.getValue());
					suggestionList.add(suggestion);
				}
	        }
			
			List<List<CSWSuggestion>> batchSuggestions = Lists.partition(suggestionList, 100);
			for (List<CSWSuggestion> suggestionSet : batchSuggestions) {
				try {
					suggestionRepository.saveAll(suggestionSet);
				} catch (DataAccessResourceFailureException e) {
					throw e;
				}
			}
			
			log.info("Indexing CSW record suggestions complete (" + suggestionList.size() + " records)");
		}
	}
	
}
