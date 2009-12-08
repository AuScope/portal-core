/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

package org.auscope.gridtools;

import java.io.Serializable;

/**
 * Simple class that represents a Grid job. An instance of this class
 * represents the state of a grid job at some point in time. It should be
 * treated as a simple data structure (e.g. in an array).
 * <p>
 * Assertions should be enabled when using this class to prevent others from
 * setting values to <code>null</code>.
 * 
 * @author Darren Kidd
 */
public class GridJob implements Serializable
{
    /** The HPC site this job is/was run at. */
    private String   site;
    /** The site's GridFTP server address. */
    private String   siteGridFTPServer;
    /** The job name. */
    private String   name;
    /** The code (software package). */
    private String   code;
    /** The code's executable name. */
    private String   exeName;
    /** The code version. */
    private String   version;
    /** The arguments passed to the code. */
    private String[] arguments;
    /** The queue name at the site. */
    private String   queue;
    /** The maximum wall time. */
    private String   maxWallTime;
    /** The maximum memory. */
    private String   maxMemory;
    /** The job type (eg mpi). */
    private String   jobType;
    /** The number of CPUs to use. */
    private Integer  cpuCount;
    /** The file transfers prior running the job. */
    private String[] inTransfers;
    /** The file transfers after running the job. */
    private String[] outTransfers;
    /** The modules to load before running. */
    private String[] modules;
    /** The email address for notifications */
    private String   emailAddress;
    /** The standard input filename */
    private String   stdInput;
    /** The standard output filename */
    private String   stdOutput;
    /** The standard error filename */
    private String   stdError;
    

    /**
     * Does some <em>very</em> basic setting up of the class variables. Just
     * to prevent errors. The strings are initialized to the empty String, and 
     * the arrays are initialized to a length of zero. 
     */
    public GridJob() {
        site = siteGridFTPServer = name = code = exeName = version = "";
        queue = maxWallTime = maxMemory = jobType = ""; 
        emailAddress = stdInput = stdOutput = stdError = "";
        arguments = inTransfers = outTransfers = modules = new String[0];
        cpuCount = 0;
    }

    /**
     * Alternate constructor 1 - allows for construction by the View. It will
     * construct all the elements that the View knows about. The rest of them
     * can be set by the Control using the available mutators.
     * <p>
     * <i>Thankyou, Eclipse, for auto-generating this method!</i>
     *
     * @param site              The site the job will be run at
     * @param name              A descriptive name for this job
     * @param code              Executable/code to be run
     * @param version           Version of code to use
     * @param arguments         Arguments for the code
     * @param queue             Which queue to use
     * @param jobType           The type of job to run
     * @param maxWallTime       Amount of time we plan to use
     * @param maxMemory         Amount of memory we plan to use
     * @param cpuCount          Number of CPUs to use (if jobType is single)
     * @param inTransfers       Files to be transferred in
     * @param outTransfers      Files to be transferred out
     * @param emailAddress      The email address for PBS job notifications
     * @param stdInput          The std input file for the job
     * @param stdOutput         The std output file for the job
     * @param stdError          The std error file for the job
     */
    public GridJob(String site, String name, String code, String version,
                   String[] arguments, String queue, String jobType,
                   String maxWallTime, String maxMemory, Integer cpuCount,
                   String[] inTransfers, String[] outTransfers,
                   String emailAddress, String stdInput, String stdOutput,
                   String stdError)
    {
        this.site = site;
        this.name = name;
        this.code = code;
        this.version = version;
        this.arguments = arguments;
        this.queue = queue;
        this.jobType = jobType;
        this.maxWallTime = maxWallTime;
        this.maxMemory = maxMemory;
        this.cpuCount = cpuCount;
        this.inTransfers = inTransfers;
        this.outTransfers = outTransfers;
        this.emailAddress = emailAddress;
        this.stdInput = stdInput;
        this.stdOutput = stdOutput;
        this.stdError = stdError;
    }

    /**
     * Helper method that creates a String which represents all of the elements
     * in an array of Strings, in order, between curly braces (similar to array
     * initialization notation).
     * <p>
     * For example, the following code
     * <pre> String[] example = {"One", "Two", "Three", "Four", "Five"};
     * System.out.println("myArray["+example.length+"] = "+arrayToString(example));</pre>
     * would produce the output
     * <pre> myArray[5] = {"One","Two","Three","Four","Five"}</pre>
     * This method is particularly useful when creating larger
     * Strings such as those in a class' <code>toString()</code> method where
     * arrays are present.
     * 
     * @param inputArr the input arr
     * 
     * @return A string representing the array that was input
     */
    private String arrayToString(String[] inputArr) {
        if (inputArr == null)
            return "null";
        
        String arr = "";
        final int SIZE = inputArr.length;

        arr += ("{");
        if (SIZE > 0)
        {
            for (int i=0; i<SIZE-1; i++)
                arr += "\"" + inputArr[i] + "\",";
            arr += "\"" + inputArr[SIZE-1] + "\"";
        }

        arr += "}";

        return arr;
    }

    /**
     * Returns the site that this job will be run at.
     * 
     * @return The site to run at
     */
    public String getSite() {
        return site;
    }

    /**
     * Sets the site that this job will be run at.
     * 
     * @param site The site to run at
     */
    public void setSite(String site) {
        assert (site != null);
        this.site = site;
    }

    /**
     * Returns the address of the local GridFTP server (NG2/Gateway).
     * 
     * @return The address of the local gateway
     */
    public String getSiteGridFTPServer() {
        return siteGridFTPServer;
    }

    /**
     * Sets the address of the local GridFTP server (NG2/Gateway).
     * 
     * @param siteGridFTPServer The address of the local gateway
     */
    public void setSiteGridFTPServer(String siteGridFTPServer) {
        assert (siteGridFTPServer != null);
        this.siteGridFTPServer = siteGridFTPServer;
    }

    /**
     * Returns the name of the job.
     * 
     * @return The job's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the job.
     * 
     * @param name The job's name
     */
    public void setName(String name) {
        assert (name != null);
        this.name = name;
    }

    /**
     * Returns the name of the code to use. 
     * 
     * @return The code name
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Sets the name of the code to use. 
     * 
     * @param code The code name
     */
    public void setCode(String code) {
        assert (code != null);
        this.code = code;
    }

    /**
     * Returns the executable name of the code.
     * 
     * @return Executable name of the code
     */
    public String getExeName() {
        return exeName;
    }

    /**
     * Sets the executable name of the code.
     * 
     * @param exeName Executable name of the code
     */
    public void setExeName(String exeName) {
        assert (exeName != null);
        this.exeName = exeName;
    }

    /**
     * Returns the version number of the code.
     * 
     * @return The code version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Sets the version number of the code.
     * 
     * @param version The code version
     */
    public void setVersion(String version) {
        assert (version != null);
        this.version = version;
    }

    /**
     * Returns a list of arguments to the code.
     * 
     * @return Any arguments to the code
     */
    public String[] getArguments() {
        return arguments;
    }
    
    /**
     * Sets a list of arguments to the code.
     * 
     * @param arguments Any arguments to the code
     */
    public void setArguments(String[] arguments) {
        assert (arguments != null);
        this.arguments = arguments;
    }

    /**
     * Returns the name of the queue to run this job on.
     * 
     * @return The queue to run the job on
     */
    public String getQueue() {
        return queue;
    }
    
    /**
     * Sets the name of the queue to run this job on.
     * 
     * @param queue The queue to run the job on
     */
    public void setQueue(String queue) {
        assert (queue != null);
        this.queue = queue;
    }

    /**
     * Returns the amount of time this job plans to use (minutes).
     * 
     * @return The amount of time to use
     */
    public String getMaxWallTime() {
        return maxWallTime;
    }
    
    /**
     * Sets the amount of time this job plans to use (minutes).
     * 
     * @param maxWallTime The amount of time to use
     */
    public void setMaxWallTime(String maxWallTime) {
        assert (maxWallTime != null);
        this.maxWallTime = maxWallTime;
    }

    /**
     * Returns the amount of memory this job plans to use (MBs).
     * 
     * @return The amount of memory to use
     */
    public String getMaxMemory() {
        return maxMemory;
    }
    
    /**
     * Sets the amount of memory this job plans to use (MBs).
     * 
     * @param maxMemory The amount of memory to use
     */
    public void setMaxMemory(String maxMemory) {
        assert (maxMemory != null);
        this.maxMemory = maxMemory;
    }

    /**
     * Returns the type of job - for example: Single or MPI.
     * 
     * @return The type of job
     */
    public String getJobType() {
        return jobType;
    }
    
    /**
     * Sets the type of job - for example: Single or MPI.
     * 
     * @param jobType The type of job
     */
    public void setJobType(String jobType) {
        assert (jobType != null);
        this.jobType = jobType;
    }

    /**
     * Returns the number of CPUs the job will use.
     * 
     * @return The number of CPUs to use
     */
    public Integer getCpuCount() {
        return cpuCount;
    }
    
    /**
     * Sets the number of CPUs the job will use.
     * 
     * @param cpuCount The number of CPUs to use
     */ 
    public void setCpuCount(Integer cpuCount) {
        assert (cpuCount != null);
        this.cpuCount = cpuCount;
    }

    /**
     * Returns the list of files to be transferred (staged in) for the job.
     * 
     * @return The file(s) to stage in
     */
    public String[] getInTransfers() {
        return inTransfers;
    }
    
    /**
     * Sets the list of files to be transferred (staged in) for the job.
     * 
     * @param inTransfers The file(s) to stage in
     */
    public void setInTransfers(String[] inTransfers) {
        assert (inTransfers != null);
        this.inTransfers = inTransfers;
    }

    /**
     * Returns the list of files to be transferred (staged out) upon completion
     * of the job.
     * 
     * @return The file(s) to be staged out
     */
    public String[] getOutTransfers() {
        return outTransfers;
    }

    /**
     * Sets the list of files to be transferred (staged out) upon completion
     * of the job.
     * 
     * @param outTransfers The file(s) to be staged out
     */
    public void setOutTransfers(String[] outTransfers) {
        assert (outTransfers != null);
        this.outTransfers = outTransfers;
    }
    
    /**
     * Returns the list of modules that are required for this job to run.
     * 
     * @return The list of modules required
     */
    public String[] getModules() {
        return modules;
    }
    
    /**
     * Sets the list of modules that are required for this job to run.
     * 
     * @param modules The list of modules required
     */
    public void setModules(String[] modules) {
        assert (modules != null);
        this.modules = modules;
    }
    
    /**
     * Gets the notification email address.
     * 
     * @return The notification email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the notification email address.
     * 
     * @param emailAddress The notification email address
     */
    public void setEmailAddress(String emailAddress) {
        assert (emailAddress != null);
        this.emailAddress = emailAddress;
    }

    /**
     * Gets the standard input for the job.
     * 
     * @return The standard input for the job
     */
    public String getStdInput() {
        return stdInput;
    }

    /**
     * Sets the standard input for the job.
     * 
     * @param stdInput The standard input for the job
     */
    public void setStdInput(String stdInput) {
        assert (stdInput != null);
        this.stdInput = stdInput;
    }

    /**
     * Gets the standard output for the job.
     * 
     * @return The standard output for the job
     */
    public String getStdOutput() {
        return stdOutput;
    }

    /**
     * Sets the standard output for the job.
     * 
     * @param stdOutput The standard output for the job
     */
    public void setStdOutput(String stdOutput) {
        assert (stdOutput != null);
        this.stdOutput = stdOutput;
    }

    /**
     * Gets the standard error for the job.
     * 
     * @return The standard error for the job
     */
    public String getStdError() {
        return stdError;
    }

    /**
     * Sets the standard error for the job.
     * 
     * @param stdError The standard error for the job
     */
    public void setStdError(String stdError) {
        assert (stdError != null);
        this.stdError = stdError;
    }


    /**
     * Returns a String representing the state of this
     * <code>GridJob</code> object.
     * 
     * @return A summary of the values of this object's fields
     */
    public String toString() {
        String ftpserver = (siteGridFTPServer==null) ? "null" :
            "\""+siteGridFTPServer+"\"";
        String exe = (exeName==null) ? "null" : "\""+exeName+"\"";
        
        return "site=\"" + site + "\"" +
               ", siteGridFTPServer=" + ftpserver +
               ", name=\"" + name + "\"" +
               ", code=\"" + code + "\"" +
               ", exeName=" + exe +
               ", version=\"" + version + "\"" +
               ", arguments=" + arrayToString(arguments) +
               ", queue=\"" + queue + "\"" +
               ", maxWallTime=\"" + maxWallTime + "\"" +
               ", maxMemory=\"" + maxMemory + "\"" +
               ", jobType=\"" + jobType + "\"" +
               ", cpuCount=" + cpuCount +
               ", inTransfers=" + arrayToString(inTransfers) +
               ", outTransfers=" + arrayToString(outTransfers) +
               ", modules=" + arrayToString(modules) +
               ", emailAddress=\"" + emailAddress + "\"" +
               ", stdInput=\"" + stdInput + "\"" +
               ", stdOutput=\"" + stdOutput + "\"" +
               ", stdError=\"" + stdError + "\"";
    }
}

