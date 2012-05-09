package org.auscope.portal.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;


/**
 * <p> Utility class that converts GeoSciML into KML format.</p>
 *
 * @author jsanders
 * @author Josh Vote
 */
@Component
public class GmlToKml {
    /** The location of the WFS response -> KML XSLT */
    public static final String GML_TO_KML_XSLT = "/WEB-INF/xsl/kml.xsl";

    private final Log log = LogFactory.getLog(this.getClass());
    private PortalXSLTTransformer transformer;
    private ServletContext servletContext;

    @Autowired
    public GmlToKml(PortalXSLTTransformer transformer, ServletContext servletContext) {
        this.transformer = transformer;
        this.servletContext = servletContext;
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

        InputStream inXSLT;
        try {
            inXSLT = new ServletContextResource(servletContext, GML_TO_KML_XSLT).getInputStream();
        } catch (IOException ex) {
            log.error("Couldn't find/read source GML->KML XSLT at " + GML_TO_KML_XSLT, ex);
            return "";
        }

        Properties properties = new Properties();
        properties.setProperty("serviceURL", serviceUrl);

        return transformer.convert(geoXML, inXSLT, properties);
    }
}
