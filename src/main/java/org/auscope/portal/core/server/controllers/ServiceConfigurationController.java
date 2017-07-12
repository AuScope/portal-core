package org.auscope.portal.core.server.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/***
 * A controller to access specific service configurations that are not accessible through CSW records i.e. paging, gml 3.2 usage.
 * 
 * @author Rini Angreani
 *
 */
@Controller
public class ServiceConfigurationController {
    
    private ServiceConfiguration serviceConfig;


    @Autowired
    public ServiceConfigurationController(ServiceConfiguration serviceConfig) {
        this.serviceConfig = serviceConfig;
    }  
    
    /**
     * Checks if a service is GML 3.2 based or not. The default is GML 3.1.1 unless configured otherwise. 
     * @param response
     * @param url - ServiceConfigurationItem url
     * @throws IOException
     */
    @RequestMapping("/checkGml32.do")    
    public void isGml32(HttpServletResponse response,
            @RequestParam("serviceUrl") final String url) throws IOException {
        boolean isGml32 = false;
        ServiceConfigurationItem service = serviceConfig.getServiceConfigurationItem(url);
        if (service != null) {
            isGml32 = service.isGml32();
        }
        response.getWriter().write(String.valueOf(isGml32));
    }

}
