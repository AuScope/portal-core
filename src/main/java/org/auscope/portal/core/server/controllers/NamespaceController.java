package org.auscope.portal.core.server.controllers;

import org.auscope.portal.core.services.NamespaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Contains methods for listing the service URLs with their cached
 * namespaces
 */
@Controller
public class NamespaceController extends BasePortalController {

    private NamespaceService namespaceService;

    @Autowired
    public NamespaceController(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }


    @RequestMapping("/getNamespaces.do")
    public ModelAndView getNamespaces(){
        try {
            Map namespaces = this.namespaceService.getNamespaceCache();
            return generateJSONResponseMAV(true, namespaces,"Service names spaces");
        } catch (Exception e) {
            log.warn(String.format("Error returning namespace cache: %1$s", e));
            log.debug("Exception:", e);
            return generateJSONResponseMAV(false);
        }

    }

    @RequestMapping("/updateNamespaceCache.do")
    public ModelAndView updateNamespaceCache(){
        try {
            this.namespaceService.updateNamespaces();
            return generateJSONResponseMAV(true);
        } catch (Exception e) {
            log.warn(String.format("Error updating namespace cache: %1$s", e));
            log.debug("Exception:", e);
            return generateJSONResponseMAV(false);
        }
    }
}
