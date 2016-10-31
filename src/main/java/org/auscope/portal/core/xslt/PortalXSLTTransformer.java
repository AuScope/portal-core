package org.auscope.portal.core.xslt;

import java.io.IOException;
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

/**
 * Class for performing XSLT Transformations
 * 
 * @author Josh Vote
 *
 */
public class PortalXSLTTransformer {
    private final Log log = LogFactory.getLog(this.getClass());

    private String xsltResourceName;

    /**
     * Creates a new instance of this class for transforming using a single XSLT
     * 
     * @param xsltResourceName
     *            The name of the resource (relative to this class)
     */
    public PortalXSLTTransformer(String xsltResourceName) {
        this.xsltResourceName = xsltResourceName;
    }

    /**
     * Utility for creating an instance of the Transformer class
     * 
     * @param xslt
     *            The style sheet contents that will form the basis of the transformer
     * @param stylesheetParams
     *            [Optional] Any additional params to set for the Transformer
     * @return
     * @throws TransformerConfigurationException
     */
    private Transformer createTransformer(InputStream xslt, Properties stylesheetParams)
            throws TransformerConfigurationException {
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

        // Ensure we resolve resources locally
        tFactory.setURIResolver(new ResourceURIResolver(getClass()));

        // Use the TransformerFactory to instantiate updateCSWRecords
        // transformer that will
        // work with the style sheet we specify. This method call also
        // processes
        // the style sheet into updateCSWRecords compiled Templates object.
        Transformer transformer = tFactory.newTransformer(new StreamSource(xslt));

        // Set stylesheet parameters
        if (stylesheetParams != null) {
            for (String param : stylesheetParams.stringPropertyNames()) {
                transformer.setParameter(param, stylesheetParams.getProperty(param));
            }
        }

        return transformer;
    }

    /**
     * Utility method to transform xml file.
     *
     * @param xml
     *            XML String to be transformed
     * @param xslt
     *            An input stream containing an XSLT that will be used to transform xml
     * @param Properties
     *            A map of properties that will be enumerated to set style sheet properties
     * @return Xml output string
     */
    public String convert(String xml, Properties stylesheetParams) {
        return convert(new StreamSource(new StringReader(xml)), stylesheetParams);
    }

    /**
     * Utility method to transform xml file.
     *
     * @param xml
     *            XML Stream to be transformed
     * @param xslt
     *            An input stream containing an XSLT that will be used to transform xml
     * @param Properties
     *            A map of properties that will be enumerated to set style sheet properties
     * @return Xml output string
     */
    public String convert(InputStream xml, Properties stylesheetParams) {
        return convert(new StreamSource(xml), stylesheetParams);
    }

    /**
     * Utility method to transform xml file.
     *
     * @param xml
     *            XML Stream to be transformed
     * @param xslt
     *            An input stream containing an XSLT that will be used to transform xml
     * @param Properties
     *            A map of properties that will be enumerated to set style sheet properties
     * @return Xml output string
     */
    public String convert(StreamSource xml, Properties stylesheetParams) {
        StringWriter sw = new StringWriter();
        try (InputStream xslt = getClass().getResourceAsStream(xsltResourceName)) {
            try {
                Transformer transformer = createTransformer(xslt, stylesheetParams);
                transformer.transform(xml, new StreamResult(sw));
            } catch (TransformerConfigurationException tce) {
                log.error(tce);
            } catch (TransformerException e) {
                log.error("Failed to transform xml: " + e);
            } 
        } catch (IOException e1) {
            log.error("Failed to read xslt resource: " + e1.getMessage(), e1);
        }
        String kml = sw.toString();
        return kml;
    }
}
