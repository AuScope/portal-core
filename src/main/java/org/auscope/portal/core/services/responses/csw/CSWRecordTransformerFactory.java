package org.auscope.portal.core.services.responses.csw;

import javax.xml.parsers.ParserConfigurationException;

import org.auscope.portal.core.services.PortalServiceException;
import org.w3c.dom.Node;

/**
 * Factory class for instantiating instances of CSWRecordTransformer
 * 
 * @author Josh Vote
 *
 */
public class CSWRecordTransformerFactory {
    /**
     * Creates a new instance of CSWRecordTransformer which will draw from the specified gmd:MD_Metadata Node representation as a template
     * 
     * @param mdMetadataNode
     * @return
     */
    public CSWRecordTransformer newCSWRecordTransformer(Node mdMetadataNode) {
        return new CSWRecordTransformer(mdMetadataNode);
    }

    /**
     * Creates a new instance of CSWRecordTransformer and generates an empty document that will be used for constructing DOM.
     * 
     * @return
     * @throws ParserConfigurationException 
     * @throws PortalServiceException 
     */
    public CSWRecordTransformer newCSWRecordTransformer() throws PortalServiceException {
        return new CSWRecordTransformer();
    }
}
