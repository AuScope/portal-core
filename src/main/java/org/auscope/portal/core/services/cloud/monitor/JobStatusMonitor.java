package org.auscope.portal.core.services.cloud.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;

/**
 * A simple class containing event listeners and the ability to poll CloudJob instances for information about their current status
 *
 * @author Josh Vote
 *
 */
public class JobStatusMonitor {

    protected final Log log = LogFactory.getLog(getClass());

    /** An object for reading status information about a particular job */
    private JobStatusReader jobStatusReader;
    /** Event listeners to be notified whenever a job's status is changed (and detected by this class) */
    private JobStatusChangeListener[] jobStatusChangeListeners;

    /**
     * Creates a new instance of this class
     *
     * @param jobStatusReader
     * @param jobStatusChangeListeners
     */
    public JobStatusMonitor(JobStatusReader jobStatusReader, JobStatusChangeListener[] jobStatusChangeListeners) {
        super();
        this.jobStatusReader = jobStatusReader;
        this.jobStatusChangeListeners = jobStatusChangeListeners;
    }

    private void statusChanged(CloudJob job, String newStatus, String oldStatus) {
        for (JobStatusChangeListener l : jobStatusChangeListeners) {
            try {
                l.handleStatusChange(job, newStatus, oldStatus);
            } catch (Exception ex) {
                //Simply log it if the event handler fails and move on
                log.error("An error has occurred while handling status change event: " + ex.getMessage());
                log.debug("Exception: ", ex);
            }
        }
    }

    /**
     * Force a status update of a particular job. This is a blocking method that will not return until all status change listeners have finished their updates.
     *
     * @param job
     *            The job to update - may have its fields modified by status change listeners
     * @throws JobStatusException
     */
    public void statusUpdate(CloudJob job) throws JobStatusException {
        String oldStatus = job.getStatus();
        String newStatus;

        try {
            newStatus = jobStatusReader.getJobStatus(job);
        } catch (Exception ex) {
            throw new JobStatusException(ex, job);
        }

        if (newStatus != null && !newStatus.equals(oldStatus)) {
            statusChanged(job, newStatus, oldStatus);
        } else {
            log.trace("Skip bad or status quo job. Job id: " + job.getId());
        }
    }

    /**
     * Force a status update of a particular collection of jobs. This is a blocking method that will not return until all status change listeners have finished
     * their updates for all jobs whose status has changed.
     *
     * If any job throws an exception, the exception will be stored and subsequent jobs will continue to be updated. At the end of all updates, all exceptions
     * thrown will be wrapped in a single JobStatusException and rethrown
     *
     * @param jobs
     *            The job collection to update - may have its member fields modified by status change listeners
     * @throws JobStatusException
     *             If and only if one or more job status updates fail
     */
    public void statusUpdate(Collection<? extends CloudJob> jobs) throws JobStatusException {
        List<Throwable> exceptions = new ArrayList<>();
        List<CloudJob> failedUpdates = new ArrayList<>();

        for (CloudJob job : jobs) {
            //Do all updates before throwing exceptions
            try {
                statusUpdate(job);
            } catch (Throwable t) {
                failedUpdates.add(job);
                exceptions.add(t);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new JobStatusException(exceptions, failedUpdates);
        }
    }
}
