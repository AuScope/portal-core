package org.auscope.portal.core.server.controllers;

import org.auscope.portal.core.services.GoogleCloudMonitoringCachedService;
import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayerGrouping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Contains methods for requesting the list of known feature types
 *
 * @author Josh Vote
 *
 */
@Controller
public class KnownLayerController extends BaseCSWController {

    /** Used for requesting groupings of CSWRecords under known layers */
    private KnownLayerService knownLayerService;

    private GoogleCloudMonitoringCachedService stackDriverCachedService = null;

    @Autowired
    public KnownLayerController(KnownLayerService knownLayerService,
            ViewKnownLayerFactory viewFactory, ViewCSWRecordFactory viewCSWRecordFactory) {
        super(viewCSWRecordFactory, viewFactory);
        this.knownLayerService = knownLayerService;
    }
    @Autowired(required = false)
    public void setCachedService(GoogleCloudMonitoringCachedService service) {
        this.stackDriverCachedService = service;
    }
    /**
     * Gets a JSON response which contains the representations of each and every "KnownFeatureTypeDefinition".
     *
     * Each KnownFeatureTypeDefinition will map [0, N] CSWRecords with display information.
     *
     * @return
     */
    @RequestMapping("getKnownLayers.do")
    public ModelAndView getKnownLayers() {
        KnownLayerGrouping grouping = knownLayerService.groupKnownLayerRecords();
        if (stackDriverCachedService != null) {
            return generateKnownLayerResponse(grouping.getKnownLayers(), stackDriverCachedService);
        } else {
            return generateKnownLayerResponse(grouping.getKnownLayers());
        }
    }

    /**
     * Gets a JSON response which contains the representations of each and every "KnownFeatureTypeDefinition".
     *
     * Each KnownFeatureTypeDefinition will map [0, N] CSWRecords with display information.
     *
     * @return
     */
    @RequestMapping("getUnmappedCSWRecords.do")
    public ModelAndView getUnmappedCSWRecords() {
        KnownLayerGrouping grouping = knownLayerService.groupKnownLayerRecords();

        return generateCSWRecordResponse(grouping.getUnmappedRecords());
    }
}