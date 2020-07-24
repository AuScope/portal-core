package org.auscope.portal.core.xslt;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

import org.auscope.portal.core.xslt.PortalXSLTTransformer;
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
     * @param serviceUrl
     *            The WFS URL where the response came from
     * @return Kml output string
     */
    public String convert(String wfs, String serviceUrl) {
        return convert(new StreamSource(new StringReader(wfs)), serviceUrl);
    }

    /**
     * Utility method to transform a WFS response into HTML
     *
     * @param wfs
     *            WFS response to be transformed
     * @param serviceUrl
     *            The WFS URL where the response came from
     * @return Xml output string
     */
    public String convert(InputStream wfs, String serviceUrl) {
        return convert(new StreamSource(wfs), serviceUrl);
    }

    /**
     * Utility method to transform a WFS response into HTML
     *
     * @param wfs
     *            WFS response to be transformed
     * @param serviceUrl
     *            The WFS URL where the response came from
     * @return Xml output string
     */
    public String convert(StreamSource wfs, String serviceUrl) {
        Properties stylesheetParams = new Properties();
        if (serviceUrl != null) {
        	// this is only used for Yilgarn...
            stylesheetParams.setProperty("serviceUrl", serviceUrl);
        }
        if (this.portalBackendUrl != null) {
            stylesheetParams.setProperty("portalBaseURL", this.portalBackendUrl);
        }
        return convert(wfs, stylesheetParams);
    }
}
