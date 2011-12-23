package org.auscope.portal.server.web.controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.admin.AdminDiagnosticResponse;
import org.auscope.portal.server.domain.admin.EndpointAndSelector;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.AdminService;
import org.auscope.portal.server.web.service.CSWServiceItem;
import org.auscope.portal.server.web.view.JSONView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides a controller interface into some basic administration functionality/tests
 * @author Josh Vote
 *
 */
@Controller
public class AdminController {

    private final Log log = LogFactory.getLog(getClass());

    /** For accessing the various CSW's*/
    private List<CSWServiceItem> cswServiceList;
    /** for checking config options*/
    private PortalPropertyPlaceholderConfigurer portalProperties;
    /** for actually performing diagnostics*/
    private AdminService adminService;




    /**
     * Creates a new instance of this class
     */
    @Autowired
    public AdminController(@Qualifier(value = "cswServiceList") ArrayList cswServiceList,
            PortalPropertyPlaceholderConfigurer portalProperties,
            AdminService adminService) {
        this.portalProperties = portalProperties;
        this.adminService = adminService;
        this.cswServiceList = new ArrayList<CSWServiceItem>();
        for (int i = 0; i < cswServiceList.size(); i++) {
            this.cswServiceList.add((CSWServiceItem) cswServiceList.get(i));
        }
    }

    /**
     * Generates a ModelAndView JSON response with the specified params
     * @param response a response from the AdminService
     * @return
     */
    private ModelAndView generateTestResponse(AdminDiagnosticResponse response) {
        JSONView view = new JSONView();
        ModelMap model = new ModelMap();

        model.put("success", response.isSuccess());
        model.put("warnings", response.getWarnings());
        model.put("errors", response.getErrors());
        model.put("details", response.getDetails());

        return new ModelAndView(view, model);

    }

    /**
     * Performs an external connectivity test through the HttpServiceCaller
     * @return
     * @throws MalformedURLException Should never occur
     */
    @RequestMapping("/testExternalConnectivity.diag")
    public ModelAndView testExternalConnectivity() throws MalformedURLException {
        URL[] urlsToTest = new URL[] {
            new URL("http://www.google.com"),
            new URL("https://www.google.com")
        };

        AdminDiagnosticResponse response = adminService.externalConnectivity(urlsToTest);
        return generateTestResponse(response);
    }

    /**
     * Performs an external connectivity test to the various CSW's through the HttpServiceCaller
     * @return
     */
    @RequestMapping("/testCSWConnectivity.diag")
    public ModelAndView testCSWConnectivity() {
        AdminDiagnosticResponse response = adminService.cswConnectivity(cswServiceList);
        return generateTestResponse(response);
    }

    /**
     * Tests that the Vocabulary service is up and running
     * @return
     */
    @RequestMapping("/testVocabulary.diag")
    public ModelAndView testVocabulary() throws Exception {

        //Has the user setup the portal with a valid vocabulary service?
        String vocabServiceUrl = portalProperties.resolvePlaceholder("HOST.vocabService.url");
        try {
            new URL(vocabServiceUrl);
        } catch (Exception ex) {
            AdminDiagnosticResponse error = new AdminDiagnosticResponse();
            error.addError(String.format("HOST.vocabService.url resolves into an invalid URL '%1$s'. Exception - %2$s", vocabServiceUrl, ex));
            return generateTestResponse(error); // no point proceeding in this case
        }

        AdminDiagnosticResponse response = adminService.vocabConnectivity(vocabServiceUrl);
        return generateTestResponse(response);
    }

    /**
     * Builds a list of EndpointAndSelector objects by combining an array of endpoints with an array of selectors.
     *
     * Duplicates are NOT included.
     *
     * @param endpoints The URL endpoints. Must be the same length as selectors
     * @param selectors The selector (WFS type name, WMS layer name etc). Must be the same length as endpoints
     * @return
     */
    private List<EndpointAndSelector> parseEndpointAndSelectors(String[] endpoints, String[] selectors) {
        //Make sure the view is calling this method correctly
        if (endpoints == null || selectors == null || endpoints.length != selectors.length) {
            throw new IllegalArgumentException("serviceUrls.length != typeNames.length");
        }

        //Build our list of endpoints (skip duplicates)
        List<EndpointAndSelector> result = new ArrayList<EndpointAndSelector>();
        for (int i = 0; i < endpoints.length; i++) {
            EndpointAndSelector eas = new EndpointAndSelector(endpoints[i], selectors[i]);
            if (!result.contains(eas)) {
                result.add(eas);
            }
        }

        return result;
    }

    /**
     * Tests that all serviceUrls + typeNames are accessible via WFS. There must be a 1-1 correspondence between serviceUrls and typeNames
     *
     * Any duplicated serviceUrl + typename combos will be culled
     *
     * This method is intentionally avoiding the WFSService to focus on the WFS request/response (ignoring the XSLT pipeline)
     * @return
     */
    @RequestMapping("/testWFS.diag")
    public ModelAndView testWFS(@RequestParam("serviceUrls") String[] serviceUrls,
                                @RequestParam("typeNames") String[] typeNames,
                                @RequestParam("bbox") String bboxJson) {

        //No point in proceeding with test without a valid bbox
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        if (bbox == null) {
            AdminDiagnosticResponse error = new AdminDiagnosticResponse();
            error.addError(String.format("The backend cannot parse the provided bbox string into a FilterBoundingBox - %1$s", bboxJson));
            return generateTestResponse(error);
        }

        //Build our list of endpoints (skip duplicates)
        List<EndpointAndSelector> endpoints = parseEndpointAndSelectors(serviceUrls, typeNames);

        //Do the diagnostics
        AdminDiagnosticResponse response = adminService.wfsConnectivity(endpoints, bbox);
        return generateTestResponse(response);
    }

    /**
     * Tests that all serviceUrls + layerNames are accessible via WMS. There must be a 1-1 correspondence between serviceUrls and layerNames
     *
     * Any duplicated serviceUrl + layer name combos will be culled
     * @return
     */
    @RequestMapping("/testWMS.diag")
    public ModelAndView testWMS(@RequestParam("serviceUrls") String[] serviceUrls,
                                @RequestParam("layerNames") String[] layerNames,
                                @RequestParam("bbox") String bboxJson) {

        //No point in proceeding with test without a valid bbox
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        if (bbox == null) {
            AdminDiagnosticResponse error = new AdminDiagnosticResponse();
            error.addError(String.format("The backend cannot parse the provided bbox string into a FilterBoundingBox - %1$s", bboxJson));
            return generateTestResponse(error);
        }

        //Build our list of endpoints (skip duplicates)
        List<EndpointAndSelector> endpoints = parseEndpointAndSelectors(serviceUrls, layerNames);

        //Do the diagnostics
        AdminDiagnosticResponse response = adminService.wmsConnectivity(endpoints, bbox);
        return generateTestResponse(response);
    }
}
