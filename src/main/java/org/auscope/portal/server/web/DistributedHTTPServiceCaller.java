package org.auscope.portal.server.web;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.web.service.HttpServiceCaller;

/**
 * An iterator class for calling a series of HTTP Methods and returning the results in an iterable instance
 *
 * The results will be made available to the iterator as their responses become available. This means
 * that the responses will be returned with no guarantee of order.
 *
 * Ensure that beginCallingServices is run before any of the iterator methods are called.
 *
 * @author Josh Vote
 *
 */
public class DistributedHTTPServiceCaller implements Iterator<InputStream> {

    protected final Log log = LogFactory.getLog(getClass());

    private List<ServiceCallStatus> statusList;

    public DistributedHTTPServiceCaller(List<HttpMethodBase> methods, HttpServiceCaller serviceCaller) {
        statusList = new ArrayList<ServiceCallStatus>(methods.size());

        for (HttpMethodBase method : methods) {
            statusList.add(new ServiceCallStatus(this, method, serviceCaller));
        }
    }

    /**
     * Call this method before using any iterator methods.
     *
     * This method will ensure that all underlying http service calls are enqueued as per the specified executor.
     * @param executor
     */
    public synchronized void beginCallingServices(Executor executor) {
        for (ServiceCallStatus status : statusList) {
            executor.execute(status);
        }

    }

    /**
     * Non blocking function - returns true if there are more HTTP streams to extract
     */
    @Override
    public synchronized boolean hasNext() {
        for (ServiceCallStatus status : statusList) {
            if (!status.isIterated()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Blocking function - will return the next input stream that is available ONLY blocking
     * if there is no input stream that is readily available.
     *
     * Input streams that are ready will be returned ahead of input streams that are yet to return data.
     *
     *
     */
    @Override
    public synchronized InputStream next() throws DistributedHTTPServiceCallerException {
        //Find a service that hasn't been iterated AND has returned data
        for (ServiceCallStatus status : statusList) {
            synchronized(status) {
                if (!status.isIterated() && !status.isRunning()) {
                    status.setIterated(true);
                    InputStream data = status.getResultingData();
                    if (data == null) {
                        throw new DistributedHTTPServiceCallerException(status.getResultingError());
                    } else {
                        return data;
                    }
                }
            }
        }

        //Now we are in a tricky situation - we need to wait for the first of our outstanding service calls to return
        //Firstly ensure we have a value to return
        if (!hasNext()) {
            return null;
        }

        //And then make this thread block until notified - each of the child statuses will call notify on this instance
        //as they finish - they will also acquire the lock of this instance before calling notify so we can be certain
        //that threads will NOT 'finish' before we call this.wait().
        try {
            this.wait();
        } catch (InterruptedException e) {
            log.debug(e);
        }
        return next();
    }


    /**
     * Throws a UnsupportedOperationException
     */
    @Override
    public synchronized void remove() {
        throw new UnsupportedOperationException();
    }


    /**
     * Call this function if do not intend to use any further iterator functions. It will prevent
     * any threads yet to start from making HTTP connections.
     *
     * Any running threads will be interrupted
     *
     * No guarantee is made that 'cancelled' threads WONT attempt to make a connection.
     */
    public void dispose() {
        for (ServiceCallStatus status : statusList) {
            status.interrupt();
        }
    }

    /**
     * Utility class for lumping the request status information for a single method into a single object
     */
    private class ServiceCallStatus extends Thread {
        private HttpMethodBase method;
        private HttpServiceCaller serviceCaller;
        private DistributedHTTPServiceCaller parent;
        private InputStream resultingData;
        private Exception resultingError;
        private volatile boolean running;
        private volatile boolean iterated;
        private volatile boolean abortStart;

        public ServiceCallStatus(DistributedHTTPServiceCaller parent, HttpMethodBase method,
                HttpServiceCaller serviceCaller) {
            this.parent = parent;
            this.running = true;
            this.method = method;
            this.serviceCaller = serviceCaller;
        }

        public boolean isIterated() {
            return iterated;
        }

        @Override
        public void interrupt() {
            this.abortStart = true;
            super.interrupt();
        }

        /**
         * Returns whether this thread is running
         * @return
         */
        public synchronized boolean isRunning() {
            return running;
        }

        private synchronized void setRunning(boolean running) {
            this.running = running;
        }

        public void setIterated(boolean iterated) {
            this.iterated = iterated;
        }

        /**
         * Gets the data stream that resulted from a succesful call (null if an error has occured)
         * This function will block if this thread is running
         * @return
         */
        public InputStream getResultingData() {
            return resultingData;
        }


        /**
         * Gets the underlying exception that resulted from a failed call (null if the call was successful)
         * This function will block if this thread is running
         * @return
         */
        public Exception getResultingError() {
            return resultingError;
        }


        @Override
        public void run() {
            //If this thread has been interrupted before it started running - don't start processing.
            if (this.abortStart) {
                return;
            }

            this.setRunning(true);

            InputStream data = null;
            Exception error = null;

            try {
                data = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());
            } catch (Exception e) {
                error = e;
            } finally {
                //Acquire the lock on our parent object
                //We sync to ensure the parent isn't halfway through checking our statuses
                synchronized(parent) {
                    //After locking the parent we can update our own status
                    synchronized(this) {
                        this.setRunning(false);
                        this.resultingData = data;
                        this.resultingError = error;

                        parent.notifyAll(); //if our parent is waiting for a service to return - lets let them know to check again
                    }
                }
            }
        }

    }
}
