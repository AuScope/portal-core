package org.auscope.portal.server.web.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.csw.CSWGeographicElement;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWOnlineResourceImpl;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;
import org.auscope.portal.server.domain.ows.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.server.web.service.GetCapabilitiesService;
import org.auscope.portal.server.web.view.CSWRecordResponse;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
public class GetCapabilitiesController extends CSWRecordResponse {

    
    // ----------------------------------------------------- Instance variables
    
    private GetCapabilitiesService capabilitiesService;
    private ViewCSWRecordFactory viewCSWRecordFactory;

    
    // ----------------------------------------------------------- Constructors    

    @Autowired
    public GetCapabilitiesController( GetCapabilitiesService capService, ViewCSWRecordFactory viewCSWRecordFactory) {
    	this.viewCSWRecordFactory = viewCSWRecordFactory;
        this.capabilitiesService = capService;
    }

    
    // ------------------------------------------- Property Setters and Getters
    
    /**
     * Gets all WMS data records from a discovery service, and then 
     * creates JSON response for the WMS layers list in the portal
     * 
     * @return a JSON representation of the CSWRecord equivalent records
     *
     * @throws Exception
     */
    @RequestMapping("/getCustomLayers.do")
    public ModelAndView getCustomLayers( @RequestParam("service_URL") String service_url) throws Exception {

        GetCapabilitiesRecord capabilitiesRec 
            = capabilitiesService.getWmsCapabilities(service_url);
                        

        List<CSWRecord> cswRecords = new ArrayList<CSWRecord>(); 

        //Make a best effort of parsing a WMS into a CSWRecord
        for (GetCapabilitiesWMSLayerRecord rec : capabilitiesRec.getLayers()) {
        	String serviceName = rec.getTitle();
        	String contactOrg = capabilitiesRec.getOrganisation();
        	String fileId = "unique-id-" + rec.getName();
        	String recordInfoUrl = null;
        	String dataAbstract = rec.getAbstract();
        	CSWGeographicElement[] geoEls = null;
        	
        	CSWGeographicBoundingBox bbox = rec.getBoundingBox();
        	if (bbox != null) {
        		geoEls = new CSWGeographicElement[] {bbox};
        	}
        	
        	CSWOnlineResource[] onlineResources = new CSWOnlineResource[1];
        	onlineResources[0] = new CSWOnlineResourceImpl(new URL(capabilitiesRec.getUrl()), 
        			"OGC:WMS-1.1.1-http-get-map", 
        			rec.getName(), 
        			rec.getTitle());
        	
        	cswRecords.add(new CSWRecord(serviceName,contactOrg, fileId, recordInfoUrl, dataAbstract, onlineResources, geoEls ));     
        }
        
        //generate the same response from a getCSWRecords call
        CSWRecord[] records = cswRecords.toArray(new CSWRecord[cswRecords.size()]);
        return generateJSONResponse(viewCSWRecordFactory, records);      
    }

}
