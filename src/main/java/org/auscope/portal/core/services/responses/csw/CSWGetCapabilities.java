package org.auscope.portal.core.services.responses.csw;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.namespaces.IterableNamespace;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Entity class to hold response from a CSW getCapabilities record. At the moment only title is need, no point wasting resources into other fields that is not
 * needed. Add to this as required.
 * 
 * @author tey006
 *
 */
public class CSWGetCapabilities {

    private final Log log = LogFactory.getLog(getClass());

    NamespaceContext nc;
    public static final String TITLE_EXPRESSION = "/csw:Capabilities/ows:ServiceIdentification/ows:Title";

    private String title;

    public CSWGetCapabilities(InputStream getCapXML) throws ParserConfigurationException, IOException, SAXException,
            XPathExpressionException {
        nc = new CSWGetCapabilitiesNamespace();
        try {
            Document doc = DOMUtil.buildDomFromStream(getCapXML, true);
            this.setTitle(doc);

        } catch (SAXException e) {
            //VT: no exception should be swallowed. pass it back up to the caller.
            log.error("Parsing error: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("IO error: " + e.getMessage());
            throw e;
        } catch (ParserConfigurationException e) {
            log.error("Parser Config Error: " + e.getMessage());
            throw e;
        } catch (XPathExpressionException xpee) {
            log.error("CSW GETCAPABILTIES xml pasing error" + xpee.getMessage());
            throw xpee;
        }
    }

    public void setTitle(Document doc) throws XPathExpressionException {
        String title = "";

        Node tempNode = (Node) DOMUtil.compileXPathExpr(TITLE_EXPRESSION, nc).evaluate(doc, XPathConstants.NODE);

        title = tempNode != null ? tempNode.getTextContent() : "";

        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    private class CSWGetCapabilitiesNamespace extends IterableNamespace {

        public CSWGetCapabilitiesNamespace() {
            map.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
            map.put("ows", "http://www.opengis.net/ows");
        };
    }

}
