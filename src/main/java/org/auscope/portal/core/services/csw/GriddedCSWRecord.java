package org.auscope.portal.core.services.csw;

import org.auscope.portal.core.services.responses.csw.CSWRecord;

/**
 * An extension to the CSWRecord that adds additional gridded data information
 * specific to Geoscience Australia.
 * 
 * @author Josh Vote
 *
 */
public class GriddedCSWRecord extends CSWRecord {

    private GriddedDataPositionalAccuracy[] griddedInfo;
    private String dateStamp;
    
    public GriddedCSWRecord(String fileIdentifier) {
        super(fileIdentifier);
    }

    /**
     * Positional accuracy information about this CSWRecord
     * @return
     */
    public GriddedDataPositionalAccuracy[] getGriddedInfo() {
        return griddedInfo;
    }

    /**
     * Positional accuracy information about this CSWRecord
     * @param griddedInfo
     */
    public void setGriddedInfo(GriddedDataPositionalAccuracy[] griddedInfo) {
        this.griddedInfo = griddedInfo;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }    
    
}
