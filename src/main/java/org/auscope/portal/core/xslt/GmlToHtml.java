package org.auscope.portal.core.xslt;

import java.util.Properties;

import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.springframework.stereotype.Component;

/**
 * Utility class for converting Gml to a 'pretty' HTML representation
 *
 * @author Josh Vote
 *
 */
@Component
public class GmlToHtml extends PortalXSLTTransformer {

	/**
	 * The base URL needed to build service calls from the XSLT
	 */
	public GmlToHtml() {
	    super("/org/auscope/portal/core/xslt/WfsToHtml.xsl");
	}

    /**
     * Utility method to transform a WFS response into HTML
     *
     * @param wfs
     *            WFS response to be transformed
     * @param namespaces
     *            EarthResourceML namespace context (v1.1 or v2.0)
     * @param baseUrl
     *            The base URL of the request e.g.  https://portal.org/api
     * 
     * @return html output string
     */
    public String convert(String wfs, ErmlNamespaceContext namespaces, String baseURL) {
    	Properties stylesheetParams = new Properties();

        stylesheetParams.setProperty("portalBaseURL", baseURL);
        stylesheetParams.setProperty("er", namespaces.getNamespaceURI("er"));
        return convert(wfs, stylesheetParams);
    }

}
