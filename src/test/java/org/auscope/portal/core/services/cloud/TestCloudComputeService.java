package org.auscope.portal.core.services.cloud;

import java.nio.charset.Charset;
import java.util.Arrays;

import junit.framework.Assert;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService.ProviderType;
import org.auscope.portal.core.test.PortalTestClass;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.zonescoped.AvailabilityZone;
import org.jclouds.openstack.nova.v2_0.domain.zonescoped.ZoneState;
import org.jclouds.openstack.nova.v2_0.extensions.AvailabilityZoneApi;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
    private final FluentIterable mockAvailZoneList = context.mock(FluentIterable.class);
    private final Predicate<NodeMetadata> mockFilter = context.mock(Predicate.class);
    private final RunNodesException mockException = context.mock(RunNodesException.class);

    private CloudComputeService service;


    private Optional<? extends AvailabilityZoneApi> mockOptAZA = context.mock(Optional.class);


    private CloudJob job;

    @Before
    public void initJobObject() {
        job = new CloudJob(13);
        job.setComputeVmId("image-id");
        job.setComputeInstanceType("type");
        service = new CloudComputeService(ProviderType.NovaKeystone, mockComputeService, mockNovaApi, mockFilter);
        service.setGroupName("group-name");
        service.setKeypair("vgl-developers");

    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     */
    @Test
    public void testExecuteJob() throws Exception {
        final String userDataString = "user-data-string";
        final String expectedInstanceId = "instance-id";

        context.checking(new Expectations() {{
            oneOf(mockNovaApi).getConfiguredZones();will(returnValue(Sets.newHashSet("here")));

            oneOf(mockNovaApi).getAvailabilityZoneApi("here");will(returnValue(mockOptAZA));
            oneOf(mockOptAZA).get();will(returnValue(mockAZA));
            oneOf(mockAZA).list();will(returnValue(mockAvailZoneList));
            oneOf(mockAvailZoneList).iterator();will(returnValue(Arrays.asList(mockAvailZone).iterator()));

            allowing(mockAvailZone).getName();will(returnValue("my-zone"));
            allowing(mockAvailZone).getState();will(returnValue(mockZoneState));
            allowing(mockZoneState).available();will(returnValue(true));

            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));

            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).availabilityZone("my-zone");will(returnValue(mockTemplateOptions));

            oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).options(mockTemplateOptions);will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).build();will(returnValue(mockTemplate));

            oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
            will(returnValue(ImmutableSet.<NodeMetadata>of(mockMetadata)));

            oneOf(mockMetadata).getId();will(returnValue(expectedInstanceId));
        }});

        String actualInstanceId = service.executeJob(job, userDataString);

        Assert.assertEquals(expectedInstanceId, actualInstanceId);
    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     * when EC2 reports failure by returning 0 running instances.
     */
    @Test(expected=PortalServiceException.class)
    public void testExecuteJobFailure() throws Exception {
        final String userDataString = "user-data-string";

        context.checking(new Expectations() {{
            oneOf(mockNovaApi).getConfiguredZones();will(returnValue(Sets.newHashSet("here")));

            oneOf(mockNovaApi).getAvailabilityZoneApi("here");will(returnValue(mockOptAZA));
            oneOf(mockOptAZA).get();will(returnValue(mockAZA));
            oneOf(mockAZA).list();will(returnValue(mockAvailZoneList));
            oneOf(mockAvailZoneList).iterator();will(returnValue(Arrays.asList(mockAvailZone).iterator()));

            allowing(mockAvailZone).getName();will(returnValue("my-zone"));
            allowing(mockAvailZone).getState();will(returnValue(mockZoneState));
            allowing(mockZoneState).available();will(returnValue(true));

            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));

            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).availabilityZone("my-zone");will(returnValue(mockTemplateOptions));


            oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).options(mockTemplateOptions);will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).build();will(returnValue(mockTemplate));

            oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
            will(throwException(mockException));

            allowing(mockComputeService).destroyNodesMatching(mockFilter);

            allowing(mockException).fillInStackTrace();will(returnValue(mockException));
            allowing(mockException).getMessage();will(returnValue("mock-message"));
        }});

        service.executeJob(job, userDataString);
    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     * when EC2 reports failure by returning 0 running instances.
     */
    @Test(expected=PortalServiceException.class)
    public void testExecuteJobFailure_EmptyResults() throws Exception {
        final String userDataString = "user-data-string";

        context.checking(new Expectations() {{

            oneOf(mockNovaApi).getConfiguredZones();will(returnValue(Sets.newHashSet("here")));

            oneOf(mockNovaApi).getAvailabilityZoneApi("here");will(returnValue(mockOptAZA));
            oneOf(mockOptAZA).get();will(returnValue(mockAZA));
            oneOf(mockAZA).list();will(returnValue(mockAvailZoneList));
            oneOf(mockAvailZoneList).iterator();will(returnValue(Arrays.asList(mockAvailZone).iterator()));

            allowing(mockAvailZone).getName();will(returnValue("my-zone"));
            allowing(mockAvailZone).getState();will(returnValue(mockZoneState));
            allowing(mockZoneState).available();will(returnValue(true));

            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));

            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).availabilityZone("my-zone");will(returnValue(mockTemplateOptions));

            oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).options(mockTemplateOptions);will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).build();will(returnValue(mockTemplate));

            oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
            will(returnValue(ImmutableSet.<NodeMetadata>of()));

        }});

        service.executeJob(job, userDataString);
    }

    /**
     * Tests that job terminate correctly calls AmazonEC2
     */
    @Test
    public void testTerminateJob() {

        job.setComputeInstanceId("running-id");

        context.checking(new Expectations() {{
            oneOf(mockComputeService).destroyNode(job.getComputeInstanceId());
        }});

        service.terminateJob(job);
    }

    /**
     * Tests that specified zones are skipped correctly
     */
    @Test
    public void testSkippedZones() throws Exception {
        final String userDataString = "user-data-string";
        final String expectedInstanceId = "instance-id";

        service.setSkippedZones(Sets.newHashSet("my-skip-zone"));

        context.checking(new Expectations() {{
            oneOf(mockNovaApi).getConfiguredZones();will(returnValue(Sets.newHashSet("here")));

            oneOf(mockNovaApi).getAvailabilityZoneApi("here");will(returnValue(mockOptAZA));
            oneOf(mockOptAZA).get();will(returnValue(mockAZA));
            oneOf(mockAZA).list();will(returnValue(mockAvailZoneList));
            oneOf(mockAvailZoneList).iterator();will(returnValue(Arrays.asList(mockAvailZone, mockAvailZone2).iterator()));

            allowing(mockAvailZone).getName();will(returnValue("my-skip-zone"));

            allowing(mockAvailZone2).getName();will(returnValue("my-notskip-zone"));
            allowing(mockAvailZone2).getState();will(returnValue(mockZoneState));

            allowing(mockZoneState).available();will(returnValue(true));

            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));

            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).availabilityZone("my-notskip-zone");will(returnValue(mockTemplateOptions));

            oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).options(mockTemplateOptions);will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).build();will(returnValue(mockTemplate));

            oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
            will(returnValue(ImmutableSet.<NodeMetadata>of(mockMetadata)));

            oneOf(mockMetadata).getId();will(returnValue(expectedInstanceId));
        }});

        String actualInstanceId = service.executeJob(job, userDataString);

        Assert.assertEquals(expectedInstanceId, actualInstanceId);
    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2 despite the first zone failing
     */
    @Test
    public void testExecuteJobBadZone() throws Exception {
        final String userDataString = "user-data-string";
        final String expectedInstanceId = "instance-id";

        context.checking(new Expectations() {{
            oneOf(mockNovaApi).getConfiguredZones();will(returnValue(Sets.newHashSet("here")));

            oneOf(mockNovaApi).getAvailabilityZoneApi("here");will(returnValue(mockOptAZA));
            oneOf(mockOptAZA).get();will(returnValue(mockAZA));
            oneOf(mockAZA).list();will(returnValue(mockAvailZoneList));
            oneOf(mockAvailZoneList).iterator();will(returnValue(Arrays.asList(mockAvailZone, mockAvailZone2).iterator()));

            allowing(mockAvailZone).getName();will(returnValue("my-zone"));
            allowing(mockAvailZone).getState();will(returnValue(mockZoneState));
            allowing(mockAvailZone2).getName();will(returnValue("my-zone-2"));
            allowing(mockAvailZone2).getState();will(returnValue(mockZoneState));
            allowing(mockZoneState).available();will(returnValue(true));

            //Our first run through will throw an exception
            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));

            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).availabilityZone("my-zone");will(returnValue(mockTemplateOptions));

            oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).options(mockTemplateOptions);will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).build();will(returnValue(mockTemplate));


            allowing(mockException).fillInStackTrace();
            allowing(mockException).getMessage();
            oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
            will(throwException(mockException));

            oneOf(mockComputeService).destroyNodesMatching(mockFilter);will(returnValue(Sets.newHashSet()));

            //The second will succeed
            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));

            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).availabilityZone("my-zone-2");will(returnValue(mockTemplateOptions));

            oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).options(mockTemplateOptions);will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).build();will(returnValue(mockTemplate));

            oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
            will(returnValue(ImmutableSet.<NodeMetadata>of(mockMetadata)));

            oneOf(mockMetadata).getId();will(returnValue(expectedInstanceId));
        }});

        String actualInstanceId = service.executeJob(job, userDataString);

        Assert.assertEquals(expectedInstanceId, actualInstanceId);
    }
}
