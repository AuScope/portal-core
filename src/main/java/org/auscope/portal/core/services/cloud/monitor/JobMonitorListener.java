package org.auscope.portal.core.services.cloud.monitor;

import java.util.EventListener;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * An interface representing a set of methods for listening
 * @author Josh Vote
 *
 */
public interface JobMonitorListener extends EventListener {
    /**
     * Raised whenever a status change has been detected in a particular job. There
     * is no guarantee that this will be raised immediately after status change
     * @param job The job whose status has changed (The new status will be written into this object too)
     * @param oldStatus The prior status string value
     * @param newStatus The new status string (equivalent to job.getStatus())
     */
    public void handleStatusChanged(CloudJob job, String newStatus, String oldStatus);
}
