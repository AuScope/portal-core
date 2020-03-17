package org.auscope.portal.core.test.jmock;

import org.jmock.api.Invocation;
import org.jmock.lib.action.ReturnValueAction;

/**
 * A simple extension on a JMock ReturnValue that adds a delay before the object is returned
 *
 * @author vot002
 *
 */
public class DelayedReturnValueAction extends ReturnValueAction {
    private long delayMs;
    private PortalRuleMockery context;

    public DelayedReturnValueAction(long delayMs, Object returnValue, PortalRuleMockery context) {
        super(returnValue);
        this.delayMs = delayMs;
        this.context = context;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public Object invoke(Invocation i) throws Throwable {
        if (context.getThreadingPolicy() instanceof PortalSynchroniser) {
            //If we are using a supported ThreadingPolicy, temporarily release our lock while we sleep
            PortalSynchroniser sync = (PortalSynchroniser) context.getThreadingPolicy();
            try {
                sync.releaseLock();
                Thread.sleep(delayMs);
            } finally {
                sync.acquireLock();
            }
        } else {
            Thread.sleep(delayMs);
        }

        return super.invoke(i);
    }
}
