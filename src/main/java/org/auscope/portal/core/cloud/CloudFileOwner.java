package org.auscope.portal.core.cloud;

/**
 * Implementors of this class are capable of "owning" a portion of cloud object storage.
 *
 * @see org.auscope.portal.core.services.cloud.CloudStorageService
 * @author Josh Vote
 *
 */
public interface CloudFileOwner {
    /**
     * A user name to associate with these files
     *
     * @return
     */
    public String getUser();

    /**
     * Unique ID identifying this owner
     *
     * @return
     */
    public Integer getId();

    /**
     * The key prefix for all files associated with this object in the specified storage bucket
     *
     * @return
     */
    public String getStorageBaseKey();

    /**
     * The key prefix for all files associated with this job in the specified storage bucket
     *
     * @param storageBaseKey
     *            The base key to set. Can be null/empty
     * @return
     */
    public void setStorageBaseKey(String baseKey);

    /**
     * Return job property
     * @param key
     *            The property key
     * @return the property value
     */
    public String getProperty(String key);

    /**
     * Returns the storage bucket to be used to store files. If null, no specific bucket is
     * required by this cloud file and the default bucket of the underlying service should be used.
     * @return The storage bucket or null
     */
    public String getStorageBucket();
}
