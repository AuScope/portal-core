package org.auscope.portal.core.services;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WMSMethodMaker;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;

/**
 * Service class providing functionality for interacting with a Web Map Service
 */
public class WMSService {

    // -------------------------------------------------------------- Constants
    private final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller serviceCaller;


    // ----------------------------------------------------------- Constructors
    public WMSService(HttpServiceCaller serviceCaller) throws Exception {
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
        try {
            // Do the request
            WMSMethodMaker methodMaker = new WMSMethodMaker(serviceUrl);
            HttpMethodBase method = methodMaker.getCapabilitiesMethod();
            response = new BufferedInputStream(serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient()));

            return new GetCapabilitiesRecord(response);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch(IOException e) {
                //Not a show stopper if stream can't be closed since
                //most likely it is because it is already closed.
                log.warn(e);
            }
        }
    }

}
