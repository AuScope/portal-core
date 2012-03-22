package org.auscope.portal.server.web.controllers;

import java.util.List;

import org.auscope.portal.server.domain.geodesy.GeodesyObservation;
import org.auscope.portal.server.web.service.GeodesyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller class for exposing GeodesyService methods
 * @author Josh Vote
 */
@Controller
public class GeodesyController extends BasePortalController {
    private GeodesyService geodesyService;

    @Autowired
    public GeodesyController(GeodesyService geodesyService) {
        this.geodesyService = geodesyService;
    }

    /**
     * Returns a set of GeodesyObservation objects for the specified filter parameters
     * @param stationId ID of the station
     * @param startDate start date range as YYYY-mm-DD
     * @param endDate end date range as YYYY-mm-DD
     * @param serviceUrl WFS url to request features from
     * @return
     */
    @RequestMapping("/getGeodesyObservations.do")
    public ModelAndView getGeodesyObservations(@RequestParam("stationId") String stationId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("serviceUrl") String serviceUrl) {

        List<GeodesyObservation> response = null;
        try {
            response = geodesyService.getObservationsForStation(serviceUrl, stationId, startDate, endDate);
        } catch (Exception ex) {
            log.warn(String.format("Failure requesting geodesy observations from '%1$s': %2$s", serviceUrl, ex));
            log.debug("Exception: ", ex);
            return this.generateExceptionResponse(ex, serviceUrl);
        }

        return this.generateJSONResponseMAV(true, response, "");
    }
}
