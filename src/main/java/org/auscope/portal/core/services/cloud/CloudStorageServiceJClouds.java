package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
public class CloudStorageServiceJClouds extends CloudStorageService {

    private final Log log = LogFactory.getLog(getClass());

    private boolean relaxHostName;

    private boolean stripExpectHeader;

    private boolean requireSts=false;

    /**
     * The region identifier string for this service (if any). Can be
     * null/empty. Currently this field is NON functional, it is only for
     * descriptive purposes due to limitations in JClouds.
     */
    private String regionName;

    /** Username credential for accessing the storage service */
    private String accessKey;
    /** Password credentials for accessing the storage service */
    private String secretKey;

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
    public void setRequireSts(boolean requireSts) {
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
    public CloudStorageServiceJClouds(String provider, String accessKey, String secretKey) {
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
    public CloudStorageServiceJClouds(String endpoint, String provider, String accessKey, String secretKey) {
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
    public CloudStorageServiceJClouds(String endpoint, String provider, String accessKey, String secretKey,
            boolean relaxHostName) {
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
    public CloudStorageServiceJClouds(String endpoint, String provider, String accessKey, String secretKey, String regionName) {
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
    public CloudStorageServiceJClouds(String endpoint, String provider, String accessKey, String secretKey, String regionName,
            boolean relaxHostName) {
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
    public CloudStorageServiceJClouds(String endpoint, String provider, String accessKey, String secretKey, String regionName,
            boolean relaxHostName, boolean stripExpectHeader) {
        super(endpoint, provider);

        this.relaxHostName=relaxHostName;
        this.stripExpectHeader=stripExpectHeader;
        this.regionName=regionName;
        
    }

    public BlobStoreContext getBlobStoreContext(String arn, String clientSecret) throws PortalServiceException {
        Properties properties = new Properties();
        properties.setProperty("jclouds.relax-hostname", relaxHostName ? "true" : "false");
        properties.setProperty("jclouds.strip-expect-header", stripExpectHeader ? "true" : "false");

        if (regionName != null) {
            properties.setProperty("jclouds.region", regionName);
        }

        if(! TextUtil.isNullOrEmpty(arn)) {
            ContextBuilder builder = ContextBuilder.newBuilder("sts");
            if(accessKey!=null && secretKey!=null)
                builder.credentials(accessKey, secretKey);

            try (STSApi api = builder.buildApi(STSApi.class)) {
                AssumeRoleOptions assumeRoleOptions = new AssumeRoleOptions().durationSeconds(3600)
                        .externalId(clientSecret);
                final UserAndSessionCredentials credentials = api.assumeRole(arn, "anvgl", assumeRoleOptions);

                Supplier<Credentials> credentialsSupplier = new Supplier<Credentials>() {
                    @Override
                    public Credentials get() {
                        return credentials.getCredentials();
                    }
                };

                ContextBuilder builder2 = ContextBuilder.newBuilder("aws-s3").overrides(properties)
                        .credentialsSupplier(credentialsSupplier);

                if (getEndpoint() != null) {
                    builder2.endpoint(getEndpoint());
                }

                return builder2.buildView(BlobStoreContext.class);
            } catch (IOException e) {
                throw new PortalServiceException(e.getMessage(), e);
            }
        } else {
            if(isRequireSts())
                throw new PortalServiceException("AWS cross account access is required, but not configured");

            ContextBuilder builder = ContextBuilder.newBuilder(getProvider()).overrides(properties);

            if (accessKey != null && secretKey != null)
                builder.credentials(accessKey, secretKey);

            if (getEndpoint() != null) {
                builder.endpoint(getEndpoint());
            }

            return builder.build(BlobStoreContext.class);
        }
    }


    /**
     * Utility to extract file size from a StorageMetadata interface
     *
     * @param smd
     * @return
     */
    protected Long getFileSize(StorageMetadata smd) {
        if (smd instanceof BlobMetadataImpl) {
            ContentMetadata cmd = ((BlobMetadataImpl) smd).getContentMetadata();
            return cmd.getContentLength();
        } else if (smd instanceof MutableBlobMetadataImpl) {
            ContentMetadata cmd = ((MutableBlobMetadataImpl) smd).getContentMetadata();
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

    @Override
    public InputStream getJobFile(CloudFileOwner job, String myKey) throws PortalServiceException {
        String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            Blob blob = bs.getBlob(getBucket(job), keyForJobFile(job, myKey));
            return blob.getPayload().openStream();
        } catch (Exception ex) {
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
    private static CloudFileInformation metadataToCloudFile(StorageMetadata md) {
        //Skip objects that are not files
        if (md.getType() != StorageType.BLOB) {
            return null;
        }

        long fileSize = 1L;
        if (md instanceof BlobMetadataImpl) {
            ContentMetadata cmd = ((BlobMetadataImpl) md).getContentMetadata();
            fileSize = cmd.getContentLength();
        } else if (md instanceof MutableBlobMetadataImpl) {
            ContentMetadata cmd = ((MutableBlobMetadataImpl) md).getContentMetadata();
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
    @Override
    public CloudFileInformation getJobFileMetadata(CloudFileOwner job, String myKey) throws PortalServiceException {
        String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            StorageMetadata md = bs.blobMetadata(getBucket(job), keyForJobFile(job, myKey));
            return metadataToCloudFile(md);
        } catch (Exception ex) {
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
    @Override
    public CloudFileInformation[] listJobFiles(CloudFileOwner job) throws PortalServiceException {
        String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            String baseKey = generateBaseKey(job);

            String bucketName = getBucket(job);

            //Paging is a little awkward - this list method may return an incomplete list requiring followup queries
            PageSet<? extends StorageMetadata> currentMetadataPage = bs.list(bucketName, ListContainerOptions.Builder.inDirectory(baseKey));
            String nextMarker = null;
            List<CloudFileInformation> jobFiles = new ArrayList<>();
            do {
                if (nextMarker != null) {
                    currentMetadataPage = bs.list(bucketName, ListContainerOptions.Builder
                            .inDirectory(baseKey)
                            .afterMarker(nextMarker));
                }

                //Turn our StorageMetadata objects into simpler CloudFileInformation objects
                for (StorageMetadata md : currentMetadataPage) {
                    CloudFileInformation info = metadataToCloudFile(md);
                    if (info != null) {
                        jobFiles.add(info);
                    }
                }

                nextMarker = currentMetadataPage.getNextMarker();
            } while (nextMarker != null);

            return jobFiles.toArray(new CloudFileInformation[jobFiles.size()]);
        } catch (Exception ex) {
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
    @Override
    public void uploadJobFiles(CloudFileOwner job, File[] files) throws PortalServiceException {
        String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);

        try {
            BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();

            String bucketName = getBucket(job);
            bs.createContainerInLocation(null, bucketName);
            for (File file : files) {

                Blob newBlob = bs.blobBuilder(keyForJobFile(job, file.getName()))
                        .payload(Files.asByteSource(file))
                        .contentLength(file.length())
                        .build();
                bs.putBlob(bucketName, newBlob);

                log.debug(file.getName() + " uploaded to '" + bucketName + "' container");
            }
        } catch (AuthorizationException ex) {
            log.error("Storage credentials are not valid for job: " + job, ex);
            throw new PortalServiceException("Storage credentials are not valid.",
                    "Please provide valid storage credentials.");
        } catch (KeyNotFoundException ex) {
            log.error("Storage container does not exist for job: " + job, ex);
            throw new PortalServiceException("Storage container does not exist.",
                    "Please provide a valid storage container.");
        } catch (Exception ex) {
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
    @Override
    public void deleteJobFiles(CloudFileOwner job) throws PortalServiceException {
        String arn = job.getProperty(CloudJob.PROPERTY_STS_ARN);
        String clientSecret = job.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);
        try {
            BlobStore bs = getBlobStoreContext(arn, clientSecret).getBlobStore();
            bs.deleteDirectory(getBucket(job), jobToBaseKey(job));
        } catch (Exception ex) {
            log.error("Error in removing job files or storage key.", ex);
            throw new PortalServiceException(
                    "An unexpected error has occurred while removing job files from S3 storage", ex);
        }
    }
}
