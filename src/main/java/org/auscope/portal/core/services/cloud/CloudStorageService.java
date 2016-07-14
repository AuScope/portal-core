package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudFileOwner;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.TextUtil;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.KeyNotFoundException;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.domain.Credentials;
import org.jclouds.io.ContentMetadata;
import org.jclouds.rest.AuthorizationException;
import org.jclouds.sts.STSApi;
import org.jclouds.sts.domain.UserAndSessionCredentials;
import org.jclouds.sts.options.AssumeRoleOptions;

import com.google.common.base.Supplier;
import com.google.common.io.Files;

/**
 * Service for providing storage of objects (blobs) in a cloud using the JClouds library
 *
 * @author Josh Vote
 *
 */
public class CloudStorageService {

    /** The bucket name used when no bucket is specified */
    public static final String DEFAULT_BUCKET = "portal-core-storage-service";

    private final Log log = LogFactory.getLog(getClass());

    /** Prefix to apply to any job files stored (will be appended with job id) - defaults to hostname */
    protected String jobPrefix;

    /** The unique ID for this service - use it for distinguishing this service from other instances of this class - can be null or empty */
    private String id;
    /** A short descriptive name for human identification of this service */
    private String name;
    /** The authentication version to use when connecting to this object store - can be null or empty */
    private String authVersion;
    /**
     * The region identifier string for this service (if any). Can be null/empty. Currently this field is NON functional, it is only for descriptive purposes
     * due to limitations in JClouds.
     */
    private String regionName;
    /** Username credential for accessing the storage service */
    private String accessKey;
    /** Password credentials for accessing the storage service */
    private String secretKey;
    /** A unique identifier identifying the type of storage API used to store this job's files - eg 'swift' */
    private String provider;
    /** The URL endpoint for the cloud storage service */
    private String endpoint;

    /**
     * The bucket that this service will access - defaults to DEFAULT_BUCKET
     */
    private String defaultBucket = DEFAULT_BUCKET;

    private boolean relaxHostName;

    private boolean stripExpectHeader;

    private boolean requireSts=false;

    private String adminEmail = "cg-admin@csiro.au";

    /**
     * @return the adminEmail
     */
    public String getAdminEmail() {
        return adminEmail;
    }

    /**
     * @param adminEmail the adminEmail to set
     */
    public void setAdminEmail(final String adminEmail) {
        this.adminEmail = adminEmail;
    }

    /**
     * Returns whether AWS cross account authorization is mandatory.
     * @return whether AWS cross account authorization is mandatory.
     */
    public boolean isRequireSts() {
        return requireSts;
    }

    /**
     * Sets whether AWS cross account authorization is mandatory.
     * @param requireSts if true, AWS cross account authorization will be mandatory.
     */
    public void setRequireSts(final boolean requireSts) {
        this.requireSts = requireSts;
    }

    /**
     * Creates a new instance for connecting to the specified parameters
     *
     * @param endpoint
     *            The URL endpoint for the cloud storage service
     * @param provider
     *            A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey
     *            Username credential for accessing the storage service
     * @param secretKey
     *            Password credentials for accessing the storage service
     */
    public CloudStorageService(final String provider, final String accessKey, final String secretKey) {
        this(null, provider, accessKey, secretKey, null, false);
    }

    /**
     * Creates a new instance for connecting to the specified parameters
     *
     * @param endpoint
     *            The URL endpoint for the cloud storage service
     * @param provider
     *            A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey
     *            Username credential for accessing the storage service
     * @param secretKey
     *            Password credentials for accessing the storage service
     */
    public CloudStorageService(final String endpoint, final String provider, final String accessKey, final String secretKey) {
        this(endpoint, provider, accessKey, secretKey, null, false);
    }

    /**
     * Creates a new instance for connecting to the specified parameters
     *
     * @param endpoint
     *            The URL endpoint for the cloud storage service
     * @param provider
     *            A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey
     *            Username credential for accessing the storage service
     * @param secretKey
     *            Password credentials for accessing the storage service
     * @param relaxHostName
     *            Whether security certs are required to strictly match the host
     */
    public CloudStorageService(final String endpoint, final String provider, final String accessKey, final String secretKey,
            final boolean relaxHostName) {
        this(endpoint, provider, accessKey, secretKey, null, relaxHostName);
    }

    /**
     * Creates a new instance for connecting to the specified parameters
     *
     * @param endpoint
     *            The URL endpoint for the cloud storage service
     * @param provider
     *            A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey
     *            Username credential for accessing the storage service
     * @param secretKey
     *            Password credentials for accessing the storage service
     * @param regionName
     *            The region identifier string for this service (if any). Can be null/empty.
     */
    public CloudStorageService(final String endpoint, final String provider, final String accessKey, final String secretKey, final String regionName) {
        this(endpoint, provider, accessKey, secretKey, regionName, false);
    }

    /**
     * Creates a new instance for connecting to the specified parameters
     *
     * @param endpoint
     *            The URL endpoint for the cloud storage service
     * @param provider
     *            A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey
     *            Username credential for accessing the storage service
     * @param secretKey
     *            Password credentials for accessing the storage service
     * @param regionName
     *            The region identifier string for this service (if any). Can be null/empty.
     * @param relaxHostName
     *            Whether security certs are required to strictly match the host
     */
    public CloudStorageService(final String endpoint, final String provider, final String accessKey, final String secretKey, final String regionName,
            final boolean relaxHostName) {
        this(endpoint, provider, accessKey, secretKey, regionName, relaxHostName, false);
    }

    /**
     * Creates a new instance for connecting to the specified parameters
     *
     * @param endpoint
     *            The URL endpoint for the cloud storage service
     * @param provider
     *            A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey
     *            Username credential for accessing the storage service
     * @param secretKey
     *            Password credentials for accessing the storage service
     * @param regionName
     *            The region identifier string for this service (if any). Can be null/empty.
     * @param relaxHostName
     *            Whether security certs are required to strictly match the host
     * @param stripExpectHeader
     *            Whether to remove HTTP Expect header from requests; set to true for blobstores that do not support 100-Continue
     */
    public CloudStorageService(final String endpoint, final String provider, final String accessKey, final String secretKey, final String regionName,
            final boolean relaxHostName, final boolean stripExpectHeader) {
        super();

        this.endpoint = endpoint;
        this.provider = provider;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.regionName = regionName;
        this.relaxHostName=relaxHostName;
        this.stripExpectHeader=stripExpectHeader;

        try {
            this.jobPrefix = "job-" + InetAddress.getLocalHost().getHostName() + "-";
        } catch (final UnknownHostException e) {
            this.jobPrefix = "job-";
            log.error("Unable to lookup hostname. Defaulting prefix to " + this.jobPrefix, e);
        }
    }

    public BlobStoreContext getBlobStoreContext(final String arn, final String clientSecret) throws PortalServiceException {
        final Properties properties = new Properties();
        properties.setProperty("jclouds.relax-hostname", relaxHostName ? "true" : "false");
        properties.setProperty("jclouds.strip-expect-header", stripExpectHeader ? "true" : "false");

        if (regionName != null) {
            properties.setProperty("jclouds.region", regionName);
        }

        if(! TextUtil.isNullOrEmpty(arn)) {
            final ContextBuilder builder = ContextBuilder.newBuilder("sts");
            if(accessKey!=null && secretKey!=null)
                builder.credentials(accessKey, secretKey);

            try (STSApi api = builder.buildApi(STSApi.class)) {
                final AssumeRoleOptions assumeRoleOptions = new AssumeRoleOptions().durationSeconds(3600)
                        .externalId(clientSecret);
                final UserAndSessionCredentials credentials = api.assumeRole(arn, "anvgl", assumeRoleOptions);

                final Supplier<Credentials> credentialsSupplier = new Supplier<Credentials>() {
                    @Override
                    public Credentials get() {
                        return credentials.getCredentials();
                    }
                };

                final ContextBuilder builder2 = ContextBuilder.newBuilder("aws-s3").overrides(properties)
                        .credentialsSupplier(credentialsSupplier);

                if (this.endpoint != null) {
                    builder2.endpoint(this.endpoint);
                }

                return builder2.buildView(BlobStoreContext.class);
            } catch (IOException e) {
                throw new PortalServiceException(e.getMessage(), e);
            }
        } else {
            if(isRequireSts())
                throw new PortalServiceException("AWS cross account access is required, but not configured");

            final ContextBuilder builder = ContextBuilder.newBuilder(provider).overrides(properties);

            if (accessKey != null && secretKey != null)
                builder.credentials(accessKey, secretKey);

            if (this.endpoint != null) {
                builder.endpoint(this.endpoint);
            }

            return builder.build(BlobStoreContext.class);
        }
    }

    /**
     * Username credential for accessing the storage service
     *
     * @return
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Password credential for accessing the storage service
     *
     * @return
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
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
    public void setJobPrefix(final String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    /**
     * The unique ID for this service - use it for distinguishing this service from other instances of this class - can be null or empty
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The unique ID for this service - use it for distinguishing this service from other instances of this class - can be null or empty
     *
     * @param id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Utility for accessing the correct bucket based on owner's configuration
     * @param owner
     * @return
     */
    private String getBucket(final CloudFileOwner owner) {
        final String ownerBucket = owner.getStorageBucket();
        if (TextUtil.isNullOrEmpty(ownerBucket)) {
            return defaultBucket;
        }

        return ownerBucket;
    }

    /**
     * The default bucket where the data will be stored (if the CloudFileOwners don't define a storage bucket)
     *
     * @return
     */
    public String getBucket() {
        return defaultBucket;
    }

    /**
     * The default bucket where the data will be stored (if the CloudFileOwners don't define a storage bucket)
     *
     * @param bucket
     */
    public void setBucket(final String bucket) {
        this.defaultBucket = bucket;
    }

    /**
     * The authentication version to use when connecting to this object store - can be null or empty
     *
     * @return
     */
    public String getAuthVersion() {
        return authVersion;
    }

    /**
     * The authentication version to use when connecting to this object store - can be null or empty
     *
     * @param authVersion
     */
    public void setAuthVersion(final String authVersion) {
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
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * The region identifier string for this service (if any). Can be null/empty. Currently this field is NON functional, it is only for descriptive purposes
     * due to limitations in JClouds.
     *
     * @return
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * The region identifier string for this service (if any). Can be null/empty. Currently this field is NON functional, it is only for descriptive purposes
     * due to limitations in JClouds.
     *
     * @param regionName
     */
    public void setRegionName(final String regionName) {
        this.regionName = regionName;
    }

    /**
     * Utility for allowing only whitelisted characters
     *
     * @param s
     * @return
     */
    private static String sanitise(final String s) {
        return s.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    /**
     * Utility for calculating an appropriate base cloud key for storing this jobs files
     *
     * @param job
     * @return
     */
    public String generateBaseKey(final CloudFileOwner job) {
        final String baseKey = String.format("%1$s%2$s-%3$010d", jobPrefix, job.getUser(), job.getId());
        return sanitise(baseKey);
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
    public String keyForJobFile(final CloudFileOwner job, final String key) {
        return String.format("%1$s/%2$s", jobToBaseKey(job), key);
    }

    /**
     * Gets the preconfigured base key for a job. If the job doesn't have a base key, one will be generated.
     *
     * @param job
     *            Will have its baseKey parameter set if it's null
     * @return
     */
    protected String jobToBaseKey(final CloudFileOwner job) {
        if (job.getStorageBaseKey() == null) {
            job.setStorageBaseKey(generateBaseKey(job));
        }

        return job.getStorageBaseKey();
    }

    /**
     * Utility to extract file size from a StorageMetadata interface
     *
     * @param smd
     * @return
     */
    protected Long getFileSize(final StorageMetadata smd) {
        if (smd instanceof BlobMetadataImpl) {
            final ContentMetadata cmd = ((BlobMetadataImpl) smd).getContentMetadata();
            return cmd.getContentLength();
        } else if (smd instanceof MutableBlobMetadataImpl) {
            final ContentMetadata cmd = ((MutableBlobMetadataImpl) smd).getContentMetadata();
            return cmd.getContentLength();
        } else {
            return 1L;
        }
    }

    /**
     * Gets the input stream for a job file identified by key.
     *
     * Ensure the resulting InputStream is closed
     *
     * @param job
     *            The job whose storage space will be queried
     * @param key
     *            The file name (no prefixes)
     * @return
     * @throws PortalServiceException
     */

    public InputStream getJobFile(final CloudFileOwner job, final String myKey) throws PortalServiceException {
        final String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        final String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            final BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            final Blob blob = bs.getBlob(getBucket(job), keyForJobFile(job, myKey));
            return blob.getPayload().openStream();
        } catch (final Exception ex) {
            log.error(String.format("Unable to get job file '%1$s' for job %2$s:", myKey, job));
            log.debug("error:", ex);
            throw new PortalServiceException("Error retriving output file details", ex);
        }
    }

    /**
     * Converts JClouds StorageMetadata into simpler CloudFileInformation.
     * Returns null if the conversion is not possible
     * @param md
     * @return
     */
    private static CloudFileInformation metadataToCloudFile(final StorageMetadata md) {
        //Skip objects that are not files
        if (md.getType() != StorageType.BLOB) {
            return null;
        }

        long fileSize = 1L;
        if (md instanceof BlobMetadataImpl) {
            final ContentMetadata cmd = ((BlobMetadataImpl) md).getContentMetadata();
            fileSize = cmd.getContentLength();
        } else if (md instanceof MutableBlobMetadataImpl) {
            final ContentMetadata cmd = ((MutableBlobMetadataImpl) md).getContentMetadata();
            fileSize = cmd.getContentLength();
        }

        return new CloudFileInformation(md.getName(), fileSize, md.getUri().toString(), md.getETag());
    }

    /**
     * Gets the metadata for a job file identified by key.
     *
     * @param job
     *            The job whose storage space will be queried
     * @param key
     *            The file name (no prefixes)
     * @return
     * @throws PortalServiceException
     */
    public CloudFileInformation getJobFileMetadata(final CloudFileOwner job, final String myKey) throws PortalServiceException {
        final String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        final String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            final BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            final StorageMetadata md = bs.blobMetadata(getBucket(job), keyForJobFile(job, myKey));
            return metadataToCloudFile(md);
        } catch (final Exception ex) {
            log.error(String.format("Unable to get job file metadata '%1$s' for job %2$s:", myKey, job));
            log.debug("error:", ex);
            throw new PortalServiceException("Error retriving output file details", ex);
        }
    }

    /**
     * Gets information about every file in the job's cloud storage space
     *
     * @param job
     *            The job whose storage space will be queried
     * @return
     * @throws PortalServiceException
     */
    public CloudFileInformation[] listJobFiles(final CloudFileOwner job) throws PortalServiceException {
        final String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        final String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            final BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            final String baseKey = generateBaseKey(job);

            final String bucketName = getBucket(job);

            //Paging is a little awkward - this list method may return an incomplete list requiring followup queries
            PageSet<? extends StorageMetadata> currentMetadataPage = bs.list(bucketName, ListContainerOptions.Builder.inDirectory(baseKey));
            String nextMarker = null;
            final List<CloudFileInformation> jobFiles = new ArrayList<>();
            do {
                if (nextMarker != null) {
                    currentMetadataPage = bs.list(bucketName, ListContainerOptions.Builder
                            .inDirectory(baseKey)
                            .afterMarker(nextMarker));
                }

                //Turn our StorageMetadata objects into simpler CloudFileInformation objects
                for (final StorageMetadata md : currentMetadataPage) {
                    final CloudFileInformation info = metadataToCloudFile(md);
                    if (info != null) {
                        jobFiles.add(info);
                    }
                }

                nextMarker = currentMetadataPage.getNextMarker();
            } while (nextMarker != null);

            return jobFiles.toArray(new CloudFileInformation[jobFiles.size()]);
        } catch (final Exception ex) {
            log.error("Unable to list files for job:" + job.toString());
            log.debug("error:", ex);
            throw new PortalServiceException("Error retriving output file details", ex);
        }
    }

    /**
     * Uploads an array of local files into the specified job's storage space
     *
     * @param job
     *            The job whose storage space will be used
     * @param files
     *            The local files to upload
     * @throws PortalServiceException
     */
    public void uploadJobFiles(final CloudFileOwner job, final File[] files) throws PortalServiceException {
        final String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        final String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            final BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();

            final String bucketName = getBucket(job);
            bs.createContainerInLocation(null, bucketName);
            for (final File file : files) {

                final Blob newBlob = bs.blobBuilder(keyForJobFile(job, file.getName()))
                        .payload(Files.asByteSource(file))
                        .contentLength(file.length())
                        .build();
                bs.putBlob(bucketName, newBlob);

                log.debug(file.getName() + " uploaded to '" + bucketName + "' container");
            }
        } catch (final AuthorizationException ex) {
            log.error("Storage credentials are not valid for job: " + job, ex);
            throw new PortalServiceException("Storage credentials are not valid.",
                    "Please provide valid storage credentials.");
        } catch (final KeyNotFoundException ex) {
            log.error("Storage container does not exist for job: " + job, ex);
            throw new PortalServiceException("Storage container does not exist.",
                    "Please provide a valid storage container.");
        } catch (final Exception ex) {
            log.error("Unable to upload files for job: " + job, ex);
            throw new PortalServiceException("An unexpected error has occurred while uploading file(s) to storage.",
                    "Please report it to " + getAdminEmail()+".");
        }
    }

    /**
     * Deletes all files including the container or directory for the specified job
     *
     * @param job
     *            The whose storage space will be deleted
     * @throws PortalServiceException
     */
    public void deleteJobFiles(final CloudFileOwner job) throws PortalServiceException {
        final String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        final String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);
        try {
            final BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            bs.deleteDirectory(getBucket(job), jobToBaseKey(job));
        } catch (final Exception ex) {
            log.error("Error in removing job files or storage key.", ex);
            throw new PortalServiceException(
                    "An unexpected error has occurred while removing job files from S3 storage", ex);
        }
    }
}
