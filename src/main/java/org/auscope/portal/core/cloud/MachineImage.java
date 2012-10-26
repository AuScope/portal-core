package org.auscope.portal.core.cloud;

import java.io.Serializable;

/**
 * Represents a single virtual machine image that can be used for spawning worker instances.
 *
 * Contains descriptive information about the image itself which will be shown to a user.
 *
 * @author Josh Vote
 *
 */
public class MachineImage implements Serializable {
    /** The unique id of the cloud image - will be used for spawning instances of this image*/
    private String imageId;
    /** Descriptive short name of this image*/
    private String name;
    /** Longer description of this image*/
    private String description;
    /** (Possibly empty) List of descriptive keywords for this image*/
    private String[] keywords;

    /**
     * Creates a new VglMachineImage object
     * @param imageId
     */
    public MachineImage(String imageId) {
        super();
        this.imageId = imageId;
        this.name = imageId;
        this.keywords = new String[0];
    }

    /**
     * Descriptive short name of this image
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Descriptive short name of this image
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Longer description of this image
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Longer description of this image
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * (Possibly empty) List of descriptive keywords for this image
     * @return
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * (Possibly empty) List of descriptive keywords for this image
     * @param keywords
     */
    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    /**
     * The unique id of the cloud image - will be used for spawning instances of this image
     * @return
     */
    public String getImageId() {
        return imageId;
    }

    @Override
    public String toString() {
        return "MachineImage [imageId=" + imageId + ", name=" + name + "]";
    }
}
