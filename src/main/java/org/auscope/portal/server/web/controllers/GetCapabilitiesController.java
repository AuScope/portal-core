package org.auscope.portal.server.web.controllers;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;
import org.auscope.portal.server.domain.ows.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.server.web.service.GetCapabilitiesService;
import org.auscope.portal.server.web.view.JSONModelAndView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles GetCapabilites (WFS)WMS queries.
 * 
 * @author Jarek Sanders
 * @version $Id$
 */
@Controller
public class GetCapabilitiesController {

    // -------------------------------------------------------------- Constants
    protected final Log log = LogFactory.getLog(getClass());
    
    
    // ----------------------------------------------------- Instance variables
    
    private GetCapabilitiesService capabilitiesService;

    
    // ----------------------------------------------------------- Constructors    

    @Autowired
    public GetCapabilitiesController( GetCapabilitiesService capService) {
        this.capabilitiesService = capService;
    }

    
    // ------------------------------------------- Property Setters and Getters
    
    /**
     * Gets all WMS data records from a discovery service, and then 
     * creates JSON response for the WMS layers list in the portal
     * 
     * @return JSON response with a data structure eg.
     * [
     * [title, description, contactOrganisation, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage, opacity],
     * [title, description, contactOrganisation, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage, opacity]
     * ]
     *
     * @throws Exception
     */
    @RequestMapping("/getCustomLayers.do")
    public ModelAndView getCustomLayers( @RequestParam("service_URL") String service_url) 
    throws Exception {

        GetCapabilitiesRecord capabilitiesRec 
            = capabilitiesService.getWmsCapabilities(service_url);
                        
        // The main holder for the items
        JSONArray dataItems = new JSONArray();

        for (GetCapabilitiesWMSLayerRecord rec : capabilitiesRec.getLayers()) {

            // Add layer
            JSONArray tableRow = new JSONArray();

            // Layer title
            tableRow.add(rec.getTitle());

            // Layer description
            tableRow.add(rec.getAbstract());
            
            // Provider organisation
            tableRow.add(capabilitiesRec.getOrganisation());
            
            // wms dont need a proxy url
            tableRow.add("");

            // Service type
            tableRow.add(capabilitiesRec.getServiceType());  
          
            // TODO: add a proper unique id
            tableRow.add(rec.hashCode());

            // Layer name
            tableRow.add(rec.getName());

            JSONArray serviceURLs = new JSONArray();
            
            // Service url
            serviceURLs.add(capabilitiesRec.getUrl() + "?SERVICE=WMS&");
            
            tableRow.add(serviceURLs);

            tableRow.element(true);

            tableRow.add("<img src='js/external/extjs/resources/images/default/grid/done.gif'>");
            tableRow.add("<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/picture_link.png'></a>");        
            tableRow.add("1.0");
            
            dataItems.add(tableRow);            
        }
        
        log.debug(dataItems.toString());
        return new JSONModelAndView(dataItems);      
    }

}
