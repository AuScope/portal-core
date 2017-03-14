package org.auscope.portal.core.services.cloud;

import java.io.IOException;
import java.nio.charset.Charset;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService.InstanceStatus;
import org.auscope.portal.core.test.PortalTestClass;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.zonescoped.AvailabilityZone;
import org.jclouds.openstack.nova.v2_0.domain.zonescoped.ZoneState;
import org.jclouds.openstack.nova.v2_0.extensions.AvailabilityZoneApi;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

public class TestCloudComputeService extends PortalTestClass {

    private final ComputeService mockComputeService = context.mock(ComputeService.class);
    private final NovaTemplateOptions mockTemplateOptions = context.mock(NovaTemplateOptions.class);
    private final TemplateBuilder mockTemplateBuilder = context.mock(TemplateBuilder.class);
    private final Template mockTemplate = context.mock(Template.class);
    private final NodeMetadata mockMetadata = context.mock(NodeMetadata.class);
    private final NovaApi mockNovaApi = context.mock(NovaApi.class);
    private final AvailabilityZoneApi mockAZA = context.mock(AvailabilityZoneApi.class);
    private final AvailabilityZone mockAvailZone = context.mock(AvailabilityZone.class, "mockAvailZone");
    private final AvailabilityZone mockAvailZone2 = context.mock(AvailabilityZone.class, "mockAvailZone2");
    private final ZoneState mockZoneState = context.mock(ZoneState.class);
    private final FluentIterable<?> mockAvailZoneList = context.mock(FluentIterable.class);
    private final Predicate<NodeMetadata> mockFilter = context.mock(Predicate.class);
    private final RunNodesException mockException = context.mock(RunNodesException.class);

    private CloudComputeServiceNectar service;

    private Optional<? extends AvailabilityZoneApi> mockOptAZA = context.mock(Optional.class);

    private CloudJob job;

    @Before
    public void initJobObject() {
        job = new CloudJob(13);
        job.setComputeVmId("image-id");
        job.setComputeInstanceType("type");
        service = new CloudComputeServiceNectar(mockComputeService, mockNovaApi, mockFilter);
        service.setGroupName("group-name");
        service.setKeypair("vgl-developers");

    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     * @throws RunNodesException
     * @throws PortalServiceException
     */
    @Test
    public void testExecuteJob() throws RunNodesException, PortalServiceException {
        final String userDataString = "user-data-string";
        final String expectedInstanceId = "instance-id";

        context.checking(new Expectations() {
            {
                oneOf(mockComputeService).templateOptions();
                will(returnValue(mockTemplateOptions));
                oneOf(mockComputeService).templateBuilder();
                will(returnValue(mockTemplateBuilder));

                oneOf(mockTemplateOptions).keyPairName("vgl-developers");
                will(returnValue(mockTemplateOptions));
                oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));
                will(returnValue(mockTemplateOptions));

                oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).options(mockTemplateOptions);
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).build();
                will(returnValue(mockTemplate));

                oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
                will(returnValue(ImmutableSet.<NodeMetadata> of(mockMetadata)));

                oneOf(mockMetadata).getId();
                will(returnValue(expectedInstanceId));
            }
        });

        String actualInstanceId = service.executeJob(job, userDataString);

        Assert.assertEquals(expectedInstanceId, actualInstanceId);
    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2 when EC2 reports failure by returning 0 running instances.
     * @throws RunNodesException
     * @throws PortalServiceException
     */
    @Test(expected = PortalServiceException.class)
    public void testExecuteJobFailure() throws RunNodesException, PortalServiceException {
        final String userDataString = "user-data-string";

        context.checking(new Expectations() {
            {
                oneOf(mockComputeService).templateOptions();
                will(returnValue(mockTemplateOptions));
                oneOf(mockComputeService).templateBuilder();
                will(returnValue(mockTemplateBuilder));

                oneOf(mockTemplateOptions).keyPairName("vgl-developers");
                will(returnValue(mockTemplateOptions));
                oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));
                will(returnValue(mockTemplateOptions));

                oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).options(mockTemplateOptions);
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).build();
                will(returnValue(mockTemplate));

                oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
                will(throwException(mockException));

                allowing(mockComputeService).destroyNodesMatching(mockFilter);

                allowing(mockException).fillInStackTrace();
                will(returnValue(mockException));
                allowing(mockException).getMessage();
                will(returnValue("mock-message"));
            }
        });

        service.executeJob(job, userDataString);
    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2 when EC2 reports failure by returning 0 running instances.
     * @throws RunNodesException
     * @throws PortalServiceException
     */
    @Test(expected = PortalServiceException.class)
    public void testExecuteJobFailure_EmptyResults() throws RunNodesException, PortalServiceException {
        final String userDataString = "user-data-string";

        context.checking(new Expectations() {
            {

                oneOf(mockComputeService).templateOptions();
                will(returnValue(mockTemplateOptions));
                oneOf(mockComputeService).templateBuilder();
                will(returnValue(mockTemplateBuilder));

                oneOf(mockTemplateOptions).keyPairName("vgl-developers");
                will(returnValue(mockTemplateOptions));
                oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));
                will(returnValue(mockTemplateOptions));

                oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).options(mockTemplateOptions);
                will(returnValue(mockTemplateBuilder));
                oneOf(mockTemplateBuilder).build();
                will(returnValue(mockTemplate));

                oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
                will(returnValue(ImmutableSet.<NodeMetadata> of()));

            }
        });

        service.executeJob(job, userDataString);
    }

    /**
     * Tests that job terminate correctly calls AmazonEC2
     */
    @Test
    public void testTerminateJob() {

        job.setComputeInstanceId("running-id");

        context.checking(new Expectations() {
            {
                oneOf(mockComputeService).destroyNode(job.getComputeInstanceId());
            }
        });

        service.terminateJob(job);
    }

    @Test
    public void testGetJobStatus() throws PortalServiceException {
        job.setComputeInstanceId("i-running");

        context.checking(new Expectations() {{
            oneOf(mockComputeService).getNodeMetadata("i-running");
            will(returnValue(mockMetadata));

            allowing(mockMetadata).getStatus();
            will(returnValue(Status.PENDING));
        }});

        Assert.assertEquals(InstanceStatus.Pending, service.getJobStatus(job));
    }

    @Test(expected=PortalServiceException.class)
    public void testGetJobStatus_IOError() throws PortalServiceException  {
        job.setComputeInstanceId("i-running");

        context.checking(new Expectations() {{
            oneOf(mockComputeService).getNodeMetadata("i-running");
            will(throwException(new IOException()));
        }});

        service.getJobStatus(job);
    }

    @Test
    public void testGetJobStatus_ReturnNull() throws PortalServiceException {
        job.setComputeInstanceId("i-running");

        context.checking(new Expectations() {{
            oneOf(mockComputeService).getNodeMetadata("i-running");
            will(returnValue(null));
        }});

        Assert.assertEquals(InstanceStatus.Missing, service.getJobStatus(job));
    }
}
