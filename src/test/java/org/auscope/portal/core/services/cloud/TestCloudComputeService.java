package org.auscope.portal.core.services.cloud;

import java.nio.charset.Charset;

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
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class TestCloudComputeService extends PortalTestClass {

    private final ComputeService mockComputeService = context.mock(ComputeService.class);
    private final NovaTemplateOptions mockTemplateOptions = context.mock(NovaTemplateOptions.class);
    private final TemplateBuilder mockTemplateBuilder = context.mock(TemplateBuilder.class);
    private final Template mockTemplate = context.mock(Template.class);
    private final NodeMetadata mockMetadata = context.mock(NodeMetadata.class);
    private CloudComputeService service;
    private CloudJob job;

    @Before
    public void initJobObject() {
        job = new CloudJob(13);
        job.setComputeVmId("image-id");
        job.setComputeInstanceType("type");
        service = new CloudComputeService(ProviderType.NovaKeystone, mockComputeService);
        service.setGroupName("group-name");
    }

    /**
     * Tests that job execution correctly calls and parses a response from AmazonEC2
     */
    @Test
    public void testExecuteJob() throws Exception {
        final String userDataString = "user-data-string";
        final String expectedInstanceId = "instance-id";

        context.checking(new Expectations() {{
            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));
            
            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            
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
        final RunNodesException ex = context.mock(RunNodesException.class);

        context.checking(new Expectations() {{
            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));
            
            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            
            oneOf(mockTemplateBuilder).imageId(job.getComputeVmId());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).hardwareId(job.getComputeInstanceType());will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).options(mockTemplateOptions);will(returnValue(mockTemplateBuilder));
            oneOf(mockTemplateBuilder).build();will(returnValue(mockTemplate));
            
            oneOf(mockComputeService).createNodesInGroup("group-name", 1, mockTemplate);
            will(throwException(ex));
            
            allowing(ex).fillInStackTrace();will(returnValue(ex));
            allowing(ex).getMessage();will(returnValue("mock-message"));
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
            oneOf(mockComputeService).templateOptions();will(returnValue(mockTemplateOptions));
            oneOf(mockComputeService).templateBuilder();will(returnValue(mockTemplateBuilder));
            
            oneOf(mockTemplateOptions).keyPairName("vgl-developers");will(returnValue(mockTemplateOptions));
            oneOf(mockTemplateOptions).userData(userDataString.getBytes(Charset.forName("UTF-8")));will(returnValue(mockTemplateOptions));
            
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
}
