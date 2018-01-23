package org.auscope.portal.core.services.responses.csw;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.xsltc.DOM;
import org.auscope.portal.core.services.namespaces.IterableNamespace;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Entity class to hold response from a CSW getCapabilities record. At the moment only title is need, no point wasting resources into other fields that is not
 * needed. Add to this as required.
 * 
 * @author tey006
 *
 */
public class CSWGetCapabilities {

    @SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(getClass());

    NamespaceContext nc;
    public static final String TITLE_EXPRESSION = "/csw:Capabilities/ows:ServiceIdentification/ows:Title";
    public static final String OPERATIONS_EXPRESSION = "/csw:Capabilities/ows:OperationsMetadata/ows:Operation";

    private String title;

    private List<String> operations;

    public CSWGetCapabilities(InputStream getCapXML) throws IOException {
        nc = new CSWGetCapabilitiesNamespace();
        Document doc;
        try {
            doc = DOMUtil.buildDomFromStream(getCapXML, true);
            this.setTitle(doc);
            this.setOperations(doc);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void setTitle(Document doc) throws XPathExpressionException {
        String t = "";

        Node tempNode = (Node) DOMUtil.compileXPathExpr(TITLE_EXPRESSION, nc).evaluate(doc, XPathConstants.NODE);

        t = tempNode != null ? tempNode.getTextContent() : "";

        this.title = t;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setOperations(Document document) throws XPathExpressionException {
        List<String> ops = new ArrayList<String>();
        NodeList nodes = (NodeList) DOMUtil.compileXPathExpr(OPERATIONS_EXPRESSION, nc).evaluate(document, XPathConstants.NODESET);
        for (int i=0; i < nodes.getLength(); i++ ) {

            NamedNodeMap attributes = nodes.item(i).getAttributes();
            if (attributes != null) {
                ops.add(attributes.getNamedItem("name").getNodeValue());
            }
        }
        this.operations = ops;
    }

    public List<String> getOperations() {
        return operations;
    }

    private class CSWGetCapabilitiesNamespace extends IterableNamespace {

        public CSWGetCapabilitiesNamespace() {
            map.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
            map.put("ows", "http://www.opengis.net/ows");
        }
    }

}
