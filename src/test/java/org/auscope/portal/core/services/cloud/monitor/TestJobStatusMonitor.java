package org.auscope.portal.core.services.cloud.monitor;

import java.util.Arrays;
import java.util.List;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

public class TestJobStatusMonitor extends PortalTestClass {
    private JobStatusMonitor monitor;
    private JobStatusReader mockJobStatusReader;
    private JobStatusChangeListener[] mockJobStatusChangeListeners;

    @Before
    public void init() {
        //Mock objects required for the unit tests
        mockJobStatusReader = context.mock(JobStatusReader.class);
        mockJobStatusChangeListeners = new JobStatusChangeListener[] {context.mock(JobStatusChangeListener.class)};

        //Component under test
        monitor = new JobStatusMonitor(mockJobStatusReader, mockJobStatusChangeListeners);
    }

    /**
     * Tests that the execution of VGLJobStatusMonitor task run as expected.
     * @throws JobStatusException 
     */
    @Test
    public void testExecuteInternal() throws JobStatusException {
        final CloudJob job1 = new CloudJob(1);
        job1.setStatus("s1");

        final CloudJob job2 = new CloudJob(2);
        job2.setStatus("s2");

        final List<CloudJob> jobs = Arrays.asList(job1, job2);

        final String job1NewStat = "ns1"; //change
        final String job2NewStat = job2.getStatus(); //no change

        context.checking(new Expectations() {
            {

                oneOf(mockJobStatusReader).getJobStatus(job1);
                will(returnValue(job1NewStat));
                oneOf(mockJobStatusReader).getJobStatus(job2);
                will(returnValue(job2NewStat));

                oneOf(mockJobStatusChangeListeners[0]).handleStatusChange(job1, job1NewStat, job1.getStatus());
            }
        });

        monitor.statusUpdate(jobs);
    }

    /**
     * Tests that exception caused by job status change handler won't impact the status change handling for other job(s) being processed.
     * @throws JobStatusException 
     */
    @Test(expected = JobStatusException.class)
    public void testExecuteInternal_Exception() throws JobStatusException {
        final CloudJob job1 = new CloudJob(1);
        job1.setStatus("s1");

        final CloudJob job2 = new CloudJob(2);
        job2.setStatus("s2");

        final List<CloudJob> jobs = Arrays.asList(job1, job2);

        final String job2NewStat = "ns2"; //change

        context.checking(new Expectations() {
            {

                oneOf(mockJobStatusReader).getJobStatus(job1);
                will(throwException(new Exception()));
                oneOf(mockJobStatusReader).getJobStatus(job2);
                will(returnValue(job2NewStat));

                oneOf(mockJobStatusChangeListeners[0]).handleStatusChange(job2, job2NewStat, job2.getStatus());
            }
        });

        monitor.statusUpdate(jobs);
    }
}
