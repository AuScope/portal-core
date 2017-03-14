package org.auscope.portal.core.services;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientResponse;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.GeonetworkMethodMaker;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformer;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A service class that provides high level interactions with Geonetwork
 *
 * @author Josh Vote
 *
 */
public class GeonetworkService {

    protected final Log logger = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;
    private GeonetworkMethodMaker gnMethodMaker;
    private String userName;
    private String password;
    private String endpoint;

    /**
     *
     * @param serviceCaller
     * @param gnMethodMaker
     * @param cswServiceItem
     *            The CSW service (must be a geonetwork CSW service)
     */
    public GeonetworkService(HttpServiceCaller serviceCaller,
            GeonetworkMethodMaker gnMethodMaker, CSWServiceItem cswServiceItem) {
        super();
        this.serviceCaller = serviceCaller;
        this.gnMethodMaker = gnMethodMaker;

        this.endpoint = cswServiceItem.getServiceUrl().replace("/srv/en/csw", "");
        this.userName = cswServiceItem.getUserName();
        this.password = cswServiceItem.getPassword();
    }

    /**
     * Helper method for transforming an arbitrary CSWRecord into a <gmd:MD_Metadata> representation
     *
     * @param record
     * @return
     * @throws ParserConfigurationException 
     * @throws TransformerException 
     */
    private static String cswRecordToMDMetadataXml(CSWRecord record) throws PortalServiceException, TransformerException {
        CSWRecordTransformer transformer = new CSWRecordTransformer(); //more than meets the eye
        Node mdMetadataNode = transformer.transformToNode(record);
        return DOMUtil.buildStringFromDom(mdMetadataNode, true);
    }

    /**
     * Returns the record id from the response of an insert operation (or empty string if N/A)
     *
     * @param gnResponse
     *            A response from a CSWInsert operation
     * @return
     */
    private String extractUuid(String gnResponse) {
        String rtnValue = "";
        if (gnResponse != null && !gnResponse.isEmpty()) {
            try {
                Document doc = DOMUtil.buildDomFromString(gnResponse);

                NodeList insertNode = doc.getElementsByTagName("csw:totalInserted");
                Node n1 = insertNode.item(0).getFirstChild();
                if (n1.getNodeValue().equals("1")) {
                    NodeList idNode = doc.getElementsByTagName("identifier");
                    Node n2 = idNode.item(0).getFirstChild();
                    rtnValue = n2.getNodeValue();
                    logger.debug("Insert response id: " + rtnValue);
                }
            } catch (Exception e) {
                logger.warn("Unable to parse a geonetwork response", e);
            }
        }
        return rtnValue;
    }

    /**
     * Attempts to query geonetwork for a record with the specified UUID. If successful the underlying "record id" (not uuid) is returned which is used by
     * certain operations in this service
     *
     * @param uuid
     * @return
     * @throws PortalServiceException 
     * @throws IOException 
     */
    private String convertUUIDToRecordID(String uuid, String sessionCookie) throws PortalServiceException, IOException {
        HttpRequestBase metadataInfoMethod = gnMethodMaker.makeRecordMetadataGetMethod(endpoint, uuid, sessionCookie);
        String responseString = serviceCaller.getMethodResponseAsString(metadataInfoMethod);
        Node idNode;
        try {
            Document responseDoc = DOMUtil.buildDomFromString(responseString);

            XPathExpression getIdExpr = DOMUtil.compileXPathExpr("/gmd:MD_Metadata/geonet:info/id",
                    new CSWNamespaceContext());
            idNode = (Node) getIdExpr.evaluate(responseDoc, XPathConstants.NODE);
        } catch (XPathExpressionException | ParserConfigurationException | SAXException e) {
            throw new PortalServiceException(e.getMessage(),e);
        }
        if (idNode == null) {
            throw new PortalServiceException("Response does not contain geonetwork info about record's internal ID");
        }
        String recordId = idNode.getTextContent();

        logger.debug(String.format("converted uuid='%1$s' to recordId='%2$s", uuid, recordId));

        return recordId;
    }

    /**
     * Attempts to insert the specified CSWRecord into Geonetwork. The record will be made publicly viewable.
     *
     * If successful the URL of the newly created record will be returned
     *
     * @param record
     * @return
     * @throws PortalServiceException 
     * @throws IOException 
     */
    public String makeCSWRecordInsertion(CSWRecord record) throws PortalServiceException, IOException {
        String mdMetadataXml;
        try {
            mdMetadataXml = cswRecordToMDMetadataXml(record);
        } catch (TransformerException e1) {
            throw new PortalServiceException(e1.getMessage(),e1);
        }
        String gnResponseString = null;

        //Login and extract our cookies (this will be our session id)
        HttpRequestBase methodLogin;
        try {
            methodLogin = gnMethodMaker.makeUserLoginMethod(endpoint, userName, password);
        } catch (URISyntaxException e) {
            throw new PortalServiceException(e.getMessage(),e);
        }
        try (HttpClientResponse gnResponse = serviceCaller.getMethodResponseAsHttpResponse(methodLogin)) {
            gnResponseString = IOUtils.toString(gnResponse.getEntity().getContent());
            logger.debug(String.format("GN Login response: %1$s", gnResponseString));
            if (!gnResponseString.contains("<ok />")) {
                throw new PortalServiceException("Geonetwork login failed");
            }

            String sessionCookie = gnResponse.getFirstHeader("Set-Cookie").getValue();

            // Insert our record
            HttpRequestBase methodInsertRecord = gnMethodMaker.makeInsertRecordMethod(endpoint, mdMetadataXml,
                    sessionCookie);
            gnResponseString = serviceCaller.getMethodResponseAsString(methodInsertRecord);
            logger.debug(String.format("GN Insert response: %1$s", gnResponseString));

            // Extract our uuid and convert it to a recordId for usage in the
            // next step
            String uuid = extractUuid(gnResponseString);
            if (uuid == null || uuid.isEmpty()) {
                throw new PortalServiceException("Unable to extract uuid");
            }
            String recordId = convertUUIDToRecordID(uuid, sessionCookie);

            // Use our new record ID to FINALLY set the record to public
            HttpRequestBase methodSetPublic = gnMethodMaker.makeRecordPublicMethod(endpoint, recordId, sessionCookie);
            gnResponseString = serviceCaller.getMethodResponseAsString(methodSetPublic);
            logger.debug(String.format("GN setting record %1$s (uuid=%2$s) public returned: %3$s", recordId, uuid,
                    gnResponseString));

            // Logout (just in case)
            HttpRequestBase methodLogout = gnMethodMaker.makeUserLogoutMethod(endpoint, sessionCookie);
            gnResponseString = serviceCaller.getMethodResponseAsString(methodLogout);
            logger.debug(String.format("GN Logout response: %1$s", gnResponseString));

            // Finally get the URL to access the record's page
            HttpRequestBase metadataInfoMethod = gnMethodMaker.makeRecordMetadataShowMethod(endpoint, uuid,
                    sessionCookie);
            return metadataInfoMethod.getURI().toString();
        }
    }
}
