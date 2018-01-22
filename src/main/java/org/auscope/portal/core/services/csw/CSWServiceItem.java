package org.auscope.portal.core.services.csw;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

/**
 * A simple class that stores the URL of a CSW service along with extra security / misc information
 *
 * @author Josh Vote
 */
public class CSWServiceItem {

    private String id;
    private String title;
    private String serviceUrl;
    private String[] restrictedRoleList;
    private String recordInformationUrl;
    private String userName;
    private String password;
    private String cqlText;
    private String defaultAnyTextFilter;
    private String[] defaultConstraints;
    private boolean noCache = false;
    private boolean hideFromCatalogue = false;

    /**
     * Creates a new service item with NO role restrictions
     * 
     * @param id
     *            Must be unique per service
     * @param serviceUrl
     */
    public CSWServiceItem(String id, String serviceUrl) {
        this(id, serviceUrl, "");
    }

    /**
     * Creates a new service item with NO role restrictions
     * 
     * @param id
     *            Must be unique per service
     * @param serviceUrl
     * @param recordInformationUrl
     */
    public CSWServiceItem(String id, String serviceUrl, String recordInformationUrl) {
        this(id, serviceUrl, recordInformationUrl, "");
    }

    /**
     * Creates a new service item with NO role restrictions
     * 
     * @param id
     *            Must be unique per service
     * @param serviceUrl
     * @param recordInformationUrl
     * @param title
     */
    public CSWServiceItem(String id, String serviceUrl, String recordInformationUrl, String title) {
        this.id = id;
        this.serviceUrl = serviceUrl;
        this.recordInformationUrl = recordInformationUrl;
        this.title = title;
    }
    
    /**
     * Creates a new service item with NO role restrictions and a cqlText query, used to restrict the downloaded CSW records
     * 
     * @param id
     *            Must be unique per service
     * @param serviceUrl
     * @param recordInformationUrl
     * @param title
     * @param cqlText
     */
    public CSWServiceItem(String id, String serviceUrl, String recordInformationUrl, String title, String cqlText) {
        this.id = id;
        this.serviceUrl = serviceUrl;
        this.recordInformationUrl = recordInformationUrl;
        this.title = title;
        this.cqlText = cqlText;
    }

    /**
     * Creates a new service item that is restricted to users with ANY of the specified roles
     * 
     * @param serviceUrl
     * @param restrictedToRoles
     *            the list of roles (the toString method will be called on each element and stored)
     */
    public CSWServiceItem(String serviceUrl, String recordInformationUrl, Collection<?> restrictedRoleList) {
        this.serviceUrl = serviceUrl;
        this.recordInformationUrl = recordInformationUrl;

        this.restrictedRoleList = new String[restrictedRoleList.size()];
        int i = 0;
        for (Object role : restrictedRoleList) {
            this.restrictedRoleList[i++] = role.toString();
        }
    }

    /**
     * The URL that the CSW service is hosted at.
     * 
     * @return
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * The list of roles that a user must have at least one of to be authorised to see records from the CSW Service
     *
     * Can be null or empty
     * 
     * @return
     */
    public String[] getRestrictedRoleList() {
        return this.restrictedRoleList;
    }

    /**
     * Gets the descriptive title of this service item
     * 
     * @return
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the unique ID of this service item
     * 
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets whether this service URL's record set can be read by the user making the request
     * 
     * @return
     */
    public boolean isUserAuthorized(HttpServletRequest request) {
        //Shortcut for no restrictions
        if (this.restrictedRoleList == null || this.restrictedRoleList.length == 0)
            return true;

        //Otherwise see if we have a role the user is in
        for (String role : this.restrictedRoleList) {
            if (request.isUserInRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a URL (can be null) representing the URL to query for extended information about this record.
     *
     * This URL may contain the string "${recordId}" which will should be replaced with the record ID to lookup
     * 
     * @return
     */
    public String getRecordInformationUrl() {
        return recordInformationUrl;
    }

    @Override
    public String toString() {
        return "CSWServiceItem [id=" + id + ", title=" + title
                + ", serviceUrl=" + serviceUrl + ", restrictedRoleList="
                + Arrays.toString(restrictedRoleList)
                + ", recordInformationUrl=" + recordInformationUrl + "]";
    }

    /**
     * Compares other against this item
     * 
     * @param other
     * @return
     */
    public boolean equals(String other) {
        if (this.id == null) {
            return other == null;
        }

        return this.id.equals(other);
    }

    /**
     * Compares these items based on id
     * 
     * @param item
     * @return
     */
    public boolean equals(CSWServiceItem item) {
        if (item == null) {
            return false;
        }

        return this.equals(item.id);
    }

    /**
     * A comparison that can be made based on whether obj is a String or a CSWServiceItem
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return this.equals((String) obj);
        } else if (obj instanceof CSWServiceItem) {
            return this.equals((CSWServiceItem) obj);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 0;
        }
        return id.hashCode();
    }

    /**
     * Gets the user name part of the credentials for this geonetwork (can be null)
     * 
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name part of the credentials for this geonetwork (can be null)
     * 
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the password part of the credentials for this geonetwork (can be null)
     * 
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password part of the credentials for this geonetwork (can be null)
     * 
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get cqlText for querying the catalog
     * 
     * @return String
     */
    public String getCqlText() {
        return cqlText;
    }

    /**
     * Set cqlText for querying the catalog
     * 
     * @return
     */
    public void setCqlText(String cqlText) {
        this.cqlText = cqlText;
    }

    /**
     * Get the defaultAnyTextFilter for querying the catalog.
     * 
     * @return String
     */
    public String getDefaultAnyTextFilter() {
        return defaultAnyTextFilter;
    }

    /**
     * Set defaultAnyTextFilter for querying the catalog.
     * 
     * @return
     */
    public void setDefaultAnyTextFilter(String defaultAnyTextFilter) {
        this.defaultAnyTextFilter = defaultAnyTextFilter;
    }

    /**
     * Gets a value indicating whether or not the caching has been disabled for this item.
     * 
     * @return
     */
    public boolean getNoCache() {
        return this.noCache;
    }

    public String[] getDefaultConstraints() {
        return this.defaultConstraints;
    }

    /**
     * Set noCache to prevent the caching of data from CSW's that have too many records.
     * 
     * @param noCache
     */
    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    /**
     * Set an array of constraints to be applied to any dummy layers that get created from this Service item. It doesn't make sense to set this value unless
     * noCache is being set to true.
     * 
     * @param defaultConstraints
     */
    public void setDefaultConstraints(String[] defaultConstraints) {
        this.defaultConstraints = defaultConstraints;
    }

    public boolean getHideFromCatalogue() {
        return hideFromCatalogue;
    }

    public void setHideFromCatalogue(boolean hideFromCatalogue) {
        this.hideFromCatalogue = hideFromCatalogue;
    }
}
