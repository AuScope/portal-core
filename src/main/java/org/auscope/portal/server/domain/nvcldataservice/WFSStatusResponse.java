package org.auscope.portal.server.domain.nvcldataservice;

import java.io.InputStream;

/**
 * Represents a response from a NVCL data service check downloadwfs request
 * @author Josh Vote
 *
 */
public class WFSStatusResponse extends AbstractStreamResponse {
    /**
     * Creates a WFSStatusResponse
     * @param response The raw binary response
     * @param contentType The content type as a MIME string (expect html MIME's).
     */
    public WFSStatusResponse(InputStream response, String contentType) {
        super(response, contentType);
    }
}
