package org.auscope.portal.core.test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple Executor for testing
 * 
 * @author Josh Vote
 */
public class BasicThreadExecutor implements Executor {
    public static final int THREADPOOL_SIZE = 5;

    private ExecutorService threadPool = Executors.newFixedThreadPool(THREADPOOL_SIZE);

    @Override
    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public ExecutorService getExecutorService() {
        return threadPool;
    }

    public void setExecutorService(ExecutorService service) {
        threadPool = service;
    }
}