package org.auscope.portal.core.services.cloud.monitor;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * A Job Monitor is a class that is capable of providing custom
 * examination of a running job's state.
 *
 * A JobMonitoringService uses a JobMonitor instance to provide
 * status updates beyond the basic 'Running'/'Not Running'
 *
 * @author Josh Vote
 *
 */
public interface JobMonitor {
    /**
     * Examines a particular job and determines its current status
     * @param job The job to examine
     * @return The job status as a string
     */
    public String getJobStatus(CloudJob job);
}
