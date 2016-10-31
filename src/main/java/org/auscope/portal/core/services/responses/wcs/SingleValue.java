package org.auscope.portal.core.services.responses.wcs;

import org.w3c.dom.Node;

/**
 * Represents a <wcs:singleValue> element from a WCS DescribeCoverage response
 * 
 * @author vot002
 *
 */
public class SingleValue implements ValueEnumType {

    private static final long serialVersionUID = 1L;
    private String type;
    private String value;

    public SingleValue(Node node) {
        type = node.getLocalName();
        value = node.getTextContent();
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * The contents of the <wcs:singleValue> element
     * 
     * @return
     */
    public String getValue() {
        return value;
    }
}
