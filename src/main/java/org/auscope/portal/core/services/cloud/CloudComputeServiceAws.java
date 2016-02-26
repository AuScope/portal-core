package org.auscope.portal.core.services.cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.TextUtil;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Processor;
import org.jclouds.compute.domain.Volume;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.GetConsoleOutputRequest;
import com.amazonaws.services.ec2.model.GetConsoleOutputResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;

/**
 * Service class wrapper for interacting with a remote cloud compute service using CloudJob objects.
 *
 * @author Josh Vote
 */
public class CloudComputeServiceAws extends CloudComputeService {
    @SuppressWarnings("unused")
	private final Log logger = LogFactory.getLog(getClass());

	private String devAccessKey;

	private String devSecretKey;

    /**
     * Creates a new instance with the specified credentials (no endpoint specified - ensure provider type has a fixed endpoint)
     *
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     *
     */
    public CloudComputeServiceAws(String accessKey, String secretKey) {
        this(null, accessKey, secretKey, null);
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
    public CloudComputeServiceAws(String endpoint, String accessKey, String secretKey, String apiVersion) {
    	super(ProviderType.AWSEc2, endpoint, apiVersion);
        this.devAccessKey = accessKey;
        this.devSecretKey = secretKey;
	}

//	private static final String STS_ROLE_ARN = "arn:aws:iam::696640869989:role/vbkd-dsadss-AnvglStsRole-1QZG62NWIOK2";
//	private static final String S3_PROFILE_ARN = "arn:aws:iam::696640869989:instance-profile/vbkd-dsadss-AnvglS3InstanceProfile-17Z06U2BEOANC";
//	private static final String CLIENT_SECRET = "1234"; // Must match value in policy

	protected AWSCredentials getCredentials(String arn, String clientSecret) throws PortalServiceException {
		if (! TextUtil.isNullOrEmpty(arn)) {
			if(TextUtil.isNullOrEmpty(clientSecret))
				throw new PortalServiceException("Job ARN set, but no client secret");
			
			AWSSecurityTokenServiceClient stsClient;

			if (TextUtil.isAnyNullOrEmpty(devAccessKey, devSecretKey)) {
				BasicAWSCredentials awsCredentials = new BasicAWSCredentials(devAccessKey, devSecretKey);
				stsClient = new AWSSecurityTokenServiceClient(awsCredentials);
			} else {
				stsClient = new AWSSecurityTokenServiceClient();
			}

			AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(arn)
					.withDurationSeconds(3600).withExternalId(clientSecret).withRoleSessionName("anvgl");

			AssumeRoleResult assumeResult = stsClient.assumeRole(assumeRequest);

			// Step 2. AssumeRole returns temporary security credentials for
			// the IAM role.

			return new BasicSessionCredentials(assumeResult.getCredentials().getAccessKeyId(),
					assumeResult.getCredentials().getSecretAccessKey(),
					assumeResult.getCredentials().getSessionToken());
		} else if (! TextUtil.isAnyNullOrEmpty(devAccessKey, devSecretKey)){
			return new BasicAWSCredentials(devAccessKey, devSecretKey);
		}
		return null;
	}
	
	protected AmazonEC2 getEc2Client(CloudJob job) throws PortalServiceException {
		String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
		String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);
		
		AWSCredentials creds = getCredentials(arn, clientSecret);
		AmazonEC2 ec2 = creds == null ? new AmazonEC2Client(): new AmazonEC2Client(creds);
		
		if(! TextUtil.isNullOrEmpty(getEndpoint()))
			ec2.setEndpoint(getEndpoint());
		return ec2;
	}
	
	public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withInstanceType(job.getComputeInstanceType())
				.withImageId(job.getComputeVmId()).withMinCount(1).withMaxCount(1)
				.withInstanceInitiatedShutdownBehavior("terminate").withUserData(userDataString);

		String instanceProfileArn = job.getProperty(CloudJob.PROPERTY_S3_ROLE);
		if (!TextUtil.isNullOrEmpty(instanceProfileArn)) {
			IamInstanceProfileSpecification iamInstanceProfile = new IamInstanceProfileSpecification()
					.withArn(instanceProfileArn);
			runInstancesRequest = runInstancesRequest.withIamInstanceProfile(iamInstanceProfile);
		}
		
		if(! TextUtil.isNullOrEmpty(getKeypair())) {
			runInstancesRequest=runInstancesRequest.withKeyName(getKeypair());
		}
		
		if(! TextUtil.isNullOrEmpty(getZone())) {
			runInstancesRequest=runInstancesRequest.withPlacement(new Placement(getZone()));
		}

		AmazonEC2 ec2 = getEc2Client(job);
		RunInstancesResult runInstances = ec2.runInstances(runInstancesRequest);

		// TAG EC2 INSTANCES
		List<Instance> instances = runInstances.getReservation().getInstances();
		if(instances.isEmpty()) throw new PortalServiceException("AWS Vm start failed without error message");
		
		Instance instance = instances.get(0);
		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.withResources(instance.getInstanceId()) //
				.withTags(new Tag("Name", "ANVGL - Job: " + job.getId()));
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
	public void terminateJob(CloudJob job) throws PortalServiceException {
		AmazonEC2 ec2 = getEc2Client(job);

		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest()
				.withInstanceIds(job.getComputeInstanceId());
		ec2.terminateInstances(terminateInstancesRequest);
	}

    /**
     * An array of compute types that are available through this compute service
     */
    public ComputeType[] getAvailableComputeTypes(Integer minimumVCPUs, Integer minimumRamMB, Integer minimumRootDiskGB) {
  //  	InstanceType[] types = InstanceType.values();
    	
        Properties overrides = new Properties();

        ContextBuilder builder = ContextBuilder.newBuilder("aws-ec2")
                .overrides(overrides);
        
        if(devAccessKey!=null && devSecretKey!=null)
        	builder.credentials(devAccessKey, devSecretKey);

        if (getApiVersion() != null) {
            builder.apiVersion(getApiVersion());
        }

        if (getEndpoint() != null) {
            builder.endpoint(getEndpoint());
        }

		ComputeServiceContext context = builder.buildView(ComputeServiceContext.class);
        ComputeService computeService = context.getComputeService();
        Set<? extends Hardware> hardwareSet = computeService.listHardwareProfiles();

        List<ComputeType> computeTypes = new ArrayList<ComputeType>();

        for (Hardware hw : hardwareSet) {
            ComputeType ct = new ComputeType(hw.getId());

            ct.setDescription(hw.getName());
            double vCpus = 0;
            for (Processor p : hw.getProcessors()) {
                vCpus += p.getCores();
            }
            ct.setVcpus((int) vCpus);
            ct.setRamMB(hw.getRam());

            double rootDiskGB = 0;
            double ephemeralDiskGB = 0;
            for (Volume v : hw.getVolumes()) {
                if (v.isBootDevice()) {
                    rootDiskGB += v.getSize();
                } else {
                    ephemeralDiskGB += v.getSize();
                }
            }
            ct.setRootDiskGB((int) rootDiskGB);
            ct.setEphemeralDiskGB((int) ephemeralDiskGB);

            //Skip anything that doesn't match our filters
            if (minimumVCPUs != null && minimumVCPUs > ct.getVcpus()) {
                continue;
            } else if (minimumRamMB != null && minimumRamMB > ct.getRamMB()) {
                continue;
            } else if (minimumRootDiskGB != null && minimumRootDiskGB > ct.getRootDiskGB()) {
                continue;
            }

            computeTypes.add(ct);
        }

        return computeTypes.toArray(new ComputeType[computeTypes.size()]);
    }

    /**
     * Will attempt to tail and return the last {@code numLines} from the given servers console.
     *
     * @param job
     *            the job which has been executed by this service
     * @param numLines
     *            the number of console lines to return
     * @return console output as string or null
     * @return
     */
	public String getConsoleLog(CloudJob job, int numLines) throws PortalServiceException {
		GetConsoleOutputRequest req = new GetConsoleOutputRequest(job.getComputeInstanceId());

		GetConsoleOutputResult res = getEc2Client(job).getConsoleOutput(req);

		return res.getDecodedOutput();
	}
}
