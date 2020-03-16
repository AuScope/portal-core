package org.auscope.portal.core.test.jmock;

import org.jmock.api.ThreadingPolicy;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.internal.SingleThreadedPolicy;
import org.jmock.lib.concurrent.Synchroniser;

/**
 * Extension to the normal Mockery for exposing some internal fields
 * @author Josh Vote (CSIRO)
 *
 */
public class PortalRuleMockery extends JUnitRuleMockery {
    private ThreadingPolicy portalThreadingPolicy = new SingleThreadedPolicy();

    /**
     * Changes the policy by which the Mockery copes with multiple threads.
     *
     *  The default policy throws an exception if the Mockery is called from different
     *  threads.
     *
     *  @see Synchroniser
     *  @param threadingPolicy how to handle different threads.
     */
    @Override
    public void setThreadingPolicy(ThreadingPolicy threadingPolicy) {
        super.setThreadingPolicy(threadingPolicy);
        portalThreadingPolicy = threadingPolicy;
    }

    public ThreadingPolicy getThreadingPolicy() {
        return portalThreadingPolicy;
    }
}
