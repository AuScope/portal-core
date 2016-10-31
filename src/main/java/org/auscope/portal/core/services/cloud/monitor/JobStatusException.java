package org.auscope.portal.core.services.cloud.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.auscope.portal.core.cloud.CloudJob;

/**
 * General exception thrown when there is an error checking/updating job status. This class can be used as a general purpose exception or as a collection of
 * exceptions for a batch status update
 *
 * @author Josh Vote
 *
 */
public class JobStatusException extends Exception {
    List<Throwable> exceptions;
    List<CloudJob> cloudJobs;

    public JobStatusException(Throwable t, CloudJob job) {
        super(t);
        this.exceptions = Arrays.asList(t);
        this.cloudJobs = Arrays.asList(job);
    }

    public JobStatusException(Collection<Throwable> t, Collection<CloudJob> jobs) {
        super(t.iterator().next());
        this.exceptions = new ArrayList<>(t);
        this.cloudJobs = new ArrayList<>(jobs);
    }

    /**
     * Gets a list of all exceptions encapsulated by this job status exception
     *
     * @return
     */
    public List<Throwable> getExceptions() {
        return exceptions;
    }

    /**
     * Gets the cloud jobs associated with the throwables in exceptions. There should be a 1-1 correspondance with getExceptions
     *
     * @return
     */
    public List<CloudJob> getCloudJobs() {
        return cloudJobs;
    }

    /**
     * Creates a message concatenated from all contained throwables
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        for (Throwable t : exceptions) {
            sb.append(t.getClass().toString());
            sb.append(": [\"");
            sb.append(t.getMessage());
            sb.append("\"] ");
        }

        return sb.toString();
    }
}
