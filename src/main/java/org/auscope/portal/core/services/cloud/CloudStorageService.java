package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.InputStreamMap;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.ContentMetadata;

/**
 * Service for providing storage of objects (blobs) in a cloud
 *
 * @author Josh Vote
 *
 */
public class CloudStorageService {

    private final Log log = LogFactory.getLog(getClass());

    protected BlobStoreContext blobStoreContext;
    /** Prefix to apply to any job files stored (will be appended with job id)*/
    protected String jobPrefix = "job-";


    /**
     * Creates a new instance of this class
     * @param blobStoreContext The context used to power this service
     */
    public CloudStorageService(BlobStoreContext blobStoreContext) {
        super();
        this.blobStoreContext = blobStoreContext;
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
     * Utility to create the base key for a job
     * @param job
     * @return
     */
    protected String jobToBaseKey(CloudJob job) {
        String escapedId = job.getId().replace('/', '_');
        return jobPrefix + escapedId;
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

        ListContainerOptions lco = ListContainerOptions.Builder.inDirectory(baseKey);
        return blobStoreContext.createInputStreamMap(bucket,lco);
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
