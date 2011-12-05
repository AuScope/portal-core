package org.auscope.portal.server.web.service;

import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.mineraloccurrence.Mine;
import org.auscope.portal.mineraloccurrence.MineFilter;
import org.auscope.portal.mineraloccurrence.MineralOccurrenceFilter;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.mineraloccurrence.MiningActivityFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages mineral occurrence queries
 *
 * @version $Id$
 */
@Service
public class MineralOccurrenceService {

    // -------------------------------------------------------------- Constants

    private final Log log = LogFactory.getLog(getClass());

    // ----------------------------------------------------- Instance variables

    private HttpServiceCaller httpServiceCaller;
    private GmlToKml gmlToKml;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private WFSGetFeatureMethodMaker methodMaker;


    // ----------------------------------------------------------- Constructors

/*
    public MineralOccurrenceService() {
log.info(".......default C'tor");
        this.httpServiceCaller = new HttpServiceCaller();
        this.mineralOccurrencesResponseHandler = new MineralOccurrencesResponseHandler();
        this.methodMaker = new WFSGetFeatureMethodMakerPOST();
    }
*/
    @Autowired
    public MineralOccurrenceService(HttpServiceCaller httpServiceCaller,
                                     MineralOccurrencesResponseHandler respHandler,
                                     WFSGetFeatureMethodMaker methodMaker,
                                     GmlToKml gmlToKml) {
        this.httpServiceCaller = httpServiceCaller;
        this.mineralOccurrencesResponseHandler = respHandler;
        this.methodMaker = methodMaker;
        this.gmlToKml = gmlToKml;
    }


    // ------------------------------------------- Property Setters and Getters

    /**
     * Get all the mines from a given service url and return them as Mine objects
     *
     * @param serviceURL - the service to get all of the mines from
     * @return a collection (List) of mine nodes
     * @throws Exception
     */
    public List<Mine> getAllMines(String serviceURL, int maxFeatures) throws Exception {
        //get the mines
        WFSKMLResponse response = this.getAllMinesGML(serviceURL, maxFeatures);

        //convert the response into a nice collection of Mine Nodes
        List<Mine> mines = this.mineralOccurrencesResponseHandler.getMines(response.getGml());

        //send it back!
        return mines;
    }

    /**
     * Get all the mines from a given service url that lie within the bounding box
     * and return them as Mine objects. Limited to 200 response objects
     *
     * @param serviceURL - the service to get all of the mines from
     * @return a collection (List) of mine nodes
     * @throws Exception
     */
    public List<Mine> getAllVisibleMines(String serviceURL, FilterBoundingBox bbox, int maxFeatures) throws Exception {
        //get the mines
        WFSKMLResponse response = this.getAllVisibleMinesGML(serviceURL, bbox, maxFeatures);

        //convert the response into a nice collection of Mine Nodes
        List<Mine> mines = this.mineralOccurrencesResponseHandler.getMines(response.getGml());

        //send it back!
        return mines;
    }

    /**
     * Similar to getAllMinesGML this returns all mines but filters so they appear in the specified bounding box (response limited to 200 results)
     * @param serviceURL
     * @param bboxSrs
     * @param lowerCornerPoints
     * @param upperCornerPoints
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getAllVisibleMinesGML(String serviceURL, FilterBoundingBox bbox, int maxFeatures) throws Exception {
        MineFilter filter = new MineFilter("");

        log.debug("Mine query... url:" + serviceURL);
        log.trace("Mine query... filter:" + filter.getFilterStringBoundingBox(bbox));

        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MiningFeatureOccurrence", filter.getFilterStringBoundingBox(bbox), maxFeatures,  bbox.getBboxSrs());

        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }

    /**
     * Given a specific service and a mineName, get that mine from the service
     * @param  serviceURL - the service to get the mine from
     * @param  mineName - the name of the mine to get
     * @return the list collection of Mine node objects
     */
    public List<Mine> getMineWithSpecifiedName(String serviceURL, String mineName, int maxFeatures) throws Exception {
        //get the mine
        WFSKMLResponse response = this.getMineWithSpecifiedNameGML(serviceURL, mineName, maxFeatures);

        //convert the response into a collection of Mine Nodes
        List<Mine> mines = this.mineralOccurrencesResponseHandler.getMines(response.getGml());

        //send it back!
        return mines;
    }

    /**
     * Get all the mines from a given service url and return the response
     * @param serviceURL
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getAllMinesGML(String serviceURL, int maxFeatures) throws Exception {
        MineFilter filter = new MineFilter("");

        log.debug("Mine query... url:" + serviceURL);
        log.trace("Mine query... filter: " + filter.getFilterStringAllRecords());

        //create a GetFeature request with an empty filter - get all
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MiningFeatureOccurrence", filter.getFilterStringAllRecords(), maxFeatures);

        //call the service, and get all the mines
        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }

    /**
     * Given a specific service and a mineName, get that mine from the service
     *
     * @param serviceURL
     * @param mineName
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getMineWithSpecifiedNameGML(String serviceURL, String mineName, int maxFeatures) throws Exception {
        //create a filter for the specified name
        MineFilter mineFilter = new MineFilter(mineName);

        log.debug("Mine query... url:" + serviceURL);
        log.trace("Mine query... filter:" + mineFilter.getFilterStringAllRecords());

        //create a GetFeature request with filter constraints on a query
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MiningFeatureOccurrence", mineFilter.getFilterStringAllRecords(), maxFeatures);

        //call the service, and get all the mines
        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }

    /**
     * Given a specific service and a mineName, get that mine from the service (that lie within the bbox)
     *
     * Limited to 200 results
     *
     * @param serviceURL
     * @param mineName
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getVisibleMineWithSpecifiedNameGML(String serviceURL, String mineName, int maxFeatures, FilterBoundingBox bbox) throws Exception {
        //create a filter for the specified name
        MineFilter mineFilter = new MineFilter(mineName);

        log.debug("Mine query... url:" + serviceURL);
        log.trace("Mine query... filter:" + mineFilter.getFilterStringBoundingBox(bbox));

        //create a GetFeature request with filter constraints on a query
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MiningFeatureOccurrence", mineFilter.getFilterStringBoundingBox(bbox), maxFeatures, bbox.getBboxSrs());

        //call the service, and get all the mines
        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }


    /**
     * Given a list of parameters, call a service and get the Mineral Occurrence GML
     * @param serviceURL
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param cutOffGrade
     * @param cutOffGradeUOM
     * @return
     */
    public WFSKMLResponse  getMineralOccurrenceGML(String serviceURL,
                                           String commodityName,
                                           String measureType,
                                           String minOreAmount,
                                           String minOreAmountUOM,
                                           String minCommodityAmount,
                                           String minCommodityAmountUOM,
                                           int maxFeatures
                                           ) throws Exception {

        MineralOccurrenceFilter mineralOccurrenceFilter
            = new MineralOccurrenceFilter(commodityName,
                                           measureType,
                                           minOreAmount,
                                           minOreAmountUOM,
                                           minCommodityAmount,
                                           minCommodityAmountUOM
                                           );

        log.debug("Mineral Occurence query... url:" + serviceURL);
        log.trace("Mineral Occurence query... filter:" + mineralOccurrenceFilter.getFilterStringAllRecords());

        log.debug("\n" + serviceURL + "\n" + mineralOccurrenceFilter.getFilterStringAllRecords());

        //create the method
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "gsml:MappedFeature", mineralOccurrenceFilter.getFilterStringAllRecords(), maxFeatures);

        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }

    /**
     * Given a list of parameters, call a service and get the Mineral Occurrence GML for
     * all occurences that lie within the bbox (limited to 200 results)
     * @param serviceURL
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param cutOffGrade
     * @param cutOffGradeUOM
     * @return
     */
    public WFSKMLResponse getVisibleMineralOccurrenceGML(String serviceURL,
                                           String commodityName,
                                           String measureType,
                                           String minOreAmount,
                                           String minOreAmountUOM,
                                           String minCommodityAmount,
                                           String minCommodityAmountUOM,
                                           int maxFeatures,
                                           FilterBoundingBox bbox

                                           ) throws Exception {

        MineralOccurrenceFilter mineralOccurrenceFilter
            = new MineralOccurrenceFilter(commodityName,
                                           measureType,
                                           minOreAmount,
                                           minOreAmountUOM,
                                           minCommodityAmount,
                                           minCommodityAmountUOM);

        log.debug("Mineral Occurence query... url:" + serviceURL);
        log.trace("Mineral Occurence query... filter:" + mineralOccurrenceFilter.getFilterStringBoundingBox(bbox));


        //create the method
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "gsml:MappedFeature", mineralOccurrenceFilter.getFilterStringBoundingBox(bbox), maxFeatures, bbox.getBboxSrs());

        //run the dam query
        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }


    public WFSKMLResponse getMiningActivityGML(String serviceURL,
                                        String mineName,
                                        String startDate,
                                        String endDate,
                                        String oreProcessed,
                                        String producedMaterial,
                                        String cutOffGrade,
                                        String production,
                                        int maxFeatures
                                        ) throws Exception {

        //create the filter
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(mineName, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

        log.debug("Mining Activity query... url:" + serviceURL);
        log.trace("Mining Activity query... filter:" + miningActivityFilter.getFilterStringAllRecords());

        //create the method
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MiningFeatureOccurrence", miningActivityFilter.getFilterStringAllRecords(), maxFeatures);
        log.debug("After methodMaker.makeMethod");

        //run query
        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }

    public WFSKMLResponse getVisibleMiningActivityGML(String serviceURL,
            String mineName,
            String startDate,
            String endDate,
            String oreProcessed,
            String producedMaterial,
            String cutOffGrade,
            String production,
            int maxFeatures,
            FilterBoundingBox bbox
            ) throws Exception {

        //create the filter
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(mineName, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

        log.debug("Mining Activity query... url:" + serviceURL);
        log.trace("Mining Activity query... filter:" + miningActivityFilter.getFilterStringBoundingBox(bbox));


        //create the method
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MiningFeatureOccurrence", miningActivityFilter.getFilterStringBoundingBox(bbox), maxFeatures, bbox.getBboxSrs());

        //run query
        String responseGml = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        String responseKml = gmlToKml.convert(responseGml, serviceURL);

        return new WFSKMLResponse(responseGml, responseKml, method);
    }
}
