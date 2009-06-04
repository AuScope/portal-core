package org.auscope.portal.server.web.mineraloccurrence;

import org.auscope.portal.server.web.HttpServiceCaller;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.Collection;

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
}
