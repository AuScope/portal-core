package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.view.CSWRecordResponse;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @version $Id$
 */
@Controller
public class CSWController extends CSWRecordResponse {

    private CSWService cswService;
    private ViewCSWRecordFactory viewCSWRecordFactory;
    
    public CSWController(ViewCSWRecordFactory viewCSWRecordFactory) {
    	this.viewCSWRecordFactory = viewCSWRecordFactory;
    }
    
    /**
     * Construct
     * @param
     */
    @Autowired
    public CSWController(CSWService cswService,
                         ViewCSWRecordFactory viewCSWRecordFactory,
                         PortalPropertyPlaceholderConfigurer propertyResolver) {

        this.cswService = cswService;
        this.viewCSWRecordFactory = viewCSWRecordFactory;

        try {
        	cswService.setServiceUrl(propertyResolver.resolvePlaceholder("HOST.cswservice.url"));
            cswService.updateRecordsInBackground();
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    /**
     * This controller method returns a representation of each and every CSWRecord from the internal cache
     * @throws Exception 
     */
    @RequestMapping("/getCSWRecords.do")
    public ModelAndView getCSWRecords() {
    	try {
			this.cswService.updateRecordsInBackground();
		} catch (Exception ex) {
			log.error("Error updating cache", ex);
			return generateJSONResponse(false, "Error updating cache", null);
		}
    	
		CSWRecord[] records = null;
		try {
			records = this.cswService.getDataRecords();
		} catch (Exception e) {
			log.error("error getting data records", e);
			generateJSONResponse(false, "Error getting data records", null);
		}
		return generateJSONResponse(this.viewCSWRecordFactory, records);
    }
}