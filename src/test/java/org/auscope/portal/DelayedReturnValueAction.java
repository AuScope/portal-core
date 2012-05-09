package org.auscope.portal;

import org.jmock.api.Invocation;
import org.jmock.lib.action.ReturnValueAction;

/**
 * A simple extension on a JMock ReturnValue that adds a delay before the object is returned
 * @author vot002
 *
 */
public class DelayedReturnValueAction extends ReturnValueAction {
    private long delayMs;
    public DelayedReturnValueAction(long delayMs, Object returnValue) {
        super(returnValue);
        this.delayMs = delayMs;
    }

    @Override
    public Object invoke(Invocation i) throws Throwable {
        Thread.sleep(delayMs);
        return super.invoke(i);
    }
}
