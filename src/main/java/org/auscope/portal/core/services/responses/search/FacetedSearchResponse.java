package org.auscope.portal.core.services.responses.search;

import java.io.Serializable;
import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * Represents a response from a faceted search request.
 * @author Josh Vote (CSIRO)
 *
 */
public class FacetedSearchResponse implements Serializable {
	private static final long serialVersionUID = 3321276637339015960L;
	
	private List<CSWRecord> records;
    private int startIndex;
    private int nextIndex;
    private int recordsMatched;

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
     * The first record index where searching started (not necessarily the same index as records[0])
     * @return
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * The first record index where searching started (not necessarily the same index as records[0])
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    /**
     * The index of the last record index + 1 (or 0 if there are no more records)
     * @return
     */
    public int getNextIndex() {
        return nextIndex;
    }

    /**
     * The index of the last record index + 1 (or 0 if there are no more records)
     * @param nextIndex
     */
    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }
    
    /**
     * The total number of records matched by the faceted search
     * @return
     */
	public int getRecordsMatched() {
		return recordsMatched;
	}
	
	/**
	 * The total number of records matched by the faceted search
	 * @param recordsMatched
	 */
	public void setRecordsMatched(int recordsMatched) {
		this.recordsMatched = recordsMatched;
	}


}
