package org.auscope.portal.core.configuration;

import java.util.List;

public class ServiceConfiguration {

    List<ServiceConfigurationItem> serviceConfigurationItems;
    
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
}
