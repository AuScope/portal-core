package org.auscope.portal.server.web;

/**
 * The generic base exception that is throw as part of calls to DistributeHTTPServiceCaller
 *
 * These exceptions will always encapsulate some form of http exception that was thrown in another thread.
 * @author Josh Vote
 *
 */
public class DistributedHTTPServiceCallerException extends RuntimeException {
    public DistributedHTTPServiceCallerException(Throwable t) {
        super(t);
    }
}
