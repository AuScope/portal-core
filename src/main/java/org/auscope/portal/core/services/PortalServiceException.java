package org.auscope.portal.core.services;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Represents an exception thrown by an AuScope portal service.
 *
 * The exception contains extra information about the underlying
 * service that is causing problems
 *
 * @author Josh Vote
 *
 */
public class PortalServiceException extends Exception {
    /** The method that was executing when this exception was thrown */
    private HttpRequestBase rootMethod;

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     * @param cause Root cause of this exception
     */
    public PortalServiceException(HttpRequestBase rootMethod, Throwable cause) {
        super(cause);
        this.rootMethod = rootMethod;
    }

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     */
    public PortalServiceException(HttpRequestBase rootMethod) {
        super();
        this.rootMethod = rootMethod;
    }

    /**
     * Creates a new exception with the specified message
     */
    public PortalServiceException(String message) {
        super(message);
    }

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     * @param message Descriptive message
     */
    public PortalServiceException(HttpRequestBase rootMethod, String message) {
        super(message);
        this.rootMethod = rootMethod;
    }

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     * @param message Descriptive message
     * @param cause Root cause of this exception
     */
    public PortalServiceException(HttpRequestBase rootMethod, String message, Throwable cause) {
        super(message, cause);
        this.rootMethod = rootMethod;
    }

    /**
     * Gets the method that was executing when this exception was thrown
     * @return
     */
    public HttpRequestBase getRootMethod() {
        return rootMethod;
    }
}
