package org.auscope.portal.core.services.cloud;

import java.util.Arrays;

import junit.framework.Assert;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService.InstanceStatus;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceState;

public class TestCloudComputeServiceAws extends PortalTestClass{

    private class TestableJob extends CloudJob {

    }

    private class TestableCCS extends CloudComputeServiceAws {

        private AmazonEC2Client testableClient;

        public TestableCCS(AmazonEC2Client client) {
            super("", "");
            testableClient = client;
        }

        protected AmazonEC2 getEc2Client(CloudJob job) throws PortalServiceException {
            return testableClient;
        }
    }

    private AmazonEC2Client mockClient = context.mock(AmazonEC2Client.class);
    private DescribeInstanceStatusResult mockDescribeResult = context.mock(DescribeInstanceStatusResult.class);
    private com.amazonaws.services.ec2.model.InstanceStatus mockStatus = context.mock(com.amazonaws.services.ec2.model.InstanceStatus.class);
    private InstanceState mockState = context.mock(InstanceState.class);
    private CloudComputeServiceAws service;

    @Before
    public void setup() {
        service = new TestableCCS(mockClient);
    }

    @Test
    public void testJobStatus_ParseRunning() throws Exception {
        CloudJob job = new TestableJob();

        job.setComputeInstanceId("testable-id");
        job.setProperty(CloudJob.PROPERTY_STS_ARN, "sts-arn");
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, "client-secret");

        context.checking(new Expectations() {{
            oneOf(mockClient).describeInstanceStatus(with(any(DescribeInstanceStatusRequest.class)));
            will(returnValue(mockDescribeResult));

            allowing(mockDescribeResult).getInstanceStatuses();
            will(returnValue(Arrays.asList(mockStatus)));

            allowing(mockStatus).getInstanceState();
            will(returnValue(mockState));

            allowing(mockState).getName();
            will(returnValue("running"));
        }});

        Assert.assertEquals(InstanceStatus.Running, service.getJobStatus(job));
    }

    @Test
    public void testJobStatus_ParsePending() throws Exception {
        CloudJob job = new TestableJob();

        job.setComputeInstanceId("testable-id");
        job.setProperty(CloudJob.PROPERTY_STS_ARN, "sts-arn");
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, "client-secret");

        context.checking(new Expectations() {{
            oneOf(mockClient).describeInstanceStatus(with(any(DescribeInstanceStatusRequest.class)));
            will(returnValue(mockDescribeResult));

            allowing(mockDescribeResult).getInstanceStatuses();
            will(returnValue(Arrays.asList(mockStatus)));

            allowing(mockStatus).getInstanceState();
            will(returnValue(mockState));

            allowing(mockState).getName();
            will(returnValue("pending"));
        }});

        Assert.assertEquals(InstanceStatus.Pending, service.getJobStatus(job));
    }

    @Test
    public void testJobStatus_ParseTerminated() throws Exception {
        CloudJob job = new TestableJob();

        job.setComputeInstanceId("testable-id");
        job.setProperty(CloudJob.PROPERTY_STS_ARN, "sts-arn");
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, "client-secret");

        context.checking(new Expectations() {{
            oneOf(mockClient).describeInstanceStatus(with(any(DescribeInstanceStatusRequest.class)));
            will(returnValue(mockDescribeResult));

            allowing(mockDescribeResult).getInstanceStatuses();
            will(returnValue(Arrays.asList(mockStatus)));

            allowing(mockStatus).getInstanceState();
            will(returnValue(mockState));

            allowing(mockState).getName();
            will(returnValue("terminated"));
        }});

        Assert.assertEquals(InstanceStatus.Missing, service.getJobStatus(job));
    }

    @Test
    public void testJobStatus_ParseMissingException() throws Exception {
        CloudJob job = new TestableJob();

        job.setComputeInstanceId("testable-id");
        job.setProperty(CloudJob.PROPERTY_STS_ARN, "sts-arn");
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, "client-secret");

        final AmazonServiceException ex = new AmazonServiceException("Testing Exception");
        ex.setErrorCode("InvalidInstanceID.NotFound");

        context.checking(new Expectations() {{
            oneOf(mockClient).describeInstanceStatus(with(any(DescribeInstanceStatusRequest.class)));
            will(throwException(ex));
        }});

        Assert.assertEquals(InstanceStatus.Missing, service.getJobStatus(job));
    }

    @Test(expected=PortalServiceException.class)
    public void testJobStatus_BadResponse() throws Exception {
        CloudJob job = new TestableJob();

        job.setComputeInstanceId("testable-id");
        job.setProperty(CloudJob.PROPERTY_STS_ARN, "sts-arn");
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, "client-secret");

        final AmazonServiceException ex = new AmazonServiceException("Testing Exception");
        ex.setErrorCode("unrecognized-ID");
        ex.setStatusCode(503);

        context.checking(new Expectations() {{
            oneOf(mockClient).describeInstanceStatus(with(any(DescribeInstanceStatusRequest.class)));
            will(throwException(ex));
        }});

        service.getJobStatus(job);
    }
}
