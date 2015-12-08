package org.auscope.portal.core.cloud;

import java.io.File;

/**
 * Represents a local file (stored somewhere on the file system) that belongs to a job.
 * 
 * @author Joshua
 *
 */
public class StagedFile {
    /** The job that owns this staged file */
    private StagedFileOwner owner;
    /** The name of this staged file (unique per job) */
    private String name;
    /** can be null - the underlying reference to the HDD where this file is staged */
    private File file;

    /**
     * Creates a new instance
     * 
     * @param owner
     *            The job that owns this staged file
     * @param name
     *            The name of this staged file (unique per job)
     * @param file
     *            can be null - the underlying reference to the HDD where this file is staged
     */
    public StagedFile(StagedFileOwner owner, String name, File file) {
        super();
        this.owner = owner;
        this.name = name;
        this.file = file;
    }

    /**
     * The job that owns this staged file
     * 
     * @return
     */
    public StagedFileOwner getOwner() {
        return owner;
    }

    /**
     * The job that owns this staged file
     * 
     * @param owner
     */
    public void setOwner(StagedFileOwner owner) {
        this.owner = owner;
    }

    /**
     * The name of this staged file (unique per job)
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The name of this staged file (unique per job)
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * can be null - the underlying reference to the HDD where this file is staged
     * 
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * can be null - the underlying reference to the HDD where this file is staged
     * 
     * @param file
     */
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "StagedFile [owner=" + owner + ", name=" + name + "]";
    }

    /**
     * Returns true if obj is a StagedFile with equal name AND owner
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StagedFile) {
            return owner.equals(((StagedFile) obj).owner) && name.equals(((StagedFile) obj).name);
        }

        return false;
    }

    /**
     * Generates a hashcode based on owner and name
     */
    @Override
    public int hashCode() {
        return owner.hashCode() ^ name.hashCode();
    }

}
