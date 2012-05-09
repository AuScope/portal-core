package org.auscope.portal.server.domain.nvcldataservice;

import java.io.InputStream;

/**
 * Represents a response from a download TSG request.
 * @author Josh Vote
 *
 */
public class TSGDownloadResponse extends AbstractStreamResponse {
    /**
     * Creates a TSGDownloadResponse
     * @param response The raw binary response
     * @param contentType The content type as a MIME string (expect html MIME's).
     */
    public TSGDownloadResponse(InputStream response, String contentType) {
        super(response, contentType);
    }
}
