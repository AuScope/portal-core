package org.auscope.portal.core.services.cloud;

import static com.google.common.base.Predicates.not;
import static org.jclouds.compute.predicates.NodePredicates.RUNNING;
import static org.jclouds.compute.predicates.NodePredicates.TERMINATED;
import static org.jclouds.compute.predicates.NodePredicates.inGroup;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.services.PortalServiceException;
import org.jclouds.ContextBuilder;
import org.jclouds.aws.AWSResponseException;
import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Processor;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.zonescoped.AvailabilityZone;
import org.jclouds.openstack.nova.v2_0.extensions.AvailabilityZoneApi;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.GetConsoleOutputRequest;
import com.amazonaws.services.ec2.model.GetConsoleOutputResult;
import com.amazonaws.services.ec2.model.InstanceAttributeName;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Service class wrapper for interacting with a remote cloud compute service using CloudJob objects.
 *
 * @author Josh Vote
 */
public class CloudComputeService {

    public enum ProviderType {
        /** Connect to an Openstack instance via the Keystone Identity service */
        NovaKeystone,
        /** Connect to an Amazon Web Services instance via EC2 */
        AWSEc2,
    }

    private final Log logger = LogFactory.getLog(getClass());

    private ComputeService computeService;
    private ComputeServiceContext context;
    private NovaApi novaApi; //will be null for non nova API's
    private AmazonEC2Client ec2Api; //will be null for non ec2 API's
    private Set<String> skippedZones = new HashSet<String>();
    private String zone; //can be null

    private Predicate<NodeMetadata> terminateFilter;

    private String itActuallyLaunchedHere;

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
    /** Name of accessKey for authentication */
    private String accessKey;
    /** Name of secretKey for authentication */
    private String secretKey;
    /** Cloud endpoint to connect to */
    private String endpoint;

    /**
     * Creates a new instance with the specified credentials (no endpoint specified - ensure provider type has a fixed endpoint)
     *
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     *
     */
    public CloudComputeService(ProviderType provider, String accessKey, String secretKey) {
        this(provider, null, accessKey, secretKey, null);
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
    public CloudComputeService(ProviderType provider, String endpoint, String accessKey, String secretKey) {
        this(provider, endpoint, accessKey, secretKey, null);
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
    public CloudComputeService(ProviderType provider, String endpoint, String accessKey, String secretKey,
            String apiVersion) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;

        Properties overrides = new Properties();

        String typeString = "";
        switch (provider) {
        case NovaKeystone:
            typeString = "openstack-nova";
            break;
        case AWSEc2:
            typeString = "aws-ec2";
            break;
        default:
            throw new IllegalArgumentException("Unsupported provider: " + provider.name());
        }
        this.provider = provider;

        ContextBuilder b = ContextBuilder.newBuilder(typeString)
                .overrides(overrides)
                .credentials(accessKey, secretKey);

        if (apiVersion != null) {
            b.apiVersion(apiVersion);
        }

        if (endpoint != null) {
            b.endpoint(endpoint);
        }

        //Setup our low level API's
        switch (provider) {
        case NovaKeystone:
            this.novaApi = b.buildApi(NovaApi.class);
            break;
        case AWSEc2:
            this.ec2Api = new AmazonEC2Client(new BasicAWSCredentials(accessKey, secretKey));
            break;
        }

        this.context = b.buildView(ComputeServiceContext.class);
        this.computeService = this.context.getComputeService();

        this.terminateFilter = Predicates.and(not(TERMINATED), not(RUNNING), inGroup(groupName));

    }

    public CloudComputeService(ProviderType provider, ComputeService computeService, NovaApi novaApi,
            Predicate<NodeMetadata> terminPredicate) {
        this.provider = provider;
        this.computeService = computeService;
        this.novaApi = novaApi;
        this.terminateFilter = terminPredicate;
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
     * Gets the region (if any) where this compute service will be restricted to run in
     *
     * Will be ignored if skipped zones is specified
     * @return
     */
    public String getZone() {
        return zone;
    }

    /**
     * Sets the region (if any) where this compute service will be restricted to run in
     *
     * Will be ignored if skipped zones is specified
     *
     * @param zone
     */
    public void setZone(String zone) {
        this.zone = zone;
        if (this.ec2Api != null) {
            this.ec2Api.setRegion(Region.getRegion(Regions.fromName(zone)));
        }
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
    public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {

        //We have different template options depending on provider
        TemplateOptions options = null;
        Set<? extends NodeMetadata> results = Collections.emptySet();
        NodeMetadata result;

        if (provider == ProviderType.AWSEc2) {
            options = ((AWSEC2TemplateOptions) computeService.templateOptions())
                    .keyPair(getKeypair())
                    .userData(userDataString.getBytes(Charset.forName("UTF-8")));

            TemplateBuilder tb = computeService.templateBuilder()
                    .imageId(job.getComputeVmId())
                    .hardwareId(job.getComputeInstanceType())
                    .options(options);

            if (this.zone != null) {
                tb.locationId(zone);
            }

            Template template = tb.build();

            //Start up the job, we should have exactly 1 node start
            try {
                results = computeService.createNodesInGroup(groupName, 1, template);
            } catch (RunNodesException e) {
                logger.error(String.format("An unexpected error '%1$s' occured while executing job '%2$s'",
                        e.getMessage(), job));
                logger.debug("Exception:", e);
                throw new PortalServiceException(
                        "An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
                                + "a smaller virtual machine", "Please report it to cg-admin@csiro.au : "
                                + e.getMessage(), e);
            } catch (AWSResponseException e) {
                logger.error(String.format("An unexpected error '%1$s' occured while executing job '%2$s'",
                        e.getMessage(), job));
                logger.debug("Exception:", e);
                throw new PortalServiceException(
                        "An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
                                + "a smaller virtual machine", "Please report it to cg-admin@csiro.au : "
                                + e.getMessage(), e);
            }
            if (results.isEmpty()) {
                logger.error("JClouds returned an empty result set. Treating it as job failure.");
                throw new PortalServiceException(
                        "Unable to start compute node due to an unknown error, no nodes returned");
            }
            result = results.iterator().next();

            //Configure the instance to terminate on shutdown:
            try {
                ModifyInstanceAttributeRequest miar = new ModifyInstanceAttributeRequest(result.getId(), InstanceAttributeName.InstanceInitiatedShutdownBehavior);
                miar.setValue("terminate");
                ec2Api.modifyInstanceAttribute(miar);
            } catch (AmazonClientException ex) {
                //if we fail here - kill the instance, we don't want a floating VM sitting around
                logger.error(String.format("Instance ID '%1$s' could NOT be set to terminate on shutdown: %2$s", result.getId(), ex.getMessage()));
                logger.debug("Exception:", ex);
                computeService.destroyNode(result.getId());
                throw new PortalServiceException(
                        "An unexpected error has occured while executing your job. There were problems when setting up the job in AWS. Please try again at a later date."
                                ,"Please report it to cg-admin@csiro.au : "
                                + ex.getMessage(), ex);
            }
        } else {
            //Iterate all regions
            for (String location : novaApi.getConfiguredZones()) {
                Optional<? extends AvailabilityZoneApi> serverApi = novaApi.getAvailabilityZoneApi(location);
                Iterable<? extends AvailabilityZone> zones = serverApi.get().list();

                for (AvailabilityZone currentZone : zones) {
                    if (skippedZones.contains(currentZone.getName())) {
                        logger.info(String.format("skipping: '%1$s' - configured as a skipped zone",
                                currentZone.getName()));
                        continue;
                    }

                    if (!currentZone.getState().available()) {
                        logger.info(String.format("skipping: '%1$s' - not available", currentZone.getName()));
                        continue;
                    }

                    logger.info(String.format("Trying '%1$s'", currentZone.getName()));
                    options = ((NovaTemplateOptions) computeService.templateOptions())
                            .keyPairName(getKeypair())
                            .availabilityZone(currentZone.getName())
                            .userData(userDataString.getBytes(Charset.forName("UTF-8")));

                    Template template = computeService.templateBuilder()
                            .imageId(job.getComputeVmId())
                            .hardwareId(job.getComputeInstanceType())
                            .options(options)
                            .build();

                    try {
                        results = computeService.createNodesInGroup(groupName, 1, template);
                        this.itActuallyLaunchedHere = currentZone.getName();
                        break;
                    } catch (RunNodesException e) {
                        logger.error(String.format("launch failed at '%1$s', '%2$s'", location, currentZone.getName()));
                        logger.debug(e.getMessage());
                        try {
                            // FIXME:
                            // I think this could possibly delete EVERY NODE RUN from PORTAL-CORE...
                            // JClouds is not very clever here -
                            // issue: how do you delete thing you didnt name and dont have an ID for??
                            Set<? extends NodeMetadata> destroyedNodes = computeService
                                    .destroyNodesMatching(this.terminateFilter);
                            logger.warn(String.format("cleaned up %1$s nodes: %2$s", destroyedNodes.size(),
                                    destroyedNodes));
                        } catch (Exception z) {
                            logger.warn("couldnt clean it up");
                        }
                        continue;
                    }
                }
            }
            if (results.isEmpty()) {
                //Now we have tried everything....
                logger.error("run out of places to try...");
                throw new PortalServiceException(
                        "An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
                                + "a smaller virtual machine", "Please report it to cg-admin@csiro.au ");
            }
            else {
                result = results.iterator().next();
            }

        }

        logger.info(String.format("We have a successful launch @ '%1$s'", this.itActuallyLaunchedHere));

        return result.getId();
    }

    /**
     * Makes a request that the VM started by job be terminated
     *
     * @param job
     *            The job whose execution should be terminated
     */
    public void terminateJob(CloudJob job) {
        computeService.destroyNode(job.getComputeInstanceId());
    }

    public ComputeType[] getAvailableComputeTypes() {
        return getAvailableComputeTypes(null, null, null);
    }

    /**
     * An array of compute types that are available through this compute service
     */
    public ComputeType[] getAvailableComputeTypes(Integer minimumVCPUs, Integer minimumRamMB, Integer minimumRootDiskGB) {
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

    public String getKeypair() {
        // Default to the old behaviour until a different keypair is
        // configured.
        return keypair != null ? keypair : "vgl-developers";
    }

    public void setKeypair(String keypair) {
        this.keypair = keypair;
    }

    /**
     * Gets the set of zone names that should be skipped when attempting to find a zone to run a job at.
     *
     * @return
     */
    public Set<String> getSkippedZones() {
        return skippedZones;
    }

    /**
     * Sets the set of zone names that should be skipped when attempting to find a zone to run a job at.
     *
     * @param skippedZones
     */
    public void setSkippedZones(Set<String> skippedZones) {
        this.skippedZones = skippedZones;
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
     * Gets console logs specifically for an OpenStack instance
     * @param computeInstanceId
     * @param job
     * @param numLines
     * @return
     * @throws PortalServiceException
     */
    private String getConsoleLogOpenStack(String computeInstanceId, CloudJob job, int numLines) throws PortalServiceException {
        try {
            String[] accessParts = this.accessKey.split(":");
            String[] idParts = computeInstanceId.split("/");

            //JClouds has no support (currently) for tailing server console output. Our current workaround
            //is to offload this to openstack4j.
            OSClient os = OSFactory.builder()
                    .endpoint(endpoint)
                    .credentials(accessParts[1], secretKey)
                    .tenantName(accessParts[0])
                    .authenticate();

            return os.compute().servers().getConsoleOutput(idParts[1], numLines);
        } catch (Exception ex) {
            logger.error("Unable to retrieve console logs for " + computeInstanceId, ex);
            throw new PortalServiceException("Unable to retrieve console logs for " + computeInstanceId, ex);
        }
    }

    /**
     * Gets console logs specifically for an AWS instance
     * @param computeInstanceId
     * @param job
     * @param numLines
     * @return
     * @throws PortalServiceException
     */
    private String getConsoleLogAws(String computeInstanceId, CloudJob job, int numLines) throws PortalServiceException {
        GetConsoleOutputRequest getConsoleOutputRequest = new GetConsoleOutputRequest(computeInstanceId);

        try {
            GetConsoleOutputResult result = ec2Api.getConsoleOutput(getConsoleOutputRequest);
            return result.getDecodedOutput();
        } catch (AmazonClientException ex) {
            logger.error("Unable to retrieve console logs for " + computeInstanceId, ex);
            throw new PortalServiceException("Unable to retrieve console logs for " + computeInstanceId, ex);
        }
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
        String computeInstanceId = job.getComputeInstanceId();
        if (computeInstanceId == null) {
            return null;
        }

        switch (provider) {
        case NovaKeystone:
            return getConsoleLogOpenStack(computeInstanceId, job, numLines);
        case AWSEc2:
            return getConsoleLogAws(computeInstanceId, job, numLines);
        }

        throw new PortalServiceException("Cannot get logs for provider type: " + provider);
    }
}
