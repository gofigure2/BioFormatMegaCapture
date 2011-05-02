/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.multithreadedplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Handles named data passing among nodes.
 *
 * @author Aivar Grislis
 */
public class NodeScheduler {
    private static NodeScheduler INSTANCE = null;
    private static final Object m_synchObject = new Object();
    private volatile boolean m_quit;
    private Map<String, BlockingQueue<Object>> m_queueMap = new HashMap<String, BlockingQueue<Object>>();

    /**
     * Singleton, with private constructor.
     */
    private NodeScheduler() { }

    /**
     * Gets the singleton.
     *
     * @return singleton instance
     */
    public static synchronized NodeScheduler getInstance() {
       if (null == INSTANCE) {
            INSTANCE = new NodeScheduler();
       }
       return INSTANCE;
    }

    /**
     * Tears down the chained nodes.
     */
    public void quit() {
        m_quit = true;
    }

    /**
     * Chains the named data from one plugin to another.
     *
     * @param outNode source node
     * @param outName source node's name
     * @param inNode destination node
     * @param inName destination node's name
     */
    public void chain(IScheduledNode outNode, String outName, IScheduledNode inNode, String inName) {
       // build a full destination name tied to this particular destination node instance
       String fullInName = inNode.uniqueInstance(inName);

       // make sure there is a queue for destination node + name
       getQueue(fullInName);

       // within the source node instance, save the association of its output name with destination node + name
       outNode.associate(outName, fullInName);
    }

    /**
     * Passes data to destination node + name.
     * 
     * @param fullInName
     * @param data
     */
    public void put(String fullInName, Object data) {
        boolean success = false;
        BlockingQueue<Object> queue = getQueue(fullInName);
        while (!success) {
            try {
                success = queue.offer(data, 100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Put interrupted");
            }
            if (m_quit) {
                throw new TeardownException("Teardown");
            }
        }
    }

    /**
     * Gets data for destination node + name.
     *
     * @param fullInName
     * @return data
     */
    public Object get(String fullInName) {
        Object data = null;
        BlockingQueue<Object> queue = getQueue(fullInName);
        while (null == data) {
            try {
                data = queue.poll(100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Get interrupted");
            }
            if (m_quit) {
                throw new TeardownException("Teardown");
            }
        }
        return data;
    }

    /**
     * Gets the queue for a given destination node + name.  Creates it if
     * necessary.
     *
     * @param fullInName
     * @return the queue
     */
    private BlockingQueue<Object> getQueue(String fullInName) {
        BlockingQueue<Object> queue = null;
        synchronized (m_synchObject) {
            queue = m_queueMap.get(fullInName);
            if (null == queue) {
                queue = new LinkedBlockingQueue<Object>();
                //queue = new LinkedBlockingQueue<Object>(1);
                m_queueMap.put(fullInName, queue);
            }
        }
        return queue;
    }
}
