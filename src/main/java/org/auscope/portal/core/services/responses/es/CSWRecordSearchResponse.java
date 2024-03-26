package org.auscope.portal.core.services.responses.es;

import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * Wrapper for Elasticsearch CSWRecord search results 
 *
 */
public class CSWRecordSearchResponse {
	
	// List of CSWRecord results for this search 
	public List<CSWRecord> cswRecords;
	
	// List of KnownLayer IDs
	public List<String> knownLayerIds;
	
	// Total CSWRecord search result count (may be greater than size of cswRecords list due to paging)
	public long totalCSWRecordHits;
	
	public CSWRecordSearchResponse(long totalCSWRecordHits, List<CSWRecord> cswRecords, List<String> knownLayerIds) {
		this.totalCSWRecordHits = totalCSWRecordHits;
		this.cswRecords = cswRecords;
		this.knownLayerIds = knownLayerIds;
	}
	
	public long getTotalCSWRecordHits() {
		return totalCSWRecordHits;
	}
	
	public List<CSWRecord> getCSWRecords() {
		return cswRecords;
	}
	
	public List<String> getKnownLayerIds() {
		return knownLayerIds;
	}
	
	public void setKnownLayerIds(List<String> knownLayerIds) {
		this.knownLayerIds = knownLayerIds;
	}

}
