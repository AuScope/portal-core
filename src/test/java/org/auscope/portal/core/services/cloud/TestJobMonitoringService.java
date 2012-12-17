package org.auscope.portal.core.services.cloud;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.monitor.JobMonitor;
import org.auscope.portal.core.services.cloud.monitor.JobMonitorListener;
import org.auscope.portal.core.test.BasicThreadExecutor;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJobMonitoringService extends PortalTestClass {

    private CloudJob mockJob1 = context.mock(CloudJob.class, "mockJob1");
    private CloudJob mockJob2 = context.mock(CloudJob.class, "mockJob2");

    private JobMonitorListener mockListener1 = context.mock(JobMonitorListener.class, "mockListener1");
    private JobMonitorListener mockListener2 = context.mock(JobMonitorListener.class, "mockListener2");

    private JobMonitor mockJobMonitor = context.mock(JobMonitor.class);

    private BasicThreadExecutor threadExecutor;
    private JobMonitoringService service;

    /**
     * Initialises service - DOESNT START IT RUNNING
     */
    @Before
    public void startTestInstance() {
        threadExecutor = new BasicThreadExecutor();
        service = new JobMonitoringService(mockJobMonitor, threadExecutor);
    }

    /**
     * Creates a couple of fake jobs, monitors their changes through a couple of event listeners
     *
     * The job status checking/event handling will be checked for handling exceptions
     * @throws Exception
     */
    @Test
    public void testStatusUpdates() throws Exception {
        final String job1Status1 = "j1s1";
        final String job1Status2 = "j1s2";
        final String job2Status1 = "j2s1";

        context.checking(new Expectations() {{
            allowing(mockJob1).getId();will(returnValue(1));
            allowing(mockJob2).getId();will(returnValue(2));

            //The first job will Return the same value a couple of times before changing
            oneOf(mockJobMonitor).getJobStatus(mockJob1);will(delayReturnValue(50, null));
            oneOf(mockJobMonitor).getJobStatus(mockJob1);will(delayReturnValue(50, job1Status1));
            oneOf(mockJobMonitor).getJobStatus(mockJob1);will(delayReturnValue(50, job1Status1));
            allowing(mockJobMonitor).getJobStatus(mockJob1);will(delayReturnValue(20, job1Status2));

            //The second job will fail on the first request but succeed on subsequents
            oneOf(mockJobMonitor).getJobStatus(mockJob2);will(throwException(new ConnectException("error")));
            allowing(mockJobMonitor).getJobStatus(mockJob2);will(delayReturnValue(20, job2Status1));

            //Events should pass to both listeners (Exceptions thrown here shouldn't affect any other event handlers)
            oneOf(mockListener1).handleStatusChanged(mockJob1, job1Status1, null);
            oneOf(mockListener1).handleStatusChanged(mockJob1, job1Status2, job1Status1);will(throwException(new Exception("error1")));
            oneOf(mockListener1).handleStatusChanged(mockJob2, job2Status1, null);
            oneOf(mockListener2).handleStatusChanged(mockJob1, job1Status1, null);will(throwException(new Exception("error2")));
            oneOf(mockListener2).handleStatusChanged(mockJob1, job1Status2, job1Status1);
            oneOf(mockListener2).handleStatusChanged(mockJob2, job2Status1, null);will(throwException(new Exception("error3")));
        }});

        //Set up our service conditions
        service.addEventListener(mockListener1);
        service.addEventListener(mockListener2);
        service.setCheckFrequency(1);
        service.startMonitoring();

        //Stagger the monitoring
        service.monitorJob(mockJob1);
        Thread.sleep(200);
        service.monitorJob(mockJob2);
        Thread.sleep(200);

        //wait for termination
        service.stopMonitoring();
        try {
            threadExecutor.getExecutorService().shutdown();
            threadExecutor.getExecutorService().awaitTermination(180, TimeUnit.SECONDS);
        } catch (Exception ex) {
            threadExecutor.getExecutorService().shutdownNow();
            Assert.fail("Exception whilst waiting for update to finish " + ex.getMessage());
        }
    }
}
