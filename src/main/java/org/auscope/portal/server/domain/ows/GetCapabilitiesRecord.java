package org.auscope.portal.server.domain.ows;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This class represents response to GetCapabilites query
 *
 * @author JarekSanders
 * @version $Id$
 */
public class GetCapabilitiesRecord {

    // -------------------------------------------------------------- Constants

    protected final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance Variables

    private String serviceType = "";
    private String organisation = "";
    private String getMapUrl = "";
    private ArrayList<GetCapabilitiesWMSLayerRecord> layers;
    private String[] layerSRS = null;

    // ----------------------------------------------------------- Constructors

    /**
     * C'tor
     * @param inXml GetCapabilites string response
     */
    public GetCapabilitiesRecord(String inXml) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(inXml));
            Document doc = builder.parse(inputSource);

            getService(xPath, doc);
            getContactOrg(xPath, doc);
            getGetMapUrl(xPath, doc);
            getWMSLayerSRS(xPath, doc);
            if (isWMS()) {
                getWMSLayers(xPath, doc);
            } else {
                log.info("Adding custom WFSs is not yet implimented");
            }

        } catch (Exception e) {
            log.error("GetCapabilitiesRecord xml parsing error: " + e.getMessage());
        }
    }


    // ------------------------------------------ Attribute Setters and Getters

    public boolean isWFS() {
        return this.serviceType.equals("wfs");
    }

    public boolean isWMS() {
        return this.serviceType.equals("wms");
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public String getOrganisation() {
        return this.organisation;
    }

    /**
     * Gets the URL that the GetCapabilities response has defined to be used for GetMap requests
     * @return
     */
    public String getMapUrl() {
        return this.getMapUrl;
    }

    public ArrayList<GetCapabilitiesWMSLayerRecord> getLayers() {
        return this.layers;
    }

    public String[] getLayerSRS(){
        return this.layerSRS;
    }


    // ------------------------------------------------------ Protected Methods

    private void getService(XPath xPath, Document doc) {
        try {
            /* Commented out this code as some services do not follow the
             * OGC WMS standard ie. <Name> element does not contain "OGC:WMS"
            String extractServiceExpression = "/WMT_MS_Capabilities/Service/Name";
            Node tempNode = (Node)xPath.evaluate( extractServiceExpression
                                                , doc
                                                , XPathConstants.NODE);
            final String service = tempNode != null ? tempNode.getTextContent() : "";

            if (service.equals("OGC:WMS")) {
                this.serviceType = "wms";
            } else if (service.equals("OGC:WFS")) {
                this.serviceType = "wfs";
            }*/

            // The only other way to figure out if the input comes from WMS
            // is to check for <WMT_MS_Capabilities> node
            // ASSUMPTION: <WMT_MS_Capabilities> = WMS

            int elemCount
                = Integer.parseInt((String) xPath.evaluate("count(/WMT_MS_Capabilities)", doc));

            if( elemCount != 0) {
                this.serviceType = "wms";
            }

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities get service xml parsing error: " + e.getMessage());
        }
    }

    private void getContactOrg(XPath xPath, Document doc) {
        String extractOrganisationExpression
            = "/WMT_MS_Capabilities/Service/ContactInformation/ContactPersonPrimary/ContactOrganization";

        try {
            Node tempNode = (Node)xPath.evaluate( extractOrganisationExpression
                                                , doc
                                                , XPathConstants.NODE);

            this.organisation = tempNode != null ? tempNode.getTextContent() : "";

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities get organisation xml parsing error: " + e.getMessage());
        }
    }

    private void getGetMapUrl(XPath xPath, Document doc) {
        String extractUrlExpression
            = "/WMT_MS_Capabilities/Capability/Request/GetMap/DCPType/HTTP/Get/OnlineResource";

        try {
            Element elem = (Element)xPath.evaluate( extractUrlExpression
                                                  , doc
                                                  , XPathConstants.NODE);

            this.getMapUrl = elem.getAttribute("xlink:href");

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities GetMapUrl xml parsing error: " + e.getMessage());
        }
    }

    private void getWMSLayers(XPath xPath, Document doc) {
        String extractLayerExpression
            = "/WMT_MS_Capabilities/Capability/descendant::Layer[@queryable='1']";

        try {
            NodeList nodes = (NodeList)xPath.evaluate( extractLayerExpression
                                                     , doc
                                                     , XPathConstants.NODESET );

            log.debug("Number of layers retrieved from GeoCapabilities: " + nodes.getLength());

            layers = new ArrayList<GetCapabilitiesWMSLayerRecord>();

            for(int i=0; i<nodes.getLength(); i++ ) {
                layers.add( new GetCapabilitiesWMSLayerRecord(nodes.item(i)) );
                log.debug("WMS layer " + (i+1) + " : " + layers.get(i).toString());
            }

        } catch (XPathExpressionException e) {
            log.error("GetCapabilities - getWMSLayers xml parsing error: " + e.getMessage());
        }
    }

    private void getWMSLayerSRS(XPath xPath, Document doc){
        String extractLayerSRS
            = "/WMT_MS_Capabilities/Capability/Layer/SRS";

        try{
            NodeList nodes = (NodeList)xPath.evaluate( extractLayerSRS
                    , doc
                    , XPathConstants.NODESET );

            layerSRS = new String[nodes.getLength()];
            for(int i =0; i< nodes.getLength(); i++){
                Node srsNode = nodes.item(i);
                //String tempValue = (nodes.item(i)).getNodeValue();
                layerSRS[i]= srsNode!= null ? srsNode.getTextContent() : "";

            }
        }catch (XPathExpressionException e) {
            log.error("GetCapabilities - getLayerSRS xml parsing error: " + e.getMessage());
        }
    }

}

