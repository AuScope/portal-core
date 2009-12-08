/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

package org.auscope.gridtools;

/**
 * This interface defines the methods that must be implemented by any model
 * that retrieves information from the Grid. As long as the controller only
 * uses methods from this interface, models can be swapped at will without
 * affecting the rest of the program.
 * 
 * @author Darren Kidd
 */
public interface GridInfoInterface
{
    /*  Grid-wide lookups.
     ***********************/
    
    /**
     * Get an array containing the names of all the sites on the Grid.
     * 
     * @return An array of site names
     */
    public abstract String[] getAllSitesOnGrid();
    
    /**
     * Get an array containing the addresses of all the GridFTP servers on the 
     * Grid (one for each site).
     * 
     * @return An array of GridFTP server addresses
     */
    public abstract String[] getAllGridFTPServersOnGrid();
    
    /**
     * Get an array of all the different (unique) codes available on the Grid.
     * 
     * @return An array of available codes
     */
    public abstract String[] getAllCodesOnGrid();
    
    /**
     * Get an array of all the different versions of a Code available on the
     * Grid.
     * 
     * @param code The code to check
     * @return An array of version labels
     */
    public abstract String[] getAllVersionsOfCodeOnGrid(String code);

    /**
     * Get the status of all the sites in the Grid. The status refers to 
     * properties such as the number of CPUs, the number of used CPUs, the 
     * number of free job slots, and others. The information is returned in a
     * <code>SiteInfo</code> object.
     * 
     * @return An array of <code>SiteInfo</code> objects
     */
    public abstract SiteInfo[] getAllSitesStatus();
    
    
    /*  Site-specific look ups.
     ****************************/
    
    
    /**
     * Get all the codes at a particular site.
     * 
     * @param site The site to check
     * @return An array of codes
     */
    public abstract String[] getAllCodesAtSite(String site);
    
    /**
     * Get the names of all the queues at a particular site.
     * 
     * @param site The site to check
     * @return An array of job queues
     */
    public abstract String[] getQueueNamesAtSite(String site);
    
    /**
     * Get the names of all the clusters at a particular site.
     * 
     * @param site The site to check
     * @return An array of cluster names
     */
    public abstract String[] getClusterNamesAtSite(String site);
    
    /**
     * Get the address of the job manager at a particular site.
     * 
     * @param site The site to check
     * @return The address of the job manager
     */
    public abstract String getJobManagerAtSite(String site);
    
    /**
     * Get the address of the Gateway/NG2 GridFTP server for this site.
     * 
     * @param site The site to check
     * @return The address of the gateway
     */
    public abstract String getGatewayGridFTPServerAtSite(String site);
    
    /**
     * Get the address of the site's cluster GridFTP server. This is the 
     * working data location - typically mapped to <code>file:///</code>.
     * 
     * @param site The site to check
     * @return The <code>file:///</code> String.
     */
    public abstract String getClusterGridFTPServerAtSite(String site);

    /**
     * Get the the compute elements in a particular cluster at a particular
     * site.
     * 
     * @param site The site to check
     * @return An array of compute elements
     */
    public abstract String[] getComputeElementsOfClusterAtSite(String site, String cluster);
    
    
    /*  Codes at site.
     *******************/
    
    
    /**
     * Get all the versions of a particular code at a site.
     * 
     * @param site The site to check
     * @param code The code to check
     * @return An array of versions
     */
    public abstract String[] getVersionsOfCodeAtSite(String site, String code);
    
    /**
     * Get the name of the module required by a particular version of a code at 
     * a particular site. 
     * 
     * @param site The site to check
     * @param code The code to check
     * @param version The version of the code
     * @return The name of the module needed
     */
    public abstract String getModuleNameOfCodeAtSite(String site, String code, String version);
    
    /**
     * Get the name of the executable to be run. This may or may not be same as
     * the name of the code. For instance 'List' maps to <code>ls</code>.
     *  
     * @param site The site to check
     * @param code The code to check
     * @param version The version of the code
     * @return The executable name of the code
     */
    public abstract String getExeNameOfCodeAtSite(String site, String code, String version);
    
    /**
     * Get the job type that the specified code supports.
     * 
     * @param site The site to check
     * @param code The code to check
     * @param version The version of the code
     * @return The job type
     */
    public abstract String getJobTypeOfCodeAtSite(String site, String code, String version);
    
    /**
     * Get a list of all sites that have a particular version of the specified
     * code.
     * 
     * @param code The code to check
     * @param version The version of the code to check
     * @return A list of sites that have the specified version of the code
     */
    public abstract String[] getAllSitesWithAVersionOfACode(String code, String version);
}

