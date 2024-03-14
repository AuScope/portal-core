package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
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

    /**
     * Constructor
     * @param
     */
    @Autowired
    public CSWCacheController(CSWCacheService cswService,
                         ViewCSWRecordFactory viewCSWRecordFactory) {
        super(viewCSWRecordFactory);
        this.cswService = cswService;
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
            log.error(String.format("error getting data records: %1$s", e));
            log.debug("Exception:", e);
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
            log.warn(String.format("Error updating CSW cache: %1$s", e));
            log.debug("Exception:", e);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Requests every keyword as cached by
     * @return
     */
    @RequestMapping("/getCSWKeywords.do")
    public ModelAndView getCSWKeywords() {
        Map<String, Set<CSWRecord>> keywords = this.cswService.getKeywordCache();

        List<ModelMap> response = new ArrayList<>();
        for (String keyword : keywords.keySet()) {
            ModelMap modelMap = new ModelMap();
            modelMap.put("keyword", keyword);
            modelMap.put("count", keywords.get(keyword).size());
            response.add(modelMap);
        }

        return generateJSONResponseMAV(true, response, "");
    }
}
