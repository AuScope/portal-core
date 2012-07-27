package org.auscope.portal.core.services;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.server.http.responses.StringResponseHandler;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.GeonetworkMethodMaker;
import org.auscope.portal.core.services.namespaces.CSWNamespaceContext;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWRecordTransformer;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A service class that provides high level interactions with Geonetwork
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
     * @param cswServiceItem The CSW service (must be a geonetwork CSW service)
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
     * Helper method for transforming an arbitrary CSWRecord into a
     * <gmd:MD_Metadata> representation
     * @param record
     * @return
     * @throws Exception
     */
    private String cswRecordToMDMetadataXml(CSWRecord record) throws Exception {
        CSWRecordTransformer transformer = new CSWRecordTransformer(); //more than meets the eye
        Node mdMetadataNode = transformer.transformToNode(record);
        return DOMUtil.buildStringFromDom(mdMetadataNode, true);
    }

    /**
     * Returns the record id from the response of an insert operation (or empty string if N/A)
     * @param gnResponse A response from a CSWInsert operation
     * @return
     */
    private String extractUuid(String gnResponse) {
        String rtnValue = "";
        if(gnResponse != null && !gnResponse.isEmpty()){
            try{
                Document doc = DOMUtil.buildDomFromString(gnResponse);

                NodeList insertNode = doc.getElementsByTagName("csw:totalInserted");
                Node n1 = insertNode.item(0).getFirstChild();
                if(n1.getNodeValue().equals("1")){
                    NodeList idNode = doc.getElementsByTagName("identifier");
                    Node n2 = idNode.item(0).getFirstChild();
                    rtnValue = n2.getNodeValue();
                    logger.debug("Insert response id: "+rtnValue);
                }
            } catch(Exception e) {
                logger.warn("Unable to parse a geonetwork response", e);
            }
        }
        return rtnValue;
    }

    /**
     * Attempts to query geonetwork for a record with the specified UUID. If succesful the
     * underlying "record id" (not uuid) is returned which is used by certain operations in this
     * service
     *
     * @param uuid
     * @return
     */
    private String convertUUIDToRecordID(String uuid, HttpContext context) throws Exception {
        HttpRequestBase metadataInfoMethod = gnMethodMaker.makeRecordMetadataGetMethod(endpoint, uuid);
        String responseString = serviceCaller.getMethodResponse(metadataInfoMethod, new StringResponseHandler(), context);
        Document responseDoc = DOMUtil.buildDomFromString(responseString);

        XPathExpression getIdExpr = DOMUtil.compileXPathExpr("/gmd:MD_Metadata/geonet:info/id", new CSWNamespaceContext());
        Node idNode = (Node) getIdExpr.evaluate(responseDoc, XPathConstants.NODE);
        if (idNode == null) {
            throw new Exception("Response does not contain geonetwork info about record's internal ID");
        }
        String recordId = idNode.getTextContent();

        logger.debug(String.format("converted uuid='%1$s' to recordId='%2$s", uuid, recordId));

        return recordId;
    }

    /**
     * Attempts to insert the specified CSWRecord into Geonetwork. The record will be made publicly
     * viewable.
     *
     * If successful the URL of the newly created record will be returned
     * @param record
     * @return
     * @throws Exception
     */
    public String makeCSWRecordInsertion(CSWRecord record) throws Exception {
        String mdMetadataXml = cswRecordToMDMetadataXml(record);
        String gnResponse = null;

        //Setup a context for this set of requests to ensure cookies get remembered for this sequence of requests
        HttpContext localContext = new BasicHttpContext();
        CookieStore cookieStore = new BasicCookieStore();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        //Login and extract our cookies (this will be our session id)
        HttpRequestBase methodLogin = gnMethodMaker.makeUserLoginMethod(endpoint, userName, password);
        gnResponse = serviceCaller.getMethodResponse(methodLogin, new StringResponseHandler(), localContext);
        logger.debug(String.format("GN Login response: %1$s", gnResponse));
        if (!gnResponse.contains("<ok />")) {
            throw new Exception("Geonetwork login failed");
        }

        //Insert our record
        serviceCaller.getMethodResponse(methodLogin, new StringResponseHandler(), localContext);

        HttpRequestBase methodInsertRecord = gnMethodMaker.makeInsertRecordMethod(endpoint, mdMetadataXml);
        gnResponse = serviceCaller.getMethodResponse(methodInsertRecord, new StringResponseHandler(), localContext);
        logger.debug(String.format("GN Insert response: %1$s", gnResponse));

        //Extract our uuid and convert it to a recordId for usage in the next step
        String uuid = extractUuid(gnResponse);
        if (uuid == null || uuid.isEmpty()) {
            throw new Exception("Unable to extract uuid");
        }
        String recordId = convertUUIDToRecordID(uuid, localContext);

        //Use our new record ID to FINALLY set the record to public
        HttpRequestBase methodSetPublic = gnMethodMaker.makeRecordPublicMethod(endpoint, recordId);
        gnResponse = serviceCaller.getMethodResponse(methodSetPublic, new StringResponseHandler(), localContext);
        logger.debug(String.format("GN setting record %1$s (uuid=%2$s) public returned: %3$s", recordId, uuid , gnResponse));

        //Logout (just in case)
        HttpRequestBase methodLogout = gnMethodMaker.makeUserLogoutMethod(endpoint);
        gnResponse = serviceCaller.getMethodResponse(methodLogout, new StringResponseHandler(), localContext);
        logger.debug(String.format("GN Logout response: %1$s", gnResponse));

        //Finally get the URL to access the record's page
        HttpRequestBase metadataInfoMethod = gnMethodMaker.makeRecordMetadataShowMethod(endpoint, uuid);
        return metadataInfoMethod.getURI().toString();
    }
}
