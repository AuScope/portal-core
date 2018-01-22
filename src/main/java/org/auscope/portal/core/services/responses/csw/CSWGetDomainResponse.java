package org.auscope.portal.core.services.responses.csw;

import org.auscope.portal.core.services.namespaces.IterableNamespace;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class CSWGetDomainResponse {

    private Set<String> domainValues;
    private String propertyName;

    NamespaceContext nc;
    public static final String DOMAIN_VALUES_EXPRESSION = "/csw:GetDomainResponse/csw:DomainValues/csw:ListOfValues/csw:Value";
    public static final String PROPERTY_NAME_EXPRESSION = "/csw:GetDomainResponse/csw:DomainValues/csw:PropertyName";

    public CSWGetDomainResponse(InputStream getDomainXML) throws IOException {

        nc = new CSWGetCapabilitiesNamespace();
        Document document;
        try {
            document = DOMUtil.buildDomFromStream(getDomainXML, true);
            this.setDomainValues(document);
            this.setPropertyName(document);

        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);

        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(Document document) throws XPathExpressionException {
        Node node = (Node) DOMUtil.compileXPathExpr(PROPERTY_NAME_EXPRESSION, nc).evaluate(document, XPathConstants.NODE);
        this.propertyName = node.getTextContent();
    }

    public Set<String> getDomainValues() {
        return domainValues;
    }

    public void setDomainValues(Document document) throws XPathExpressionException {


        Set<String> values = new HashSet<String>();
        NodeList nodes = (NodeList) DOMUtil.compileXPathExpr(DOMAIN_VALUES_EXPRESSION, nc).evaluate(document, XPathConstants.NODESET);
        for (int i=0; i < nodes.getLength(); i++ ) {

           Node node = nodes.item(i);

           values.add(node.getTextContent());
        }
        this.domainValues = values;
    }

    private class CSWGetCapabilitiesNamespace extends IterableNamespace {

        public CSWGetCapabilitiesNamespace() {
            map.put("csw", "http://www.opengis.net/cat/csw/2.0.2");

        }
    }
}
