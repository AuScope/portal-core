package org.auscope.portal.server.web.controllers;

import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.view.CSWRecordResponse;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
     * Constructor
     * @param
     */
    @Autowired
    public CSWController(CSWService cswService,
                         ViewCSWRecordFactory viewCSWRecordFactory,
                         PortalPropertyPlaceholderConfigurer propertyResolver) {

        this.cswService = cswService;
        this.viewCSWRecordFactory = viewCSWRecordFactory;

        try {
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
			records = this.cswService.getAllRecords();
		} catch (Exception e) {
			log.error("error getting data records", e);
			return generateJSONResponse(false, "Error getting data records", null);
		}
		return generateJSONResponse(this.viewCSWRecordFactory, records);
    }
    
    /**
     * This controller method is for forcing the internal cache of CSWRecords to invalidate and update. 
     * @return
     */
    @RequestMapping("/updateCSWCache.do")
    public ModelAndView updateCSWCache() {
        try {
            this.cswService.updateRecordsInBackground(true);
            return generateJSONResponse(true, "", null);
        } catch (Exception e) {
            return generateJSONResponse(false, e.getMessage(), null);
        }
    }
}