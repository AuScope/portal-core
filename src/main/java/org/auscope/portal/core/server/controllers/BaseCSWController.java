package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerAndRecords;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Base class for all controllers that intend on returning CSWRecords
 *
 * @author Josh Vote
 *
 */
public abstract class BaseCSWController extends BasePortalController {
    /** Used for converting data to something the view can understand */
    protected ViewCSWRecordFactory viewCSWRecordFactory;

    /** Used for converting data to something the view can understand */
    private ViewKnownLayerFactory viewKnownLayerFactory;

    protected BaseCSWController(final ViewCSWRecordFactory viewCSWRecordFactory, final ViewKnownLayerFactory viewKnownLayerFactory) {
        this.viewCSWRecordFactory = viewCSWRecordFactory;
        this.viewKnownLayerFactory = viewKnownLayerFactory;
    }

    /**
     * Utility for generating a response model that represents a number of CSWRecord objects
     *
     * @param records
     *            The records to transform
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(final CSWRecord[] records) {
        return generateJSONResponseMAV(records, null);
    }

    /**
     * Utility for generating a response model that represents a number of CSWRecord objects
     *
     * @param records
     *            The records to transform
     * @param matchedResults
     *            The total number of records available (which may differ from records.length)
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(final CSWRecord[] records, final Integer matchedResults) {
        if (records == null) {
            return generateJSONResponseMAV(false, new CSWRecord[] {}, "");
        }

        final List<ModelMap> recordRepresentations = new ArrayList<>();

        try {
            for (final CSWRecord record : records) {
                recordRepresentations.add(viewCSWRecordFactory.toView(record));
            }
        } catch (final Exception ex) {
            log.error("Error converting data records", ex);
            return generateJSONResponseMAV(false, new CSWRecord[] {}, 0, "Error converting data records");
        }
        return generateJSONResponseMAV(true, recordRepresentations, matchedResults, "No errors");
    }

    /**
     * Utility for generating a response model that represents a number of KnownLayerAndRecord objects
     *
     * @param knownLayers
     *            The known layers to transform
     * @return
     */
    protected ModelAndView generateKnownLayerResponse(final List<KnownLayerAndRecords> knownLayers) {
        final List<ModelMap> viewKnownLayers = new ArrayList<>();
        for (final KnownLayerAndRecords knownLayerAndRecords : knownLayers) {
            final KnownLayer kl = knownLayerAndRecords.getKnownLayer();
            if (kl.isHidden()) {
                continue; //any hidden layers will NOT be sent to the view
            }
            final ModelMap viewKnownLayer = viewKnownLayerFactory.toView(knownLayerAndRecords.getKnownLayer());

            final List<ModelMap> viewMappedRecords = new ArrayList<>();
            for (final CSWRecord rec : knownLayerAndRecords.getBelongingRecords()) {
                if (rec != null) {
                    viewMappedRecords.add(viewCSWRecordFactory.toView(rec));
                }
            }

            final List<ModelMap> viewRelatedRecords = new ArrayList<>();
            for (final CSWRecord rec : knownLayerAndRecords.getRelatedRecords()) {
                if (rec != null) {
                    viewRelatedRecords.add(viewCSWRecordFactory.toView(rec));
                }
            }

            viewKnownLayer.put("cswRecords", viewMappedRecords);
            viewKnownLayer.put("relatedRecords", viewRelatedRecords);
            viewKnownLayers.add(viewKnownLayer);
        }

        return generateJSONResponseMAV(true, viewKnownLayers, "");
    }

    /**
     * Utility for generating a response model that represents a number of CSWRecord objects
     *
     * @param records
     * @return
     */
    protected ModelAndView generateCSWRecordResponse(final List<CSWRecord> records) {
        final List<ModelMap> viewRecords = new ArrayList<>();
        for (final CSWRecord rec : records) {
            if (rec.getServiceName() == null || rec.getServiceName().isEmpty()) {
                continue;//dont include any records with an empty name (it looks bad)
            }

            viewRecords.add(viewCSWRecordFactory.toView(rec));
        }

        return generateJSONResponseMAV(true, viewRecords, "");
    }
}
