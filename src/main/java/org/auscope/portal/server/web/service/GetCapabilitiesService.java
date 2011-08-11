package org.auscope.portal.server.web.service;

import java.net.URL;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;
import org.auscope.portal.server.web.WMSMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation of the GetCapabilitiesService interface.
 *
 * @author Jarek Sanders
 * @version $Id$
 */
@Service
public class GetCapabilitiesService {

    // -------------------------------------------------------------- Constants
    protected final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller serviceCaller;


    // ----------------------------------------------------------- Constructors
    @Autowired
    public GetCapabilitiesService( HttpServiceCaller serviceCaller) throws Exception {
        this.serviceCaller = serviceCaller;
    }


    // ------------------------------------------- Property Setters and Getters
    /**
     * Request GetCapabilities document from the given service
     *
     * @param serviceUrl Url of WMS service
     * @return GetCapabilitiesRecord
     */
    public GetCapabilitiesRecord getWmsCapabilities(final String serviceUrl) throws Exception {

        String response = "";

        //Check that its a real URL
        //Quick test that it looks like a URL....

        URL newUrl = new URL(serviceUrl);

        // Do the request
        WMSMethodMaker methodMaker = new WMSMethodMaker(serviceUrl);
        HttpMethodBase method = methodMaker.getCapabilitiesMethod();
        response = serviceCaller.getMethodResponseAsString(method, serviceCaller.getHttpClient());

        return new GetCapabilitiesRecord(response);
    }

}
