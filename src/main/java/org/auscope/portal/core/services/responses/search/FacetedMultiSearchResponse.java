package org.auscope.portal.core.services.responses.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * Represents a response from multiple faceted search requests.
 * @author Josh Vote (CSIRO)
 *
 */
public class FacetedMultiSearchResponse implements Serializable {
	private static final long serialVersionUID = -615570404768745749L;
	
	private List<CSWRecord> records;
    private Map<String, Integer> startIndexes;
    private Map<String, Integer> nextIndexes;
    private Map<String, Integer> recordsMatched;

    public FacetedMultiSearchResponse() {
        super();
        this.records = new ArrayList<CSWRecord>();
        this.startIndexes = new HashMap<String, Integer>();
        this.nextIndexes = new HashMap<String, Integer>();
        this.setRecordsMatched(new HashMap<String, Integer>());
    }
    /**
     * The CSWRecords parsed from the CSW response
     * @return
     */
    public List<CSWRecord> getRecords() {
        return records;
    }
    /**
     * The CSWRecords parsed from the CSW response
     * @param records
     */
    public void setRecords(List<CSWRecord> records) {
        this.records = records;
    }
    /**
     * The first record index where searching started keyed by service id
     * @return
     */
    public Map<String, Integer> getStartIndexes() {
        return startIndexes;
    }
    /**
     * The first record index where searching started keyed by service id
     * @param startIndexes
     */
    public void setStartIndexes(Map<String, Integer> startIndexes) {
        this.startIndexes = startIndexes;
    }
    /**
     * The index of the last record index + 1 keyed by service id
     * @return
     */
    public Map<String, Integer> getNextIndexes() {
        return nextIndexes;
    }
    /**
     * The index of the last record index + 1 keyed by service id
     * @param nextIndexes
     */
    public void setNextIndexes(Map<String, Integer> nextIndexes) {
        this.nextIndexes = nextIndexes;
    }
    
    /**
     * The total number of records matched for the search by service id
     * @return
     */
	public Map<String, Integer> getRecordsMatched() {
		return recordsMatched;
	}
	
	/**
	 * The total number of records matched for the search by service id
	 * @param recordsMatched
	 */
	public void setRecordsMatched(Map<String, Integer> recordsMatched) {
		this.recordsMatched = recordsMatched;
	}
}
