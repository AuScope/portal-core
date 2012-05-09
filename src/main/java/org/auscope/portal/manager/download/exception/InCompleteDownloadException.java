package org.auscope.portal.manager.download.exception;

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
