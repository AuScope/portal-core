package org.auscope.portal.core.xslt;

import java.util.Properties;

import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
	private String portalBackendUrl;

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
     * @return html output string
     */
    public String convert(String wfs, ErmlNamespaceContext namespaces) {
    	Properties stylesheetParams = new Properties();

    	if (this.portalBackendUrl == null) {
            this.portalBackendUrl = ServletUriComponentsBuilder
        		.fromCurrentContextPath().build().toUriString();
    	}

        stylesheetParams.setProperty("portalBaseURL", portalBackendUrl);
        stylesheetParams.setProperty("er", namespaces.getNamespaceURI("er"));
        return convert(wfs, stylesheetParams);
    }

}
