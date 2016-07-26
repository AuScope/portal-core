package org.auscope.portal.core.server;

import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * A class that overrides the way the spring framework looks up bean configuration locations and replaces it with a context parameter specified 'profile'
 * 
 * @author Josh Vote
 *
 */
public class PortalProfileXmlWebApplicationContext extends
        XmlWebApplicationContext {

    @Override
    protected String[] getDefaultConfigLocations() {

        String profile = this.getServletContext().getInitParameter("AUSCOPE_PORTAL_PROFILE");
        if (profile == null) {
            profile = "portal-test"; //our default profile
        }

        //Return applicationContext.xml AND our profile
        return new String[] {DEFAULT_CONFIG_LOCATION,
                DEFAULT_CONFIG_LOCATION_PREFIX + "profile-" + profile + DEFAULT_CONFIG_LOCATION_SUFFIX};
    }
}
