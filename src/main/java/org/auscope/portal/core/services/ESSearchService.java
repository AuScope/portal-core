package org.auscope.portal.core.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.auscope.portal.core.repositories.CSWRecordRepository;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.es.CSWRecordSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoJsonPolygon;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.scheduling.annotation.Async;

import com.google.common.collect.Lists;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SuggestMode;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionContext;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.SuggestFuzziness;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.elasticsearch.core.search.SuggestionBuilders;


//@Service
public class ESSearchService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private final int GET_RECORD_PAGE_SIZE = 10000;
	
	//private final int RESULTS_PER_PAGE = 10;
	
	@Value("${spring.data.elasticsearch.cluster-nodes}")
	private String elasticsearchNodesUrl;
	
	@Value("${spring.data.elasticsearch.cswRecordIndex:auscope-api-cswrecord}")
    private String cswRecordIndex;
	/*
	private URL elasticsearchUrl;
	*/
	
	final String HIGHLIGHT_REGEX = Pattern.quote("<em>") + "(.*?)" + Pattern.quote("</em>");
	final Pattern highlightPattern = Pattern.compile(HIGHLIGHT_REGEX);

	// CSWRecord search fields
	public static final List<String> CSWRECORD_QUERY_FIELDS = Arrays.asList(new String[]{
		// Native CSWRecord fields
		"fileIdentifier", "serviceName", "descriptiveKeywords", "dataIdentificationAbstract", "layerName", "knownLayerNames", "knownLayerDescriptions",
		// Nested onlineResources fields (OnlineResource)
		"onlineResources.name", "onlineResources.description", "onlineResources.protocol", "funder.organisationName"
	});
	
	// The minimum length of highlight match words
	private static final int MINIMUM_HIGHLIGHT_WORD_LENGTH = 4;
	
	// The number of highlight matches to return
	private static final int NUMBER_OF_HIGHLIGHT_MATCHES = 10;
	
	
	@Autowired
	private CSWRecordRepository recordRepository;
	
	
	@Autowired
	private ElasticsearchOperations elasticsearchOperations;
	
	
	/*
	@Autowired
	@Qualifier("esSearchServiceCaller")
	HttpServiceCaller esSearchServiceCaller;
	*/
	
	/*
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;
	*/
	
	@Autowired
	ElasticsearchClient elasticsearchClient;
	
	// For writing search results to Objects
	// XXX CHECK STILL NEEDED
	/*
	private ObjectMapper objectMapper = new ObjectMapper();
	*/
	
	public ESSearchService() {
		/*
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		*/
	}
	
	//@PostConstruct
	//private void setElasticsearchUrl() {
	//	try {
	//		URIBuilder uriBuilder = new URIBuilder(elasticsearchNodesUrl);
	//    	uriBuilder.setPath("/*/_search");
	//    	elasticsearchUrl = uriBuilder.build().toURL();
	//	} catch(Exception e) {
	//		log.error("Unable to build Elasticsearch URL: " + e.getLocalizedMessage());
	//	}
	//}

	/**
	 * Index CSWRecords.
	 * Creates a bounding polygon for each record from the CSWGeographicElement.
	 * 
	 * @param cswRecords the CSWRecord list
	 */
	public void indexCSWRecords(final List<CSWRecord> cswRecords) {
		log.info("Indexing CSW records");
		// Batch records for indexing
		List<List<CSWRecord>> batchRecords = Lists.partition(cswRecords, 50);
		for (List<CSWRecord> recordSet : batchRecords) {
			for (CSWRecord record: recordSet) {
				if(record.getCSWGeographicElements() != null && record.getCSWGeographicElements().length > 0) {
					// Sanitize data
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
			recordRepository.saveAll(recordSet);
		}
		log.info("Indexing CSW records complete (" + cswRecords.size() + " records)");
	}

	/**
	 * Update the current CSWRecord in the index
	 * 
	 * @param cswRecord the CSWRecord instance
	 */
	public void updateCSWRecord(final CSWRecord cswRecord) {
		this.recordRepository.save(cswRecord);
	}
	
	/**
	 * Update a List of CSWRecords.
	 * Currently used by the KnownLayerService to update the KnownLayerIds lists of CSWRecords.
	 * Does not calculate bounding polygon information.
	 * 
	 * @param cswRecords List of CSWRecords
	 */
	public void updateCSWRecords(final List<CSWRecord> cswRecords) {
		List<List<CSWRecord>> batchRecords = Lists.partition(cswRecords, 100);
		for (List<CSWRecord> recordSet : batchRecords) {
			this.recordRepository.saveAll(recordSet);
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
		Page<CSWRecord> cswRecordsPage = recordRepository.findAll(PageRequest.of(0, GET_RECORD_PAGE_SIZE));
		records.addAll(cswRecordsPage.getContent());
		while(cswRecordsPage.hasNext()) {
			cswRecordsPage = recordRepository.findAll(cswRecordsPage.nextPageable());
			records.addAll(cswRecordsPage.getContent());
		}
		return records;
	}

	/**
	 * Add a spatial bound criteria if bounds have been supplied
	 * 
	 * @param spatialField
	 * @param westBoundLongitude
	 * @param eastBoundLongitude
	 * @param southBoundLatitude
	 * @param northBoundLatitude
	 * @return
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

			// XXX
			System.out.println("Adding spatial criteria polygon for " + spatialField + ": (" + northBoundLatitude + ", " + westBoundLongitude + ") (" + southBoundLatitude + ", " + westBoundLongitude + ") (" + southBoundLatitude + ", " + eastBoundLongitude + ") (" + northBoundLatitude + ", " + eastBoundLongitude + ") (" + northBoundLatitude + ", " + westBoundLongitude + ")");
			
			switch(spatialRelation) {
				case "contains":
					System.out.println("** CONTAINS **");
					spatialCriteria = new Criteria(spatialField).contains(boundsPoly);
					break;
				case "within":
					System.out.println("** WITHIN **");
					spatialCriteria = new Criteria(spatialField).within(boundsPoly);
					break;
				case "intersects":
				default:
					System.out.println("** INTERSECTS **");
					spatialCriteria = new Criteria(spatialField).intersects(boundsPoly);
					break;
			}
		}
		return spatialCriteria;
	}
	
	/**
	 * 
	 * @param matchPhraseText
	 * @param queryFields
	 * @param page
	 * @param pageSize
	 * @param ogcServices
	 * @param spatialRelation
	 * @param westBoundLongitude
	 * @param eastBoundLongitude
	 * @param southBoundLatitude
	 * @param northBoundLatitude
	 * @return
	 */
	public CSWRecordSearchResponse searchCSWRecords(final String matchPhraseText,
			final List<String> queryFields, final Integer page, final Integer pageSize,
			final List<String> ogcServices, final String spatialRelation,
			final Double westBoundLongitude, final Double eastBoundLongitude,
			final Double southBoundLatitude, final Double northBoundLatitude) {

		// XXX
		System.out.println("Querying: " + matchPhraseText);

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
			if (cswRecordFields == null || cswRecordFields.size() == 0) {
				cswRecordCriteria = cswRecordCriteria.matches(matchPhraseText);
			} else {
				for (String field: cswRecordFields) {
					cswRecordCriteria = cswRecordCriteria.or(field).matches(matchPhraseText);
				}
			}
		}
		
		// OGC services query
		if (ogcServices != null && ogcServices.size() > 0) {
			Criteria ogcServicesCriteria = new Criteria();
			for (String service: ogcServices) {
				// XXX
				System.out.println("Adding service: " + service);
				ogcServicesCriteria = ogcServicesCriteria.and("onlineResources.protocol").contains(service.toLowerCase());
			}
			cswRecordCriteria = cswRecordCriteria.and(ogcServicesCriteria);
		}
		
		// CSWRecords spatial Criteria
		Criteria cswRecordSpatialCriteria = createSpatialBoundsCriteria("cswGeographicBoundingBoxes.boundingPoly", spatialRelation,
				westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
		if (cswRecordSpatialCriteria != null) {
			cswRecordCriteria = cswRecordCriteria.and(cswRecordSpatialCriteria);
		}

		Query cswRecordQuery = new CriteriaQuery(cswRecordCriteria);
		Pageable pageable = PageRequest.of(0, 1000);
		if (page != null && pageSize != null) {
			// XXX
			System.out.println("Setting page: " + page + " of pageSize: " + pageSize);
			pageable = PageRequest.of(page, pageSize);
			
		}
		cswRecordQuery.setPageable(pageable);
		
		//SearchPage<Entity> page = SearchHitSupport.searchPageFor(searchHits, query.getPageable)
		SearchHits<CSWRecord> searchHits = elasticsearchOperations.search(cswRecordQuery, CSWRecord.class, IndexCoordinates.of(cswRecordIndex));
		
		// XXX
		System.out.println("CSWRecord only results: " + searchHits.getTotalHits());
		
		List<CSWRecord> recordResults = new ArrayList<CSWRecord>();
		Set<String> knownLayerIds = new HashSet<String>();
		for (SearchHit<CSWRecord> hit: searchHits) {
			recordResults.add(hit.getContent());
			if (hit.getContent().getKnownLayerIds() != null) {
				knownLayerIds.addAll(hit.getContent().getKnownLayerIds());
			}
		}
		
		/* XXX Testing spatial
		for(int i = 0; i < Math.min(10, searchHits.getTotalHits()); i++) {
			if (((CSWRecord)searchHits.getSearchHit(i).getContent()).getCSWGeographicBoundingBoxes() != null) {
				for (CSWGeographicBoundingBox geo: ((CSWRecord)searchHits.getSearchHit(i).getContent()).getCSWGeographicBoundingBoxes()) {
					System.out.println("CSWRecord: " + ((CSWRecord)searchHits.getSearchHit(i).getContent()).getServiceName());
					System.out.println("  ->  boundingPoly: " + geo.getBoundingPoly().toString());
				}
			}
		}
		*/
		
		return new CSWRecordSearchResponse(searchHits.getTotalHits(), recordResults, new ArrayList<String>(knownLayerIds));
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public List<String> suggestTerms(String prefix) {
		List<String> terms = new ArrayList<String>();
		
		// Use SearchHits<T> search(Query query, Class<T> clazz) instead, passing in a NativeSearchQuery
		// which can contain a SuggestBuilder and read the suggest results from the returned SearchHit<T>
		
		/*
		NativeQuery searchQuery = NativeQuery.builder()
                //.withSourceFilter(new FetchSourceFilterBuilder().withIncludes().build())
                //.withQuery(QueryBuilders.termQueryAsQuery("fieldName", ""))
                .withSuggester(SuggestionBuilders.)
                //.withSort(Sort.by(Sort.Direction.DESC, "fieldName"))
                //.withPageable(pageable)
                .build();
		*/

		/*
		Map<String, FieldSuggester> map = new HashMap<>();
		map.put("serviceName_suggestion", FieldSuggester.of(fs -> fs
		    .completion(cs -> cs.skipDuplicates(true)
		        .size(5)
		        .field("serviceName")
		        //.analyzer("autocomplete_search")
		    )
		));
		//map.put("dataIdentificationAbstract_suggestion", FieldSuggester.of(fs -> fs
		//    .completion(cs -> cs.skipDuplicates(true)
		//        .size(5)
		//        .field("dataIdentificationAbstract")
		//    )
		//));
		
		Suggester suggester = Suggester.of(s -> s
		    .suggesters(map)
		    .text(query)
		);
	
		try {
			SearchResponse<CSWRecord> response = elasticsearchClient.search(s ->
				s.index("auscope-api-cswrecord").
					suggest(suggester), CSWRecord.class);
			List<Suggestion<CSWRecord>> suggestions = response.suggest().get("serviceName_suggestion");
			for (Suggestion<CSWRecord> suggest: suggestions) {
			  for (CompletionSuggestOption<CSWRecord> option : suggest.completion().options()) {
			    System.out.println("suggestion: " + option.text());
			  }
			}
		} catch (IOException ioe) {
			log.error("Error retrieving suggestion: " + ioe.getLocalizedMessage());
		}
		*/
		
		/*
		try {
			SearchResponse<CSWRecord> response = elasticsearchClient.search(request -> request.
				index("auscope-api-cswrecord").
				suggest(suggest -> suggest.suggesters("suggest", value -> value.
				term(v -> v.
				//field("serviceName").maxTermFreq(Float.valueOf("10")).suggestMode(SuggestMode.Always))).text(query)), CSWRecord.class);
				field("serviceName"))).text(query)), CSWRecord.class);
			System.out.println("PAUSE!!!!!!!!!!!!");
		} catch(IOException ioe) {
			log.error("Error retrieving suggestion: " + ioe.getLocalizedMessage());
		}
		*/
		Query suggestQuery = NativeQuery.builder()
				.withSuggester(Suggester.of(s -> s
						.suggesters("suggest-terms", FieldSuggester.of(fs -> fs
								.prefix(prefix)
								.completion(cs -> cs
										.field("suggesterCompletion")
										
										.size(10)
										
										.skipDuplicates(true)/*
										.fuzzy(SuggestFuzziness.of(f -> f
												.fuzziness("AUTO")
												.minLength(3)
												.prefixLength(1)
												.transpositions(true)
												.unicodeAware(false)))*/)
						)))
				).build();
		
		SearchHits<CSWRecord> searchHits = elasticsearchOperations.search(suggestQuery, CSWRecord.class);
		Suggest suggest = searchHits.getSuggest();
		Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion = suggest
				.getSuggestion("suggest-terms");
		List<CompletionSuggestion.Entry.Option<CSWRecord>> options = ((CompletionSuggestion<CSWRecord>) suggestion)
				.getEntries().get(0).getOptions();
		
		for(CompletionSuggestion.Entry.Option<CSWRecord> option: options) {
			terms.add(option.getText());
		}
		
		return terms;
	}
	
	/*
	protected Query getSuggestQuery(String suggestionName, String fieldName, String prefix) {
		return NativeQuery.builder() //
				.withSuggester(Suggester.of(s -> s //
						.suggesters(suggestionName, FieldSuggester.of(fs -> fs //
								.prefix(prefix)//
								.completion(cs -> cs //
										.field(fieldName) //
										.fuzzy(SuggestFuzziness.of(f -> f //
												.fuzziness("AUTO") //
												.minLength(3) //
												.prefixLength(1) //
												.transpositions(true) //
												.unicodeAware(false))))//
						))) //
				).build();
	}
	
	public void shouldFindSuggestionsForGivenCriteriaQueryUsingCompletionEntity() {

		loadCompletionObjectEntities();
		Query query = getSuggestQuery("test-suggest", "suggest", "m");

		SearchHits<CompletionEntity> searchHits = operations.search(query, CompletionEntity.class);

		assertThat(searchHits.hasSuggest()).isTrue();
		Suggest suggest = searchHits.getSuggest();
		// noinspection ConstantConditions
		Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion = suggest
				.getSuggestion("test-suggest");
		assertThat(suggestion).isNotNull();
		assertThat(suggestion).isInstanceOf(CompletionSuggestion.class);
		// noinspection unchecked
		List<CompletionSuggestion.Entry.Option<AnnotatedCompletionEntity>> options = ((CompletionSuggestion<AnnotatedCompletionEntity>) suggestion)
				.getEntries().get(0).getOptions();
		assertThat(options).hasSize(2);
		assertThat(options.get(0).getText()).isIn("Marchand", "Mohsin");
		assertThat(options.get(1).getText()).isIn("Marchand", "Mohsin");
	}
	*/
	
	/**
	 * 
	 * @param searchQuery
	 * @return
	 */
	public List<String> getPrefixHighlights(String searchQuery) {
		Set<String> prefixHighlightsSet = new HashSet<String>(); 
		SearchHits<CSWRecord> searchHits = this.recordRepository.findHighlightsByPrefixQuery(searchQuery);
		for(SearchHit<CSWRecord> hit: searchHits) {
			Collection<List<String>> highlightFieldsList = hit.getHighlightFields().values();
			for(List<String> highlightList: highlightFieldsList) {
				for(String highlight: highlightList) {
					Matcher matcher = highlightPattern.matcher(highlight);
					while (matcher.find()) {
						String highlightMatch = matcher.group(1).toLowerCase();
						if (highlightMatch.length() >= MINIMUM_HIGHLIGHT_WORD_LENGTH) {
							prefixHighlightsSet.add(highlightMatch);
						}
					}
				}
			}
		}
		List<String> highlightList = new ArrayList<String>(prefixHighlightsSet);
		if(highlightList.size() > NUMBER_OF_HIGHLIGHT_MATCHES) {
			highlightList = highlightList.subList(0, NUMBER_OF_HIGHLIGHT_MATCHES);
		}
		highlightList.sort(String::compareToIgnoreCase);
		return highlightList;
	}
	
	/**
	 * 
	 * @param queryResponseString
	 * @return
	 */
	/* XXX
	public List<String> extractUniqueHighlightMatches(String queryResponseString) {
		List<String> highlightMatches = new ArrayList<String>();
		Matcher matcher = highlightPattern.matcher(queryResponseString);
		while (matcher.find() && highlightMatches.size() <= NUMBER_OF_HIGHLIGHT_MATCHES) {
			String highlightMatch = matcher.group(1).toLowerCase();
			if (!highlightMatches.contains(highlightMatch) && highlightMatch.length() >= MINIMUM_HIGHLIGHT_WORD_LENGTH) {
				highlightMatches.add(highlightMatch);
			}
		}
		highlightMatches.sort(String::compareToIgnoreCase);
		return highlightMatches;
	}
	*/
	
	/**
	 * Retrieve index suggestions for a given search query
	 * 
	 * @param searchQuery
	 * @return
	 */
	/* XXX
	@Async
	public CompletableFuture<List<String>> getIndexSuggestions(String searchQuery) throws PortalServiceException {
		List<String> indexSuggestions = new ArrayList<String>();
		HttpRequestBase method = null;
		try {
            method = this.makeQueryStringMethod(searchQuery);
            String responseData = this.esSearchServiceCaller.getMethodResponseAsString(method);
            indexSuggestions = extractUniqueHighlightMatches(responseData);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
		return CompletableFuture.completedFuture(indexSuggestions);
	}
	*/
	
	/**
	 * Builds an HTTPRequest for highlight queries across all Elasticsearch indices
	 * 
	 * @param searchString the string to search for
	 * @return an HttpRequestBase method for the highlight query
	 */
	/* XXX
    public HttpRequestBase makeQueryStringMethod(String searchString) throws URISyntaxException {
        HttpPost httpMethod = new HttpPost(elasticsearchUrl.toString());
        StringBuilder sb = new StringBuilder();
        sb.append("{\"query\":{\"query_string\":{\"query\":\"");
        sb.append(searchString);
        sb.append("*\"}},\"highlight\":{\"fields\":{\"dataIdentificationAbstract\":{},\"serviceName\":{},\"descriptiveKeywords\":{},\"knownLayer.name\":{},\"knownLayer.description\":{},\"belongingRecords.serviceName\":{},\"belongingRecords.dataIdentificationAbstract\":{},\"belongingRecords.descriptiveKeywords\":{}}}}");
        log.debug("Elasticsearch service URL:\n\t" + elasticsearchUrl.toString());
        log.debug("GET query_string query:\n" + sb.toString());
        httpMethod.setEntity(new StringEntity(sb.toString(), "UTF-8"));
        httpMethod.setHeader("Content-Type", "application/json");
        return httpMethod;
    }
    */
	
}
