/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.core.cloud;

import java.io.Serializable;

/**
 * Simple bean class that stores information about a file in cloud storage.
 *
 * @author Cihan Altinay
 * @author Joshua Vote
 */
public class CloudFileInformation implements Serializable {

    /**
     * Generated 2012-06-07
     */
    private static final long serialVersionUID = -2300795656821477004L;
    /** The file size in bytes */
    private long size;
    /** cloud storage key */
    private String cloudKey = "";
    /**
     * URL where the file can be accessed by anyone (only valid if file is publicly readable)
     */
    private String publicUrl = "";
    /**
     * The hash information of the currently stored file (implementation depends on cloud provider). Can be null/empty
     */
    private String fileHash = "";

    /**
     * Constructor with name and size
     */
    public CloudFileInformation(String cloudKey, long size, String publicUrl) {
        this(cloudKey, size, publicUrl, null);
    }

    /**
     * Constructor with name and size
     */
    public CloudFileInformation(String cloudKey, long size, String publicUrl, String fileHash) {
        this.cloudKey = cloudKey;
        this.size = size;
        this.publicUrl = publicUrl;
        this.fileHash = fileHash;
    }

    /**
     * Returns the filename.
     *
     * @return The filename.
     */
    public String getName() {
        String[] keyParts = cloudKey.split("/");
        return keyParts[keyParts.length - 1];
    }

    /**
     * Returns the file size.
     *
     * @return The file size in bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the underlying cloud key representing this file
     *
     * @return
     */
    public String getCloudKey() {
        return cloudKey;
    }

    /**
     * Sets the underlying cloud key representing this file
     *
     * @param cloudKey
     */
    public void setCloudKey(String cloudKey) {
        this.cloudKey = cloudKey;
    }

    /**
     * Gets the public URL where this file can be accessed (assuming the file has its ACL set to public read)
     *
     * @return
     */
    public String getPublicUrl() {
        return publicUrl;
    }

    /**
     * Sets the public URL where this file can be accessed (assuming the file has its ACL set to public read)
     *
     * @param publicUrl
     */
    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    /**
     * The hash information of the currently stored file (implementation depends on cloud provider). Can be null/empty
     * @return
     */
    public String getFileHash() {
        return fileHash;
    }

    /**
     * The hash information of the currently stored file (implementation depends on cloud provider). Can be null/empty
     * @param hash
     */
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

}
