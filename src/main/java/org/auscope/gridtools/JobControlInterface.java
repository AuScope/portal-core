/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

package org.auscope.gridtools;

/**
 * This interface defines the methods that must be implemented by any model that
 * deals with the submission and management of jobs. If the controller only ever
 * uses the methods defined in this interface, then the actual model can be 
 * swapped at will without affecting the rest of the program.
 * 
 * @author Darren Kidd
 */
public interface JobControlInterface
{
    /**
     * Submit a job to a site. Receives back a handle/reference/id to the job.
     * Returns <code>null</code> if the job was not submitted correctly or
     * if any other errors occur.
     * 
     * @param jobString The job String to be submitted
     * @param host      The host site the job is being submitted to
     * @return A String reference to the job, or <code>null</code> if it failed
     */
    public abstract String submitJob(String jobString, String host);
    
    /**
     * Kills a job given a reference to it. Returns a String indicating the
     * state of the job after the attempt to kill it. Returns <code>null</code>
     * if something went wrong. 
     * 
     * @param reference Reference to the job
     * @return The state of the job after the attempted kill, or 
     *         <code>null</code> if something bad happened
     */
    public abstract String killJob(String reference);
    
    /**
     * Gets the status of a job given a reference to it. 
     * 
     * @param reference Reference to the job
     * @return Status of the job
     */
    public abstract String getJobStatus(String reference);
    
    /**
     * Gets the results of a job given a reference to it. This may be
     * implemented so that it returns a link to the location of the results,
     * or may return <code>stdout</code> and <code>stderr</code> of the job.
     * 
     * @param reference Reference to the job
     * @return A link to the results or the actual 'results' of the job
     */
    public abstract String getJobResults(String reference);
}

