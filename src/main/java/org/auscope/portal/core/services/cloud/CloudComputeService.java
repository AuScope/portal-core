package org.auscope.portal.core.services.cloud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.services.PortalServiceException;

/**
 * Service class wrapper for interacting with a remote cloud compute service using CloudJob objects.
 *
 * @author Josh Vote
 */
abstract public class CloudComputeService {

    public enum ProviderType {
        /** Connect to an Openstack instance via the Keystone Identity service */
        NovaKeystone,
        /** Connect to an Amazon Web Services instance via EC2 */
        AWSEc2,
        /** Connect to Raijin NCI HPC */
        RAIJIN
    }

    /**
     * The status of a compute instance (not the job status) as reported by the remote cloud.
     * @author Josh Vote (CSIRO)
     *
     */
    public enum InstanceStatus {
        /**
         * Job is still waiting to start
         */
        Pending,
        /**
         * Instance is running
         */
        Running,
        /**
         * The instance could not be found or it's in a terminated state.
         */
        Missing,
    }

    @SuppressWarnings("unused")
    private final Log logger = LogFactory.getLog(getClass());

    /** Unique ID for distinguishing instances of this class - can be null */
    private String id;
    /** A short descriptive name for human identification of this service */
    private String name;
    /** What type of cloud service are we communicating with */
    private ProviderType provider;

    /** A group name that all jobs will be assigned to */
    private String groupName = "portal-cloud-compute-service";

    /** An array of images that are available through this compute service */
    private MachineImage[] availableImages = new MachineImage[0];

    /**
     * Name of the developers' keypair to inject into instances on this provider.
     */
    private String keypair;

    /** Cloud endpoint to connect to */
    private String endpoint;

    public ProviderType getProvider() {
        return provider;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Cloud API version
     */
    private String apiVersion;

    /**
     * Creates a new instance with the specified credentials (no endpoint specified - ensure provider type has a fixed endpoint)
     *
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     *
     */
    public CloudComputeService(ProviderType provider) {
        this(provider, null, null);
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
     *
     */
    public CloudComputeService(ProviderType provider, String endpoint) {
        this(provider, endpoint, null);
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
    public CloudComputeService(ProviderType provider, String endpoint, String apiVersion) {
        this.provider = provider;
        this.endpoint = endpoint;
        this.apiVersion = apiVersion;
    }

    /**
     * Unique ID for distinguishing instances of this class - can be null
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Unique ID for distinguishing instances of this class - can be null
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /** A group name that all jobs will be assigned to */
    public String getGroupName() {
        return groupName;
    }

    /** A group name that all jobs will be assigned to */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * An array of images that are available through this compute service
     *
     * @return
     */
    public MachineImage[] getAvailableImages() {
        return availableImages;
    }

    /**
     * An array of images that are available through this compute service
     *
     * @param availableImages
     */
    public void setAvailableImages(MachineImage[] availableImages) {
        this.availableImages = availableImages;
    }

    /**
     * A short descriptive name for human identification of this service
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * A short descriptive name for human identification of this service
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Begins execution of the specified job and returns the ID of the started instance.
     *
     * This function will create a VM to run the job which will be responsible for decoding the userDataString and downloading any input files from the
     * JobStorageService
     *
     * @param job
     *            The job to execute
     * @param userDataString
     *            A string that is made available to the job when it starts execution (this will be Base64 encoded before being sent to the VM)
     * @return null if execution fails or the instance ID of the running VM
     */
    abstract public String executeJob(CloudJob job, String userDataString) throws PortalServiceException;

    /**
     * Makes a request that the VM started by job be terminated
     *
     * @param job
     *            The job whose execution should be terminated
     * @throws PortalServiceException
     */
    abstract public void terminateJob(CloudJob job) throws PortalServiceException;

    public ComputeType[] getAvailableComputeTypes() {
        return getAvailableComputeTypes(null, null, null);
    }

    /**
     * An array of compute types that are available through this compute service
     */
    abstract public ComputeType[] getAvailableComputeTypes(Integer minimumVCPUs, Integer minimumRamMB, Integer minimumRootDiskGB);

    /**
     * Return the ssh keypair to be used with the VM
     * @return
     */
    public String getKeypair() {
        return keypair;
    }

    /**
     * Sets the ssh keypair to be used with the VM
     * @param keypair
     */
    public void setKeypair(String keypair) {
        this.keypair = keypair;
    }

    /**
     * Will attempt to tail and return the last 1000 lines from the given servers console.
     *
     * @param job
     *            the job which has been executed by this service
     * @param numLines
     *            the number of console lines to return
     * @return console output as string or null
     * @return
     */
    public String getConsoleLog(CloudJob job) throws PortalServiceException {
        return getConsoleLog(job, 1000);
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
    abstract public String getConsoleLog(CloudJob job, int numLines) throws PortalServiceException;

    /**
     * Attempts to lookup low level status information about this job's compute instance from the remote cloud.
     *
     * Having no computeInstanceId set will result in an exception being thrown.
     *
     * @param job
     * @return
     * @throws PortalServiceException
     */
    abstract public InstanceStatus getJobStatus(CloudJob job) throws PortalServiceException;

    /**
     * Return all VM types that can run the specified image
     * 
     * @param machineImageId
     * @return all VM types that can run the specified image
     * @throws PortalServiceException 
     */
    abstract public ComputeType[] getAvailableComputeTypes(String machineImageId) throws PortalServiceException;
}
