package org.auscope.portal.server.web.controllers;

import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
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
 * @author Josh Vote
 */
@Controller
public class EarthResourcesFilterController extends BasePortalController {

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
            WFSKMLResponse response = this.mineralOccurrenceService.getMinesGml(serviceUrl, mineName, bbox, maxFeatures);

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            log.warn("Error performing mine filter: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles getting the count of the Earth Resource Mine filter queries.
     * (If the bbox elements are specified, they will limit the output response to 200 records implicitly)
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineFilterCount.do")
    public ModelAndView doMineFilterCount(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            WFSCountResponse response = this.mineralOccurrenceService.getMinesCount(serviceUrl, mineName, bbox, maxFeatures);
            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn("Error performing mine count: ", e);
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
            WFSKMLResponse response = this.mineralOccurrenceService.getMineralOccurrenceGml(
                    serviceUrl,
                    commodityName,
                    measureType,
                    minOreAmount,
                    minOreAmountUOM,
                    minCommodityAmount,
                    minCommodityAmountUOM,
                    maxFeatures,
                    bbox);

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            log.warn("Error performing mineral occurrence filter: ", e);
            return this.generateExceptionResponse(e, serviceUrl);

        }
    }

    /**
     * Handles counting the results of a Earth Resource MineralOccerrence filter query.
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
     * @return Returns Integer count
     * @throws Exception
     */
    @RequestMapping("/doMineralOccurrenceFilterCount.do")
    public ModelAndView doMineralOccurrenceFilterCount(
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
            WFSCountResponse response = this.mineralOccurrenceService.getMineralOccurrenceCount(
                    serviceUrl,
                    commodityName,
                    measureType,
                    minOreAmount,
                    minOreAmountUOM,
                    minCommodityAmount,
                    minCommodityAmountUOM,
                    maxFeatures,
                    bbox);

            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn("Error performing mineral occurrence count: ", e);
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
            WFSKMLResponse response = this.mineralOccurrenceService.getMiningActivityGml(serviceUrl
                    , mineName
                    , startDate
                    , endDate
                    , oreProcessed
                    , producedMaterial
                    , cutOffGrade
                    , production
                    , maxFeatures
                    , bbox);

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            log.warn("Error performing mining activity filter: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles counting the number Mining Activities matched by a filter
     * Returns Integer count
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
     * @return Returns Integer count
     * @throws Exception
     */
    @RequestMapping("/doMiningActivityFilterCount.do")
    public ModelAndView doMiningActivityFilterCount(
            @RequestParam("serviceUrl")       String serviceUrl,
            @RequestParam("mineName")         String mineName,
            @RequestParam("startDate")        String startDate,
            @RequestParam("endDate")          String endDate,
            @RequestParam("oreProcessed")     String oreProcessed,
            @RequestParam("producedMaterial") String producedMaterial,
            @RequestParam("cutOffGrade")      String cutOffGrade,
            @RequestParam("production")       String production,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            // Get the mining activities
            WFSCountResponse response = this.mineralOccurrenceService.getMiningActivityCount(serviceUrl
                    , mineName
                    , startDate
                    , endDate
                    , oreProcessed
                    , producedMaterial
                    , cutOffGrade
                    , production
                    , maxFeatures
                    , bbox);

            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn("Error performing mining activity count: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }
}
