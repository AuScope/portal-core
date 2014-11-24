package org.auscope.portal.core.services.cloud;

import static com.google.common.base.Predicates.not;
import static org.jclouds.compute.predicates.NodePredicates.RUNNING;
import static org.jclouds.compute.predicates.NodePredicates.TERMINATED;

import static org.jclouds.compute.predicates.NodePredicates.inGroup;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
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
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Processor;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.Volume;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.ec2.reference.EC2Constants;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.zonescoped.AvailabilityZone;
import org.jclouds.openstack.nova.v2_0.extensions.AvailabilityZoneApi;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Service class wrapper for interacting with a remote cloud compute service using
 * CloudJob objects.
 *
 * @author Josh Vote
 */
public class CloudComputeService {

    public enum ProviderType {
        /** Connect to an Openstack instance via the Keystone Identity service*/
        NovaKeystone,
        /** Connect to an Openstack instance via the emulated EC2 endpoint*/
        NovaEc2,
    }

    private final Log logger = LogFactory.getLog(getClass());

    private ComputeService computeService;
    private ComputeServiceContext context;
    private NovaApi lowLevelApi;
    
    private Predicate<NodeMetadata> terminateFilter;
    
    /** Unique ID for distinguishing instances of this class - can be null*/
    private String id;
    /** A short descriptive name for human identification of this service*/
    private String name;
    /** What type of cloud service are we communicating with */
    private ProviderType provider;


    /** A group name that all jobs will be assigned to*/
    private String groupName = "portal-cloud-compute-service";

    /** An array of images that are available through this compute service*/
    private MachineImage[] availableImages = new MachineImage[0];

    /**
     * Creates a new instance with the specified credentials
     * @param endpoint (URL) The location of the Compute (Nova) service
     * @param accessKey The Compute Access key (user name)
     * @param secretKey The Compute Secret key (password)
     */
    public CloudComputeService(ProviderType provider, String endpoint, String accessKey, String secretKey) {
        this(provider, endpoint, accessKey, secretKey, null);
    }

    /**
     * Creates a new instance with the specified credentials
     * @param endpoint (URL) The location of the Compute (Nova) service
     * @param accessKey The Compute Access key (user name)
     * @param secretKey The Compute Secret key (password)
     */
    public CloudComputeService(ProviderType provider, String endpoint, String accessKey, String secretKey, String apiVersion) {
        Properties overrides = new Properties();

        String typeString = "";
        switch (provider) {
        case NovaEc2:
            typeString = "openstack-nova-ec2";
            overrides.put(EC2Constants.PROPERTY_EC2_AUTO_ALLOCATE_ELASTIC_IPS, "false");
            break;
        case NovaKeystone:
            typeString = "openstack-nova";
            break;
        default:
            throw new IllegalArgumentException("Unsupported provider: " + provider.name());
        }
        this.provider = provider;

        ContextBuilder b = ContextBuilder.newBuilder(typeString)
                .endpoint(endpoint)
                .overrides(overrides)
                .credentials(accessKey, secretKey);

        if (apiVersion != null) {
            b.apiVersion(apiVersion);
        }

        //Terry -just in case...
        this.lowLevelApi = b.buildApi(NovaApi.class);

        this.context = b.buildView(ComputeServiceContext.class);
        this.computeService = this.context.getComputeService();
        
        this.terminateFilter = Predicates.and(not(TERMINATED), not(RUNNING), inGroup(groupName));

    }

    public CloudComputeService(ProviderType provider, ComputeService computeService, NovaApi lowLevelApi, Predicate<NodeMetadata> terminPredicate) {
        this.provider = provider;
        this.computeService = computeService;
        this.lowLevelApi = lowLevelApi;
        this.terminateFilter = terminPredicate;
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

    /** A group name that all jobs will be assigned to*/
    public String getGroupName() {
        return groupName;
    }

    /** A group name that all jobs will be assigned to*/
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

        //We have different template options depending on provider
        TemplateOptions options = null;
        Set<? extends NodeMetadata> results = Collections.emptySet();
        NodeMetadata result;

        if (provider == ProviderType.NovaEc2) {
            options = ((EC2TemplateOptions) computeService.templateOptions())
            .keyPair("vgl-developers")
            .userData(userDataString.getBytes(Charset.forName("UTF-8")));


            Template template = computeService.templateBuilder()
                    .imageId(job.getComputeVmId())
                    .hardwareId(job.getComputeInstanceType())
                    .options(options)
                    .build();

            //Start up the job, we should have exactly 1 node start
            try {
                results = computeService.createNodesInGroup(groupName, 1, template);
            } catch (RunNodesException e) {
                logger.error(String.format("An unexpected error '%1$s' occured while executing job '%2$s'", e.getMessage(), job));
                logger.debug("Exception:", e);
                throw new PortalServiceException("An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
                        + "a smaller virtual machine", "Please report it to cg-admin@csiro.au : " + e.getMessage(),e);
            }
            if (results.isEmpty()) {
                logger.error("JClouds returned an empty result set. Treating it as job failure.");
                throw new PortalServiceException("Unable to start compute node due to an unknown error, no nodes returned");
            }
            result = results.iterator().next();

        }
        else {
        	
        	//Brute force anyone?
            for (String location: lowLevelApi.getConfiguredZones()) {
	    		Optional<? extends AvailabilityZoneApi> serverApi = lowLevelApi.getAvailabilityZoneApi(location);
	    		Iterable<? extends AvailabilityZone> zones = serverApi.get().list();

	    		for (AvailabilityZone currentZone : zones) {
	    			if (currentZone.getName().startsWith("tasmania")) 
	    					continue;
	        	
		            options = ((NovaTemplateOptions)computeService.templateOptions())
		            .keyPairName("vgl-developers")
		            .availabilityZone(currentZone.getName())
		            .userData(userDataString.getBytes(Charset.forName("UTF-8")));
	
		            Template template = computeService.templateBuilder()
		                    .imageId(job.getComputeVmId())
		                    .hardwareId(job.getComputeInstanceType())
		                    .options(options)
		                    .build();
	
		            try {
		                results = computeService.createNodesInGroup(groupName, 1, template);
		                break;
		            } catch (RunNodesException e) {
		                logger.error(String.format("launch failed at '%1$s', '%2$s'", location, currentZone));
		                logger.debug(e.getMessage());
		                try {
		                	// FIXME: 
		                	// I think this could possibly delete EVERY NODE RUN from PORTAL-CORE...
		                	// JClouds is not very clever here - 
		                	// issue: how do you delete thing you didnt name and dont have an ID for??
		                	computeService.destroyNodesMatching(this.terminateFilter);
		                	logger.warn("cleaned it up");
		                }
		                catch (Exception z) {
		                    logger.warn("couldnt clean it up");
		                }
		                continue;
		            }
	    		}
        	}
            if (results.isEmpty()) {
            	//Now we have tried everything....
            	logger.error("run out of places to try...");
            	throw new PortalServiceException("An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
                    + "a smaller virtual machine", "Please report it to cg-admin@csiro.au ");
            }
            else {
            	result = results.iterator().next();
            }

        }

        return result.getId();
    }

	/**
     * Makes a request that the VM started by job be terminated
     * @param job The job whose execution should be terminated
     */
    public void terminateJob(CloudJob job) {
        computeService.destroyNode(job.getComputeInstanceId());
    }

    /**
     * An array of compute types that are available through this compute service
     */
    public ComputeType[] getAvailableComputeTypes() {
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

            computeTypes.add(ct);
        }

        return computeTypes.toArray(new ComputeType[computeTypes.size()]);
    }
}
