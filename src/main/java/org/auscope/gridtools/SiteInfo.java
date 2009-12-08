/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

package org.auscope.gridtools;

import java.util.Date;

/**
 * Simple class that represents a site. Stores important information about
 * the status of the site.
 * <p>
 * <code>SiteInfo</code> objects are <i>write-once</i>, <i>read-only</i> -
 * meaning that they are constructed with their complete values, and can only
 * be interrogated for their state values after that. In summary, once
 * constructed, <b>there is no way to update/set values in a
 * <code>SiteInfo</code> object!</b>
 * 
 * @author Terry Rankine
 * @author Darren Kidd
 */
public class SiteInfo
{
	/** The site name. */
	private String 	siteName;
    
    /** The site description. */
    private String  siteDescription;
    
    /** The site computing element. */
    private String  siteComputingElement;
	
	/** The site free job slots. */
	private int 	siteFreeJobSlots;
	
	/** The site total CP us. */
	private int		siteTotalCPUs;
	
	/** The site proposed wait time. */
	private Date	siteProposedWaitTime;
	
	/**
	 * This object must be initialized with values, so the default constructor
	 * has been made <code>private</code> so it cannot be called.
	 */
    private SiteInfo() { }
    
    /**
     * Alternate constructor, but only constructor available for use. This
     * constructor sets up all the information the object will store for its
     * entire lifetime.
     * 
     * @param cpus      Site total CPU count
     * @param jobs      Site free job slots
     * @param desc      Site Description
     * @param name      Site name
     * @param compElem  Site computing element
     * @param wait      Site proposed waiting time
     */
    public SiteInfo(String name, String desc, String compElem, int jobs, int cpus, Date wait)
    {
        siteName = name;
        siteDescription = desc;
        siteComputingElement = compElem;
        siteFreeJobSlots = jobs;
        siteTotalCPUs = cpus;
        siteProposedWaitTime = wait;
    }
    
	/**
	 * Returns the name of the site.
	 * 
	 * @return The site name
	 */
	public String getSiteName() { return siteName; }
    
	/**
	 * Returns a description of the site.
	 * 
	 * @return The site description
	 */
    public String getSiteDescription() { return siteDescription; }

    /**
     * Returns the computing element of the site.
     * 
     * @return The computing element
     */
    public String getSiteComputingElement() { return siteComputingElement; }

    /**
     * Returns the number of free job slots at this site.
     * 
     * @return The free job slots
     */
	public int getSiteFreeJobSlots() {return siteFreeJobSlots; }
    
	/**
	 * Returns the total number of CPUs at this site.
	 * 
	 * @return The total CPUs
	 */
	public int getSiteTotalCPUs() { return siteTotalCPUs; }
    
	/**
	 * Returns an estimate of the waiting time until a job can be run at
	 * this site.
	 * 
	 * @return The proposed waiting time
	 */
	public Date getSiteProposedWaitTime() { return siteProposedWaitTime; }
    
	/**
	 * Return a String representing the state of this
	 * <code>SiteInfo</code> object.
	 * 
	 * @return A summary of the values of this object's fields
	 */
	public String toString()
    {
        return "siteName=\""+siteName+
               "\",siteDescription=\""+siteDescription+
               "\",siteComputingElement=\""+siteComputingElement+
               "\",siteFreeJobSlots="+siteFreeJobSlots+
               ",siteTotalCPUs="+siteTotalCPUs+
               ",siteProposedWaitTime=\""+siteProposedWaitTime+"\"";
    }
}
