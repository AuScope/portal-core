package org.auscope.portal.core.test.jmock;

import org.jmock.api.Invocation;
import org.jmock.lib.action.ThrowAction;

/**
 * A simple extension on ThrowAction that adds a delay before throwing the exception
 * 
 * @author Josh Vote
 *
 */
public class DelayedThrowAction extends ThrowAction {

    private long delayMs;

    /**
     * Creates a new instance
     * 
     * @param throwable
     * @param delayMs
     *            The delay in milli seconds
     */
    public DelayedThrowAction(Throwable throwable, long delayMs) {
        super(throwable);
        this.delayMs = delayMs;
    }

    @Override
    public Object invoke(Invocation i) throws Throwable {
        Thread.sleep(delayMs);
        return super.invoke(i);
    }
}
