package org.auscope.portal.core.util.structure;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.auscope.portal.core.services.PortalServiceException;

/**
 * A queue data structure that holds Jobs
 *
 * @author tey006
 */
public abstract class AbstractJobQueue implements Runnable{

    //VT: unable to decide between a ConcurrentLinkedQueue vs a LinkedBlockingQueue.
    protected ConcurrentLinkedQueue<Job> queue;


    public AbstractJobQueue(){
        queue= new ConcurrentLinkedQueue<Job>();

    }

    public AbstractJobQueue(ConcurrentLinkedQueue<Job> queue){
        this.queue= queue;

    }

    public boolean hasJob(){
        return !queue.isEmpty();
    }


    public boolean runJob() throws PortalServiceException{
        return this.queue.peek().run();
    }

    public Job removeJob(){
        return this.queue.remove();
    }


    /**
     * Implement how the job is run. What is determined as successful and remove the job from the queue
     * or what is deemed unsuccessful and leave the job in the queue.
     * @return
     */
    public abstract void manageJob() ;


    public void addJob(Job job){
        this.queue.add(job);
    }

    public void run(){
        this.manageJob();
    }

    public int size(){
        return this.queue.size();
    }

    public void clear(){
        this.queue.clear();
    }

    public boolean remove(Job o){
        return this.queue.remove(o);
    }


}
