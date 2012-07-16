package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.InputStreamMap;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.ContentMetadata;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Service for providing storage of objects (blobs) in a cloud using the JClouds library
 *
 * @author Josh Vote
 *
 */
public class CloudStorageService {

    private final Log log = LogFactory.getLog(getClass());

    protected BlobStoreContextFactory blobStoreContextFactory;
    /** Prefix to apply to any job files stored (will be appended with job id)*/
    protected String jobPrefix = "job-";
    /** Whether security certs are required to strictly match the host*/
    protected boolean relaxHostName = false;

    /**
     * Creates a new instance of this class
     */
    public CloudStorageService() {
        this(new BlobStoreContextFactory());
    }

    /**
     * Creates a new instance of this class
     */
    public CloudStorageService(BlobStoreContextFactory blobStoreContextFactory) {
        super();
        this.blobStoreContextFactory = blobStoreContextFactory;
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
     * Generates a BlobStoreContext, configured
     * @param job
     * @return
     */
    protected BlobStoreContext getBlobStoreContextForJob(CloudJob job) {
        Properties properties = new Properties();
        properties.setProperty(String.format("%1$s.endpoint", job.getStorageProvider()), job.getStorageEndpoint());
        properties.setProperty("jclouds.relax-hostname", relaxHostName ? "true" : "false");
        return blobStoreContextFactory.createContext(job.getStorageProvider(), job.getStorageAccessKey(), job.getStorageSecretKey(), ImmutableSet.<Module>of(), properties);
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
        String bucket = job.getStorageBucket();

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
                log.debug(file.getName() + " uploaded to '" + job.getStorageBucket() + "' container");
            }
        } catch (Exception ex) {
            log.error("Unable to upload files for job:" + job.toString());
            log.debug("error:", ex);
            throw new PortalServiceException(null, "Error uploading job files", ex);
        }
    }

    /**
     * Deletes all files for the specified job
     * @param job The whose storage space will be deleted
     * @throws PortalServiceException
     */
    public void deleteJobFiles(CloudJob job) throws PortalServiceException {
        InputStreamMap map = jobToInputStreamMap(job);

        CloudFileInformation[] files = listJobFiles(job);
        if (files != null) {
            for (CloudFileInformation file : files) {
                map.remove(file.getName());
            }
        }
    }
}
