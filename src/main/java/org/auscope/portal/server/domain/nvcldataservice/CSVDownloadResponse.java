package org.auscope.portal.server.domain.nvcldataservice;

import java.io.InputStream;

/**
 * Represents a response from a CSW download request
 * @author Josh Vote
 *
 */
public class CSVDownloadResponse extends AbstractStreamResponse {
    /**
     * Creates a CSVDownloadResponse
     * @param response The raw binary response
     * @param contentType The content type as a MIME string (expect csv MIME's).
     */
    public CSVDownloadResponse(InputStream response, String contentType) {
        super(response, contentType);
    }
}
