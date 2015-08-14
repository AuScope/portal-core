package org.auscope.portal.core.server.http.download;

/**
 * Exception that is thrown when a user request for a download but download is currently incomplete
 * 
 * @author tey006
 *
 */
public class InCompleteDownloadException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -2995267671038679968L;

    public InCompleteDownloadException() {
        super();
    }

    public InCompleteDownloadException(String msg) {
        super(msg);
    }
}
