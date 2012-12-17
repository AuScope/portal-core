package org.auscope.portal.core.services.cloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.monitor.JobMonitor;
import org.auscope.portal.core.services.cloud.monitor.JobMonitorListener;
import org.auscope.portal.core.services.cloud.monitor.JobStatus;

import com.google.common.collect.Lists;

/**
 * A class for monitoring the status of jobs. This class will NOT write status updates to the monitored jobs
 * @author Josh Vote
 *
 */
public class JobMonitoringService {
    private final Log log = LogFactory.getLog(getClass());

    /** Used for examining jobs for a given status. Will only be accessed by a SINGLE worker thread*/
    private JobMonitor jobMonitor;
    /** Used to schedule the monitoring process*/
    private Executor executor;
    /** Used for keeping track of which jobs are being monitored by this class. The status will reference the status as of the LAST check*/
    private Map<Integer, JobStatus> monitoredJobs;
    /** The frequency (in ms) that this service will check for updates. This frequency is seperate from the time taken to actually test job statuses*/
    private long checkFrequency;
    /** The running monitor thread. null if no monitoring job is currently running */
    private MonitorThread monitorThread;
    /** All subscribed event listeners*/
    private List<JobMonitorListener> listeners;

    /**
     * Creates a new instance of this class
     * @param jobMonitor Used for examining jobs for a given status. Will only be accessed by a SINGLE worker thread.
     * @param executor Used to schedule the monitoring process
     */
    public JobMonitoringService(JobMonitor jobMonitor, Executor executor) {
        super();
        this.jobMonitor = jobMonitor;
        this.executor = executor;
        this.monitoredJobs = new HashMap<Integer, JobStatus>();
        this.listeners = new ArrayList<JobMonitorListener>();
    }

    /**
     * The frequency (in ms) that this service will check for updates. This frequency is seperate from the time taken to actually test job statuses
     * @return
     */
    public long getCheckFrequency() {
        return checkFrequency;
    }

    /**
     * The frequency (in ms) that this service will check for updates. This frequency is seperate from the time taken to actually test job statuses
     * @param checkFrequency
     */
    public void setCheckFrequency(long checkFrequency) {
        this.checkFrequency = checkFrequency;
    }

    /**
     * Adds this job to the internal monitoring watch list. The job will be polled at some point in the future.
     *
     * When the job status changes the actual job's status parameter will NOT be written to. It is up to
     * event listeners to manage updating that information
     * @param job
     */
    public void monitorJob(CloudJob job) {
        log.trace("About to monitor job: " + job.toString());

        synchronized(monitoredJobs) {
            //We load with an empty status to ensure a notification occurs when its first checked
            monitoredJobs.put(job.getId(), new JobStatus(job, null));
        }
    }

    /**
     * Removes this job from the internal monitoring watch list. It's possible that a check of this job is already in place
     * and as such, the status may update for this job AFTER this function is called
     * @param job
     */
    public void unmonitorJob(CloudJob job) {
        log.trace("About to unmonitor job: " + job.toString());

        synchronized(monitoredJobs) {
            monitoredJobs.remove(job.getId());
        }
    }

    /**
     * Adds the specified listener to the list of listeners to be notified of jobs changing status.
     *
     * This function may block if an event is in the process of being fired
     * @param l
     */
    public synchronized void addEventListener(JobMonitorListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    /**
     * Removes the first occurrence of listener l from this monitoring service. Returns true if a listener is removed,
     * false if listener DNE
     *
     * This function may block if an event is in the process of being fired
     * @param l
     * @return
     */
    public synchronized boolean removeEventListener(JobMonitorListener l) {
        synchronized(listeners) {
            return listeners.remove(l);
        }
    }

    /**
     * Starts the internal monitoring thread so that every monitored job will be continuously polled on a seperate thread
     */
    public void startMonitoring() {
        //If it's already running - let it keep running
        if (monitorThread != null) {
            return;
        }

        monitorThread = new MonitorThread(jobMonitor, monitoredJobs, listeners);
        executor.execute(monitorThread);
    }

    /**
     * Stops the internal monitoring thread. There will likely be a delay and jobs will
     * continue to be updated until the shutdown finishes cleanly
     */
    public void stopMonitoring() {
        if (monitorThread == null) {
            return;
        }

        monitorThread.requestTermination();
        monitorThread = null;
    }

    /**
     * Worker thread for polling jobs every N seconds
     * @author Josh Vote
     *
     */
    private class MonitorThread implements Runnable {
        private final Log log = LogFactory.getLog(getClass());

        private JobMonitor jobMonitor;
        private Map<Integer, JobStatus> monitoredJobs;
        private List<JobMonitorListener> listeners;
        private long checkFrequency;
        private boolean terminateRequested;


        public MonitorThread(JobMonitor jobMonitor,
                Map<Integer, JobStatus> monitoredJobs,
                List<JobMonitorListener> listeners) {
            this.jobMonitor = jobMonitor;
            this.monitoredJobs = monitoredJobs;
            this.terminateRequested = false;
            this.listeners = listeners;
        }

        public synchronized void requestTermination() {
            this.terminateRequested = true;
            log.debug("terminate requested");
        }

        public synchronized boolean isTerminateRequested() {
            return this.terminateRequested;
        }

        public synchronized long getCheckFrequency() {
            return checkFrequency;
        }

        public synchronized void setCheckFrequency(long checkFrequency) {
            this.checkFrequency = checkFrequency;
        }

        /**
         * This will lock the listeners object - don't call it while holding
         * other locks or you risk deadlock
         * @param job
         * @param oldStatus
         */
        public void fireStatusChanged(CloudJob job, String newStatus, String oldStatus) {
            synchronized(listeners) {
                for (JobMonitorListener l : listeners) {
                    try {
                        l.handleStatusChanged(job, newStatus, oldStatus);
                    } catch (Exception ex) {
                        //do nothing - if the event handler fails, it's not our problem
                        log.debug("Event listener has thrown exception: " + ex.getMessage());
                    }
                }
            }
        }

        @Override
        public void run() {
            log.info("worker thread beginning...");

            while (!isTerminateRequested()) {
                //We don't want to lock monitoredJobs while we poll EVERY job
                //Any other threads wanting to append jobs to the parent service
                //would end up being blocked for a long time
                List<Integer> jobIdsToPoll = new ArrayList<Integer>();
                synchronized(monitoredJobs) {
                    jobIdsToPoll = Lists.newArrayList(monitoredJobs.keySet());
                }

                //This will add a chunk of locking overhead but should prevent the main app from hanging
                //when appending jobs to the service. We just need to account for the fact that the job map
                //may change between iterations.
                for (Integer jobId : jobIdsToPoll) {
                    JobStatus jobStatus = null;
                    synchronized(monitoredJobs) {
                        jobStatus = monitoredJobs.get(jobId);
                    }
                    if (jobStatus == null) {
                        continue;
                    }

                    //This will work fine for a single monitoring thread - if this ever scales
                    //beyond a single worker thread you will need to sort out locking on the
                    //on the JobStatus (amongst other things...)
                    CloudJob job = jobStatus.getCloudJob();
                    String newStatus = null;
                    try {
                        //Ask the jobmonitor for info about the current job status
                        newStatus = jobMonitor.getJobStatus(job);
                    } catch (Exception ex) {
                        log.warn(String.format("Unable to fetch status for job %1$s: %2$s", job, ex));
                    }
                    String oldStatus = jobStatus.getStatus();
                    if (newStatus != null && !newStatus.equals(oldStatus)) {
                        jobStatus.setStatus(newStatus);
                        fireStatusChanged(job, newStatus, oldStatus);
                    }
                }

                //Sleep between checks
                try {
                    Thread.sleep(getCheckFrequency());
                } catch (InterruptedException e) { } //do nothing
            }

            log.info("worker thread terminating normally...");
        }
    }
}
