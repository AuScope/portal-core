package org.auscope.portal.core.test.jmock;

import java.util.concurrent.locks.ReentrantLock;

import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.api.ThreadingPolicy;

/**
 * Extension to the base JMock synchroniser to support some edge cases
 * with our custom JMock extensions
 * @author Josh Vote (CSIRO)
 *
 */
public class PortalSynchroniser implements ThreadingPolicy {
    private final ReentrantLock lock = new ReentrantLock(true);

    public Invokable synchroniseAccessTo(final Invokable mockObject) {
        return new Invokable() {
            public Object invoke(Invocation invocation) throws Throwable {
                return synchroniseInvocation(mockObject, invocation);
            }
        };
    }

    /**
     * Acquires (or reacquires) the lock used by this ThreadingPolicy
     * for the current thread. Blocks if the lock cannot be acquired.
     *
     * Ensure that a call to releaseLock is made if acquring locks
     * @throws InterruptedException
     */
    public void acquireLock() throws InterruptedException {
        lock.lock();
    }

    /**
     * Releases the lock used by this ThreadingPolicy for the current thread.
     */
    public void releaseLock() {
        lock.unlock();
    }

    private Object synchroniseInvocation(Invokable mockObject, Invocation invocation) throws Throwable {
        acquireLock();
        try {
            return mockObject.invoke(invocation);
        } finally {
            releaseLock();
        }
    }
}
