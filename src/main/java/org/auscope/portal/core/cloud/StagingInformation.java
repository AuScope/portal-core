package org.auscope.portal.core.cloud;

/**
 * A simple POJO for storing all information about where a portal can stage in job files
 * 
 * @author Josh Vote
 *
 */
public class StagingInformation {
    private String stageInDirectory;

    /**
     * Creates a new instance
     * 
     * @param stageInDirectory
     *            Gets where the portal can add/remove directories with impunity for the purpose of staging in new job directories
     */
    public StagingInformation(String stageInDirectory) {
        super();
        this.stageInDirectory = stageInDirectory;
    }

    /**
     * Gets where the portal can add/remove directories with impunity for the purpose of staging in new job directories
     * 
     * @return
     */
    public String getStageInDirectory() {
        return stageInDirectory;
    }

    /**
     * Sets where the portal can add/remove directories with impunity for the purpose of staging in new job directories
     * 
     * @param stageInDirectory
     */
    public void setStageInDirectory(String stageInDirectory) {
        this.stageInDirectory = stageInDirectory;
    }
}
