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
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.util.GmlToHtml;
import org.auscope.portal.server.util.GmlToKml;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker.ResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages mineral occurrence queries
 *
 * @version $Id$
 */
@Service
public class MineralOccurrenceService extends BaseWFSService {

    // -------------------------------------------------------------- Constants

    private final Log log = LogFactory.getLog(getClass());
    public static final String MINE_FEATURE_TYPE = "er:MiningFeatureOccurrence";
    public static final String MINERAL_OCCURRENCE_FEATURE_TYPE = "gsml:MappedFeature";
    public static final String MINING_ACTIVITY_FEATURE_TYPE = "er:MiningFeatureOccurrence";

    // ----------------------------------------------------- Instance variables

    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;


    // ----------------------------------------------------------- Constructors

    @Autowired
    public MineralOccurrenceService(HttpServiceCaller httpServiceCaller,
                                     MineralOccurrencesResponseHandler respHandler,
                                     WFSGetFeatureMethodMaker methodMaker,
                                     GmlToKml gmlToKml,
                                     GmlToHtml gmlToHtml) {
        super(httpServiceCaller, methodMaker, gmlToKml, gmlToHtml);
        this.mineralOccurrencesResponseHandler = respHandler;
    }

    /**
     * Utility for turning a filter and optional bounding box into a OGC filter string
     * @param filter The filter
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @return
     */
    private String generateFilterString(IFilter filter, FilterBoundingBox bbox) {
        String filterString = null;
        if (bbox == null) {
            filterString = filter.getFilterStringAllRecords();
        } else {
            filterString = filter.getFilterStringBoundingBox(bbox);
        }

        log.trace(filterString);

        return filterString;
    }

    /**
     * Gets the GML/KML response for all mines matching the specified parameters
     * @param serviceUrl a Web Feature Service URL
     * @param mineName [Optional] The mine name to constrain the result set
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @param maxFeatures The maximum number of features to request
     * @return
     * @throws PortalServiceException
     */
    public WFSKMLResponse getMinesGml(String serviceUrl, String mineName, FilterBoundingBox bbox, int maxFeatures) throws PortalServiceException {
        MineFilter filter = new MineFilter(mineName);
        String filterString = generateFilterString(filter, bbox);

        HttpMethodBase method = generateWFSRequest(serviceUrl, MINE_FEATURE_TYPE, null, filterString, maxFeatures, null, ResultType.Results);
        return getWfsResponseAsKml(serviceUrl, method);
    }

    /**
     * Gets the parsed Mine response for all mines matching the specified parameters
     * @param serviceUrl a Web Feature Service URL
     * @param mineName [Optional] The mine name to constrain the result set
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @param maxFeatures The maximum number of features to request
     * @return
     * @throws PortalServiceException
     */
    public List<Mine> getMines(String serviceUrl, String mineName, FilterBoundingBox bbox, int maxFeatures) throws PortalServiceException {
        MineFilter filter = new MineFilter(mineName);
        String filterString = generateFilterString(filter, bbox);
        String srs = bbox == null ? null : bbox.getBboxSrs();

        HttpMethodBase method = generateWFSRequest(serviceUrl, MINE_FEATURE_TYPE, null, filterString, maxFeatures, srs, ResultType.Results);
        try {
            String response = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
            return mineralOccurrencesResponseHandler.getMines(response);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Gets the count of all mines matching the specified parameters
     * @param serviceUrl a Web Feature Service URL
     * @param mineName [Optional] The mine name to constrain the result set
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @param maxFeatures The maximum number of features to request
     * @return
     * @throws PortalServiceException
     */
    public WFSCountResponse getMinesCount(String serviceUrl, String mineName, FilterBoundingBox bbox, int maxFeatures) throws PortalServiceException {
        MineFilter filter = new MineFilter(mineName);
        String filterString = generateFilterString(filter, bbox);

        HttpMethodBase method = generateWFSRequest(serviceUrl, MINE_FEATURE_TYPE, null, filterString, maxFeatures, null, ResultType.Hits);
        return getWfsFeatureCount(method);
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
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @return
     */
    public WFSKMLResponse getMineralOccurrenceGml(String serviceURL,
                                           String commodityName,
                                           String measureType,
                                           String minOreAmount,
                                           String minOreAmountUOM,
                                           String minCommodityAmount,
                                           String minCommodityAmountUOM,
                                           int maxFeatures,
                                           FilterBoundingBox bbox) throws PortalServiceException {

        MineralOccurrenceFilter filter = new MineralOccurrenceFilter(commodityName,
                                           measureType,
                                           minOreAmount,
                                           minOreAmountUOM,
                                           minCommodityAmount,
                                           minCommodityAmountUOM);

        String filterString = generateFilterString(filter, bbox);
        HttpMethodBase method = generateWFSRequest(serviceURL, MINERAL_OCCURRENCE_FEATURE_TYPE, null, filterString, maxFeatures, null, ResultType.Results);
        return getWfsResponseAsKml(serviceURL, method);
    }

    /**
     * Given a list of parameters, call a service and get the count of Mineral Occurrence GML
     * @param serviceURL
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param cutOffGrade
     * @param cutOffGradeUOM
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @return
     */
    public WFSCountResponse getMineralOccurrenceCount(String serviceURL,
                                           String commodityName,
                                           String measureType,
                                           String minOreAmount,
                                           String minOreAmountUOM,
                                           String minCommodityAmount,
                                           String minCommodityAmountUOM,
                                           int maxFeatures,
                                           FilterBoundingBox bbox) throws PortalServiceException {

        MineralOccurrenceFilter filter = new MineralOccurrenceFilter(commodityName,
                                           measureType,
                                           minOreAmount,
                                           minOreAmountUOM,
                                           minCommodityAmount,
                                           minCommodityAmountUOM);

        String filterString = generateFilterString(filter, bbox);
        HttpMethodBase method = generateWFSRequest(serviceURL, MINERAL_OCCURRENCE_FEATURE_TYPE, null, filterString, maxFeatures, null, ResultType.Hits);
        return getWfsFeatureCount(method);
    }


    /**
     * Given a list of parameters, call a service and get the Mineral Activity features as GML/KML
     * @param serviceURL
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param maxFeatures
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @return
     * @throws Exception
     */
    public WFSKMLResponse getMiningActivityGml(String serviceURL,
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
        MiningActivityFilter filter = new MiningActivityFilter(mineName, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);
        String filterString = generateFilterString(filter, bbox);

        HttpMethodBase method = generateWFSRequest(serviceURL, MINING_ACTIVITY_FEATURE_TYPE, null, filterString, maxFeatures, null, ResultType.Results);
        return getWfsResponseAsKml(serviceURL, method);
    }

    /**
     * Given a list of parameters, call a service and get the count of Mineral Activity features
     * @param serviceURL
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param maxFeatures
     * @param bbox [Optional] the spatial bounds to constrain the result set
     * @return
     * @throws Exception
     */
    public WFSCountResponse getMiningActivityCount(String serviceURL,
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
        MiningActivityFilter filter = new MiningActivityFilter(mineName, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);
        String filterString = generateFilterString(filter, bbox);

        HttpMethodBase method = generateWFSRequest(serviceURL, MINING_ACTIVITY_FEATURE_TYPE, null, filterString, maxFeatures, null, ResultType.Hits);
        return getWfsFeatureCount(method);
    }
}
