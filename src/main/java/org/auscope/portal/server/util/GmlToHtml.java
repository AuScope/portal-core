package org.auscope.portal.server.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for converting Gml to a 'pretty' HTML representation
 * @author Josh Vote
 *
 */
@Component
public class GmlToHtml {
    /** The location of the WFS response -> HTML XSLT */
    public static final String GML_TO_HTML_XSLT = "WfsToHtml.xsl";

    private final Log log = LogFactory.getLog(this.getClass());
    private PortalXSLTTransformer transformer;

    @Autowired
    public GmlToHtml(PortalXSLTTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Utility method to transform xml file. It is kml.xsl specific as the
     * stylesheet needs serviceURL parameter.
     *
     * @param geoXML file to be converted in kml format
     * @param serviceUrl URL of the service providing data
     * @return Xml output string
     */
    public String convert(String geoXML, String serviceUrl) {
        InputStream inXSLT = getClass().getResourceAsStream(GML_TO_HTML_XSLT);
        if (inXSLT == null) {
            log.error("Couldn't find/read source GML->HTML XSLT at " + GML_TO_HTML_XSLT);
            return "";
        }

        Properties properties = new Properties();
        properties.setProperty("serviceURL", serviceUrl);

        return transformer.convert(geoXML, inXSLT, properties);
    }
}
