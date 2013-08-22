package org.auscope.portal.core.services.cloud;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.services.PortalServiceException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
/**
 * Service class wrapper for interacting with a remote cloud compute service using
 * CloudJob objects.
 *
 * @author Josh Vote
 */
public class CloudComputeService {

    private final Log logger = LogFactory.getLog(getClass());

    private AWSCredentials credentials;
    private String endpoint;

    /** Unique ID for distinguishing instances of this class - can be null*/
    private String id;
    /** A short descriptive name for human identification of this service*/
    private String name;

    /** An array of images that are available through this compute service*/
    private MachineImage[] availableImages = new MachineImage[0];

    /** An array of compute types that are available through this compute service*/
    private ComputeType[] availableComputeTypes = new ComputeType[0];

    /**
     * Creates a new instance with the specified credentials
     * @param endpoint (URL) The location of the EC2 service
     * @param credentials the compute credentials
     */
    public CloudComputeService(String endpoint, AWSCredentials credentials) {
        this.endpoint = endpoint;
        this.credentials = credentials;
    }

    /**
     * Creates a new instance with the specified credentials
     * @param endpoint (URL) The location of the EC2 service
     * @param accessKey The EC2 Access key (user name)
     * @param secretKey The EC2 Secret key (password)
     */
    public CloudComputeService(String endpoint, String accessKey, String secretKey) {
        this.endpoint = endpoint;
        this.credentials = new BasicAWSCredentials(accessKey, secretKey);
    }

    /**
     * Unique ID for distinguishing instances of this class - can be null
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Unique ID for distinguishing instances of this class - can be null
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * An array of images that are available through this compute service
     * @return
     */
    public MachineImage[] getAvailableImages() {
        return availableImages;
    }

    /**
     * An array of images that are available through this compute service
     * @param availableImages
     */
    public void setAvailableImages(MachineImage[] availableImages) {
        this.availableImages = availableImages;
    }


    /**
     * An array of compute types that are available through this compute service
     */
    public ComputeType[] getAvailableComputeTypes() {
        return availableComputeTypes;
    }

    /**
     * An array of compute types that are available through this compute service
     * @param availableComputeTypes
     */
    public void setAvailableComputeTypes(ComputeType[] availableComputeTypes) {
        this.availableComputeTypes = availableComputeTypes;
    }

    /**
     * Gets an instance of an AmazonEC2 for use in submitting/terminating jobs
     * @return
     */
    protected AmazonEC2 getAmazonEC2Instance() {
        AmazonEC2Client client = new AmazonEC2Client(credentials);
        client.setEndpoint(endpoint);
        return client;
    }

    /**
     *  A short descriptive name for human identification of this service
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * A short descriptive name for human identification of this service
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Begins execution of the specified job and returns the ID of the started instance.
     *
     * This function will create a VM to run the job which will be responsible for decoding
     * the userDataString and downloading any input files from the JobStorageService
     *
     * @param job The job to execute
     * @param userDataString A string that is made available to the job when it starts execution (this will be Base64 encoded before being sent to the VM)
     * @return null if execution fails or the instance ID of the running VM
     */
    public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {
        try {
            AmazonEC2 ec2 = getAmazonEC2Instance();
            RunInstancesRequest instanceRequest = new RunInstancesRequest(job.getComputeVmId(), 1, 1);

            String base64EncodedUserData = new String(Base64.encodeBase64(userDataString.toString().getBytes()));
            instanceRequest.setUserData(base64EncodedUserData);
            if (job.getComputeInstanceType() != null) {
                instanceRequest.setInstanceType(job.getComputeInstanceType());
            }
            if (job.getComputeInstanceKey() != null) {
                instanceRequest.setKeyName(job.getComputeInstanceKey());
            }
            instanceRequest.setInstanceInitiatedShutdownBehavior("terminate");

            RunInstancesResult result = ec2.runInstances(instanceRequest);
            List<Instance> instances = result.getReservation().getInstances();

            //We should get a single item on success
            if (instances.size() == 0 || instances.get(0) == null) {
                throw new Exception("VM started but failed to fetch instance id.");
            }
            Instance instance = instances.get(0);
            return instance.getInstanceId();
        } catch (AmazonServiceException ex) {
            logger.error("Compute service is currently unavailable.", ex);
            throw new PortalServiceException("Compute service is currently unavailable.", "Please try again in a few minutes. [" + ex.getMessage() + "]");
        } catch (AmazonClientException ex) {
            logger.error("Network connection to compute service is currently unavailable.", ex);
            throw new PortalServiceException("Network connection to compute service is currently unavailable.", "Please try again in a few minutes.");
        } catch (Exception ex) {
            logger.error("An unexpected error has occurred while executing job: " + job, ex);
            throw new PortalServiceException("An unexpected error has occurred while executing your job.", "Please report it to cg-admin@csiro.au.");
        }
    }

    /**
     * Makes a request that the VM started by job be terminated
     * @param job The job whose execution should be terminated
     */
    public void terminateJob(CloudJob job) {
        AmazonEC2 ec2 = getAmazonEC2Instance();

        TerminateInstancesRequest termReq = new TerminateInstancesRequest();
        ArrayList<String> instanceIdList = new ArrayList<String>();
        instanceIdList.add(job.getComputeInstanceId());
        termReq.setInstanceIds(instanceIdList);
        ec2.terminateInstances(termReq);
    }
}
