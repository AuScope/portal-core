package org.auscope.portal.server.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * <p>Extension to org.springframework.beans.factory.config.PropertyPlaceholderConfigurer.
 * In contrast to PropertyPlaceholderConfigurer, this configurer allows to fill
 * in explicit placeholders in context of run-time server. This hopefully will
 * simplify moving the same deployment war package between environments w/o 
 * having to rebuild</p>
 * 
 * @see PropertyPlaceholderConfigurer
 * @version $Id$
 */
public class PortalPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private static Log log 
            = LogFactory.getLog(PortalPropertyPlaceholderConfigurer.class);
   
    /**
     * Resolve the given placeholder
     * 
     * @param placeholder - the placeholder to resolve
     * @return the resolved value, of null if none
     */
    public String resolvePlaceholder(String placeholder) {
        
        Properties mergedProps;      
        try {
            // Merged Properties instance contains both the loaded
            // properties and properties set on this FactoryBean.
            mergedProps = mergeProperties();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        
        return resolvePlaceholder(placeholder, mergedProps);    
    }
   
    /**
     * Resolve the given placeholder using the given properties.
     *  
     * The default implementation checks first for a HOST specific resolution 
     * request. If this is the case it finds out run time server's hostname/IP
     * address and prefixes the property name before searching for a mapping 
     * in configuration file. Otherwise it looks for a property.
     *  
     * @param placeholder - the placeholder to resolve
     * @param props - the merged properties of this configurer
     * @return the resolved value, of null if none
     * @see #resolvePlaceholder(String placeholder)
     */
    public String resolvePlaceholder(String placeholder, Properties props) {
        
        try {
            if (placeholder.startsWith("HOST.")) {
                log.info("Local Host: " + InetAddress.getLocalHost().getHostName()
                                        + " for property " + placeholder);
                
                String hostname = InetAddress.getLocalHost().getHostName();
                
                // Fix for getHostName() returning FQHN
                String search = ".";
                int j = hostname.indexOf(search);
                if (j!=-1)
                    hostname = hostname.substring(0,j);
                
                log.debug("Host Name: " + hostname);
                
                String property = placeholder.replaceFirst("HOST",hostname);
                
                log.info("Translated property: " + property);
                
                String prop = props.getProperty(property);
                
                if (prop == null) 
                    log.warn("Please define property: " + property);
                
                return prop;
            } else { 
                return props.getProperty(placeholder);
            }
        } catch (UnknownHostException e) {
            log.warn(e);
            return null;
        }
   }

}