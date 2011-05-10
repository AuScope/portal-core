package org.auscope.portal.pressuredb;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.domain.ows.OWSExceptionParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static utility class for parsing possible exception responses from the pressure db dataservices
 * @author JoshVote
 *
 */
public class PressureDBExceptionParser {
    
    private static final Log log = LogFactory.getLog(OWSExceptionParser.class);
    
    /**
     * Will attempt to parse an <DataServiceError> element
     *
     * Will throw an PressureDBException if document does contain an <DataServiceError>, otherwise it will do nothing
     * @param doc
     * @throws PressureDBException
     */
    public static void checkForExceptionResponse(Document doc) throws PressureDBException {
        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            //Check for an exception response
            NodeList exceptionNodes = (NodeList)xPath.evaluate("/DataServiceError", doc, XPathConstants.NODESET);
            if (exceptionNodes.getLength() > 0) {
                Node exceptionNode = exceptionNodes.item(0);

                throw new PressureDBException(exceptionNode.getTextContent());
            }
        } catch (XPathExpressionException ex) {
            //This should *hopefully* never occur
            log.error("Error whilst attempting to check for errors", ex);
        }
    }
}
