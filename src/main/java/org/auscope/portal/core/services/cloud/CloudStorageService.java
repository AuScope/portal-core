package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.InputStreamMap;
import org.jclouds.blobstore.KeyNotFoundException;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.ContentMetadata;
import org.jclouds.rest.AuthorizationException;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Service for providing storage of objects (blobs) in a cloud using the JClouds library
 *
 * @author Josh Vote
 *
 */
public class CloudStorageService {

    /** The bucket name used when no bucket is specified*/
    public static final String DEFAULT_BUCKET = "portal-core-storage-service";

    private final Log log = LogFactory.getLog(getClass());

    protected BlobStoreContextFactory blobStoreContextFactory;
    /** Prefix to apply to any job files stored (will be appended with job id) - defaults to hostname*/
    protected String jobPrefix;
    /** Whether security certs are required to strictly match the host*/
    protected boolean relaxHostName = false;

    /** Username credential for accessing the storage service*/
    private String accessKey;
    /** Password credentials for accessing the storage service*/
    private String secretKey;
    /** A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'*/
    private String provider;
    /** The URL endpoint for the cloud storage service*/
    private String endpoint;

    /** The unique ID for this service - use it for distinguishing this service from other instances of this class - can be null or empty*/
    private String id;

    /**
     * The bucket that this service will access - defaults to DEFAULT_BUCKET
     */
    private String bucket = DEFAULT_BUCKET;

    /**
     * Creates a new instance
     * @param endpoint The URL endpoint for the cloud storage service
     * @param provider A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey Username credential for accessing the storage service
     * @param secretKey Password credentials for accessing the storage service
     */
    public CloudStorageService(String endpoint, String provider, String accessKey, String secretKey) {
        this(endpoint, provider, accessKey, secretKey, new BlobStoreContextFactory());
    }

    /**
     * Creates a new instance
     * @param endpoint The URL endpoint for the cloud storage service
     * @param provider A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @param accessKey Username credential for accessing the storage service
     * @param secretKey Password credentials for accessing the storage service
     */
    public CloudStorageService(String endpoint, String provider, String accessKey, String secretKey, BlobStoreContextFactory blobStoreContextFactory) {
        super();
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.provider = provider;
        this.blobStoreContextFactory = blobStoreContextFactory;
        try {
            this.jobPrefix = "job-" + InetAddress.getLocalHost().getHostName() + "-";
        } catch (UnknownHostException e) {
            this.jobPrefix = "job-";
            log.error("Unable to lookup hostname. Defaulting prefix to " + this.jobPrefix, e);
        }
    }

    /**
     * Whether security certs are required to strictly match the host
     * @return
     */
    public boolean isRelaxHostName() {
        return relaxHostName;
    }

    /**
     * Whether security certs are required to strictly match the host
     * @param relaxHostName
     */
    public void setRelaxHostName(boolean relaxHostName) {
        this.relaxHostName = relaxHostName;
    }

    /**
     * Prefix to apply to any job files stored (will be appended with job id)
     * @return
     */
    public String getJobPrefix() {
        return jobPrefix;
    }

    /**
     * Prefix to apply to any job files stored (will be appended with job id)
     * @param jobPrefix
     */
    public void setJobPrefix(String jobPrefix) {
        this.jobPrefix = jobPrefix;
    }

    /**
     * The unique ID for this service - use it for distinguishing this service from other instances of this class - can be null or empty
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The unique ID for this service - use it for distinguishing this service from other instances of this class - can be null or empty
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The bucket where the data will be stored
     * @return
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * The bucket where the data will be stored
     * @param bucket
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * Username credential for accessing the storage service
     * @return
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Password credential for accessing the storage service
     * @return
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * A unique identifier identifying the type of storage API used to store this job's files - eg 'swift'
     * @return
     */
    public String getProvider() {
        return provider;
    }

    /**
     * The URL endpoint for the cloud storage service
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Generates a BlobStoreContext, configured
     * @param job
     * @return
     */
    protected BlobStoreContext getBlobStoreContextForJob(CloudJob job) {
        Properties properties = new Properties();
        properties.setProperty(String.format("%1$s.endpoint", provider), endpoint);
        properties.setProperty("jclouds.relax-hostname", relaxHostName ? "true" : "false");
        return blobStoreContextFactory.createContext(provider, accessKey, secretKey, ImmutableSet.<Module>of(), properties);
    }

    /**
     * Utility for calculating an appropriate base cloud key for storing this jobs files
     * @param job
     * @return
     */
    public String generateBaseKey(CloudJob job) {
        String baseKey = String.format("%1$s%2$s-%3$010d", jobPrefix, job.getUser(), job.getId());
        baseKey = baseKey.replaceAll("[^a-zA-Z0-9_\\-]", "_"); //get rid of some nasty characters
        return baseKey;
    }

    /**
     * Gets the preconfigured base key for a job. If the job doesn't have a base key, one will be generated.
     * @param job Will have its baseKey parameter set if it's null
     * @return
     */
    protected String jobToBaseKey(CloudJob job) {
        if (job.getStorageBaseKey() == null) {
            job.setStorageBaseKey(generateBaseKey(job));
        }

        return job.getStorageBaseKey();
    }

    /**
     * Utility to extract file size from a StorageMetadata interface
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
     * Utility for accessing the InputStreamMap targeted towards a single CloudJob
     * @param job
     * @return
     */
    protected InputStreamMap jobToInputStreamMap(CloudJob job) {
        String baseKey = jobToBaseKey(job);

        log.debug(String.format("Attempting to open a InputStreamMap for bucket '%1$s' with base key '%2$s'", bucket, baseKey));

        BlobStoreContext bsc = getBlobStoreContextForJob(job);
        ListContainerOptions lco = ListContainerOptions.Builder.inDirectory(baseKey);
        return bsc.createInputStreamMap(bucket,lco);
    }

    /**
     * Gets the input stream for a job file identified by key.
     *
     * Ensure the resulting InputStream is closed
     *
     * @param job The job whose storage space will be queried
     * @param key The file name (no prefixes)
     * @return
     * @throws PortalServiceException
     */
    public InputStream getJobFile(CloudJob job, String key) throws PortalServiceException {
        try {
            InputStreamMap map = jobToInputStreamMap(job);
            return map.get(key);
        } catch (Exception ex) {
            log.error(String.format("Unable to get job file '%1$s' for job %2$s:", key, job));
            log.debug("error:", ex);
            throw new PortalServiceException(null, "Error retriving output file details", ex);
        }
    }

    /**
     * Gets information about every file in the job's cloud storage space
     * @param job The job whose storage space will be queried
     * @return
     * @throws PortalServiceException
     */
    public CloudFileInformation[] listJobFiles(CloudJob job) throws PortalServiceException {
        InputStreamMap map = jobToInputStreamMap(job);
        CloudFileInformation[] fileDetails = new CloudFileInformation[map.size()];
        Iterable<? extends StorageMetadata> fileMetaDataList = map.list();

        try {
            int i = 0;
            for (StorageMetadata fileMetadata : fileMetaDataList) {
                fileDetails[i++] = new CloudFileInformation(
                        fileMetadata.getName(), getFileSize(fileMetadata),
                        fileMetadata.getUri().toString());
            }

            return fileDetails;
        } catch (Exception ex) {
            log.error("Unable to list files for job:" + job.toString());
            log.debug("error:", ex);
            throw new PortalServiceException(null, "Error retriving output file details", ex);
        }
    }

    /**
     * Uploads an array of local files into the specified job's storage space
     * @param job The job whose storage space will be used
     * @param files The local files to upload
     * @throws PortalServiceException
     */
    public void uploadJobFiles(CloudJob job, File[] files) throws PortalServiceException {
        try {
            InputStreamMap map = jobToInputStreamMap(job);
            for (File file : files) {
                map.putFile(file.getName(), file);
                log.debug(file.getName() + " uploaded to '" + bucket + "' container");
            }
        } catch (AuthorizationException ex) {
            log.error("Storage credentials are not valid for job: " + job, ex);
            throw new PortalServiceException("Storage credentials are not valid.", "Please provide valid storage credentials.");
        } catch (KeyNotFoundException ex) {
            log.error("Storage container does not exist for job: " + job, ex);
            throw new PortalServiceException("Storage container does not exist.", "Please provide a valid storage container.");
        } catch (Exception ex) {
            log.error("Unable to upload files for job: " + job, ex);
            throw new PortalServiceException("An unexpected error has occurred while uploading file(s) to S3 storage.", "Please report it to cg-admin@csiro.au.");
        }
    }

    /**
     * Deletes all files including the container or directory for the specified job
     * @param job The whose storage space will be deleted
     * @throws PortalServiceException
     */
    public void deleteJobFiles(CloudJob job) throws PortalServiceException {
        BlobStoreContext bsc = null;
        try {
            //Remove all files
            InputStreamMap map = jobToInputStreamMap(job);
            CloudFileInformation[] files = listJobFiles(job);
            if (files != null) {
                for (CloudFileInformation file : files) {
                    map.remove(file.getName());
                }
            }
            //Remove the job storage base key (directory) from the storage bucket
            bsc = getBlobStoreContextForJob(job);
            bsc.getBlobStore().deleteDirectory(bucket, job.getStorageBaseKey());
        } catch (Exception ex) {
            log.error("Error in removing job files or storage key.", ex);
            throw new PortalServiceException(null, "An unexpected error has occurred while removing job files from S3 storage", ex);
        } finally {
            if (bsc != null) {
                bsc.close();
            }
        }
    }
}