package org.auscope.portal.server.web.service;

import java.net.URL;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.auscope.portal.server.domain.ows.GetCapabilitiesRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Concrete implementation of the GetCapabilitiesService interface.
 *
 * @author Jarek Sanders
 * @version $Id$
 */
@Service
public class GetCapabilitiesServiceImpl implements GetCapabilitiesService{

    // -------------------------------------------------------------- Constants
    protected final Log log = LogFactory.getLog(getClass());


    // ----------------------------------------------------- Instance variables
    private HttpServiceCaller serviceCaller;


    // ----------------------------------------------------------- Constructors
    @Autowired
    public GetCapabilitiesServiceImpl( HttpServiceCaller serviceCaller)
    throws Exception {
        this.serviceCaller = serviceCaller;
    }


    // ------------------------------------------- Property Setters and Getters
    /**
     * Request GetCapabilities document from the given service
     *
     * @param serviceUrl Url of WMS service
     * @return GetCapabilitiesRecord
     */
    public GetCapabilitiesRecord getWmsCapabilities(final String serviceUrl)
    throws Exception {

        String response = "";

        //Check that its a real URL
    	//Quick test that it looks like a URL....

   		URL newUrl = new URL(serviceUrl);


        // Do the request
        response = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
            public HttpMethodBase makeMethod() {

                GetMethod method;

                // set parameters
                NameValuePair version = new NameValuePair("version", "1.1.1");
                NameValuePair service = new NameValuePair("service", "WMS");
                NameValuePair request = new NameValuePair("request", "GetCapabilities");


                // The entered url may already contain parameters. We need to
                // remove them first, create new method based on url and then
                // add them back.
                // TODO: This is hack ... we need proper handling as it only
                // handles one parameter

                int i = serviceUrl.indexOf("?");

                if (i > -1) {
                    String url = serviceUrl.substring(0, serviceUrl.indexOf("?"));
                    method = new GetMethod(url);

                    String searchUrl = serviceUrl.substring(serviceUrl.indexOf("?") + 1);
                    String temp[] = searchUrl.split("=");

                    // An existing parameter
                    NameValuePair unknown = new NameValuePair(temp[0], temp[1]);

                    // Attach parameters to the method
                    method.setQueryString(new NameValuePair[]{unknown,version,service,request});
                } else {
                    method = new GetMethod(serviceUrl);

                    // Attach parameters to the method
                    method.setQueryString(new NameValuePair[]{version,service,request});
                }
                return method;
            }
        }.makeMethod(), serviceCaller.getHttpClient());

        return new GetCapabilitiesRecord(response);
    }

}
