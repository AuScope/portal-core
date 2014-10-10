package org.auscope.portal.core.services.responses.ows;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * A utility class that provides convenience methods for detecting an ows:Exception response in an
 * arbitrary ows response.
 * @author vot002
 *
 */
public class OWSExceptionParser {

    /** The Constant log. */
    private static final Log log = LogFactory.getLog(OWSExceptionParser.class);

    /**
     * Returns an XPath object that is configured to read the ows:Namespace.
     *
     * @return the XPath object
     * @throws OWSException
     */
    private static XPath createNamespaceAwareXPath() throws OWSException {
        XPath xPath = null;
        try {
            xPath = XPathFactory.newInstance(
                    XPathFactory.DEFAULT_OBJECT_MODEL_URI,
                    "net.sf.saxon.xpath.XPathFactoryImpl",
                    OWSExceptionParser.class.getClassLoader()).newXPath();

            // use our own bodgy namespace context that just recognizes
            // xmlns:ows
            xPath.setNamespaceContext(new NamespaceContext() {

                @SuppressWarnings("rawtypes")
                public Iterator getPrefixes(String namespaceURI) {
                    return null; // not used
                }

                public String getPrefix(String namespaceURI) {
                    return null; // not used
                }

                public String getNamespaceURI(String prefix) {
                    if (prefix.equals("ows")) {
                        return "http://www.opengis.net/ows";
                    } else {
                        return null;
                    }
                }
            });
            return xPath;
        } catch (XPathFactoryConfigurationException e) {
            throw new OWSException("Can't load XPath parser: "+e.getMessage(), e);
        }
    }

    /**
     * Will attempt to parse an <ows:Exception> element where ows will be http://www.opengis.net/ows.
     *
     * Will throw an OWSException if document does contain an <ows:ExceptionReport>, otherwise it will do nothing
     *
     * @param doc a string containing valid XML, this will be parsed into a W3C DOM document
     * @throws OWSException the oWS exception
     */
    public static void checkForExceptionResponse(String xmlString) throws OWSException {
        Document doc = null;
        try {
            doc = DOMUtil.buildDomFromString(xmlString);
        } catch (Exception ex) {
            //This should *hopefully* never occur
            log.error("Error whilst attempting to parse xmlString for errors", ex);
            throw new OWSException("Unable to parse xmlString", ex);
        }

        checkForExceptionResponse(doc);
    }

    /**
     * Will attempt to parse an <ows:Exception> element where ows will be http://www.opengis.net/ows.
     *
     * Will throw an OWSException if document does contain an <ows:ExceptionReport>, otherwise it will do nothing
     *
     * @param doc the doc
     * @throws OWSException the oWS exception
     */
    public static void checkForExceptionResponse(Document doc) throws OWSException {
        XPath xPath = createNamespaceAwareXPath();

        try {
            //Check for an exception response
            NodeList exceptionNodes = (NodeList) xPath.evaluate("/ows:ExceptionReport/ows:Exception", doc, XPathConstants.NODESET);
            if (exceptionNodes.getLength() > 0) {
                Node exceptionNode = exceptionNodes.item(0);

                Node exceptionTextNode = (Node) xPath.evaluate("ows:ExceptionText", exceptionNode, XPathConstants.NODE);
                String exceptionText = (exceptionTextNode == null) ? "[Cannot extract error message]" : exceptionTextNode.getTextContent();
                String exceptionCode = (String) xPath.evaluate("@exceptionCode", exceptionNode, XPathConstants.STRING);

                throw new OWSException(String.format("Code='%1$s' Message='%2$s'", exceptionCode, exceptionText));
            }
        } catch (XPathExpressionException ex) {
            //This should *hopefully* never occur
            log.error("Error whilst attempting to check for errors", ex);
        }
    }
}
