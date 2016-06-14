package org.auscope.portal.core.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * 
 * @author fri096
 *
 */
public class PortalPropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    private static Log log = LogFactory.getLog(PortalPropertySourcesPlaceholderConfigurer.class);

    private String hostname = null;
    
    public PortalPropertySourcesPlaceholderConfigurer() {
        try {
            hostname = InetAddress.getLocalHost().getHostName().toUpperCase();
            log.debug("Local Host: " + hostname);
        } catch (UnknownHostException e) {
            log.error("Could not determine hostname: "+e.getMessage(), e);
        }
    }
    
    @Override
    protected void loadProperties(Properties props) throws IOException {
        super.loadProperties(props);
        Properties hostProps = new Properties();
        
        for (Object property : props.keySet()) {
            if(property instanceof String) {
                String prop = (String) property;
                if(prop.trim().toUpperCase().startsWith(hostname)) {
                    String newProp = "HOST"+prop.substring(hostname.length());
                    hostProps.put(newProp, props.get(property));                    
                }
                else if(prop.startsWith("DEFAULT.")){
                    String newProp = "HOST"+prop.substring("DEFAULT".length());
                    if(! hostProps.containsKey(newProp)) {
                        hostProps.put(newProp, props.get(property));
                    }
                }
            }
        }
        props.putAll(hostProps);
    }
}
