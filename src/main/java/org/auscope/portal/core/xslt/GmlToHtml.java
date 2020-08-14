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

	private String portalBackendUrl;

	public GmlToHtml(String url) {
		super("/org/auscope/portal/core/xslt/WfsToHtml.xsl");
		this.portalBackendUrl = url;
	}

	public GmlToHtml() {
	    super("/org/auscope/portal/core/xslt/WfsToHtml.xsl");
	}

    /**
     * Utility method to transform a WFS response into HTML
     *
     * @param wfs
     *            WFS response to be transformed
     * @return html output string
     */
    public String convert(String wfs, ErmlNamespaceContext namespaces) {
    	Properties stylesheetParams = new Properties();
        if (this.portalBackendUrl != null) {
            stylesheetParams.setProperty("portalBaseURL", this.portalBackendUrl);
        }
        stylesheetParams.setProperty("er", namespaces.getNamespaceURI("er"));
        return convert(wfs, stylesheetParams);
    }

}
