package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.record.AbstractCSWOnlineResource;
import org.auscope.portal.csw.record.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.mineraloccurrence.BoreholeFilter;
import org.auscope.portal.nvcl.NVCLNamespaceContext;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * A utility class which provides methods for querying borehole service
 *
 * @author Jarek Sanders
 * @version $Id$
 *
 */
@Service
public class BoreholeService {

    // -------------------------------------------------------------- Constants

    private final Log log = LogFactory.getLog(getClass());

    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller httpServiceCaller;
    private WFSGetFeatureMethodMaker methodMaker;
    private GmlToKml gmlToKml;

    // ----------------------------------------------------------- Constructors

    // ------------------------------------------ Attribute Setters and Getters

    @Autowired
    public void setHttpServiceCaller(HttpServiceCaller httpServiceCaller) {
        this.httpServiceCaller = httpServiceCaller;
    }

    @Autowired
    public void setWFSGetFeatureMethodMakerPOST(WFSGetFeatureMethodMaker iwfsGetFeatureMethodMaker) {
        this.methodMaker = iwfsGetFeatureMethodMaker;
    }

    @Autowired
    public void setGmlToKml(GmlToKml gmlToKml) {
        this.gmlToKml = gmlToKml;
    }

    // --------------------------------------------------------- Public Methods



    /**
     * Get all boreholes from a given service url and return the response
     * @param serviceURL
     * @param bbox Set to the bounding box in which to fetch results, otherwise set it to null
     * @param restrictToIDList [Optional] A list of gml:id values that the resulting filter should restrict its search space to
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getAllBoreholes(String serviceURL, String boreholeName, String custodian, String dateOfDrilling, int maxFeatures, FilterBoundingBox bbox, List<String> restrictToIDList) throws Exception {
        String filterString;
        BoreholeFilter nvclFilter = new BoreholeFilter(boreholeName, custodian, dateOfDrilling, restrictToIDList);
        if (bbox == null) {
            filterString = nvclFilter.getFilterStringAllRecords();
        } else {
            filterString = nvclFilter.getFilterStringBoundingBox(bbox);
        }

        // Create a GetFeature request with an empty filter - get all
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "gsml:Borehole", filterString, maxFeatures);
        try {
            // Call the service, and get all the boreholes
            String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
            String responseKml = gmlToKml.convert(responseGml, serviceURL);

            return new WFSKMLResponse(responseGml, responseKml, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }


    private void appendHyloggerBoreholeIDs(String url, String typeName, List<String> idList) throws Exception {
        //Make request
        HttpMethodBase method = methodMaker.makeMethod(url, typeName, "", 0);
        String wfsResponse = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

        //Parse response
        Document doc = DOMUtil.buildDomFromString(wfsResponse);
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NVCLNamespaceContext());

        //Get our ID's
        NodeList publishedDatasets = (NodeList)xPath.evaluate("/wfs:FeatureCollection/gml:featureMembers/" + NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME + "/nvcl:scannedBorehole", doc, XPathConstants.NODESET);
        for (int i = 0; i < publishedDatasets.getLength(); i++) {
            Node holeIdentifier = (Node)xPath.evaluate("@xlink:href", publishedDatasets.item(i), XPathConstants.NODE);
            if (holeIdentifier != null) {
                idList.add(holeIdentifier.getTextContent());
            }
        }
    }

    /**
     * Goes to the CSWService to get all services that support the PUBLISHED_DATASETS_TYPENAME and queries them
     * to generate a list of borehole ID's that represent every borehole with Hylogger data.
     *
     * If any of the services queried fail to return valid responses they will be skipped
     *
     * @param cswService Will be used to find the appropriate service to query
     * @param CSWRecordsFilterVisitor A filter visitor used to perform filter operation on the online resource. Use null if not required
     * @throws Exception
     */
    public List<String> discoverHyloggerBoreholeIDs(CSWCacheService cswService,CSWRecordsFilterVisitor visitor) throws Exception {
        List<String> ids = new ArrayList<String>();

        for (CSWRecord record : cswService.getWFSRecords()) {
            for (AbstractCSWOnlineResource resource : record.getOnlineResourcesByType(visitor,OnlineResourceType.WFS)) {
                if (resource.getName().equals(NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME)) {
                    try {
                        appendHyloggerBoreholeIDs(resource.getLinkage().toString(), resource.getName(), ids);
                    } catch (Exception ex) {
                        log.warn(String.format("Discovering boreholes at '%1$s' failed", resource.getLinkage()), ex);
                    }
                }
            }
        }

        return ids;
    }
}
