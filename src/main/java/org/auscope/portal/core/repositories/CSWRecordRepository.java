package org.auscope.portal.core.repositories;

import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.springframework.stereotype.Repository;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


@Repository
public interface CSWRecordRepository extends ElasticsearchRepository<CSWRecord, String> {
//@EnableJpaRepositories(basePackages = "org.auscope.portal.core.repositories")
//public interface CSWRecordRepository extends CrudRepository<CSWRecord, String> {

	//List<CSWRecord> findByFileIdentifier(String name);
	//List<CSWRecord> findByServiceId(String serviceId);	// XXX Needed?

	/*
	// XXX TESTING
	@Query("{\"bool\":{\"must\":{\"match_all\":{}},\"filter\":{\"nested\":{\"path\":\"cswGeographicBoundingBoxes\",\"query\":{\"geo_shape\":{\"cswGeographicBoundingBoxes.boundingPoly\":{\"shape\":{\"type\":\"envelope\",\"coordinates\":[[?0,?1],[?2,?3]]},\"relation\":\"intersects\"}}}}}}}")
	List<CSWRecord> findByBoundingBox(double minLon, double maxLat, double maxLon, double minLat);
	*/
	
	/*
	// Could potentially include responsible parties, funder etc.
	@Highlight(fields = {
			@HighlightField(name = "name"),
			@HighlightField(name = "description"),
	})
	@Query(query = "{\"multi_match\":{\"fields\":[\"serviceName\",\"dataIdentificationAbstract\",\"descriptiveKeywords\"],\"query\":\"?0\",\"type\":\"phrase_prefix\"}}")
	SearchHits<CSWRecord> findHighlightsByPrefixQuery(String query);
	*/
	
	@Highlight(fields = {
			@HighlightField(name = "serviceName"),
			@HighlightField(name = "dataIdentificationAbstract"),
			@HighlightField(name = "descriptiveKeywords"),
			@HighlightField(name = "knownLayerNames"),
			@HighlightField(name = "knownLayerDescriptions"),
			@HighlightField(name = "onlineResources.description")
	})
	
	//@Query(query = "{\"query\":{\"multi_match\":{\"fields\":[\"serviceName\",\"dataIdentificationAbstract\",\"descriptiveKeywords\",\"knownLayerNames\",\"knownLayerDescriptions\",\"onlineResources.description\"],\"query\":\"mag\",\"type\":\"phrase_prefix\"}}}")
	@Query(query = "{\"multi_match\":{\"fields\":[\"serviceName\",\"dataIdentificationAbstract\",\"descriptiveKeywords\",\"knownLayerNames\",\"knownLayerDescriptions\",\"onlineResources.description\"],\"query\":\"?0\",\"type\":\"phrase_prefix\"}}")
	SearchHits<CSWRecord> findHighlightsByPrefixQuery(String query);
	
	/*
	@Query("select r from cswrecord r")
	Stream<CSWRecord> findAllCSWRecordStream();
	*/
	
//	Stream<CSWRecord> findBy();
	
	//findFirst10ByLastnameOrderByFirstname(String lastname, OffsetScrollPosition position);

	//WindowIterator<User> users = WindowIterator.of(position -> repository.findFirst10ByLastnameOrderByFirstname("Doe", position))
	//  .startingAt(OffsetScrollPosition.initial());
	
	//List<String> findDistinctDescriptiveKeywords();
	//List<String> findDistinctDescriptiveKeywordsByServiceId(String serviceId);
}
