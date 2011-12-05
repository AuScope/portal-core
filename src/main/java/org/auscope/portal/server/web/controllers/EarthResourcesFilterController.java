package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;

import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


/**
 * Controller that handles all Earth Resource related requests
 * <p>
 * It handles the following WFS features:
 * <ul>
 * <li>Mine</li>
 * <li>Mineral Occurrence</li>
 * <li>Mininig Activity</li>
 * </ul>
 * </p>
 *
 * @author Jarek Sanders
 * @version $Id$
 */
@Controller
public class EarthResourcesFilterController extends BasePortalController {

    // -------------------------------------------------------------- Constants

    /** Query all mines command */
    private static String ALLMINES = "";

    // ----------------------------------------------------- Instance variables

    private MineralOccurrenceService mineralOccurrenceService;



    // ----------------------------------------------------------- Constructors

    @Autowired
    public EarthResourcesFilterController(MineralOccurrenceService mineralOccurrenceService) {
        this.mineralOccurrenceService = mineralOccurrenceService;
    }

    // ------------------------------------------- Property Setters and Getters


    /**
     * Handles the Earth Resource Mine filter queries.
     * (If the bbox elements are specified, they will limit the output response to 200 records implicitly)
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineFilter.do")
    public ModelAndView doMineFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            WFSKMLResponse response = null;
            if (mineName.equals(ALLMINES)) {//get all mines
                if (bbox == null) {
                    response = this.mineralOccurrenceService.getAllMinesGML(serviceUrl, maxFeatures);
                } else {
                    response = this.mineralOccurrenceService.getAllVisibleMinesGML(serviceUrl, bbox, maxFeatures);
                }
            } else {
                if (bbox == null) {
                    response = this.mineralOccurrenceService.getMineWithSpecifiedNameGML(serviceUrl, mineName, maxFeatures);
                } else {
                    response = this.mineralOccurrenceService.getVisibleMineWithSpecifiedNameGML(serviceUrl, mineName, maxFeatures, bbox);
                }
            }

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl);
        }

    }


    /**
     * Handles the Earth Resource MineralOccerrence filter queries.
     *
     * @param serviceUrl
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param request                the HTTP client request
     *
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
        @RequestParam(value="serviceUrl",            required=false) String serviceUrl,
        @RequestParam(value="commodityName",         required=false) String commodityName,
        @RequestParam(value="measureType",           required=false) String measureType,
        @RequestParam(value="minOreAmount",          required=false) String minOreAmount,
        @RequestParam(value="minOreAmountUOM",       required=false) String minOreAmountUOM,
        @RequestParam(value="minCommodityAmount",    required=false) String minCommodityAmount,
        @RequestParam(value="minCommodityAmountUOM", required=false) String minCommodityAmountUOM,
        @RequestParam(required=false, value="bbox") String bboxJson,
        @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {
        //The presence of a bounding box causes us to assume we will be using this GML for visualising on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            //get the mineral occurrences
            WFSKMLResponse response = null;
            if (bbox == null) {
                response = this.mineralOccurrenceService.getMineralOccurrenceGML(
                        serviceUrl,
                        commodityName,
                        measureType,
                        minOreAmount,
                        minOreAmountUOM,
                        minCommodityAmount,
                        minCommodityAmountUOM,
                        maxFeatures);
            } else {
                response = this.mineralOccurrenceService.getVisibleMineralOccurrenceGML(
                            serviceUrl,
                            commodityName,
                            measureType,
                            minOreAmount,
                            minOreAmountUOM,
                            minCommodityAmount,
                            minCommodityAmountUOM,
                            maxFeatures,
                            bbox);
            }

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl);

        }
    }


    /**
     * Handles Mining Activity filter queries
     * Returns WFS response converted into KML.
     *
     * @param serviceUrl
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param request
     * @return the KML response
     * @throws Exception
     */
    @RequestMapping("/doMiningActivityFilter.do")
    public ModelAndView doMiningActivityFilter(
            @RequestParam("serviceUrl")       String serviceUrl,
            @RequestParam("mineName")         String mineName,
            @RequestParam("startDate")        String startDate,
            @RequestParam("endDate")          String endDate,
            @RequestParam("oreProcessed")     String oreProcessed,
            @RequestParam("producedMaterial") String producedMaterial,
            @RequestParam("cutOffGrade")      String cutOffGrade,
            @RequestParam("production")       String production,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures)
    throws Exception
    {
        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            // Get the mining activities
            WFSKMLResponse response = null;
            if (bbox == null) {
                response = this.mineralOccurrenceService.getMiningActivityGML(serviceUrl
                                                                    , mineName
                                                                    , startDate
                                                                    , endDate
                                                                    , oreProcessed
                                                                    , producedMaterial
                                                                    , cutOffGrade
                                                                    , production
                                                                    , maxFeatures);

            } else {
                response = this.mineralOccurrenceService.getVisibleMiningActivityGML(serviceUrl
                                                                            , mineName
                                                                            , startDate
                                                                            , endDate
                                                                            , oreProcessed
                                                                            , producedMaterial
                                                                            , cutOffGrade
                                                                            , production
                                                                            , maxFeatures
                                                                            , bbox);
            }

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }
}
