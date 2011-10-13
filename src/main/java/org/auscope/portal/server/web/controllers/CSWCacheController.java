package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.CSWCacheService;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @version $Id: CSWCacheController.java 1863 2011-08-08 07:55:42Z JoshVote $
 */
@Controller
public class CSWCacheController extends BaseCSWController {

    private CSWCacheService cswService;

    public CSWCacheController(ViewCSWRecordFactory viewCSWRecordFactory) {
        super(viewCSWRecordFactory);
    }

    /**
     * Constructor
     * @param
     */
    @Autowired
    public CSWCacheController(CSWCacheService cswService,
                         ViewCSWRecordFactory viewCSWRecordFactory,
                         PortalPropertyPlaceholderConfigurer propertyResolver) {

        super(viewCSWRecordFactory);
        this.cswService = cswService;
        try {
            cswService.updateCache();
        } catch (Exception e) {
            log.error("Error whilst starting initial cache update",e);
        }
    }

    /**
     * This controller method returns a representation of each and every CSWRecord from the internal cache
     * @throws Exception
     */
    @RequestMapping("/getCSWRecords.do")
    public ModelAndView getCSWRecords() {
        List<CSWRecord> records = null;
        try {
            records = this.cswService.getRecordCache();
        } catch (Exception e) {
            log.error("error getting data records", e);
            return generateJSONResponseMAV(false, new CSWRecord[] {}, "Error getting data records");
        }
        return generateJSONResponseMAV(records.toArray(new CSWRecord[records.size()]));
    }

    /**
     * This controller method is for forcing the internal cache of CSWRecords to invalidate and update.
     * @return
     */
    @RequestMapping("/updateCSWCache.do")
    public ModelAndView updateCSWCache() {
        try {
            this.cswService.updateCache();
            return generateJSONResponseMAV(true);
        } catch (Exception e) {
            log.warn("Error updating CSW cache", e);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Requests every keyword as cached by
     * @return
     */
    @RequestMapping("/getCSWKeywords.do")
    public ModelAndView getCSWKeywords() {
        Map<String, Integer> keywords = this.cswService.getKeywordCache();

        List<ModelMap> response = new ArrayList<ModelMap>();
        for (String keyword : keywords.keySet()) {
            ModelMap modelMap = new ModelMap();
            modelMap.put("keyword", keyword);
            modelMap.put("count", keywords.get(keyword));
            response.add(modelMap);
        }

        return generateJSONResponseMAV(true, response, "");
    }
}
