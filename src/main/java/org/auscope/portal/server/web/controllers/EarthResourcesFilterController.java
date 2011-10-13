package org.auscope.portal.server.web.controllers;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.ErrorMessages;
import org.auscope.portal.server.web.service.CommodityService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
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
public class EarthResourcesFilterController extends BaseWFSToKMLController {

    // -------------------------------------------------------------- Constants

    /** Query all mines command */
    private static String ALL_MINES = "";

    // ----------------------------------------------------- Instance variables

    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private MineralOccurrenceService mineralOccurrenceService;
    private CommodityService commodityService;



    // ----------------------------------------------------------- Constructors

    @Autowired
    public EarthResourcesFilterController
        ( MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler,
          MineralOccurrenceService mineralOccurrenceService,
          GmlToKml gmlToKml,
          CommodityService commodityService,
          HttpServiceCaller httpServiceCaller
          ) {

        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
        this.mineralOccurrenceService = mineralOccurrenceService;
        this.gmlToKml = gmlToKml;
        this.commodityService = commodityService;
        this.httpServiceCaller = httpServiceCaller;
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
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
            HttpServletRequest request) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        HttpMethodBase method = null;
        try {
            String gmlBlob;
            if (mineName.equals(ALL_MINES)) {//get all mines
                if (bbox == null){
                    method = this.mineralOccurrenceService.getAllMinesGML(serviceUrl, maxFeatures);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
                else{
                    method = this.mineralOccurrenceService.getAllVisibleMinesGML(serviceUrl, bbox, maxFeatures);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
            } else {
                if (bbox == null){
                    method = this.mineralOccurrenceService.getMineWithSpecifiedNameGML(serviceUrl, mineName, maxFeatures);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
                else{
                    method = this.mineralOccurrenceService.getVisibleMineWithSpecifiedNameGML(serviceUrl, mineName, maxFeatures, bbox);
                    gmlBlob = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
                }
            }

            String kmlBlob =  convertMineResponseToKml(gmlBlob, request, serviceUrl);

            //log.debug(kmlBlob);
            //This failure test should be made a little bit more robust
            //And should probably try to extract an error message
            if (kmlBlob == null || kmlBlob.length() == 0) {
                log.error(String.format("Transform failed serviceUrl='%1$s' gmlBlob='%2$s'",serviceUrl, gmlBlob));
                return makeModelAndViewFailure(ErrorMessages.OPERATION_FAILED ,method);
            } else {
                return makeModelAndViewKML(kmlBlob, gmlBlob, method);
            }
        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl, method);
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
        @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
        HttpServletRequest request) throws Exception
    {
        //The presence of a bounding box causes us to assume we will be using this GML for visualising on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        HttpMethodBase method = null;
        try {
            //get the mineral occurrences
            String mineralOccurrenceResponse = null;
            if (bbox == null) {
                method = this.mineralOccurrenceService.getMineralOccurrenceGML (
                        serviceUrl,
                        commodityName,
                        measureType,
                        minOreAmount,
                        minOreAmountUOM,
                        minCommodityAmount,
                        minCommodityAmountUOM,
                        maxFeatures);
            } else {
                method = this.mineralOccurrenceService.getVisibleMineralOccurrenceGML (
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

            mineralOccurrenceResponse = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

            // If there are 0 features then send NO_RESULTS message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(mineralOccurrenceResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS, method);

            //if everything is good then return the KML
            return makeModelAndViewKML(convertToKml(mineralOccurrenceResponse, request, serviceUrl), mineralOccurrenceResponse, method);

        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl, method);

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
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures,
            HttpServletRequest request)
    throws Exception
    {
        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        HttpMethodBase method = null;;
        try {
            // Get the mining activities
            String miningActivityResponse = null;
            if (bbox == null) {
                method = this.mineralOccurrenceService.getMiningActivityGML( serviceUrl
                                                                    , mineName
                                                                    , startDate
                                                                    , endDate
                                                                    , oreProcessed
                                                                    , producedMaterial
                                                                    , cutOffGrade
                                                                    , production
                                                                    , maxFeatures);
                miningActivityResponse = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

            } else {
                method = this.mineralOccurrenceService.getVisibleMiningActivityGML( serviceUrl
                                                                            , mineName
                                                                            , startDate
                                                                            , endDate
                                                                            , oreProcessed
                                                                            , producedMaterial
                                                                            , cutOffGrade
                                                                            , production
                                                                            , maxFeatures
                                                                            , bbox);
                miningActivityResponse = this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

            }
            // If there are 0 features then send NO_RESULTS message to the user
            if (mineralOccurrencesResponseHandler.getNumberOfFeatures(miningActivityResponse) == 0)
                return makeModelAndViewFailure(ErrorMessages.NO_RESULTS, method);

            return makeModelAndViewKML(convertToKml(miningActivityResponse, request, serviceUrl), miningActivityResponse, method);

        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl, method);
        }
    }

    // ------------------------------------------------------ Protected Methods

    // ------------------------------------------------------ Private Methods


    /**
     * Assemble a call to convert GeoSciML into kml format
     * @param geoXML
     * @param httpRequest
     * @param serviceUrl
     */
    private String convertMineResponseToKml(String geoXML, HttpServletRequest httpRequest, String serviceUrl) {
        InputStream inXSLT = httpRequest.getSession().getServletContext().getResourceAsStream("/WEB-INF/xsl/mine-kml.xslt");

        return gmlToKml.convert(geoXML, inXSLT, serviceUrl);
    }

}
