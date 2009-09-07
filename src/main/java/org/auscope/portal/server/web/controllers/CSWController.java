package org.auscope.portal.server.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.web.KnownFeatureTypeDefinition;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.apache.log4j.Logger;
import net.sf.json.JSONArray;

import java.util.ArrayList;

/**
 * User: Mathew Wyatt
 * Date: 23/06/2009
 * Time: 4:57:12 PM
 */
@Controller
public class CSWController {

    private Logger logger = Logger.getLogger(getClass());
    private CSWService cswService;
    private PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer;
    private ArrayList knownTypes;

    /**
     * Construct
     * @param
     */
    @Autowired
    public CSWController(CSWService cswService,
                         PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer,
                         ArrayList knownTypes) {

        this.cswService = cswService;
        this.portalPropertyPlaceholderConfigurer = portalPropertyPlaceholderConfigurer;
        this.knownTypes = knownTypes;

        String cswServiceUrl =
            portalPropertyPlaceholderConfigurer.resolvePlaceholder("HOST.cswservice.url");
        logger.debug("cswServiceUrl: " + cswServiceUrl);
        cswService.setServiceUrl(cswServiceUrl);

        try {
            cswService.updateRecordsInBackground();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * This controller queries a CSW for all of its WFS data records based on known feature types, then created a JSON response as a list
     * which can then be put into a table.
     *
     * Returns a JSON response with a data structure like so
     *
     * [
     * [title, description, proxyURL, serviceType, id, typeName, serviceURLs, checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage],
     * [title, description, proxyURL, serviceType, id, typeName, serviceURLs, checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage]
     * ]
     *
     * @return
     */
    @RequestMapping("/getComplexFeatures.do")
    public ModelAndView getComplexFeatures() throws Exception {
        //the main holder for the items
        JSONArray dataItems = new JSONArray();

        for(Object known : knownTypes) {
            KnownFeatureTypeDefinition knownType = (KnownFeatureTypeDefinition)known;

            //Add the mineral occurrence
            JSONArray tableRow = new JSONArray();

            //add the name of the layer/feature type
            tableRow.add(knownType.getDisplayName());

            CSWRecord[] records = cswService.getWFSRecordsForTypename(knownType.getFeatureTypeName());
            String servicesDescription = "Institutions: ";
            JSONArray serviceURLs = new JSONArray();

            //if there are no services available for this feature type then don't show it in the portal
            if(records.length == 0)
                break;

            for(CSWRecord record : records) {
                serviceURLs.add(record.getServiceUrl());
                servicesDescription += record.getContactOrganisation() + ", ";
                //serviceURLs.add("http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services");
                //serviceURLs.add("http://auscope-services.arrc.csiro.au/deegree-wfs/services");
            }

            //add the abstract text to be shown as updateCSWRecords description
            tableRow.add(knownType.getDescription() + " " + servicesDescription);

            //add the service URL - this is the spring controller for handling minocc
            tableRow.add(knownType.getProxyUrl());

            //add the type: wfs or wms
            tableRow.add("wfs");

            //TODO: add updateCSWRecords proper unique id
            tableRow.add(knownType.hashCode());

            //add the featureType name (in case of updateCSWRecords WMS feature)
            tableRow.add(knownType.getFeatureTypeName());

            tableRow.add(serviceURLs);

            tableRow.add("true");
            tableRow.add("<img src='js/external/ext-2.2/resources/images/default/grid/done.gif'>");

            tableRow.add("<img width='16' heigh='16' src='" + knownType.getIconUrl() + "'>");
            tableRow.add(knownType.getIconUrl());

            tableRow.add("<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/page_code.png'></a>");

            dataItems.add(tableRow);
        }
        logger.debug(dataItems.toString());
        return new JSONModelAndView(dataItems);
    }

    /**
     * Gets all WMS data records from a CSW service, and then creats a JSON response for the WMS layers list in the portal
     *
     * Returns a JSON response with a data structure like so
     *
     * [
     * [title, description, proxyURL, serviceType, id, typeName, serviceURLs, checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage],
     * [title, description, proxyURL, serviceType, id, typeName, serviceURLs, checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage]
     * ]
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/getWMSLayers.do")
    public ModelAndView getWMSLayers() throws Exception {
        //the main holder for the items
        JSONArray dataItems = new JSONArray();

        CSWRecord[] records = cswService.getWMSRecords();

        for(CSWRecord record : records) {
            //Add the mineral occurrence
            JSONArray tableRow = new JSONArray();

            //add the name of the layer/feature type
            tableRow.add(record.getServiceName());

            //add the abstract text to be shown as updateCSWRecords description
            tableRow.add(record.getOnlineResourceDescription());

            //wms dont need updateCSWRecords proxy url
            tableRow.add("");

            //add the type: wfs or wms
            tableRow.add("wms");

            //TODO: add updateCSWRecords proper unique id
            tableRow.add(record.hashCode());

            //add the featureType name (in case of updateCSWRecords WMS feature)
            tableRow.add(record.getOnlineResourceName());

            JSONArray serviceURLs = new JSONArray();

            serviceURLs.add(record.getServiceUrl());

            tableRow.add(serviceURLs);

            tableRow.add("true");
            tableRow.add("<img src='js/external/ext-2.2/resources/images/default/grid/done.gif'>");

            tableRow.add("<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/picture_link.png'></a>");

            dataItems.add(tableRow);
        }
        logger.debug(dataItems.toString());
        return new JSONModelAndView(dataItems);
    }
}