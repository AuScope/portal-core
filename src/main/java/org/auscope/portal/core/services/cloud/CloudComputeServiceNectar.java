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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.services.PortalServiceException;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadata.Status;
import org.jclouds.compute.domain.Processor;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.Image;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Service class wrapper for interacting with a remote cloud compute service using CloudJob objects.
 *
 * @author Josh Vote
 */
public class CloudComputeServiceNectar extends CloudComputeService {
    private final Log logger = LogFactory.getLog(getClass());

    private ComputeService computeService;
    private ComputeServiceContext context;
    private NovaApi novaApi; //will be null for non nova API's

    private Set<String> skippedZones = new HashSet<>();

    private Predicate<NodeMetadata> terminateFilter;

    /** Name of accessKey for authentication */
    private String accessKey;
    /** Name of secretKey for authentication */
    private String secretKey;
    /** Cloud endpoint to connect to */
    private String endpoint;

    private ContextBuilder builder;

    private String zone; //can be null

    private String adminEmail = "cg-admin@csiro.au";

    private ImageApi imageApi;

    /**
     * @return the adminEmail
     */
    public String getAdminEmail() {
        return adminEmail;
    }

    /**
     * @param adminEmail the adminEmail to set
     */
    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
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
    }

    /**
     * Creates a new instance with the specified credentials (no endpoint specified - ensure provider type has a fixed endpoint)
     *
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     *
     */
    public CloudComputeServiceNectar(String accessKey, String secretKey) {
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
     *
     */
    public CloudComputeServiceNectar(String endpoint, String accessKey, String secretKey) {
        this(endpoint, accessKey, secretKey, null);
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
    @SuppressWarnings("unchecked")
    public CloudComputeServiceNectar(String endpoint, String accessKey, String secretKey, String apiVersion) {
        super(ProviderType.NovaKeystone, endpoint, apiVersion);
        this.accessKey = accessKey;
        this.secretKey = secretKey;

        Properties overrides = new Properties();

        String typeString = "openstack-nova";

        builder = ContextBuilder.newBuilder(typeString).overrides(overrides);

        if(accessKey!=null && secretKey!=null)
            builder.credentials(accessKey, secretKey);

        if (getApiVersion() != null) {
            builder.apiVersion(getApiVersion());
        }

        if (endpoint != null) {
            builder.endpoint(endpoint);
        }

        this.novaApi = builder.buildApi(NovaApi.class);

        Set<String> regions = novaApi.getConfiguredRegions();
        String region = null;
        if(! regions.isEmpty())
            region = regions.iterator().next();
        
        this.imageApi = novaApi.getImageApi(region);
        
        this.context = builder.buildView(ComputeServiceContext.class);
        this.computeService = this.context.getComputeService();
        this.terminateFilter = Predicates.and(not(TERMINATED), not(RUNNING), inGroup(getGroupName()));
    }

    public CloudComputeServiceNectar(ComputeService computeService, NovaApi novaApi,
            Predicate<NodeMetadata> terminPredicate) {
        super(ProviderType.NovaKeystone);
        this.computeService = computeService;
        this.novaApi = novaApi;
        this.terminateFilter = terminPredicate;
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
    @Override
    public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {

        // We have different template options depending on provider
        NodeMetadata result;
        Set<? extends NodeMetadata> results = Collections.emptySet();
        TemplateOptions options = ((NovaTemplateOptions) computeService.templateOptions())
                .keyPairName(getKeypair())
                .userData(userDataString.getBytes(Charset.forName("UTF-8")));

        Template template = computeService.templateBuilder().imageId(job.getComputeVmId())
                .hardwareId(job.getComputeInstanceType()).options(options).build();

        try {
            results = computeService.createNodesInGroup(getGroupName(), 1, template);
        } catch (RunNodesException e) {
            logger.error(String.format("Launch failure from service '%4$s' for job %3$s image '%1$s' type '%2$s'", job.getComputeVmId(), job.getComputeInstanceType(), job.getId(), this.getId()));
            logger.debug(e.getMessage());
            try {
                // FIXME:
                // I think this could possibly delete EVERY NODE RUN
                // from PORTAL-CORE...
                // JClouds is not very clever here -
                // issue: how do you delete thing you didnt name and
                // dont have an ID for??
                Set<? extends NodeMetadata> destroyedNodes = computeService.destroyNodesMatching(this.terminateFilter);
                logger.warn(String.format("cleaned up %1$s nodes: %2$s", destroyedNodes.size(), destroyedNodes));
            } catch (Exception z) {
                logger.error("couldnt clean it up");
            }
        }

        if (results.isEmpty()) {
            throw new PortalServiceException(
                    "An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
                            + "a smaller virtual machine",
                            "Please report it to " +getAdminEmail()+".");
        } else {
            result = results.iterator().next();
        }

        logger.trace(String.format("Successful launch from service '%4$s' for job %3$s image '%1$s' type '%2$s'", job.getComputeVmId(), job.getComputeInstanceType(), job.getId(), this.getId()));

        return result.getId();
    }

    /**
     * Makes a request that the VM started by job be terminated
     *
     * @param job
     *            The job whose execution should be terminated
     */
    @Override
    public void terminateJob(CloudJob job) {
        computeService.destroyNode(job.getComputeInstanceId());
    }

    @Override
    public ComputeType[] getAvailableComputeTypes(String machineImageId) throws PortalServiceException {
        String imageId = machineImageId.split("/")[1];
        
        Image image = imageApi.get(imageId);

        return getAvailableComputeTypes(null, image.getMinRam(), image.getMinDisk());
    }

    /**
     * An array of compute types that are available through this compute service
     */
    @Override
    public ComputeType[] getAvailableComputeTypes(Integer minimumVCPUs, Integer minimumRamMB, Integer minimumRootDiskGB) {
        Set<? extends Hardware> hardwareSet = computeService.listHardwareProfiles();

        List<ComputeType> computeTypes = new ArrayList<>();

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
     * Will attempt to tail and return the last {@code numLines} from the given servers console.
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
        String computeInstanceId = job.getComputeInstanceId();
        if (computeInstanceId == null) {
            return null;
        }

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
        if (StringUtils.isEmpty(job.getComputeInstanceId())) {
            throw new PortalServiceException("No compute instance ID has been set");
        }

        try {
            NodeMetadata md = computeService.getNodeMetadata(job.getComputeInstanceId());
            if (md == null) {
                return InstanceStatus.Missing;
            }

            Status status = md.getStatus();
            switch (status) {
            case PENDING:
                return InstanceStatus.Pending;
            case RUNNING:
                return InstanceStatus.Running;
            default:
                return InstanceStatus.Missing;
            }
        } catch (Exception ex) {
            throw new PortalServiceException("Error fetching node metadata", ex);
        }
    }
}
