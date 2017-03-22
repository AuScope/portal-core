package org.auscope.portal.core.server.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.auscope.portal.core.services.Nagios4CachedService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse;
import org.auscope.portal.core.services.responses.nagios.ServiceStatusResponse.Status;
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

    protected BaseCSWController(ViewCSWRecordFactory viewCSWRecordFactory, ViewKnownLayerFactory viewKnownLayerFactory) {
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
     * Utility for generating a response model that represents a number of KnownLayerAndRecord objects
     *
     * @param knownLayers
     *            The known layers to transform
     * @return
     */
    protected ModelAndView generateKnownLayerResponse(List<KnownLayerAndRecords> knownLayers) {
        return generateKnownLayerResponse(knownLayers, null);
    }

    /**
     * Utility for generating a response model that represents a number of KnownLayerAndRecord objects. Also adds
     * service failure information from nagios to the layers (if recorded in the known layer).
     *
     * @param knownLayers
     *            The known layers to transform
     * @return
     */
    protected ModelAndView generateKnownLayerResponse(List<KnownLayerAndRecords> knownLayers, Nagios4CachedService nagiosService) {
        List<ModelMap> viewKnownLayers = new ArrayList<>();
        for (KnownLayerAndRecords knownLayerAndRecords : knownLayers) {
            KnownLayer kl = knownLayerAndRecords.getKnownLayer();
            if (kl.isHidden()) {
                continue; //any hidden layers will NOT be sent to the view
            }
            ModelMap viewKnownLayer = viewKnownLayerFactory.toView(knownLayerAndRecords.getKnownLayer());

            List<ModelMap> viewMappedRecords = new ArrayList<>();
            for (CSWRecord rec : knownLayerAndRecords.getBelongingRecords()) {
                if (rec != null) {
                    viewMappedRecords.add(viewCSWRecordFactory.toView(rec));
                }
            }

            List<ModelMap> viewRelatedRecords = new ArrayList<>();
            for (CSWRecord rec : knownLayerAndRecords.getRelatedRecords()) {
                if (rec != null) {
                    viewRelatedRecords.add(viewCSWRecordFactory.toView(rec));
                }
            }

            viewKnownLayer.put("cswRecords", viewMappedRecords);
            viewKnownLayer.put("relatedRecords", viewRelatedRecords);

            if (nagiosService != null && kl.getNagiosHostGroup() != null) {
                try {
                    Map<String, List<ServiceStatusResponse>> response = nagiosService.getStatuses(kl.getNagiosHostGroup());
                    List<String> failingHosts = new ArrayList<String>();
                    for (Entry<String, List<ServiceStatusResponse>> entry : response.entrySet()) {
                        for (ServiceStatusResponse status : entry.getValue()) {
                            if (status.getStatus() == Status.critical || status.getStatus() == Status.warning) {
                                failingHosts.add(entry.getKey());
                                break;
                            }
                        }
                    }

                    if (!failingHosts.isEmpty()) {
                        viewKnownLayer.put("nagiosFailingHosts", failingHosts);
                    }
                } catch (PortalServiceException ex) {
                    log.error("Error updating nagios hostgroup info for " + kl.getNagiosHostGroup() + " :" + ex.getMessage());
                }
            }

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
