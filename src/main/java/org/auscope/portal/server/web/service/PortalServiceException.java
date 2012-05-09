package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpMethodBase;

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
    private HttpMethodBase rootMethod;

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     * @param cause Root cause of this exception
     */
    public PortalServiceException(HttpMethodBase rootMethod, Throwable cause) {
        super(cause);
        this.rootMethod = rootMethod;
    }

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     */
    public PortalServiceException(HttpMethodBase rootMethod) {
        super();
        this.rootMethod = rootMethod;
    }

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     * @param message Descriptive message
     */
    public PortalServiceException(HttpMethodBase rootMethod, String message) {
        super(message);
        this.rootMethod = rootMethod;
    }

    /**
     * Creates a new exception
     * @param rootMethod The method that was being run when this exception was thrown
     * @param message Descriptive message
     * @param cause Root cause of this exception
     */
    public PortalServiceException(HttpMethodBase rootMethod, String message, Throwable cause) {
        super(message, cause);
        this.rootMethod = rootMethod;
    }

    /**
     * Gets the method that was executing when this exception was thrown
     * @return
     */
    public HttpMethodBase getRootMethod() {
        return rootMethod;
    }
}
