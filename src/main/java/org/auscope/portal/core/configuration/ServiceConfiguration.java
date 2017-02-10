package org.auscope.portal.core.configuration;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ServiceConfiguration {

    List<ServiceConfigurationItem> serviceConfigurationItems;
    
    @Autowired
    public ServiceConfiguration(List<ServiceConfigurationItem> serviceConfigurationItems) {
        this.serviceConfigurationItems = serviceConfigurationItems;
    }

    /**
     * Retrieve the corresponding ServiceConfigurationItem.
     * 
     * @param url
     *            - Get ServiceConfigurationItem that contains url
     * @return ServiceConfigurationItem if found else return null;
     */
    public ServiceConfigurationItem getServiceConfigurationItem(String url) {
        for (ServiceConfigurationItem serviceConfigurationItem : serviceConfigurationItems) {
            if (serviceConfigurationItem.matchUrl(url)) {
                return serviceConfigurationItem;
            }
        }

        return null;
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
        ServiceConfigurationItem service = getServiceConfigurationItem(url);
        if (service != null) {
            isGml32 = service.isGml32();
        }
        response.getWriter().write(String.valueOf(isGml32));
    }
}
