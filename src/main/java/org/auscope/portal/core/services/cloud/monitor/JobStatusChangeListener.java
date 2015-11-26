package org.auscope.portal.core.services.cloud.monitor;

import java.util.EventListener;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * All job status change listener or handler should implement this interface.
 * 
 * @author Richard Goh
 */
public interface JobStatusChangeListener extends EventListener {
    /**
     * This is a synchronous event to alert that a particular CloudJob status has changed
     * 
     * Implementors should ensure that this method is thread safe
     * 
     * @param job
     * @param newStatus
     * @param oldStatus
     */
    public void handleStatusChange(CloudJob job, String newStatus, String oldStatus);
}