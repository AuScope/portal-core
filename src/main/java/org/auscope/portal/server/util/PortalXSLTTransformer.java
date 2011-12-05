package org.auscope.portal.server.util;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * Class for performing XSLT Transformations
 * @author Josh Vote
 *
 */
@Component
public class PortalXSLTTransformer {
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Utility method to transform xml file. It is kml.xsl specific as the
     * stylesheet needs serviceURL parameter.
     *
     * @param xml XML String to be transformed
     * @param xslt An input stream containing an XSLT that will be used to transform xml
     * @param Properties A map of properties that will be enumerated to set style sheet properties
     * @return Xml output string
     */
    public String convert(String xml, InputStream xslt, Properties stylesheetParams) {
        log.debug("XML input: \n" + xml);

        StringWriter sw = new StringWriter();
        try {
            // Use the static TransformerFactory.newInstance() method:
            // TransformerFactory tFactory = TransformerFactory.newInstance();
            // to instantiate updateCSWRecords TransformerFactory.
            // The javax.xml.transform.TransformerFactory system property
            // setting
            // determines the actual class to instantiate:
            // org.apache.xalan.transformer.TransformerImpl.
            // However, we prefer Saxon...
            TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
            log.debug("XSLT implementation in use: " + tFactory.getClass());

            // Use the TransformerFactory to instantiate updateCSWRecords
            // transformer that will
            // work with the style sheet we specify. This method call also
            // processes
            // the style sheet into updateCSWRecords compiled Templates object.
            Transformer transformer = tFactory.newTransformer(new StreamSource(xslt));

            // Set stylesheet parameters
            for (String param : stylesheetParams.stringPropertyNames()) {
                transformer.setParameter(param, stylesheetParams.getProperty(param));
            }

            // Write the output to updateCSWRecords stream
            transformer.transform(new StreamSource(new StringReader(xml)),
                    new StreamResult(sw));

        } catch (TransformerConfigurationException tce) {
            log.error(tce);
        } catch (TransformerException e) {
            log.error("Failed to transform xml: " + e);
        }
        log.debug("XML output: \n" + sw.toString());
        return sw.toString();
    }
}
