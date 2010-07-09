package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * A utility class which provides methods for querying nvcl service
 * 
 * @author Jarek Sanders
 * @version $Id$
 *
 */
@Service
public class NvclService {

    // -------------------------------------------------------------- Constants
    
    protected final Log log = LogFactory.getLog(getClass());
    
    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller httpServiceCaller;
    private IWFSGetFeatureMethodMaker methodMaker;

    // ----------------------------------------------------------- Constructors
    /*
    public NvclService() {
        this.httpServiceCaller = new HttpServiceCaller();
        this.methodMaker = new WFSGetFeatureMethodMakerPOST();
    } */
    
    
    // ------------------------------------------ Attribute Setters and Getters    
    
    @Autowired
    public void setHttpServiceCaller(HttpServiceCaller httpServiceCaller) {
        this.httpServiceCaller = httpServiceCaller;
    }
    
    @Autowired
    public void setWFSGetFeatureMethodMakerPOST(IWFSGetFeatureMethodMaker iwfsGetFeatureMethodMaker) {
        this.methodMaker = iwfsGetFeatureMethodMaker;
    }

    
    // --------------------------------------------------------- Public Methods    
    
    /**
     * Get all boreholes from a given service url and return the response
     * @param serviceURL
     * @return
     * @throws Exception
     */
    public String getAllBoreholes(String serviceURL, int maxFeatures) throws Exception {
        
        // Create a GetFeature request with an empty filter - get all
        HttpMethodBase method = methodMaker.makeMethod(serviceURL, "gsml:Borehole", "", maxFeatures);
        // Call the service, and get all the boreholes
        return httpServiceCaller.getMethodResponseAsString(method, httpServiceCaller.getHttpClient());
    }
    
}
