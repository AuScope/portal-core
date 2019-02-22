package org.auscope.portal.core.xslt;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

/**
 * A PortalXSLTTransformer for working with the wfsToKml stylesheet
 * 
 * @author Josh Vote
 */
public class WfsToKmlTransformer extends PortalXSLTTransformer {

    /**
     * Creates a new transformer using /org/auscope/portal/core/xslt/wfsToKml.xsl
     */
    public WfsToKmlTransformer() {
        super("/org/auscope/portal/core/xslt/wfsToKml.xsl");
    }

    /**
     * Creates a new transformer using the specified resource (should accept a serviceUrl parameter)
     * 
     * @param resource
     */
    public WfsToKmlTransformer(String resource) {
        super(resource);
    }

    /**
     * Utility method to transform a WFS response into kml
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
     * Utility method to transform a WFS response into kml
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
     * Utility method to transform a WFS response into kml
     *
     * @param wfs
     *            WFS response to be transformed
     * @param serviceUrl
     *            The WFS URL where the response came from
     * @return Xml output string
     */
    public String convert(StreamSource wfs, String serviceUrl) {
        Properties stylesheetParams = new Properties();
        stylesheetParams.setProperty("serviceUrl", serviceUrl);
        return convert(wfs, stylesheetParams);
    }
}
