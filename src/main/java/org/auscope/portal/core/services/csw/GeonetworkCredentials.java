package org.auscope.portal.core.services.csw;

/**
 * A POJO for storing geonetwork credentials and URL's for usage by any code
 * that intends to WRITE to geonetwork
 * @author Josh Vote
 *
 */
public class GeonetworkCredentials {
    /** The URL endpoint of the geonetwork for which these credentials are valid*/
    private String url;
    /** The user name*/
    private String user;
    /** The password*/
    private String password;

    public GeonetworkCredentials() {
        this("", "", "");
    }
    public GeonetworkCredentials(String url, String user, String password) {
        super();
        this.url = url;
        this.user = user;
        this.password = password;
    }
    /**
     * The URL endpoint of the geonetwork for which these credentials are valid
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * The URL endpoint of the geonetwork for which these credentials are valid
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The user name
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     * The user name
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * The password
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * The password
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }


}
