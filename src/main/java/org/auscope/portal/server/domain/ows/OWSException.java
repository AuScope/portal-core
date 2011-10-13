package org.auscope.portal.server.domain.ows;

/**
 * Represents an exception response returned from an OGC OWS
 * @author vot002
 *
 */
public class OWSException extends Exception {

	private static final long serialVersionUID = 1L;

	public OWSException(String message) {
        super(message);
    }

    public OWSException(String message, Throwable t) {
        super(message, t);
    }

    public OWSException (Throwable t) {
        super(t);
    }
}
