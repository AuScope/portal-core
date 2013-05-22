package org.auscope.portal.core.services.cloud.monitor;

import java.util.EventListener;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * All job status change listener or handler should
 * implement this interface.
 *  
 * @author Richard Goh
 */
public interface JobStatusChangeListener extends EventListener {
    public void handleStatusChange(CloudJob job, String newStatus, String oldStatus);
}