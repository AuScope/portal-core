/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */

package org.auscope.gridtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Date;
import java.util.TreeSet;

import javax.xml.rpc.Stub;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.axis.util.Util;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.client.BaseClient;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.globus.wsrf.utils.FaultHelper;
import org.oasis.wsrf.properties.QueryExpressionType;
import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;
import org.oasis.wsrf.properties.QueryResourceProperties_Element;
import org.oasis.wsrf.properties.QueryResourceProperties_PortType;
import org.oasis.wsrf.properties.WSResourcePropertiesServiceAddressingLocator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This class talks to the MDS to get information about the Grid. It implements
 * the GridInfoInterface and thus knows how to get general information about 
 * the Grid itself, the sites on the Grid, and the codes at each site.
 * <p>
 * Additionally, this class has extra methods which allow it to effectively
 * filter the output of the methods defined in the interface, and thus produce
 * more specialist queries. It also contains a few independent methods that
 * perform outrageous queries that can pinpoint information.
 * Note: XPath translate matching is case insensitive. 
 *  
 * @author Ryan Fraser
 * @author Terry Rankine
 * @author Darren Kidd
 */
public class RegistryQueryClient extends BaseClient implements GridInfoInterface
{
    private static final String TEMP_DIR =
        System.getProperty("java.io.tmpdir") + File.separator;

    /** This file contains a cached version of the MDS information. */
    private static final String MDS_CACHE_FILE = TEMP_DIR + "MDSCache.xml";

    /** location of the backup MDS Cache */
    private static final String MDS_CACHE_BACKUP = TEMP_DIR + "backupMDSCache.xml";

    /** Maximum age of cache file in seconds before it is updated. */
    private static final long MDS_CACHE_MAX_AGE = 60*60;

    private static final String MDS_SERVER = 
        "https://mds.sapac.edu.au:8443/wsrf/services/DefaultIndexService";
    private static final WSResourcePropertiesServiceAddressingLocator locator = 
        new WSResourcePropertiesServiceAddressingLocator();

    /** Reference to the Log4J logger. */
    private Log logger = LogFactory.getLog(getClass());

    /**
     * Initialises the Registry Query Client. Should make sure that the MDS
     * server it is connecting to is actually alive.
     * 
     * <b>TODO: Make sure MDS server is alive or fail!</b>
     */
    public RegistryQueryClient() {
        super();
        Util.registerTransport(); // For secure transport
        this.endpoint = new EndpointReferenceType();
        try {
            this.endpoint.setAddress(new Address(MDS_SERVER));
        } catch (MalformedURIException e) {
            logger.error(e.toString(), e);
        }
        this.authorization = NoAuthorization.getInstance();
        this.anonymous = Boolean.TRUE;

        // update cache file now
        checkCache();
    }

    /* LOCAL HELPER METHODS */

    /**
     * Checks to see if the MDS cache file exists, and then checks its age.
     * If it is too old, or doesn't exist, it is recreated.
     */
    private void checkCache() {
        File mdsCache = new File(MDS_CACHE_FILE);
        if (mdsCache.exists()) {
            Date now = new Date();
            long fileAge = now.getTime() - mdsCache.lastModified();
            if (fileAge > (MDS_CACHE_MAX_AGE * 1000)) {
                logger.debug("MDS cache file too old -> creating new one...");
                updateCacheFile();
            }
        } else {
            logger.debug("No MDS cache file -> creating one...");
            updateCacheFile();
        }
    }

    /**
     * Updates the MDS cache file.
     * 
     * @return <code>true</code> if the cache file was successfully created
     */
    private boolean updateCacheFile() {
        boolean success = false;
        // Get site based info only! No MDS service info collected.
        String xPathqueryString = "//*[local-name()='Site']"; 
        String mdsStr = masterQueryMDS(xPathqueryString);

        // If bad data - restore mds backup
        if (mdsStr == null || mdsStr.length() == 0) {
            try {
                byte[] iobuff = new byte[4096];
                int bytes;

                FileInputStream fis = new FileInputStream(MDS_CACHE_BACKUP);
                FileOutputStream fos = new FileOutputStream(MDS_CACHE_FILE);

                while ( (bytes = fis.read( iobuff )) != -1 ) {
                    fos.write( iobuff, 0, bytes );
                }
                fis.close();
                fos.close();
                success = true;
                
                logger.info("Cache file restored from backup");
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else {
            // Good data - update cache and backup.
            try {
                FileWriter fw = new FileWriter(MDS_CACHE_FILE);
                fw.write("<trmds>\n");
                fw.write(mdsStr);
                fw.write("\n</trmds>");
                logger.info("MDS cache file updated");
                fw.close();
                
                FileWriter fwCache = new FileWriter(MDS_CACHE_BACKUP);
                fwCache.write("<trmds backupCache=\"true\">\n");
                fwCache.write(mdsStr);
                fwCache.write("\n</trmds>");
                logger.info("MDS backup file updated");
                fwCache.close();
                
                success = true;
            }
            catch (Throwable e) {
                logger.error("Error writing MDS cache files - " + e);
            }
        }
        return success;
    }

    /**
     * Returns the text value of the first child element with the specified
     * tag name in the given parent element.
     * 
     * @param ele The parent element
     * @param tagName The tag to look for
     * @return The text value of the first child element
     */
    private String getTextValue(Element ele, String tagName) {
        String textVal = "";
        // Get the NodeList of all child elements with the tag name
        NodeList nl = ele.getElementsByTagName(tagName);
    
        if (nl != null && nl.getLength() > 0) {
            // Get the first element
            Element el = (Element) nl.item(0);
            try {
                // Get text value of first child
                textVal = el.getFirstChild().getNodeValue();
            } catch (Exception e) {
            }
        }
    
        return textVal;
    }

    /**
     * Runs an XPath query on the local (cached) MDS file.
     * 
     * @param query The XPath query to run
     * @return A <code>NodeList</code> containing the nodes/elements selected
     *         by the query or null on error.
     */
    private NodeList turboMDSquery(String query) {
        checkCache();

        NodeList myNodeList = null;
        XPathFactory xPath = XPathFactory.newInstance();
    
        try {
            InputSource inpXml = new InputSource(MDS_CACHE_FILE);
            myNodeList = (NodeList) xPath.newXPath().
                            evaluate(query, inpXml, XPathConstants.NODESET);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        
        return myNodeList;
    }
 
    /* Implementing GridInfoInterface. These methods basically define a query
     * and run this query on the MDS file. They then return the results. */

    /**
     * Runs an XPath query on the MDS information at a given address.
     * 
     * @param url The address of the Monitoring and Discovery Service.
     * @param xPathqueryString The XPath query to run
     * @return A String containing the result of the query
     */
    private String masterQueryMDS(String xPathQuery) {
        String returnStr = "";
 
        try {
            logger.debug("Querying MDS server at " + MDS_SERVER); 
            QueryResourceProperties_PortType queryPort = locator
                    .getQueryResourcePropertiesPort(getEPR());

            setOptions((Stub) queryPort);

            // This is the XPath query that we will use.
            // It requests all entries that contain the string
            // specified in xPathQuery
            QueryExpressionType query = new QueryExpressionType();
            query.setDialect(new URI(WSRFConstants.XPATH_1_DIALECT));
            query.setValue(xPathQuery);
            QueryResourceProperties_Element qrp =
                new QueryResourceProperties_Element(query);
 
            QueryResourcePropertiesResponse response = queryPort
                    .queryResourceProperties(qrp);

            // Now response contains 0 or more entries.
            // We need to loop over each entry and extract the
            // appropriate interesting bits

            MessageElement[] entries = response.get_any();
            for (int i = 0; entries != null && i < entries.length; i++) {
                returnStr = returnStr + entries[i].getAsString();
            }
        }
        catch (AxisFault e) {
            logger.error(FaultHelper.getMessage(e), e);
        }
        catch (Exception e) {
            logger.error(FaultHelper.getMessage(e), e);
        }

        return returnStr;
    }

    /* IMPLEMENTATION of GridInfoInterface */

    /**
     * Retrieves the names of all sites on the Grid.
     * 
     * @return Array of site names
     */
    public String[] getAllSitesOnGrid() {
        String[] hosts = new String[0];
        String xpathQuery = "//*[local-name()='Site']";
        // OldQuery: "/child::node()[local-name()='Name']"
    
        // Query MDS file.
        NodeList hostLists = turboMDSquery(xpathQuery);
    
        if (hostLists != null) {
            // Keep sites unique using TreeSet
            TreeSet<String> myTreeSet = new TreeSet<String>();
        
            for (int i = 0; i < hostLists.getLength(); i++) {
                Element siteEl = (Element) hostLists.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }
        
            // Shove it into a String[] array
            hosts = myTreeSet.toArray(new String[myTreeSet.size()]);
        }
    
        return hosts;
    }

    /**
     * Retrieves all codes (software packages) available on the Grid.
     * 
     * @return String[] Names of all codes available
     */
    public String[] getAllCodesOnGrid() {
        String names[] = new String[0];
        String xpathQuery = "//*[local-name()='SoftwarePackage']";

        // Query MDS file.
        NodeList codeAvailNodeList = turboMDSquery(xpathQuery);

        if (codeAvailNodeList != null) {
            // Keep codes unique using TreeSet
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < codeAvailNodeList.getLength(); i++) {
                Element siteEl = (Element) codeAvailNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            // Shove in array
            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return names;
    }
    
    /**
     * Gets the names of the queues (computational elements) at a site.
     * 
     * @param site Name of the site 
     * @return An array of available queues
     */
    public String[] getQueueNamesAtSite(String site) {
        String[] queueNames = new String[0];
        String xpathQuery = "//*[local-name()='Site']" +
                "/child::node()[local-name()='Name'][text()='" + site + "']" +
                "/ancestor::node()[local-name()='Site']" +
                "/descendant::node()[local-name()='ComputingElement']";

        // Query MDS file.
        NodeList queuesNodeList = turboMDSquery(xpathQuery);

        if (queuesNodeList != null) {
            // Keep unique using TreeSet
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < queuesNodeList.getLength(); i++) {
                Element siteEl = (Element) queuesNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            // Shove in array
            queueNames = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return queueNames;
    }
    
    /**
     * Gets all GridFTP servers available on the Grid.
     * 
     * @return Array of hostnames of available GridFTP servers.
     */
    public String[] getAllGridFTPServersOnGrid() {
        String[] serverNames = new String[0];

        String xpathQuery = 
            "//*[local-name()='Site']" +
            "/child::node()[local-name()='StorageElement']" +
            "/child::node()[local-name()='AccessProtocol']" +
            "/child::node()[local-name()='Type']" +
            "[text()='gsiftp']/parent::node()";

        // Query MDS file.
        NodeList ftpServersList = turboMDSquery(xpathQuery);

        if (ftpServersList != null) {
            // Keep unique using TreeSet
            TreeSet<String> myTreeSet = new TreeSet<String>();
            
            for (int i = 0; i < ftpServersList.getLength(); i++) {
                Element siteEl = (Element) ftpServersList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Endpoint"));
            }

            // Shove in array
            serverNames = myTreeSet.toArray(new String[myTreeSet.size()]);
        }
        
        return serverNames;
    }
    
    /**
     * Gets the clusters available at a site.
     * 
     * @param site The name of the site
     * @return An array of the available clusters.
     */
    public String[] getClusterNamesAtSite(String site) {
        String clusters[] = new String[0];
        String xpathQuery = 
            "//*[local-name()='Site']/child::node()[local-name()='Name']"
                + "[translate(text(),'abcdefghijklmnopqrstuvwxyz',"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ')=translate('"
                + site
                + "',"
                + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')]"
                + "/parent::node()/descendant::node()[local-name()='Cluster']";

        // Query MDS file.
        NodeList codeAvailNodeList = turboMDSquery(xpathQuery);

        if (codeAvailNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < codeAvailNodeList.getLength(); i++) {
                Element siteEl = (Element) codeAvailNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            clusters = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return clusters;
    }
    
    /**
     * Gets all the distinct versions of a particular code that are available
     * on the Grid. This method must query all the sites for their versions of
     * this code, and then collate the information into a list of unique
     * versions.
     * 
     * @return An array of all versions available
     */
    public String[] getAllVersionsOfCodeOnGrid(String code) {
        String versions[] = new String[0];
        String xpathQuery = "//*[local-name()='SoftwarePackage']"
                + "/ancestor::node()[local-name()='Site']"
                + "/descendant::node()[local-name()='SoftwarePackage']"
                + "/child::node()[contains(name(),Name)][text()='"
                + code + "']" + "/parent::node()";

        // Query MDS file
        NodeList verAvailableList = turboMDSquery(xpathQuery);

        if (verAvailableList != null) {
            // Keep unique using TreeSet
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < verAvailableList.getLength(); i++) {
                Element siteEl = (Element) verAvailableList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Version"));
            }

            versions = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return versions;
    }
    
    /**
     * Gets the address of the job manager at a particular site.
     * 
     * @param site The site to get the host address of
     * @return Address of the job manager
     */
    public String getJobManagerAtSite(String site) {
        String hostAddress = "";
        String xpathQuery = "//*[local-name()='Site']" +
                "/child::node()[local-name()='Name'][text()='"+site+"']" +
                "/ancestor::node()[local-name()='Site']" +
                "/descendant::node()[local-name()='ContactString']/text()";

        NodeList contactStrNodeList = turboMDSquery(xpathQuery);

        if (contactStrNodeList != null && contactStrNodeList.getLength() > 0) {
            hostAddress = contactStrNodeList.item(0).getNodeValue();
        }
        return hostAddress;
    }

    /**
     * Gets the address of the gateway server for the given site.
     * 
     * @param site The site to get the server of
     * @return The address of the gateway server
     */
    public String getGatewayGridFTPServerAtSite(String site) {
        String localGridFTPServer = "";
        String xpathQuery = "//*[local-name()='Site']"
                + "/child::node()[local-name()='Name'][text()='" + site
                + "']" + "/ancestor::node()[local-name()='Site']";

        // Parse the document
        NodeList serverNodeList = turboMDSquery(xpathQuery);
    
        // iterate through the document to get Code's Version
        for (int i = 0; i < serverNodeList.getLength(); i++) {
            Element siteEl = (Element) serverNodeList.item(i);
            localGridFTPServer = getTextValue(siteEl, "Endpoint");
        }

        return localGridFTPServer;
    }

    /**
     * Retrieves all codes (software packages) at a particular site.
     * 
     * @param site The name of the site
     * @return An array of codes available at the site
     */
    public String[] getAllCodesAtSite(String site) {
        String[] siteCodesAvail = new String[0];
        // XPath query to get codes (SoftwarePackages) available at a given site
        String xpathQuery = "//*[local-name()='Site']/child::node()" +
                "[contains(name(),Name)][text()='" + site + "']" +
                "/ancestor::node()[local-name()='Site']" +
                "/descendant::node()[local-name()='SoftwarePackage']"; 
        
        // Parse the document
        NodeList siteSWPackageNodeList = turboMDSquery(xpathQuery);

        if (siteSWPackageNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            // Iterate through the document to get SoftwarePackage's name.
            for (int i = 0; i < siteSWPackageNodeList.getLength(); i++) {
                // Get SoftwarePackage name.
                Element siteEl = (Element) siteSWPackageNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            siteCodesAvail = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return siteCodesAvail;
    }
    
    /**
     * Gets the compute elements in a particular cluster at the given site.
     * 
     * @param site The site to query
     * @param cluster The cluster at the site to query
     * @return An array of the available compute elements
     */
    public String[] getComputeElementsOfClusterAtSite(String site, String cluster) {
        String names[] = new String[0];
        String xpathQuery = "//*[local-name()='Site']/child::node()[local-name()='Name']"
                + "[translate(text(),'abcdefghijklmnopqrstuvwxyz',"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ')=translate('"
                + site
                + "',"
                + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')]"
                + "/parent::node()/descendant::node()[local-name()='Cluster']"
                + "/child::node()[local-name()='Name'][translate(text(),"
                + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"
                + "=translate('"
                + cluster
                + "','abcdefghijklmnopqrstuvwxyz'"
                + ",'ABCDEFGHIJKLMNOPQRSTUVWXYZ')]/parent::node()"
                + "/descendant::node()[local-name()='ComputingElement']";
    
        // Parse the document
        NodeList computeElsNodeList = turboMDSquery(xpathQuery);
    
        if (computeElsNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < computeElsNodeList.getLength(); i++) {
                Element siteEl = (Element) computeElsNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }
        
            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return names;
    }

    /**
     * Gets important information about the status of each site.
     * TODO: Not implemented.
     * 
     * @return An array of <code>SiteInfo</code> objects
     */
    
    public SiteInfo[] getAllSitesStatus() {
        return new SiteInfo[0];
    }

    /**
     * Gets the name of the module which is required for this particular code
     * to run correctly.
     * 
     * @param site The name of the site where the code resides
     * @param code The name of the code
     * @param version The version of the code
     * @return The name of the required module
     */
    public String getModuleNameOfCodeAtSite(String site, String code, String version) {
        String module = "";
        String xpathQuery = "//*[local-name()='Site']"
                + "/child::node()[local-name()='Name']" + "[text()='" + site
                + "']" + "/ancestor::node()[local-name()='Site']"
                + "/descendant::node()[local-name()='SoftwarePackage']"
                + "/child::node()[local-name()='Name']" + "[text()='" + code
                + "']/parent::node()"
                + "/child::node()[local-name()='Version']" + "[text()='"
                + version + "']" + "/parent::node()";
    
        // Parse the document
        NodeList swPackageNodeList = turboMDSquery(xpathQuery);
    
        if (swPackageNodeList != null) {
            for (int i = 0; i < swPackageNodeList.getLength(); i++) {
                Element siteEl = (Element) swPackageNodeList.item(i);
                module = getTextValue(siteEl, "Module");
            }
        }
        return module;
    }

    /**
     * Gets the job type of the code at a site. Some codes allow 
     * parallel processing, so this method finds out what is/isn't allowed.
     * TODO: Always returns "single" at the moment
     * 
     * @param site The name of the site where the code resides
     * @param code The name of the code
     * @param version The version of the code
     * @return The type of job this code supports
     */
    public String getJobTypeOfCodeAtSite(String site, String code, String version) {
        String jobType = "single"; // mpi, etc
        // TODO: Need to define a query here for parallel, single and mpi usage
        return jobType;
    }

    /**
     * Gets the executable name of given code at a particular site.
     * 
     * @param site The name of the site where the code resides
     * @param code The name of the code
     * @param version The version of the code
     * @return The executable name of the code
     */
    public String getExeNameOfCodeAtSite(String site, String code, String version) {
        String exeName = "";
        String xpathQuery = "//*[local-name()='Site']/child::node()"
                + "[local-name()='Name'][text()='"
                + site
                + "']"
                + "/ancestor::node()[local-name()='Site']"
                + "/descendant::node()[local-name()='SoftwarePackage']"
                + "/child::node()[contains(name(),Name)][text()='"
                + code
                + "']"
                + "/parent::node()/child::node()[local-name()='Version']"
                + "[text()='"
                + version
                + "']"
                + "/parent::node()/child::node()[local-name()='SoftwareExecutable']";
    
        // Parse the document
        NodeList namesNodeList = turboMDSquery(xpathQuery);

        if (namesNodeList != null) {
            for (int i = 0; i < namesNodeList.getLength(); i++) {
                Element siteEl = (Element) namesNodeList.item(i);
                // temp change for JCU
                 exeName = getTextValue(siteEl, "Name");
                //exeName = getTextValue(siteEl, "Path");
            }
        }
 
        return exeName;
    }

    /**
     * Gets the address of the site's cluster GridFTP server.
     * FIXME: Currently hardcoded to return <code>file:///</code>.
     * 
     * @param site The site to check
     * @return The <code>file:///</code> String
     */
    public String getClusterGridFTPServerAtSite(String site) {
        return "file:///";
    }

    /**
     * Gets a list of versions of a code available at a site.
     * 
     * @param site The name of the site     
     * @param code The name of the code
     * @return An array of versions of this code
     */
    public String[] getVersionsOfCodeAtSite(String site, String code) {
        String[] version = new String[0];
        String xpathQuery = "//*[local-name()='Site']"
                + "/child::node()[local-name()='Name'][text()='" + site + "']"
                + "/ancestor::node()[local-name()='Site']"
                + "/descendant::node()[local-name()='SoftwarePackage']"
                + "/child::node()[local-name()='Name']" + "[text()='" + code
                + "']/parent::node()";

        // Parse the document
        NodeList codeVersionNodeList = turboMDSquery(xpathQuery);

        if (codeVersionNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();
            for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
                Element siteEl = (Element) codeVersionNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Version"));
            }

            version = myTreeSet.toArray(new String[myTreeSet.size()]);
        }
        return version;
    }

    /**
     * Gets a list of all the sites that have the specified version of a code.
     * 
     * @param code The name of the code
     * @param version The particular version required
     * @return An array of sites with this exact code/version combination
     */
    public String[] getAllSitesWithAVersionOfACode(String code, String version) {
        String names[] = new String[0];
        String versionString = "";
        if (version.length() > 0) {
            versionString = "/child::node()[contains(name(),Version)][text()='"
                    + version + "']";
        }
    
        String xpathQuery = "//*[local-name()='SoftwarePackage']/child::node()[contains(name(),Name)]"
                + "[text()='"
                + code
                + "']/parent::node()"
                + versionString + "/ancestor::node()[local-name()='Site']";
    
        // Parse the document
        NodeList codeAvailNodeList = turboMDSquery(xpathQuery);

        if (codeAvailNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < codeAvailNodeList.getLength(); i++) {
                Element siteEl = (Element) codeAvailNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }
    
        return names;
    }

    /* NOT PART OF GRID INFO INTERFACE */
    
    /**
     * Gets all sites' code versions.
     * 
     * @param requestedCode the requested code
     * 
     * @return all sites' code versions
     */
    public String[] getAllSitesCodeVersions(String requestedCode) {
        String versions[] = new String[0];
        String xpathQuery = "//*[local-name()='SoftwarePackage']"
                + "/ancestor::node()[local-name()='Site']"
                + "/descendant::node()[local-name()='SoftwarePackage']"
                + "/child::node()[contains(name(),Name)][text()='"
                + requestedCode + "']" + "/parent::node()";

        // Old Version.
        // "/child::node()[local-name()='Version']";

        // Parse the document
        NodeList codeAvailNodeList = turboMDSquery(xpathQuery);

        if (codeAvailNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < codeAvailNodeList.getLength(); i++) {
                Element siteEl = (Element) codeAvailNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Version"));
            }

            versions = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return versions;
    }

    /**
     * Gets the free job slots of compute elements of a cluster at a site.
     * 
     * @param site      The site name
     * @param cluster   The cluster name
     * @param computeEl The compute element
     * @return The free job slots
     */
    public String[] getFreeJobSlotsOfComputeElementsOfClusterAtSite(
            String site, String cluster, String computeEl) {

        String names[] = new String[0];
        String xpathQuery = "//*[local-name()='Site']"
                + "/child::node()[local-name()='Name']"
                + "[translate(text(),'abcdefghijklmnopqrstuvwxyz',"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ')=translate('"
                + site
                + "','abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')]"
                + "/parent::node()/descendant::node()[local-name()='Cluster']"
                + "/child::node()[local-name()='Name'][translate(text(),"
                + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"
                + "=translate('" + cluster
                + "','abcdefghijklmnopqrstuvwxyz'"
                + ",'ABCDEFGHIJKLMNOPQRSTUVWXYZ')]/parent::node()"
                + "/descendant::node()[local-name()='ComputingElement']";

        // Parse the document
        NodeList slotsAvailNodeList = turboMDSquery(xpathQuery);

        if (slotsAvailNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < slotsAvailNodeList.getLength(); i++) {
                Element siteEl = (Element) slotsAvailNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return names;
    }

    /**
     * Gets the names of the subclusters at a site.
     * 
     * @param site The site to check
     * @return An array of subcluster names
     */
    public String[] getSubClusterNamesAtSite(String site) {
        String names[] = new String[0];
        String xpathQuery = "//*[local-name()='Site']/child::node()[local-name()='Name']"
                + "[translate(text(),'abcdefghijklmnopqrstuvwxyz',"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ')=translate('"
                + site
                + "',"
                + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')]"
                + "/parent::node()/descendant::node()[local-name()='Cluster']";

        // Parse the document
        NodeList clusterNodeList = turboMDSquery(xpathQuery);

        if (clusterNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < clusterNodeList.getLength(); i++) {
                Element siteEl = (Element) clusterNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return names;
    }

    public String[] getSubClusterWithMemAndCPUsFromClusterFromSite(String site,
            String cluster, String cpus, String mem) {

        /*
         * [local-name()='Site']/child::node()[local-name()='Name'][text()='iVEC']/parent::node()
         * /child::node()[local-name()='Cluster']/child::node()[local-name()='SubCluster']
         * /child::node()[local-name()='PhysicalCPUs'][number(text())>'16']/parent::node()
         * /child::node()[local-name()='MainMemory'][number(@RAMSize)>'5000']/parent::node()
         * /child::node()[local-name()='Name']
         */

        String xpathQuery = "//*[local-name()='Site']/child::node()[local-name()='Name'][text()='"
                + site
                + "']/"
                + "parent::node()/child::node()[local-name()='Cluster']/"
                + "child::node()[local-name()='SubCluster']";

        if (cpus.length() > 0) {
            xpathQuery += "/child::node()[local-name()='PhysicalCPUs'][number(text())>='"
                    + cpus + "']/parent::node()";
        }
        if (mem.length() > 0) {
            xpathQuery += "/child::node()[local-name()='MainMemory'][number(@RAMSize)>='"
                    + mem + "']/parent::node()";
        }

        // Old Version
        // xpathQuery += "/child::node()[local-name()='Name']";

        String names[] = new String[0];

        // Parse the document
        NodeList clusterNodeList = turboMDSquery(xpathQuery);

        if (clusterNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < clusterNodeList.getLength(); i++) {
                Element siteEl = (Element) clusterNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return names;
    }

    /**
     * Gets the subcluster that matches the code, version, number of CPUs, and
     * memory. CPUs, mem and version can be empty strings.
     * 
     * @param code    The code to find
     * @param version The version of the code
     * @param cpus    The number of CPUs to request
     * @param mem     The amount of memory to request
     * @return Name of the subcluster(s) that match
     */
    public String[] getSubClusterWithSoftwareAndVersionWithMemAndCPUs(
            String code, String version, String cpus, String mem) {

        /*
         * //*[local-name()='Site']/child::node()[local-name()='Name'][text()='iVEC']/parent::node()
         * //*[local-name()='Site']/
         * /child::node()[local-name()='Cluster']/child::node()[local-name()='SubCluster']
         * /child::node()[local-name()='PhysicalCPUs'][number(text())>'16']/parent::node()
         * /child::node()[local-name()='MainMemory'][number(@RAMSize)>'5000']/parent::node()
         * /child::node()[local-name()='SoftwarePackage']
         * /child::node()[contains(name(), Name)][text()='MrBayes']
         * /parent::node()/child::node()[contains(name(),Version)][text()='3.1.2']
         * /ancestor::node()[local-name()='SubCluster']
         */

        String xpathQuery = "//*[local-name()='Site']/child::node()[local-name()='Cluster']/"
                + "child::node()[local-name()='SubCluster']";
        if (cpus.length() > 0) {
            xpathQuery += "/child::node()[local-name()='PhysicalCPUs'][number(text())>='"
                    + cpus + "']/parent::node()";
        }
        if (mem.length() > 0) {
            xpathQuery += "/child::node()[local-name()='MainMemory'][number(@RAMSize)>='"
                    + mem + "']/parent::node()";
        }

        xpathQuery += "/child::node()[local-name()='SoftwarePackage']"
                + "/child::node()[contains(name(), Name)][text()='"
                + code + "']/parent::node()";

        if (version.length() > 0) {
            xpathQuery += "/child::node()[contains(name(),Version)][text()='"
                    + version + "']/";
        }

        xpathQuery += "/ancestor::node()[local-name()='SubCluster']";

        String names[] = new String[0];

        // Parse the document
        NodeList clusterNodeList = turboMDSquery(xpathQuery);

        if (clusterNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < clusterNodeList.getLength(); i++) {
                Element siteEl = (Element) clusterNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return names;
    }

    /**
     * Gets the queue (a.k.a. Compute Elements) that matches the walltime 
     * requested and the subcluster's hostname.
     * 
     * @param subCluster The subcluster's host name
     * @param wallTime   The walltime to request
     * @return A list of matching queues/computing elements
     */
    public String[] getComputingElementForWalltimeAndSubcluster(
            String subCluster, String wallTime) {

        /*
         * //*[local-name()='Site']/child::node()[local-name()='Cluster']
         * /child::node()[local-name()='ComputingElement']
         * /child::node()[local-name()='MaxWallClockTime'][number(text())>'16']/parent::node()
         * /child::node()[local-name()='HostName'][text()='hydra.sapac.edu.au']/parent::node()
         */

        String xpathQuery = "//*[local-name()='Site']/child::node()[local-name()='Cluster']"
                + "/child::node()[local-name()='ComputingElement']"
                + "/child::node()[local-name()='HostName'][text()='"
                + subCluster + "']/parent::node()";

        if (wallTime.length() > 0) {
            xpathQuery += "/child::node()[local-name()='MaxWallClockTime']"
                    + "[number(text())>'" + wallTime + "']/parent::node()";
        }

        String names[] = new String[0];

        // Parse the document
        NodeList elementNodeList = turboMDSquery(xpathQuery);

        if (elementNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Element siteEl = (Element) elementNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Name"));
            }

            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return names;
    }

    /**
     * Gets the storage element available at a site given the queue which will
     * be used.
     * 
     * <b>TODO: Not implemented properly yet...</b>
     * 
     * @param queue The queue to check
     * @return The available storage element
     */
    public String getStorageElementFromComputingElement(String queue) {
        /*
         * //*[local-name()='Site']/child::node()[local-name()='Cluster']
         * /child::node()[local-name()='ComputingElement']
         * /child::node()[local-name()='Name'][text()='queueName']/parent::node()
         * /child::node()[local-name()='DefaultSE']
         */

        String xpathQuery = "//*[local-name()='Site']/child::node()[local-name()='Cluster']"
                + "/child::node()[local-name()='ComputingElement']"
                + "/child::node()[local-name()='Name'][text()='queueName']"
                + "/parent::node()";

        String names[] = new String[0];

        // Parse the document
        NodeList elementNodeList = turboMDSquery(xpathQuery);

        if (elementNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Element siteEl = (Element) elementNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "DefaultSE"));
            }

            names = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        if (names.length == 0)
            return "";
        else
            return names[0];
    }

    /**
     * Gets the storage path that satisfies the amount of disk space available
     * at a storage element.
     * 
     * <b>TODO: Not implemented properly yet...</b>
     * 
     * @param defaultSE The storage element to check
     * @param diskSpace The amount of diskspace required
     * @return The storage path that satisfies these requirements
     */
    public String getStoragePathWithSpaceAvailFromDefaultStorageElement(
            String defaultSE, String diskSpace) {
        /*
         * //*[local-name()='Site']/child::node()[local-name()='StorageElement']
         * [@UniqueID='defaultSE']/child::node()[local-name()='StorageArea']
         * /child::node()[local-name()='AvailableSpace'][number(text())>diskSpace]/parent::node()
         * /child::node()[local-name()='Path']
         */

        String storagePath = "";

        String xpathQuery = "//*[local-name()='Site']"
                + "/child::node()[local-name()='Cluster']"
                + "/child::node()[local-name()='ComputingElement']"
                + "/child::node()[local-name()='Name'][text()='queueName']"
                + "/parent::node()";

        // Parse the document
        NodeList pathNodeList = turboMDSquery(xpathQuery);

        if (pathNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();

            for (int i = 0; i < pathNodeList.getLength(); i++) {
                Element siteEl = (Element) pathNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "DefaultSE"));
            }
            // TODO: THIS IS WRONG.
            storagePath = myTreeSet.toArray(new String[myTreeSet.size()])[0];
        }

        return storagePath;
    }

    /**
     * Retrieves all available GridFTP servers from MDS.
     * 
     * @return Array of GridFTP server hostnames
     */
    public String[] getAllGridFtpServers() {
        /*
         * //*[local-name()='Site']/child::node()[local-name()='StorageElement']
         * /child::node()[local-name()='AccessProtocol']
         * /child::node()[local-name()='Type'][text()='gsiftp']/parent::node()
         * /child::node()[local-name()='Endpoint']
         */

        String[] serverNames = new String[0];

        String xpathQuery = "//*[local-name()='Site']"
                + "/child::node()[local-name()='StorageElement']"
                + "/child::node()[local-name()='AccessProtocol']"
                + "/child::node()[local-name()='Type']"
                + "[text()='gsiftp']/parent::node()";

        // Parse the document
        NodeList serverNodeList = turboMDSquery(xpathQuery);

        if (serverNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();
            for (int i = 0; i < serverNodeList.getLength(); i++) {
                Element siteEl = (Element) serverNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "Endpoint"));
            }

            serverNames = myTreeSet.toArray(new String[myTreeSet.size()]);
        }

        return serverNames;
    }


    /**
     * Gets the email address of a site's support contact
     * 
     * @param site The site to check
     * @return The singular site email address string
     */
    public String getSiteContactEmailAtSite(String site) {
        String xpathQuery = "//*[local-name()='Site']/child::node()[local-name()='Name']"
                + "[translate(text(),'abcdefghijklmnopqrstuvwxyz',"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ')=translate('"
                + site
                + "',"
                + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')]"
                + "/parent::node()";

        String email = "";

        // Parse the document
        NodeList emailNodeList = turboMDSquery(xpathQuery);

        if (emailNodeList != null) {
            TreeSet<String> myTreeSet = new TreeSet<String>();
            for (int i = 0; i < emailNodeList.getLength(); i++) {
                Element siteEl = (Element) emailNodeList.item(i);
                myTreeSet.add(getTextValue(siteEl, "UserSupportContact"));
            }

            // Take the first element.... pretty poor way to do it...
            email = myTreeSet.toArray(new String[myTreeSet.size()])[0];
        }

        return email;
    }
}

