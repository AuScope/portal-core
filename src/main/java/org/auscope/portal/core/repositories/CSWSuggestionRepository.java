package org.auscope.portal.core.repositories;

import org.auscope.portal.core.services.responses.csw.CSWSuggestion;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CSWSuggestionRepository extends ElasticsearchRepository<CSWSuggestion, String> {

}
