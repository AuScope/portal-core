/**
 *
 */
package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudFileOwner;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.TextUtil;

/**
 * @author fri096
 *
 */
public abstract class CloudStorageService {
    private final Log log = LogFactory.getLog(getClass());

    abstract public InputStream getJobFile(CloudFileOwner job, String logFile) throws PortalServiceException;

    abstract public CloudFileInformation[] listJobFiles(CloudFileOwner job) throws PortalServiceException;

    abstract public void deleteJobFiles(CloudFileOwner job) throws PortalServiceException;

    abstract public void uploadJobFiles(CloudFileOwner curJob, File[] files) throws PortalServiceException;

    abstract public void uploadJobFile(CloudFileOwner curJob, String fileName, InputStream data) throws PortalServiceException;

    abstract public CloudFileInformation getJobFileMetadata(CloudFileOwner job, String fileName) throws PortalServiceException;

    /**
     * The region identifier string for this service (if any). Can be
     * null/empty. Currently this field is NON functional, it is only for
     * descriptive purposes due to limitations in JClouds.
     */
    private String regionName;
    /**
     * @return the regionName
     */

    public String getRegionName() {
        return regionName;
    }

    private String defaultBucket = DEFAULT_BUCKET;

    private String adminEmail = "cg-admin@csiro.au";

    /**
     * @param regionName the regionName to set
     */
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    /**
     * @return the accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @param accessKey the accessKey to set
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * @return the secretKey
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey the secretKey to set
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /** Username credential for accessing the storage service */
    private String accessKey;
    /** Password credentials for accessing the storage service */
    private String secretKey;

    /** The bucket name used when no bucket is specified */
    public static final String DEFAULT_BUCKET = "vgl";

    /**
     * Prefix to apply to any job files stored (will be appended with job id) -
     * defaults to hostname
     */
    protected String jobPrefix;

    /**
     * The unique ID for this service - use it for distinguishing this service
     * from other instances of this class - can be null or empty
     */
    private String id;
    /** A short descriptive name for human identification of this service */
    private String name;
    /**
     * The authentication version to use when connecting to this object store -
     * can be null or empty
     */
    private String authVersion;

    /**
     * A unique identifier identifying the type of storage API used to store
     * this job's files - eg 'swift'
     */
    private String provider;
    /** The URL endpoint for the cloud storage service */
    private String endpoint;

    public CloudStorageService(String endpoint, String provider, String regionName) {
        this.endpoint = endpoint;
        this.provider = provider;
        this.regionName= regionName;

        try {
            this.jobPrefix = "job-" + InetAddress.getLocalHost().getHostName() + "-";
        } catch (UnknownHostException e) {
            this.jobPrefix = "job-";
            log.error("Unable to lookup hostname. Defaulting prefix to " + this.jobPrefix, e);
        }
    }



    /**
     * @return the adminEmail
     */
    public String getAdminEmail() {
        return adminEmail;
    }

    /**
     * @param adminEmail
     *            the adminEmail to set
     */
    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    /**
     * A unique identifier identifying the type of storage API used to store
     * this job's files - eg 'swift'
     *
     * @return
     */
    public String getProvider() {
        return provider;
    }

    /**
     * The URL endpoint for the cloud storage service
     *
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Prefix to apply to any job files stored (will be appended with job id)
     *
     * @return
     */
    public String getJobPrefix() {
        return jobPrefix;
    }

    /**
     * Prefix to apply to any job files stored (will be appended with job id)
     *
     * @param jobPrefix
     */
    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    /**
     * The unique ID for this service - use it for distinguishing this service
     * from other instances of this class - can be null or empty
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The unique ID for this service - use it for distinguishing this service
     * from other instances of this class - can be null or empty
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Utility for accessing the correct bucket based on owner's configuration
     *
     * @param owner
     * @return
     */
    String getBucket(CloudFileOwner owner) {
        String ownerBucket = owner.getStorageBucket();
        if (TextUtil.isNullOrEmpty(ownerBucket)) {
            return defaultBucket;
        }

        return ownerBucket;
    }

    /**
     * The default bucket where the data will be stored (if the CloudFileOwners
     * don't define a storage bucket)
     *
     * @return
     */
    public String getBucket() {
        return defaultBucket;
    }

    /**
     * The default bucket where the data will be stored (if the CloudFileOwners
     * don't define a storage bucket)
     *
     * @param bucket
     */
    public void setBucket(String bucket) {
        this.defaultBucket = bucket;
    }

    /**
     * The authentication version to use when connecting to this object store -
     * can be null or empty
     *
     * @return
     */
    public String getAuthVersion() {
        return authVersion;
    }

    /**
     * The authentication version to use when connecting to this object store -
     * can be null or empty
     *
     * @param authVersion
     */
    public void setAuthVersion(String authVersion) {
        this.authVersion = authVersion;
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
     * Utility for allowing only whitelisted characters
     *
     * @param s
     * @return
     */
    private static String sanitise(String s, boolean allowDot) {
        if (allowDot) {
            return s.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
        } else {
            return s.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        }

    }

    /**
     * Utility for calculating an appropriate base cloud key for storing this
     * jobs files
     *
     * @param job
     * @return
     */
    public String generateBaseKey(CloudFileOwner job) {
        String baseKey = String.format("%1$s%2$s-%3$010d", jobPrefix, job.getUser(), job.getId());
        return sanitise(baseKey, false);
    }

    /**
     * Utility for generating the full path for a specific job file
     *
     * @param job
     *            The job whose storage space will be queried for
     * @param key
     *            The key of the file (local to job).
     * @return
     */
    public String keyForJobFile(CloudFileOwner job, String key) {
        return String.format("%1$s/%2$s", jobToBaseKey(job), sanitise(key, true));
    }

    /**
     * Gets the preconfigured base key for a job. If the job doesn't have a base
     * key, one will be generated.
     *
     * @param job
     *            Will have its baseKey parameter set if it's null
     * @return
     */
    protected String jobToBaseKey(CloudFileOwner job) {
        if (job.getStorageBaseKey() == null) {
            job.setStorageBaseKey(generateBaseKey(job));
        }

        return job.getStorageBaseKey();
    }
}
