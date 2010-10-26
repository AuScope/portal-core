package org.auscope.portal.server.domain.wcs;


import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

/**
 * Represents a <wcs:singleValue> element  from a WCS DescribeCoverage response
 * @author vot002
 *
 */
public class SingleValue implements ValueEnumType {

    private String type;
    private String value;

    public SingleValue(Node node, XPath xPath) throws Exception {
        type = node.getLocalName();
        value = node.getTextContent();
    }

    public String getType() {
        return type;
    }

    /**
     * The contents of the <wcs:singleValue> element
     * @return
     */
    public String getValue() {
        return value;
    }
}
