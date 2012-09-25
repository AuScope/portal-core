package org.auscope.portal.core.cloud;

/**
 * Represents a local file (stored somewhere on the file system) that belongs to a job.
 * @author Joshua
 *
 */
public class StagedFile {
	/** The job that owns this staged file*/
	private CloudJob owner;
	/** The name of this staged file (unique per job)*/
	private String name;
	
	/**
	 * Creates a new instance
	 * @param owner The job that owns this staged file
	 * @param name The name of this staged file (unique per job)
	 */
	public StagedFile(CloudJob owner, String name) {
		super();
		this.owner = owner;
		this.name = name;
	}

	/**
	 * The job that owns this staged file
	 * @return
	 */
	public CloudJob getOwner() {
		return owner;
	}

	/**
	 * The job that owns this staged file
	 * @param owner
	 */
	public void setOwner(CloudJob owner) {
		this.owner = owner;
	}

	/**
	 * The name of this staged file (unique per job)
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * The name of this staged file (unique per job)
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
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
