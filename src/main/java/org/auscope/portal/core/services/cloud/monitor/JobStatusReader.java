package org.auscope.portal.core.services.cloud.monitor;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * An interface that represents functionality for reading the latest details about a specific CloudJob.
 * 
 * @author Josh Vote
 *
 */
public interface JobStatusReader {
    /**
     * Implementors may either just return cloudJob.getStatus() or may calculate the latest status in a manner specific to the CloudJob implementation.
     * 
     * @param cloudJob
     *            a CloudJob whose status will be calculated
     * @return
     */
    public String getJobStatus(CloudJob cloudJob);
}
