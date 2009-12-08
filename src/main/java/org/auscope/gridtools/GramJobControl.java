/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

package org.auscope.gridtools;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.axis.util.Util;
import org.globus.exec.client.GramJob;
import org.globus.exec.client.GramJobListener;
import org.globus.exec.generated.FaultType;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.generated.MultiJobDescriptionType;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.utils.FaultHelper;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.oasis.wsrf.faults.BaseFaultTypeDescription;


/**
 * This class manages jobs using WS-GRAM. It implements the
 * <code>JobControlInterface</code>, and thus is able to submit, kill and get
 * the status and results of a job. Additionally, this class creates the final
 * job script that can be submitted.
 * 
 * @author Ryan Fraser
 * @author Terry Rankine
 * @author Darren Kidd
 * @author Cihan Altinay
 */
public class GramJobControl implements JobControlInterface {
    /** Reference to the Controller's Log4J logger. */
    private Log logger = LogFactory.getLog(getClass());

    // allow a job to run a maximum of 10 days (in milliseconds) by default
    private static final long JOB_LIFETIME_MILLIS = 864000000l;
    private GramJobListener listener = null;
    private GSSCredential credential = null;

    /**
     * Default constructor. Currently empty.
     */
    public GramJobControl() {
    }

    /**
     * Constructor initializing credential.
     */
    public GramJobControl(GSSCredential credential) {
        this.credential = credential;
    }

    /**
     * Sets the listener object that will be notified of status changes.
     *
     * @param listener The new listener object
     */
    public void setListener(GramJobListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the credentials to use for grid operations.
     *
     * @param credential The credentials object
     */
    public void setCredential(GSSCredential credential) {
        this.credential = credential;
    }

    /**
     * Returns the current credential object. If none was set the method tries
     * to create a new grid proxy using the user certificate on the host.
     *
     * @return The grid credentials being used
     */
    private GSSCredential getCredential() throws GSSException {
        GSSCredential cred = this.credential;
        if (cred == null) {
            // if credential was not set try importing default
            logger.warn("Credential is null. Trying to create new proxy.");
            ExtendedGSSManager manager =
                (ExtendedGSSManager)ExtendedGSSManager.getInstance();
            cred = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
        }
        return cred;
    }

    /**
     * Constructs an XML job script (RSL) for a particular job.
     * Given a <code>GridJob</code> object (which holds all the necessary
     * properties of a particular job), this method will construct the actual
     * XML job string which can be submitted.
     * 
     * @param job The <code>GridJob</code> object
     * 
     * @return A string representing the XML job script
     */
    public String constructJobScript(GridJob job) {

        final String DATE_FORMAT = "-yyyyMMdd_HHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        // Create a unique job ID using the current (formatted) date
        final String JOB_ID = job.getName() + sdf.format(new Date());

        String localInputDir;
        String localOutputDir;

        localInputDir = localOutputDir =
            "${GLOBUS_SCRATCH_DIR}/" + JOB_ID + '/';

        String gridFtpInput = job.getSiteGridFTPServer() + localInputDir;
        String gridFtpOutput = job.getSiteGridFTPServer() + localOutputDir;

        // Header
        String finalJobString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<job> <executable>" + job.getExeName() + "</executable>" +
            " <directory>" + localOutputDir + "</directory>";

        // Arguments
        for (String arg : job.getArguments()) {
            finalJobString += " <argument>" + arg + "</argument>";
        }

        // Environment
        finalJobString += " <environment>  <name>USERJOB</name>  <value>" +
            job.getName() + "</value> </environment> <environment>" +
            "  <name>INPUTDIR</name>  <value>" + localInputDir + "</value>" +
            " </environment> <environment>  <name>OUTPUTDIR</name>" +
            "  <value>" + localOutputDir + "</value> </environment>";


        if (job.getJobType().equalsIgnoreCase("single") &&
                job.getCpuCount() > 1)
            finalJobString += " <environment>  <name>OMP_NUM_THREADS</name>" +
                "  <value>" + job.getCpuCount()+ "</value> </environment>";

        // IO redirection.

        // Input redirection
        if (job.getStdInput().length() > 0) {
            finalJobString += " <stdin>" + localInputDir + job.getStdInput() + "</stdin>";
        }
        // Output redirection        
        if (job.getStdOutput().length() > 0) {
            finalJobString += " <stdout>" + localOutputDir + job.getStdOutput() + "</stdout>";
        } else {
            finalJobString += " <stdout>" + localOutputDir + "stdOut</stdout>";
        }
        // Error redirection        
        if (job.getStdError().length() > 0) {
            finalJobString += " <stderr>" + localOutputDir + job.getStdError() + "</stderr>";
        } else {
            finalJobString += " <stderr>" + localOutputDir + "stdError</stderr>";
        }

        // Job Properties...
        finalJobString += " <count>" + job.getCpuCount() + "</count>" +
            " <queue>" + job.getQueue() + "</queue> <maxWallTime>" +
            job.getMaxWallTime() + "</maxWallTime> <maxMemory>" +
            job.getMaxMemory() + "</maxMemory> <jobType>" + job.getJobType() +
            "</jobType>";

        // File Stage In
        if (job.getInTransfers()[0].compareToIgnoreCase("NULL") != 0) {
            finalJobString += " <fileStageIn>";

            for (String xfer : job.getInTransfers()) {
                finalJobString += "  <transfer>   <sourceUrl>" + xfer +
                    "</sourceUrl>   <destinationUrl>" + gridFtpInput +
                    "</destinationUrl>  </transfer>";
            }
            finalJobString += " </fileStageIn>";
        }

        // File Stage Out
        if (job.getOutTransfers()[0].compareToIgnoreCase("NULL") !=0 ) {
            finalJobString += " <fileStageOut>";
            for (String xfer : job.getOutTransfers()) {
                finalJobString += "  <transfer>   <sourceUrl>" +
                    gridFtpOutput + "</sourceUrl>" +
                    "   <destinationUrl>" + xfer + "/</destinationUrl>" +
                    "  </transfer>";
            }
            finalJobString += " </fileStageOut>";
        }

        // Delete the directory created on remote resource and all associated
        // files
        finalJobString += " <fileCleanUp>  <deletion>   <file>" +
            gridFtpOutput + "</file>  </deletion> </fileCleanUp>";

        // Start Extensions
        finalJobString += " <extensions>  <globusrunAnnotation>" +
            "   <automaticJobDelegation>true</automaticJobDelegation>" +
            "   <automaticStagingDelegation>true</automaticStagingDelegation>" +
            "   <automaticStageInDelegation>true</automaticStageInDelegation>" +
            "   <automaticStageOutDelegation>true</automaticStageOutDelegation>" +
            "   <automaticCleanUpDelegation>true</automaticCleanUpDelegation>" +
            "  </globusrunAnnotation>";

        // Modules
        if (job.getModules() != null) {
            for (String mod : job.getModules()) {
                finalJobString += "  <module>" + mod + "</module>";
            }
        }

        // email address for notification
        finalJobString += "  <email_address>" + job.getEmailAddress() +
            "</email_address>  <email_on_execution>yes</email_on_execution>" +
            "  <email_on_abort>yes</email_on_abort>" +
            "  <email_on_termination>yes</email_on_termination>";

        // Code and version - just for debugging
        finalJobString += "  <code>" + job.getCode() + "</code>  <version>" +
            job.getVersion() + "</version>";

        // Finish Extensions
        finalJobString += " </extensions>";

        // Close Job Tag
        finalJobString += "</job>";

        // Make pretty (readable) job description. Put newlines at the end of
        // each tag.
        finalJobString = finalJobString.replaceAll("><", ">\n<");
        finalJobString = finalJobString.replaceAll("> <", ">\n <");
        finalJobString = finalJobString.replaceAll(">  <", ">\n  <");
        finalJobString = finalJobString.replaceAll(">   <", ">\n   <");

        return finalJobString;
    }

    /**
     * Constructs an array of job descriptions for a multi-job.
     * Given a <code>GridJob</code> object (which holds all the necessary
     * properties of a particular multi-job), this method will construct
     * <code>JobDescriptionType</code> objects which can later be submitted.
     * 
     * @param job The <code>GridJob</code> object
     * 
     * @return An array of job descriptions
     */
    public JobDescriptionType[] constructMultiJobScript(GridJob job) {
        final String DATE_FORMAT = "-yyyyMMdd_HHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        // Create a unique job ID using the current (formatted) date
        final String JOB_ID = job.getName() + sdf.format(new Date());

        String writeJobStr = "";

        // multi with optional mpi support
        // we are in multi so set the multi/mpi jobtype to simply mpi now
        if(job.getJobType().equalsIgnoreCase("multi/mpi"))
            job.setJobType("mpi");
        else
            job.setJobType("single");

        JobDescriptionType[] jobs = new JobDescriptionType[job.getArguments().length];

        for (int i=0; i<job.getArguments().length; i++) {
            String localInputDir;
            String localOutputDir;
            localInputDir = localOutputDir = "${GLOBUS_SCRATCH_DIR}/" +
                JOB_ID + "_subJob_" + Integer.toString(i)+ '/';
            String gridFtpInput = job.getSiteGridFTPServer() + localInputDir;
            String gridFtpOutput = job.getSiteGridFTPServer() + localOutputDir;

            String finalJobString = "<job> <executable>" + job.getExeName() +
                "</executable> <directory>" + localOutputDir + "</directory>";

            // Arguments
            finalJobString += " <argument>" + job.getArguments()[i] + "</argument>";

            // Environment
            finalJobString += " <environment>  <name>USERJOB</name>" +
                "  <value>" + job.getName() + "</value> </environment>" +
                " <environment>  <name>INPUTDIR</name>  <value>" +
                localInputDir + "</value> </environment> <environment>" +
                "  <name>OUTPUTDIR</name>  <value>" + localOutputDir +
                "</value> </environment>";

            if (job.getJobType().equalsIgnoreCase("single") &&
                    job.getCpuCount() > 1)
                finalJobString += " <environment>  <name>OMP_NUM_THREADS</name>" +
                    "  <value>" + job.getCpuCount()+ "</value> </environment>";
        
            // IO redirection.

            // Input redirection
            if (job.getStdInput().length() > 0) {
                finalJobString += " <stdin>" + localInputDir + job.getStdInput() + "</stdin>";
            }
            //Output redirection        
            if (job.getStdOutput().length() > 0) {
                finalJobString += " <stdout>" + localOutputDir + job.getStdOutput() + "</stdout>";
            } else {
                finalJobString += " <stdout>" + localOutputDir + "stdOut</stdout>";
            }
        
            finalJobString += " <stderr>" + localOutputDir + "stdError</stderr>";
        
            // Job Properties...
            finalJobString += " <count>" + job.getCpuCount() + "</count>" +
                " <queue>" + job.getQueue() + "</queue>" + " <maxWallTime>" +
                job.getMaxWallTime() + "</maxWallTime> <maxMemory>" +
                job.getMaxMemory() + "</maxMemory> <jobType>" +
                job.getJobType() + "</jobType>";
        
            // File Stage In
            //something was specified in the stageIn directives thus do the following:
            if (job.getInTransfers()[0].compareToIgnoreCase("NULL") != 0) {
                finalJobString += " <fileStageIn>";
                    
                for (String xfer : job.getInTransfers()) {
                    finalJobString += "  <transfer>   <sourceUrl>" + xfer +
                        "</sourceUrl>   <destinationUrl>" + gridFtpInput +
                        "</destinationUrl>  </transfer>";
                }
                finalJobString += " </fileStageIn>";
            }
                
            // File Stage Out
            if (job.getOutTransfers()[0].compareToIgnoreCase("NULL") != 0) {
                finalJobString += " <fileStageOut>";
                for (String xfer : job.getOutTransfers()) {
                    finalJobString += "  <transfer>   <sourceUrl>" +
                        gridFtpOutput +
                        "</sourceUrl>   <destinationUrl>" + xfer + JOB_ID +
                        "_subJob_" + Integer.toString(i) +
                        "/</destinationUrl>  </transfer>";
                }
                finalJobString += " </fileStageOut>";
            }
                
            // Delete the directory created on remote resource and all
            // associated files
            finalJobString += " <fileCleanUp>  <deletion>   <file>" +
                gridFtpOutput + "</file>  </deletion> </fileCleanUp>";
        
            // Start Extensions
            finalJobString += " <extensions>  <globusrunAnnotation>" +
                "   <automaticJobDelegation>true</automaticJobDelegation>" +
                "   <automaticStagingDelegation>true</automaticStagingDelegation>" +
                "   <automaticStageInDelegation>true</automaticStageInDelegation>" +
                "   <automaticStageOutDelegation>true</automaticStageOutDelegation>" +
                "   <automaticCleanUpDelegation>true</automaticCleanUpDelegation>" +
                "  </globusrunAnnotation>";

            // Modules
            if (job.getModules() != null) {
                for (String mod : job.getModules()) {
                    finalJobString += "  <module>" + mod + "</module>";
                }
            }
        
            // Code and version - just for debugging
            finalJobString += "  <code>" + job.getCode() + "</code>  <version>" +
                job.getVersion() + "</version>";
        
            // Finish Extensions
            finalJobString += " </extensions>";

            // Close Job Tag
            finalJobString += "</job>";
            try {
                jobs[i]= RSLHelper.readRSL(finalJobString);
            } catch (Throwable e) {
                logger.error("Could not translate job string into JobDescriptionType");
                logger.error(e.getMessage(), e);
            }
            logger.debug("Finished creating job description "+i);

            writeJobStr += finalJobString;
        }
            
        /*
        try {
            final String JOBFILE = "GridJob.xml";

            logger.info("Writing " + JOBFILE + " file...");
            FileWriter fw = new FileWriter(JOBFILE);

            // Make pretty (readable) job description. Put newlines at the
            // end of each tag.
            writeJobStr = writeJobStr.replaceAll("><", ">\n<");
            writeJobStr = writeJobStr.replaceAll("> <", ">\n <");
            writeJobStr = writeJobStr.replaceAll(">  <", ">\n  <");
            writeJobStr = writeJobStr.replaceAll(">   <", ">\n   <");
            fw.write(writeJobStr);
            fw.close();
        } catch (Throwable e) {
            logger.error("Could not write Job string to file.");
            logger.error(e.getMessage());
        }
        */

        return jobs;
    }
 
    /**
     * Submits a job to a site.
     * Given a <code>GridJob</code> object (which holds all the necessary
     * properties of a particular job) and a job manager address, this method
     * will construct the correct type of job script and submit the job.
     * 
     * @param job The <code>GridJob</code> object
     * @param host The address of a site's job manager to submit the job to
     * 
     * @return The endpoint reference (EPR) of the job, or <code>null</code>
     *         if the submission was not successful.
     *
     * @see #submitJob(String, String)
     * @see #submitMultiJob
     */
    public String submitJob(GridJob job, String host) {
        String jobSubmitEPR = null;
        
        // Construct the correct job script depending on job type.
        if (job.getJobType().equalsIgnoreCase("single") ||
            job.getJobType().equalsIgnoreCase("mpi"))
        {
            String jobStr = constructJobScript(job);
            logger.info("Submitting job to " + host);
            logger.debug("RSL:\n" + jobStr);
            jobSubmitEPR = submitJob(jobStr, host);
        } else {
            JobDescriptionType[] jobStrMulti = constructMultiJobScript(job);
            logger.info("Submitting multijob to " + host);
            jobSubmitEPR = submitMultiJob(jobStrMulti, host);
        }

        return jobSubmitEPR;
    }

    /**
     * Submit the job using WS-GRAM. Sends the job string to the specified host.
     * Returns the EPR of the job, unless an error occurred, in which case it
     * will happily return <code>null</code>.
     * 
     * @param jobString The RSL of the job to be submitted
     * @param host      The host site to send the job to
     * 
     * @return The EPR to the job, or <code>null</code> if something went wrong
     */
    public String submitJob(String jobString, String host) {

        String gramJobHandle = null;
        // Set a system property - for Apache Axis (Java Web Services platform).
        System.setProperty("axis.configFile", "client-config.wsdd");
        Util.registerTransport();

        try {
            JobDescriptionType jobDescription = RSLHelper.readRSL(jobString);

            // Create new GRAM job. Get GRAM end point reference.
            GramJob job = new GramJob(jobDescription);
            URL factoryUrl = ManagedJobFactoryClientHelper.
                    getServiceURL(host).getURL();
            EndpointReferenceType gramEndpoint = ManagedJobFactoryClientHelper.
                    getFactoryEndpoint(factoryUrl, "PBS");

            // Auth stuff, and setting the credentials.
            job.setDelegationEnabled(true);
            job.setCredentials(getCredential());
            // this is weird but see here:
            // http://lists.globus.org/pipermail/gram-user/2007-November/000633.html
            job.setDuration(new Date(
                        System.currentTimeMillis()+JOB_LIFETIME_MILLIS));
            //job.setTerminationTime(XXX);
            HostAuthorization iA = new HostAuthorization();

            job.setAuthorization(iA);
            // Listen for Job state changes if requested.
            if (listener != null)
                job.addListener(listener);
            job.submit(gramEndpoint, false); // SUBMIT THE JOB! YAY!

            job.refreshStatus();
            gramJobHandle = job.getHandle(); // Get the handle to the job.
        } catch (Exception e) {
            logger.error(getGlobusErrorDescription(e), e);
        }
        return gramJobHandle; // Return the handle...
    }

 
    /**
     * Submits a multi-job using WS-GRAM. Creates a multi-job description
     * containing all provided jobs and sends it to the specified host.
     * Returns the EPR of the multi-job, unless an error occurred, in which
     * case <code>null</code> is returned.
     * 
     * @param jobs An array of job descriptions to be submitted
     * @param host The host site the multi-job is to be sent to
     * 
     * @return The EPR to the job, or <code>null</code> if something went wrong
     */
    public String submitMultiJob(JobDescriptionType[] jobs, String host) {
        String gramJobHandle = null;
        // Set a system property - for Apache Axis (Java Web Services platform).
        System.setProperty("axis.configFile", "client-config.wsdd");
        Util.registerTransport();

        try {
            for(int i=0;i<jobs.length;i++) {
                String factoryTypeSubs = ManagedJobFactoryConstants.FACTORY_TYPE.PBS;
                URL factoryUrl = ManagedJobFactoryClientHelper.getServiceURL(host).getURL();
                EndpointReferenceType factoryEndpointSubJobs = ManagedJobFactoryClientHelper.getFactoryEndpoint(factoryUrl, factoryTypeSubs);
                jobs[i].setFactoryEndpoint(factoryEndpointSubJobs);
            }

            String factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.MULTI;
            URL factoryUrl = ManagedJobFactoryClientHelper.getServiceURL(host).getURL();
            EndpointReferenceType gramEndpoint = ManagedJobFactoryClientHelper.getFactoryEndpoint(factoryUrl, factoryType);

            logger.debug("creating MultiJobDescriptionType");
            MultiJobDescriptionType multiJobDescription =  new MultiJobDescriptionType();
            multiJobDescription.setJob(jobs);
            logger.debug("creating GramJob");
            // Create new GRAM job. Get GRAM end point reference.
            GramJob job = new GramJob(multiJobDescription);

            // Auth stuff, and setting the credentials.
            job.setDelegationEnabled(true);
            job.setCredentials(getCredential());
            // this is weird but see here:
            // http://lists.globus.org/pipermail/gram-user/2007-November/000633.html
            job.setDuration(new Date(
                        System.currentTimeMillis()+JOB_LIFETIME_MILLIS));
            //job.setTerminationTime(XXX);
            HostAuthorization iA = new HostAuthorization();

            job.setAuthorization(iA);
            // Listen for Job state changes if requested.
            if (listener != null)
                job.addListener(listener);
            job.submit(gramEndpoint, false); // SUBMIT THE JOB! YAY!

            job.getState();
            gramJobHandle = job.getHandle(); // Get the handle to the job.
        } catch (Exception e) {
            logger.error(getGlobusErrorDescription(e), e);
        }
        return gramJobHandle; // Return the handle...
    }
 
    /**
     * Kills a job given its EPR. Returns a <code>String</code> indicating the
     * status of the job after being killed.
     * 
     * @param reference The EPR to the job to kill
     * 
     * @return The new state of the killed job
     */
    public String killJob(String reference) {

        String condition = null;

        try {
            // Find the job, and make sure we are authorized to kill it.
            GramJob job = new GramJob();
            job.setHandle(reference);
            job.setCredentials(getCredential());

            // Kill it.
            job.cancel();

            try {
                // Figure out the status...
                StateEnumeration jobState = job.getState();
                condition = jobState.getValue();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(getGlobusErrorDescription(e), e);
        }

        return condition;
    }

    /**
     * Retrieves the status of a job, as well as any faults that may have
     * occurred. Returns a <code>String</code> containing the status and logs
     * any faults.
     * 
     * @param reference The EPR of the job
     * 
     * @return A <code>String</code> indicating the status of the job
     */
    public String getJobStatus(String reference) {
 
        String condition = null;

        try {
            // Find our job, and refresh its status.
            GramJob job = new GramJob();
            job.setHandle(reference);
            job.setCredentials(getCredential());
            job.refreshStatus();

            try {
                // Get the state of our job.
                StateEnumeration jobState = job.getState();
                condition = jobState.getValue();

                // Checking for faults...
                FaultType jobFaults = job.getFault();
                if (jobFaults != null) {
                    BaseFaultTypeDescription[] faultArray =
                        jobFaults.getDescription();
                    StringBuffer buf = new StringBuffer();
                    buf.append("Fault array:\n");
                    for (BaseFaultTypeDescription currFault : faultArray) {
                        buf.append('\t');
                        buf.append(currFault.get_value());
                        buf.append('\n');
                    }
                    logger.info(buf.toString());
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } catch (Exception e) {
            logger.error(getGlobusErrorDescription(e));
        }
        return condition;
    }

    /**
     * Returns a descriptive error message for given exception.
     * 
     * @param e the exception
     * 
     * @return a <code>String</code> describing the error
     */
    private String getGlobusErrorDescription(Exception e) {
        if (e.getMessage() == null) {
            return FaultHelper.getMessage(e);
        } else if (e.getMessage().indexOf("Expired credentials detected") != -1) {
            StringBuffer myBuffer = new StringBuffer();
            myBuffer.append("Error: Expired Credentials detected.\n");
            myBuffer.append("Please ensure you have a current proxy!");
            return myBuffer.toString();

        } else if (e.getCause() != null &&
                   e.getCause().getMessage() != null) {
            if (e.getCause().getMessage().indexOf("Unknown CA") != -1) {
                // Unknown CA error
                StringBuffer myBuffer = new StringBuffer();
                myBuffer.append("Error: You don't trust the CA certificate for this server.\n");
                myBuffer.append("To trust it, you must add its CA cert into either:\n");
                myBuffer.append(" A) your personal trust dir: $HOME/.globus/certificates\n");
                myBuffer.append("   or\n");
                myBuffer.append(" B) the systemwide trust dir: /etc/grid-security/certificates");
                return myBuffer.toString();

            } else if (e.getCause().getMessage().indexOf("Connection timed out: connect") != -1) {
                // connection exception -- either firewall or connect time out
                StringBuffer myBuffer = new StringBuffer();
                myBuffer.append("Timeout while connecting to the host.\n");
                myBuffer.append("Please contact the site or try again.");
                return myBuffer.toString();
            }
        }
        return e.getMessage();
    }

    /**
     * Retrieves the intermittent results of a job given its EPR.
     * This method will submit a 'dummy' job that merely stages current files
     * of the given (running) job to the designated output.
     * 
     * @param reference The EPR of the job to stage files from
     * 
     * @return the stage-out location where files will be transferred to
     */
    public String getJobResults(String reference) {
        String outputDirectory = null;
        try {
            GramJob job = new GramJob();
            job.setHandle(reference);
            job.setCredentials(getCredential());
            JobDescriptionType jobDesc = job.getDescription();
            String workingDirectory = jobDesc.getDirectory();
            outputDirectory = jobDesc.getFileStageOut().getTransfer(0)
                .getDestinationUrl();
            // strip the service and port off the host (EPR contains
            // ManagedJobExecutableService, need ManagedJobFactoryService to
            // submit to!
            String submissionHost = reference.split("\\:8443")[0];
            logger.debug("host = " + submissionHost);

            System.setProperty("axis.configFile", "client-config.wsdd");
            Util.registerTransport();

            // create a job description for the file transfer
            String jobDescriptionString = "<job>" + 
                " <executable>/bin/hostname</executable>" +
                " <directory>/tmp/</directory> <stdout>/dev/null</stdout> " +
                " <stderr>/dev/null</stderr>  <fileStageOut> <transfer>" +
                " <sourceUrl>file://" + workingDirectory + "</sourceUrl>" +
                " <destinationUrl>" + outputDirectory + "</destinationUrl>" +
                " </transfer> </fileStageOut> </job>";
            logger.debug("RSL:\n" + jobDescriptionString);

            jobDesc = RSLHelper.readRSL(jobDescriptionString);

            // Create new GRAM job
            job = new GramJob(jobDesc);

            URL factoryUrl = ManagedJobFactoryClientHelper.getServiceURL(
                submissionHost + ":8443/wsrf/services/ManagedJobFactoryService").getURL();

            EndpointReferenceType gramEndpoint = ManagedJobFactoryClientHelper
                .getFactoryEndpoint(factoryUrl,
                        ManagedJobFactoryConstants.FACTORY_TYPE.FORK);

            job.setDelegationEnabled(true);
            job.setCredentials(getCredential());
            job.submit(gramEndpoint, false); // SUBMIT THE JOB!
            logger.debug("Job handle: "+job.getHandle());

        } catch (Exception e) {
            logger.error(getGlobusErrorDescription(e), e);
            outputDirectory = null;
        }

        return outputDirectory;
    }
}

