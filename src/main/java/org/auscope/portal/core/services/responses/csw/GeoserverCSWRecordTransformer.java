package org.auscope.portal.core.services.responses.csw;

import org.auscope.portal.core.services.PortalServiceException;
import org.w3c.dom.Node;

public class GeoserverCSWRecordTransformer extends CSWRecordTransformer {

	public GeoserverCSWRecordTransformer() throws PortalServiceException {
		super();
	}

    public GeoserverCSWRecordTransformer(Node mdMetadataNode) {
        super(mdMetadataNode);
    }
}
