package org.auscope.portal.core.services.cloud;

import java.io.UnsupportedEncodingException;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.TextUtil;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.GetConsoleOutputRequest;
import com.amazonaws.services.ec2.model.GetConsoleOutputResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;

/**
 * Service class wrapper for interacting with a remote cloud compute service
 * using CloudJob objects.
 *
 * @author Josh Vote
 */
public class CloudComputeServiceAws extends CloudComputeService {
    /**
     * Any getStatus request on a job whose submission time is less than STATUS_PENDING_SECONDS seconds
     * away from the current time will be forced to return a Pending status (ignoring any status checks)
     *
     * This is to avoid missing errors occurring when AWS hasn't fully caught up to the new VM.
     */
    public static final long STATUS_PENDING_SECONDS = 30;

    private final Log logger = LogFactory.getLog(getClass());

    private String devAccessKey;

    private String devSecretKey;

    private STSRequirement stsRequirement  = STSRequirement.Permissable;

	private String devSessionKey;

    /**
     * Creates a new instance with the specified credentials (no endpoint
     * specified - ensure provider type has a fixed endpoint)
     *
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     * @param sessionKey
     *            The Compute Session key (password)
     *
     */
    public CloudComputeServiceAws(String accessKey, String secretKey, String sessionKey) {
        this(null, accessKey, secretKey, null, sessionKey);
    }

    /**
     * Creates a new instance with the specified credentials (no endpoint
     * specified - ensure provider type has a fixed endpoint)
     *
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     *
     */
    public CloudComputeServiceAws(String accessKey, String secretKey) {
        this(null, accessKey, secretKey, null, null);
    }

    private static String getJaxpImplementationInfo(String componentName, Class<?> componentClass) {
        CodeSource source = componentClass.getProtectionDomain().getCodeSource();
        return MessageFormat.format("{0} implementation: {1} loaded from: {2}", componentName, componentClass.getName(),
                source == null ? "Java Runtime" : source.getLocation());
    }

    /**
     * Creates a new instance with the specified credentials
     *
     * @param endpoint
     *            (URL) The location of the Compute (Nova) service
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     * @param apiVersion
     *            The API version
     */
    public CloudComputeServiceAws(String endpoint, String accessKey, String secretKey, String apiVersion, String sessionKey) {
        super(ProviderType.AWSEc2, endpoint, apiVersion);
        this.devAccessKey = accessKey;
        this.devSecretKey = secretKey;
        this.devSessionKey = sessionKey;

        logger.debug(
                getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
        logger.debug(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
        logger.debug(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
        logger.debug(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));

    }

    /**
     * Returns whether AWS cross account authorization is mandatory, optional or forced off
     * @return
     */
    public STSRequirement getStsRequirement() {
        return stsRequirement;
    }

    /**
     * Sets whether AWS cross account authorization is mandatory, optional or forced off
     * @param stsRequirement
     */
    public void setStsRequirement(STSRequirement stsRequirement) {
        this.stsRequirement = stsRequirement;
    }

    protected AWSCredentials getCredentials(String arn, String clientSecret) throws PortalServiceException {
        if (stsRequirement == STSRequirement.ForceNone) {
            arn = null;
            clientSecret = null;
        }

        if (!TextUtil.isNullOrEmpty(arn)) {
            if (TextUtil.isNullOrEmpty(clientSecret))
                throw new PortalServiceException("Job ARN set, but no client secret");

            AWSSecurityTokenServiceClient stsClient;

            if (!TextUtil.isAnyNullOrEmpty(devAccessKey, devSecretKey)) {
            	AWSCredentials awsCredentials;
                
                if(!TextUtil.isAnyNullOrEmpty(devSessionKey)) {
                	awsCredentials = new BasicSessionCredentials(devAccessKey, devSecretKey, devSessionKey);                
                } else {
                	awsCredentials = new BasicAWSCredentials(devAccessKey, devSecretKey);
                }
                stsClient = new AWSSecurityTokenServiceClient(awsCredentials);
            } else {
                stsClient = new AWSSecurityTokenServiceClient();
            }

            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(arn).withDurationSeconds(3600)
                    .withExternalId(clientSecret).withRoleSessionName("vgl");

            AssumeRoleResult assumeResult = stsClient.assumeRole(assumeRequest);

            // Step 2. AssumeRole returns temporary security credentials for
            // the IAM role.

            return new BasicSessionCredentials(assumeResult.getCredentials().getAccessKeyId(),
                    assumeResult.getCredentials().getSecretAccessKey(),
                    assumeResult.getCredentials().getSessionToken());
        } else if (stsRequirement == STSRequirement.Mandatory) {
            throw new PortalServiceException("AWS cross account authorization required, but not configured");
        } else if (!TextUtil.isAnyNullOrEmpty(devAccessKey, devSecretKey)) {
        	if(!TextUtil.isAnyNullOrEmpty(devSessionKey)) {
        		return new BasicSessionCredentials(devAccessKey, devSecretKey, devSessionKey);
        	} else {
        		return new BasicAWSCredentials(devAccessKey, devSecretKey);
        	}
        }
        return null;
    }

    protected AmazonEC2 getEc2Client(CloudJob job) throws PortalServiceException {
        String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        AWSCredentials creds = getCredentials(arn, clientSecret);
        AmazonEC2 ec2 = creds == null ? new AmazonEC2Client() : new AmazonEC2Client(creds);

        if (!TextUtil.isNullOrEmpty(getEndpoint()))
            ec2.setEndpoint(getEndpoint());
        return ec2;
    }

    /**
     * Returns true if the Compute VM ID in job has one or more persistent volumes (volumes that
     * won't delete on termination).
     *
     * @param job The job whose Compute VM will be checked for persistent volumes
     * @throws PortalServiceException If there is an error communicating with AWS
     * @return
     */
    public boolean containsPersistentVolumes(CloudJob job) throws PortalServiceException {
        String vmId = job.getComputeVmId();
        if (vmId.contains("/")) {
            vmId = vmId.substring(vmId.lastIndexOf("/") + 1);
        }

        try {
            DescribeImagesRequest dir = new DescribeImagesRequest().withImageIds(vmId);
            DescribeImagesResult imageDescs = getEc2Client(job).describeImages(dir);
            if(imageDescs.getImages().isEmpty()) {
                throw new PortalServiceException("Could not get description for image: " + vmId);
            }

            Image imageDesc = imageDescs.getImages().get(0);
            for (BlockDeviceMapping bdm : imageDesc.getBlockDeviceMappings()) {
                Boolean deleteOnTermination =  bdm.getEbs().getDeleteOnTermination();
                if( deleteOnTermination != null && deleteOnTermination == false) {
                    return true;
                }
            }
            return false;
        } catch (AmazonClientException ex) {
            logger.error("Unable to describe images: " + ex.getMessage());
            logger.debug("Error:", ex);
            throw new PortalServiceException("Unable to describe images: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {
        String vmId = job.getComputeVmId();
        if (vmId.contains("/")) {
            vmId = vmId.substring(vmId.lastIndexOf("/") + 1);
        }
        try {
            userDataString = com.amazonaws.util.Base64.encodeAsString(userDataString.getBytes("Utf-8"));
        } catch (UnsupportedEncodingException e) {
            // can't happen
        }

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withInstanceType(job.getComputeInstanceType()).withImageId(vmId).withMinCount(1).withMaxCount(1)
                .withInstanceInitiatedShutdownBehavior("terminate").withUserData(userDataString);

        String instanceProfileArn = job.getProperty(CloudJob.PROPERTY_S3_ROLE);
        if ( (stsRequirement != STSRequirement.ForceNone) && 
        		(!TextUtil.isNullOrEmpty(instanceProfileArn))) {
            IamInstanceProfileSpecification iamInstanceProfile = new IamInstanceProfileSpecification()
                    .withArn(instanceProfileArn);
            runInstancesRequest = runInstancesRequest.withIamInstanceProfile(iamInstanceProfile);
        }

        // Check for a keypair in the CloudJob first, then fall back onto the
        // default one for this instance.
        String keypair = null;
        if (!TextUtil.isNullOrEmpty(job.getComputeInstanceKey())) {
            keypair = job.getComputeInstanceKey();
        }
        if (!TextUtil.isNullOrEmpty(getKeypair())) {
            keypair = getKeypair();
        }
        if (keypair != null) {
            runInstancesRequest = runInstancesRequest.withKeyName(keypair);
        }

        AmazonEC2 ec2 = getEc2Client(job);

        if (!TextUtil.isNullOrEmpty(getEndpoint())) {
            ec2.setEndpoint(getEndpoint());
            // runInstancesRequest=runInstancesRequest.withPlacement(new
            // Placement(getZone()));
        }

        // Wrap any AWS exception into a PortalServiceException
        RunInstancesResult runInstances;
        try {
            runInstances = ec2.runInstances(runInstancesRequest);
        }
        catch (AmazonServiceException ex) {
            throw new PortalServiceException("AWS RunInstances request failed", ex);
        }

        // TAG EC2 INSTANCES
        List<Instance> instances = runInstances.getReservation().getInstances();
        if (instances.isEmpty())
            throw new PortalServiceException("AWS Vm start failed without error message");

        Instance instance = instances.get(0);
        CreateTagsRequest createTagsRequest = new CreateTagsRequest();
        createTagsRequest.withResources(instance.getInstanceId()) //
                .withTags(new Tag("Name", "VGL - Job: " + job.getId()));
        ec2.createTags(createTagsRequest);

        return instance.getInstanceId();
    }

    /**
     * Makes a request that the VM started by job be terminated
     *
     * @param job
     *            The job whose execution should be terminated
     * @throws PortalServiceException
     */
    @Override
    public void terminateJob(CloudJob job) throws PortalServiceException {
        AmazonEC2 ec2 = getEc2Client(job);

        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
                .withInstanceIds(job.getComputeInstanceId());
        ec2.terminateInstances(terminateInstancesRequest);
    }

    final static ComputeType[] COMPUTE_TYPES = {
            new ComputeType("x1.32xlarge", 128, 1920000),
            new ComputeType("c4.8xlarge", 36, 60000),
            new ComputeType("c4.4xlarge", 16, 30000),
            new ComputeType("c4.2xlarge", 8, 15000),
            new ComputeType("c4.xlarge", 4, 7500),
            new ComputeType("c4.large", 2, 3750),
            new ComputeType("m4.10xlarge", 40, 160000),
            new ComputeType("m4.4xlarge", 16, 64000),
            new ComputeType("m4.2xlarge", 8, 32000),
            new ComputeType("m4.xlarge", 4, 16000),
            new ComputeType("m4.large", 2, 8000)
    };

    /**
     * An array of compute types that are available through this compute service
     */
    @Override
    public ComputeType[] getAvailableComputeTypes(Integer minimumVCPUs, Integer minimumRamMB,
            Integer minimumRootDiskGB) {

        ArrayList<ComputeType> result = new ArrayList<>();

        for (ComputeType type : COMPUTE_TYPES) {
            if(    (minimumVCPUs == null || type.getVcpus()>= minimumVCPUs) && (minimumRamMB == null || type.getRamMB()>= minimumRamMB)) {
                result.add(type);
            }
        }
        return result.toArray(new ComputeType[result.size()]);
    }

    /**
     * Will attempt to tail and return the last {@code numLines} from the given
     * servers console.
     *
     * @param job
     *            the job which has been executed by this service
     * @param numLines
     *            the number of console lines to return
     * @return console output as string or null
     * @return
     */
    @Override
    public String getConsoleLog(CloudJob job, int numLines) throws PortalServiceException {
        GetConsoleOutputRequest req = new GetConsoleOutputRequest(job.getComputeInstanceId());

        GetConsoleOutputResult res = getEc2Client(job).getConsoleOutput(req);

        try {
            return res.getDecodedOutput();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Attempts to lookup low level status information about this job's compute instance from the remote cloud.
     *
     * Having no computeInstanceId set will result in an exception being thrown.
     *
     * @param job
     * @return
     * @throws PortalServiceException
     */
    @Override
    public InstanceStatus getJobStatus(CloudJob job) throws PortalServiceException {

        //If the job has just been submitted - don't go checking with AWS, we'll probably get a missing VM message
        //Let the VM have a chance to propogate through AWS
        //See also ANVGL-112
        Date submitDate = job.getSubmitDate();
        if (submitDate != null) {
            Date now = new Date();
            if (TimeUnit.MILLISECONDS.toSeconds(now.getTime() - submitDate.getTime()) < STATUS_PENDING_SECONDS) {
                return InstanceStatus.Pending;
            }
        }

        if (StringUtils.isEmpty(job.getComputeInstanceId())) {
            logger.debug("Unexpected missing job ID in getJobStatus(). Will return 'pending'. Local status: "+job.getStatus());
            return InstanceStatus.Pending;
        }

        DescribeInstanceStatusRequest request = new DescribeInstanceStatusRequest();
        request.setInstanceIds(Arrays.asList(job.getComputeInstanceId()));

        try {
            AmazonEC2 ec2 = getEc2Client(job);

            DescribeInstanceStatusResult result = ec2.describeInstanceStatus(request);
            List<com.amazonaws.services.ec2.model.InstanceStatus> statuses = result.getInstanceStatuses();
            if (statuses.isEmpty()) {
                return InstanceStatus.Missing;
            }
            String status = statuses.get(0).getInstanceState().getName();
            switch(status) {
            case "pending":
                return InstanceStatus.Pending;
            case "running":
                return InstanceStatus.Running;
            default:
                return InstanceStatus.Missing;
            }
        } catch (AmazonServiceException ex) {
            //Some of the "expected" AWS responses come from parsing exceptions
            switch (ex.getErrorCode()) {
            case "InvalidInstanceID.NotFound":
                return InstanceStatus.Missing;
            default:
                // ignore all other cases
                break;
            }

            switch (ex.getStatusCode()) {
            case HttpStatus.SC_FORBIDDEN:
                return InstanceStatus.Missing;
            default:
                throw new PortalServiceException("Unable to lookup status code for :" + job.getComputeInstanceId(), ex);
            }
        } catch (Exception ex) {
            throw new PortalServiceException("Unable to lookup status code for :" + job.getComputeInstanceId(), ex);
        }
    }

    @Override
    public ComputeType[] getAvailableComputeTypes(String machineImageId) throws PortalServiceException {
        // Carsten: As far as I know AWS images have no specific limitation on what ComputeType can run them. To be implemented properly if this
        //          turns out to be wrong
        return getAvailableComputeTypes();
    }
}
