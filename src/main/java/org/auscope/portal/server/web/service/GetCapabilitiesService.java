package org.auscope.portal.server.web.service;

import java.io.BufferedInputStream;
import java.io.IOException;
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
    private final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller serviceCaller;


    // ----------------------------------------------------------- Constructors
    @Autowired
    public GetCapabilitiesService(HttpServiceCaller serviceCaller) throws Exception {
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

        BufferedInputStream response = null;
        try{

            //Check that its a real URL
            //Quick test that it looks like a URL....

            URL newUrl = new URL(serviceUrl);

            // Do the request
            WMSMethodMaker methodMaker = new WMSMethodMaker(serviceUrl);
            HttpMethodBase method = methodMaker.getCapabilitiesMethod();
            response = new BufferedInputStream(serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient()));

            return new GetCapabilitiesRecord(response);
        }finally{
            try{
                if (response != null) {
                    response.close();
                }
            }catch(IOException e){
                //Not a show stopper if stream can't be closed since
                //most likely it is because it is already closed.
                log.warn(e);
            }
        }
    }

}
