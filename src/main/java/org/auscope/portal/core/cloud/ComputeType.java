package org.auscope.portal.core.cloud;

import java.io.Serializable;

/**
 * A compute type (or compute flavor) is a simplified collection of virtual machine resources.
 * 
 * VM Images can be instantiated at a compute node using a VM type or flavor that contains these resources.
 * 
 * The values of this class (with the exception of id) are NOT authoritive and exist only for descriptive purposes
 * 
 * @author Josh Vote
 *
 */
public class ComputeType implements Serializable {
    private static final long serialVersionUID = 5143102635586852517L;

    /** Name of this compute type (valid only at parent compute provider) */
    private String id;
    /** Human readable short description of this compute type */
    private String description;
    /** How many virtual CPU's does this compute type offer */
    private Integer vcpus;
    /** How much RAM (roughly) in MB does this compute type offer */
    private Integer ramMB;
    /** How much does the root disk of this compute type offer (in GB) */
    private Integer rootDiskGB;
    /** How much does the Ephemeral disk of this compute type offer (in GB) */
    private Integer ephemeralDiskGB;

    public ComputeType(String id, int vcpus, int ramMB) {
        this.id=id;
        this.vcpus=vcpus;
        this.ramMB=ramMB;
    }
    /**
     * 
     * @param id
     *            Name of this compute type (valid only at parent compute provider)
     */
    public ComputeType(String id) {
        super();
        this.id = id;
    }

    /**
     * Human readable short description of this compute type
     * 
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Human readable short description of this compute type
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * How many virtual CPU's does this compute type offer
     * 
     * @return
     */
    public Integer getVcpus() {
        return vcpus;
    }

    /**
     * How many virtual CPU's does this compute type offer
     * 
     * @param vcpus
     */
    public void setVcpus(Integer vcpus) {
        this.vcpus = vcpus;
    }

    /**
     * How much RAM (roughly) in MB does this compute type offer
     * 
     * @return
     */
    public Integer getRamMB() {
        return ramMB;
    }

    /**
     * How much RAM (roughly) in MB does this compute type offer
     * 
     * @param ramMB
     */
    public void setRamMB(Integer ramMB) {
        this.ramMB = ramMB;
    }

    /**
     * How much does the root disk of this compute type offer (in GB)
     * 
     * @return
     */
    public Integer getRootDiskGB() {
        return rootDiskGB;
    }

    /**
     * How much does the root disk of this compute type offer (in GB)
     * 
     * @param rootDiskGB
     */
    public void setRootDiskGB(Integer rootDiskGB) {
        this.rootDiskGB = rootDiskGB;
    }

    /**
     * How much does the Ephemeral disk of this compute type offer (in GB)
     * 
     * @return
     */
    public Integer getEphemeralDiskGB() {
        return ephemeralDiskGB;
    }

    /**
     * How much does the Ephemeral disk of this compute type offer (in GB)
     * 
     * @param ephemeralDiskGB
     */
    public void setEphemeralDiskGB(Integer ephemeralDiskGB) {
        this.ephemeralDiskGB = ephemeralDiskGB;
    }

    /**
     * Name of this compute type (valid only at parent compute provider)
     * 
     * @return
     */
    public String getId() {
        return id;
    }

}
