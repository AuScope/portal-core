package org.auscope.portal.server.web.service;

import java.util.Collection;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.mineraloccurrence.Commodity;
import org.auscope.portal.mineraloccurrence.CommodityFilter;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.WFSGetFeatureMethodMakerPOST;
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
    private IWFSGetFeatureMethodMaker methodMaker;

    
    // ----------------------------------------------------------- Constructors
    
    public CommodityServiceImpl() {
        this.httpServiceCaller = new HttpServiceCaller();
        this.mineralOccurrencesResponseHandler = new MineralOccurrencesResponseHandler();
        this.methodMaker = new WFSGetFeatureMethodMakerPOST();
    }
    
    
    // ------------------------------------------- Property Setters and Getters
    
    public Collection<Commodity> get(String serviceURL, String commodityName) 
    throws Exception {
        
        HttpMethodBase method = null;
        
        // If we don't have a name, then just get all of them
        if (commodityName.equals("")) {
            method = methodMaker.makeMethod(serviceURL, "er:Commodity", "");
        } else {
            // Create the filter to append to the url
            CommodityFilter commodityFilter = new CommodityFilter(commodityName);
            log.debug(serviceURL + "\n" + commodityFilter.getFilterString());
            
            //create a GetFeature request with filter constraints on a query
            method = methodMaker.makeMethod ( serviceURL
                                            , "er:Commodity"
                                            , commodityFilter.getFilterString());
        }

        // Call the service, and get all the commodities
        String commodityResponse = httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        
        log.debug("................Commodity response");
        log.debug(commodityResponse);

        //parse the commodities and return them                
        return this.mineralOccurrencesResponseHandler.getCommodities(commodityResponse);
    }
}
