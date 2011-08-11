package org.auscope.portal.server.web.service;

import java.util.Collection;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.mineraloccurrence.Commodity;
import org.auscope.portal.mineraloccurrence.CommodityFilter;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.web.WFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Concrete implementation of the CommodityService interface.
 *
 * @author Jarek Sanders
 * @version $Id$
 */
@Service
public class CommodityServiceImpl implements CommodityService{

    // -------------------------------------------------------------- Constants

    protected final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance variables

    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private WFSGetFeatureMethodMaker methodMaker;


    // ----------------------------------------------------------- Constructors

    @Autowired
    public CommodityServiceImpl( HttpServiceCaller httpServiceCaller,
                                 MineralOccurrencesResponseHandler respHandler,
                                 WFSGetFeatureMethodMaker methodMaker ) {
        this.httpServiceCaller = httpServiceCaller;
        this.mineralOccurrencesResponseHandler = respHandler;
        this.methodMaker = methodMaker;
    }


    // ------------------------------------------- Property Setters and Getters



    private Collection<Commodity> get(String serviceURL, String commodityName, FilterBoundingBox bbox, int maxFeatures)
    throws Exception {
        HttpMethodBase method = null;

        CommodityFilter commodityFilter = new CommodityFilter(commodityName);
        String filterString = null;
        String srsName = null;

        if (bbox != null) {
            filterString = commodityFilter.getFilterStringBoundingBox(bbox);
            srsName = bbox.getBboxSrs();
        } else {
            filterString = commodityFilter.getFilterStringAllRecords();
        }

        method = methodMaker.makeMethod(serviceURL, "er:Commodity", filterString, maxFeatures, srsName);


        // Call the service, and get all the commodities
        String commodityResponse = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());

        //parse the commodities and return them
        try {
            return this.mineralOccurrencesResponseHandler.getCommodities(commodityResponse);
        } catch (Exception ex) {
            log.error(ex);
            log.debug(String.format("Error Args - serviceUrl='%1$s' commodityName='%2$s' bbox='%3$s' maxFeatures=%4$d", serviceURL, commodityName, bbox, maxFeatures), ex);
            log.trace(String.format("filterString='%1$s'", filterString));
            throw ex;
        }
    }

    public Collection<Commodity> getAll(String serviceURL, String commodityName, int maxFeatures)
            throws Exception {
        return get(serviceURL, commodityName, null, maxFeatures);
    }

    public Collection<Commodity> getVisible(String serviceURL, String commodityName, FilterBoundingBox bbox, int maxFeatures) throws Exception {
        return get(serviceURL, commodityName, bbox, maxFeatures);
    }
}
