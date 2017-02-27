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
    /** The unique id of the cloud image - will be used for spawning instances of this image */
    private String imageId;
    /** Descriptive short name of this image */
    private String name;
    /** Longer description of this image */
    private String description;
    /** (Possibly empty) List of descriptive keywords for this image */
    private String[] keywords;
    /** The minimum root disk size (in GB) that this image can be run on. Null if this is N/A */
    private Integer minimumDiskGB;
    /** The (possibly null) run command that should be used to execute python scripts. If null, most providers will default to 'python'*/
    private String runCommand;

    /**
     * Creates a new VglMachineImage object
     *
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
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The (possibly null) run command that should be used to execute python scripts. If null, most providers will default to 'python'
     * @return
     */
    public String getRunCommand() {
        return runCommand;
    }

    /**
     * The (possibly null) run command that should be used to execute python scripts. If null, most providers will default to 'python'
     * @param runCommand
     */
    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    /**
     * Descriptive short name of this image
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Longer description of this image
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Longer description of this image
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * (Possibly empty) List of descriptive keywords for this image
     *
     * @return
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * (Possibly empty) List of descriptive keywords for this image
     *
     * @param keywords
     */
    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    /**
     * The unique id of the cloud image - will be used for spawning instances of this image
     *
     * @return
     */
    public String getImageId() {
        return imageId;
    }

    /** The minimum root disk size (in GB) that this image can be run on. Null if this is N/A */
    public Integer getMinimumDiskGB() {
        return minimumDiskGB;
    }

    /** The minimum root disk size (in GB) that this image can be run on. Null if this is N/A */
    public void setMinimumDiskGB(Integer minimumDiskGB) {
        this.minimumDiskGB = minimumDiskGB;
    }

    @Override
    public String toString() {
        return "MachineImage [imageId=" + imageId + ", name=" + name + "]";
    }
}
