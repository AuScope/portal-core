package org.auscope.portal.server.domain.nvcldataservice;

import java.io.InputStream;

/**
 * Represents a response from a check download status TSG request.
 * @author Josh Vote
 *
 */
public class TSGStatusResponse extends AbstractStreamResponse {
    /**
     * Creates a TSGStatusResponse
     * @param response The raw binary response
     * @param contentType The content type as a MIME string (expect html MIME's).
     */
    public TSGStatusResponse(InputStream response, String contentType) {
        super(response, contentType);
    }
}
