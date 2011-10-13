package org.auscope.portal.pressuredb;

/**
 * Base exception thrown by PressureDB specific classes
 * @author JoshVote
 *
 */
public class PressureDBException extends Exception {

	private static final long serialVersionUID = 1L;

	public PressureDBException() {
        super();
    }

    public PressureDBException(String message) {
        super(message);
    }

    public PressureDBException(String message, Throwable t) {
        super(message, t);
    }
}
