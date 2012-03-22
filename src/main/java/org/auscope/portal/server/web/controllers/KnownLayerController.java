package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.csw.record.CSWRecord;
import org.auscope.portal.server.domain.auscope.KnownLayerAndRecords;
import org.auscope.portal.server.domain.auscope.KnownLayerGrouping;
import org.auscope.portal.server.web.service.KnownLayerService;
import org.auscope.portal.server.web.view.KnownLayer;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.auscope.portal.server.web.view.ViewKnownLayerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Contains methods for requesting the list of known feature types
 * @author Josh Vote
 *
 */
@Controller
public class KnownLayerController extends BaseCSWController {

    /** Used for requesting groupings of CSWRecords under known layers*/
    private KnownLayerService knownLayerService;
    /** Used for converting data to something the view can understand*/
    private ViewKnownLayerFactory viewKnownLayerFactory;

    @Autowired
    public KnownLayerController(KnownLayerService knownLayerService,
            ViewKnownLayerFactory viewFactory, ViewCSWRecordFactory viewCSWRecordFactory) {
        super(viewCSWRecordFactory);
        this.knownLayerService = knownLayerService;
        this.viewKnownLayerFactory = viewFactory;
    }

    private ModelAndView generateKnownLayerResponse(List<KnownLayerAndRecords> knownLayers) {
        List<ModelMap> viewKnownLayers = new ArrayList<ModelMap>();
        for (KnownLayerAndRecords knownLayerAndRecords : knownLayers) {
            KnownLayer kl = knownLayerAndRecords.getKnownLayer();
            if (kl.isHidden()) {
                continue; //any hidden layers will NOT be sent to the view
            }
            ModelMap viewKnownLayer = viewKnownLayerFactory.toView(knownLayerAndRecords.getKnownLayer());

            List<ModelMap> viewMappedRecords = new ArrayList<ModelMap>();
            for (CSWRecord rec : knownLayerAndRecords.getBelongingRecords()) {
                viewMappedRecords.add(viewCSWRecordFactory.toView(rec));
            }

            List<ModelMap> viewRelatedRecords = new ArrayList<ModelMap>();
            for (CSWRecord rec : knownLayerAndRecords.getRelatedRecords()) {
                viewRelatedRecords.add(viewCSWRecordFactory.toView(rec));
            }

            viewKnownLayer.put("cswRecords", viewMappedRecords);
            viewKnownLayer.put("relatedRecords", viewRelatedRecords);
            viewKnownLayers.add(viewKnownLayer);
        }

        return generateJSONResponseMAV(true, viewKnownLayers, "");
    }

    private ModelAndView generateCSWRecordResponse(List<CSWRecord> records) {
        List<ModelMap> viewRecords = new ArrayList<ModelMap>();
        for (CSWRecord rec : records) {
            if (rec.getServiceName() == null || rec.getServiceName().isEmpty()) {
                continue;//dont include any records with an empty name (it looks bad)
            }
            viewRecords.add(viewCSWRecordFactory.toView(rec));
        }

        return generateJSONResponseMAV(true, viewRecords, "");
    }

    /**
     * Gets a JSON response which contains the representations of each and every "KnownFeatureTypeDefinition".
     *
     * Each KnownFeatureTypeDefinition will map [0, N] CSWRecords with display information.
     * @return
     */
    @RequestMapping("getKnownLayers.do")
    public ModelAndView getKnownLayers() {
        KnownLayerGrouping grouping = knownLayerService.groupKnownLayerRecords();

        return generateKnownLayerResponse(grouping.getKnownLayers());
    }

    /**
     * Gets a JSON response which contains the representations of each and every "KnownFeatureTypeDefinition".
     *
     * Each KnownFeatureTypeDefinition will map [0, N] CSWRecords with display information.
     * @return
     */
    @RequestMapping("getUnmappedCSWRecords.do")
    public ModelAndView getUnmappedCSWRecords() {
        KnownLayerGrouping grouping = knownLayerService.groupKnownLayerRecords();

        return generateCSWRecordResponse(grouping.getUnmappedRecords());
    }
}
