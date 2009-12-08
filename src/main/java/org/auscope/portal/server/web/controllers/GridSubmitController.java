/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.StringReader;
import java.net.URL;
import java.rmi.ServerException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.gridjob.ScriptParser;
import org.auscope.portal.server.gridjob.Util;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;
import org.auscope.portal.server.gridjob.GeodesySeries;
import org.auscope.portal.server.util.GeodesyUtil;
import org.auscope.portal.server.web.view.JSONModelAndView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.view.RedirectView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//Globus stuff
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyException;
import org.globus.myproxy.MyProxyException;
import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSCredential;


/**
 * Controller for the job submission view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 */
@Controller
public class GridSubmitController {

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private GridAccessController gridAccess;
    @Autowired
    private GeodesyJobManager jobManager;

    private static final String TABLE_DIR = "tables";
    private static final String RINEX_DIR = "rinex";
    private static final String PRE_STAGE_IN_TABLE_FILES = "/home/grid-auscope/tables/";
    private static final String IVEC_MIRROR_URL = "http://files.ivec.org/geodesy/";
    private static final String PBSTORE_RINEX_PATH = "//pbstore/cg01/geodesy/ftp.ga.gov.au/gpsdata/";

    //Grid File Transfer messages
    private static final String FILE_COPIED = "Please wait while files being transfered.... ";
    private static final String FILE_COPY_ERROR = "Job submission failed due to file transfer Error.";
    private static final String INTERNAL_ERROR= "Job submission failed due to INTERNAL ERROR";
    private static final String GRID_LINK = "Job submission failed due to GRID Link Error";
    private static final String TRANSFER_COMPLETE = "Transfer Complete";
    private static final String CREDENTIAL_ERROR = "Job submission failed due to Invalid Credential Error";
    
    
    /**
     * Sets the <code>GridAccessController</code> to be used for grid
     * activities.
     *
     * @param gridAccess the GridAccessController to use
     */
    /*public void setGridAccess(GridAccessController gridAccess) {
        this.gridAccess = gridAccess;
    }*/

    /**
     * Sets the <code>GeodesyJobManager</code> to be used to retrieve and store
     * series and job details.
     *
     * @param jobManager the JobManager to use
     */
    /*public void setJobManager(GeodesyJobManager jobManager) {
        this.jobManager = jobManager;
    }*/

    /*protected ModelAndView handleNoSuchRequestHandlingMethod(
            NoSuchRequestHandlingMethodException ex,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Ensure user has valid grid credentials
        if (gridAccess.isProxyValid(
                request.getSession().getAttribute("userCred"))) {
        	logger.debug("No/invalid action parameter; returning gridsubmit view.");
        	return new ModelAndView("gridsubmit");
        } 
        else 
        {
        	request.getSession().setAttribute(
                "redirectAfterLogin", "/gridsubmit.html");
        	logger.warn("Proxy not initialized. Redirecting to gridLogin.");
        	return new ModelAndView(
                new RedirectView("/gridLogin.html", true, false, false));
        }
    }*/

    /**
     * Returns a JSON object containing a list of the current user's series.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         GeodesySeries objects.
     */
    @RequestMapping("/mySeries.do")
    public ModelAndView mySeries(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = request.getRemoteUser();

        logger.debug("Querying series of "+user);
        List<GeodesySeries> series = jobManager.querySeries(user, null, null);

        logger.debug("Returning list of "+series.size()+" series.");
        return new ModelAndView("jsonView", "series", series);
    }

    /**
     * Very simple helper class (bean).
     */
    public class SimpleBean {
        private String value;
        public SimpleBean(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    /**
     * Returns a JSON object containing an array of ESyS-particle sites.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a sites attribute which is an array of
     *         sites on the grid that have an installation of ESyS-particle.
     */
    @RequestMapping("/listSites.do")    
    public ModelAndView listSites(HttpServletRequest request,
                                  HttpServletResponse response) {

        logger.debug("Retrieving sites with "+GeodesyJob.CODE_NAME+" installations.");
        String[] particleSites = gridAccess.
                retrieveSitesWithSoftwareAndVersion(GeodesyJob.CODE_NAME, "");

        List<SimpleBean> sites = new ArrayList<SimpleBean>();
        for (int i=0; i<particleSites.length; i++) {
            sites.add(new SimpleBean(particleSites[i]));
            logger.debug("Site name: "+particleSites[i]);
        }

        logger.debug("Returning list of "+particleSites.length+" sites.");
        return new ModelAndView("jsonView", "sites", sites);
    }

    /**
     * Returns a JSON object containing an array of job manager queues at
     * the specified site.
     *
     * @param request The servlet request including a site parameter
     * @param response The servlet response
     *
     * @return A JSON object with a queues attribute which is an array of
     *         job queues available at requested site.
     */
    @RequestMapping("/listSiteQueues.do")    
    public ModelAndView listSiteQueues(HttpServletRequest request,
                                       HttpServletResponse response) {

        String site = request.getParameter("site");
        List<SimpleBean> queues = new ArrayList<SimpleBean>();

        if (site != null) {
            logger.debug("Retrieving queue names at "+site);

            String[] siteQueues = gridAccess.
                    retrieveQueueNamesAtSite(site);

            for (int i=0; i<siteQueues.length; i++) {
                queues.add(new SimpleBean(siteQueues[i]));
            }
        } else {
            logger.warn("No site specified!");
        }

        logger.debug("Returning list of "+queues.size()+" queue names.");
        return new ModelAndView("jsonView", "queues", queues);
    }

    /**
     * Returns a JSON object containing an array of versions at
     * the specified site.
     *
     * @param request The servlet request including a site parameter
     * @param response The servlet response
     *
     * @return A JSON object with a versions attribute which is an array of
     *         versions installed at requested site.
     */
    @RequestMapping("/listSiteVersions.do")    
    public ModelAndView listSiteVersions(HttpServletRequest request,
                                         HttpServletResponse response) {

        String site = request.getParameter("site");
        List<SimpleBean> versions = new ArrayList<SimpleBean>();

        if (site != null) {
            logger.debug("Retrieving versions at "+site);

            String[] siteVersions = gridAccess.
                    retrieveCodeVersionsAtSite(site, GeodesyJob.CODE_NAME);

            for (int i=0; i<siteVersions.length; i++) {
                versions.add(new SimpleBean(siteVersions[i]));
            }
        } else {
            logger.warn("No site specified!");
        }

        logger.debug("Returning list of "+versions.size()+" versions.");
        return new ModelAndView("jsonView", "versions", versions);
    }

    /**
     * Returns a JSON object containing a populated GeodesyJob object.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a data attribute containing a populated
     *         GeodesyJob object and a success attribute.
     */
    @RequestMapping("/getJobObject.do")    
    public ModelAndView getJobObject(HttpServletRequest request,
                                     HttpServletResponse response) {

        GeodesyJob job = prepareModel(request);

        logger.debug("Returning job.");
        ModelAndView result = new ModelAndView("jsonView");

        GridTransferStatus status = (GridTransferStatus)request.getSession().getAttribute("gridStatus");
        if(status == null || job == null){
            logger.error("Job setup failure.");
            result.addObject("success", false);
        }else{
        	if(status.jobSubmissionStatus == JobSubmissionStatus.Failed){
                logger.error("Job setup failure.");
                result.addObject("success", false);        		
        	}
            logger.debug("Job setup success.");
            result.addObject("data", job);
            result.addObject("success", true);
        }

        return result;
    }

    /**
     * Returns a JSON object containing an array of filenames and sizes which
     * are currently in the job's stage in directory.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a files attribute which is an array of
     *         filenames.
     */
    @RequestMapping("/listJobFiles.do")    
    public ModelAndView listJobFiles(HttpServletRequest request,
                                     HttpServletResponse response) {

        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");

        List files = new ArrayList<FileInformation>();

        if (jobInputDir != null) {
            File dir = new File(jobInputDir);
            String fileNames[] = dir.list();
            for (int i=0; i<fileNames.length; i++) {
                File f = new File(dir, fileNames[i]);
                files.add(new FileInformation(fileNames[i], f.length()));
            }
        }

        logger.debug("Returning list of "+files.size()+" files.");
        return new ModelAndView("jsonView", "files", files);
    }

    /**
     * Processes a file upload request returning a JSON object which indicates
     * whether the upload was successful and contains the filename and file
     * size.
     *
     * @param request The servlet request
     * @param response The servlet response containing the JSON data
     *
     * @return null
     */
    @RequestMapping("/uploadFile.do")    
    public ModelAndView uploadFile(HttpServletRequest request,
                                   HttpServletResponse response) {

        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");

        boolean success = true;
        String error = null;
        FileInformation fileInfo = null;

        if (jobInputDir != null) {
            MultipartHttpServletRequest mfReq =
                (MultipartHttpServletRequest) request;

            MultipartFile f = mfReq.getFile("file");
            if (f == null) {
                logger.error("No file parameter provided.");
                success = false;
                error = new String("Invalid request.");
            } else {
                logger.info("Saving uploaded file "+f.getOriginalFilename());
                //TO-DO allow to upload on tables directory as well. GUI functions to be added.
                File destination = new File(
                        jobInputDir+GridSubmitController.RINEX_DIR+File.separator+f.getOriginalFilename());
                if (destination.exists()) {
                    logger.debug("Will overwrite existing file.");
                }
                try {
                    f.transferTo(destination);
                } catch (IOException e) {
                    logger.error("Could not move file: "+e.getMessage());
                    success = false;
                    error = new String("Could not process file.");
                }
                fileInfo = new FileInformation(
                        f.getOriginalFilename(), f.getSize());
            }

        } else {
            logger.error("Input directory not found in current session!");
            success = false;
            error = new String("Internal error. Please reload the page.");
        }

        // We cannot use jsonView here since this is a file upload request and
        // ExtJS uses a hidden iframe which receives the response.
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            PrintWriter pw = response.getWriter();
            pw.print("{success:'"+success+"'");
            if (error != null) {
                pw.print(",error:'"+error+"'");
            }
            if (fileInfo != null) {
                pw.print(",name:'"+fileInfo.getName()+"',size:"+fileInfo.getSize());
            }
            pw.print("}");
            pw.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * Deletes one or more uploaded files of the current job.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the files were successfully deleted.
     */
    @RequestMapping("/deleteFiles.do")    
    public ModelAndView deleteFiles(HttpServletRequest request,
                                    HttpServletResponse response) {

        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success;

        if (jobInputDir != null) {
            success = true;
            String filesPrm = request.getParameter("files");
            logger.debug("Request to delete "+filesPrm);
            String[] files = (String[]) JSONArray.toArray(
                    JSONArray.fromObject(filesPrm), String.class);

            for (String filename: files) {
                File f = new File(jobInputDir+filename);
                if (f.exists() && f.isFile()) {
                    logger.debug("Deleting "+f.getPath());
                    boolean lsuccess = f.delete();
                    if (!lsuccess) {
                        logger.warn("Unable to delete "+f.getPath());
                        success = false;
                    }
                } else {
                    logger.warn(f.getPath()+" does not exist or is not a file!");
                }
            }
        } else {
            success = false;
        }

        mav.addObject("success", success);
        return mav;
    }


    
    /**
     * Get status of the current job submission.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute that indicates the status.
     *         
     */
    @RequestMapping("/getJobStatus.do")  
    public ModelAndView getJobStatus(HttpServletRequest request,
                                    HttpServletResponse response) {

        ModelAndView mav = new ModelAndView("jsonView");
        GridTransferStatus jobStatus = (GridTransferStatus)request.getSession().getAttribute("gridStatus");
        if (jobStatus != null) {
        	mav.addObject("data", jobStatus.currentStatusMsg);
        	mav.addObject("jobStatus", jobStatus.jobSubmissionStatus);
        } else {
        	mav.addObject("data", "Grid File Transfere failed.");
        	mav.addObject("jobStatus", JobSubmissionStatus.Failed);
        }

        mav.addObject("success", true);
        return mav;
    }
    
    /**
     * Cancels the current job submission. Called to clean up temporary files.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return null
     */
    @RequestMapping("/cancelSubmission.do")    
    public ModelAndView cancelSubmission(HttpServletRequest request,
                                         HttpServletResponse response) {

        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");

        if (jobInputDir != null) {
            logger.debug("Deleting temporary job files.");
            File jobDir = new File(jobInputDir);
            Util.deleteFilesRecursive(jobDir);
            request.getSession().removeAttribute("localJobInputDir");
        }

        return null;
    }

    /**
     * Processes a job submission request.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the job was successfully submitted.
     */
    @RequestMapping("/submitJob.do")    
    public ModelAndView submitJob(HttpServletRequest request,
                                  HttpServletResponse response,
                                  GeodesyJob job) {

        logger.debug("Job details:\n"+job.toString());

        GeodesySeries series = null;
        boolean success = true;
        final String user = request.getRemoteUser();
        String jobInputDir = (String) request.getSession()
            .getAttribute("jobInputDir");
        String newSeriesName = request.getParameter("seriesName");
        String seriesIdStr = request.getParameter("seriesId");
        ModelAndView mav = new ModelAndView("jsonView");
        Object credential = request.getSession().getAttribute("userCred");

        //Used to store Job Submission status, because there will be another request checking this.
		GridTransferStatus gridStatus = new GridTransferStatus();
		
        if (credential == null) {
            //final String errorString = "Invalid grid credentials!";
            logger.error(GridSubmitController.CREDENTIAL_ERROR);
            gridStatus.currentStatusMsg = GridSubmitController.CREDENTIAL_ERROR;
            gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
            
            // Save in session for status update request for this job.
            request.getSession().setAttribute("gridStatus", gridStatus);
            //mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }

        // if seriesName parameter was provided then we create a new series
        // otherwise seriesId contains the id of the series to use.
        if (newSeriesName != null && newSeriesName != "") {
            String newSeriesDesc = request.getParameter("seriesDesc");

            logger.debug("Creating new series '"+newSeriesName+"'.");
            series = new GeodesySeries();
            series.setUser(user);
            series.setName(newSeriesName);
            if (newSeriesDesc != null) {
                series.setDescription(newSeriesDesc);
            }
            jobManager.saveSeries(series);
            // Note that we can now access the series' new ID

        } else if (seriesIdStr != null && seriesIdStr != "") {
            try {
                int seriesId = Integer.parseInt(seriesIdStr);
                series = jobManager.getSeriesById(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID!");
            }
        }

        if (series == null) {
            success = false;
            final String msg = "No valid series found. NOT submitting job!";
            logger.error(msg);
            gridStatus.currentStatusMsg = msg;
            gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;

        } else {
            String gpsFiles = (String)request.getSession().getAttribute("gridInputFiles");	
            List<String> urlsList = GeodesyUtil.getSelectedGPSFiles(gpsFiles);

            //TODO add this to stageIn array
            //List<String> localFiles = this.getLocalGPSFiles(urlsList);
            
            // Convert List<String> to String[]
    		String[] urlArray = new String[urlsList.size()];
    		urlArray = urlsList.toArray(urlArray);
    		
    		//Transfer job input files to Grid StageInURL
    		if(urlsList != null && !urlsList.isEmpty()){
        		gridStatus = urlCopy(urlArray, request);
    		}    		
    		    		
    		if(gridStatus.jobSubmissionStatus != JobSubmissionStatus.Failed){
    			
                job.setSeriesId(series.getId());
                job.setArguments(new String[] { job.getScriptFile() });

                // Add grid stage-in directory and local stage-in directory (Ryan asked for this).
                String stageInURL = gridAccess.getGridFtpServer()+jobInputDir;
                logger.debug("stagInURL: "+stageInURL);
                
                String localStageInURL = gridAccess.getLocalGridFtpServer()+
                (String) request.getSession().getAttribute("localJobInputDir");
                job.setInTransfers(new String[]{stageInURL,localStageInURL});
                
                logger.debug("localStagInURL: "+localStageInURL);
                
                // Create a new directory for the output files of this job
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String dateFmt = sdf.format(new Date());
                String jobID = user + "-" + job.getName() + "-" + dateFmt +
                    File.separator;
                String jobOutputDir = gridAccess.getGridFtpStageOutDir()+jobID;
                String submitEPR = null;
                job.setEmailAddress(user);
                job.setOutputDir(jobOutputDir);
                job.setOutTransfers(new String[]
                        { gridAccess.getGridFtpServer() + jobOutputDir });

                logger.info("Submitting job with name " + job.getName() +
                        " to " + job.getSite());
                // ACTION!
                submitEPR = gridAccess.submitJob(job, credential);

                if (submitEPR == null) {
                    success = false;
       				gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
       				gridStatus.currentStatusMsg = GridSubmitController.INTERNAL_ERROR; 
                } else {
                    logger.info("SUCCESS! EPR: "+submitEPR);
                    String status = gridAccess.retrieveJobStatus(
                            submitEPR, credential);
                    job.setReference(submitEPR);
                    job.setStatus(status);
                    job.setSubmitDate(dateFmt);
                    jobManager.saveJob(job);
                    request.getSession().removeAttribute("jobInputDir");
                    request.getSession().removeAttribute("localJobInputDir");
                    
        			//This means job submission to the grid done.
       				gridStatus.jobSubmissionStatus = JobSubmissionStatus.Done;
       				gridStatus.currentStatusMsg = GridSubmitController.TRANSFER_COMPLETE; 
                }                   			
    		}else{
    			success = false;
    			logger.error(GridSubmitController.FILE_COPY_ERROR);
                gridStatus.currentStatusMsg = GridSubmitController.FILE_COPY_ERROR;
                gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
    			mav.addObject("error", GridSubmitController.FILE_COPY_ERROR);
    		}
        }
        // Save in session for status update request for this job.
        request.getSession().setAttribute("gridStatus", gridStatus);
        mav.addObject("success", success);

        return mav;
    }

    /**
     * Creates a new Job object with predefined values for some fields.
     *
     * @param request The servlet request containing a session object
     *
     * @return The new job object.
     */
    private GeodesyJob prepareModel(HttpServletRequest request) {
        final String user = request.getRemoteUser();
        final String maxWallTime = "60"; // in minutes
        final String maxMemory = "2048"; // in MB
        final String stdInput = "";
        final String stdOutput = "stdOutput.txt";
        final String stdError = "stdError.txt";
        final String[] arguments = new String[0];
        final String[] inTransfers = new String[0];
        final String[] outTransfers = new String[0];
        String name = "GeodesyJob";
        String site = "iVEC";
        Integer cpuCount = 1;
        String version = "";
        String queue = "";
        String description = "";
        String scriptFile = "";


        // Set a default version and queue
        String[] allVersions = gridAccess.retrieveCodeVersionsAtSite(
                site, GeodesyJob.CODE_NAME);
        if (allVersions.length > 0)
            version = allVersions[0];

        String[] allQueues = gridAccess.retrieveQueueNamesAtSite(site);
        if (allQueues.length > 0)
            queue = allQueues[0];

        // Create a new directory to put all files for this job into.
        // This directory will always be the first stageIn directive.
        boolean success = createGridDir(request);
        if(!success){
        	logger.error("Setting up Grid StageIn directory failed.");
        	return null;
        }
        
        //Create local stageIn directory.
        success = createLocalDir(request);
        if(!success){
        	logger.error("Setting up local StageIn directory failed.");
        	return null;
        }
        
        String jobInputDir = (String) request.getSession().getAttribute("jobInputDir");
        

        // Check if the user requested to re-submit a previous job.
        String jobIdStr = (String) request.getSession().getAttribute("resubmitJob");
        GeodesyJob existingJob = null;
        if (jobIdStr != null) {
            request.getSession().removeAttribute("resubmitJob");
            logger.debug("Request to re-submit a job.");
            try {
                int jobId = Integer.parseInt(jobIdStr);
                existingJob = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        }

        if (existingJob != null) {
            logger.debug("Using attributes of "+existingJob.getName());
            site = existingJob.getSite();
            version = existingJob.getVersion();
            name = existingJob.getName()+"_resubmit";
            scriptFile = existingJob.getScriptFile();
            description = existingJob.getDescription();

            allQueues = gridAccess.retrieveQueueNamesAtSite(site);
            if (allQueues.length > 0)
                queue = allQueues[0];

            logger.debug("Copying files from old job to stage-in directory");
            File srcDir = new File(existingJob.getOutputDir());
            File destDir = new File(jobInputDir);
            success = Util.copyFilesRecursive(srcDir, destDir);
            if (!success) {
                logger.error("Could not copy all files!");
                // TODO: Let user know this didn't work
            }
        }

        // Check if the ScriptBuilder was used. If so, there is a file in the
        // system temp directory which needs to be staged in.
        String newScript = (String) request.getSession().getAttribute("scriptFile");
        if (newScript != null) {
            request.getSession().removeAttribute("scriptFile");
            logger.debug("Adding "+newScript+" to stage-in directory");
            File tmpScriptFile = new File(System.getProperty("java.io.tmpdir") +
                    File.separator+newScript+".py");
            File newScriptFile = new File(jobInputDir, tmpScriptFile.getName());
            success = Util.moveFile(tmpScriptFile, newScriptFile);
            if (success) {
                logger.info("Moved "+newScript+" to stageIn directory");
                scriptFile = newScript+".py";

                // Extract information from script file
                ScriptParser parser = new ScriptParser();
                try {
                    parser.parse(newScriptFile);
                    cpuCount = parser.getNumWorkerProcesses()+1;
                } catch (IOException e) {
                    logger.warn("Error parsing file: "+e.getMessage());
                }
            } else {
                logger.warn("Could not move "+newScript+" to stage-in!");
            }
        }

        logger.debug("Creating new GeodesyJob instance");
        GeodesyJob job = new GeodesyJob(site, name, version, arguments, queue,
                maxWallTime, maxMemory, cpuCount, inTransfers, outTransfers,
                user, stdInput, stdOutput, stdError);

        job.setScriptFile(scriptFile);
        job.setDescription(description);

        return job;
    }
    


	/** 
     * Create stageIn directories on portal host, so user can upload files easy.
     *
     */
	private boolean createLocalDir(HttpServletRequest request) {
		
		final String user = request.getRemoteUser();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateFmt = sdf.format(new Date());
        String jobID = user + "-" + dateFmt + File.separator;
        String jobInputDir = gridAccess.getLocalGridFtpStageInDir() + jobID;
        
        boolean success = (new File(jobInputDir)).mkdir();
        
        //create rinex directory.
        success = (new File(jobInputDir+GridSubmitController.RINEX_DIR+File.separator)).mkdir();
        if (!success) {
            logger.error("Could not create stageIn directories ");
            jobInputDir = gridAccess.getGridFtpStageInDir();
        }
        
        //tables files.
        success = Util.copyFilesRecursive(new File(GridSubmitController.PRE_STAGE_IN_TABLE_FILES),
        		                new File(jobInputDir+GridSubmitController.TABLE_DIR+File.separator));
        
        // Save in session to use it when submitting job
        request.getSession().setAttribute("localJobInputDir", jobInputDir);
        
        return success;
	}


	/** 
	 * urlCopy
	 * 
     * Copy data to the Grid Storage using URLCopy.  
     * This is method which does authentication, remote create directory 
     * and files copying
     *
     * @param fromURLs	an array of URLs to copy to the storage
     * 
     * @return          GridTransferStatus of files copied
     * 
     */
	private GridTransferStatus urlCopy(String[] fromURLs, HttpServletRequest request) {

		Object credential = request.getSession().getAttribute("userCred");		
		String jobInputDir = (String) request.getSession().getAttribute("jobInputDir");				
        GridTransferStatus status = new GridTransferStatus();
		
		if( jobInputDir != null )
		{
			for (int i = 0; i < fromURLs.length; i++) {
				// StageIn to Grid etc
				
				try {
					GlobusURL from = new GlobusURL(fromURLs[i]);
					logger.info("fromURL is: " + from.getURL());
					
					String fullFilename = new URL(fromURLs[i]).getFile();

					// Extract just the filename
					String filename = new File(fullFilename).getName();	
					status.file = filename;
					
					
					// Full URL
					// e.g. "gsiftp://pbstore.ivec.org:2811//pbstore/au01/grid-auscope/Abdi.Jama@csiro.au-20091103_163322/"
					//       +"rinex/" + "abeb0010.00d.Z"
					String toURL = gridAccess.getGridFtpServer()+File.separator+ jobInputDir 
					               +GridSubmitController.RINEX_DIR+File.separator+ filename;
					GlobusURL to = new GlobusURL(toURL);		
					logger.info("toURL is: " + to.getURL());
					
					//Not knowing how long UrlCopy will take, the UI request status update of 
					//file transfer periodically
					UrlCopy uCopy = new UrlCopy();
					uCopy.setCredentials((GSSCredential)credential);
					uCopy.setDestinationUrl(to);
					uCopy.setSourceUrl(from);
					// Disables usage of third party transfers, for grid security reasons.
					uCopy.setUseThirdPartyCopy(false); 	
					uCopy.copy();
										
					
					
					logger.info(to.getProtocol()+"://"+to.getHost()+":"+to.getPort()+"/");
					
					String gridServer = to.getProtocol() + "://" + to.getHost() + ":" + to.getPort();
					//String gridDir = fullDirName;
					String gridFullURL =  gridServer + "/"+ jobInputDir;
										
					status.numFileCopied++;
					status.currentStatusMsg = GridSubmitController.FILE_COPIED + status.numFileCopied
					+" of "+fromURLs.length+" files transfered.";
					status.gridFullURL = gridFullURL;
					status.gridServer = gridServer;
					logger.debug(status.currentStatusMsg+" : "+fromURLs[i]);
					
					// Save in session for status update request for this job.
			        request.getSession().setAttribute("gridStatus", status);
					
				} catch (UrlCopyException e) {
					logger.error("UrlCopy Error: " + e.getMessage());
					status.numFileCopied = i;
					status.currentStatusMsg = GridSubmitController.FILE_COPY_ERROR;
					status.jobSubmissionStatus = JobSubmissionStatus.Failed;
					// Save in session for status update request for this job.
			        request.getSession().setAttribute("gridStatus", status);
				} catch (Exception e) {
					logger.error("Error: " + e.getMessage());
					status.numFileCopied = i;
					status.currentStatusMsg = GridSubmitController.INTERNAL_ERROR;
					status.jobSubmissionStatus = JobSubmissionStatus.Failed;
					// Save in session for status update request for this job.
			        request.getSession().setAttribute("gridStatus", status);
				}
			}
		}
		
		return status;
	}
    

    /**
     * Create a stageIn directories on Pbstore. If any errors update status.
     * @param the request to save created directories.
     * 
     */
	private boolean createGridDir(HttpServletRequest request) {
		GridTransferStatus status = new GridTransferStatus();
        Object credential = request.getSession().getAttribute("userCred");
        boolean success = true;
        if (credential == null) {
        	status.currentStatusMsg = GridSubmitController.CREDENTIAL_ERROR;
        	return false;
        }
        
		final String user = request.getRemoteUser();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateFmt = sdf.format(new Date());
        String jobID = user + "-" + dateFmt + File.separator;
        String jobInputDir = gridAccess.getGridFtpStageInDir() + jobID;
		
		try {
			GridFTPClient gridStore = new GridFTPClient(gridAccess.getRepoHostName(), gridAccess.getRepoHostFTPPort());		
			gridStore.authenticate((GSSCredential)credential); //authenticating
			gridStore.setDataChannelAuthentication(DataChannelAuthentication.SELF);
			gridStore.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
			gridStore.makeDir(jobInputDir);
	        // Save in session to use it when submitting job
	        request.getSession().setAttribute("jobInputDir", jobInputDir);
	        
			//TO-DO create rinex and tables directories
			gridStore.makeDir(jobInputDir+GridSubmitController.RINEX_DIR+File.separator);
	        logger.debug("Created Grid Directory.");
	        gridStore.close();
			
		} catch (ServerException e) {
			logger.error("GridFTP ServerException: " + e.getMessage());
			status.currentStatusMsg = GridSubmitController.GRID_LINK;
			status.jobSubmissionStatus = JobSubmissionStatus.Failed;
			success = false;
		} catch (IOException e) {
			logger.error("GridFTP IOException: " + e.getMessage());
			status.currentStatusMsg = GridSubmitController.GRID_LINK;
			status.jobSubmissionStatus = JobSubmissionStatus.Failed;
			success = false;
		} catch (Exception e) {
			logger.error("GridFTP Exception: " + e.getMessage());
			status.currentStatusMsg = GridSubmitController.GRID_LINK;
			status.jobSubmissionStatus = JobSubmissionStatus.Failed;
			success = false;
		}
		
		// Save in session for status update request for this job.
        request.getSession().setAttribute("gridStatus", status);
        return success;
	}

	/**
	 * function that moves local GPS files at ivec to a separate list.
	 * @param list of selected GPS files
	 * @return list of local GPS files.
	 */
	private List<String> getLocalGPSFiles(List<String> list){
		List<String> ivecList = new ArrayList<String>();
		for(String fileName : list){
			if (fileName.contains(".ivec.org")){
				ivecList.add(convertFilePathToIvec(fileName));
				//The file can not be in two list
				list.remove(fileName);				
			}
		}		
		return ivecList;
	}

	/**
	 * 
	 * @param fileName file which to change it's path name
	 * @return
	 */
	private String convertFilePathToIvec(String fileName){
		//replace "http://files.ivec.org/geodesy/"  
		//with "gsiftp://pbstore.ivec.org:2811//pbstore/cg01/geodesy/ftp.ga.gov.au/gpsdata/"
		return fileName.replace(IVEC_MIRROR_URL, gridAccess.getGridFtpServer()+PBSTORE_RINEX_PATH);
	}
	
	/**
	 * Simple object to hold Grid file transfer status.
	 * @author jam19d
	 *
	 */
	class GridTransferStatus {
		
		public int numFileCopied = 0;
		public String file = "";
		public String gridFullURL = "";
		public String gridServer = "";
		public String currentStatusMsg = "";
		public JobSubmissionStatus jobSubmissionStatus = JobSubmissionStatus.Running;				
	}
	

	/**
	 * Enum to indicate over all job submission status.
	 */
	public enum JobSubmissionStatus{Running,Done,Failed }
}