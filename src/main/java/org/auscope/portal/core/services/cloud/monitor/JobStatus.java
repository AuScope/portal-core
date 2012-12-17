package org.auscope.portal.core.services.cloud.monitor;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * A simple POJO for defining the status of a job as a string
 * @author Josh Vote
 *
 */
public class JobStatus {
    /** The job whose status is being referenced */
    private CloudJob cloudJob;
    /** The status as a string */
    private String status;

    /**
     *
     * @param cloudJob The job whose status is being referenced
     * @param status The status as a string
     */
    public JobStatus(CloudJob cloudJob, String status) {
        super();
        this.cloudJob = cloudJob;
        this.status = status;
    }

    /**
     * The job whose status is being referenced
     * @return
     */
    public CloudJob getCloudJob() {
        return cloudJob;
    }

    /**
     * The job whose status is being referenced
     * @param cloudJob
     */
    public void setCloudJob(CloudJob cloudJob) {
        this.cloudJob = cloudJob;
    }

    /**
     * The status as a string
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * The status as a string
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }




}
