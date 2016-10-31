package org.auscope.portal.core.services.responses.wms;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents response to GetCapabilites query.
 *
 * @version $Id$
 */
public class GetCapabilitiesRecord_1_3_0 implements GetCapabilitiesRecord {

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());

    /** The service type. */
    private String serviceType = "";

    /** The organisation. */
    private String organisation = "";

    /** The get map url. */
    private String getMapUrl = "";

    /** The metadata url. */
    private String metadataUrl = "";

    private String[] getMapFormats = new String[] {};

    /** The layers. */
    private ArrayList<GetCapabilitiesWMSLayerRecord> layers;

    /** The layer srs. */
    private String[] layerSRS = null;

    /** The extract organisation expression. */
    private static final String EXTRACTORGANISATIONEXPRESSION = "/WMS_Capabilities/Service/ContactInformation/ContactPersonPrimary/ContactOrganization";

    /** The extract layer srs. */
    private static final String EXTRACTLAYERSRS = "/WMS_Capabilities/Capability/Layer/CRS";

    /** The extract url expression. */
    private static final String EXTRACTURLEXPRESSION = "/WMS_Capabilities/Capability/Request/GetMap/DCPType/HTTP/Get/OnlineResource";

    private static final String EXTRACTGETMAPFORMATEXPRESSION = "/WMS_Capabilities/Capability/Request/GetMap/Format";

    /** The extract layer expression. */
    private static final String EXTRACTLAYEREXPRESSION = "/WMS_Capabilities/Capability/descendant::Layer";

    /** The MetadataURL expression. */
    private static final String METADATAURLREXPRESSION = "/WMS_Capabilities/Capability/Layer/MetadataURL/OnlineResource";

    /**
     * Constructor.
     *
     * @param inXml
     *            GetCapabilites string response
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public GetCapabilitiesRecord_1_3_0(InputStream inXml) throws SAXException, IOException,
    ParserConfigurationException {
        try {

            Document doc = DOMUtil.buildDomFromStream(inXml, false);

            this.serviceType = getService(doc);
            this.organisation = getContactOrganisation(doc);
            this.getMapUrl = getGetMapUrl(doc);
            this.metadataUrl = getMetadataUrl(doc);
            this.layerSRS = getWMSLayerSRS(doc);
            this.getMapFormats = getWMSGetMapFormats(doc);
            if (isWMS()) {
                this.layers = getWMSLayers(doc);
            } else {
                log.debug("Adding non WMS's are not yet implimented");
            }
            /**
             * VT:We should not catch the exception rather, we should rethrow it. The reason I didn't change it right now is because of this bug, GSWA is
             * working http://geossdi.dmp.wa.gov.au/services/schemas/wms/1.1.1/WMS_MS_Capabilities.dtd is not accessible and when
             * http://geossdi.dmp.wa.gov.au/services/wms?SERVICE=WMS&service=WMS&request=GetCapabilities&version=1.1.1 the parser trys to access the dtd and
             * fails BECAUSE this exception is swallowed, GetCapabilitiesRecord_1_3_0 and GetCapabilitiesRecord.accepts returns true but the proper behaviour
             * and the correct behavior is to fail.
             */

        } catch (SAXException e) {
            log.error("Parsing error: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("IO error: " + e.getMessage());
            throw e;
        } catch (ParserConfigurationException e) {
            log.error("Parser Config Error: " + e.getMessage());
            throw e;
        }
    }

    // ------------------------------------------ Attribute Setters and Getters

    /**
     * Checks if is wFS.
     *
     * @return true, if is wFS
     */
    @Override
    public boolean isWFS() {
        return this.serviceType.equals("wfs");
    }

    /**
     * Checks if is wMS.
     *
     * @return true, if is wMS
     */
    @Override
    public boolean isWMS() {
        return this.serviceType.equals("wms");
    }

    /**
     * Gets the service type.
     *
     * @return the service type
     */
    @Override
    public String getServiceType() {
        return this.serviceType;
    }

    /**
     * Gets the organisation.
     *
     * @return the organisation
     */
    @Override
    public String getOrganisation() {
        return this.organisation;
    }

    /**
     * Gets the URL that the GetCapabilities response has defined to be used for GetMap requests.
     *
     * @return the map url
     */
    @Override
    public String getMapUrl() {
        return this.getMapUrl;
    }

    /**
     * Gets the MetadataURL for the base layer of this record.
     *
     * @return the metadata url
     */
    @Override
    public String getMetadataUrl() {
        return this.metadataUrl;
    }

    /**
     * Gets the layers.
     *
     * @return the layers
     */
    @Override
    public ArrayList<GetCapabilitiesWMSLayerRecord> getLayers() {
        return this.layers;
    }

    /**
     * Gets the layer srs.
     *
     * @return the layer srs
     */
    @Override
    public String[] getLayerSRS() {
        return this.layerSRS;
    }

    /**
     * Returns an array of MIME strings representing the valid format for the GetMap operation
     *
     * @return
     */
    @Override
    public String[] getGetMapFormats() {
        return getMapFormats;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Gets the service.
     *
     * @param xPath
     *            the x path
     * @param doc
     *            the doc
     * @return serviceUrlString the service endpoint
     */
    private String getService(Document doc) {
        String serviceUrlString = "";
        try {
            int elemCount = Integer.parseInt((String) DOMUtil.compileXPathExpr("count(/WMS_Capabilities)").evaluate(
                    doc, XPathConstants.STRING));

            if (elemCount != 0) {
                serviceUrlString = "wms";
            }

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities get service xml parsing error: " + e.getMessage());
        }
        return serviceUrlString;
    }

    /**
     * Gets the contact organisation.
     *
     * @param xPath
     *            the x path
     * @param doc
     *            the doc
     * @return contactOrganisation the contact organisation
     */
    private String getContactOrganisation(Document doc) {
        String contactOrganisation = "";
        try {
            Node tempNode = (Node) DOMUtil.compileXPathExpr(EXTRACTORGANISATIONEXPRESSION).evaluate(doc,
                    XPathConstants.NODE);

            contactOrganisation = tempNode != null ? tempNode.getTextContent() : "";

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities get organisation xml parsing error: " + e.getMessage());
        }
        return contactOrganisation;
    }

    /**
     * Gets the gets the map url.
     *
     * @param xPath
     *            the xpath
     * @param doc
     *            the doc
     * @return mapUrl the map url String
     */
    private String getGetMapUrl(Document doc) {
        String mapUrl = "";
        try {
            Element elem = (Element) DOMUtil.compileXPathExpr(EXTRACTURLEXPRESSION).evaluate(doc, XPathConstants.NODE);

            mapUrl = elem.getAttribute("xlink:href");

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities GetMapUrl xml parsing error: " + e.getMessage());
        }
        return mapUrl;
    }

    /**
     * Gets the metadata url.
     *
     * @param xPath
     *            the xpath to use to find the element
     * @param doc
     *            the document
     * @return the map url String
     */
    private String getMetadataUrl(Document doc) {
        String metadataUrlStr = "";
        try {
            Element elem = (Element) DOMUtil.compileXPathExpr(METADATAURLREXPRESSION).evaluate(doc, XPathConstants.NODE);
            if (elem != null) {
                metadataUrlStr = elem.getAttribute("xlink:href");
            }
        } catch (XPathExpressionException e) {
            log.error("GetCapabilities MetadataURL xml parsing error: " + e.getMessage());
        }
        return metadataUrlStr;
    }

    /**
     * Gets the wMS layers.
     *
     * @param xPath
     *            the x path
     * @param doc
     *            the doc
     * @return the wMS layers
     */
    private ArrayList<GetCapabilitiesWMSLayerRecord> getWMSLayers(Document doc) {
        ArrayList<GetCapabilitiesWMSLayerRecord> mylayerList = new ArrayList<>();
        try {

            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr(EXTRACTLAYEREXPRESSION).evaluate(doc,
                    XPathConstants.NODESET);

            log.debug("Number of layers retrieved from GeoCapabilities: " + nodes.getLength());

            for (int i = 0; i < nodes.getLength(); i++) {
                mylayerList.add(new GetCapabilitiesWMSLayer_1_3_0(nodes.item(i)));
                log.debug("WMS layer " + (i + 1) + " : " + mylayerList.get(i).toString());
            }

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities - getWMSLayers xml parsing error: " + e.getMessage());
        }

        return mylayerList;
    }

    /**
     * Gets the wMS layer srs.
     *
     * @param xPath
     *            the x path
     * @param doc
     *            the doc
     * @return the wMS layer srs
     */
    private String[] getWMSLayerSRS(Document doc) {
        String[] layerSRSList = null;
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr(EXTRACTLAYERSRS).evaluate(doc, XPathConstants.NODESET);

            layerSRSList = new String[nodes.getLength()];

            for (int i = 0; i < nodes.getLength(); i++) {
                Node srsNode = nodes.item(i);
                layerSRSList[i] = srsNode != null ? srsNode.getTextContent() : "";
            }
        } catch (XPathExpressionException e) {
            log.error("GetCapabilities - getLayerSRS xml parsing error: " + e.getMessage());
        }
        return layerSRSList;
    }

    /**
     * Gets the WMS layer GetMap formats.
     *
     * @param xPath
     *            the x path
     * @param doc
     *            the doc
     * @return the wMS layer srs
     */
    private String[] getWMSGetMapFormats(Document doc) {
        String[] formatList = null;
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr(EXTRACTGETMAPFORMATEXPRESSION).evaluate(doc,
                    XPathConstants.NODESET);

            formatList = new String[nodes.getLength()];

            for (int i = 0; i < nodes.getLength(); i++) {
                Node formatNode = nodes.item(i);
                formatList[i] = formatNode != null ? formatNode.getTextContent() : "";
            }
        } catch (XPathExpressionException e) {
            log.error("GetCapabilities - getWMSGetMapFormats xml parsing error: " + e.getMessage());
        }
        return formatList;
    }

    @Override
    public String getVersion() {

        return "1.3.0";
    }
}
