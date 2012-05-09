package org.auscope.portal.server.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * <p> Utility class that converts GeoSciML into KML format.</p>
 *
 * @author jsanders
 * @author Josh Vote
 */
@Component
public class GmlToKml {
    /** The location of the WFS response -> KML XSLT */
    public static final String GML_TO_KML_XSLT = "kml.xsl";

    private final Log log = LogFactory.getLog(this.getClass());
    private PortalXSLTTransformer transformer;

    @Autowired
    public GmlToKml(PortalXSLTTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Utility method to transform a gml file into a 'pretty' HTML equivalent
     *
     * @param geoXML file to be converted in html format
     * @param serviceUrl URL of the service providing data
     * @return Xml output string
     * @throws
     */
    public String convert(String geoXML, String serviceUrl) {
        log.debug("GML input: \n" + geoXML);

        InputStream inXSLT = getClass().getResourceAsStream(GML_TO_KML_XSLT);
        if (inXSLT == null) {
            log.error("Couldn't find/read source GML->KML XSLT at " + GML_TO_KML_XSLT);
            return "";
        }

        Properties properties = new Properties();
        properties.setProperty("serviceURL", serviceUrl);

        return transformer.convert(geoXML, inXSLT, properties);
    }
}
