package org.auscope.portal.server.domain.nvcldataservice;

import java.io.Serializable;

/**
 * A POJO representation of the response from a NVCLDataService getDatasetCollection request
 * @author Josh Vote
 *
 */
public class GetDatasetCollectionResponse implements Serializable {
    String datasetId;
    String datasetName;
    String omUrl;

    /**
     * Creates a new instance of this class
     * @param datasetId The datasetId (use for future requests)
     * @param datasetName The dataset name (human readable)
     * @param omUrl The observations and measurements WFS URL
     */
    public GetDatasetCollectionResponse(String datasetId, String datasetName,
            String omUrl) {
        this.datasetId = datasetId;
        this.datasetName = datasetName;
        this.omUrl = omUrl;
    }

    /**
     * Gets the datasetId (use for future requests)
     * @return
     */
    public String getDatasetId() {
        return datasetId;
    }

    /**
     * Sets the datasetId (use for future requests)
     * @param datasetId
     */
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Gets the dataset name (human readable)
     * @return
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * Sets the dataset name (human readable)
     * @param datasetName
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Gets the observations and measurements WFS URL
     * @return
     */
    public String getOmUrl() {
        return omUrl;
    }

    /**
     * Sets the observations and measurements WFS URL
     * @param omUrl
     */
    public void setOmUrl(String omUrl) {
        this.omUrl = omUrl;
    }


}
