package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.mineraloccurrence.BoreholeFilter;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.filter.IFilter;
import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * A utility class which provides methods for querying borehole service
 * 
 * @author Jarek Sanders
 * @version $Id$
 *
 */
@Service
public class BoreholeService {

    // -------------------------------------------------------------- Constants
    
    protected final Log log = LogFactory.getLog(getClass());
    
    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller httpServiceCaller;
    private IWFSGetFeatureMethodMaker methodMaker;
    private IFilter filter;

    // ----------------------------------------------------------- Constructors
    
    // ------------------------------------------ Attribute Setters and Getters    
    
    @Autowired
    public void setHttpServiceCaller(HttpServiceCaller httpServiceCaller) {
        this.httpServiceCaller = httpServiceCaller;
    }
    
    @Autowired
    public void setWFSGetFeatureMethodMakerPOST(IWFSGetFeatureMethodMaker iwfsGetFeatureMethodMaker) {
        this.methodMaker = iwfsGetFeatureMethodMaker;
    }
    
    @Autowired
    public void setFilter(IFilter filter) {
        this.filter = filter;
    }
    
    

    
    // --------------------------------------------------------- Public Methods    
    
    
    
    /**
     * Get all boreholes from a given service url and return the response
     * @param serviceURL
     * @param bbox Set to the bounding box in which to fetch results, otherwise set it to null
     * @return
     * @throws Exception
     */
    public HttpMethodBase getAllBoreholes(String serviceURL, String boreholeName, String custodian, String dateOfDrilling, int maxFeatures, FilterBoundingBox bbox) throws Exception {
        String filterString;
        BoreholeFilter nvclFilter = new BoreholeFilter(boreholeName, custodian, dateOfDrilling);
        if (bbox == null) {
            filterString = nvclFilter.getFilterStringAllRecords();
        } else {
            filterString = nvclFilter.getFilterStringBoundingBox(bbox);
        }
        
        // Create a GetFeature request with an empty filter - get all
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "gsml:Borehole", filterString, maxFeatures);
        // Call the service, and get all the boreholes
        //return httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
        return method;
    }
    
}
