package org.auscope.portal.server.domain.nvcldataservice;

import java.io.InputStream;

/**
 * Represents a response from a NVCL data service downloadwfs request
 * @author Josh Vote
 *
 */
public class WFSDownloadResponse extends AbstractStreamResponse {
    /**
     * Creates a WFSDownloadResponse
     * @param response The raw binary response
     * @param contentType The content type as a MIME string (expect html MIME's).
     */
    public WFSDownloadResponse(InputStream response, String contentType) {
        super(response, contentType);
    }
}
