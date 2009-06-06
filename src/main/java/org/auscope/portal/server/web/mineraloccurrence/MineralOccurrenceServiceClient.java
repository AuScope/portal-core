package org.auscope.portal.server.web.mineraloccurrence;

import org.auscope.portal.server.web.HttpServiceCaller;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A utility class which provides methods for querying a mineral occurence service
 *
 * Created by IntelliJ IDEA.
 * User: Mathew Wyatt
 * Date: Jun 4, 2009
 * Time: 11:30:47 AM
 */
public class MineralOccurrenceServiceClient {
    private HttpServiceCaller httpServiceCaller;
    private MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler;

    /**
     * Initialise
     */
    public MineralOccurrenceServiceClient() {
        this.httpServiceCaller = new HttpServiceCaller(new HttpClient());
        this.mineralOccurrencesResponseHandler = new MineralOccurrencesResponseHandler();
    }

    public MineralOccurrenceServiceClient(HttpServiceCaller httpServiceCaller, MineralOccurrencesResponseHandler mineralOccurrencesResponseHandler) {
        this.httpServiceCaller = httpServiceCaller;
        this.mineralOccurrencesResponseHandler = mineralOccurrencesResponseHandler;
    }

    /**
     * Get all the mines from a given service url and return them as Mine objects
     *
     * @param serviceURL - the service to get all of the mines from
     * @return
     * @throws Exception
     */
    public Collection<Mine> getAllMines(String serviceURL) throws Exception {
        //get the mines
        String mineResponse = this.getAllMinesGML(serviceURL);

        //convert the response into a nice collection of Mine Nodes
        Collection<Mine> mines = this.mineralOccurrencesResponseHandler.getMines(mineResponse);

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
        GetMethod method = httpServiceCaller.constructWFSGetFeatureMethod(serviceURL, "mo:Mine", "");

        //call the service, and get all the mines
        return httpServiceCaller.callGetMethod(method);
    }

    /**
     * Given a specifies service, and mineName, we want to get that mine from the service
     * @param serviceURL - the service to get the mine from
     * @param mineName - the name of the mine to get
     * @return
     */
    public Collection<Mine> getMineWithSpecifiedName(String serviceURL, String mineName) throws Exception {
        //get the mine
        String mineResponse = this.getMineWithSpecifiedNameGML(serviceURL, mineName);

        //convert the response into a nice collection of Mine Nodes
        Collection<Mine> mines = this.mineralOccurrencesResponseHandler.getMines(mineResponse);

        //send it back!
        return mines;
    }

    /**
     * Given a specifies service, and mineName, we want to get that mine from the service
     *
     * @param serviceURL
     * @param mineName
     * @return
     * @throws Exception
     */
    public String getMineWithSpecifiedNameGML(String serviceURL, String mineName) throws Exception {
        //create a filter for the specified name
        MineFilter mineFilter = new MineFilter(mineName);

        //create a GetFeature request with an empty filter - get all
        GetMethod method = httpServiceCaller.constructWFSGetFeatureMethod(serviceURL, "mo:Mine", mineFilter.getFilterString());

        //call the service, and get all the mines
        return httpServiceCaller.callGetMethod(method);
    }

    /**
     * Returns commodities based on a given service, the commodityGroup, and  acommodityName
     *
     * If both commodityGroup and commodityName are empty strings a GetALL query will be run
     *
     * @param commodityGroup
     * @param commodityName
     * @return
     */
    public Collection<Commodity> getCommodity(String serviceURL, String commodityGroup, String commodityName) throws Exception {
        //httpclient method
        GetMethod method = null;

        //if we don't have a name or a group, then just get all of them
        if(commodityGroup.equals("") && commodityName.equals("")) {
            method = httpServiceCaller.constructWFSGetFeatureMethod(serviceURL, "mo:Commodity", "");
        } else {
            //create the filter to append to the url
            CommodityFilter commodityFilter = new CommodityFilter(commodityGroup, commodityName);

            //create a GetFeature request with an empty filter - get all
            method = httpServiceCaller.constructWFSGetFeatureMethod(serviceURL, "mo:Commodity", commodityFilter.getFilterString());
        }

        //call the service, and get all the commodities
        String commodityResponse = httpServiceCaller.callGetMethod(method);

        //parse the commodites and return them
        return this.mineralOccurrencesResponseHandler.getCommodities(commodityResponse);
    }

    /**
     * Given a list of parameters, call a service and get the Mineral Occurrence GML
     * @param serviceURL
     * @param commodityName
     * @param commodityGroup
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
            String commodityName,
            String commodityGroup,
            String measureType,
            String minOreAmount,
            String minOreAmountUOM,
            String minCommodityAmount,
            String minCommodityAmountUOM,
            String cutOffGrade,
            String cutOffGradeUOM) throws Exception {

        //get the commodities, we need their URI's to do a min occ query
        Collection<Commodity> commodities = this.getCommodity(serviceURL, commodityGroup, commodityName);

        //if there ar no commodities we cant continue
        if(commodities.size() == 0)
                return "";

        //create the mineral occurrence filter
        MineralOccurrenceFilter mineralOccurrenceFilter = new MineralOccurrenceFilter(  commodities,
                                                                                        measureType,
                                                                                        minOreAmount,
                                                                                        minOreAmountUOM,
                                                                                        minCommodityAmount,
                                                                                        minCommodityAmountUOM,
                                                                                        cutOffGrade,
                                                                                        cutOffGradeUOM);

        //create the method
        GetMethod method = httpServiceCaller.constructWFSGetFeatureMethod(serviceURL, "mo:MineralOccurrence", mineralOccurrenceFilter.getFilterString());

        //run the dam query
        return httpServiceCaller.callGetMethod(method);
    }
}
