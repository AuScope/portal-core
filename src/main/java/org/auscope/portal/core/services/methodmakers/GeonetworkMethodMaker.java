package org.auscope.portal.core.services.methodmakers;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * A class for making HttpMethods that can communicate with an instance of Geonetwork (not the CSW service)
 * 
 * @author Josh Vote
 *
 */
public class GeonetworkMethodMaker extends AbstractMethodMaker {

    /**
     * Creates a method that when called will logout the current user
     * 
     * @param baseGeonetworkUrl
     *            The base URL for the geonetwork instance - eg http://example.com/geonetwork
     * @param sessionCookie
     *            Session cookie returned from a succesful login response
     * @return
     */
    public HttpRequestBase makeUserLogoutMethod(String baseGeonetworkUrl, String sessionCookie) {
        String url = urlPathConcat(baseGeonetworkUrl, "srv/en/xml.user.logout");
        HttpGet method = new HttpGet(url);
        method.setHeader("Cookie", sessionCookie);
        return method;
    }

    /**
     * Creates a method that when called will login the specified user
     * 
     * @param baseGeonetworkUrl
     *            The base URL for the geonetwork instance - eg http://example.com/geonetwork
     * @return
     * @throws URISyntaxException
     */
    public HttpRequestBase makeUserLoginMethod(String baseGeonetworkUrl, String userName, String password)
            throws URISyntaxException {
        String url = urlPathConcat(baseGeonetworkUrl, "srv/en/xml.user.login");

        HttpGet method = new HttpGet();
        URIBuilder builder = new URIBuilder(url);
        builder.setParameter("username", userName);
        builder.setParameter("password", password);

        method.setURI(builder.build());

        return method;
    }

    /**
     * Creates a method that when called will insert the specified metadata record
     * 
     * @param baseGeonetworkUrl
     *            The base URL for the geonetwork instance - eg http://example.com/geonetwork
     * @param mdMetadataXml
     *            A string representation of the XML for a <gmd:MD_Metadata> element that will be inserted
     * @param sessionCookie
     *            Session cookie returned from a succesful login response
     * @return
     */
    public HttpRequestBase makeInsertRecordMethod(String baseGeonetworkUrl, String mdMetadataXml, String sessionCookie) {
        String url = urlPathConcat(baseGeonetworkUrl, "srv/en/csw");
        HttpPost method = new HttpPost(url);

        method.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        method.setHeader("Content-Type", "application/xml; charset=UTF-8");
        method.setHeader("Cookie", sessionCookie);

        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<csw:Transaction service=\"CSW\" version=\"2.0.2\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\">\n");
        sb.append("<csw:Insert>\n");
        sb.append(mdMetadataXml);
        sb.append("</csw:Insert>\n");
        sb.append("</csw:Transaction>");

        method.setEntity(new StringEntity(sb.toString(), ContentType.create("application/xml", "UTF-8")));

        return method;
    }

    /**
     * Makes a method that when called will request a HTML page describing the CSWRecord with specified uuid
     * 
     * @param baseGeonetworkUrl
     *            The base URL for the geonetwork instance - eg http://example.com/geonetwork
     * @param uuid
     *            The unique ID of the record
     * @param sessionCookie
     *            Session cookie returned from a successful login response
     * @return
     */
    public HttpRequestBase makeRecordMetadataShowMethod(String baseGeonetworkUrl, String uuid, String sessionCookie) {
        String url = urlPathConcat(baseGeonetworkUrl, String.format("srv/en/metadata.show?uuid=%1$s", uuid));

        HttpGet method = new HttpGet(url);
        method.setHeader("Cookie", sessionCookie);
        return method;
    }

    /**
     * Makes a method that when called will request a specified record be publicly readable
     * 
     * @param baseGeonetworkUrl
     *            The base URL for the geonetwork instance - eg http://example.com/geonetwork
     * @param recordId
     *            The unique ID of the record (as a recordId, not a uuid).
     * @param sessionCookie
     *            Session cookie returned from a successful login response
     * @return
     */
    public HttpRequestBase makeRecordPublicMethod(String baseGeonetworkUrl, String recordId, String sessionCookie) {
        String url = urlPathConcat(baseGeonetworkUrl,
                String.format("srv/en/metadata.admin?id=%1$s&_1_0=on&_1_1=on&_1_5=on&_1_6=on", recordId));

        HttpGet method = new HttpGet(url);
        method.setHeader("Cookie", sessionCookie);
        return method;
    }

    /**
     * Makes a method that when called will request XML describing the metadata associated with a CSWRecord with specified uuid
     * 
     * @param baseGeonetworkUrl
     *            The base URL for the geonetwork instance - eg http://example.com/geonetwork
     * @param uuid
     *            The unique ID of the record
     * @param sessionCookie
     *            Session cookie returned from a successful login response
     * @return
     */
    public HttpRequestBase makeRecordMetadataGetMethod(String baseGeonetworkUrl, String uuid, String sessionCookie) {
        String url = urlPathConcat(baseGeonetworkUrl, String.format("srv/en/xml.metadata.get?uuid=%1$s", uuid));

        HttpGet method = new HttpGet(url);
        method.setHeader("Cookie", sessionCookie);
        return method;
    }
}
