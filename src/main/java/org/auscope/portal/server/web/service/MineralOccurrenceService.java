package org.auscope.portal.server.web.service;

import java.util.Collection;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.mineraloccurrence.Commodity;
import org.auscope.portal.mineraloccurrence.Mine;
import org.auscope.portal.mineraloccurrence.MineFilter;
import org.auscope.portal.mineraloccurrence.MineralOccurrenceFilter;
import org.auscope.portal.mineraloccurrence.MineralOccurrencesResponseHandler;
import org.auscope.portal.mineraloccurrence.MiningActivityFilter;
import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;
import org.auscope.portal.server.web.WFSGetFeatureMethodMakerPOST;

import org.springframework.stereotype.Service;

/**
 * Manages mineral occurrence queries
 *
 * @version $Id$
 */
@Service
public class MineralOccurrenceService {

    // -------------------------------------------------------------- Constants
    
    protected final Log log = LogFactory.getLog(getClass());
    
    // ----------------------------------------------------- Instance variables
    
    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;
    private IWFSGetFeatureMethodMaker methodMaker;

    
    // ----------------------------------------------------------- Constructors
    
    public MineralOccurrenceService() {
        this.httpServiceCaller = new HttpServiceCaller();
        this.mineralOccurrencesResponseHandler = new MineralOccurrencesResponseHandler();
        this.methodMaker = new WFSGetFeatureMethodMakerPOST();
    }

    public MineralOccurrenceService(HttpServiceCaller httpServiceCaller, MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler, IWFSGetFeatureMethodMaker methodMaker) {
        this.httpServiceCaller = httpServiceCaller;
        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
        this.methodMaker = methodMaker;
    }

    
    // ------------------------------------------- Property Setters and Getters 
    
    /**
     * Get all the mines from a given service url and return them as Mine objects
     *
     * @param serviceURL - the service to get all of the mines from
     * @return a collection (List) of mine nodes 
     * @throws Exception
     */
    public List<Mine> getAllMines(String serviceURL) throws Exception {
        //get the mines
        String mineResponse = this.getAllMinesGML(serviceURL);

        //convert the response into a nice collection of Mine Nodes
        List<Mine> mines = this.mineralOccurrencesResponseHandler.getMines(mineResponse);

        //send it back!
        return mines;
    }

    
    /**
     * Get all the mines from a given service url and return the response
     * @param serviceURL
     * @return
     * @throws Exception
     */
    public String getAllMinesGML(String serviceURL) throws Exception {
        //create a GetFeature request with an empty filter - get all
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:Mine", "");

        //call the service, and get all the mines
        return httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
    }

    
    /**
     * Given a specific service and a mineName, get that mine from the service
     * @param  serviceURL - the service to get the mine from
     * @param  mineName - the name of the mine to get
     * @return the list collection of Mine node objects
     */
    public List<Mine> getMineWithSpecifiedName(String serviceURL, String mineName) throws Exception {
        //get the mine
        String mineResponse = this.getMineWithSpecifiedNameGML(serviceURL, mineName);

        //convert the response into a collection of Mine Nodes
        List<Mine> mines = this.mineralOccurrencesResponseHandler.getMines(mineResponse);

        //send it back!
        return mines;
    }

    
    /**
     * Given a specific service and a mineName, get that mine from the service
     *
     * @param serviceURL
     * @param mineName
     * @return
     * @throws Exception
     */
    public String getMineWithSpecifiedNameGML(String serviceURL, String mineName) throws Exception {
        //create a filter for the specified name
        MineFilter mineFilter = new MineFilter(mineName);

        log.debug("\n" + serviceURL + "\n" + mineFilter.getFilterString());

        //create a GetFeature request with filter constraints on a query
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:Mine", mineFilter.getFilterString());

        //call the service, and get all the mines
        return httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
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
    public String getMineralOccurrenceGML( String serviceURL,
                                           Collection<Commodity> commodities, // String commodityName,
                                           String measureType,
                                           String minOreAmount,
                                           String minOreAmountUOM,
                                           String minCommodityAmount,
                                           String minCommodityAmountUOM 
                                         ) throws Exception {
            
        MineralOccurrenceFilter mineralOccurrenceFilter 
            = new MineralOccurrenceFilter( commodities,
                                           measureType,
                                           minOreAmount,
                                           minOreAmountUOM,
                                           minCommodityAmount,
                                           minCommodityAmountUOM );

        log.debug("\n" + serviceURL + "\n" + mineralOccurrenceFilter.getFilterString());
        
        //create the method
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MineralOccurrence", mineralOccurrenceFilter.getFilterString());

        //run the dam query
        return httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
    }

    
    public String getMiningActivityGML( String serviceURL,
                                        List<Mine> mines,
                                        String startDate,
                                        String endDate,
                                        String oreProcessed,
                                        String producedMaterial,
                                        String cutOffGrade,
                                        String production) throws Exception {

        //create the filter
        MiningActivityFilter miningActivityFilter = new MiningActivityFilter(mines, startDate, endDate, oreProcessed, producedMaterial, cutOffGrade, production);

        log.debug("Mining Activity query... \n url:" + serviceURL + "\n" + miningActivityFilter.getFilterString());
        
        //create the method
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "er:MiningActivity", miningActivityFilter.getFilterString());

        //run dat query
        return this.httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
    }
}
