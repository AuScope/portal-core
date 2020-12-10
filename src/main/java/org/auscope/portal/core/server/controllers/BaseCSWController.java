package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.auscope.portal.core.services.GoogleCloudMonitoringCachedService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.stackdriver.ServiceStatusResponse;
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


    protected BaseCSWController(ViewCSWRecordFactory viewCSWRecordFactory) {
        this.viewCSWRecordFactory = viewCSWRecordFactory;
    }

    /**
     * Utility for generating a response model that represents a number of CSWRecord objects
     *
     * @param records
     *            The records to transform
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(CSWRecord[] records) {
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
    protected ModelAndView generateJSONResponseMAV(CSWRecord[] records, Integer matchedResults) {
        if (records == null) {
            return generateJSONResponseMAV(false, new CSWRecord[] {}, "");
        }

        List<ModelMap> recordRepresentations = new ArrayList<>();

        try {
            for (CSWRecord record : records) {
                recordRepresentations.add(viewCSWRecordFactory.toView(record));
            }
        } catch (Exception ex) {
            log.error("Error converting data records", ex);
            return generateJSONResponseMAV(false, new CSWRecord[] {}, 0, "Error converting data records");
        }
        return generateJSONResponseMAV(true, recordRepresentations, matchedResults, "No errors");
    }

    /**
     * Utility for generating a response model that represents a number of CSWRecord objects
     *
     * @param records
     * @return
     */
    protected ModelAndView generateCSWRecordResponse(List<CSWRecord> records) {
        List<ModelMap> viewRecords = new ArrayList<>();
        for (CSWRecord rec : records) {
            if (rec.getServiceName() == null || rec.getServiceName().isEmpty()) {
                continue;//dont include any records with an empty name (it looks bad)
            }

            viewRecords.add(viewCSWRecordFactory.toView(rec));
        }

        return generateJSONResponseMAV(true, viewRecords, "");
    }
}
