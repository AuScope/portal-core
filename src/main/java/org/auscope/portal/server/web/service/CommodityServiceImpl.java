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

    /** The log. */
    private final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance variables

    /** The http service caller. */
    private HttpServiceCaller httpServiceCaller;

    /** The mineral occurrences response handler. */
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;

    /** The method maker. */
    private WFSGetFeatureMethodMaker methodMaker;


    // ----------------------------------------------------------- Constructors

    /**
     * Instantiates a new commodity service impl.
     *
     * @param httpServiceCaller the http service caller
     * @param respHandler the resp handler
     * @param methodMaker the method maker
     */
    @Autowired
    public CommodityServiceImpl(HttpServiceCaller httpServiceCaller,
                                MineralOccurrencesResponseHandler respHandler,
                                WFSGetFeatureMethodMaker methodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.mineralOccurrencesResponseHandler = respHandler;
        this.methodMaker = methodMaker;
    }


    // ------------------------------------------- Property Setters and Getters



    /**
     * Gets the.
     *
     * @param serviceURL the service url
     * @param commodityName the commodity name
     * @param bbox the bbox
     * @param maxFeatures the max features
     * @return the collection
     * @throws Exception the exception
     */
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

    /* (non-Javadoc)
     * @see org.auscope.portal.server.web.service.CommodityService#getAll(java.lang.String, java.lang.String, int)
     */
    public Collection<Commodity> getAll(String serviceURL, String commodityName, int maxFeatures)
            throws Exception {
        return get(serviceURL, commodityName, null, maxFeatures);
    }

    /* (non-Javadoc)
     * @see org.auscope.portal.server.web.service.CommodityService#getVisible(java.lang.String, java.lang.String, org.auscope.portal.server.domain.filter.FilterBoundingBox, int)
     */
    public Collection<Commodity> getVisible(String serviceURL, String commodityName, FilterBoundingBox bbox, int maxFeatures) throws Exception {
        return get(serviceURL, commodityName, bbox, maxFeatures);
    }
}
