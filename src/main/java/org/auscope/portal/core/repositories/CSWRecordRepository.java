package org.auscope.portal.core.repositories;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


@Repository
public interface CSWRecordRepository extends ElasticsearchRepository<CSWRecord, String> {

	// Slice will allow iteration over all records to build cache
	Slice<CSWRecord> findBy(Pageable pageable);

}
