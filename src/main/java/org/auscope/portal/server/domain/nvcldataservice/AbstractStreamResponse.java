package org.auscope.portal.server.domain.nvcldataservice;

import java.io.InputStream;

/**
 * Abstract parts common to a typical HTTP Stream response
 * @author Josh Vote
 *
 */
public abstract class AbstractStreamResponse {
    protected InputStream response;
    protected String contentType;

    /**
     * Prepares a AbstractStreamResponse
     * @param response The raw binary response
     * @param contentType The content type as a MIME string (expect either html or image MIME's).
     */
    protected AbstractStreamResponse(InputStream response, String contentType) {
        this.response = response;
        this.contentType = contentType;
    }

    /**
     * Gets the raw response stream
     * @return
     */
    public InputStream getResponse() {
        return response;
    }

    /**
     * Sets the response
     * @param response
     */
    public void setResponse(InputStream response) {
        this.response = response;
    }

    /**
     * Gets the content type of the response as a MIME string
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the response as a MIME string
     * @param contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }



}
