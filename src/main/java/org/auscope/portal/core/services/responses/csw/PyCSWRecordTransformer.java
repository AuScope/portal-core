package org.auscope.portal.core.services.responses.csw;

import org.auscope.portal.core.services.PortalServiceException;
import org.w3c.dom.Node;

public class PyCSWRecordTransformer extends CSWRecordTransformer{

	public PyCSWRecordTransformer() throws PortalServiceException {
		super();
	}

    public PyCSWRecordTransformer(Node mdMetadataNode) {
        super(mdMetadataNode);
    }
}
