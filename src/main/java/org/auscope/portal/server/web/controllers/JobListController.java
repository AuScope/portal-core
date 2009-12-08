/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.ServerException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.gridjob.Util;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;
import org.auscope.portal.server.gridjob.GeodesySeries;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.ietf.jgss.GSSCredential;
import org.globus.ftp.FileInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for the job list view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 */
@Controller
public class JobListController {

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private GridAccessController gridAccess;
    @Autowired
    private GeodesyJobManager jobManager;

    /**
     * Sets the <code>GridAccessController</code> to be used for grid
     * activities.
     *
     * @param gridAccess the GridAccessController to use
     */
     //public void setGridAccess(GridAccessController gridAccess) {
     //   this.gridAccess = gridAccess;
     //}

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
	        return new ModelAndView("joblist");
	    } else {
	        request.getSession().setAttribute(
	                "redirectAfterLogin", "/joblist.html");
	        logger.warn("Proxy not initialized. Redirecting to gridLogin.");
	        return new ModelAndView(
	                new RedirectView("/gridLogin.html", true, false, false));
	    }
    }*/

    /**
     * Triggers the retrieval of latest job files
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the job was not found in the job manager.
     */
    @RequestMapping("/retrieveJobFiles.do")
    public ModelAndView retrieveJobFiles(HttpServletRequest request,
                                         HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        GeodesyJob job = null;
        ModelAndView mav = new ModelAndView("jsonView");
        Object credential = request.getSession().getAttribute("userCred");

        if (credential == null) {
            final String errorString = "Invalid grid credentials!";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }

        if (job == null) {
            final String errorString = "The requested job was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);

        } else {
            logger.debug("jobID = " + jobIdStr);
            boolean success = false;
            String jobState = gridAccess.retrieveJobStatus(
                    job.getReference(), credential);
            if (jobState != null && jobState.equals("Active")) {
                success = gridAccess.retrieveJobResults(
                        job.getReference(), credential);
            } else {
                mav.addObject("error", "Cannot retrieve files of a job that is not running!");
            }
            logger.debug("Success = "+success);
            mav.addObject("success", success);
        }

        return mav;
    }
    /**
     * Delete the job given by its reference.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the job was not found or can not be deleted.
     */
    @RequestMapping("/deleteJob.do")
    public ModelAndView deleteJob(HttpServletRequest request,
                                HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        GeodesyJob job = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;
        Object credential = request.getSession().getAttribute("userCred");

        if (credential == null) {
            final String errorString = "Invalid grid credentials!";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }


        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }

        if (job == null) {
            final String errorString = "The requested job was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);

        } else {
            // check if current user is the owner of the job
            GeodesySeries s = jobManager.getSeriesById(job.getSeriesId());
            if (request.getRemoteUser().equals(s.getUser())) {
                logger.info("Deleting job with ID "+jobIdStr);
                jobManager.deleteJob(job);
                success = true;
            } else {
                logger.warn(request.getRemoteUser()+"'s attempt to kill "+
                        s.getUser()+"'s job denied!");
                mav.addObject("error", "You are not authorised to delete this job.");
            }
        }
        mav.addObject("success", success);

        return mav;
    }
    /**
     * delete all jobs of given series.
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the series was not found in the job manager.
     */
    @RequestMapping("/deleteSeriesJobs.do")
    public ModelAndView deleteSeriesJobs(HttpServletRequest request,
                                       HttpServletResponse response) {

        String seriesIdStr = request.getParameter("seriesId");
        List<GeodesyJob> jobs = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;
        int seriesId = -1;
        Object credential = request.getSession().getAttribute("userCred");

        if (credential == null) {
            final String errorString = "Invalid grid credentials!";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }


        if (seriesIdStr != null) {
            try {
                seriesId = Integer.parseInt(seriesIdStr);
                jobs = jobManager.getSeriesJobs(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID!");
            }
        } else {
            logger.warn("No series ID specified!");
        }

        if (jobs == null) {
            final String errorString = "The requested series was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);

        } else {
            // check if current user is the owner of the series
            GeodesySeries s = jobManager.getSeriesById(seriesId);
            if (request.getRemoteUser().equals(s.getUser())) {
                logger.info("Deleting jobs of series "+seriesIdStr);
                boolean jobsDeleted = true;
                for (GeodesyJob job : jobs) {
                    String oldStatus = job.getStatus();
                    if (oldStatus.equals("Failed") || oldStatus.equals("Done") ||
                            oldStatus.equals("Cancelled")) {
                        jobManager.deleteJob(job);
                        
                    }else{
                    	logger.debug("Skipping running job "+job.getId());
                    	if(jobsDeleted){
                    		jobsDeleted = false;
                    		mav.addObject("error", "Can not delete series, there are running jobs.");
                    	}        	
                    	continue;                  	
                    }
                }
                if(jobsDeleted){
                	logger.info("Deleting series "+seriesIdStr);
                	jobManager.deleteSeries(s);
                	logger.info("Deleted series "+seriesIdStr);
                	success = true;
                }else{
                	success = false;
                }
            } else {
                logger.warn(request.getRemoteUser()+"'s attempt to delete "+
                        s.getUser()+"'s jobs denied!");
                mav.addObject("error", "You are not authorised to delete the jobs of this series.");
            }
        }

        mav.addObject("success", success);
        return mav;
    }
    
    /**
     * Kills the job given by its reference.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the job was not found in the job manager.
     */
    @RequestMapping("/killJob.do")
    public ModelAndView killJob(HttpServletRequest request,
                                HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        GeodesyJob job = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;
        Object credential = request.getSession().getAttribute("userCred");

        if (credential == null) {
            final String errorString = "Invalid grid credentials!";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }


        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }

        if (job == null) {
            final String errorString = "The requested job was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);

        } else {
            // check if current user is the owner of the job
            GeodesySeries s = jobManager.getSeriesById(job.getSeriesId());
            if (request.getRemoteUser().equals(s.getUser())) {
                logger.info("Cancelling job with ID "+jobIdStr);
                String newState = gridAccess.killJob(
                        job.getReference(), credential);
                if (newState == null)
                    newState = "Cancelled";
                logger.debug("New job state: "+newState);

                job.setStatus(newState);
                jobManager.saveJob(job);
                success = true;
            } else {
                logger.warn(request.getRemoteUser()+"'s attempt to kill "+
                        s.getUser()+"'s job denied!");
                mav.addObject("error", "You are not authorised to cancel this job.");
            }
        }
        mav.addObject("success", success);

        return mav;
    }

    /**
     * Kills all jobs of given series.
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the series was not found in the job manager.
     */
    @RequestMapping("/killSeriesJobs.do")
    public ModelAndView killSeriesJobs(HttpServletRequest request,
                                       HttpServletResponse response) {

        String seriesIdStr = request.getParameter("seriesId");
        List<GeodesyJob> jobs = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;
        int seriesId = -1;
        Object credential = request.getSession().getAttribute("userCred");

        if (credential == null) {
            final String errorString = "Invalid grid credentials!";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }


        if (seriesIdStr != null) {
            try {
                seriesId = Integer.parseInt(seriesIdStr);
                jobs = jobManager.getSeriesJobs(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID!");
            }
        } else {
            logger.warn("No series ID specified!");
        }

        if (jobs == null) {
            final String errorString = "The requested series was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);

        } else {
            // check if current user is the owner of the series
            GeodesySeries s = jobManager.getSeriesById(seriesId);
            if (request.getRemoteUser().equals(s.getUser())) {
                logger.info("Cancelling jobs of series "+seriesIdStr);
                for (GeodesyJob job : jobs) {
                    String oldStatus = job.getStatus();
                    if (oldStatus.equals("Failed") || oldStatus.equals("Done") ||
                            oldStatus.equals("Cancelled")) {
                        logger.debug("Skipping finished job "+job.getId());
                        continue;
                    }
                    logger.info("Killing job with ID "+job.getId());
                    String newState = gridAccess.killJob(
                            job.getReference(), credential);
                    if (newState == null)
                        newState = "Cancelled";
                    logger.debug("New job state: "+newState);

                    job.setStatus(newState);
                    jobManager.saveJob(job);
                }
                success = true;
            } else {
                logger.warn(request.getRemoteUser()+"'s attempt to kill "+
                        s.getUser()+"'s jobs denied!");
                mav.addObject("error", "You are not authorised to cancel the jobs of this series.");
            }
        }

        mav.addObject("success", success);
        return mav;
    }

    /**
     * Returns a JSON object containing an array of files belonging to a
     * given job.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a files attribute which is an array of
     *         FileInformation objects. If the job was not found in the job
     *         manager the JSON object will contain an error attribute
     *         indicating the error.
     */
    @RequestMapping("/jobFiles.do")
    public ModelAndView jobFiles(HttpServletRequest request,
                                 HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        GeodesyJob job = null;
        ModelAndView mav = new ModelAndView("jsonView");
        Object credential = request.getSession().getAttribute("userCred");

        if (credential == null) {
        	logger.error("Error invalid credential.");
        }
        
        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }
        GridFTPClient gridStore = null;
        if (job == null) {
            final String errorString = "The requested job was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);

        } else {
    		try {
    			gridStore = new GridFTPClient(gridAccess.getRepoHostName(), gridAccess.getRepoHostFTPPort());		
    			gridStore.authenticate((GSSCredential)credential); //authenticating
    			gridStore.setDataChannelAuthentication(DataChannelAuthentication.SELF);
    			gridStore.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
    			logger.debug("Change to Grid StageOut dir:"+job.getOutputDir());
    			gridStore.changeDir(job.getOutputDir());
    			logger.debug("List files in StageOut dir:"+gridStore.getCurrentDir());
    			gridStore.setType(GridFTPSession.TYPE_ASCII);
    			gridStore.setPassive();
    			gridStore.setLocalActive();
    			

    			//gridStore.setTCPBufferSize(32*1024);
    			Vector list = gridStore.list("*");
    			FileInformation[] fileDetails = null;
    			if (list != null && !(list.isEmpty())) {
        			fileDetails = new FileInformation[list.size()];
        			for (int i = list.size() - 1; i >= 0; i--) {
        				FileInfo fInfo = (FileInfo) list.get(i);
                        fileDetails[i] = new FileInformation(
                        		fInfo.getName(), fInfo.getSize());
        			}                    
    			} else{
                    // Files not staged out (yet)
                    fileDetails = new FileInformation[0];
    			}
    			mav.addObject("files", fileDetails);
    		} catch (ServerException e) {
    			logger.error("GridFTP ServerException: " + e.getMessage());
    		} catch (IOException e) {
    			logger.error("GridFTP IOException: " + e.getMessage());
    		} catch (Exception e) {
    			logger.error("GridFTP Exception: " + e.getMessage());
    		}
    		finally{
    			try{
    				if(gridStore != null)
    					gridStore.close();
    			}catch (Exception e) {
        			logger.error("GridFTP Exception: " + e.getMessage());
        		}
    		}
        }
    	return mav;
    }

    /**
     * Sends the contents of a job file to the client.
     *
     * @param request The servlet request including a jobId parameter and a
     *                filename parameter
     * @param response The servlet response receiving the data
     *
     * @return null on success or the joblist view with an error parameter on
     *         failure.
     */
    @RequestMapping("/downloadFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
                                     HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        String fileName = request.getParameter("filename");
        GeodesyJob job = null;
        String errorString = null;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        }

        if (job != null && fileName != null) {
            logger.debug("Download "+fileName+" of job with ID "+jobIdStr+".");
            File f = new File(job.getOutputDir()+File.separator+fileName);
            if (!f.canRead()) {
                logger.error("File "+f.getPath()+" not readable!");
                errorString = new String("File could not be read.");
            } else {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\""+fileName+"\"");

                try {
                    byte[] buffer = new byte[16384];
                    int count = 0;
                    OutputStream out = response.getOutputStream();
                    FileInputStream fin = new FileInputStream(f);
                    while ((count = fin.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                    out.flush();
                    return null;

                } catch (IOException e) {
                    errorString = new String("Could not send file: " +
                            e.getMessage());
                    logger.error(errorString);
                }
            }
        }

        // We only end up here in case of an error so return a suitable message
        if (errorString == null) {
            if (job == null) {
                errorString = new String("Invalid job specified!");
                logger.error(errorString);
            } else if (fileName == null) {
                errorString = new String("No filename provided!");
                logger.error(errorString);
            } else {
                // should never get here
                errorString = new String("Something went wrong.");
                logger.error(errorString);
            }
        }
        return new ModelAndView("joblist", "error", errorString);
    }

    /**
     * Sends the contents of one or more job files as a ZIP archive to the
     * client.
     *
     * @param request The servlet request including a jobId parameter and a
     *                files parameter with the filenames separated by comma
     * @param response The servlet response receiving the data
     *
     * @return null on success or the joblist view with an error parameter on
     *         failure.
     */
    @RequestMapping("/downloadAsZip.do")
    public ModelAndView downloadAsZip(HttpServletRequest request,
                                      HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        String filesParam = request.getParameter("files");
        GeodesyJob job = null;
        String errorString = null;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        }

        if (job != null && filesParam != null) {
            String[] fileNames = filesParam.split(",");
            logger.debug("Archiving " + fileNames.length + " file(s) of job " +
                    jobIdStr);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"jobfiles.zip\"");

            try {
                boolean readOneOrMoreFiles = false;
                ZipOutputStream zout = new ZipOutputStream(
                        response.getOutputStream());
                for (String fileName : fileNames) {
                    File f = new File(job.getOutputDir()+File.separator+fileName);
                    if (!f.canRead()) {
                        // if a file could not be read we go ahead and try the
                        // next one.
                        logger.error("File "+f.getPath()+" not readable!");
                    } else {
                        byte[] buffer = new byte[16384];
                        int count = 0;
                        zout.putNextEntry(new ZipEntry(fileName));
                        FileInputStream fin = new FileInputStream(f);
                        while ((count = fin.read(buffer)) != -1) {
                            zout.write(buffer, 0, count);
                        }
                        zout.closeEntry();
                        readOneOrMoreFiles = true;
                    }
                }
                if (readOneOrMoreFiles) {
                    zout.finish();
                    zout.flush();
                    zout.close();
                    return null;

                } else {
                    zout.close();
                    errorString = new String("Could not access the files!");
                    logger.error(errorString);
                }

            } catch (IOException e) {
                errorString = new String("Could not create ZIP file: " +
                        e.getMessage());
                logger.error(errorString);
            }
        }

        // We only end up here in case of an error so return a suitable message
        if (errorString == null) {
            if (job == null) {
                errorString = new String("Invalid job specified!");
                logger.error(errorString);
            } else if (filesParam == null) {
                errorString = new String("No filename(s) provided!");
                logger.error(errorString);
            } else {
                // should never get here
                errorString = new String("Something went wrong.");
                logger.error(errorString);
            }
        }
        return new ModelAndView("joblist", "error", errorString);
    }

    /**
     * Returns a JSON object containing an array of series that match the query
     * parameters.
     *
     * @param request The servlet request with query parameters
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         GeodesySeries objects matching the criteria.
     */
    @RequestMapping("/querySeries.do")
    public ModelAndView querySeries(HttpServletRequest request,
                                    HttpServletResponse response) {

        String qUser = request.getParameter("qUser");
        String qName = request.getParameter("qSeriesName");
        String qDesc = request.getParameter("qSeriesDesc");

        if (qUser == null && qName == null && qDesc == null) {
            qUser = request.getRemoteUser();
            logger.debug("No query parameters provided. Will return "+qUser+"'s series.");
        }

        logger.debug("qUser="+qUser+", qName="+qName+", qDesc="+qDesc);
        List<GeodesySeries> series = jobManager.querySeries(qUser, qName, qDesc);

        logger.debug("Returning list of "+series.size()+" series.");
        return new ModelAndView("jsonView", "series", series);
    }

    /**
     * Returns a JSON object containing an array of jobs for the given series.
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a jobs attribute which is an array of
     *         <code>GeodesyJob</code> objects.
     */
    @RequestMapping("/listJobs.do")
    public ModelAndView listJobs(HttpServletRequest request,
                                 HttpServletResponse response) {

        String seriesIdStr = request.getParameter("seriesId");
        List<GeodesyJob> seriesJobs = null;
        ModelAndView mav = new ModelAndView("jsonView");
        Object credential = request.getSession().getAttribute("userCred");
        int seriesId = -1;

        if (credential == null) {
            final String errorString = "Invalid grid credentials!";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }

        if (seriesIdStr != null) {
            try {
                seriesId = Integer.parseInt(seriesIdStr);
                seriesJobs = jobManager.getSeriesJobs(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID '"+seriesIdStr+"'");
            }
        } else {
            logger.warn("No series ID specified!");
        }

        if (seriesJobs != null) {
            // check if current user is the owner of the series and update
            // the status of the jobs if so
            GeodesySeries s = jobManager.getSeriesById(seriesId);
            if (request.getRemoteUser().equals(s.getUser())) {
                logger.debug("Updating status of jobs attached to series " +
                        seriesIdStr + ".");
                for (GeodesyJob j : seriesJobs) {
                    String state = j.getStatus();
                    if (!state.equals("Done") && !state.equals("Failed") &&
                            !state.equals("Cancelled")) {
                        String newState = gridAccess.retrieveJobStatus(
                                j.getReference(), credential);
                        if (newState != null && !state.equals(newState)) {
                            j.setStatus(newState);
                            jobManager.saveJob(j);
                        }
                        // TODO: job might have finished but status cannot be
                        // retrieved anymore -> a good heuristics is to check
                        // if the job files have been staged out and assume
                        // success if that is the case.
                    }
                }
            }
            mav.addObject("jobs", seriesJobs);
        }

        logger.debug("Returning series job list");
        return mav;
    }

    /**
     * Re-submits a single job.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return The scriptbuilder view prepared to resubmit the job or the
     *         joblist view with an error parameter if the job was not found.
     */
    @RequestMapping("/resubmitJob.do")
    public ModelAndView resubmitJob(HttpServletRequest request,
                                    HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        GeodesyJob job = null;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }

        if (job == null) {
            final String errorString = "Could not retrieve job details!";
            logger.error(errorString);
            return new ModelAndView("joblist", "error", errorString);
        }

        logger.info("Re-submitting job " + jobIdStr + ".");
        request.getSession().setAttribute("resubmitJob", jobIdStr);
        return useScript(request, response);
    }

    /**
     * Allows the user to edit a copy of an input script from a previous job
     * and use it for a new job.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return The scriptbuilder model and view for editing the script or
     *         the joblist model and view with an error parameter if the job
     *         or file was not found.
     */
    @RequestMapping("/useScript.do")
    public ModelAndView useScript(HttpServletRequest request,
                                  HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");

        GeodesyJob job = null;
        String errorString = null;
        String scriptFileName = null;
        File sourceFile = null;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }

        if (job == null) {
            errorString = new String("Could not access the job!");
            logger.error(errorString);
        } else {
            scriptFileName = job.getScriptFile();
            sourceFile = new File(
                    job.getOutputDir()+File.separator+scriptFileName);
            if (!sourceFile.canRead()) {
                errorString = new String("Script file could not be read.");
                logger.error("File "+sourceFile.getPath()+" not readable!");
            }
        }

        if (errorString == null) {
            logger.debug("Copying script file of job " + jobIdStr + " to temp.");
            String tempDir = System.getProperty("java.io.tmpdir");
            File tempScript = new File(tempDir+File.separator+scriptFileName);
            boolean success = Util.copyFile(sourceFile, tempScript);
            if (success) {
                tempScript.deleteOnExit();
            } else {
                errorString = new String("Script file could not be read.");
                logger.error(errorString);
            }
        }

        if (errorString != null) {
            request.getSession().removeAttribute("resubmitJob");
            return new ModelAndView("joblist", "error", errorString);
        }

        request.getSession().setAttribute("scriptFile", scriptFileName);
        return new ModelAndView(
                new RedirectView("/scriptbuilder.html", true, false, false));
    }
}

