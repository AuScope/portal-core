/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import org.auscope.gridtools.GridJob;

/**
 * Simple class that stores information about a VRL job which can be submitted
 * to the grid by a user.
 * An instance of this class represents the state of a VRL job at some point in
 * time. It can be treated as a simple data structure (e.g. in an array).
 * <p>
 * Assertions should be enabled when using this class to prevent others from
 * setting values to <code>null</code>.
 *
 * @author Cihan Altinay
 */
public class GeodesyJob extends GridJob {
    /** The name of the software to run Geodesy jobs with */
    public static final String CODE_NAME = "Gamit";
    /** The job type */
    private static final String JOB_TYPE = "single";
    /** The prefix of checkpoint files */
    private String    checkpointPrefix;
    /** A description for this job */
    private String    description;
    /** A unique identifier for this job */
    private Integer   id;
    /** The number of bonds */
    private Integer   numBonds;
    /** The number of particles */
    private Integer   numParticles;
    /** The number of timesteps */
    private Integer   numTimesteps;
    /** Directory containing output files */
    private String    outputDir;
    /** A unique job reference (e.g. EPR). */
    private String    reference;
    /** The script filename */
    private String    scriptFile;
    /** The ID of the series this job belongs to */
    private Integer   seriesId;
    /** The job status */
    private String    status;
    /** The submission date and time */
    private String    submitDate;


    /**
     * Does some basic setting up of the class variables to prevent errors.
     * Strings are initialized to the empty String, and integers are
     * initialized with zero.
     */
    public GeodesyJob() {
        super();
        description = outputDir = reference = scriptFile = status =
            submitDate = "";
        id = numBonds = numParticles = numTimesteps = seriesId = 0;
        setCode(CODE_NAME);
        setJobType(JOB_TYPE);
    }

    /**
     * Alternate constructor initializing <code>GridJob</code> members.
     *
     * @param site              The site the job will be run at
     * @param name              A descriptive name for this job
     * @param version           Version of code to use
     * @param arguments         Arguments for the code
     * @param queue             Which queue to use
     * @param maxWallTime       Amount of time we plan to use
     * @param maxMemory         Amount of memory we plan to use
     * @param cpuCount          Number of CPUs to use (if jobType is single)
     * @param inTransfers       Files to be transferred in
     * @param outTransfers      Files to be transferred out
     * @param emailAddress      The email address for PBS notifications
     * @param stdInput          The std input file for the job
     * @param stdOutput         The std output file for the job
     * @param stdError          The std error file for the job
     */
    public GeodesyJob(String site, String name, String version, String[] arguments,
                  String queue, String maxWallTime, String maxMemory,
                  Integer cpuCount, String[] inTransfers,
                  String[] outTransfers, String emailAddress, String stdInput,
                  String stdOutput, String stdError)
    {
        super(site, name, CODE_NAME, version, arguments, queue, JOB_TYPE,
                maxWallTime, maxMemory, cpuCount, inTransfers, outTransfers,
                emailAddress, stdInput, stdOutput, stdError);
        description = outputDir = reference = scriptFile = status =
            submitDate = "";
        id = numBonds = numParticles = numTimesteps = seriesId = 0;
    }


    /**
     * Returns the unique identifier of this job.
     *
     * @return The ID of this job.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this job.
     *
     * @param id The unique ID of this job.
     */
    private void setId(Integer id) {
        assert (id != null);
        this.id = id;
    }

    /**
     * Returns the checkpoint file prefix of this job.
     *
     * @return The checkpoint file prefix of this job.
     */
    public String getCheckpointPrefix() {
        return checkpointPrefix;
    }

    /**
     * Sets the checkpoint file prefix of this job.
     *
     * @param checkpointPrefix The checkpoint file prefix of this job.
     */
    public void setCheckpointPrefix(String checkpointPrefix) {
        assert (checkpointPrefix != null);
        this.checkpointPrefix = checkpointPrefix;
    }

    /**
     * Returns the description of this job.
     *
     * @return The description of this job.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this job.
     *
     * @param description The description of this job.
     */
    public void setDescription(String description) {
        assert (description != null);
        this.description = description;
    }

    /**
     * Returns the number of bonds in this simulation job.
     *
     * @return The number of bonds in this simulation job.
     */
    public Integer getNumBonds() {
        return numBonds;
    }

    /**
     * Sets the number of bonds in this simulation job.
     *
     * @param numBonds The number of bonds in this simulation job.
     */
    public void setNumBonds(Integer numBonds) {
        assert (numBonds != null);
        this.numBonds = numBonds;
    }

    /**
     * Returns the number of particles in this simulation job.
     *
     * @return The number of particles in this simulation job.
     */
    public Integer getNumParticles() {
        return numParticles;
    }

    /**
     * Sets the number of particles in this simulation job.
     *
     * @param numParticles The number of particles in this simulation job.
     */
    public void setNumParticles(Integer numParticles) {
        assert (numParticles != null);
        this.numParticles = numParticles;
    }

    /**
     * Returns the number of timesteps in this simulation job.
     *
     * @return The number of timesteps in this simulation job.
     */
    public Integer getNumTimesteps() {
        return numTimesteps;
    }

    /**
     * Sets the number of timesteps in this simulation job.
     *
     * @param numTimesteps The number of timesteps in this simulation job.
     */
    public void setNumTimesteps(Integer numTimesteps) {
        assert (numTimesteps != null);
        this.numTimesteps = numTimesteps;
    }

    /**
     * Returns the output directory of this job.
     *
     * @return The output directory of this job.
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * Sets the output directory of this job.
     *
     * @param outputDir The output directory of this job.
     */
    public void setOutputDir(String outputDir) {
        assert (outputDir != null);
        this.outputDir = outputDir;
    }

    /**
     * Returns the unique reference of this job.
     *
     * @return The reference of this job.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the unique reference of this job.
     *
     * @param reference The unique reference of this job.
     */
    public void setReference(String reference) {
        assert (reference != null);
        this.reference = reference;
    }

    /**
     * Returns the script filename of this job.
     *
     * @return The script filename of this job.
     */
    public String getScriptFile() {
        return scriptFile;
    }

    /**
     * Sets the script filename of this job.
     *
     * @param scriptFile The script filename.
     */
    public void setScriptFile(String scriptFile) {
        assert (scriptFile != null);
        this.scriptFile = scriptFile;
    }

    /**
     * Returns the series ID this job belongs to.
     *
     * @return The series ID of this job.
     */
    public Integer getSeriesId() {
        return seriesId;
    }

    /**
     * Sets the ID of the series this job belongs to.
     *
     * @param seriesId The series ID of this job.
     */
    public void setSeriesId(Integer seriesId) {
        assert (seriesId != null);
        this.seriesId = seriesId;
    }

    /**
     * Returns the submit date of this job.
     *
     * @return The submit date of this job.
     */
    public String getSubmitDate() {
        return submitDate;
    }

    /**
     * Sets the submit date of this job.
     *
     * @param submitDate The submit date of this job.
     */
    public void setSubmitDate(String submitDate) {
        assert (submitDate != null);
        this.submitDate = submitDate;
    }

    /**
     * Returns the status of this job.
     *
     * @return The status of this job.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of this job.
     *
     * @param status The status of this job.
     */
    public void setStatus(String status) {
        assert (status != null);
        this.status = status;
    }

    /**
     * Returns a String representing the state of this <code>GeodesyJob</code>
     * object.
     *
     * @return A summary of the values of this object's fields
     */
    public String toString() {
        return super.toString() +
               ", id=" + id +
               ", seriesId=" + seriesId +
               ", description=\"" + description + "\"" +
               ", checkpointPrefix=\"" + checkpointPrefix + "\"" +
               ", numBonds=" + numBonds +
               ", numParticles=" + numParticles +
               ", numTimesteps=" + numTimesteps +
               ", outputDir=\"" + outputDir + "\"" +
               ", reference=\"" + reference + "\"" +
               ", scriptFile=\"" + scriptFile + "\"" +
               ", submitDate=\"" + submitDate + "\"" +
               ", status=\"" + status + "\"";
    }
}

