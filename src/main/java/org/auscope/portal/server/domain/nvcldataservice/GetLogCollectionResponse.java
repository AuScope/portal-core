package org.auscope.portal.server.domain.nvcldataservice;

import java.io.Serializable;

/**
 * A POJO representation of the response from a NVCLDataService getDatasetCollection request
 * @author Josh Vote
 *
 */
public class GetLogCollectionResponse implements Serializable {
    String logId;
    String logName;

    /**
     * Creates a new instance
     * @param logId The unique ID for the logged analyte
     * @param logName The logged analyte name
     */
    public GetLogCollectionResponse(String logId, String logName) {
        this.logId = logId;
        this.logName = logName;
    }

    /**
     * Gets the unique ID for the logged analyte
     * @return
     */
    public String getLogId() {
        return logId;
    }

    /**
     * Sets the unique ID for the logged analyte
     * @param logId
     */
    public void setLogId(String logId) {
        this.logId = logId;
    }

    /**
     * Gets the logged analyte name
     * @return
     */
    public String getLogName() {
        return logName;
    }

    /**
     * Sets the logged analyte name
     * @param logName
     */
    public void setLogName(String logName) {
        this.logName = logName;
    }


}
